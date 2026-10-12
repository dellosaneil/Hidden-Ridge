package com.thelazybattley.hiddenridge.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

/**
 * A modal bottom sheet shown as a navigation `dialog` destination. The destination's own dialog
 * window would dim the screen on top of the sheet's scrim, so its dim is switched off here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    content: @Composable ColumnScope.() -> Unit,
) {
    val destinationWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    SideEffect { destinationWindow?.setDimAmount(0f) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        content = content,
    )
}
