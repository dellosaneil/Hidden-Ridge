package com.thelazybattley.hiddenridge.feature.houses.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.component.dashedBorder
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.feature.houses.HouseRowUi
import com.thelazybattley.hiddenridge.feature.houses.HouseStatus
import com.thelazybattley.hiddenridge.feature.houses.HouseSummary
import com.thelazybattley.hiddenridge.feature.houses.NightState
import com.thelazybattley.hiddenridge.feature.houses.R
import java.time.LocalDate
import java.time.format.TextStyle

private val NightSize = 18.dp
private val NightGap = 2.dp
private val NightShape = RoundedCornerShape(4.dp)

/** Narrow weekday letters over the strips, today's in bold. */
@Composable
internal fun WeekHeader(week: List<LocalDate>, today: LocalDate, modifier: Modifier = Modifier) {
    val locale = currentLocale()
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(NightGap, Alignment.End),
    ) {
        week.forEach { day ->
            val isToday = day == today
            Text(
                text = day.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                fontSize = 10.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) colors.primary else colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(NightSize),
            )
        }
    }
}

@Composable
internal fun GroupHeader(type: HouseType, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(type.groupLabel).uppercase(locale),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.08.em,
            color = colors.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        Text(
            text = stringResource(R.string.houses_sleeps, type.sleeps),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}

/**
 * One house: its status tonight, a line on who is in it or next, and a strip of the week's nights.
 * Taps open tonight's or this morning's booking; a free house opens a new booking instead.
 */
@Composable
internal fun HouseRow(
    house: HouseRowUi,
    todayIndex: Int,
    showDivider: Boolean,
    onBookingClick: (bookingId: String) -> Unit,
    onFreeHouseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val style = house.status.style()
    val summary = summaryText(house.summary)
    val description = stringResource(R.string.houses_row_description, house.name, style.label, summary)
    val bookingId = house.bookingId
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable(
                    onClickLabel = stringResource(if (bookingId != null) R.string.houses_view_booking else R.string.houses_new_booking),
                    role = Role.Button,
                ) { if (bookingId != null) onBookingClick(bookingId) else onFreeHouseClick() }
                .clearAndSetSemantics { contentDescription = description }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(style.tile, RoundedCornerShape(12.dp))
                    .then(if (style.tileBorder != null) Modifier.border(1.dp, style.tileBorder, RoundedCornerShape(12.dp)) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Icon(style.icon, contentDescription = null, tint = style.onTile, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = house.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    StatusPill(style)
                }
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            WeekStrip(nights = house.nights, todayIndex = todayIndex)
        }
        if (showDivider) HorizontalDivider(color = colors.outlineVariant)
    }
}

@Composable
private fun StatusPill(style: StatusStyle) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = style.pill,
        contentColor = style.onPill,
        border = style.pillBorder?.let { BorderStroke(1.dp, it) },
    ) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(style.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
private fun WeekStrip(nights: List<NightState>, todayIndex: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(NightGap)) {
        nights.forEachIndexed { index, night -> NightCell(night, isToday = index == todayIndex) }
    }
}

@Composable
private fun NightCell(night: NightState, isToday: Boolean, size: Dp = NightSize) {
    val colors = MaterialTheme.colorScheme
    val base = Modifier.size(size)
    val cell = when (night) {
        NightState.BOOKED -> base
            .background(colors.primaryContainer, NightShape)
            .then(if (isToday) Modifier.border(2.dp, colors.primary, NightShape) else Modifier)
        NightState.HOLD -> base
            .background(colors.surfaceContainerHighest, NightShape)
            .dashedBorder(width = 1.5.dp, color = colors.outline, cornerRadius = 4.dp, dashLength = 3.dp, gapLength = 2.dp)
        NightState.FREE -> base.border(
            if (isToday) 2.dp else 1.dp,
            if (isToday) colors.primary else colors.outlineVariant,
            NightShape,
        )
    }
    Box(cell)
}

