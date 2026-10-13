package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.timeline.R
import com.thelazybattley.hiddenridge.feature.timeline.TimelineMonth
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle

private const val MONTHS_PER_ROW = 3

/**
 * Jumps the timeline to any month it covers. Each month shows how full it is, and a lock when a
 * whole-resort booking falls in it, so staff can spot busy months before scrolling there.
 *
 * @param visibleMonth the month the timeline shows now, highlighted and its year picked first.
 */
@Composable
internal fun GoToMonthDialog(
    months: List<TimelineMonth>,
    visibleMonth: YearMonth,
    onMonthClick: (index: Int) -> Unit,
    onTodayClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val years = months.map { it.yearMonth.year }.distinct()
    var year by rememberSaveable { mutableIntStateOf(visibleMonth.year) }
    AlertDialog(
        onDismissRequest = onDismiss,
        // The design's dialog is 24dp from each screen edge, wide enough that month tiles don't wrap.
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 24.dp),
        title = { Text(stringResource(R.string.timeline_go_to_month_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.timeline_go_to_month_hint))
                YearChips(years = years, selected = year, onSelect = { year = it })
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Month.entries.chunked(MONTHS_PER_ROW).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { month ->
                                val yearMonth = YearMonth.of(year, month)
                                val index = months.indexOfFirst { it.yearMonth == yearMonth }
                                MonthTile(
                                    yearMonth = yearMonth,
                                    month = months.getOrNull(index),
                                    isSelected = yearMonth == visibleMonth,
                                    onClick = { onMonthClick(index) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onTodayClick) { Text(stringResource(R.string.timeline_today)) } },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.timeline_dialog_cancel)) } },
    )
}

@Composable
private fun YearChips(years: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val yearLabel = stringResource(R.string.timeline_year)
    Row(
        modifier = Modifier
            .selectableGroup()
            .semantics { contentDescription = yearLabel },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        years.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.secondaryContainer else Color.Transparent)
                    .border(1.dp, if (isSelected) colors.secondaryContainer else colors.outline, CircleShape)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) })
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                    color = if (isSelected) colors.onSecondaryContainer else colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MonthTile(
    yearMonth: YearMonth,
    month: TimelineMonth?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    val shortName = yearMonth.month.getDisplayName(TextStyle.SHORT, locale)
    val shape = RoundedCornerShape(12.dp)
    if (month == null) {
        // Outside what the timeline covers: shown so the grid stays a calendar, but not a target.
        Box(
            modifier = modifier
                .height(64.dp)
                .alpha(0.4f)
                .border(1.dp, colors.outlineVariant, shape)
                .semantics { disabled() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(shortName, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        }
        return
    }
    val fullName = yearMonth.month.getDisplayName(TextStyle.FULL, locale)
    val description = stringResource(
        if (month.hasWholeResort) R.string.timeline_month_description_resort else R.string.timeline_month_description,
        "$fullName ${yearMonth.year}",
        month.bookedPercent,
    )
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (isSelected) colors.secondaryContainer else Color.Transparent,
        contentColor = if (isSelected) colors.onSecondaryContainer else colors.onSurface,
        border = if (isSelected) null else BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier
            .height(64.dp)
            .semantics { contentDescription = description },
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight()
                .clearAndSetSemantics {},
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(shortName, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium))
                Spacer(Modifier.weight(1f))
                if (month.hasWholeResort) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = colors.tertiary, modifier = Modifier.size(14.dp))
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.outlineVariant),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(month.bookedFraction)
                        .height(4.dp)
                        .background(colors.primary),
                )
            }
            Text(
                text = stringResource(R.string.timeline_month_booked, month.bookedPercent),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontFeatureSettings = "tnum"),
                color = if (isSelected) colors.onSecondaryContainer else colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
