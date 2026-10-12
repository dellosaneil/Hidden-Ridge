package com.thelazybattley.hiddenridge.feature.bookingdetails.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.HolidayVillage
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.core.domain.model.StayPhase
import com.thelazybattley.hiddenridge.feature.bookingdetails.BookingDetailsUi
import com.thelazybattley.hiddenridge.feature.bookingdetails.BookingKind
import com.thelazybattley.hiddenridge.feature.bookingdetails.BookingStatusUi
import com.thelazybattley.hiddenridge.feature.bookingdetails.R
import kotlin.math.roundToInt

private const val DAYS_BEFORE_SHOWING_MONTHS = 60
private const val DAYS_PER_MONTH = 30.0

@Composable
internal fun BookingHeader(details: BookingDetailsUi, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            KindChip(details.kind)
            StatusChip(details.status)
        }
        Text(
            text = details.guestName,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stayPhaseText(details.stayPhase),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun KindChip(kind: BookingKind) {
    val colors = MaterialTheme.colorScheme
    when (kind) {
        BookingKind.WHOLE_RESORT -> InfoChip(
            icon = Icons.Outlined.HolidayVillage,
            label = stringResource(R.string.booking_kind_whole_resort),
            container = colors.tertiaryContainer,
            content = colors.onTertiaryContainer,
        )
        BookingKind.GROUP -> InfoChip(
            icon = Icons.Outlined.Cottage,
            label = stringResource(R.string.booking_kind_group),
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer,
        )
        BookingKind.INDIVIDUAL -> InfoChip(
            icon = Icons.Outlined.Cottage,
            label = stringResource(R.string.booking_kind_individual),
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer,
        )
    }
}

@Composable
private fun StatusChip(status: BookingStatusUi) {
    val colors = MaterialTheme.colorScheme
    when (status) {
        BookingStatusUi.Confirmed -> InfoChip(
            icon = Icons.Outlined.CheckCircle,
            label = stringResource(R.string.booking_status_confirmed),
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer,
        )
        is BookingStatusUi.OnHold -> InfoChip(
            icon = Icons.Outlined.HourglassEmpty,
            label = status.until?.let {
                stringResource(R.string.booking_status_hold_until, DateFormats.weekdayDayMonth(currentLocale()).format(it))
            } ?: stringResource(R.string.booking_status_hold),
            container = Color.Transparent,
            content = colors.onSurfaceVariant,
            border = BorderStroke(1.dp, colors.outline),
        )
    }
}

/** A label, not a control, so it is a plain surface rather than a Material chip. */
@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    container: Color,
    content: Color,
    border: BorderStroke? = null,
) {
    Surface(shape = RoundedCornerShape(8.dp), color = container, contentColor = content, border = border) {
        Row(
            modifier = Modifier
                .height(28.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun stayPhaseText(phase: StayPhase): String = when (phase) {
    StayPhase.Departed -> stringResource(R.string.stay_departed)
    StayPhase.LeavesToday -> stringResource(R.string.stay_leaves_today)
    StayPhase.ArrivesToday -> stringResource(R.string.stay_arrives_today)
    is StayPhase.InHouse -> stringResource(R.string.stay_in_house, phase.night, phase.nights)
    is StayPhase.ArrivesIn -> when {
        phase.days == 1 -> stringResource(R.string.stay_arrives_tomorrow)
        phase.days > DAYS_BEFORE_SHOWING_MONTHS -> {
            val months = (phase.days / DAYS_PER_MONTH).roundToInt()
            pluralStringResource(R.plurals.stay_arrives_in_months, months, months)
        }
        else -> pluralStringResource(R.plurals.stay_arrives_in_days, phase.days, phase.days)
    }
}
