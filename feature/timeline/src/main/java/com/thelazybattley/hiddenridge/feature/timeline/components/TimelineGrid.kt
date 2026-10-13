package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.designsystem.theme.ExtendedColors
import com.thelazybattley.hiddenridge.core.designsystem.theme.HiddenRidgeTheme
import com.thelazybattley.hiddenridge.feature.timeline.MoveState
import com.thelazybattley.hiddenridge.feature.timeline.MoveUi
import com.thelazybattley.hiddenridge.feature.timeline.R
import com.thelazybattley.hiddenridge.feature.timeline.TimelineRow
import com.thelazybattley.hiddenridge.feature.timeline.TimelineUi
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt
import java.time.format.TextStyle as DateTextStyle

/**
 * The master availability grid: day columns scroll sideways under a sticky day header, house rows
 * scroll vertically beside a sticky house column. Only the visible days are drawn each frame.
 *
 * @param move the booking picked up, if any. While set, only it responds: dragging it calls
 *   [onDragTo] with the day shift and house under the finger, measured from where the drag began.
 */
@Composable
internal fun TimelineGrid(
    timeline: TimelineUi,
    move: MoveUi?,
    horizontalScroll: ScrollState,
    onBookingClick: (bookingId: String) -> Unit,
    onBookingLongClick: (bookingId: String) -> Unit,
    onEmptyDayClick: (houseId: String, date: LocalDate) -> Unit,
    onDragTo: (dayShift: Int, houseId: String?) -> Unit,
    modifier: Modifier = Modifier,
    verticalScroll: ScrollState = rememberScrollState(),
) {
    val rowLayout = remember(timeline.rows) { TimelineRowLayout(timeline.rows) }
    val divider = MaterialTheme.colorScheme.outlineVariant

    Column(modifier.background(MaterialTheme.colorScheme.surface)) {
        Row(Modifier.height(TimelineDimens.HeaderHeight)) {
            HouseColumnHeader(
                Modifier
                    .width(TimelineDimens.HouseColumnWidth)
                    .fillMaxHeight(),
            )
            DayHeader(
                timeline = timeline,
                horizontalScroll = horizontalScroll,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        HorizontalDivider(color = divider)
        Row(
            Modifier
                .weight(1f)
                .verticalScroll(verticalScroll),
        ) {
            HouseColumn(
                rows = timeline.rows,
                // Drawn above the grid so group labels can overflow onto it.
                modifier = Modifier
                    .width(TimelineDimens.HouseColumnWidth)
                    .zIndex(1f),
            )
            Box(
                Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll),
            ) {
                TimelineBody(
                    timeline = timeline,
                    move = move,
                    rowLayout = rowLayout,
                    horizontalScroll = horizontalScroll,
                    onBookingClick = onBookingClick,
                    onBookingLongClick = onBookingLongClick,
                    onEmptyDayClick = onEmptyDayClick,
                    onDragTo = onDragTo,
                )
            }
        }
    }
}

@Composable
private fun HouseColumnHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.endBorder(MaterialTheme.colorScheme.outlineVariant),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            text = stringResource(R.string.timeline_house_column).uppercase(currentLocale()),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.08.em),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun DayHeader(
    timeline: TimelineUi,
    horizontalScroll: ScrollState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    val textMeasurer = rememberTextMeasurer(cacheSize = 128)
    val monthLabels = remember(timeline.months, locale) {
        val formatter = TimelineFormat.monthYear(locale)
        timeline.months.map { formatter.format(it.yearMonth) }
    }
    val weekdayLetters = remember(locale) {
        DayOfWeek.entries.associateWith { it.getDisplayName(DateTextStyle.NARROW, locale) }
    }
    val monthStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
    val weekdayStyle = TextStyle(fontSize = 10.sp, color = colors.onSurfaceVariant)
    val dayStyle = TextStyle(fontSize = 12.sp, fontFeatureSettings = "tnum")

    Canvas(modifier.clipToBounds()) {
        val dayPx = TimelineDimens.DayWidth.toPx()
        val scroll = horizontalScroll.value.toFloat()
        val days = visibleDays(scroll, size.width, dayPx, timeline.dayCount)
        val monthRowPx = TimelineDimens.MonthRowHeight.toPx()
        val todayRadius = 12.dp.toPx()

        for (day in days) {
            val date = timeline.dateAt(day)
            val centerX = (day + 0.5f) * dayPx - scroll
            val isToday = day == timeline.todayIndex
            val weekday = textMeasurer.measure(weekdayLetters.getValue(date.dayOfWeek), weekdayStyle)
            val number = textMeasurer.measure(
                text = date.dayOfMonth.toString(),
                style = when {
                    isToday -> dayStyle.copy(color = colors.onPrimary, fontWeight = FontWeight.SemiBold)
                    date.isWeekend() -> dayStyle.copy(color = colors.onSurfaceVariant)
                    else -> dayStyle.copy(color = colors.onSurface)
                },
            )
            val gap = 2.dp.toPx()
            val top = monthRowPx + (TimelineDimens.DayRowHeight.toPx() - weekday.size.height - gap - todayRadius * 2) / 2
            val numberCenterY = top + weekday.size.height + gap + todayRadius

            drawText(weekday, topLeft = Offset(centerX - weekday.size.width / 2f, top))
            if (isToday) drawCircle(colors.primary, radius = todayRadius, center = Offset(centerX, numberCenterY))
            drawText(
                number,
                topLeft = Offset(centerX - number.size.width / 2f, numberCenterY - number.size.height / 2f),
            )
        }

        val padding = 8.dp.toPx()
        timeline.months.forEachIndexed { index, month ->
            val monthEnd = month.startIndex + month.dayCount
            if (month.startIndex > days.last || monthEnd <= days.first) return@forEachIndexed
            val startX = month.startIndex * dayPx - scroll
            val endX = monthEnd * dayPx - scroll
            drawRect(colors.outline, topLeft = Offset(startX, 0f), size = Size(TimelineDimens.Hairline.toPx(), monthRowPx))

            val label = textMeasurer.measure(monthLabels[index], monthStyle)
            // Sticks to the left edge while its month is in view, then is pushed off by the next one.
            val labelX = maxOf(startX, 0f).plus(padding)
                .coerceAtMost(endX - label.size.width - padding)
                .coerceAtLeast(startX + padding)
            drawText(label, topLeft = Offset(labelX, (monthRowPx - label.size.height) / 2f + 2.dp.toPx()))
        }
    }
}

