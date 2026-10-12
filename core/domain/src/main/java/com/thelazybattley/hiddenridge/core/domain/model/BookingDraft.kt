package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** A booking being filled in. Every field may still be missing or wrong; see [problems]. */
data class BookingDraft(
    val mode: BookingMode = BookingMode.INDIVIDUAL,
    val houseIds: List<String> = emptyList(),
    val checkIn: LocalDate? = null,
    val checkOut: LocalDate? = null,
    val guestName: String = "",
    val phone: String = "",
    val guestCount: Int = DEFAULT_GUEST_COUNT,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val holdDays: Int = HoldRelease.DEFAULT_DAYS,
    val note: String = "",
) {
    val hasDates: Boolean get() = checkIn != null && checkOut != null && checkOut.isAfter(checkIn)

    val nights: Int get() = if (hasDates) ChronoUnit.DAYS.between(checkIn, checkOut).toInt() else 0

    val isWholeResort: Boolean get() = mode == BookingMode.WHOLE_RESORT

    val isHold: Boolean get() = status == BookingStatus.HOLD

    /** What this draft would occupy, once it has dates. */
    fun stayRequest(): StayRequest? =
        if (hasDates) StayRequest(mode, if (isWholeResort) emptyList() else houseIds, checkIn!!, checkOut!!) else null

    fun holdUntil(today: LocalDate): LocalDate? =
        if (isHold) checkIn?.let { HoldRelease.date(today, it, holdDays) } else null

    /** What still stops this draft from being saved, in the order the form asks for it. */
    fun problems(today: LocalDate): List<DraftProblem> = buildList {
        when {
            checkIn == null -> add(DraftProblem.MISSING_CHECK_IN)
            checkIn.isBefore(today) -> add(DraftProblem.CHECK_IN_IN_PAST)
            !hasDates -> add(DraftProblem.MISSING_CHECK_OUT)
        }
        if (!isWholeResort && houseIds.isEmpty()) add(DraftProblem.NO_HOUSES)
        if (guestName.isBlank()) add(DraftProblem.MISSING_GUEST_NAME)
        if (guestCount < 1) add(DraftProblem.NO_GUESTS)
    }

    companion object {
        const val DEFAULT_GUEST_COUNT = 2
    }
}

enum class DraftProblem {
    MISSING_CHECK_IN,
    CHECK_IN_IN_PAST,
    MISSING_CHECK_OUT,
    NO_HOUSES,
    MISSING_GUEST_NAME,
    NO_GUESTS,
}
