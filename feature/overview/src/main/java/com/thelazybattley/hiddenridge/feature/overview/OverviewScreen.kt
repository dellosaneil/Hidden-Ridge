package com.thelazybattley.hiddenridge.feature.overview

import android.content.res.Resources
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.designsystem.theme.HiddenRidgeTheme
import com.thelazybattley.hiddenridge.feature.overview.components.DecisionsCard
import com.thelazybattley.hiddenridge.feature.overview.components.MonthsCard
import com.thelazybattley.hiddenridge.feature.overview.components.MovementsCard
import com.thelazybattley.hiddenridge.feature.overview.components.OverviewFormat
import com.thelazybattley.hiddenridge.feature.overview.components.TodayTiles
import com.thelazybattley.hiddenridge.feature.overview.components.WeekChart

@Composable
internal fun OverviewScreen(
    onOpenHouses: () -> Unit,
    onOpenBookings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    LaunchedEffect(viewModel) {
        viewModel.done.collect { action ->
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = action.confirmation(resources),
                actionLabel = resources.getString(R.string.overview_undo),
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undo(action)
        }
    }
    OverviewScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::perform,
        onOpenHouses = onOpenHouses,
        onOpenBookings = onOpenBookings,
        modifier = modifier,
    )
}

@Composable
internal fun OverviewScreenContent(
    uiState: OverviewUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (OverviewAction) -> Unit,
    onOpenHouses: () -> Unit,
    onOpenBookings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        when (uiState) {
            OverviewUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            is OverviewUiState.Ready -> OverviewReady(
                overview = uiState.overview,
                onAction = onAction,
                onOpenHouses = onOpenHouses,
                onOpenBookings = onOpenBookings,
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { data -> ConfirmationSnackbar(data) }
    }
}

@Composable
private fun OverviewReady(
    overview: OverviewUi,
    onAction: (OverviewAction) -> Unit,
    onOpenHouses: () -> Unit,
    onOpenBookings: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        OverviewTopBar(overview)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TodayTiles(overview = overview, onOpenHouses = onOpenHouses, onOpenBookings = onOpenBookings)
            MovementsCard(moves = overview.pendingMoves, onAction = onAction, onOpenHouses = onOpenHouses)
            WeekChart(nights = overview.nights, houseCount = overview.houseCount, today = overview.today)
            DecisionsCard(decisions = overview.decisions, today = overview.today, onAction = onAction)
            MonthsCard(months = overview.months, wholeResort = overview.wholeResort, today = overview.today)
        }
    }
}

@Composable
private fun OverviewTopBar(overview: OverviewUi) {
    val locale = currentLocale()
    val date = remember(overview.today, locale) { OverviewFormat.fullDate(locale).format(overview.today) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.overview_property_label, date),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.overview_title),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
            modifier = Modifier.semantics { heading() },
        )
    }
}

/** A check mark, what changed, and Undo, as on the Houses tab. */
@Composable
private fun ConfirmationSnackbar(data: SnackbarData) {
    Snackbar(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        action = {
            data.visuals.actionLabel?.let { label ->
                TextButton(
                    onClick = data::performAction,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.inversePrimary),
                ) { Text(label) }
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.inversePrimary,
                modifier = Modifier.size(20.dp),
            )
            Text(data.visuals.message)
        }
    }
}

private fun OverviewAction.confirmation(resources: Resources): String = when (this) {
    is OverviewAction.CheckIn -> resources.getString(R.string.overview_checked_in, move.guestName)
    is OverviewAction.CheckOut -> resources.getString(R.string.overview_checked_out, move.guestName)
    is OverviewAction.Confirm -> resources.getString(R.string.overview_confirmed, decision.booking.guestName)
    is OverviewAction.Release -> resources.getString(R.string.overview_released, decision.booking.guestName)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1400)
@Composable
private fun OverviewScreenPreview() {
    HiddenRidgeTheme {
        OverviewScreenContent(
            uiState = OverviewUiState.Ready(OverviewPreviewData.log.toOverview()),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onOpenHouses = {},
            onOpenBookings = {},
        )
    }
}
