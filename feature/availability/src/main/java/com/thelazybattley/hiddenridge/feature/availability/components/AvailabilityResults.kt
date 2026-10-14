package com.thelazybattley.hiddenridge.feature.availability.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.HolidayVillage
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.HouseFilter
import com.thelazybattley.hiddenridge.feature.availability.AvailabilityAction
import com.thelazybattley.hiddenridge.feature.availability.AvailabilityResultUi
import com.thelazybattley.hiddenridge.feature.availability.ConflictRowUi
import com.thelazybattley.hiddenridge.feature.availability.HouseRowUi
import com.thelazybattley.hiddenridge.feature.availability.R
import java.time.LocalDate

/** What's free for the chosen stay, what's in the way, and how to book it. */
@Composable
internal fun AvailabilityResults(
    result: AvailabilityResultUi,
    onAction: (AvailabilityAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryCard(result)
        result.nextOpening?.let { NextOpeningCard(nights = result.nights, checkIn = it, onClick = { onAction(AvailabilityAction.ShowNextOpening) }) }
        when (result) {
            is AvailabilityResultUi.Houses -> HouseResults(result, onAction)
            is AvailabilityResultUi.WholeResort -> ResortResults(result, onAction)
        }
    }
}

@Composable
private fun SummaryCard(result: AvailabilityResultUi) {
    val colors = MaterialTheme.colorScheme
    val fits = when (result) {
        is AvailabilityResultUi.Houses -> result.fits
        is AvailabilityResultUi.WholeResort -> result.fits
    }
    val title = when (result) {
        is AvailabilityResultUi.WholeResort ->
            stringResource(if (result.fits) R.string.result_resort_free else R.string.result_resort_taken)
        is AvailabilityResultUi.Houses -> if (result.fits) {
            val kind = (result.filter as? HouseFilter.OfType)?.type?.pluralName?.lowercase(currentLocale()) ?: stringResource(R.string.result_houses)
            stringResource(R.string.result_houses_free, result.freeRows.size, result.rows.size, kind)
        } else {
            stringResource(R.string.result_nothing_free)
        }
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (fits) colors.primaryContainer else colors.errorContainer,
        contentColor = if (fits) colors.onPrimaryContainer else colors.onErrorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(if (fits) Icons.Outlined.EventAvailable else Icons.Outlined.EventBusy, contentDescription = null)
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(
                        R.string.result_range,
                        DateFormats.stayRange(result.checkIn, result.checkOut, currentLocale()),
                        pluralStringResource(R.plurals.nights, result.nights, result.nights),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun NextOpeningCard(nights: Int, checkIn: LocalDate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Update, contentDescription = null, tint = colors.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.next_opening, pluralStringResource(R.plurals.nights, nights, nights)),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = DateFormats.stayRange(checkIn, checkIn.plusDays(nights.toLong()), currentLocale()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun HouseResults(result: AvailabilityResultUi.Houses, onAction: (AvailabilityAction) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            result.rows.forEachIndexed { index, row ->
                HouseRow(row = row, onAction = onAction)
                if (index < result.rows.lastIndex) HorizontalDivider(color = colors.outlineVariant)
            }
        }
    }
    val free = result.freeRows
    if (free.size > 1) {
        OutlinedButton(
            onClick = { onAction(AvailabilityAction.Book(houseIds = free.map(HouseRowUi::houseId))) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Icon(Icons.Outlined.GroupAdd, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Text(
                text = stringResource(R.string.action_book_all, free.size, free.sumOf(HouseRowUi::sleeps)),
                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
            )
        }
    }
}

@Composable
private fun HouseRow(row: HouseRowUi, onAction: (AvailabilityAction) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val takenBy = row.takenBy
    val opensOn = row.opensOn
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = 12.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = when {
                opensOn != null -> Icons.Outlined.Schedule
                takenBy == null -> Icons.Filled.CheckCircle
                takenBy.isHold -> Icons.Outlined.HourglassEmpty
                else -> Icons.Filled.Block
            },
            contentDescription = null,
            tint = if (row.isFree) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (row.isFree) colors.onSurface else colors.onSurfaceVariant,
                )
                Text(stringResource(R.string.house_sleeps, row.sleeps), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Text(
                text = when {
                    takenBy != null -> takenByText(takenBy)
                    opensOn != null -> stringResource(R.string.house_opens_on, DateFormats.weekdayDayMonth(currentLocale()).format(opensOn))
                    else -> stringResource(R.string.house_free_all_nights)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (row.isFree) colors.primary else colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (row.isFree) {
            val label = stringResource(R.string.action_book_house, row.name)
            FilledTonalButton(
                onClick = { onAction(AvailabilityAction.Book(houseIds = listOf(row.houseId))) },
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.semantics { contentDescription = label },
            ) { Text(stringResource(R.string.action_book)) }
        } else if (takenBy != null) {
            val label = stringResource(R.string.action_view_booking, takenBy.guestName)
            TextButton(
                onClick = { onAction(AvailabilityAction.ViewBooking(takenBy.bookingId)) },
                modifier = Modifier.semantics { contentDescription = label },
            ) { Text(stringResource(R.string.action_view)) }
        }
    }
}

@Composable
private fun takenByText(booking: ConflictRowUi): String {
    val range = DateFormats.stayRange(booking.checkIn, booking.checkOut, currentLocale())
    return when {
        booking.isWholeResort -> stringResource(R.string.house_taken_by_resort, stringResource(R.string.house_taken_by, booking.guestName, range))
        booking.isHold -> stringResource(R.string.house_taken_by_hold, booking.guestName, range)
        else -> stringResource(R.string.house_taken_by, booking.guestName, range)
    }
}

@Composable
private fun ResortResults(result: AvailabilityResultUi.WholeResort, onAction: (AvailabilityAction) -> Unit) {
    if (result.fits) {
        Button(
            onClick = { onAction(AvailabilityAction.Book(houseIds = emptyList(), wholeResort = true)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Icon(Icons.Outlined.HolidayVillage, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Text(stringResource(R.string.action_book_whole_resort), modifier = Modifier.padding(start = ButtonDefaults.IconSpacing))
        }
        return
    }
    val colors = MaterialTheme.colorScheme
    Column {
        result.conflicts.forEach { conflict ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clickable(
                        onClickLabel = stringResource(R.string.action_view),
                        role = Role.Button,
                    ) { onAction(AvailabilityAction.ViewBooking(conflict.bookingId)) }
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (conflict.isHold) Icons.Outlined.HourglassEmpty else Icons.Filled.Block,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (conflict.isHold) stringResource(R.string.conflict_hold, conflict.guestName) else conflict.guestName,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(
                            R.string.conflict_detail,
                            if (conflict.isWholeResort) {
                                pluralStringResource(R.plurals.whole_resort_houses, result.houseCount, result.houseCount)
                            } else {
                                conflict.houseNames.joinToString()
                            },
                            DateFormats.stayRange(conflict.checkIn, conflict.checkOut, currentLocale()),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
            }
        }
    }
}
