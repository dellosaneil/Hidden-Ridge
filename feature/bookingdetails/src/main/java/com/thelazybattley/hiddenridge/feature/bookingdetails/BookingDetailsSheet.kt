package com.thelazybattley.hiddenridge.feature.bookingdetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thelazybattley.hiddenridge.core.designsystem.component.DestinationBottomSheet
import com.thelazybattley.hiddenridge.core.designsystem.theme.HiddenRidgeTheme
import com.thelazybattley.hiddenridge.core.domain.model.StayPhase
import com.thelazybattley.hiddenridge.feature.bookingdetails.components.BookingActions
import com.thelazybattley.hiddenridge.feature.bookingdetails.components.BookingFacts
import com.thelazybattley.hiddenridge.feature.bookingdetails.components.BookingHeader
import com.thelazybattley.hiddenridge.feature.bookingdetails.components.CancelConfirmation
import com.thelazybattley.hiddenridge.feature.bookingdetails.components.DeleteBookingDialog
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookingDetailsSheet(
    onDismiss: () -> Unit,
    onEditDetails: (bookingId: String) -> Unit,
    onShowMessage: (message: String) -> Unit,
    viewModel: BookingDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val resources = LocalContext.current.resources

    /** Lets the sheet slide away before leaving, so it doesn't vanish mid-frame. */
    fun hideThen(next: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { next() }
    }

    val closed = uiState as? BookingDetailsUiState.Closed
    if (closed != null) {
        LaunchedEffect(closed) {
            val outcome = closed.outcome
            val guestName = closed.details?.guestName
            if (outcome != null && guestName != null) onShowMessage(resources.getString(outcome.message, guestName))
            hideThen(onDismiss)
        }
    }

    DestinationBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        BookingDetailsContent(
            uiState = uiState,
            onAction = { action ->
                when (action) {
                    BookingDetailsAction.EditDetails -> hideThen { onEditDetails(viewModel.bookingId) }
                    else -> viewModel.onAction(action)
                }
            },
        )
    }
}

@Composable
internal fun BookingDetailsContent(
    uiState: BookingDetailsUiState,
    onAction: (BookingDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        BookingDetailsUiState.Loading -> Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }

        is BookingDetailsUiState.Ready -> BookingDetailsBody(
            details = uiState.details,
            confirmation = uiState.confirmation,
            onAction = onAction,
            modifier = modifier,
        )

        // Keep the last content showing, without actions, while the sheet animates away.
        is BookingDetailsUiState.Closed -> uiState.details?.let {
            BookingDetailsBody(details = it, confirmation = null, onAction = {}, modifier = modifier)
        }
    }
}

@Composable
private fun BookingDetailsBody(
    details: BookingDetailsUi,
    confirmation: PendingConfirmation?,
    onAction: (BookingDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        BookingHeader(details = details, modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp))
        BookingFacts(details = details)
        if (confirmation == PendingConfirmation.CANCEL) {
            CancelConfirmation(
                details = details,
                onKeep = { onAction(BookingDetailsAction.DismissConfirmation) },
                onCancel = { onAction(BookingDetailsAction.ConfirmCancel) },
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
            )
        } else {
            BookingActions(
                isOnHold = details.isOnHold,
                onAction = onAction,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
            )
        }
    }
    if (confirmation == PendingConfirmation.DELETE) {
        DeleteBookingDialog(
            details = details,
            onKeep = { onAction(BookingDetailsAction.DismissConfirmation) },
            onDelete = { onAction(BookingDetailsAction.ConfirmDelete) },
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun BookingDetailsPreview() {
    HiddenRidgeTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            BookingDetailsContent(
                uiState = BookingDetailsUiState.Ready(
                    details = BookingDetailsUi(
                        guestName = "Maria Santos",
                        kind = BookingKind.INDIVIDUAL,
                        status = BookingStatusUi.Confirmed,
                        stayPhase = StayPhase.InHouse(night = 3, nights = 4),
                        checkIn = LocalDate.of(2026, 10, 10),
                        checkOut = LocalDate.of(2026, 10, 14),
                        nights = 4,
                        houses = HousesUi.Single(name = "Cabin 1", sleeps = 2),
                        guestCount = 2,
                        phone = "(555) 318-2207",
                        note = "Anniversary. Asked for a late check-out.",
                    ),
                    confirmation = null,
                ),
                onAction = {},
            )
        }
    }
}
