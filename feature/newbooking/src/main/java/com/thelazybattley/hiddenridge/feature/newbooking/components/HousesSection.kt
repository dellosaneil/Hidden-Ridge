package com.thelazybattley.hiddenridge.feature.newbooking.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.feature.newbooking.HouseTileState
import com.thelazybattley.hiddenridge.feature.newbooking.HouseTileUi
import com.thelazybattley.hiddenridge.feature.newbooking.HouseTypeCount
import com.thelazybattley.hiddenridge.feature.newbooking.HouseTypeTileUi
import com.thelazybattley.hiddenridge.feature.newbooking.NewBookingAction
import com.thelazybattley.hiddenridge.feature.newbooking.NewBookingFormUi
import com.thelazybattley.hiddenridge.feature.newbooking.R

private const val TILES_PER_ROW = 3
private val TileShape = RoundedCornerShape(12.dp)

@Composable
internal fun HousesSection(
    form: NewBookingFormUi,
    onAction: (NewBookingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedCount = form.draft.houseIds.size
    FormSection(
        title = stringResource(R.string.section_houses),
        aside = if (!form.draft.isWholeResort && selectedCount > 0) {
            pluralStringResource(R.plurals.houses_selected, selectedCount, selectedCount)
        } else {
            null
        },
        modifier = modifier,
    ) {
        if (form.draft.isWholeResort) {
            WholeResortCard(totalHouses = form.totalHouses, counts = form.houseCountsByType)
        } else {
            Row(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                form.houseTypes.forEach { tile ->
                    HouseTypeTile(
                        tile = tile,
                        onClick = { onAction(NewBookingAction.SelectHouseType(tile.type)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            form.houses.chunked(TILES_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { house ->
                        HouseTile(
                            house = house,
                            onClick = { onAction(NewBookingAction.ToggleHouse(house.houseId)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(TILES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Text(
                text = if (form.selectedHouseNames.isEmpty()) {
                    stringResource(R.string.houses_hint)
                } else {
                    stringResource(R.string.houses_selected_hint, form.selectedHouseNames.joinToString())
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun HouseTypeTile(tile: HouseTypeTileUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        selected = tile.isActive,
        onClick = onClick,
        shape = TileShape,
        color = if (tile.isActive) colors.secondaryContainer else Color.Transparent,
        contentColor = if (tile.isActive) colors.onSecondaryContainer else colors.onSurface,
        border = if (tile.isActive) null else BorderStroke(1.dp, colors.outline),
        modifier = modifier.height(56.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(tile.type.shortLabel),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                if (tile.selectedCount > 0) {
                    Surface(shape = CircleShape, color = colors.primary, contentColor = colors.onPrimary) {
                        Text(
                            text = tile.selectedCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp),
                        )
                    }
                }
            }
            Text(
                text = tile.freeCount?.let { stringResource(R.string.house_type_sleeps_free, tile.sleeps, it) }
                    ?: stringResource(R.string.house_type_sleeps, tile.sleeps),
                style = MaterialTheme.typography.labelSmall,
                color = if (tile.isActive) colors.onSecondaryContainer else colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HouseTile(house: HouseTileUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val status = houseStatus(house)
    val description = stringResource(R.string.house_tile_description, house.name, status)
    val (container, content, border) = when (house.state) {
        HouseTileState.SELECTED -> Triple(colors.primaryContainer, colors.onPrimaryContainer, BorderStroke(2.dp, colors.primary))
        HouseTileState.SELECTED_BUT_TAKEN -> Triple(colors.errorContainer, colors.onErrorContainer, BorderStroke(2.dp, colors.error))
        else -> Triple(Color.Transparent, colors.onSurface, BorderStroke(1.dp, colors.outline))
    }
    Surface(
        checked = house.isSelected,
        onCheckedChange = { onClick() },
        // A taken house can still be unticked if it was picked before the dates changed.
        enabled = house.state != HouseTileState.TAKEN,
        shape = TileShape,
        color = container,
        contentColor = content,
        border = border,
        modifier = modifier
            .height(92.dp)
            .alpha(if (house.state == HouseTileState.TAKEN) 0.6f else 1f)
            .semantics { contentDescription = description },
    ) {
        // The tile announces "name, status" once; the big label and lines below are visual only.
        Column(
            modifier = Modifier
                .padding(10.dp)
                .clearAndSetSemantics {},
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = house.shortLabel,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = if (house.shortLabel.length > 2) 18.sp else 28.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                when (house.state) {
                    HouseTileState.SELECTED -> Icon(Icons.Filled.CheckCircle, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    HouseTileState.SELECTED_BUT_TAKEN -> Icon(Icons.Filled.Error, null, tint = colors.error, modifier = Modifier.size(20.dp))
                    HouseTileState.TAKEN -> Icon(Icons.Filled.Block, null, tint = colors.error, modifier = Modifier.size(20.dp))
                    else -> Unit
                }
            }
            Column {
                Text(house.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (house.state) {
                        HouseTileState.TAKEN, HouseTileState.SELECTED_BUT_TAKEN -> colors.error
                        HouseTileState.FREE -> colors.primary
                        HouseTileState.NEEDS_DATES -> colors.onSurfaceVariant
                        HouseTileState.SELECTED -> content
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun houseStatus(house: HouseTileUi): String {
    val takenBy = house.takenBy
    return when {
        takenBy != null -> stringResource(
            R.string.house_status_taken,
            if (takenBy.isWholeResort) stringResource(R.string.house_status_whole_resort) else takenBy.guestName,
            DateFormats.stayRange(takenBy.checkIn, takenBy.checkOut, currentLocale()),
        )
        house.state == HouseTileState.NEEDS_DATES -> stringResource(R.string.house_status_pick_dates)
        house.state == HouseTileState.SELECTED -> stringResource(R.string.house_status_selected)
        else -> stringResource(R.string.house_status_free)
    }
}

@Composable
private fun WholeResortCard(totalHouses: Int, counts: List<HouseTypeCount>) {
    val countsText = counts.map { pluralStringResource(it.type.countLabel, it.count, it.count) }.joinToString()
    Surface(
        shape = TileShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Filled.Lock, contentDescription = null)
            Column {
                Text(
                    text = pluralStringResource(R.plurals.resort_all_blocked, totalHouses, totalHouses),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.resort_blocked_detail, countsText),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@get:StringRes
private val HouseType.shortLabel: Int
    get() = when (this) {
        HouseType.CABIN -> R.string.house_type_cabins
        HouseType.CASITA -> R.string.house_type_casitas
        HouseType.FAMILY_HOUSE -> R.string.house_type_family
    }

private val HouseType.countLabel: Int
    get() = when (this) {
        HouseType.CABIN -> R.plurals.count_cabins
        HouseType.CASITA -> R.plurals.count_casitas
        HouseType.FAMILY_HOUSE -> R.plurals.count_family_houses
    }
