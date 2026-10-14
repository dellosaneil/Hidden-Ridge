package com.thelazybattley.hiddenridge.feature.houses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveHouseRosterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HousesViewModel @Inject constructor(
    observeHouseRoster: ObserveHouseRosterUseCase,
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

    internal fun selectFilter(filter: HousesFilter) {
        savedStateHandle[KEY_FILTER] = filter
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val KEY_FILTER = "filter"
    }
}
