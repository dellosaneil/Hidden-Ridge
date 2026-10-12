package com.thelazybattley.hiddenridge.feature.bookingdetails.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.feature.bookingdetails.BookingDetailsAction
import com.thelazybattley.hiddenridge.feature.bookingdetails.R

private val ActionHeight = 48.dp
private val ActionPadding = PaddingValues(horizontal = 12.dp)

/**
 * Three tiers, by consequence: the main changes (filled, tonal), smaller edits (outlined),
 * then the destructive pair set apart below a divider in the error color.
 */
@Composable
internal fun BookingActions(
    isOnHold: Boolean,
    onAction: (BookingDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionRow {
            Button(
                onClick = { onAction(BookingDetailsAction.Reschedule) },
                contentPadding = ActionPadding,
                modifier = actionModifier(),
            ) { ActionLabel(Icons.Outlined.EventRepeat, R.string.action_reschedule) }
            FilledTonalButton(
                onClick = { onAction(BookingDetailsAction.Move) },
                contentPadding = ActionPadding,
                modifier = actionModifier(),
            ) { ActionLabel(Icons.Outlined.OpenWith, R.string.action_move) }
        }
        ActionRow {
            OutlinedButton(
                onClick = { onAction(BookingDetailsAction.EditDetails) },
                contentPadding = ActionPadding,
                modifier = actionModifier(),
            ) { ActionLabel(Icons.Outlined.Edit, R.string.action_edit_details) }
            OutlinedButton(
                onClick = { onAction(BookingDetailsAction.ToggleHold) },
                contentPadding = ActionPadding,
                modifier = actionModifier(),
            ) {
                if (isOnHold) {
                    ActionLabel(Icons.Outlined.CheckCircle, R.string.action_confirm_booking)
                } else {
                    ActionLabel(Icons.Outlined.HourglassEmpty, R.string.action_put_on_hold)
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
        ActionRow {
            OutlinedButton(
                onClick = { onAction(BookingDetailsAction.RequestCancel) },
                contentPadding = ActionPadding,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                border = BorderStroke(1.dp, colors.error),
                modifier = actionModifier(),
            ) { ActionLabel(Icons.Outlined.EventBusy, R.string.action_cancel_booking) }
            TextButton(
                onClick = { onAction(BookingDetailsAction.RequestDelete) },
                contentPadding = ActionPadding,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.error),
                modifier = actionModifier(),
            ) { ActionLabel(Icons.Outlined.Delete, R.string.action_delete) }
        }
    }
}

@Composable
private fun ActionRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

private fun RowScope.actionModifier(): Modifier = Modifier
    .weight(1f)
    .heightIn(min = ActionHeight)

@Composable
private fun ActionLabel(icon: ImageVector, @StringRes label: Int) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
    Text(stringResource(label), maxLines = 1, overflow = TextOverflow.Ellipsis)
}
