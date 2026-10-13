package com.thelazybattley.hiddenridge.feature.bookingdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingDetails
import com.thelazybattley.hiddenridge.core.domain.usecase.CancelBookingUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.ConfirmBookingUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.DeleteBookingUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveBookingDetailsUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.PutBookingOnHoldUseCase
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.BookingDetailsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class BookingDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeBookingDetails: ObserveBookingDetailsUseCase,
    private val putBookingOnHold: PutBookingOnHoldUseCase,
    private val confirmBooking: ConfirmBookingUseCase,
    private val cancelBooking: CancelBookingUseCase,
    private val deleteBooking: DeleteBookingUseCase,
) : ViewModel() {

    internal val bookingId: String = savedStateHandle.toRoute<BookingDetailsRoute>().bookingId

    private var latest: BookingDetails? = null
    private val confirmation = MutableStateFlow<PendingConfirmation?>(null)
    private val closed = MutableStateFlow<BookingDetailsUiState.Closed?>(null)

    internal val uiState: StateFlow<BookingDetailsUiState> = combine(
        observeBookingDetails(bookingId).onEach { latest = it },
        confirmation,
        closed,
    ) { details, pending, closedState ->
        when {
            closedState != null -> closedState
            details == null -> BookingDetailsUiState.Closed(outcome = null, details = null)
            else -> BookingDetailsUiState.Ready(details = details.toUi(), confirmation = pending)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = BookingDetailsUiState.Loading,
    )

    internal fun onAction(action: BookingDetailsAction) {
        when (action) {
            BookingDetailsAction.ToggleHold -> toggleHold()
            BookingDetailsAction.RequestCancel -> confirmation.value = PendingConfirmation.CANCEL
            BookingDetailsAction.RequestDelete -> confirmation.value = PendingConfirmation.DELETE
            BookingDetailsAction.DismissConfirmation -> confirmation.value = null
            BookingDetailsAction.ConfirmCancel -> close(Outcome.CANCELLED) { cancelBooking(it) }
            BookingDetailsAction.ConfirmDelete -> close(Outcome.DELETED) { deleteBooking(it) }
            // Navigation is the screen's job; nothing to change here.
            BookingDetailsAction.EditDetails -> Unit
        }
    }

    private fun toggleHold() {
        val booking = latest?.booking ?: return
        viewModelScope.launch {
            if (booking.isOnHold) confirmBooking(booking) else putBookingOnHold(booking)
        }
    }

    /**
     * Closes the sheet straight away, then writes. The write must finish even though closing the
     * sheet clears this ViewModel, so it runs without cancellation.
     */
    private fun close(outcome: Outcome, write: suspend (Booking) -> Unit) {
        val details = latest ?: return
        confirmation.value = null
        closed.value = BookingDetailsUiState.Closed(outcome = outcome, details = details.toUi())
        viewModelScope.launch {
            withContext(NonCancellable) { write(details.booking) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
