package com.thelazybattley.hiddenridge.feature.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.component.dashedBorder
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.overview.DecisionHeadline
import com.thelazybattley.hiddenridge.feature.overview.DecisionUi
import com.thelazybattley.hiddenridge.feature.overview.HousesText
import com.thelazybattley.hiddenridge.feature.overview.OverviewAction
import com.thelazybattley.hiddenridge.feature.overview.R
import java.time.LocalDate

/** Holds waiting on staff: each says what makes it pressing, with Release and Confirm. */
@Composable
internal fun DecisionsCard(
    decisions: List<DecisionUi>,
    today: LocalDate,
    onAction: (OverviewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = CardShape, color = colors.surfaceContainer, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
            Row(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .semantics(mergeDescendants = true) { heading() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = colors.tertiary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.overview_decisions_title), fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                if (decisions.isNotEmpty()) {
                    Surface(shape = RoundedCornerShape(11.dp), color = colors.tertiaryContainer, contentColor = colors.onTertiaryContainer) {
                        Box(
                            modifier = Modifier
                                .height(22.dp)
                                .widthIn(min = 22.dp)
                                .padding(horizontal = 7.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(decisions.size.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            if (decisions.isEmpty()) {
                Text(
                    text = stringResource(R.string.overview_decisions_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
            }
            decisions.forEachIndexed { index, decision ->
                if (index > 0) HorizontalDivider(color = colors.outlineVariant)
                DecisionItem(decision = decision, today = today, onAction = onAction)
            }
        }
    }
}

@Composable
private fun DecisionItem(decision: DecisionUi, today: LocalDate, onAction: (OverviewAction) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val locale = currentLocale()
    val booking = decision.booking
    val guest = booking.guestName
    val title = when (val headline = decision.headline) {
        is DecisionHeadline.Releases -> stringResource(R.string.overview_decision_releases, guest, OverviewFormat.weekdayDayMonth(locale).format(headline.date))
        is DecisionHeadline.ArrivesIn -> when (headline.days) {
            0 -> stringResource(R.string.overview_decision_arrives_today, guest)
            1 -> stringResource(R.string.overview_decision_arrives_tomorrow, guest)
            else -> stringResource(R.string.overview_decision_arrives_in, guest, headline.days)
        }
        DecisionHeadline.InHouse -> stringResource(R.string.overview_decision_in_house, guest)
    }
    val houses = when (val h = decision.houses) {
        is HousesText.Names -> h.names.joinToString()
        is HousesText.WholeResort -> stringResource(R.string.overview_whole_resort_houses, h.houseCount)
    }
    val detail = stringResource(
        R.string.overview_decision_detail,
        houses,
        OverviewFormat.stayRange(booking.checkIn, booking.checkOut, today, locale),
        pluralStringResource(R.plurals.overview_nights, booking.nights, booking.nights),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(detail, fontSize = 13.sp, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            HoldTag(until = booking.holdUntil)
        }
        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val releaseDescription = stringResource(R.string.overview_release_description, guest)
            val confirmDescription = stringResource(R.string.overview_confirm_description, guest)
            TextButton(
                onClick = { onAction(OverviewAction.Release(decision)) },
                modifier = Modifier
                    .height(40.dp)
                    .semantics { contentDescription = releaseDescription },
            ) { Text(stringResource(R.string.overview_release)) }
            Button(
                onClick = { onAction(OverviewAction.Confirm(decision)) },
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .height(40.dp)
                    .semantics { contentDescription = confirmDescription },
            ) { Text(stringResource(R.string.overview_confirm)) }
        }
    }
}

/** A dashed tag, like a hold on the timeline: open-ended, or until its release date. */
@Composable
private fun HoldTag(until: LocalDate?) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = colors.surfaceContainerHighest,
        modifier = Modifier.dashedBorder(width = 1.5.dp, color = colors.outline, cornerRadius = 6.dp, dashLength = 4.dp, gapLength = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .height(22.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = if (until == null) Icons.Filled.AllInclusive else Icons.Outlined.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = until?.let { stringResource(R.string.overview_decision_until, OverviewFormat.weekdayDayMonth(currentLocale()).format(it)) }
                    ?: stringResource(R.string.overview_decision_no_limit),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
