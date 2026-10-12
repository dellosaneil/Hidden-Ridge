package com.thelazybattley.hiddenridge.feature.availability.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.thelazybattley.hiddenridge.feature.availability.AvailabilitySheet
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object AvailabilityRoute

fun NavController.navigateToAvailability() {
    navigate(AvailabilityRoute) { launchSingleTop = true }
}

/** Closes the availability check; safe to call when it is already gone. */
fun NavController.closeAvailability() {
    popBackStack<AvailabilityRoute>(inclusive = true)
}

/**
 * The availability check, shown as a full-height bottom sheet.
 *
 * @param onBook runs after the sheet has animated away, to start a booking for the chosen stay.
 * @param onViewBooking opens a booking that's in the way; the check stays open underneath.
 */
fun NavGraphBuilder.availabilitySheet(
    onDismiss: () -> Unit,
    onBook: (houseIds: List<String>, wholeResort: Boolean, checkIn: LocalDate, checkOut: LocalDate) -> Unit,
    onViewBooking: (bookingId: String) -> Unit,
) {
    dialog<AvailabilityRoute>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        AvailabilitySheet(onDismiss = onDismiss, onBook = onBook, onViewBooking = onViewBooking)
    }
}
