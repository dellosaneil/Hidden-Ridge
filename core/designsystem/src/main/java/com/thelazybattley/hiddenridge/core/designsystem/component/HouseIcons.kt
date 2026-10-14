package com.thelazybattley.hiddenridge.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cabin
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.Festival
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.HolidayVillage
import androidx.compose.material.icons.outlined.Villa
import androidx.compose.ui.graphics.vector.ImageVector
import com.thelazybattley.hiddenridge.core.domain.model.HouseIcon

/** The picture for a house type. The design's tent has no Material icon, so a festival tent stands in. */
val HouseIcon.imageVector: ImageVector
    get() = when (this) {
        HouseIcon.CABIN -> Icons.Outlined.Cabin
        HouseIcon.COTTAGE -> Icons.Outlined.Cottage
        HouseIcon.VILLAGE -> Icons.Outlined.HolidayVillage
        HouseIcon.TREE -> Icons.Outlined.Forest
        HouseIcon.TENT -> Icons.Outlined.Festival
        HouseIcon.VILLA -> Icons.Outlined.Villa
    }
