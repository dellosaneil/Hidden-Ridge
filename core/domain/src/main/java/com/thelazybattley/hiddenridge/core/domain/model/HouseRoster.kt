package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate

/** Every house as front-desk staff see it on [today]: who leaves, who sleeps there tonight, who's next. */
data class HouseRoster(
    val today: LocalDate,
    val houses: List<HouseDay>,
)

data class HouseDay(
    val house: House,
    /** Active stays in this house from last night on, by check-in; a whole-resort stay is in every house. */
    val stays: List<Booking>,
    val today: LocalDate,
) {
    /** The stay that checks out of this house this morning. */
    val departing: Booking? get() = stays.firstOrNull { it.checkOut == today }

    /** The stay covering tonight, including one that arrives today. */
    val tonight: Booking? get() = stays.firstOrNull { it.coversNight(today) }

    /** The first stay arriving after today. */
    val next: Booking? get() = stays.firstOrNull { it.checkIn.isAfter(today) }

    /** The stay covering the night of [night], if any. */
    fun stayOn(night: LocalDate): Booking? = stays.firstOrNull { it.coversNight(night) }
}

/** Each house in [houses] with the active [bookings] that touch it. */
fun houseRoster(today: LocalDate, houses: List<House>, bookings: List<Booking>): HouseRoster =
    HouseRoster(
        today = today,
        houses = houses.map { house ->
            HouseDay(
                house = house,
                stays = bookings.filter { it.isWholeResort || house.id in it.houseIds }.sortedBy(Booking::checkIn),
                today = today,
            )
        },
    )
