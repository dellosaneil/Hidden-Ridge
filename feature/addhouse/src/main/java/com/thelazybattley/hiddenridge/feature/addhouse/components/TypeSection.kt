package com.thelazybattley.hiddenridge.feature.addhouse.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.component.dashedBorder
import com.thelazybattley.hiddenridge.core.designsystem.component.imageVector
import com.thelazybattley.hiddenridge.core.domain.model.HouseIcon
import com.thelazybattley.hiddenridge.core.domain.model.HouseProblem
import com.thelazybattley.hiddenridge.feature.addhouse.AddHouseAction
import com.thelazybattley.hiddenridge.feature.addhouse.AddHouseFormUi
import com.thelazybattley.hiddenridge.feature.addhouse.NewTypeIcons
import com.thelazybattley.hiddenridge.feature.addhouse.R
import com.thelazybattley.hiddenridge.feature.addhouse.TypeCardUi

private const val TYPES_PER_ROW = 2
private val CardShape = RoundedCornerShape(12.dp)

/** The existing types to pick from, a card to name a new one, and that new type's name and icon. */
@Composable
internal fun TypeSection(form: AddHouseFormUi, onAction: (AddHouseAction) -> Unit, modifier: Modifier = Modifier) {
    FormSection(title = stringResource(R.string.addhouse_section_type), modifier = modifier, spacing = 10.dp) {
        // Null stands for the "New type" card, which always comes last.
        val cards: List<TypeCardUi?> = form.types + null
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            cards.chunked(TYPES_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { type ->
                        if (type == null) {
                            TypeCard(
                                icon = Icons.Outlined.AddHome,
                                label = stringResource(R.string.addhouse_type_new),
                                note = stringResource(R.string.addhouse_type_new_note),
                                isSelected = form.isNewType,
                                isNew = true,
                                onClick = { onAction(AddHouseAction.SelectNewType) },
                                modifier = Modifier.weight(1f),
                            )
                        } else {
                            TypeCard(
                                icon = type.icon.imageVector,
                                label = type.name,
                                note = stringResource(R.string.addhouse_type_note, type.sleeps, type.houseCount),
                                isSelected = type.isSelected,
                                isNew = false,
                                onClick = { onAction(AddHouseAction.SelectType(type.id)) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    repeat(TYPES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        if (form.isNewType) NewTypeFields(form = form, onAction = onAction, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun TypeCard(
    icon: ImageVector,
    label: String,
    note: String,
    isSelected: Boolean,
    isNew: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val content = when {
        isSelected -> colors.onPrimaryContainer
        isNew -> colors.primary
        else -> colors.onSurface
    }
    Surface(
        selected = isSelected,
        onClick = onClick,
        shape = CardShape,
        color = if (isSelected) colors.primaryContainer else Color.Transparent,
        contentColor = content,
        border = when {
            isSelected -> BorderStroke(2.dp, colors.primary)
            isNew -> null
            else -> BorderStroke(1.dp, colors.outline)
        },
        modifier = modifier
            .heightIn(min = 72.dp)
            .then(if (isNew && !isSelected) Modifier.dashedBorder(1.dp, colors.primary, cornerRadius = 12.dp) else Modifier)
            .semantics { contentDescription = "$label, $note" },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.weight(1f))
                if (isSelected) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = note,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NewTypeFields(form: AddHouseFormUi, onAction: (AddHouseAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = CardShape, color = colors.surfaceContainer, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val problem = when {
                form.shows(HouseProblem.MISSING_TYPE_NAME) -> R.string.addhouse_problem_missing_type_name
                form.shows(HouseProblem.TYPE_NAME_TAKEN) -> R.string.addhouse_problem_type_name_taken
                else -> null
            }
            OutlinedTextField(
                value = form.newTypeName,
                onValueChange = { onAction(AddHouseAction.SetNewTypeName(it)) },
                label = { Text(stringResource(R.string.addhouse_field_type_name)) },
                isError = problem != null,
                supportingText = {
                    Text(
                        when {
                            problem != null -> stringResource(problem)
                            form.groupName.isNotEmpty() -> stringResource(R.string.addhouse_type_name_help, form.groupName)
                            else -> stringResource(R.string.addhouse_type_name_help_empty)
                        },
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.addhouse_field_icon),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NewTypeIcons.forEach { icon ->
                        IconChoice(icon = icon, isSelected = icon == form.newTypeIcon, onClick = { onAction(AddHouseAction.SetNewTypeIcon(icon)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun IconChoice(icon: HouseIcon, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val label = stringResource(icon.label)
    Surface(
        selected = isSelected,
        onClick = onClick,
        shape = CardShape,
        color = if (isSelected) colors.primaryContainer else Color.Transparent,
        contentColor = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        border = if (isSelected) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier
            .size(44.dp)
            .semantics {
                contentDescription = label
                role = Role.RadioButton
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon.imageVector, contentDescription = null, modifier = Modifier.size(22.dp))
        }
    }
}

private val HouseIcon.label: Int
    get() = when (this) {
        HouseIcon.TREE -> R.string.addhouse_icon_tree
        HouseIcon.CABIN -> R.string.addhouse_icon_cabin
        HouseIcon.COTTAGE -> R.string.addhouse_icon_cottage
        HouseIcon.TENT -> R.string.addhouse_icon_tent
        HouseIcon.VILLA -> R.string.addhouse_icon_villa
        HouseIcon.VILLAGE -> R.string.addhouse_icon_village
    }
