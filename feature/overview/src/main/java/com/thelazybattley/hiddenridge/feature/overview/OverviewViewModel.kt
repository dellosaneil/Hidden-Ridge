package com.thelazybattley.hiddenridge.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thelazybattley.hiddenridge.core.domain.usecase.CheckInGuestUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.CheckOutGuestUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.DecideHoldUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveBookingLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OverviewViewModel @Inject constructor(
    observeBookingLog: ObserveBookingLogUseCase,
    private val checkInGuest: CheckInGuestUseCase,
    private val checkOutGuest: CheckOutGuestUseCase,
    private val decideHold: DecideHoldUseCase,
) : ViewModel() {

    internal val uiState: StateFlow<OverviewUiState> = observeBookingLog()
        .map<_, OverviewUiState> { log -> OverviewUiState.Ready(log.toOverview()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = OverviewUiState.Loading,
        )

    private val doneActions = Channel<OverviewAction>(Channel.BUFFERED)

    /** Each action once it's saved, for a confirmation that offers undo. */
    internal val done: Flow<OverviewAction> = doneActions.receiveAsFlow()

    internal fun perform(action: OverviewAction) {
        viewModelScope.launch {
            when (action) {
                is OverviewAction.CheckIn -> checkInGuest(action.move.bookingId)
                is OverviewAction.CheckOut -> checkOutGuest(action.move.bookingId)
                is OverviewAction.Confirm -> decideHold.confirm(action.decision.booking)
                is OverviewAction.Release -> decideHold.release(action.decision.booking)
            }
            doneActions.send(action)
        }
    }

    internal fun undo(action: OverviewAction) {
        viewModelScope.launch {
            when (action) {
                is OverviewAction.CheckIn -> checkInGuest.undo(action.move.bookingId)
                is OverviewAction.CheckOut -> checkOutGuest.undo(action.move.bookingId)
                is OverviewAction.Confirm -> decideHold.reopen(action.decision.booking)
                is OverviewAction.Release -> decideHold.reopen(action.decision.booking)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
