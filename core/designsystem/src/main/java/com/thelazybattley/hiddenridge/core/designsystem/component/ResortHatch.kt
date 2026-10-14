package com.thelazybattley.hiddenridge.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.theme.HiddenRidgeTheme
import kotlin.math.sqrt

/** Diagonal stripes marking a confirmed whole-resort booking. */
@Composable
fun rememberResortHatchBrush(): Brush {
    val stripe = MaterialTheme.colorScheme.tertiaryContainer
    val gap = HiddenRidgeTheme.extendedColors.tertiaryStripe
    val density = LocalDensity.current
    return remember(stripe, gap, density) {
        with(density) { diagonalStripes(stripe, gap, stripeWidth = 7.dp.toPx(), period = 14.dp.toPx()) }
    }
}

/** Thin diagonal stripes marking a whole-resort hold; draw over `surface`. */
@Composable
fun rememberResortHoldHatchBrush(): Brush {
    val stripe = HiddenRidgeTheme.extendedColors.holdStripe
    val density = LocalDensity.current
    return remember(stripe, density) {
        with(density) { diagonalStripes(stripe, Color.Transparent, stripeWidth = 3.dp.toPx(), period = 12.dp.toPx()) }
    }
}

/** Thin [stripe] lines over transparent, for anything on hold in a chart; draw over the surface. */
@Composable
fun rememberHoldHatchBrush(stripe: Color): Brush {
    val density = LocalDensity.current
    return remember(stripe, density) {
        with(density) { diagonalStripes(stripe, Color.Transparent, stripeWidth = 2.dp.toPx(), period = 6.dp.toPx()) }
    }
}

/** Repeating 45° stripes: [stripeWidth] of [stripe] then [gap] for the rest of each [period], in px. */
private fun diagonalStripes(stripe: Color, gap: Color, stripeWidth: Float, period: Float): Brush {
    val step = period / sqrt(2f)
    val split = stripeWidth / period
    return Brush.linearGradient(
        0f to stripe,
        split to stripe,
        split to gap,
        1f to gap,
        start = Offset.Zero,
        end = Offset(step, step),
        tileMode = TileMode.Repeated,
    )
}
