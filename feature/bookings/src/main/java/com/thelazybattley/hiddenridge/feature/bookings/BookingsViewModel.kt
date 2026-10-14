package com.thelazybattley.hiddenridge.feature.bookings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveBookingLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class BookingsViewModel @Inject constructor(
    observeBookingLog: ObserveBookingLogUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val tab = savedStateHandle.getStateFlow(KEY_TAB, BookingsTab.UPCOMING)
    private val filter = savedStateHandle.getStateFlow(KEY_FILTER, BookingsFilter.ALL)

    internal val uiState: StateFlow<BookingsUiState> = combine(observeBookingLog(), tab, filter) { log, tab, filter ->
        BookingsUiState.Ready(log.toUi(tab, filter, WeekFields.of(Locale.getDefault()).firstDayOfWeek))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = BookingsUiState.Loading,
    )

    /** Each tab starts unfiltered, as its counts differ. */
    internal fun selectTab(tab: BookingsTab) {
        savedStateHandle[KEY_TAB] = tab
        savedStateHandle[KEY_FILTER] = BookingsFilter.ALL
    }

    internal fun selectFilter(filter: BookingsFilter) {
        savedStateHandle[KEY_FILTER] = filter
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val KEY_TAB = "tab"
        const val KEY_FILTER = "filter"
    }
}
