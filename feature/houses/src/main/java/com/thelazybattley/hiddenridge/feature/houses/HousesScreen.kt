package com.thelazybattley.hiddenridge.feature.houses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.thelazybattley.hiddenridge.feature.houses.components.GroupHeader
import com.thelazybattley.hiddenridge.feature.houses.components.HouseFilters
import com.thelazybattley.hiddenridge.feature.houses.components.HouseRow
import com.thelazybattley.hiddenridge.feature.houses.components.HousesTopBar
import com.thelazybattley.hiddenridge.feature.houses.components.StripLegend
import com.thelazybattley.hiddenridge.feature.houses.components.TonightCard
import com.thelazybattley.hiddenridge.feature.houses.components.WeekHeader
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
internal fun HousesScreen(
    onBookingClick: (bookingId: String) -> Unit,
    onFreeHouseClick: (houseId: String, checkIn: LocalDate) -> Unit,
    onSearchClick: () -> Unit,
    onAddHouseClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HousesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HousesScreenContent(
        uiState = uiState,
        onFilterSelect = viewModel::selectFilter,
        onBookingClick = onBookingClick,
        onFreeHouseClick = onFreeHouseClick,
        onSearchClick = onSearchClick,
        onAddHouseClick = onAddHouseClick,
        modifier = modifier,
    )
}

@Composable
internal fun HousesScreenContent(
    uiState: HousesUiState,
    onFilterSelect: (HousesFilter) -> Unit,
    onBookingClick: (bookingId: String) -> Unit,
    onFreeHouseClick: (houseId: String, checkIn: LocalDate) -> Unit,
    onSearchClick: () -> Unit,
    onAddHouseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        when (uiState) {
            HousesUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is HousesUiState.Ready -> HousesReady(
                houses = uiState.houses,
                onFilterSelect = onFilterSelect,
                onBookingClick = onBookingClick,
                onFreeHouseClick = onFreeHouseClick,
                onSearchClick = onSearchClick,
                onAddHouseClick = onAddHouseClick,
            )
        }
    }
}

@Composable
private fun HousesReady(
    houses: HousesUi,
    onFilterSelect: (HousesFilter) -> Unit,
    onBookingClick: (bookingId: String) -> Unit,
    onFreeHouseClick: (houseId: String, checkIn: LocalDate) -> Unit,
    onSearchClick: () -> Unit,
    onAddHouseClick: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        HousesTopBar(today = houses.today, onSearchClick = onSearchClick, onAddHouseClick = onAddHouseClick)
        TonightCard(houses = houses, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
        HouseFilters(selected = houses.filter, counts = houses.filterCounts, onSelect = onFilterSelect)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            item(key = "week") { WeekHeader(week = houses.week, today = houses.today) }
            items(
                items = houses.rows,
                key = { row ->
                    when (row) {
                        is HousesRow.Group -> "group:${row.type}"
                        is HousesRow.House -> row.house.houseId
                    }
                },
                contentType = { row -> row::class },
            ) { row ->
                when (row) {
                    is HousesRow.Group -> GroupHeader(row.type)
                    is HousesRow.House -> HouseRow(
                        house = row.house,
                        todayIndex = houses.todayIndex,
                        showDivider = !row.isLastInGroup,
                        onBookingClick = onBookingClick,
                        onFreeHouseClick = { onFreeHouseClick(row.house.houseId, houses.today) },
                    )
                }
            }
            if (houses.rows.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.houses_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                    )
                }
            }
            item(key = "legend") { StripLegend(week = houses.week) }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Composable
private fun HousesScreenPreview() {
    HiddenRidgeTheme {
        HousesScreenContent(
            uiState = HousesUiState.Ready(HousesPreviewData.roster.toUi(HousesFilter.ALL, DayOfWeek.SUNDAY)),
            onFilterSelect = {},
            onBookingClick = {},
            onFreeHouseClick = { _, _ -> },
            onSearchClick = {},
            onAddHouseClick = {},
        )
    }
}
