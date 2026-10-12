package com.thelazybattley.hiddenridge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.bookingDetailsSheet
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.closeBookingDetails
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.navigateToBookingDetails
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.closeNewBooking
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.navigateToNewBooking
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.newBookingSheet
import com.thelazybattley.hiddenridge.feature.timeline.navigation.TimelineRoute
import com.thelazybattley.hiddenridge.feature.timeline.navigation.timelineScreen

/**
 * @param onShowMessage shows a short confirmation, such as after a booking is cancelled.
 * @param onNotBuiltYet called for destinations whose screens don't exist yet
 *   (reschedule, move, edit, availability, month picker).
 */
@Composable
fun HiddenRidgeNavHost(
    navController: NavHostController,
    onShowMessage: (String) -> Unit,
    onNotBuiltYet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val leaveBookingDetailsFor: (String) -> Unit = {
        navController.closeBookingDetails()
        onNotBuiltYet()
    }
    NavHost(
        navController = navController,
        startDestination = TimelineRoute,
        modifier = modifier,
    ) {
        timelineScreen(
            onBookingClick = navController::navigateToBookingDetails,
            onEmptyDayClick = { houseId, date -> navController.navigateToNewBooking(houseId = houseId, checkIn = date) },
            onNewBookingClick = { navController.navigateToNewBooking() },
            onCheckAvailabilityClick = onNotBuiltYet,
            onMonthTitleClick = onNotBuiltYet,
        )
        bookingDetailsSheet(
            onDismiss = navController::closeBookingDetails,
            onReschedule = leaveBookingDetailsFor,
            onMove = leaveBookingDetailsFor,
            onEditDetails = leaveBookingDetailsFor,
            onShowMessage = onShowMessage,
        )
        newBookingSheet(
            onDismiss = navController::closeNewBooking,
            onShowMessage = onShowMessage,
        )
    }
}
