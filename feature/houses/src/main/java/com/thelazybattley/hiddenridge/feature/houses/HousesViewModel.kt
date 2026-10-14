package com.thelazybattley.hiddenridge.feature.houses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thelazybattley.hiddenridge.core.domain.usecase.CheckInGuestUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.CheckOutGuestUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveHouseRosterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HousesViewModel @Inject constructor(
    observeHouseRoster: ObserveHouseRosterUseCase,
    private val checkInGuest: CheckInGuestUseCase,
    private val checkOutGuest: CheckOutGuestUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val filter = savedStateHandle.getStateFlow(KEY_FILTER, HousesFilter.ALL)

    internal val uiState: StateFlow<HousesUiState> = combine(observeHouseRoster(), filter) { roster, filter ->
        HousesUiState.Ready(roster.toUi(filter, WeekFields.of(Locale.getDefault()).firstDayOfWeek))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HousesUiState.Loading,
    )

    private val doneActions = Channel<HouseAction>(Channel.BUFFERED)

    /** Each check-in or check-out once it's saved, for a confirmation that offers undo. */
    internal val done: Flow<HouseAction> = doneActions.receiveAsFlow()

    internal fun selectFilter(filter: HousesFilter) {
        savedStateHandle[KEY_FILTER] = filter
    }

    internal fun perform(action: HouseAction) {
        viewModelScope.launch {
            when (action) {
                is HouseAction.CheckIn -> checkInGuest(action.bookingId)
                is HouseAction.CheckOut -> checkOutGuest(action.bookingId)
            }
            doneActions.send(action)
        }
    }

    internal fun undo(action: HouseAction) {
        viewModelScope.launch {
            when (action) {
                is HouseAction.CheckIn -> checkInGuest.undo(action.bookingId)
                is HouseAction.CheckOut -> checkOutGuest.undo(action.bookingId)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val KEY_FILTER = "filter"
    }
}
