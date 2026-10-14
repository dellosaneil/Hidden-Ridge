package com.thelazybattley.hiddenridge.feature.overview.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val CardShape = RoundedCornerShape(16.dp)

/** An outlined card, the design's frame for each Overview section. */
@Composable
internal fun OutlinedCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = CardShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(content = content)
    }
}

@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.semantics { heading() },
    )
}

internal object OverviewFormat {
    /** "Mon 12 Oct 2026" */
    fun fullDate(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM yyyy", locale)

    /** "Thu 15 Oct" */
    fun weekdayDayMonth(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", locale)

    /** "Oct 2026" */
    fun shortMonthYear(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy", locale)

    fun shortWeekday(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", locale)

    /** "18–21 Dec", "30 Oct – 2 Nov", or with years when the stay isn't in [today]'s year. */
    fun stayRange(checkIn: LocalDate, checkOut: LocalDate, today: LocalDate, locale: Locale): String {
        if (checkIn.year != today.year || checkOut.year != today.year) return DateFormats.stayRange(checkIn, checkOut, locale)
        val dayMonth = DateTimeFormatter.ofPattern("d MMM", locale)
        return if (checkIn.month == checkOut.month) {
            "${checkIn.dayOfMonth}–${dayMonth.format(checkOut)}"
        } else {
            "${dayMonth.format(checkIn)} – ${dayMonth.format(checkOut)}"
        }
    }
}
