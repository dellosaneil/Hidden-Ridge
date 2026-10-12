package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.timeline.R
import com.thelazybattley.hiddenridge.feature.timeline.TimelineMonth
import java.time.LocalDate
import java.time.Month
import java.util.Locale

/** "Today" plus a chip per month; the chip for the month in view stays selected and scrolled into sight. */
@Composable
internal fun MonthRail(
    months: List<TimelineMonth>,
    activeMonthIndex: Int,
    today: LocalDate,
    onTodayClick: () -> Unit,
    onMonthClick: (monthIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = activeMonthIndex)
    val goToToday = stringResource(R.string.timeline_go_to_today, TimelineFormat.fullDate(locale).format(today))
    val labels = remember(months, locale) {
        months.mapIndexed { index, month -> monthChipLabel(month, showYear = index == 0, locale = locale) }
    }

    LaunchedEffect(activeMonthIndex) {
        listState.animateScrollToItem((activeMonthIndex - 1).coerceAtLeast(0))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = onTodayClick,
            label = { Text(stringResource(R.string.timeline_today)) },
            leadingIcon = { Icon(Icons.Outlined.Today, contentDescription = null, modifier = Modifier.size(18.dp)) },
            colors = AssistChipDefaults.assistChipColors(
                labelColor = colors.primary,
                leadingIconContentColor = colors.primary,
            ),
            border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = colors.primary),
            modifier = Modifier.semantics { contentDescription = goToToday },
        )
        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(24.dp),
            color = colors.outlineVariant,
        )
        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(end = 12.dp),
        ) {
            itemsIndexed(months, key = { _, month -> month.yearMonth.toString() }) { index, _ ->
                FilterChip(
                    selected = index == activeMonthIndex,
                    onClick = { onMonthClick(index) },
                    label = { Text(labels[index]) },
                )
            }
        }
    }
}

/** "Oct", with the year added on the first chip and on each January: "Oct ’26", "Jan ’27". */
private fun monthChipLabel(month: TimelineMonth, showYear: Boolean, locale: Locale): String {
    val name = TimelineFormat.shortMonth(locale).format(month.yearMonth)
    return if (showYear || month.yearMonth.month == Month.JANUARY) {
        "$name ’${TimelineFormat.shortYear(locale).format(month.yearMonth)}"
    } else {
        name
    }
}
