package com.thelazybattley.hiddenridge.feature.newbooking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val FieldShape = RoundedCornerShape(4.dp)

/** A titled block of the form, with an optional note on the right such as "3 nights". */
@Composable
internal fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    aside: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )
            aside?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
        content()
    }
}

/**
 * An outlined box with a floating label, matching [androidx.compose.material3.OutlinedTextField],
 * for controls that aren't text input (a date to pick, a guest counter).
 */
@Composable
internal fun LabeledBox(
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

/** A filter chip that shows a check while selected, as the design's option chips do. */
@Composable
internal fun OptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else {
            null
        },
        modifier = modifier,
    )
}

internal class SegmentOption<T>(val value: T, val label: String, val icon: ImageVector)

@Composable
internal fun <T> SegmentedChoice(
    options: List<SegmentOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            val isSelected = option.value == selected
            SegmentedButton(
                selected = isSelected,
                onClick = { onSelect(option.value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                icon = {
                    SegmentedButtonDefaults.Icon(active = isSelected) {
                        Icon(option.icon, contentDescription = null, modifier = Modifier.size(SegmentedButtonDefaults.IconSize))
                    }
                },
            ) {
                Text(option.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
