package com.thelazybattley.hiddenridge.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.Timeline
import com.thelazybattley.hiddenridge.core.domain.model.allowedShift
import com.thelazybattley.hiddenridge.core.domain.model.canChangeHouse
import com.thelazybattley.hiddenridge.core.domain.usecase.MoveBookingUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TimelineViewModel @Inject constructor(
    observeTimeline: ObserveTimelineUseCase,
    private val moveBooking: MoveBookingUseCase,
) : ViewModel() {

    private var latest: Timeline? = null
    private val draft = MutableStateFlow<MoveDraft?>(null)
    private val isSaving = MutableStateFlow(false)
    private val movedEvents = Channel<MovedBooking>(Channel.BUFFERED)

    /** Each booking put down somewhere new, once, for the screen to confirm. */
    internal val moved: Flow<MovedBooking> = movedEvents.receiveAsFlow()

    internal val uiState: StateFlow<TimelineUiState> = combine(
        observeTimeline().onEach { latest = it },
        draft,
        isSaving,
    ) { timeline, move, saving ->
        TimelineUiState.Ready(timeline = timeline.toUi(move), move = timeline.moveUi(move, saving))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TimelineUiState.Loading,
    )

    internal fun onAction(action: TimelineAction) {
        when (action) {
            is TimelineAction.StartMove -> {
                isSaving.value = false
                draft.value = MoveDraft(bookingId = action.bookingId)
            }
            is TimelineAction.DragTo -> place(action.dayShift) { action.houseId }
            is TimelineAction.Nudge -> nudge(action.days, action.houses)
            TimelineAction.CancelMove -> if (!isSaving.value) draft.value = null
            TimelineAction.SaveMove -> save()
        }
    }

    private fun nudge(days: Int, houses: Int) {
        val current = draft.value ?: return
        place(current.dayShift + days) { moving ->
            if (houses == 0) return@place current.houseId
            val order = latest?.houseOrder().orEmpty()
            val from = order.indexOfFirst { it.id == moving.moved.houseIds.singleOrNull() }
            if (from < 0) current.houseId else order[(from + houses).coerceIn(0, order.lastIndex)].id
        }
    }

    /** Moves the draft to [dayShift], kept within what the booking allows, and the house [houseFor] picks. */
    private fun place(dayShift: Int, houseFor: (MovingBooking) -> String?) {
        if (isSaving.value) return
        val timeline = latest ?: return
        draft.update { current ->
            val moving = timeline.moving(current) ?: return@update current
            val allowed = moving.original.allowedShift(timeline.today, timeline.window.endExclusive)
            val houseId = houseFor(moving)
                ?.takeIf { id -> moving.original.canChangeHouse && timeline.houses.any { it.id == id } }
            current?.copy(dayShift = dayShift.coerceIn(allowed), houseId = houseId)
        }
    }

    private fun save() {
        val timeline = latest ?: return
        val moving = timeline.moving(draft.value) ?: return
        if (isSaving.value) return
        isSaving.value = true
        viewModelScope.launch {
            val result = moveBooking(moving.original, moving.moved.checkIn, moving.moved.houseIds)
            if (result is MoveBookingUseCase.Result.Moved) {
                val names = timeline.houses.associateBy(House::id)
                movedEvents.send(
                    MovedBooking(
                        guestName = result.booking.guestName,
                        checkIn = result.booking.checkIn,
                        checkOut = result.booking.checkOut,
                        houseNames = result.booking.houseIds.mapNotNull { names[it]?.name },
                    ),
                )
                draft.value = null
            }
            // A clash or problem is already showing on the move bar, from the latest bookings.
            isSaving.value = false
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
