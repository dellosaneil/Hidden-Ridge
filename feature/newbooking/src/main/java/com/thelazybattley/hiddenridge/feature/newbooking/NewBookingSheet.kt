package com.thelazybattley.hiddenridge.feature.newbooking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.HolidayVillage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thelazybattley.hiddenridge.core.designsystem.component.DestinationBottomSheet
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.feature.newbooking.components.DatesSection
import com.thelazybattley.hiddenridge.feature.newbooking.components.FormFooter
import com.thelazybattley.hiddenridge.feature.newbooking.components.FormSection
import com.thelazybattley.hiddenridge.feature.newbooking.components.GuestSection
import com.thelazybattley.hiddenridge.feature.newbooking.components.HousesSection
import com.thelazybattley.hiddenridge.feature.newbooking.components.SegmentOption
import com.thelazybattley.hiddenridge.feature.newbooking.components.SegmentedChoice
import kotlinx.coroutines.launch

private const val SHEET_HEIGHT_FRACTION = 0.94f

/** Material 3's default sheet drag handle: a 4dp bar with 22dp above and below. */
private val SheetDragHandleHeight = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewBookingSheet(
    onDismiss: () -> Unit,
    onShowMessage: (message: String) -> Unit,
    viewModel: NewBookingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val resources = LocalContext.current.resources
    val locale = currentLocale()
    val hideThenDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    val created = uiState as? NewBookingUiState.Created
    if (created != null) {
        LaunchedEffect(created) {
            val range = DateFormats.stayRange(created.checkIn, created.checkOut, locale)
            val message = if (created.isHold) R.string.message_held else R.string.message_booked
            onShowMessage(resources.getString(message, created.guestName, range))
            hideThenDismiss()
        }
    }

    // Most of the screen, leaving the timeline peeking above as a reminder of where this returns to.
    // Sized from the window, not the sheet's incoming constraints: those change while the sheet is
    // dragged, and a height that follows them moves the sheet's anchors and makes it shake.
    // The sheet adds no inset padding of its own, so this height is the whole sheet below the handle.
    val windowHeight = LocalWindowInfo.current.containerSize.height
    val formHeight = with(LocalDensity.current) { (windowHeight * SHEET_HEIGHT_FRACTION).toDp() } - SheetDragHandleHeight

    DestinationBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        Column(
            modifier = Modifier
                .height(formHeight)
                // Keeps the footer above the navigation bar, and above the keyboard while typing.
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        ) {
            when (val state = uiState) {
                NewBookingUiState.Loading, is NewBookingUiState.Created -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is NewBookingUiState.Editing -> NewBookingContent(
                    form = state.form,
                    onAction = viewModel::onAction,
                    onClose = hideThenDismiss,
                )
            }
        }
    }
}

@Composable
internal fun NewBookingContent(
    form: NewBookingFormUi,
    onAction: (NewBookingAction) -> Unit,
    onClose: () -> Unit,
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
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_discard))
            }
            Text(
                text = stringResource(R.string.new_booking_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            TextButton(onClick = { onAction(NewBookingAction.Save) }, enabled = form.canSave) {
                Text(stringResource(R.string.action_save))
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            BookingTypeSection(mode = form.draft.mode, onAction = onAction)
            DatesSection(form = form, onAction = onAction)
            HousesSection(form = form, onAction = onAction)
            GuestSection(form = form, onAction = onAction)
        }
        FormFooter(form = form, onSave = { onAction(NewBookingAction.Save) })
    }
}

@Composable
private fun BookingTypeSection(mode: BookingMode, onAction: (NewBookingAction) -> Unit) {
    FormSection(title = stringResource(R.string.section_booking_type)) {
        SegmentedChoice(
            options = listOf(
                SegmentOption(BookingMode.INDIVIDUAL, stringResource(R.string.mode_individual), Icons.Outlined.Cottage),
                SegmentOption(BookingMode.WHOLE_RESORT, stringResource(R.string.mode_whole_resort), Icons.Outlined.HolidayVillage),
            ),
            selected = mode,
            onSelect = { onAction(NewBookingAction.SetMode(it)) },
        )
        Text(
            text = stringResource(
                if (mode == BookingMode.WHOLE_RESORT) R.string.mode_whole_resort_hint else R.string.mode_individual_hint,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
