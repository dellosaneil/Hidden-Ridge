package com.thelazybattley.hiddenridge.feature.addhouse.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.thelazybattley.hiddenridge.feature.addhouse.AddHouseSheet
import kotlinx.serialization.Serializable

@Serializable
data object AddHouseRoute

fun NavController.navigateToAddHouse() {
    navigate(AddHouseRoute) { launchSingleTop = true }
}

/** Closes the form; safe to call when it is already gone. */
fun NavController.closeAddHouse() {
    popBackStack<AddHouseRoute>(inclusive = true)
}

/** The add-house form, shown as a full-height bottom sheet. */
fun NavGraphBuilder.addHouseSheet(
    onDismiss: () -> Unit,
    onShowMessage: (message: String) -> Unit,
) {
    dialog<AddHouseRoute>(
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        AddHouseSheet(onDismiss = onDismiss, onShowMessage = onShowMessage)
    }
}
