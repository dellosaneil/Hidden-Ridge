package com.thelazybattley.hiddenridge.feature.overview.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thelazybattley.hiddenridge.feature.overview.OverviewScreen
import kotlinx.serialization.Serializable

@Serializable
data object OverviewRoute

/**
 * @param onOpenHouses opens the Houses tab, from the tonight tiles and today's movements.
 * @param onOpenBookings opens the Bookings tab, from the holds tile.
 */
fun NavGraphBuilder.overviewScreen(
    onOpenHouses: () -> Unit,
    onOpenBookings: () -> Unit,
) {
    composable<OverviewRoute> {
        OverviewScreen(onOpenHouses = onOpenHouses, onOpenBookings = onOpenBookings)
    }
}