@Composable
private fun HouseColumn(rows: List<TimelineRow>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .background(colors.surface)
            .endBorder(colors.outlineVariant),
    ) {
        rows.forEach { row ->
            when (row) {
                is TimelineRow.Group -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TimelineDimens.GroupRowHeight)
                        .background(colors.surfaceContainer)
                        .padding(horizontal = 12.dp)
                        .semantics { heading() },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    // Group labels run on over the grid's matching group band rather than truncating.
                    Text(
                        text = stringResource(row.type.groupLabel).uppercase(currentLocale()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.08.em,
                        ),
                        color = colors.onSurfaceVariant,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        modifier = Modifier.wrapContentWidth(Alignment.Start, unbounded = true),
                    )
                }

                is TimelineRow.House -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TimelineDimens.HouseRowHeight)
                        .bottomBorder(colors.outlineVariant)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = row.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineBody(
    timeline: TimelineUi,
    move: MoveUi?,
    rowLayout: TimelineRowLayout,
    horizontalScroll: ScrollState,
    onBookingClick: (bookingId: String) -> Unit,
    onBookingLongClick: (bookingId: String) -> Unit,
    onEmptyDayClick: (houseId: String, date: LocalDate) -> Unit,
    onDragTo: (dayShift: Int, houseId: String?) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extendedColors = HiddenRidgeTheme.extendedColors
    val dayWidth = TimelineDimens.DayWidth
    val isMoving = move != null
    val currentOnEmptyDayClick by rememberUpdatedState(onEmptyDayClick)
    val currentTimeline by rememberUpdatedState(timeline)
    val currentMove by rememberUpdatedState(move)
    val currentOnDragTo by rememberUpdatedState(onDragTo)

    Box(
        Modifier
            .size(width = dayWidth * timeline.dayCount, height = rowLayout.totalHeight)
            .drawWithContent {
                drawGrid(
                    timeline = timeline,
                    rowLayout = rowLayout,
                    scrollPx = horizontalScroll.value.toFloat(),
                    viewportPx = horizontalScroll.viewportSize.toFloat(),
                    colors = colors,
                    extendedColors = extendedColors,
                )
                drawContent()
                drawNowLine(timeline.todayIndex, colors)
            }
            .pointerInput(isMoving, rowLayout) {
                if (!isMoving) return@pointerInput
                // Drags that start on the lifted booking move it; any other touch still scrolls the grid.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val start = currentMove ?: return@awaitEachGesture
                    if (liftedRects(currentTimeline, rowLayout).none { it.contains(down.position) }) return@awaitEachGesture
                    down.consume()
                    val dayPx = dayWidth.toPx()
                    val startCenterY = start.houseRowIndex?.let { (rowLayout.tops[it] + TimelineDimens.HouseRowHeight / 2).toPx() }
                    drag(down.id) { change ->
                        change.consume()
                        val delta = change.position - down.position
                        val houseId = startCenterY?.let { rowLayout.nearestHouseTo((it + delta.y).toDp()) }
                        currentOnDragTo(start.dayShift + (delta.x / dayPx).roundToInt(), houseId)
                    }
                }
            }
            .pointerInput(timeline, rowLayout, isMoving) {
                if (isMoving) return@pointerInput
                detectTapGestures { offset ->
                    val dayIndex = (offset.x / dayWidth.toPx()).toInt()
                    val row = rowLayout.rowAt(offset.y.toDp()) as? TimelineRow.House ?: return@detectTapGestures
                    // Past nights can't be booked.
                    if (dayIndex >= timeline.todayIndex) currentOnEmptyDayClick(row.houseId, timeline.dateAt(dayIndex))
                }
            },
    ) {
        timeline.ghosts.forEach { ghost ->
            val rowIndex = ghost.rowIndex
            GhostBarItem(
                width = dayWidth * ghost.nights - 2.dp,
                height = if (rowIndex == null) rowLayout.totalHeight else TimelineDimens.BarHeight,
                modifier = Modifier.offset(
                    x = dayWidth * (ghost.startIndex + 0.5f) + 1.dp,
                    y = if (rowIndex == null) 0.dp else rowLayout.barTop(rowIndex),
                ),
            )
        }
        timeline.resortBlocks.forEach { block ->
            key(block.bookingId) {
                ResortBlockItem(
                    block = block,
                    width = dayWidth * block.nights - 2.dp,
                    height = rowLayout.totalHeight,
                    isInteractive = !isMoving,
                    onClick = { onBookingClick(block.bookingId) },
                    onLongClick = { onBookingLongClick(block.bookingId) },
                    modifier = Modifier
                        .zIndex(if (block.moveState == MoveState.NONE) 0f else 2f)
                        .offset(x = dayWidth * (block.startIndex + 0.5f) + 1.dp),
                )
            }
        }
        timeline.bookingBars.forEach { bar ->
            key(bar.key) {
                BookingBarItem(
                    bar = bar,
                    width = dayWidth * bar.nights - 2.dp,
                    isInteractive = !isMoving,
                    onClick = { onBookingClick(bar.bookingId) },
                    onLongClick = { onBookingLongClick(bar.bookingId) },
                    // Bars run from check-in afternoon to check-out morning, so they start and end mid-column.
                    modifier = Modifier
                        .zIndex(if (bar.moveState == MoveState.NONE) 1f else 3f)
                        .offset(x = dayWidth * (bar.startIndex + 0.5f) + 1.dp, y = rowLayout.barTop(bar.rowIndex)),
                )
            }
        }
    }
}

