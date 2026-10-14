package com.thelazybattley.hiddenridge.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val FieldShape = RoundedCornerShape(4.dp)

/**
 * An outlined box with a floating label, matching [androidx.compose.material3.OutlinedTextField],
 * for controls that aren't text input (a date to pick, a guest counter).
 */
@Composable
fun LabeledBox(
    label: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier.padding(top = 8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(FieldShape)
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) colors.primary else colors.outline,
                    shape = FieldShape,
                ),
            contentAlignment = Alignment.CenterStart,
            content = content,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier
                .offset(x = 12.dp, y = (-8).dp)
                .background(colors.surfaceContainerLow)
                .padding(horizontal = 4.dp),
        )
    }
}

/**
 * A filter chip that shows a check while selected, as the design's option chips do.
 *
 * @param icon shown before the label while unselected, such as ∞ for a hold with no limit.
 */
@Composable
fun OptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        leadingIcon = when {
            selected -> {
                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            }
            icon != null -> {
                { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
            }
            else -> null
        },
        modifier = modifier,
    )
}
