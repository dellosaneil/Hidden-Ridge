package com.thelazybattley.hiddenridge.feature.newbooking.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.thelazybattley.hiddenridge.feature.newbooking.NewBookingSheet
import java.time.LocalDate
import kotlinx.serialization.Serializable

/**
 * Everything is optional: the form can open empty, or prefilled from a tapped night on the
 * timeline or a stay found free in Check availability. With [bookingId] it edits that booking
 * instead.
 */
@Serializable
data class NewBookingRoute(
    val houseIds: List<String> = emptyList(),
    val wholeResort: Boolean = false,
    val checkInEpochDay: Long? = null,
    val checkOutEpochDay: Long? = null,
    val bookingId: String? = null,
)

fun NavController.navigateToNewBooking(
    houseIds: List<String> = emptyList(),
    wholeResort: Boolean = false,
    checkIn: LocalDate? = null,
    checkOut: LocalDate? = null,
) {
    val route = NewBookingRoute(
        houseIds = houseIds,
        wholeResort = wholeResort,
        checkInEpochDay = checkIn?.toEpochDay(),
        checkOutEpochDay = checkOut?.toEpochDay(),
    )
    navigate(route) { launchSingleTop = true }
}

/** The booking form on an existing booking, for any of its details. */
fun NavController.navigateToEditBooking(bookingId: String) {
    navigate(NewBookingRoute(bookingId = bookingId)) { launchSingleTop = true }
}

/** Closes the form; safe to call when it is already gone. */
fun NavController.closeNewBooking() {
    popBackStack<NewBookingRoute>(inclusive = true)
}

/** The booking form, new or editing, shown as a full-height bottom sheet. */
fun NavGraphBuilder.newBookingSheet(
    onDismiss: () -> Unit,
    onShowMessage: (message: String) -> Unit,
) {
    dialog<NewBookingRoute>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        NewBookingSheet(onDismiss = onDismiss, onShowMessage = onShowMessage)
    }
}
