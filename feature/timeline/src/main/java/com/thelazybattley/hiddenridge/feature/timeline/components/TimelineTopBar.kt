package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.feature.timeline.R
import java.time.YearMonth

@Composable
internal fun TimelineTopBar(
    houseCount: Int,
    visibleMonth: YearMonth,
    onMonthTitleClick: () -> Unit,
    onCheckAvailabilityClick: () -> Unit,
    onNewBookingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentLocale()
    val monthTitle = remember(visibleMonth, locale) { TimelineFormat.monthYear(locale).format(visibleMonth) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The title takes whatever width the buttons leave, but only its own width is tappable.
        Box(Modifier.weight(1f)) {
            MonthTitle(houseCount = houseCount, monthTitle = monthTitle, onClick = onMonthTitleClick)
        }
        FilledTonalIconButton(onClick = onCheckAvailabilityClick, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Outlined.EventAvailable,
                contentDescription = stringResource(R.string.timeline_check_availability),
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(4.dp))
        Button(
            onClick = onNewBookingClick,
            contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
            modifier = Modifier.height(40.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.timeline_new_booking))
        }
    }
}

@Composable
private fun MonthTitle(houseCount: Int, monthTitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                onClickLabel = stringResource(R.string.timeline_change_month),
                role = Role.Button,
                onClick = onClick,
            )
            .heightIn(min = 56.dp)
            .padding(start = 10.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f, fill = false)) {
            Text(
                text = pluralStringResource(R.plurals.timeline_property_label, houseCount, houseCount),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
    }
}
