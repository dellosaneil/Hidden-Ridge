package com.thelazybattley.hiddenridge.feature.addhouse.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.component.LabeledBox
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.HouseDraft
import com.thelazybattley.hiddenridge.core.domain.model.HouseProblem
import com.thelazybattley.hiddenridge.feature.addhouse.AddHouseAction
import com.thelazybattley.hiddenridge.feature.addhouse.AddHouseFormUi
import com.thelazybattley.hiddenridge.feature.addhouse.NameHint
import com.thelazybattley.hiddenridge.feature.addhouse.R

/** The house's own details: name, first bookable night, how many it sleeps, and a note. */
@Composable
internal fun HouseSection(form: AddHouseFormUi, onAction: (AddHouseAction) -> Unit, modifier: Modifier = Modifier) {
    FormSection(title = stringResource(R.string.addhouse_section_house), modifier = modifier, spacing = 16.dp) {
        NameField(form = form, onAction = onAction)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val date = DateFormats.weekdayDayMonthYear(currentLocale()).format(form.bookableFrom)
                val changeLabel = stringResource(R.string.addhouse_change_bookable_from)
                LabeledBox(
                    label = stringResource(R.string.addhouse_field_bookable_from),
                    modifier = Modifier.clickable(onClickLabel = changeLabel, role = Role.Button) { onAction(AddHouseAction.OpenDatePicker) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(date, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), maxLines = 1)
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HelpText(stringResource(R.string.addhouse_bookable_from_help))
            }
            SleepsCounter(
                sleeps = form.sleeps,
                onRemove = { onAction(AddHouseAction.RemoveSleeps) },
                onAdd = { onAction(AddHouseAction.AddSleeps) },
                isError = form.shows(HouseProblem.SLEEPS_OUT_OF_RANGE),
            )
        }
        OutlinedTextField(
            value = form.note,
            onValueChange = { onAction(AddHouseAction.SetNote(it)) },
            label = { Text(stringResource(R.string.addhouse_field_note)) },
            placeholder = { Text(stringResource(R.string.addhouse_note_placeholder)) },
            supportingText = { Text(stringResource(R.string.addhouse_note_help)) },
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NameField(form: AddHouseFormUi, onAction: (AddHouseAction) -> Unit) {
    val problem = when {
        form.shows(HouseProblem.MISSING_NAME) -> R.string.addhouse_problem_missing_name
        form.shows(HouseProblem.NAME_TAKEN) -> R.string.addhouse_problem_name_taken
        else -> null
    }
    OutlinedTextField(
        value = form.name,
        onValueChange = { onAction(AddHouseAction.SetName(it)) },
        label = { Text(stringResource(R.string.addhouse_field_name)) },
        placeholder = { Text(stringResource(R.string.addhouse_name_placeholder)) },
        isError = problem != null,
        supportingText = {
            Text(
                when {
                    problem != null -> stringResource(problem)
                    form.nameHint is NameHint.SuggestedFrom ->
                        stringResource(R.string.addhouse_name_help_suggested, form.nameHint.groupName.lowercase(currentLocale()))
                    form.nameHint == NameHint.NumberedFromType -> stringResource(R.string.addhouse_name_help_numbered)
                    else -> stringResource(R.string.addhouse_name_help)
                },
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SleepsCounter(sleeps: Int, onRemove: () -> Unit, onAdd: () -> Unit, isError: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LabeledBox(label = stringResource(R.string.addhouse_field_sleeps), isActive = isError, modifier = Modifier.width(140.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove, enabled = sleeps > HouseDraft.SLEEPS_RANGE.first) {
                    Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.addhouse_sleeps_fewer), tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = sleeps.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
                IconButton(onClick = onAdd, enabled = sleeps < HouseDraft.SLEEPS_RANGE.last) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.addhouse_sleeps_more), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (isError) HelpText(stringResource(R.string.addhouse_problem_sleeps), isError = true)
    }
}

@Composable
internal fun HelpText(text: String, isError: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}
