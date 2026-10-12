package com.thelazybattley.hiddenridge.feature.newbooking.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.newbooking.CalendarDayUi
import com.thelazybattley.hiddenridge.feature.newbooking.CalendarUi
import com.thelazybattley.hiddenridge.feature.newbooking.DateField
import com.thelazybattley.hiddenridge.feature.newbooking.NewBookingAction
import com.thelazybattley.hiddenridge.feature.newbooking.NewBookingFormUi
import com.thelazybattley.hiddenridge.feature.newbooking.R
import com.thelazybattley.hiddenridge.feature.newbooking.RangeBand
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields

private val LengthsOfStay = listOf(1, 2, 3, 5, 7, 14)
private const val DAYS_PER_WEEK = 7
private const val MONTHS_PER_ROW = 3

@Composable
internal fun DatesSection(
    form: NewBookingFormUi,
    onAction: (NewBookingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = form.draft
    FormSection(
        title = stringResource(R.string.section_dates),
        aside = if (draft.hasDates) pluralStringResource(R.plurals.nights, draft.nights, draft.nights) else null,
        modifier = modifier,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateBox(
                label = stringResource(R.string.field_check_in),
                date = draft.checkIn,
                isActive = form.editingDate == DateField.CHECK_IN,
                onClick = { onAction(NewBookingAction.EditDate(DateField.CHECK_IN)) },
                modifier = Modifier.weight(1f),
            )
            DateBox(
                label = stringResource(R.string.field_check_out),
                date = draft.checkOut,
                isActive = form.editingDate == DateField.CHECK_OUT,
                onClick = { onAction(NewBookingAction.EditDate(DateField.CHECK_OUT)) },
                modifier = Modifier.weight(1f),
            )
        }
        if (draft.checkIn != null) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LengthsOfStay.forEach { nights ->
                    OptionChip(
                        label = pluralStringResource(R.plurals.nights, nights, nights),
                        selected = draft.nights == nights,
                        onClick = { onAction(NewBookingAction.SetNights(nights)) },
                    )
                }
            }
        }
        RangeCalendar(
            calendar = form.calendar,
            onPick = { onAction(NewBookingAction.PickDate(it)) },
            onShowMonth = { onAction(NewBookingAction.ShowMonth(it)) },
            onToggleMonthPicker = { onAction(NewBookingAction.ToggleMonthPicker) },
        )
    }
}

