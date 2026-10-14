package com.thelazybattley.hiddenridge.feature.houses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.feature.houses.HousesUi
import com.thelazybattley.hiddenridge.feature.houses.R

/** Tonight at a glance: how full the resort is, and today's arrivals, departures and holds. */
@Composable
internal fun TonightCard(houses: HousesUi, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                val occupiedOf = stringResource(R.string.houses_occupied_of, houses.houseCount)
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium)) {
                            append(houses.occupiedTonight.toString())
                        }
                        append(" ")
                        append(occupiedOf)
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.houses_free_count, houses.freeTonight),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
            OccupancyMeter(occupied = houses.occupiedTonight, total = houses.houseCount)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Movement(Icons.AutoMirrored.Outlined.Login, stringResource(R.string.houses_arriving_count, houses.arrivingToday), colors.tertiary)
                Movement(Icons.AutoMirrored.Outlined.Logout, stringResource(R.string.houses_leaving_count, houses.leavingToday), colors.tertiary)
                Movement(
                    Icons.Outlined.HourglassEmpty,
                    pluralStringResource(R.plurals.houses_hold_count, houses.holds, houses.holds),
                    colors.onSurfaceVariant,
                )
            }
        }
    }
}

/** One segment per house, occupied ones first. */
@Composable
private fun OccupancyMeter(occupied: Int, total: Int) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(R.string.houses_meter_description, occupied, total)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(total) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(if (index < occupied) colors.primary else colors.outlineVariant, RoundedCornerShape(4.dp)),
            )
        }
    }
}

@Composable
private fun Movement(icon: ImageVector, label: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
