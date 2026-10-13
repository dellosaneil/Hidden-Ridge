package com.thelazybattley.hiddenridge.feature.bookingdetails.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.thelazybattley.hiddenridge.feature.bookingdetails.BookingDetailsSheet
import kotlinx.serialization.Serializable

@Serializable
data class BookingDetailsRoute(val bookingId: String)

fun NavController.navigateToBookingDetails(bookingId: String) {
    navigate(BookingDetailsRoute(bookingId)) { launchSingleTop = true }
}

/** Closes the details sheet; safe to call when it is already gone. */
fun NavController.closeBookingDetails() {
    popBackStack<BookingDetailsRoute>(inclusive = true)
}

/**
 * Booking details, shown as a bottom sheet over the screen that opened it.
 * The action callbacks run after the sheet has animated away.
 */
fun NavGraphBuilder.bookingDetailsSheet(
    onDismiss: () -> Unit,
    onEditDetails: (bookingId: String) -> Unit,
    onShowMessage: (message: String) -> Unit,
) {
    dialog<BookingDetailsRoute>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        BookingDetailsSheet(
            onDismiss = onDismiss,
            onEditDetails = onEditDetails,
            onShowMessage = onShowMessage,
        )
    }
}