@Composable
private fun DateBox(
    label: String,
    date: LocalDate?,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val value = when {
        date != null -> DateFormats.weekdayDayMonthYear(currentLocale()).format(date)
        isActive -> stringResource(R.string.field_tap_a_date)
        else -> stringResource(R.string.field_not_set)
    }
    LabeledBox(
        label = label,
        isActive = isActive,
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { selected = isActive },
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (date == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

/** A month of nights with check-in to check-out banded, and nights already taken struck through. */
@Composable
private fun RangeCalendar(
    calendar: CalendarUi,
    onPick: (LocalDate) -> Unit,
    onShowMonth: (YearMonth) -> Unit,
    onToggleMonthPicker: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    val monthTitle = remember(calendar.month, locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale).format(calendar.month) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            onClickLabel = stringResource(R.string.calendar_choose_month),
                            role = Role.Button,
                            onClick = onToggleMonthPicker,
                        )
                        .height(40.dp)
                        .padding(start = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(monthTitle, style = MaterialTheme.typography.titleSmall)
                    Icon(
                        imageVector = if (calendar.isMonthPickerOpen) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (!calendar.isMonthPickerOpen) {
                    IconButton(onClick = { onShowMonth(calendar.month.minusMonths(1)) }, enabled = calendar.canGoBack) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.calendar_previous_month))
                    }
                    IconButton(onClick = { onShowMonth(calendar.month.plusMonths(1)) }, enabled = calendar.canGoForward) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.calendar_next_month))
                    }
                }
            }
            if (calendar.isMonthPickerOpen) {
                MonthPicker(calendar = calendar, onShowMonth = onShowMonth)
            } else {
                MonthGrid(calendar = calendar, onPick = onPick)
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("14", style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough))
                    Text(
                        text = stringResource(R.string.calendar_blocked_legend),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(calendar: CalendarUi, onPick: (LocalDate) -> Unit) {
    val locale = currentLocale()
    val firstDayOfWeek = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val weekdays = remember(firstDayOfWeek, locale) {
        (0 until DAYS_PER_WEEK).map { firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.NARROW, locale) }
    }
    val leadingBlanks = daysFrom(firstDayOfWeek, calendar.month.atDay(1).dayOfWeek)
    val cells: List<CalendarDayUi?> = List(leadingBlanks) { null } + calendar.days

    // Day initials are visual only; each day button announces its full date.
    Row(
        modifier = Modifier
            .height(32.dp)
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
    cells.chunked(DAYS_PER_WEEK).forEach { week ->
        Row {
            week.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (day != null) DayCell(day = day, onPick = onPick)
                }
            }
            repeat(DAYS_PER_WEEK - week.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun DayCell(day: CalendarDayUi, onPick: (LocalDate) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val description = DateFormats.weekdayDayMonthYear(currentLocale()).format(day.date).let {
        if (day.isBlocked) stringResource(R.string.calendar_day_booked, it) else it
    }
    Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
        when (day.band) {
            RangeBand.NONE -> Unit
            RangeBand.FULL -> Box(Modifier.fillMaxWidth().height(40.dp).background(colors.primaryContainer))
            RangeBand.START_HALF -> Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                Box(Modifier.weight(1f).height(40.dp).background(colors.primaryContainer))
            }
            RangeBand.END_HALF -> Row(Modifier.fillMaxWidth()) {
                Box(Modifier.weight(1f).height(40.dp).background(colors.primaryContainer))
                Spacer(Modifier.weight(1f))
            }
        }
        val textColor = when {
            day.isEndpoint -> colors.onPrimary
            day.isBlocked -> colors.onSurfaceVariant
            day.band == RangeBand.FULL -> colors.onPrimaryContainer
            day.isToday -> colors.primary
            else -> colors.onSurface
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (day.isEndpoint) colors.primary else Color.Transparent)
                .then(if (day.isToday && !day.isEndpoint) Modifier.border(1.dp, colors.primary, CircleShape) else Modifier)
                .clickable(enabled = !day.isPast, role = Role.Button) { onPick(day.date) }
                .semantics {
                    contentDescription = description
                    selected = day.isEndpoint
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (day.isEndpoint) FontWeight.SemiBold else FontWeight.Normal,
                    textDecoration = if (day.isBlocked && !day.isEndpoint) TextDecoration.LineThrough else null,
                ),
                color = if (day.isPast) textColor.copy(alpha = 0.38f) else textColor,
            )
            if (day.isBlocked && !day.isEndpoint) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 5.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.error),
                )
            }
        }
    }
}

/** Jump to any bookable month, a year at a time. */
@Composable
private fun MonthPicker(calendar: CalendarUi, onShowMonth: (YearMonth) -> Unit) {
    val locale = currentLocale()
    var year by remember(calendar.month) { mutableIntStateOf(calendar.month.year) }
    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (calendar.firstMonth.year..calendar.lastMonth.year).forEach { option ->
                OptionChip(label = option.toString(), selected = option == year, onClick = { year = option })
            }
        }
        Month.entries.chunked(MONTHS_PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { month ->
                    val yearMonth = YearMonth.of(year, month)
                    val inRange = yearMonth in calendar.firstMonth..calendar.lastMonth
                    val isSelected = yearMonth == calendar.month
                    Surface(
                        onClick = { onShowMonth(yearMonth) },
                        enabled = inRange,
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = month.getDisplayName(TextStyle.SHORT, locale),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (inRange) 1f else 0.38f),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun daysFrom(start: DayOfWeek, day: DayOfWeek): Int = (day.value - start.value + DAYS_PER_WEEK) % DAYS_PER_WEEK
