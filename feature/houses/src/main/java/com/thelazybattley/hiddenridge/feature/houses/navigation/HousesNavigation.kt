package com.thelazybattley.hiddenridge.feature.houses.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thelazybattley.hiddenridge.feature.houses.HousesScreen
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object HousesRoute

/**
 * @param onFreeHouseClick opens a new booking for a house that is free tonight.
 */
fun NavGraphBuilder.housesScreen(
    onBookingClick: (bookingId: String) -> Unit,
    onFreeHouseClick: (houseId: String, checkIn: LocalDate) -> Unit,
    onSearchClick: () -> Unit,
    onAddHouseClick: () -> Unit,
) {
    composable<HousesRoute> {
        HousesScreen(
            onBookingClick = onBookingClick,
            onFreeHouseClick = onFreeHouseClick,
            onSearchClick = onSearchClick,
            onAddHouseClick = onAddHouseClick,
        )
    }
}
