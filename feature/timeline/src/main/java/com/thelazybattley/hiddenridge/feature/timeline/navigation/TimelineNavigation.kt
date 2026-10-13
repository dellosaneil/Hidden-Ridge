package com.thelazybattley.hiddenridge.feature.timeline.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thelazybattley.hiddenridge.feature.timeline.TimelineScreen
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object TimelineRoute

/** Saved-state key on the timeline's back stack entry holding a booking to pick up. */
private const val MOVE_BOOKING_REQUEST_KEY = "move_booking_request"

/**
 * Closes whatever is open over the timeline, such as booking details, and picks the booking up
 * there so it can be dragged to new dates or another house.
 */
fun NavController.moveBookingOnTimeline(bookingId: String) {
    popBackStack<TimelineRoute>(inclusive = false)
    getBackStackEntry<TimelineRoute>().savedStateHandle[MOVE_BOOKING_REQUEST_KEY] = bookingId
}

/**
 * @param onMovingChange true while a booking is picked up; the app hides its navigation bar so
 *   the move bar can take its place.
 */
fun NavGraphBuilder.timelineScreen(
    onBookingClick: (bookingId: String) -> Unit,
    onEmptyDayClick: (houseId: String, date: LocalDate) -> Unit,
    onNewBookingClick: () -> Unit,
    onCheckAvailabilityClick: () -> Unit,
    onMovingChange: (isMoving: Boolean) -> Unit,
    onShowMessage: (message: String) -> Unit,
) {
    composable<TimelineRoute> { entry ->
        val moveRequest by entry.savedStateHandle.getStateFlow<String?>(MOVE_BOOKING_REQUEST_KEY, null)
            .collectAsStateWithLifecycle()
        TimelineScreen(
            moveRequest = moveRequest,
            onMoveRequestHandled = { entry.savedStateHandle[MOVE_BOOKING_REQUEST_KEY] = null },
            onBookingClick = onBookingClick,
            onEmptyDayClick = onEmptyDayClick,
            onNewBookingClick = onNewBookingClick,
            onCheckAvailabilityClick = onCheckAvailabilityClick,
            onMovingChange = onMovingChange,
            onShowMessage = onShowMessage,
        )
    }
}
