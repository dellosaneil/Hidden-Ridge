package com.thelazybattley.hiddenridge.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/**
 * A modal bottom sheet shown as a navigation `dialog` destination. The destination's own dialog
 * window would dim the screen on top of the sheet's scrim, so its dim is switched off here.
 *
 * @param contentWindowInsets the insets padded around [content]. Pass none when the content sizes
 *   itself to the window and applies the insets it needs, so the padding isn't added on top.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    content: @Composable ColumnScope.() -> Unit,
) {
    val destinationWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    SideEffect { destinationWindow?.setDimAmount(0f) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

private const val TALL_SHEET_HEIGHT_FRACTION = 0.94f

/** Material 3's default sheet drag handle: a 4dp bar with 22dp above and below. */
private val SheetDragHandleHeight = 48.dp

/**
 * A [DestinationBottomSheet] covering most of the screen, for forms with a pinned footer.
 * The screen behind peeks above it as a reminder of where closing returns to.
 *
 * The height comes from the window, not the sheet's incoming constraints: those change while the
 * sheet is dragged, and a height that follows them moves the sheet's anchors and makes it shake.
 * [content] stays above the navigation bar, and above the keyboard while typing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallDestinationBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    content: @Composable ColumnScope.() -> Unit,
) {
    val windowHeight = LocalWindowInfo.current.containerSize.height
    val contentHeight = with(LocalDensity.current) { (windowHeight * TALL_SHEET_HEIGHT_FRACTION).toDp() } - SheetDragHandleHeight
    DestinationBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        Column(
            modifier = Modifier
                .height(contentHeight)
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
            content = content,
        )
    }
}
