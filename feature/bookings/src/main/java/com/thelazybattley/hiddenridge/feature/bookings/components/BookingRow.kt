package com.thelazybattley.hiddenridge.feature.bookings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.component.dashedBorder
import com.thelazybattley.hiddenridge.core.designsystem.component.rememberResortHatchBrush
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.bookings.BookingRowUi
import com.thelazybattley.hiddenridge.feature.bookings.GroupLabel
import com.thelazybattley.hiddenridge.feature.bookings.HousesLabel
import com.thelazybattley.hiddenridge.feature.bookings.R
import com.thelazybattley.hiddenridge.feature.bookings.RowStatus
import java.time.LocalDate

private const val CANCELLED_ALPHA = 0.7f
private val TileShape = RoundedCornerShape(12.dp)

@Composable
internal fun GroupHeader(label: GroupLabel, count: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    val text = when (label) {
        is GroupLabel.Today -> stringResource(R.string.bookings_group_today, BookingsFormat.weekdayDayMonth(locale).format(label.date))
        GroupLabel.ThisWeek -> stringResource(R.string.bookings_group_this_week)
        is GroupLabel.EarlierIn -> stringResource(R.string.bookings_group_earlier_in, BookingsFormat.month(locale).format(label.month))
        is GroupLabel.Month -> (if (label.showYear) BookingsFormat.monthYear(locale) else BookingsFormat.month(locale)).format(label.month)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(locale),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.08.em,
            color = if (label is GroupLabel.Today) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        Text(
            text = pluralStringResource(R.plurals.bookings_group_count, count, count),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}

/** One booking: a date tile for check-in, the guest and stay, and a pill for where it stands. */
@Composable
internal fun BookingRow(
    booking: BookingRowUi,
    today: LocalDate,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    val cancelled = booking.status == RowStatus.CANCELLED
    val houses = when (val label = booking.houses) {
        is HousesLabel.Names -> label.names.joinToString()
        is HousesLabel.WholeResort -> stringResource(R.string.bookings_whole_resort_houses, label.houseCount)
    }
    val dates = if (booking.showWeekdays) {
        BookingsFormat.weekdayRange(booking.checkIn, booking.checkOut, locale)
    } else {
        BookingsFormat.shortRange(booking.checkIn, booking.checkOut, today.year, locale)
    }
    val nights = pluralStringResource(R.plurals.bookings_nights, booking.nights, booking.nights)
    val detail = booking.holdUntil?.let { until ->
        stringResource(R.string.bookings_row_detail_hold_until, houses, dates, nights, BookingsFormat.weekdayDayMonth(locale).format(until))
    } ?: stringResource(R.string.bookings_row_detail, houses, dates, nights)
    val pill = booking.status.pill()
    val description = stringResource(R.string.bookings_row_description, booking.guestName, detail, pill.label)

    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable(onClickLabel = stringResource(R.string.bookings_view), role = Role.Button, onClick = onClick)
                .clearAndSetSemantics { contentDescription = description }
                .alpha(if (cancelled) CANCELLED_ALPHA else 1f)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateTile(booking = booking, month = BookingsFormat.shortMonth(locale).format(booking.checkIn).uppercase(locale))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (booking.isWholeResort) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = colors.tertiary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = booking.guestName,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        textDecoration = if (cancelled) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            StatusPill(pill)
        }
        if (showDivider) HorizontalDivider(color = colors.outlineVariant)
    }
}

@Composable
private fun DateTile(booking: BookingRowUi, month: String) {
    val colors = MaterialTheme.colorScheme
    val hatch = rememberResortHatchBrush()
    val content = when {
        booking.isWholeResort -> colors.onTertiaryContainer
        booking.isToday -> colors.onSecondaryContainer
        else -> colors.onSurface
    }
    val base = Modifier.size(width = 44.dp, height = 48.dp)
    val tile = when {
        booking.isWholeResort -> base.background(hatch, TileShape).border(1.dp, colors.tertiary, TileShape)
        booking.isToday -> base.background(colors.secondaryContainer, TileShape)
        booking.isHold -> base
            .background(colors.surfaceContainer, TileShape)
            .dashedBorder(width = 1.5.dp, color = colors.outline, cornerRadius = 12.dp, dashLength = 4.dp, gapLength = 3.dp)
        else -> base.background(colors.surfaceContainer, TileShape)
    }
    Column(tile, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(booking.checkIn.dayOfMonth.toString(), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = content, lineHeight = 18.sp)
        Text(
            text = month,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.06.em,
            color = content,
            lineHeight = 10.sp,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

private class PillStyle(val label: String, val container: Color, val content: Color, val border: Color? = null, val dashed: Boolean = false)

@Composable
private fun RowStatus.pill(): PillStyle {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        RowStatus.ARRIVING -> PillStyle(stringResource(R.string.bookings_status_arriving), colors.tertiaryContainer, colors.onTertiaryContainer)
        RowStatus.LEAVING -> PillStyle(stringResource(R.string.bookings_status_leaving), colors.tertiaryContainer, colors.onTertiaryContainer)
        RowStatus.IN_HOUSE -> PillStyle(stringResource(R.string.bookings_status_in_house), colors.primaryContainer, colors.onPrimaryContainer)
        RowStatus.CHECKED_OUT -> PillStyle(stringResource(R.string.bookings_status_checked_out), Color.Transparent, colors.onSurfaceVariant, colors.outlineVariant)
        RowStatus.CANCELLED -> PillStyle(stringResource(R.string.bookings_status_cancelled), Color.Transparent, colors.error, colors.error)
        RowStatus.ON_HOLD -> PillStyle(stringResource(R.string.bookings_status_on_hold), colors.surfaceContainerHighest, colors.onSurface, colors.outline, dashed = true)
        RowStatus.WHOLE_RESORT -> PillStyle(stringResource(R.string.bookings_status_whole_resort), colors.tertiary, colors.onTertiary)
        RowStatus.CONFIRMED -> PillStyle(stringResource(R.string.bookings_status_confirmed), Color.Transparent, colors.primary, colors.primary)
    }
}

@Composable
private fun StatusPill(pill: PillStyle) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        shape = shape,
        color = pill.container,
        contentColor = pill.content,
        border = pill.border?.takeUnless { pill.dashed }?.let { BorderStroke(1.dp, it) },
        modifier = if (pill.dashed && pill.border != null) {
            Modifier.dashedBorder(width = 1.5.dp, color = pill.border, cornerRadius = 8.dp, dashLength = 4.dp, gapLength = 3.dp)
        } else {
            Modifier
        },
    ) {
        Box(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(pill.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}