/** Where the lifted booking is drawn, in grid pixels, with a little slack so it's easy to grab. */
private fun Density.liftedRects(timeline: TimelineUi, rowLayout: TimelineRowLayout): List<Rect> {
    val dayPx = TimelineDimens.DayWidth.toPx()
    val slack = 8.dp.toPx()
    val bars = timeline.bookingBars.filter { it.moveState != MoveState.NONE }.map { bar ->
        val top = rowLayout.barTop(bar.rowIndex).toPx()
        Rect((bar.startIndex + 0.5f) * dayPx, top, (bar.startIndex + 0.5f + bar.nights) * dayPx, top + TimelineDimens.BarHeight.toPx())
    }
    val blocks = timeline.resortBlocks.filter { it.moveState != MoveState.NONE }.map { block ->
        Rect((block.startIndex + 0.5f) * dayPx, 0f, (block.startIndex + 0.5f + block.nights) * dayPx, rowLayout.totalHeight.toPx())
    }
    return (bars + blocks).map { it.inflate(slack) }
}

private fun DrawScope.drawGrid(
    timeline: TimelineUi,
    rowLayout: TimelineRowLayout,
    scrollPx: Float,
    viewportPx: Float,
    colors: ColorScheme,
    extendedColors: ExtendedColors,
) {
    val dayPx = TimelineDimens.DayWidth.toPx()
    val hairline = TimelineDimens.Hairline.toPx()
    val days = visibleDays(scrollPx, viewportPx.takeIf { it > 0f } ?: size.width, dayPx, timeline.dayCount)
    if (days.isEmpty()) return
    val left = days.first * dayPx
    val right = (days.last + 1) * dayPx

    val nowX = (timeline.todayIndex + 0.5f) * dayPx
    if (nowX > left) {
        drawRect(extendedColors.past, topLeft = Offset(left, 0f), size = Size(minOf(nowX, right) - left, size.height))
    }
    for (day in days) {
        val x = day * dayPx
        if (timeline.dateAt(day).isWeekend()) {
            drawRect(extendedColors.weekend, topLeft = Offset(x, 0f), size = Size(dayPx, size.height))
        }
        drawRect(colors.outlineVariant, topLeft = Offset(x, 0f), size = Size(hairline, size.height))
    }
    rowLayout.rows.forEachIndexed { index, row ->
        val top = rowLayout.tops[index].toPx()
        val height = row.height.toPx()
        when (row) {
            is TimelineRow.Group ->
                drawRect(colors.surfaceContainer, topLeft = Offset(left, top), size = Size(right - left, height))
            is TimelineRow.House ->
                drawRect(colors.outlineVariant, topLeft = Offset(left, top + height - hairline), size = Size(right - left, hairline))
        }
    }
    timeline.months.forEach { month ->
        val x = month.startIndex * dayPx
        if (x in left..right) drawRect(colors.outline, topLeft = Offset(x, 0f), size = Size(hairline, size.height))
    }
}

private fun DrawScope.drawNowLine(todayIndex: Int, colors: ColorScheme) {
    val width = 2.dp.toPx()
    val x = (todayIndex + 0.5f) * TimelineDimens.DayWidth.toPx() - width / 2
    drawRect(colors.primary, topLeft = Offset(x, 0f), size = Size(width, size.height))
}

private fun LocalDate.isWeekend(): Boolean = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
