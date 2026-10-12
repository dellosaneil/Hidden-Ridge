package com.thelazybattley.hiddenridge.feature.newbooking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.component.LabeledBox
import com.thelazybattley.hiddenridge.core.designsystem.component.OptionChip
import com.thelazybattley.hiddenridge.core.designsystem.component.dashedBorder
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.DraftProblem
import com.thelazybattley.hiddenridge.feature.newbooking.NewBookingAction
import com.thelazybattley.hiddenridge.feature.newbooking.NewBookingFormUi
import com.thelazybattley.hiddenridge.feature.newbooking.R

private val HoldOptions = listOf(1 to R.string.hold_1_day, 3 to R.string.hold_3_days, 7 to R.string.hold_1_week)

@Composable
internal fun GuestSection(
    form: NewBookingFormUi,
    onAction: (NewBookingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = form.draft
    val nameMissing = form.showProblems && DraftProblem.MISSING_GUEST_NAME in form.problems
    FormSection(title = stringResource(R.string.section_guest), modifier = modifier) {
        OutlinedTextField(
            value = draft.guestName,
            onValueChange = { onAction(NewBookingAction.SetGuestName(it)) },
            label = { Text(stringResource(R.string.field_guest_name)) },
            isError = nameMissing,
            supportingText = if (nameMissing) {
                { Text(stringResource(R.string.error_guest_name)) }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
            OutlinedTextField(
                value = draft.phone,
                onValueChange = { onAction(NewBookingAction.SetPhone(it)) },
                label = { Text(stringResource(R.string.field_phone)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f),
            )
            GuestCounter(
                count = draft.guestCount,
                onRemove = { onAction(NewBookingAction.RemoveGuest) },
                onAdd = { onAction(NewBookingAction.AddGuest) },
                modifier = Modifier.weight(1f),
            )
        }
        CapacityNote(form)
        SegmentedChoice(
            options = listOf(
                SegmentOption(BookingStatus.CONFIRMED, stringResource(R.string.status_confirmed), Icons.Outlined.CheckCircle),
                SegmentOption(BookingStatus.HOLD, stringResource(R.string.status_hold), Icons.Outlined.HourglassEmpty),
            ),
            selected = draft.status,
            onSelect = { onAction(NewBookingAction.SetStatus(it)) },
        )
        if (draft.isHold) HoldOptions(form = form, onAction = onAction)
        OutlinedTextField(
            value = draft.note,
            onValueChange = { onAction(NewBookingAction.SetNote(it)) },
            label = { Text(stringResource(R.string.field_note)) },
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GuestCounter(count: Int, onRemove: () -> Unit, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    LabeledBox(label = stringResource(R.string.field_guests), modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onRemove, enabled = count > 1) {
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.guests_fewer))
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.guests_more))
            }
        }
    }
}

@Composable
private fun CapacityNote(form: NewBookingFormUi) {
    if (form.capacity == 0) return
    val over = form.isOverCapacity
    val text = when {
        form.draft.isWholeResort && over -> stringResource(R.string.capacity_resort_over, form.capacity)
        form.draft.isWholeResort -> stringResource(R.string.capacity_resort, form.capacity)
        over -> stringResource(R.string.capacity_selected_over, form.capacity)
        else -> stringResource(R.string.capacity_selected, form.capacity)
    }
    val color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (over) Icon(Icons.Filled.Warning, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Composable
private fun HoldOptions(form: NewBookingFormUi, onAction: (NewBookingAction) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val heldHouses = when {
        form.draft.isWholeResort -> stringResource(R.string.hold_the_whole_resort)
        form.selectedHouseNames.isNotEmpty() -> form.selectedHouseNames.joinToString()
        else -> stringResource(R.string.hold_the_houses)
    }
    Surface(shape = RoundedCornerShape(12.dp), color = colors.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .size(width = 28.dp, height = 16.dp)
                        .dashedBorder(width = 2.dp, color = colors.outline, cornerRadius = 4.dp, dashLength = 3.dp, gapLength = 2.dp),
                )
                Text(stringResource(R.string.hold_explainer, heldHouses), style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = stringResource(R.string.hold_release_label),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HoldOptions.forEach { (days, label) ->
                    OptionChip(
                        label = stringResource(label),
                        selected = form.draft.holdDays == days,
                        onClick = { onAction(NewBookingAction.SetHoldDays(days)) },
                    )
                }
            }
            form.holdUntil?.let { releaseDate ->
                val date = DateFormats.weekdayDayMonth(currentLocale()).format(releaseDate)
                val sentence = stringResource(R.string.hold_releases_on, date)
                Text(
                    text = buildAnnotatedString {
                        val start = sentence.indexOf(date)
                        append(sentence.substring(0, start))
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(date) }
                        append(sentence.substring(start + date.length))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
