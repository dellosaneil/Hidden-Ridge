package com.thelazybattley.hiddenridge.feature.newbooking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.DraftProblem
import com.thelazybattley.hiddenridge.feature.newbooking.ConflictUi
import com.thelazybattley.hiddenridge.feature.newbooking.FormPurpose
import com.thelazybattley.hiddenridge.feature.newbooking.NewBookingFormUi
import com.thelazybattley.hiddenridge.feature.newbooking.R

private const val CONFLICTS_LISTED = 3

/** Pinned under the form: what's wrong, or a one-line summary, and the create button. */
@Composable
internal fun FormFooter(
    form: NewBookingFormUi,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val problem = form.problems.firstOrNull()?.takeIf { form.showProblems }
            when {
                form.conflicts.isNotEmpty() -> ConflictCard(form.conflicts, isWholeResort = form.draft.isWholeResort)
                problem != null -> ProblemLine(problem)
                else -> Summary(form)
            }
            Button(
                onClick = onSave,
                enabled = form.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        stringResource(
                            when (form.purpose) {
                                FormPurpose.NEW -> if (form.draft.isHold) R.string.create_hold else R.string.create_booking
                                FormPurpose.EDIT -> R.string.save_changes
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Summary(form: NewBookingFormUi) {
    val draft = form.draft
    val checkIn = draft.checkIn
    val checkOut = draft.checkOut
    val text = if (draft.hasDates && checkIn != null && checkOut != null) {
        val houses = if (draft.isWholeResort) {
            pluralStringResource(R.plurals.footer_all_houses, form.totalHouses, form.totalHouses)
        } else {
            pluralStringResource(R.plurals.footer_houses, draft.houseIds.size, draft.houseIds.size)
        }
        stringResource(
            if (draft.isHold) R.string.footer_summary_on_hold else R.string.footer_summary,
            DateFormats.stayRange(checkIn, checkOut, currentLocale()),
            pluralStringResource(R.plurals.nights, draft.nights, draft.nights),
            houses,
        )
    } else {
        stringResource(R.string.footer_choose)
    }
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ProblemLine(problem: DraftProblem) {
    Row(
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        Text(stringResource(problem.message), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ConflictCard(conflicts: List<ConflictUi>, isWholeResort: Boolean) {
    val locale = currentLocale()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.EventBusy, contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = pluralStringResource(R.plurals.conflict_title, conflicts.size, conflicts.size),
                    style = MaterialTheme.typography.titleSmall,
                )
                conflicts.take(CONFLICTS_LISTED).forEach { conflict ->
                    Text(
                        text = stringResource(
                            R.string.conflict_line,
                            conflict.guestName,
                            if (conflict.isWholeResort) stringResource(R.string.conflict_whole_resort) else conflict.houseNames.joinToString(),
                            DateFormats.stayRange(conflict.checkIn, conflict.checkOut, locale),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (conflicts.size > CONFLICTS_LISTED) {
                    Text(stringResource(R.string.conflict_more, conflicts.size - CONFLICTS_LISTED), style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = stringResource(if (isWholeResort) R.string.conflict_hint_resort else R.string.conflict_hint_houses),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

private val DraftProblem.message: Int
    get() = when (this) {
        DraftProblem.MISSING_CHECK_IN -> R.string.problem_missing_check_in
        DraftProblem.CHECK_IN_IN_PAST -> R.string.problem_check_in_in_past
        DraftProblem.MISSING_CHECK_OUT -> R.string.problem_missing_check_out
        DraftProblem.NO_HOUSES -> R.string.problem_no_houses
        DraftProblem.HOUSE_NOT_OPEN -> R.string.problem_house_not_open
        DraftProblem.MISSING_GUEST_NAME -> R.string.problem_missing_guest_name
        DraftProblem.NO_GUESTS -> R.string.problem_no_guests
    }
