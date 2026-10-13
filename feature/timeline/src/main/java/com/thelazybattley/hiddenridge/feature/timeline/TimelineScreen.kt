package com.thelazybattley.hiddenridge.feature.timeline

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.designsystem.theme.HiddenRidgeTheme
import com.thelazybattley.hiddenridge.feature.timeline.components.MonthRail
import com.thelazybattley.hiddenridge.feature.timeline.components.MoveBar
import com.thelazybattley.hiddenridge.feature.timeline.components.TimelineDimens
import com.thelazybattley.hiddenridge.feature.timeline.components.TimelineGrid
import com.thelazybattley.hiddenridge.feature.timeline.components.TimelineLegend
import com.thelazybattley.hiddenridge.feature.timeline.components.TimelineRowLayout
import com.thelazybattley.hiddenridge.feature.timeline.components.TimelineTopBar
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** Days of context kept to the left when jumping to today. */
private const val DAYS_BEFORE_TODAY = 1

/** The visible month is the one a few days into the viewport, not the half-visible first column. */
private const val ACTIVE_MONTH_PROBE_DAYS = 3

/**
 * @param moveRequest a booking to pick up, asked for from elsewhere (Move on its details sheet);
 *   [onMoveRequestHandled] clears it once picked up.
 */
@Composable
internal fun TimelineScreen(
    moveRequest: String?,
    onMoveRequestHandled: () -> Unit,
    onBookingClick: (bookingId: String) -> Unit,
    onEmptyDayClick: (houseId: String, date: LocalDate) -> Unit,
    onNewBookingClick: () -> Unit,
    onCheckAvailabilityClick: () -> Unit,
    onMonthTitleClick: () -> Unit,
    onMovingChange: (isMoving: Boolean) -> Unit,
    onShowMessage: (message: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalContext.current.resources
    val locale = currentLocale()
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)
    LaunchedEffect(viewModel, locale) {
        viewModel.moved.collect { moved ->
            val range = DateFormats.stayRange(moved.checkIn, moved.checkOut, locale)
            currentOnShowMessage(
                if (moved.houseNames.isEmpty()) {
                    resources.getString(R.string.timeline_moved_resort, moved.guestName, range)
                } else {
                    resources.getString(R.string.timeline_moved, moved.guestName, range, moved.houseNames.joinToString())
                },
            )
        }
    }

    LaunchedEffect(moveRequest) {
        if (moveRequest == null) return@LaunchedEffect
        viewModel.onAction(TimelineAction.StartMove(moveRequest))
        onMoveRequestHandled()
    }

    val isMoving = (uiState as? TimelineUiState.Ready)?.move != null
    val currentOnMovingChange by rememberUpdatedState(onMovingChange)
    LaunchedEffect(isMoving) { currentOnMovingChange(isMoving) }
    // Leaving the timeline mid-move must bring the navigation bar back.
    DisposableEffect(Unit) { onDispose { currentOnMovingChange(false) } }
    BackHandler(enabled = isMoving) { viewModel.onAction(TimelineAction.CancelMove) }

    TimelineScreenContent(
        uiState = uiState,
        onBookingClick = onBookingClick,
        onEmptyDayClick = onEmptyDayClick,
        onNewBookingClick = onNewBookingClick,
        onCheckAvailabilityClick = {
            viewModel.onAction(TimelineAction.CancelMove)
            onCheckAvailabilityClick()
        },
        onMonthTitleClick = onMonthTitleClick,
        onAction = viewModel::onAction,
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
    onAction: (TimelineAction) -> Unit,
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
                move = uiState.move,
                onBookingClick = onBookingClick,
                onEmptyDayClick = onEmptyDayClick,
                onNewBookingClick = onNewBookingClick,
                onCheckAvailabilityClick = onCheckAvailabilityClick,
                onMonthTitleClick = onMonthTitleClick,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun TimelineReady(
    timeline: TimelineUi,
    move: MoveUi?,
    onBookingClick: (bookingId: String) -> Unit,
    onEmptyDayClick: (houseId: String, date: LocalDate) -> Unit,
    onNewBookingClick: () -> Unit,
    onCheckAvailabilityClick: () -> Unit,
    onMonthTitleClick: () -> Unit,
    onAction: (TimelineAction) -> Unit,
) {
    val density = LocalDensity.current
    val dayPx = with(density) { TimelineDimens.DayWidth.toPx() }
    val horizontalScroll = rememberScrollState(initial = scrollOffsetFor(timeline.todayIndex - DAYS_BEFORE_TODAY, dayPx))
    val verticalScroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val scrollToDay: (Int) -> Unit = { day ->
        scope.launch { horizontalScroll.animateScrollTo(scrollOffsetFor(day, dayPx)) }
    }
    val scrollToToday = { scrollToDay(timeline.todayIndex - DAYS_BEFORE_TODAY) }

    // A booking picked up from its details sheet may be off screen: bring it into view.
    val currentTimeline by rememberUpdatedState(timeline)
    LaunchedEffect(move?.bookingId) {
        val bookingId = move?.bookingId ?: return@LaunchedEffect
        val bar = currentTimeline.bookingBars.firstOrNull { it.bookingId == bookingId }
        val startIndex = bar?.startIndex ?: currentTimeline.resortBlocks.firstOrNull { it.bookingId == bookingId }?.startIndex
            ?: return@LaunchedEffect
        val first = firstVisibleDay(horizontalScroll, dayPx)
        val visibleDays = (horizontalScroll.viewportSize / dayPx).toInt()
        if (startIndex < first || startIndex > first + visibleDays - 2) scrollToDay(startIndex - DAYS_BEFORE_TODAY)
        if (bar != null) {
            val rowTop = with(density) { TimelineRowLayout(currentTimeline.rows).tops[bar.rowIndex].toPx() }.roundToInt()
            val rowBottom = rowTop + with(density) { TimelineDimens.HouseRowHeight.toPx() }.roundToInt()
            if (rowTop < verticalScroll.value || rowBottom > verticalScroll.value + verticalScroll.viewportSize) {
                verticalScroll.animateScrollTo(rowTop)
            }
        }
    }

    val activeMonthIndex by remember(timeline, dayPx) {
        derivedStateOf { timeline.monthIndexAt(firstVisibleDay(horizontalScroll, dayPx) + ACTIVE_MONTH_PROBE_DAYS) }
    }
    Column(Modifier.fillMaxSize()) {
        TimelineTopBar(
            houseCount = timeline.houseCount,
            visibleMonth = timeline.months[activeMonthIndex].yearMonth,
            onMonthTitleClick = onMonthTitleClick,
            onCheckAvailabilityClick = onCheckAvailabilityClick,
            onNewBookingClick = onNewBookingClick,
            isNewBookingEnabled = move == null,
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
        TimelineGrid(
            timeline = timeline,
            move = move,
            horizontalScroll = horizontalScroll,
            verticalScroll = verticalScroll,
            onBookingClick = onBookingClick,
            onBookingLongClick = { onAction(TimelineAction.StartMove(it)) },
            onEmptyDayClick = onEmptyDayClick,
            onDragTo = { dayShift, houseId -> onAction(TimelineAction.DragTo(dayShift, houseId)) },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        if (move != null) {
            MoveBar(
                move = move,
                onNudge = { days, houses -> onAction(TimelineAction.Nudge(days, houses)) },
                onCancel = { onAction(TimelineAction.CancelMove) },
                onSave = { onAction(TimelineAction.SaveMove) },
            )
        }
    }
}

private fun scrollOffsetFor(dayIndex: Int, dayPx: Float): Int = (dayIndex.coerceAtLeast(0) * dayPx).roundToInt()

private fun firstVisibleDay(scroll: ScrollState, dayPx: Float): Int = (scroll.value / dayPx).toInt()

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
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 760, name = "Moving into a conflict")
@Composable
private fun TimelineMovePreview() {
    val timeline = TimelinePreviewData.timeline
    val draft = MoveDraft(bookingId = "5", dayShift = 1, houseId = "c2")
    HiddenRidgeTheme {
        TimelineScreenContent(
            uiState = TimelineUiState.Ready(timeline.toUi(draft), timeline.moveUi(draft, isSaving = false)),
            onBookingClick = {},
            onEmptyDayClick = { _, _ -> },
            onNewBookingClick = {},
            onCheckAvailabilityClick = {},
            onMonthTitleClick = {},
            onAction = {},
        )
    }
}
