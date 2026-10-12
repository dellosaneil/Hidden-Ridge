package com.thelazybattley.hiddenridge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.thelazybattley.hiddenridge.feature.timeline.navigation.TimelineRoute
import com.thelazybattley.hiddenridge.feature.timeline.navigation.timelineScreen

/**
 * @param onNotBuiltYet called for destinations whose screens don't exist yet
 *   (booking details, new booking, availability, month picker).
 */
@Composable
fun HiddenRidgeNavHost(
    navController: NavHostController,
    onNotBuiltYet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TimelineRoute,
        modifier = modifier,
    ) {
        timelineScreen(
            onBookingClick = { onNotBuiltYet() },
            onEmptyDayClick = { _, _ -> onNotBuiltYet() },
            onNewBookingClick = onNotBuiltYet,
            onCheckAvailabilityClick = onNotBuiltYet,
            onMonthTitleClick = onNotBuiltYet,
        )
    }
}
