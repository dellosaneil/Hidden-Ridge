package com.thelazybattley.hiddenridge.feature.availability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveAvailabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    observeAvailability: ObserveAvailabilityUseCase,
) : ViewModel() {

    private val form = MutableStateFlow(AvailabilityForm())

    internal val uiState: StateFlow<AvailabilityUiState> = combine(observeAvailability(), form) { availability, form ->
        AvailabilityUiState.Ready(form.toUi(availability))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AvailabilityUiState.Loading,
    )

    internal fun onAction(action: AvailabilityAction) {
        when (action) {
            is AvailabilityAction.SetFilter -> form.update { it.copy(filter = action.filter) }
            is AvailabilityAction.PickDate -> form.update { it.picking(action.date) }
            is AvailabilityAction.SetNights -> form.update { it.withNights(action.nights) }
            is AvailabilityAction.ShowMonth -> form.update { it.copy(visibleMonth = action.month) }
            AvailabilityAction.ShowNextOpening -> showNextOpening()
            AvailabilityAction.Clear -> form.update { AvailabilityForm(filter = it.filter, visibleMonth = it.visibleMonth) }
            // Navigation is the screen's job; nothing to change here.
            is AvailabilityAction.Book,
            is AvailabilityAction.ViewBooking,
            -> Unit
        }
    }

    private fun showNextOpening() {
        val next = (uiState.value as? AvailabilityUiState.Ready)?.availability?.result?.nextOpening ?: return
        form.update { it.startingOn(next) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
