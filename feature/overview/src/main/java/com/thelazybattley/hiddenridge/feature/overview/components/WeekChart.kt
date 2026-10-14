package com.thelazybattley.hiddenridge.feature.overview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.component.dashedBorder
import com.thelazybattley.hiddenridge.core.designsystem.component.rememberHoldHatchBrush
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.overview.NightUi
import com.thelazybattley.hiddenridge.feature.overview.R
import java.time.LocalDate

private val PlotHeight = 120.dp
private val ColumnHeight = 128.dp
private val BarWidth = 24.dp
private val AxisWidth = 22.dp
private val ColumnGap = 4.dp

/** The surface gap between the booked and held segments of a bar. */
private val SegmentGap = 2.dp
private const val HATCH_ALPHA = 0.35f
private const val HIGHLIGHT_ALPHA = 0.05f

/**
 * Houses booked each of the next seven nights, with holds stacked on top as a hatched segment.
 * Tapping a night reads its numbers out above the bars.
 */
@Composable
internal fun WeekChart(nights: List<NightUi>, houseCount: Int, today: LocalDate, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val night = nights.getOrNull(selected) ?: nights.firstOrNull() ?: return
    val hatch = rememberHoldHatchBrush(colors.onSurfaceVariant.copy(alpha = HATCH_ALPHA))
    val unit = if (houseCount > 0) PlotHeight / houseCount else 0.dp

    OutlinedCard(modifier) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                SectionTitle(stringResource(R.string.overview_week_title), Modifier.weight(1f))
                Text(stringResource(R.string.overview_week_of, houseCount), fontSize = 12.sp, color = colors.onSurfaceVariant)
            }
            val name = nightName(night.date, today)
            val free = (houseCount - night.booked - night.held).coerceAtLeast(0)
            Text(
                text = if (night.held > 0) {
                    stringResource(R.string.overview_week_readout_held, name, night.booked, night.held, free)
                } else {
                    stringResource(R.string.overview_week_readout, name, night.booked, free)
                },
                fontSize = 13.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 10.dp)
                    .heightIn(min = 20.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
            Box(Modifier.height(ColumnHeight)) {
                Gridlines(houseCount)
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(ColumnGap)) {
                    Spacer(Modifier.width(AxisWidth))
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(ColumnGap),
                    ) {
                        nights.forEachIndexed { index, n ->
                            NightColumn(
                                night = n,
                                houseCount = houseCount,
                                name = nightName(n.date, today),
                                unit = unit,
                                hatch = hatch,
                                isSelected = index == selected,
                                onSelect = { selected = index },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clearAndSetSemantics {},
                horizontalArrangement = Arrangement.spacedBy(ColumnGap),
            ) {
                Spacer(Modifier.width(AxisWidth))
                nights.forEachIndexed { index, n ->
                    val isSelected = index == selected
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (n.date == today) stringResource(R.string.overview_week_today) else OverviewFormat.shortWeekday(locale).format(n.date),
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = n.date.dayOfMonth.toString(),
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LegendItem(stringResource(R.string.overview_legend_booked)) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(colors.primary, RoundedCornerShape(3.dp)),
                    )
                }
                LegendItem(stringResource(R.string.overview_legend_hold)) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(hatch, RoundedCornerShape(3.dp))
                            .dashedBorder(width = 1.5.dp, color = colors.outline, cornerRadius = 3.dp, dashLength = 3.dp, gapLength = 2.dp),
                    )
                }
            }
        }
    }
}

/** Lines at full, half and empty, with the house counts beside them. */
@Composable
private fun Gridlines(houseCount: Int) {
    val colors = MaterialTheme.colorScheme
    val half = houseCount / 2
    val top = ColumnHeight - PlotHeight
    val halfY = top + PlotHeight * (1f - half.toFloat() / houseCount.coerceAtLeast(1))
    Box(
        Modifier
            .fillMaxSize()
            .clearAndSetSemantics {},
    ) {
        listOf(houseCount to top, half to halfY, 0 to ColumnHeight - 1.dp).distinctBy { it.first }.forEach { (value, y) ->
            Text(
                text = value.toString(),
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(AxisWidth)
                    .offset(y = y - 6.dp),
            )
            HorizontalDivider(
                color = if (value == 0) colors.outline else colors.outlineVariant,
                modifier = Modifier
                    .padding(start = AxisWidth + ColumnGap)
                    .offset(y = y),
            )
        }
    }
}

@Composable
private fun NightColumn(
    night: NightUi,
    houseCount: Int,
    name: String,
    unit: Dp,
    hatch: Brush,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val description = if (night.held > 0) {
        stringResource(R.string.overview_week_night_description_held, name, night.booked, houseCount, night.held)
    } else {
        stringResource(R.string.overview_week_night_description, name, night.booked, houseCount)
    }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isSelected) colors.onSurface.copy(alpha = HIGHLIGHT_ALPHA) else Color.Transparent,
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            )
            .selectable(selected = isSelected, role = Role.Tab, onClick = onSelect)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (night.held > 0) {
            Box(
                Modifier
                    .size(width = BarWidth, height = unit * night.held - SegmentGap)
                    .background(hatch, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .dashedBorder(width = 1.5.dp, color = colors.outline, cornerRadius = 4.dp, dashLength = 3.dp, gapLength = 2.dp),
            )
            if (night.booked > 0) Spacer(Modifier.height(SegmentGap))
        }
        if (night.booked > 0) {
            Box(
                Modifier
                    .size(width = BarWidth, height = unit * night.booked)
                    .background(
                        colors.primary,
                        if (night.held > 0) RoundedCornerShape(0.dp) else RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                    ),
            )
        }
    }
}

@Composable
private fun LegendItem(label: String, swatch: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        swatch()
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun nightName(date: LocalDate, today: LocalDate): String =
    if (date == today) stringResource(R.string.overview_week_tonight) else OverviewFormat.weekdayDayMonth(currentLocale()).format(date)
