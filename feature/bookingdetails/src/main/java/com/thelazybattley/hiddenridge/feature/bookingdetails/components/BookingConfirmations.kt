package com.thelazybattley.hiddenridge.feature.bookingdetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.bookingdetails.BookingDetailsUi
import com.thelazybattley.hiddenridge.feature.bookingdetails.HousesUi
import com.thelazybattley.hiddenridge.feature.bookingdetails.R

/** Shown in place of the actions: cancelling keeps the booking on record, so it is confirmed inline. */
@Composable
internal fun CancelConfirmation(
    details: BookingDetailsUi,
    onKeep: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.errorContainer,
        contentColor = colors.onErrorContainer,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(
                    R.string.cancel_confirmation,
                    details.guestName,
                    freedHouses(details.houses),
                    DateFormats.stayRange(details.checkIn, details.checkOut, currentLocale()),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
            ) {
                TextButton(
                    onClick = onKeep,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.onErrorContainer),
                ) { Text(stringResource(R.string.action_keep_booking)) }
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
                ) { Text(stringResource(R.string.action_cancel_booking)) }
            }
        }
    }
}

/** Deleting erases history, so it gets a full dialog that points at the gentler option. */
@Composable
internal fun DeleteBookingDialog(
    details: BookingDetailsUi,
    onKeep: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val cancelLabel = stringResource(R.string.action_cancel_booking)
    val hint = stringResource(R.string.delete_hint, cancelLabel)
    AlertDialog(
        onDismissRequest = onKeep,
        icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.error) },
        title = { Text(stringResource(R.string.delete_title, details.guestName)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(
                        R.string.delete_message,
                        freedHouses(details.houses),
                        DateFormats.stayRange(details.checkIn, details.checkOut, currentLocale()),
                    ),
                )
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surfaceContainer) {
                    Text(
                        text = buildAnnotatedString {
                            val start = hint.indexOf(cancelLabel)
                            if (start < 0) {
                                append(hint)
                            } else {
                                append(hint.substring(0, start))
                                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(cancelLabel) }
                                append(hint.substring(start + cancelLabel.length))
                            }
                        },
                        color = colors.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onKeep) { Text(stringResource(R.string.action_keep_booking)) }
        },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
            ) { Text(stringResource(R.string.action_delete)) }
        },
    )
}

/** What becomes free if this booking goes: "Every house" or the booked house names. */
@Composable
private fun freedHouses(houses: HousesUi): String = when (houses) {
    is HousesUi.WholeResort -> stringResource(R.string.every_house)
    is HousesUi.Single -> houses.name
    is HousesUi.Several -> houses.names.joinToString()
}
