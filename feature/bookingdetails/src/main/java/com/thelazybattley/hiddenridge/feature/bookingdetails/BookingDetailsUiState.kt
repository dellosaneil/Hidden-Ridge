package com.thelazybattley.hiddenridge.feature.bookingdetails

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.thelazybattley.hiddenridge.core.domain.model.StayPhase
import java.time.LocalDate

internal sealed interface BookingDetailsUiState {
    data object Loading : BookingDetailsUiState

    data class Ready(
        val details: BookingDetailsUi,
        val confirmation: PendingConfirmation?,
    ) : BookingDetailsUiState

    /**
     * The sheet should close. [outcome] is null when the booking disappeared on its own;
     * [details] keeps the last content on screen while the sheet animates away.
     */
    data class Closed(
        val outcome: Outcome?,
        val details: BookingDetailsUi?,
    ) : BookingDetailsUiState
}

/** A destructive action waiting for the user to confirm it. */
internal enum class PendingConfirmation { CANCEL, DELETE }

internal enum class Outcome(@StringRes val message: Int) {
    CANCELLED(R.string.message_cancelled),
    DELETED(R.string.message_deleted),
}

internal sealed interface BookingDetailsAction {
    data object Reschedule : BookingDetailsAction
    data object Move : BookingDetailsAction
    data object EditDetails : BookingDetailsAction
    data object ToggleHold : BookingDetailsAction
    data object RequestCancel : BookingDetailsAction
    data object RequestDelete : BookingDetailsAction
    data object DismissConfirmation : BookingDetailsAction
    data object ConfirmCancel : BookingDetailsAction
    data object ConfirmDelete : BookingDetailsAction
}

@Immutable
internal data class BookingDetailsUi(
    val guestName: String,
    val kind: BookingKind,
    val status: BookingStatusUi,
    val stayPhase: StayPhase,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val nights: Int,
    val houses: HousesUi,
    val guestCount: Int,
    val phone: String,
    val note: String,
    /** Whether the stay can still be moved; false once it has checked out. */
    val canMove: Boolean,
) {
    val isOnHold: Boolean get() = status is BookingStatusUi.OnHold
}

internal enum class BookingKind { INDIVIDUAL, GROUP, WHOLE_RESORT }

@Immutable
internal sealed interface BookingStatusUi {
    data object Confirmed : BookingStatusUi

    data class OnHold(val until: LocalDate?) : BookingStatusUi
}

@Immutable
internal sealed interface HousesUi {
    data class Single(val name: String, val sleeps: Int) : HousesUi

    data class Several(val names: List<String>, val totalSleeps: Int) : HousesUi

    data class WholeResort(val houseCount: Int, val totalSleeps: Int) : HousesUi
}
