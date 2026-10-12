package com.thelazybattley.hiddenridge.core.domain.time

import java.time.LocalDate

/** The resort's current date, injectable so tests can pin "today". */
fun interface DateProvider {
    fun today(): LocalDate
}
