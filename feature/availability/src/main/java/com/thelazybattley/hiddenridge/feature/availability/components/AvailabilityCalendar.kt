package com.thelazybattley.hiddenridge.feature.availability.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.component.dashedBorder
import com.thelazybattley.hiddenridge.core.designsystem.component.rememberResortHatchBrush
import com.thelazybattley.hiddenridge.core.designsystem.component.rememberResortHoldHatchBrush
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.HouseFilter
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.feature.availability.AvailabilityCalendarUi
import com.thelazybattley.hiddenridge.feature.availability.AvailabilityDayUi
import com.thelazybattley.hiddenridge.feature.availability.DaySelection
import com.thelazybattley.hiddenridge.feature.availability.DayTone
import com.thelazybattley.hiddenridge.feature.availability.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields

private const val DAYS_PER_WEEK = 7
private val TileShape = RoundedCornerShape(8.dp)

/** A month where every night shows how much is free for the current filter. */
@Composable
internal fun AvailabilityCalendar(
    calendar: AvailabilityCalendarUi,
    filter: HouseFilter,
    onPick: (LocalDate) -> Unit,
    onShowMonth: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    val monthTitle = remember(calendar.month, locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale).format(calendar.month) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                )
                IconButton(onClick = { onShowMonth(calendar.month.minusMonths(1)) }, enabled = calendar.canGoBack) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.calendar_previous_month))
                }
                IconButton(onClick = { onShowMonth(calendar.month.plusMonths(1)) }, enabled = calendar.canGoForward) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.calendar_next_month))
                }
            }
            DayGrid(calendar = calendar, isWholeResort = filter == HouseFilter.WholeResort, onPick = onPick)
            Legend(isWholeResort = filter == HouseFilter.WholeResort)
            if (filter != HouseFilter.WholeResort) {
                Text(
                    text = when (filter) {
                        is HouseFilter.OfType -> stringResource(R.string.calendar_note_type, stringResource(filter.type.plural))
                        else -> stringResource(R.string.calendar_note_houses)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun DayGrid(calendar: AvailabilityCalendarUi, isWholeResort: Boolean, onPick: (LocalDate) -> Unit) {
    val locale = currentLocale()
    val firstDayOfWeek = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val weekdays = remember(firstDayOfWeek, locale) {
        (0 until DAYS_PER_WEEK).map { firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.NARROW, locale) }
    }
    val leadingBlanks = (calendar.month.atDay(1).dayOfWeek.value - firstDayOfWeek.value + DAYS_PER_WEEK) % DAYS_PER_WEEK
    val cells: List<AvailabilityDayUi?> = List(leadingBlanks) { null } + calendar.days

    // Day initials are visual only; each day announces its full date.
    Row(
        modifier = Modifier
            .height(28.dp)
            .clearAndSetSemantics {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        weekdays.forEach { letter ->
            Text(
                text = letter,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        cells.chunked(DAYS_PER_WEEK).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        DayTile(
                            day = day,
                            isWholeResort = isWholeResort,
                            onPick = onPick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                repeat(DAYS_PER_WEEK - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayTile(
    day: AvailabilityDayUi,
    isWholeResort: Boolean,
    onPick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val isEndpoint = day.selection == DaySelection.ENDPOINT
    val surface = when {
        isEndpoint -> Modifier.background(colors.primary)
        day.tone == DayTone.RESORT_BOOKED -> Modifier.background(rememberResortHatchBrush())
        day.tone == DayTone.RESORT_HOLD -> Modifier
            .background(colors.surface)
            .background(rememberResortHoldHatchBrush())
            .dashedBorder(width = 1.dp, color = colors.tertiary, cornerRadius = 8.dp, dashLength = 3.dp, gapLength = 2.dp)
        day.tone == DayTone.FULL -> Modifier.background(colors.surfaceContainerHighest)
        day.tone == DayTone.ALL_FREE -> Modifier.background(colors.primaryContainer)
        day.tone == DayTone.PAST -> Modifier
        else -> Modifier.background(colors.surfaceContainer)
    }
    val outline = when {
        day.selection == DaySelection.INSIDE -> Modifier.border(2.dp, colors.primary, TileShape)
        day.isToday && !isEndpoint -> Modifier.border(1.dp, colors.primary, TileShape)
        else -> Modifier
    }
    val contentColor = when {
        isEndpoint -> colors.onPrimary
        day.tone == DayTone.RESORT_BOOKED -> colors.onTertiaryContainer
        day.tone == DayTone.FULL -> colors.onSurfaceVariant
        day.tone == DayTone.ALL_FREE -> colors.onPrimaryContainer
        else -> colors.onSurface
    }
    val description = dayDescription(day, isWholeResort)

    Box(
        modifier = modifier
            .height(56.dp)
            .alpha(if (day.isPast) 0.38f else 1f)
            .clip(TileShape)
            .then(surface)
            .then(outline)
            .clickable(enabled = !day.isPast, role = Role.Button) { onPick(day.date) }
            .clearAndSetSemantics {
                contentDescription = description
                selected = isEndpoint
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isEndpoint) FontWeight.SemiBold else FontWeight.Normal),
                color = contentColor,
            )
            if (day.tone == DayTone.PAST) {
                // Nothing to report: the night has gone.
            } else if (day.resortGuest != null) {
                Icon(
                    imageVector = if (day.tone == DayTone.RESORT_HOLD) Icons.Outlined.HourglassEmpty else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (isEndpoint) contentColor else colors.tertiary,
                    modifier = Modifier.size(14.dp),
                )
            } else {
                Text(
                    text = when {
                        !isWholeResort -> stringResource(R.string.day_free_count, day.freeCount, day.total)
                        day.freeCount > 0 -> stringResource(R.string.day_free)
                        else -> stringResource(R.string.day_busy)
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = when {
                        isEndpoint -> contentColor
                        day.tone == DayTone.FULL -> colors.error
                        day.tone == DayTone.SOME_FREE -> colors.primary
                        else -> contentColor
                    },
                )
            }
        }
    }
}

@Composable
private fun dayDescription(day: AvailabilityDayUi, isWholeResort: Boolean): String {
    val date = DateFormats.weekdayDayMonthYear(currentLocale()).format(day.date)
    val resortGuest = day.resortGuest
    return when {
        day.tone == DayTone.PAST -> date
        resortGuest != null && day.tone == DayTone.RESORT_HOLD -> stringResource(R.string.day_description_resort_hold, date, resortGuest)
        resortGuest != null -> stringResource(R.string.day_description_resort_booked, date, resortGuest)
        isWholeResort && day.freeCount > 0 -> stringResource(R.string.day_description_resort_free, date)
        isWholeResort -> stringResource(R.string.day_description_resort_busy, date)
        else -> stringResource(R.string.day_description_count, date, day.freeCount, day.total)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Legend(isWholeResort: Boolean) {
    val colors = MaterialTheme.colorScheme
    val hatch = rememberResortHatchBrush()
    val holdHatch = rememberResortHoldHatchBrush()
    FlowRow(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 4.dp)
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LegendItem(R.string.legend_all_free, Modifier.background(colors.primaryContainer))
        if (!isWholeResort) {
            LegendItem(
                R.string.legend_some_free,
                Modifier
                    .background(colors.surfaceContainer)
                    .border(1.dp, colors.outlineVariant, RoundedCornerShape(2.dp)),
            )
        }
        LegendItem(R.string.legend_full, Modifier.background(colors.surfaceContainerHighest))
        LegendItem(R.string.legend_resort_booked, Modifier.background(hatch).border(1.dp, colors.tertiary, RoundedCornerShape(2.dp)))
        LegendItem(
            R.string.legend_resort_hold,
            Modifier
                .background(colors.surface)
                .background(holdHatch)
                .dashedBorder(width = 1.dp, color = colors.tertiary, cornerRadius = 2.dp, dashLength = 2.dp, gapLength = 1.dp),
        )
    }
}

@Composable
private fun LegendItem(@StringRes label: Int, swatch: Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .then(swatch),
        )
        Text(stringResource(label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@get:StringRes
internal val HouseType.plural: Int
    get() = when (this) {
        HouseType.CABIN -> R.string.type_cabins
        HouseType.CASITA -> R.string.type_casitas
        HouseType.FAMILY_HOUSE -> R.string.type_family_houses
    }
