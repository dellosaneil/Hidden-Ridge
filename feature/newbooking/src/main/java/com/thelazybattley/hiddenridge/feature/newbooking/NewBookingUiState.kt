package com.thelazybattley.hiddenridge.feature.newbooking

import androidx.compose.runtime.Immutable
import com.thelazybattley.hiddenridge.core.domain.model.BookingDraft
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.DraftProblem
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import java.time.LocalDate
import java.time.YearMonth

internal sealed interface NewBookingUiState {
    data object Loading : NewBookingUiState

    data class Editing(val form: NewBookingFormUi) : NewBookingUiState

    /** Saved; the sheet should close and confirm what was booked or changed. */
    data class Saved(
        val purpose: FormPurpose,
        val guestName: String,
        val checkIn: LocalDate,
        val checkOut: LocalDate,
        val isHold: Boolean,
    ) : NewBookingUiState

    /** The booking being edited was deleted or cancelled meanwhile; the sheet should close. */
    data object Unavailable : NewBookingUiState
}

/** What the form is for: the same fields, titled and confirmed differently. */
internal enum class FormPurpose { NEW, EDIT }

/** Which date the next calendar tap sets. */
internal enum class DateField { CHECK_IN, CHECK_OUT }

internal sealed interface NewBookingAction {
    data class SetMode(val mode: BookingMode) : NewBookingAction
    data class EditDate(val field: DateField) : NewBookingAction
    data class PickDate(val date: LocalDate) : NewBookingAction
    data class SetNights(val nights: Int) : NewBookingAction
    data class ShowMonth(val month: YearMonth) : NewBookingAction
    data object ToggleMonthPicker : NewBookingAction
    data class SelectHouseType(val type: HouseType) : NewBookingAction
    data class ToggleHouse(val houseId: String) : NewBookingAction
    data class SetGuestName(val name: String) : NewBookingAction
    data class SetPhone(val phone: String) : NewBookingAction
    data object AddGuest : NewBookingAction
    data object RemoveGuest : NewBookingAction
    data class SetStatus(val status: BookingStatus) : NewBookingAction
    /** Null days keeps the hold until staff confirm or release it. */
    data class SetHoldDays(val days: Int?) : NewBookingAction
    data class SetNote(val note: String) : NewBookingAction
    data object Save : NewBookingAction
}

@Immutable
internal data class NewBookingFormUi(
    val purpose: FormPurpose,
    val draft: BookingDraft,
    val editingDate: DateField,
    val calendar: CalendarUi,
    val houseTypes: List<HouseTypeTileUi>,
    val houses: List<HouseTileUi>,
    val selectedHouseNames: List<String>,
    val houseCountsByType: List<HouseTypeCount>,
    val totalHouses: Int,
    val capacity: Int,
    val holdUntil: LocalDate?,
    val conflicts: List<ConflictUi>,
    val problems: List<DraftProblem>,
    val showProblems: Boolean,
    val isSaving: Boolean,
) {
    val canSave: Boolean get() = conflicts.isEmpty() && !isSaving

    val isOverCapacity: Boolean get() = capacity > 0 && draft.guestCount > capacity
}

@Immutable
internal data class CalendarUi(
    val month: YearMonth,
    val days: List<CalendarDayUi>,
    val firstMonth: YearMonth,
    val lastMonth: YearMonth,
    val isMonthPickerOpen: Boolean,
) {
    val canGoBack: Boolean get() = month > firstMonth
    val canGoForward: Boolean get() = month < lastMonth
}

@Immutable
internal data class CalendarDayUi(
    val date: LocalDate,
    val isPast: Boolean,
    val isToday: Boolean,
    /** A night that can't be booked for what's selected so far. */
    val isBlocked: Boolean,
    val isEndpoint: Boolean,
    val band: RangeBand,
)

/** The tinted band joining check-in to check-out behind the day numbers. */
internal enum class RangeBand { NONE, FULL, START_HALF, END_HALF }

@Immutable
internal data class HouseTypeTileUi(
    val type: HouseType,
    val sleeps: Int,
    /** Free houses of this type for the chosen dates; null until dates are picked. */
    val freeCount: Int?,
    val selectedCount: Int,
    val isActive: Boolean,
)

@Immutable
internal data class HouseTileUi(
    val houseId: String,
    val name: String,
    val shortLabel: String,
    val state: HouseTileState,
    val takenBy: TakenBy?,
    /** When the house first takes guests, if that's after the chosen check-in. */
    val opensOn: LocalDate? = null,
) {
    val isSelected: Boolean get() = state == HouseTileState.SELECTED || state == HouseTileState.SELECTED_BUT_TAKEN
}

internal enum class HouseTileState { NEEDS_DATES, FREE, SELECTED, TAKEN, SELECTED_BUT_TAKEN }

@Immutable
internal data class TakenBy(
    val guestName: String,
    val isWholeResort: Boolean,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
)

@Immutable
internal data class HouseTypeCount(val type: HouseType, val count: Int)

@Immutable
internal data class ConflictUi(
    val guestName: String,
    val isWholeResort: Boolean,
    val houseNames: List<String>,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
)
