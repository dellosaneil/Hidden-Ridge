package com.thelazybattley.hiddenridge.feature.bookingdetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.HolidayVillage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.bookingdetails.BookingDetailsUi
import com.thelazybattley.hiddenridge.feature.bookingdetails.HousesUi
import com.thelazybattley.hiddenridge.feature.bookingdetails.R

private class Fact(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val sub: String? = null,
    val onClickLabel: String? = null,
    val onClick: (() -> Unit)? = null,
)

/** Dates, houses, guests and, when known, phone and note. */
@Composable
internal fun BookingFacts(details: BookingDetailsUi, modifier: Modifier = Modifier) {
    val facts = listOfNotNull(
        datesFact(details),
        housesFact(details.houses),
        Fact(
            icon = Icons.Outlined.Group,
            label = stringResource(R.string.fact_guests),
            value = pluralStringResource(R.plurals.fact_guests_value, details.guestCount, details.guestCount),
        ),
        phoneFact(details.phone),
        details.note.takeIf(String::isNotBlank)?.let {
            Fact(icon = Icons.AutoMirrored.Outlined.StickyNote2, label = stringResource(R.string.fact_note), value = it)
        },
    )
    Column(modifier = modifier) {
        facts.forEachIndexed { index, fact ->
            FactRow(fact)
            if (index < facts.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun datesFact(details: BookingDetailsUi): Fact {
    val locale = currentLocale()
    return Fact(
        icon = Icons.Outlined.CalendarMonth,
        label = stringResource(R.string.fact_dates),
        value = stringResource(
            R.string.fact_dates_value,
            DateFormats.weekdayDayMonth(locale).format(details.checkIn),
            DateFormats.weekdayDayMonthYear(locale).format(details.checkOut),
        ),
        sub = pluralStringResource(R.plurals.fact_nights, details.nights, details.nights),
    )
}

@Composable
private fun housesFact(houses: HousesUi): Fact = when (houses) {
    is HousesUi.Single -> Fact(
        icon = Icons.Outlined.Cottage,
        label = stringResource(R.string.fact_house),
        value = stringResource(R.string.fact_house_value, houses.name, houses.sleeps),
    )
    is HousesUi.Several -> Fact(
        icon = Icons.Outlined.Cottage,
        label = stringResource(R.string.fact_houses),
        value = houses.names.joinToString(),
        sub = stringResource(R.string.fact_sleeps_total, houses.totalSleeps),
    )
    is HousesUi.WholeResort -> Fact(
        icon = Icons.Outlined.HolidayVillage,
        label = stringResource(R.string.fact_houses),
        value = pluralStringResource(R.plurals.fact_whole_resort_value, houses.houseCount, houses.houseCount),
        sub = stringResource(R.string.fact_sleeps_total, houses.totalSleeps),
    )
}

@Composable
private fun phoneFact(phone: String): Fact? {
    if (phone.isBlank()) return null
    val uriHandler = LocalUriHandler.current
    return Fact(
        icon = Icons.Outlined.Call,
        label = stringResource(R.string.fact_phone),
        value = phone,
        onClickLabel = stringResource(R.string.fact_phone_call),
        onClick = { uriHandler.openUri("tel:" + phone.filter { it.isDigit() || it == '+' }) },
    )
}

@Composable
private fun FactRow(fact: Fact) {
    val colors = MaterialTheme.colorScheme
    val onClick = fact.onClick
    val interaction = if (onClick != null) {
        Modifier.clickable(onClickLabel = fact.onClickLabel, onClick = onClick)
    } else {
        Modifier.semantics(mergeDescendants = true) {}
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(interaction)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(fact.icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        Column {
            Text(fact.label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Text(
                text = fact.value,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = if (onClick != null) colors.primary else colors.onSurface,
            )
            fact.sub?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
        }
    }
}
