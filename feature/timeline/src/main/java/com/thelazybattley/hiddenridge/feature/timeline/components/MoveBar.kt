package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.timeline.MoveStatus
import com.thelazybattley.hiddenridge.feature.timeline.MoveUi
import com.thelazybattley.hiddenridge.feature.timeline.R

/**
 * Takes the navigation bar's place while a booking is picked up: where it would land, whether
 * that clashes, arrows for small steps, and Cancel or Save.
 */
@Composable
internal fun MoveBar(
    move: MoveUi,
    onNudge: (days: Int, houses: Int) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    Column(modifier.background(colors.surfaceContainer)) {
        HorizontalDivider(color = colors.outlineVariant)
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.timeline_move_hint, move.guestName),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.timeline_move_target,
                    DateFormats.stayRange(move.checkIn, move.checkOut, locale),
                    if (move.isWholeResort) stringResource(R.string.timeline_move_whole_resort) else move.houseNames.joinToString(),
                ),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MoveStatusLine(move.status)
            Row(
                // The first arrow's icon lines up with the text above, as its 48dp target overhangs.
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = (-12).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArrowButton(Icons.Filled.ChevronLeft, R.string.timeline_move_earlier, move.canGoEarlier) { onNudge(-1, 0) }
                ArrowButton(Icons.Filled.ChevronRight, R.string.timeline_move_later, move.canGoLater) { onNudge(1, 0) }
                if (move.canChangeHouse) {
                    ArrowButton(Icons.Filled.ArrowUpward, R.string.timeline_move_house_above, move.canGoUp) { onNudge(0, -1) }
                    ArrowButton(Icons.Filled.ArrowDownward, R.string.timeline_move_house_below, move.canGoDown) { onNudge(0, 1) }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onCancel, enabled = !move.isSaving, modifier = Modifier.offset(x = 12.dp)) {
                    Text(stringResource(R.string.timeline_move_cancel))
                }
                Button(
                    onClick = onSave,
                    enabled = move.canSave,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    modifier = Modifier
                        .offset(x = 12.dp)
                        .padding(start = 4.dp)
                        .height(40.dp),
                ) { Text(stringResource(R.string.timeline_move_save)) }
            }
        }
    }
}

@Composable
private fun MoveStatusLine(status: MoveStatus) {
    val colors = MaterialTheme.colorScheme
    val (icon, color, text) = when (status) {
        is MoveStatus.Overlaps -> Triple(
            Icons.Outlined.ErrorOutline,
            colors.error,
            stringResource(
                R.string.timeline_move_overlaps,
                status.guestName,
                if (status.isWholeResort) stringResource(R.string.timeline_move_overlaps_whole_resort) else status.houseNames.joinToString(),
                DateFormats.stayRange(status.checkIn, status.checkOut, currentLocale()),
            ),
        )
        MoveStatus.Ready -> Triple(Icons.Outlined.CheckCircle, colors.primary, stringResource(R.string.timeline_move_ready))
        MoveStatus.NotMoved -> Triple(Icons.Outlined.CheckCircle, colors.primary, stringResource(R.string.timeline_move_not_moved))
    }
    Row(
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ArrowButton(icon: ImageVector, label: Int, enabled: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(icon, contentDescription = stringResource(label), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f))
    }
}
