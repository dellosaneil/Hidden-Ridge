package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.feature.timeline.TimelineRow

internal object TimelineDimens {
    val DayWidth = 48.dp
    val HouseColumnWidth = 104.dp
    val MonthRowHeight = 24.dp
    val DayRowHeight = 36.dp
    val HeaderHeight = MonthRowHeight + DayRowHeight
    val GroupRowHeight = 32.dp
    val HouseRowHeight = 56.dp
    val BarHeight = 40.dp
    val BarCornerRadius = 8.dp
    val Hairline = 1.dp
}

internal val TimelineRow.height: Dp
    get() = when (this) {
        is TimelineRow.Group -> TimelineDimens.GroupRowHeight
        is TimelineRow.House -> TimelineDimens.HouseRowHeight
    }

/** Where each row starts vertically, so bars, grid lines and taps all agree on row positions. */
@Immutable
internal class TimelineRowLayout(val rows: List<TimelineRow>) {
    val tops: List<Dp>
    val totalHeight: Dp

    init {
        var y = 0.dp
        tops = rows.map { row -> y.also { y += row.height } }
        totalHeight = y
    }

    fun rowAt(y: Dp): TimelineRow? {
        if (y < 0.dp || y >= totalHeight) return null
        return rows[tops.indexOfLast { it <= y }]
    }
}

/** Day columns at least partly inside the viewport `[scrollPx, scrollPx + viewportPx)`. */
internal fun visibleDays(scrollPx: Float, viewportPx: Float, dayPx: Float, dayCount: Int): IntRange {
    if (dayCount <= 0) return IntRange.EMPTY
    val first = (scrollPx / dayPx).toInt().coerceIn(0, dayCount - 1)
    val last = ((scrollPx + viewportPx) / dayPx).toInt().coerceIn(first, dayCount - 1)
    return first..last
}
