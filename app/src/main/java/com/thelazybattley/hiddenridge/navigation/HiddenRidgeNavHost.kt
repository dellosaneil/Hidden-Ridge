package com.thelazybattley.hiddenridge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.thelazybattley.hiddenridge.feature.availability.navigation.availabilitySheet
import com.thelazybattley.hiddenridge.feature.availability.navigation.closeAvailability
import com.thelazybattley.hiddenridge.feature.availability.navigation.navigateToAvailability
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.bookingDetailsSheet
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.closeBookingDetails
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.navigateToBookingDetails
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.closeNewBooking
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.navigateToNewBooking
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.newBookingSheet
import com.thelazybattley.hiddenridge.feature.timeline.navigation.TimelineRoute
import com.thelazybattley.hiddenridge.feature.timeline.navigation.moveBookingOnTimeline
import com.thelazybattley.hiddenridge.feature.timeline.navigation.timelineScreen

/**
 * @param onShowMessage shows a short confirmation, such as after a booking is cancelled.
 * @param onNotBuiltYet called for destinations whose screens don't exist yet
 *   (reschedule, edit, month picker).
 * @param onMovingChange true while a booking is picked up on the timeline, when the move bar
 *   takes the navigation bar's place.
 */
@Composable
fun HiddenRidgeNavHost(
    navController: NavHostController,
    onShowMessage: (String) -> Unit,
    onNotBuiltYet: () -> Unit,
    onMovingChange: (isMoving: Boolean) -> Unit,
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
            onEmptyDayClick = { houseId, date -> navController.navigateToNewBooking(houseIds = listOf(houseId), checkIn = date) },
            onNewBookingClick = { navController.navigateToNewBooking() },
            onCheckAvailabilityClick = { navController.navigateToAvailability() },
            onMonthTitleClick = onNotBuiltYet,
            onMovingChange = onMovingChange,
            onShowMessage = onShowMessage,
        )
        bookingDetailsSheet(
            onDismiss = navController::closeBookingDetails,
            onReschedule = leaveBookingDetailsFor,
            // The booking is picked up on the timeline, where it can be dragged.
            onMove = navController::moveBookingOnTimeline,
            onEditDetails = leaveBookingDetailsFor,
            onShowMessage = onShowMessage,
        )
        newBookingSheet(
            onDismiss = navController::closeNewBooking,
            onShowMessage = onShowMessage,
        )
        availabilitySheet(
            onDismiss = navController::closeAvailability,
            onBook = { houseIds, wholeResort, checkIn, checkOut ->
                navController.closeAvailability()
                navController.navigateToNewBooking(houseIds = houseIds, wholeResort = wholeResort, checkIn = checkIn, checkOut = checkOut)
            },
            // Opens on top, so closing the booking returns to the same check.
            onViewBooking = navController::navigateToBookingDetails,
        )
    }
}
