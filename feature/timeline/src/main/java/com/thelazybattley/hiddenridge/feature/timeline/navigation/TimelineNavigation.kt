package com.thelazybattley.hiddenridge.feature.timeline.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thelazybattley.hiddenridge.feature.timeline.TimelineScreen
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object TimelineRoute

fun NavGraphBuilder.timelineScreen(
    onBookingClick: (bookingId: String) -> Unit,
    onEmptyDayClick: (houseId: String, date: LocalDate) -> Unit,
    onNewBookingClick: () -> Unit,
    onCheckAvailabilityClick: () -> Unit,
) {
    composable<TimelineRoute> {
        TimelineScreen(
            onBookingClick = onBookingClick,
            onEmptyDayClick = onEmptyDayClick,
            onNewBookingClick = onNewBookingClick,
            onCheckAvailabilityClick = onCheckAvailabilityClick,
        )
    }
}
