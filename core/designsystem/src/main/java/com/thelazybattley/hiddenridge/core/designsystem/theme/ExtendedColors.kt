package com.thelazybattley.hiddenridge.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colors the design uses that Material's color scheme has no slot for. */
@Immutable
data class ExtendedColors(
    /** The light band of the whole-resort hatch, drawn over `tertiaryContainer`. */
    val tertiaryStripe: Color,
    /** The thin stripe of a whole-resort hold's hatch. */
    val holdStripe: Color,
    /** Shading behind Saturday and Sunday columns. */
    val weekend: Color,
    /** Shading over days that have already passed. */
    val past: Color,
)

internal val LightExtendedColors = ExtendedColors(
    tertiaryStripe = Color(0xFFFFF0E2),
    holdStripe = Color(0x248B5000),
    weekend = Color(0x09171D1A),
    past = Color(0x0D171D1A),
)

internal val DarkExtendedColors = ExtendedColors(
    tertiaryStripe = Color(0xFF52300A),
    holdStripe = Color(0x24FFB870),
    weekend = Color(0x08FFFFFF),
    past = Color(0x38000000),
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
