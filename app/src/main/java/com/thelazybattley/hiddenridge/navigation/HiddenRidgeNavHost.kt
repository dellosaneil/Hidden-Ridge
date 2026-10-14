package com.thelazybattley.hiddenridge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.thelazybattley.hiddenridge.R
import com.thelazybattley.hiddenridge.feature.addhouse.navigation.addHouseSheet
import com.thelazybattley.hiddenridge.feature.addhouse.navigation.closeAddHouse
import com.thelazybattley.hiddenridge.feature.addhouse.navigation.navigateToAddHouse
import com.thelazybattley.hiddenridge.feature.availability.navigation.availabilitySheet
import com.thelazybattley.hiddenridge.feature.availability.navigation.closeAvailability
import com.thelazybattley.hiddenridge.feature.availability.navigation.navigateToAvailability
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.bookingDetailsSheet
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.closeBookingDetails
import com.thelazybattley.hiddenridge.feature.bookingdetails.navigation.navigateToBookingDetails
import com.thelazybattley.hiddenridge.feature.houses.navigation.housesScreen
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.closeNewBooking
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.navigateToEditBooking
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.navigateToNewBooking
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.newBookingSheet
import com.thelazybattley.hiddenridge.feature.timeline.navigation.TimelineRoute
import com.thelazybattley.hiddenridge.feature.timeline.navigation.timelineScreen

/**
 * @param onShowMessage shows a short confirmation, such as after a booking is cancelled.
 */
@Composable
fun HiddenRidgeNavHost(
    navController: NavHostController,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val notBuiltYetMessage = stringResource(R.string.not_built_yet)
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
            onAddHouseClick = navController::navigateToAddHouse,
        )
        housesScreen(
            onBookingClick = navController::navigateToBookingDetails,
            onFreeHouseClick = { houseId, checkIn -> navController.navigateToNewBooking(houseIds = listOf(houseId), checkIn = checkIn) },
            onSearchClick = { onShowMessage(notBuiltYetMessage) },
            onAddHouseClick = navController::navigateToAddHouse,
        )
        addHouseSheet(
            onDismiss = navController::closeAddHouse,
            onShowMessage = onShowMessage,
        )
        bookingDetailsSheet(
            onDismiss = navController::closeBookingDetails,
            // The form replaces the details sheet, so closing it returns to where details was opened from.
            onEditDetails = { bookingId ->
                navController.closeBookingDetails()
                navController.navigateToEditBooking(bookingId)
            },
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
