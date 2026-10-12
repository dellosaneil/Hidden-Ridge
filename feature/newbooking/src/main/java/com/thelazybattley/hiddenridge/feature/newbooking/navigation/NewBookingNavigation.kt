package com.thelazybattley.hiddenridge.feature.newbooking.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.thelazybattley.hiddenridge.feature.newbooking.NewBookingSheet
import java.time.LocalDate
import kotlinx.serialization.Serializable

/** Both arguments are optional: tapping an empty night on the timeline prefills them. */
@Serializable
data class NewBookingRoute(
    val houseId: String? = null,
    val checkInEpochDay: Long? = null,
)

fun NavController.navigateToNewBooking(houseId: String? = null, checkIn: LocalDate? = null) {
    navigate(NewBookingRoute(houseId = houseId, checkInEpochDay = checkIn?.toEpochDay())) { launchSingleTop = true }
}

/** Closes the form; safe to call when it is already gone. */
fun NavController.closeNewBooking() {
    popBackStack<NewBookingRoute>(inclusive = true)
}

/** The new booking form, shown as a full-height bottom sheet. */
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
