package com.thelazybattley.hiddenridge.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TimelineViewModel @Inject constructor(
    observeTimeline: ObserveTimelineUseCase,
) : ViewModel() {

    internal val uiState: StateFlow<TimelineUiState> = observeTimeline()
        .map<_, TimelineUiState> { timeline -> TimelineUiState.Ready(timeline.toUi()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = TimelineUiState.Loading,
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
