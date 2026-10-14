package com.thelazybattley.hiddenridge.feature.houses.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.feature.houses.HousesFilter
import com.thelazybattley.hiddenridge.feature.houses.R

@Composable
internal fun HouseFilters(
    selected: HousesFilter,
    counts: Map<HousesFilter, Int>,
    onSelect: (HousesFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HousesFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(filter.label))
                        Text(
                            text = counts[filter]?.toString().orEmpty(),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .alpha(COUNT_ALPHA),
                        )
                    }
                },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}

private const val COUNT_ALPHA = 0.8f

private val HousesFilter.label: Int
    get() = when (this) {
        HousesFilter.ALL -> R.string.houses_filter_all
        HousesFilter.FREE -> R.string.houses_filter_free
        HousesFilter.OCCUPIED -> R.string.houses_filter_occupied
        HousesFilter.MOVING -> R.string.houses_filter_moving
    }
