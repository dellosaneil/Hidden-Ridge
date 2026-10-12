package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate

/** Everything the availability timeline shows: every house and the active bookings inside [window]. */
data class Timeline(
    val today: LocalDate,
    val window: DateWindow,
    val houses: List<House>,
    val bookings: List<Booking>,
)
