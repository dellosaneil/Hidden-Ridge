package com.thelazybattley.hiddenridge.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.thelazybattley.hiddenridge.R
import com.thelazybattley.hiddenridge.feature.bookings.navigation.BookingsRoute
import com.thelazybattley.hiddenridge.feature.houses.navigation.HousesRoute
import com.thelazybattley.hiddenridge.feature.overview.navigation.OverviewRoute
import com.thelazybattley.hiddenridge.feature.timeline.navigation.TimelineRoute

/** The bottom navigation tabs. [route] is null for tabs whose screens aren't built yet. */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val label: Int,
    val route: Any?,
) {
    OVERVIEW(Icons.Filled.SpaceDashboard, Icons.Outlined.SpaceDashboard, R.string.nav_overview, OverviewRoute),
    TIMELINE(Icons.Filled.ViewTimeline, Icons.Outlined.ViewTimeline, R.string.nav_timeline, TimelineRoute),
    BOOKINGS(Icons.AutoMirrored.Filled.EventNote, Icons.AutoMirrored.Outlined.EventNote, R.string.nav_bookings, BookingsRoute),
    HOUSES(Icons.Filled.Cottage, Icons.Outlined.Cottage, R.string.nav_houses, HousesRoute),
    SETTINGS(Icons.Filled.Settings, Icons.Outlined.Settings, R.string.nav_settings, null),
}

/** Switches tabs the way the bottom bar does: each tab keeps its own back stack and state. */
fun NavController.navigateToTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
