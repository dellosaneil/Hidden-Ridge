package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.feature.timeline.R

private val SwatchShape = RoundedCornerShape(3.dp)

/** What each mark on the timeline means. Scrolls sideways on narrow screens. */
@Composable
internal fun TimelineLegend(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(R.string.timeline_legend_free) {
            Modifier
                .background(colors.surface, SwatchShape)
                .border(1.dp, colors.outlineVariant, SwatchShape)
        }
        LegendItem(R.string.timeline_legend_booked) {
            Modifier.background(colors.primaryContainer, SwatchShape)
        }
        LegendItem(R.string.timeline_legend_hold) {
            Modifier
                .background(colors.surfaceContainerHighest, SwatchShape)
                .dashedBorder(2.dp, colors.outline, cornerRadius = 3.dp, dashLength = 3.dp, gapLength = 2.dp)
        }
        val resortHatch = rememberResortHatchBrush()
        LegendItem(R.string.timeline_legend_whole_resort) {
            Modifier
                .clip(SwatchShape)
                .background(resortHatch)
                .border(1.dp, colors.tertiary, SwatchShape)
        }
        val holdHatch = rememberResortHoldHatchBrush()
        LegendItem(R.string.timeline_legend_resort_hold) {
            Modifier
                .clip(SwatchShape)
                .background(colors.surface)
                .background(holdHatch)
                .dashedBorder(1.dp, colors.tertiary, cornerRadius = 3.dp, dashLength = 2.dp, gapLength = 2.dp)
        }
        LegendItem(R.string.timeline_legend_now, swatchWidth = 2.dp) {
            Modifier.background(colors.primary)
        }
    }
}

@Composable
private fun LegendItem(
    @StringRes label: Int,
    swatchWidth: Dp = 16.dp,
    swatch: () -> Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(width = swatchWidth, height = 12.dp)
                .then(swatch()),
        )
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