/** Booked, hold and free swatches, and which nights the strips cover. */
@Composable
internal fun StripLegend(week: List<LocalDate>, modifier: Modifier = Modifier) {
    val locale = currentLocale()
    val first = remember(week, locale) { HousesFormat.weekdayDay(locale).format(week.first()) }
    val last = remember(week, locale) { HousesFormat.weekdayDayMonth(locale).format(week.last()) }
    val textStyle = MaterialTheme.typography.bodySmall
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf(
            NightState.BOOKED to R.string.houses_legend_booked,
            NightState.HOLD to R.string.houses_legend_hold,
            NightState.FREE to R.string.houses_legend_free,
        ).forEach { (night, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NightCell(night, isToday = false, size = 12.dp)
                Text(stringResource(label), style = textStyle, color = color)
            }
        }
        Text(stringResource(R.string.houses_legend_strip, first, last), style = textStyle, color = color)
    }
}

private class StatusStyle(
    val label: String,
    val icon: ImageVector,
    val tile: Color,
    val onTile: Color,
    val tileBorder: Color?,
    val pill: Color,
    val onPill: Color,
    val pillBorder: Color?,
)

@Composable
private fun HouseStatus.style(): StatusStyle {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        HouseStatus.OCCUPIED -> StatusStyle(
            label = stringResource(R.string.houses_status_occupied),
            icon = Icons.Outlined.Bed,
            tile = colors.primaryContainer, onTile = colors.onPrimaryContainer, tileBorder = null,
            pill = colors.primaryContainer, onPill = colors.onPrimaryContainer, pillBorder = null,
        )
        HouseStatus.ARRIVING -> StatusStyle(
            label = stringResource(R.string.houses_status_arriving),
            icon = Icons.AutoMirrored.Outlined.Login,
            tile = colors.tertiaryContainer, onTile = colors.onTertiaryContainer, tileBorder = null,
            pill = colors.tertiaryContainer, onPill = colors.onTertiaryContainer, pillBorder = null,
        )
        HouseStatus.LEAVING -> StatusStyle(
            label = stringResource(R.string.houses_status_leaving),
            icon = Icons.AutoMirrored.Outlined.Logout,
            tile = colors.tertiaryContainer, onTile = colors.onTertiaryContainer, tileBorder = null,
            pill = colors.tertiaryContainer, onPill = colors.onTertiaryContainer, pillBorder = null,
        )
        HouseStatus.FREE -> StatusStyle(
            label = stringResource(R.string.houses_status_free),
            icon = Icons.Outlined.Cottage,
            tile = Color.Transparent, onTile = colors.onSurfaceVariant, tileBorder = colors.outlineVariant,
            pill = Color.Transparent, onPill = colors.primary, pillBorder = colors.primary,
        )
    }
}

@Composable
private fun summaryText(summary: HouseSummary): String {
    val locale = currentLocale()
    return when (summary) {
        is HouseSummary.InHouse -> stringResource(
            R.string.houses_summary_in_house,
            summary.guestName,
            summary.night,
            summary.nights,
            HousesFormat.weekdayDay(locale).format(summary.checkOut),
        )
        is HouseSummary.Arriving -> stringResource(
            R.string.houses_summary_arriving,
            summary.guestName,
            pluralStringResource(R.plurals.houses_nights, summary.nights, summary.nights),
        )
        is HouseSummary.Leaving -> stringResource(R.string.houses_summary_leaving, summary.guestName)
        is HouseSummary.FreeUntil -> {
            val day = HousesFormat.weekdayDay(locale).format(summary.until)
            if (summary.isHold) {
                stringResource(R.string.houses_summary_free_hold, summary.guestName, day)
            } else {
                stringResource(R.string.houses_summary_free_until, day, summary.guestName)
            }
        }
        HouseSummary.FreeAllWeek -> stringResource(R.string.houses_summary_free_all_week)
    }
}

private val HouseType.groupLabel: Int
    get() = when (this) {
        HouseType.CABIN -> R.string.houses_group_cabins
        HouseType.CASITA -> R.string.houses_group_casitas
        HouseType.FAMILY_HOUSE -> R.string.houses_group_family_houses
    }
