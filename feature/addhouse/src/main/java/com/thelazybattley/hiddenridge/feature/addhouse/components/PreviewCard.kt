package com.thelazybattley.hiddenridge.feature.addhouse.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelazybattley.hiddenridge.core.designsystem.component.imageVector
import com.thelazybattley.hiddenridge.core.designsystem.format.currentLocale
import com.thelazybattley.hiddenridge.feature.addhouse.AddHouseFormUi
import com.thelazybattley.hiddenridge.feature.addhouse.R
import java.time.format.DateTimeFormatter

/** The house as its row in the Houses tab will look once it's added. */
@Composable
internal fun PreviewCard(form: AddHouseFormUi, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val from = DateTimeFormatter.ofPattern("EEE d MMM", currentLocale()).format(form.bookableFrom)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.addhouse_preview_title),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(form.icon.imageVector, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
                Column(Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = form.name.ifBlank { stringResource(R.string.addhouse_name_placeholder) },
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Transparent,
                            contentColor = colors.primary,
                            border = BorderStroke(1.dp, colors.primary),
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(if (form.isNewType) R.string.addhouse_preview_badge_new_type else R.string.addhouse_preview_badge_new),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(
                            R.string.addhouse_preview_detail,
                            form.groupName.ifBlank { stringResource(R.string.addhouse_type_new) },
                            form.sleeps,
                            from,
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
