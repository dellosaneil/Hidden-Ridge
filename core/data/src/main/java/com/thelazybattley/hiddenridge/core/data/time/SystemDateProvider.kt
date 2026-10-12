package com.thelazybattley.hiddenridge.core.data.time

import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

internal class SystemDateProvider @Inject constructor(
    private val clock: Clock,
) : DateProvider {
    override fun today(): LocalDate = LocalDate.now(clock)
}
