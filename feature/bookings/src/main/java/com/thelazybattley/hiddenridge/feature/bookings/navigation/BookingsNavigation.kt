package com.thelazybattley.hiddenridge.feature.bookings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thelazybattley.hiddenridge.feature.bookings.BookingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object BookingsRoute

fun NavGraphBuilder.bookingsScreen(
    onBookingClick: (bookingId: String) -> Unit,
    onNewBookingClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    composable<BookingsRoute> {
        BookingsScreen(
            onBookingClick = onBookingClick,
            onNewBookingClick = onNewBookingClick,
            onSearchClick = onSearchClick,
        )
    }
}
