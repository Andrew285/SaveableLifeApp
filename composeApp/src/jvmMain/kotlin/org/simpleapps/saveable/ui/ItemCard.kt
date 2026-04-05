package org.simpleapps.saveable.ui

import AccentDim
import AccentGreen
import Surface1
import Surface2
import TextPrimary
import TextSecondary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.util.TimeFormatter

@Composable
fun ItemCard(item: SaveableItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface1)
            .border(1.dp, Surface2, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(AccentDim)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text       = item.categoryName.uppercase(),
                color      = AccentGreen,
                fontSize   = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.width(14.dp))

        // Content
        Text(
            text       = item.content,
            color      = TextPrimary,
            fontSize   = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier   = Modifier.weight(1f)
        )

        // Timestamp
        Text(
            text     = TimeFormatter.formatTime(item.createdAt),
            color    = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}