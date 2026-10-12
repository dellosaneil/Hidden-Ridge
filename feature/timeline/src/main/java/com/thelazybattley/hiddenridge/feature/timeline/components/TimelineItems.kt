package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.component.dashedBorder
import com.thelazybattley.hiddenridge.core.designsystem.component.rememberResortHatchBrush
import com.thelazybattley.hiddenridge.core.designsystem.component.rememberResortHoldHatchBrush
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.timeline.BookingBar
import com.thelazybattley.hiddenridge.feature.timeline.R
import com.thelazybattley.hiddenridge.feature.timeline.ResortBlock

private val ShowIconsMinWidth = 40.dp
private val ShowNightsMinWidth = 120.dp
private val ShowResortStatusMinWidth = 60.dp
private val ShowResortPrefixMinWidth = 90.dp

/** A booking in one house: filled when confirmed, dashed when on hold. */
@Composable
internal fun BookingBarItem(
    bar: BookingBar,
    width: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(
        if (bar.isOnHold) R.string.timeline_booking_on_hold_description else R.string.timeline_booking_description,
        bar.guestName,
        bar.houseName,
        DateFormats.stayRange(bar.checkIn, bar.checkOut, currentLocale()),
    )
    val surface = if (bar.isOnHold) {
        Modifier
            .background(colors.surfaceContainerHighest)
            .dashedBorder(width = 2.dp, color = colors.outline, cornerRadius = TimelineDimens.BarCornerRadius)
    } else {
        Modifier.background(colors.primaryContainer)
    }
    val showIcons = width > ShowIconsMinWidth

    Row(
        modifier = modifier
            .size(width = width, height = TimelineDimens.BarHeight)
            .clip(RoundedCornerShape(TimelineDimens.BarCornerRadius))
            .then(surface)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (bar.isOnHold) colors.onSurface else colors.onPrimaryContainer,
        ) {
            if (bar.isOnHold && showIcons) {
                Icon(Icons.Outlined.HourglassEmpty, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            if (bar.isGroup && showIcons) {
                Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            Text(
                text = bar.guestName,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (width > ShowNightsMinWidth) {
                Text(
                    text = stringResource(R.string.timeline_nights_short, bar.nights),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.alpha(0.8f),
                )
            }
        }
    }
}

/** A whole-resort booking: a hatched block across every house, solid when confirmed, dashed on hold. */
@Composable
internal fun ResortBlockItem(
    block: ResortBlock,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(TimelineDimens.BarCornerRadius)
    val description = stringResource(
        if (block.isOnHold) R.string.timeline_resort_hold_description else R.string.timeline_resort_description,
        block.guestName,
        DateFormats.stayRange(block.checkIn, block.checkOut, currentLocale()),
    )
    val surface = if (block.isOnHold) {
        Modifier
            .background(colors.surface)
            .background(rememberResortHoldHatchBrush())
            .dashedBorder(width = 2.dp, color = colors.tertiary, cornerRadius = TimelineDimens.BarCornerRadius)
    } else {
        Modifier
            .background(rememberResortHatchBrush())
            .border(width = 2.dp, color = colors.tertiary, shape = shape)
    }

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(shape)
            .then(surface)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            },
    ) {
        ResortTitle(
            block = block,
            showPrefix = width > ShowResortPrefixMinWidth,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .fillMaxWidth(),
        )
        if (width > ShowResortStatusMinWidth) {
            ResortStatusPill(
                block = block,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun ResortTitle(block: ResortBlock, showPrefix: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(6.dp)
    val surface = if (block.isOnHold) {
        Modifier
            .background(colors.surface)
            .dashedBorder(width = 1.dp, color = colors.tertiary, cornerRadius = 6.dp, dashLength = 3.dp, gapLength = 2.dp)
    } else {
        Modifier.background(colors.tertiary)
    }
    val title = when {
        !showPrefix -> block.guestName
        block.isOnHold -> stringResource(R.string.timeline_resort_hold_label, block.guestName)
        else -> stringResource(R.string.timeline_whole_resort_label, block.guestName)
    }
    Row(
        modifier = modifier
            .clip(shape)
            .then(surface)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides if (block.isOnHold) colors.tertiary else colors.onTertiary) {
            Icon(
                imageVector = if (block.isOnHold) Icons.Outlined.HourglassEmpty else Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ResortStatusPill(block: ResortBlock, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val holdUntil = block.holdUntil
    val label = when {
        !block.isOnHold -> stringResource(R.string.timeline_status_confirmed)
        holdUntil != null ->
            stringResource(R.string.timeline_status_hold_until, TimelineFormat.monthDay(currentLocale()).format(holdUntil))
        else -> stringResource(R.string.timeline_status_hold)
    }
    val surface = if (block.isOnHold) {
        Modifier
            .background(colors.surface)
            .dashedBorder(width = 1.dp, color = colors.tertiary, cornerRadius = 12.dp, dashLength = 3.dp, gapLength = 2.dp)
    } else {
        Modifier.background(colors.tertiary)
    }
    Text(
        text = label,
        color = if (block.isOnHold) colors.tertiary else colors.onTertiary,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .widthIn(max = 200.dp)
            .clip(CircleShape)
            .then(surface)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
