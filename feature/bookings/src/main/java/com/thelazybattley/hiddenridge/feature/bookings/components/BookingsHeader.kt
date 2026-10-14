package com.thelazybattley.hiddenridge.feature.bookings.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.bookings.BookingsFilter
import com.thelazybattley.hiddenridge.feature.bookings.BookingsTab
import com.thelazybattley.hiddenridge.feature.bookings.R
import java.time.LocalDate

private const val COUNT_ALPHA = 0.8f

/** Trimmed so the count sits in the middle of its 18dp badge. */
private val BadgeTextStyle = TextStyle(
    fontSize = 11.sp,
    lineHeight = 11.sp,
    lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.Both),
)

@Composable
internal fun BookingsTopBar(
    today: LocalDate,
    onSearchClick: () -> Unit,
    onNewBookingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentLocale()
    val date = remember(today, locale) { BookingsFormat.weekdayDayMonth(locale).format(today) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.bookings_property_label, date),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.bookings_title),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                modifier = Modifier.semantics { heading() },
            )
        }
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.bookings_search),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(4.dp))
        Button(
            onClick = onNewBookingClick,
            contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
            modifier = Modifier.height(40.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.bookings_new))
        }
    }
}

/** Upcoming and Past, each with a count badge, over a short centered indicator. */
@Composable
internal fun PeriodTabs(
    selected: BookingsTab,
    counts: Map<BookingsTab, Int>,
    onSelect: (BookingsTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    PrimaryTabRow(
        selectedTabIndex = selected.ordinal,
        modifier = modifier,
        containerColor = colors.surface,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selected.ordinal, matchContentSize = false),
                width = 64.dp,
            )
        },
        divider = { HorizontalDivider(color = colors.outlineVariant) },
    ) {
        BookingsTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val label = stringResource(if (tab == BookingsTab.UPCOMING) R.string.bookings_tab_upcoming else R.string.bookings_tab_past)
            val count = counts[tab] ?: 0
            val description = stringResource(R.string.bookings_tab_description, label, count)
            Tab(
                selected = isSelected,
                onClick = { onSelect(tab) },
                selectedContentColor = colors.primary,
                unselectedContentColor = colors.onSurfaceVariant,
                modifier = Modifier
                    .height(48.dp)
                    .semantics { contentDescription = description },
            ) {
                Row(
                    modifier = Modifier.clearAndSetSemantics {},
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(label, style = MaterialTheme.typography.titleSmall)
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (isSelected) colors.primary else colors.surfaceContainerHigh,
                        contentColor = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
                    ) {
                        Box(
                            modifier = Modifier
                                .height(18.dp)
                                .widthIn(min = 20.dp)
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(count.toString(), style = BadgeTextStyle, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun StatusFilters(
    selected: BookingsFilter,
    counts: Map<BookingsFilter, Int>,
    onSelect: (BookingsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookingsFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(filter.label))
                        Text(
                            text = counts[filter]?.toString().orEmpty(),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .alpha(COUNT_ALPHA),
                        )
                    }
                },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}

private val BookingsFilter.label: Int
    get() = when (this) {
        BookingsFilter.ALL -> R.string.bookings_filter_all
        BookingsFilter.CONFIRMED -> R.string.bookings_filter_confirmed
        BookingsFilter.HOLD -> R.string.bookings_filter_hold
        BookingsFilter.WHOLE_RESORT -> R.string.bookings_filter_whole_resort
    }
