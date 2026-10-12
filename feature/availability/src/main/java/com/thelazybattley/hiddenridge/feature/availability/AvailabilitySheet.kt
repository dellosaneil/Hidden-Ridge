package com.thelazybattley.hiddenridge.feature.availability

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thelazybattley.hiddenridge.core.designsystem.component.LabeledBox
import com.thelazybattley.hiddenridge.core.designsystem.component.OptionChip
import com.thelazybattley.hiddenridge.core.designsystem.component.TallDestinationBottomSheet
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.HouseFilter
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.feature.availability.components.AvailabilityCalendar
import com.thelazybattley.hiddenridge.feature.availability.components.AvailabilityResults
import java.time.LocalDate
import kotlinx.coroutines.launch

private val LengthsOfStay = listOf(1, 2, 3, 5, 7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AvailabilitySheet(
    onDismiss: () -> Unit,
    onBook: (houseIds: List<String>, wholeResort: Boolean, checkIn: LocalDate, checkOut: LocalDate) -> Unit,
    onViewBooking: (bookingId: String) -> Unit,
    viewModel: AvailabilityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    /** Lets the sheet slide away before leaving, so it doesn't vanish mid-frame. */
    fun hideThen(next: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { next() }
    }

    TallDestinationBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        when (val state = uiState) {
            AvailabilityUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is AvailabilityUiState.Ready -> AvailabilityContent(
                availability = state.availability,
                onClose = { hideThen(onDismiss) },
                onAction = { action ->
                    when (action) {
                        is AvailabilityAction.Book -> {
                            val result = state.availability.result ?: return@AvailabilityContent
                            hideThen { onBook(action.houseIds, action.wholeResort, result.checkIn, result.checkOut) }
                        }
                        is AvailabilityAction.ViewBooking -> onViewBooking(action.bookingId)
                        else -> viewModel.onAction(action)
                    }
                },
            )
        }
    }
}

@Composable
internal fun AvailabilityContent(
    availability: AvailabilityUi,
    onClose: () -> Unit,
    onAction: (AvailabilityAction) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
            }
            Text(
                text = stringResource(R.string.availability_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            if (availability.checkIn != null) {
                TextButton(onClick = { onAction(AvailabilityAction.Clear) }) { Text(stringResource(R.string.action_clear)) }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FilterRow(selected = availability.filter, filters = availability.filters, onSelect = { onAction(AvailabilityAction.SetFilter(it)) })
            StayDates(availability)
            if (availability.checkIn != null) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LengthsOfStay.forEach { nights ->
                        OptionChip(
                            label = pluralStringResource(R.plurals.nights, nights, nights),
                            selected = availability.nights == nights,
                            onClick = { onAction(AvailabilityAction.SetNights(nights)) },
                        )
                    }
                }
            }
            AvailabilityCalendar(
                calendar = availability.calendar,
                filter = availability.filter,
                onPick = { onAction(AvailabilityAction.PickDate(it)) },
                onShowMonth = { onAction(AvailabilityAction.ShowMonth(it)) },
            )
            val result = availability.result
            if (result == null) {
                Prompt(hasCheckIn = availability.checkIn != null)
            } else {
                AvailabilityResults(result = result, onAction = onAction)
            }
        }
    }
}

@Composable
private fun FilterRow(selected: HouseFilter, filters: List<HouseFilter>, onSelect: (HouseFilter) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { filter ->
            OptionChip(label = filterLabel(filter), selected = filter == selected, onClick = { onSelect(filter) })
        }
    }
}

@Composable
private fun filterLabel(filter: HouseFilter): String = when (filter) {
    HouseFilter.AnyHouse -> stringResource(R.string.filter_any_house)
    HouseFilter.WholeResort -> stringResource(R.string.filter_whole_resort)
    is HouseFilter.OfType -> stringResource(
        when (filter.type) {
            HouseType.CABIN -> R.string.filter_cabins
            HouseType.CASITA -> R.string.filter_casitas
            HouseType.FAMILY_HOUSE -> R.string.filter_family
        },
    )
}

/** Read-only: dates are picked on the calendar. The field the next tap fills is highlighted. */
@Composable
private fun StayDates(availability: AvailabilityUi) {
    val format = DateFormats.weekdayDayMonthYear(currentLocale())
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            Triple(R.string.field_check_in, availability.checkIn, availability.checkIn == null),
            Triple(R.string.field_check_out, availability.checkOut, availability.checkIn != null && availability.checkOut == null),
        ).forEach { (label, date, isNext) ->
            LabeledBox(label = stringResource(label), isActive = isNext, modifier = Modifier.weight(1f)) {
                Text(
                    text = date?.let(format::format) ?: stringResource(R.string.field_tap_a_date),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (date == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun Prompt(hasCheckIn: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
        Icon(Icons.Outlined.TouchApp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(
            text = stringResource(if (hasCheckIn) R.string.prompt_pick_check_out else R.string.prompt_pick_check_in),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
