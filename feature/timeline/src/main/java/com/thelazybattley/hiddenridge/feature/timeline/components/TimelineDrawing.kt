package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

internal fun Modifier.endBorder(color: Color, width: Dp = TimelineDimens.Hairline): Modifier = drawBehind {
    val stroke = width.toPx()
    drawRect(color, topLeft = Offset(size.width - stroke, 0f), size = Size(stroke, size.height))
}

internal fun Modifier.bottomBorder(color: Color, width: Dp = TimelineDimens.Hairline): Modifier = drawBehind {
    val stroke = width.toPx()
    drawRect(color, topLeft = Offset(0f, size.height - stroke), size = Size(size.width, stroke))
}
