package com.thelazybattley.hiddenridge.feature.addhouse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thelazybattley.hiddenridge.core.designsystem.component.TallDestinationBottomSheet
import com.thelazybattley.hiddenridge.feature.addhouse.components.HouseSection
import com.thelazybattley.hiddenridge.feature.addhouse.components.PreviewCard
import com.thelazybattley.hiddenridge.feature.addhouse.components.TypeSection
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddHouseSheet(
    onDismiss: () -> Unit,
    onShowMessage: (message: String) -> Unit,
    viewModel: AddHouseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val resources = LocalContext.current.resources
    val hideThenDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    (uiState as? AddHouseUiState.Added)?.let { added ->
        LaunchedEffect(added) {
            onShowMessage(
                added.newGroupName?.let { resources.getString(R.string.addhouse_message_added_with_group, added.houseName, it) }
                    ?: resources.getString(R.string.addhouse_message_added, added.houseName),
            )
            hideThenDismiss()
        }
    }

    TallDestinationBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        when (val state = uiState) {
            AddHouseUiState.Loading, is AddHouseUiState.Added -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is AddHouseUiState.Editing -> AddHouseContent(form = state.form, onAction = viewModel::onAction, onClose = hideThenDismiss)
        }
    }
}

@Composable
internal fun AddHouseContent(
    form: AddHouseFormUi,
    onAction: (AddHouseAction) -> Unit,
    onClose: () -> Unit,
) {
    if (form.isDatePickerOpen) {
        BookableFromPicker(
            selected = form.bookableFrom,
            today = form.today,
            onPick = { onAction(AddHouseAction.SetBookableFrom(it)) },
            onDismiss = { onAction(AddHouseAction.CloseDatePicker) },
        )
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.addhouse_action_discard))
            }
            Text(
                text = stringResource(R.string.addhouse_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            TextButton(onClick = { onAction(AddHouseAction.Save) }, enabled = form.canSave) {
                Text(stringResource(R.string.addhouse_action_save))
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TypeSection(form = form, onAction = onAction)
            HouseSection(form = form, onAction = onAction)
            PreviewCard(form = form)
        }
        Footer(form = form, onAdd = { onAction(AddHouseAction.Save) })
    }
}

@Composable
private fun Footer(form: AddHouseFormUi, onAdd: () -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val after = form.houseCount + 1
            Text(
                text = when {
                    !form.isNewType -> stringResource(R.string.addhouse_footer_houses, form.houseCount, after)
                    form.groupName.isNotEmpty() -> stringResource(R.string.addhouse_footer_new_group, form.groupName, form.houseCount, after)
                    else -> stringResource(R.string.addhouse_footer_new_group_unnamed, form.houseCount, after)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onAdd,
                enabled = form.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Text(
                        text = if (form.name.isBlank()) stringResource(R.string.addhouse_action_add_house) else stringResource(R.string.addhouse_action_add, form.name.trim()),
                        modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
                    )
                }
            }
        }
    }
}

/** Material's picker counts days in UTC milliseconds; earlier than today can't be picked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookableFromPicker(
    selected: LocalDate,
    today: LocalDate,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val todayMillis = today.toUtcMillis()
    val selectableDates = remember(todayMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayMillis

            override fun isSelectableYear(year: Int): Boolean = year >= today.year
        }
    }
    val state = rememberDatePickerState(initialSelectedDateMillis = selected.toUtcMillis(), selectableDates = selectableDates)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let { onPick(it.toLocalDate()) } ?: onDismiss() },
            ) { Text(stringResource(R.string.addhouse_date_picker_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.addhouse_date_picker_cancel)) }
        },
    ) {
        DatePicker(state = state)
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
