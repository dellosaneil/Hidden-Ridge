package com.thelazybattley.hiddenridge.feature.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thelazybattley.hiddenridge.core.designsystem.theme.HiddenRidgeTheme
import com.thelazybattley.hiddenridge.feature.bookings.components.BookingRow
import com.thelazybattley.hiddenridge.feature.bookings.components.BookingsTopBar
import com.thelazybattley.hiddenridge.feature.bookings.components.GroupHeader
import com.thelazybattley.hiddenridge.feature.bookings.components.PeriodTabs
import com.thelazybattley.hiddenridge.feature.bookings.components.StatusFilters
import java.time.DayOfWeek

@Composable
internal fun BookingsScreen(
    onBookingClick: (bookingId: String) -> Unit,
    onNewBookingClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BookingsScreenContent(
        uiState = uiState,
        onTabSelect = viewModel::selectTab,
        onFilterSelect = viewModel::selectFilter,
        onBookingClick = onBookingClick,
        onNewBookingClick = onNewBookingClick,
        onSearchClick = onSearchClick,
        modifier = modifier,
    )
}

@Composable
internal fun BookingsScreenContent(
    uiState: BookingsUiState,
    onTabSelect: (BookingsTab) -> Unit,
    onFilterSelect: (BookingsFilter) -> Unit,
    onBookingClick: (bookingId: String) -> Unit,
    onNewBookingClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        when (uiState) {
            BookingsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is BookingsUiState.Ready -> BookingsReady(
                bookings = uiState.bookings,
                onTabSelect = onTabSelect,
                onFilterSelect = onFilterSelect,
                onBookingClick = onBookingClick,
                onNewBookingClick = onNewBookingClick,
                onSearchClick = onSearchClick,
            )
        }
    }
}

@Composable
private fun BookingsReady(
    bookings: BookingsUi,
    onTabSelect: (BookingsTab) -> Unit,
    onFilterSelect: (BookingsFilter) -> Unit,
    onBookingClick: (bookingId: String) -> Unit,
    onNewBookingClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        BookingsTopBar(today = bookings.today, onSearchClick = onSearchClick, onNewBookingClick = onNewBookingClick)
        PeriodTabs(selected = bookings.tab, counts = bookings.tabCounts, onSelect = onTabSelect)
        StatusFilters(selected = bookings.filter, counts = bookings.filterCounts, onSelect = onFilterSelect)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(
                items = bookings.rows,
                key = { row ->
                    when (row) {
                        is BookingsRow.Group -> "group:${row.label}"
                        is BookingsRow.Booking -> row.booking.bookingId
                    }
                },
                contentType = { row -> row::class },
            ) { row ->
                when (row) {
                    is BookingsRow.Group -> GroupHeader(label = row.label, count = row.count)
                    is BookingsRow.Booking -> BookingRow(
                        booking = row.booking,
                        today = bookings.today,
                        showDivider = !row.isLastInGroup,
                        onClick = { onBookingClick(row.booking.bookingId) },
                    )
                }
            }
            if (bookings.rows.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.bookings_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 40.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Composable
private fun BookingsScreenPreview() {
    HiddenRidgeTheme {
        BookingsScreenContent(
            uiState = BookingsUiState.Ready(BookingsPreviewData.log.toUi(BookingsTab.UPCOMING, BookingsFilter.ALL, DayOfWeek.SUNDAY)),
            onTabSelect = {},
            onFilterSelect = {},
            onBookingClick = {},
            onNewBookingClick = {},
            onSearchClick = {},
        )
    }
}
