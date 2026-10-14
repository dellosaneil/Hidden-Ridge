package com.thelazybattley.hiddenridge.feature.overview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.feature.overview.MovementUi
import com.thelazybattley.hiddenridge.feature.overview.OverviewAction
import com.thelazybattley.hiddenridge.feature.overview.R

/** Who still needs checking in or out today, each with the button that does it. */
@Composable
internal fun MovementsCard(
    moves: List<MovementUi>,
    onAction: (OverviewAction) -> Unit,
    onOpenHouses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(stringResource(R.string.overview_moves_title), Modifier.weight(1f))
            TextButton(onClick = onOpenHouses) { Text(stringResource(R.string.overview_moves_houses)) }
        }
        if (moves.isEmpty()) {
            HorizontalDivider(color = colors.outlineVariant)
            Text(
                text = stringResource(R.string.overview_moves_none),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
        moves.forEach { move ->
            HorizontalDivider(color = colors.outlineVariant)
            MovementRow(move = move, onAction = onAction)
        }
    }
}

@Composable
private fun MovementRow(move: MovementUi, onAction: (OverviewAction) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val where = if (move.isWholeResort) stringResource(R.string.overview_whole_resort) else move.houseNames.joinToString()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(colors.tertiaryContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (move.isArrival) Icons.AutoMirrored.Outlined.Login else Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = colors.onTertiaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(move.guestName, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (move.isArrival) {
                    stringResource(R.string.overview_moves_arriving, where, pluralStringResource(R.plurals.overview_nights, move.nights, move.nights))
                } else {
                    stringResource(R.string.overview_moves_leaving, where)
                },
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val padding = PaddingValues(horizontal = 14.dp)
        if (move.isArrival) {
            val description = stringResource(R.string.overview_check_in_description, move.guestName)
            Button(
                onClick = { onAction(OverviewAction.CheckIn(move)) },
                contentPadding = padding,
                modifier = Modifier
                    .height(40.dp)
                    .semantics { contentDescription = description },
            ) { Text(stringResource(R.string.overview_check_in)) }
        } else {
            val description = stringResource(R.string.overview_check_out_description, move.guestName)
            FilledTonalButton(
                onClick = { onAction(OverviewAction.CheckOut(move)) },
                contentPadding = padding,
                modifier = Modifier
                    .height(40.dp)
                    .semantics { contentDescription = description },
            ) { Text(stringResource(R.string.overview_check_out)) }
        }
    }
}
