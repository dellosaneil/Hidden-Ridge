package com.thelazybattley.hiddenridge.feature.newbooking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingDraft
import com.thelazybattley.hiddenridge.core.domain.usecase.CreateBookingUseCase
import com.thelazybattley.hiddenridge.core.domain.usecase.ObserveAvailabilityUseCase
import com.thelazybattley.hiddenridge.feature.newbooking.navigation.NewBookingRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NewBookingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeAvailability: ObserveAvailabilityUseCase,
    private val createBooking: CreateBookingUseCase,
) : ViewModel() {

    private val form = MutableStateFlow(
        savedStateHandle.toRoute<NewBookingRoute>().let { route ->
            NewBookingForm.startingWith(
                houseIds = route.houseIds,
                wholeResort = route.wholeResort,
                checkIn = route.checkInEpochDay?.let(LocalDate::ofEpochDay),
                checkOut = route.checkOutEpochDay?.let(LocalDate::ofEpochDay),
            )
        },
    )
    private val created = MutableStateFlow<Booking?>(null)

    internal val uiState: StateFlow<NewBookingUiState> = combine(observeAvailability(), form, created) { availability, form, booking ->
        if (booking != null) {
            NewBookingUiState.Created(booking.guestName, booking.checkIn, booking.checkOut, booking.isOnHold)
        } else {
            NewBookingUiState.Editing(form.toUi(availability))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = NewBookingUiState.Loading,
    )

    internal fun onAction(action: NewBookingAction) {
        when (action) {
            is NewBookingAction.SetMode -> updateDraft { copy(mode = action.mode) }
            is NewBookingAction.EditDate -> form.update { it.editing(action.field) }
            is NewBookingAction.PickDate -> form.update { it.picking(action.date) }
            is NewBookingAction.SetNights -> form.update { it.withNights(action.nights) }
            is NewBookingAction.ShowMonth -> form.update { it.copy(visibleMonth = action.month, isMonthPickerOpen = false) }
            NewBookingAction.ToggleMonthPicker -> form.update { it.copy(isMonthPickerOpen = !it.isMonthPickerOpen) }
            is NewBookingAction.SelectHouseType -> form.update { it.copy(houseTypeTab = action.type) }
            is NewBookingAction.ToggleHouse -> updateDraft { copy(houseIds = houseIds.toggled(action.houseId)) }
            is NewBookingAction.SetGuestName -> updateDraft { copy(guestName = action.name) }
            is NewBookingAction.SetPhone -> updateDraft { copy(phone = action.phone) }
            NewBookingAction.AddGuest -> updateDraft { copy(guestCount = guestCount + 1) }
            NewBookingAction.RemoveGuest -> updateDraft { copy(guestCount = (guestCount - 1).coerceAtLeast(1)) }
            is NewBookingAction.SetStatus -> updateDraft { copy(status = action.status) }
            is NewBookingAction.SetHoldDays -> updateDraft { copy(holdDays = action.days) }
            is NewBookingAction.SetNote -> updateDraft { copy(note = action.note) }
            NewBookingAction.Save -> save()
        }
    }

    private fun updateDraft(change: BookingDraft.() -> BookingDraft) {
        form.update { it.copy(draft = it.draft.change()) }
    }

    private fun save() {
        val current = form.value
        if (current.isSaving) return
        form.update { it.copy(isSaving = true, showProblems = true) }
        viewModelScope.launch {
            when (val result = createBooking(current.draft)) {
                is CreateBookingUseCase.Result.Created -> created.value = result.booking
                // Problems and conflicts are already shown on the form once showProblems is on.
                is CreateBookingUseCase.Result.Invalid,
                is CreateBookingUseCase.Result.Conflicting,
                -> form.update { it.copy(isSaving = false) }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun NewBookingForm.editing(field: DateField): NewBookingForm {
    val date = if (field == DateField.CHECK_OUT) draft.checkOut ?: draft.checkIn else draft.checkIn
    return copy(editingDate = field, visibleMonth = date?.let(YearMonth::from) ?: visibleMonth, isMonthPickerOpen = false)
}

/**
 * A tap starts a new stay unless it lands after check-in while check-out is being picked,
 * so two taps choose a range and a third starts over.
 */
private fun NewBookingForm.picking(date: LocalDate): NewBookingForm {
    val checkIn = draft.checkIn
    return if (editingDate == DateField.CHECK_IN || checkIn == null || !date.isAfter(checkIn)) {
        copy(draft = draft.copy(checkIn = date, checkOut = null), editingDate = DateField.CHECK_OUT)
    } else {
        copy(draft = draft.copy(checkOut = date), editingDate = DateField.CHECK_IN)
    }
}

private fun NewBookingForm.withNights(nights: Int): NewBookingForm {
    val checkIn = draft.checkIn ?: return this
    return copy(draft = draft.copy(checkOut = checkIn.plusDays(nights.toLong())), editingDate = DateField.CHECK_IN)
}

private fun List<String>.toggled(id: String): List<String> = if (id in this) this - id else this + id
