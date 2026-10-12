package com.thelazybattley.hiddenridge.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.ui.graphics.vector.ImageVector
import com.thelazybattley.hiddenridge.R
import com.thelazybattley.hiddenridge.feature.timeline.navigation.TimelineRoute

/** The bottom navigation tabs. [route] is null for tabs whose screens aren't built yet. */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val label: Int,
    val route: Any?,
) {
    TIMELINE(Icons.Filled.ViewTimeline, Icons.Outlined.ViewTimeline, R.string.nav_timeline, TimelineRoute),
    BOOKINGS(Icons.AutoMirrored.Filled.EventNote, Icons.AutoMirrored.Outlined.EventNote, R.string.nav_bookings, null),
    HOUSES(Icons.Filled.Cottage, Icons.Outlined.Cottage, R.string.nav_houses, null),
    SETTINGS(Icons.Filled.Settings, Icons.Outlined.Settings, R.string.nav_settings, null),
}
