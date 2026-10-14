package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.component.dashedBorder
import com.thelazybattley.hiddenridge.feature.timeline.R

/** Faded placeholder rows hinting at the timeline to come: label width, bar start, bar width, opacity. */
private val GhostRows = listOf(
    GhostRow(labelWidth = 56.dp, barX = 30.dp, barWidth = 120.dp, alpha = 0.9f),
    GhostRow(labelWidth = 44.dp, barX = 100.dp, barWidth = 150.dp, alpha = 0.6f),
    GhostRow(labelWidth = 64.dp, barX = 12.dp, barWidth = 90.dp, alpha = 0.35f),
)

private class GhostRow(val labelWidth: Dp, val barX: Dp, val barWidth: Dp, val alpha: Float)

/** Shown in place of the grid until the resort has a house, leading straight to adding one. */
@Composable
internal fun EmptyTimeline(onAddHouseClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize()) {
        Column(Modifier.clearAndSetSemantics {}) {
            GhostRows.forEach { ghost ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(TimelineDimens.HouseRowHeight)
                        .alpha(ghost.alpha)
                        .bottomDashedBorder(colors.outlineVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .width(TimelineDimens.HouseColumnWidth)
                            .fillMaxHeight()
                            .endDashedBorder(colors.outlineVariant)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Surface(shape = RoundedCornerShape(5.dp), color = colors.surfaceContainerHighest) {
                            Box(Modifier.size(width = ghost.labelWidth, height = 10.dp))
                        }
                    }
                    Box(
                        Modifier
                            .padding(vertical = 8.dp)
                            .offset(x = ghost.barX)
                            .size(width = ghost.barWidth, height = TimelineDimens.BarHeight)
                            .dashedBorder(width = 2.dp, color = colors.outlineVariant, cornerRadius = 8.dp),
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 32.dp, end = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AddHome, contentDescription = null, modifier = Modifier.size(36.dp))
                }
            }
            Text(
                text = stringResource(R.string.timeline_empty_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, lineHeight = 28.sp),
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.timeline_empty_body),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 290.dp),
            )
            Button(
                onClick = onAddHouseClick,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(min = 48.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.timeline_empty_action), modifier = Modifier.padding(start = 8.dp))
            }
            Text(
                text = stringResource(R.string.timeline_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun Modifier.bottomDashedBorder(color: Color): Modifier = drawBehind {
    dashedLine(color, Offset(0f, size.height), Offset(size.width, size.height))
}

private fun Modifier.endDashedBorder(color: Color): Modifier = drawBehind {
    dashedLine(color, Offset(size.width, 0f), Offset(size.width, size.height))
}

private fun DrawScope.dashedLine(color: Color, start: Offset, end: Offset) {
    val stroke = 1.dp.toPx()
    val inset = Offset(if (start.x == end.x) -stroke / 2 else 0f, if (start.y == end.y) -stroke / 2 else 0f)
    drawLine(
        color = color,
        start = start + inset,
        end = end + inset,
        strokeWidth = stroke,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 3.dp.toPx())),
    )
}
