package com.thelazybattley.hiddenridge.feature.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.feature.overview.MovementUi
import com.thelazybattley.hiddenridge.feature.overview.OverviewUi
import com.thelazybattley.hiddenridge.feature.overview.R

/** Tonight's occupancy, today's arrivals and departures, and holds, two by two. */
@Composable
internal fun TodayTiles(
    overview: OverviewUi,
    onOpenHouses: () -> Unit,
    onOpenBookings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val holds = overview.holds
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tile(
                icon = Icons.Outlined.Bed,
                iconTint = colors.primary,
                label = stringResource(R.string.overview_tile_occupied),
                value = overview.occupiedTonight,
                unit = stringResource(R.string.overview_tile_occupied_unit, overview.houseCount),
                sub = pluralStringResource(R.plurals.overview_tile_free, overview.freeTonight, overview.freeTonight),
                onClick = onOpenHouses,
                modifier = Modifier.weight(1f),
            )
            Tile(
                icon = Icons.AutoMirrored.Outlined.Login,
                iconTint = colors.tertiary,
                label = stringResource(R.string.overview_tile_arriving),
                value = overview.arrivals.size,
                unit = pluralStringResource(R.plurals.overview_unit_guests, overview.arrivals.size),
                sub = movementSummary(overview.arrivals) ?: stringResource(R.string.overview_no_arrivals),
                onClick = onOpenHouses,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tile(
                icon = Icons.AutoMirrored.Outlined.Logout,
                iconTint = colors.tertiary,
                label = stringResource(R.string.overview_tile_leaving),
                value = overview.departures.size,
                unit = pluralStringResource(R.plurals.overview_unit_guests, overview.departures.size),
                sub = movementSummary(overview.departures) ?: stringResource(R.string.overview_no_departures),
                onClick = onOpenHouses,
                modifier = Modifier.weight(1f),
            )
            Tile(
                icon = Icons.Outlined.HourglassEmpty,
                iconTint = colors.onSurfaceVariant,
                label = stringResource(R.string.overview_tile_holds),
                value = holds.count,
                unit = pluralStringResource(R.plurals.overview_unit_bookings, holds.count),
                sub = listOfNotNull(
                    holds.releasingSoon.takeIf { it > 0 }?.let { stringResource(R.string.overview_holds_soon, it) },
                    holds.later.takeIf { it > 0 }?.let { stringResource(R.string.overview_holds_later, it) },
                    holds.noLimit.takeIf { it > 0 }?.let { stringResource(R.string.overview_holds_no_limit, it) },
                ).joinToString(" · ").ifEmpty { stringResource(R.string.overview_holds_none) },
                onClick = onOpenBookings,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** "Grace Lim · Cabin 2", or "Grace Lim + 1 more" when several are due. */
@Composable
private fun movementSummary(moves: List<MovementUi>): String? {
    val first = moves.firstOrNull() ?: return null
    if (moves.size > 1) return stringResource(R.string.overview_tile_more, first.guestName, moves.size - 1)
    val where = if (first.isWholeResort) stringResource(R.string.overview_whole_resort) else first.houseNames.joinToString()
    return stringResource(R.string.overview_tile_movement, first.guestName, where)
}

@Composable
private fun Tile(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: Int,
    unit: String,
    sub: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(R.string.overview_tile_description, label, value.toString(), unit, sub)
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = colors.surfaceContainer,
        modifier = modifier
            .heightIn(min = 92.dp)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(value.toString(), fontSize = 32.sp, lineHeight = 32.sp)
                Text(unit, fontSize = 14.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
            }
            Text(sub, fontSize = 12.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
