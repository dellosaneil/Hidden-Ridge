package com.thelazybattley.hiddenridge.feature.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thelazybattley.hiddenridge.core.designsystem.theme.HiddenRidgeTheme
import com.thelazybattley.hiddenridge.feature.timeline.components.BackToTodayButton
import com.thelazybattley.hiddenridge.feature.timeline.components.MonthRail
import com.thelazybattley.hiddenridge.feature.timeline.components.TimelineDimens
import com.thelazybattley.hiddenridge.feature.timeline.components.TimelineGrid
import com.thelazybattley.hiddenridge.feature.timeline.components.TimelineLegend
import com.thelazybattley.hiddenridge.feature.timeline.components.TimelineTopBar
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** Days of context kept to the left when jumping to today. */
private const val DAYS_BEFORE_TODAY = 1

/** The visible month is the one a few days into the viewport, not the half-visible first column. */
private const val ACTIVE_MONTH_PROBE_DAYS = 3

@Composable
internal fun TimelineScreen(
    onBookingClick: (bookingId: String) -> Unit,
    onEmptyDayClick: (houseId: String, date: LocalDate) -> Unit,
    onNewBookingClick: () -> Unit,
    onCheckAvailabilityClick: () -> Unit,
    onMonthTitleClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TimelineScreenContent(
        uiState = uiState,
        onBookingClick = onBookingClick,
        onEmptyDayClick = onEmptyDayClick,
        onNewBookingClick = onNewBookingClick,
        onCheckAvailabilityClick = onCheckAvailabilityClick,
        onMonthTitleClick = onMonthTitleClick,
        modifier = modifier,
    )
}

@Composable
internal fun TimelineScreenContent(
    uiState: TimelineUiState,
    onBookingClick: (bookingId: String) -> Unit,
    onEmptyDayClick: (houseId: String, date: LocalDate) -> Unit,
    onNewBookingClick: () -> Unit,
    onCheckAvailabilityClick: () -> Unit,
    onMonthTitleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        when (uiState) {
            TimelineUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is TimelineUiState.Ready -> TimelineReady(
                timeline = uiState.timeline,
                onBookingClick = onBookingClick,
                onEmptyDayClick = onEmptyDayClick,
                onNewBookingClick = onNewBookingClick,
                onCheckAvailabilityClick = onCheckAvailabilityClick,
                onMonthTitleClick = onMonthTitleClick,
            )
        }
    }
}

@Composable
private fun TimelineReady(
    timeline: TimelineUi,
    onBookingClick: (bookingId: String) -> Unit,
    onEmptyDayClick: (houseId: String, date: LocalDate) -> Unit,
    onNewBookingClick: () -> Unit,
    onCheckAvailabilityClick: () -> Unit,
    onMonthTitleClick: () -> Unit,
) {
    val dayPx = with(LocalDensity.current) { TimelineDimens.DayWidth.toPx() }
    val horizontalScroll = rememberScrollState(initial = scrollOffsetFor(timeline.todayIndex - DAYS_BEFORE_TODAY, dayPx))
    val scope = rememberCoroutineScope()
    val scrollToDay: (Int) -> Unit = { day ->
        scope.launch { horizontalScroll.animateScrollTo(scrollOffsetFor(day, dayPx)) }
    }
    val scrollToToday = { scrollToDay(timeline.todayIndex - DAYS_BEFORE_TODAY) }

    val activeMonthIndex by remember(timeline, dayPx) {
        derivedStateOf { timeline.monthIndexAt(firstVisibleDay(horizontalScroll, dayPx) + ACTIVE_MONTH_PROBE_DAYS) }
    }
    val todayVisible by remember(timeline.todayIndex, dayPx) {
        derivedStateOf { isDayVisible(timeline.todayIndex, horizontalScroll, dayPx) }
    }
    val todayIsAhead by remember(timeline.todayIndex, dayPx) {
        derivedStateOf { timeline.todayIndex > firstVisibleDay(horizontalScroll, dayPx) }
    }

    Column(Modifier.fillMaxSize()) {
        TimelineTopBar(
            houseCount = timeline.houseCount,
            visibleMonth = timeline.months[activeMonthIndex].yearMonth,
            onMonthTitleClick = onMonthTitleClick,
            onCheckAvailabilityClick = onCheckAvailabilityClick,
            onNewBookingClick = onNewBookingClick,
        )
        MonthRail(
            months = timeline.months,
            activeMonthIndex = activeMonthIndex,
            today = timeline.today,
            onTodayClick = scrollToToday,
            onMonthClick = { index -> scrollToDay(timeline.months[index].startIndex) },
        )
        TimelineLegend()
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Box(Modifier.weight(1f)) {
            TimelineGrid(
                timeline = timeline,
                horizontalScroll = horizontalScroll,
                onBookingClick = onBookingClick,
                onEmptyDayClick = onEmptyDayClick,
                modifier = Modifier.fillMaxSize(),
            )
            BackToTodayOverlay(
                visible = !todayVisible,
                today = timeline.today,
                todayIsAhead = todayIsAhead,
                onClick = scrollToToday,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun BackToTodayOverlay(
    visible: Boolean,
    today: LocalDate,
    todayIsAhead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier,
    ) {
        BackToTodayButton(today = today, todayIsAhead = todayIsAhead, onClick = onClick)
    }
}

private fun scrollOffsetFor(dayIndex: Int, dayPx: Float): Int = (dayIndex.coerceAtLeast(0) * dayPx).roundToInt()

private fun firstVisibleDay(scroll: ScrollState, dayPx: Float): Int = (scroll.value / dayPx).toInt()

/** Before the first layout the viewport is unknown; treat today as visible so nothing flickers in. */
private fun isDayVisible(dayIndex: Int, scroll: ScrollState, dayPx: Float): Boolean {
    if (scroll.viewportSize == 0) return true
    val dayCenter = (dayIndex + 0.5f) * dayPx
    return dayCenter >= scroll.value && dayCenter <= scroll.value + scroll.viewportSize
}

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Composable
private fun TimelineScreenPreview() {
    HiddenRidgeTheme {
        TimelineScreenContent(
            uiState = TimelineUiState.Ready(TimelinePreviewData.timeline.toUi()),
            onBookingClick = {},
            onEmptyDayClick = { _, _ -> },
            onNewBookingClick = {},
            onCheckAvailabilityClick = {},
            onMonthTitleClick = {},
        )
    }
}
