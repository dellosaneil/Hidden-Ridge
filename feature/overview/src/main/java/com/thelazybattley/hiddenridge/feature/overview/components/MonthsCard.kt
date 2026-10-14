package com.thelazybattley.hiddenridge.feature.overview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.overview.MonthUi
import com.thelazybattley.hiddenridge.feature.overview.R
import com.thelazybattley.hiddenridge.feature.overview.WholeResortNote
import java.time.LocalDate
import kotlin.math.roundToInt

private const val PERCENT = 100

/** How full this month and the next two are, and the next whole-resort block among them. */
@Composable
internal fun MonthsCard(months: List<MonthUi>, wholeResort: WholeResortNote?, today: LocalDate, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    OutlinedCard(modifier) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(stringResource(R.string.overview_months_title))
            months.forEach { month ->
                val label = OverviewFormat.shortMonthYear(locale).format(month.month)
                val percent = (month.bookedFraction * PERCENT).roundToInt()
                val description = stringResource(R.string.overview_month_description, label, percent)
                Row(
                    modifier = Modifier.clearAndSetSemantics { contentDescription = description },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(label, fontSize = 13.sp, modifier = Modifier.width(64.dp), maxLines = 1)
                    Box(
                        Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surfaceContainerHigh),
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(month.bookedFraction)
                                .background(colors.primary, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)),
                        )
                    }
                    Text(
                        text = stringResource(R.string.overview_percent, percent),
                        fontSize = 13.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(40.dp),
                    )
                }
            }
            wholeResort?.let { note ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = colors.tertiary, modifier = Modifier.size(16.dp))
                    Text(
                        text = stringResource(
                            if (note.isHold) R.string.overview_resort_held else R.string.overview_resort_booked,
                            OverviewFormat.stayRange(note.checkIn, note.checkOut, today, locale),
                            note.guestName,
                        ),
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
