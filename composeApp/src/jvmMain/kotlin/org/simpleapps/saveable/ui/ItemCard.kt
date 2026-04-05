package org.simpleapps.saveable.ui

import AccentDim
import AccentGreen
import Surface1
import Surface2
import TextPrimary
import TextSecondary
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.util.TimeFormatter

private val DeleteRed   = Color(0xFFFF4D6A)
private val DeleteRedDim = Color(0x26FF4D6A)
private val EditBlue    = Color(0xFF4D9EFF)
private val EditBlueDim = Color(0x264D9EFF)

@Composable
fun ItemCard(
    item     : SaveableItem,
    onEdit   : (SaveableItem, String) -> Unit,
    onDelete : (SaveableItem) -> Unit
) {
    var isEditing    by remember { mutableStateOf(false) }
    var editText     by remember { mutableStateOf(item.content) }
    var showConfirm  by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface1)
            .border(1.dp, Surface2, RoundedCornerShape(8.dp))
    ) {
        // ── Main row ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    text          = item.categoryName.uppercase(),
                    color         = AccentGreen,
                    fontSize      = 9.sp,
                    fontFamily    = FontFamily.Monospace,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.width(14.dp))

            // Content or inline edit field
            if (isEditing) {
                BasicTextField(
                    value         = editText,
                    onValueChange = { editText = it },
                    singleLine    = true,
                    textStyle     = TextStyle(
                        color      = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 13.sp
                    ),
                    cursorBrush = SolidColor(AccentGreen),
                    modifier    = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2A2A30))
                        .border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .onPreviewKeyEvent { keyEvent ->
                            when {
                                keyEvent.type != KeyEventType.KeyDown -> false
                                keyEvent.key == Key.Enter -> {
                                    onEdit(item, editText)
                                    isEditing = false
                                    true
                                }
                                keyEvent.key == Key.Escape -> {
                                    editText  = item.content  // revert
                                    isEditing = false
                                    true
                                }
                                else -> false
                            }
                        }
                )
            } else {
                Text(
                    text       = item.content,
                    color      = TextPrimary,
                    fontSize   = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier   = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Timestamp
            Text(
                text       = TimeFormatter.formatTime(item.createdAt),
                color      = TextSecondary,
                fontSize   = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(Modifier.width(16.dp))

            // ── Action buttons ──────────────────────────────────────────
            if (isEditing) {
                // Save button
                ActionButton(
                    label    = "save",
                    color    = AccentGreen,
                    dimColor = AccentDim,
                    onClick  = {
                        onEdit(item, editText)
                        isEditing = false
                    }
                )
                Spacer(Modifier.width(6.dp))
                // Cancel button
                ActionButton(
                    label    = "cancel",
                    color    = TextSecondary,
                    dimColor = Surface2,
                    onClick  = {
                        editText  = item.content
                        isEditing = false
                    }
                )
            } else {
                // Edit button
                ActionButton(
                    label    = "edit",
                    color    = EditBlue,
                    dimColor = EditBlueDim,
                    onClick  = { isEditing = true }
                )
                Spacer(Modifier.width(6.dp))
                // Delete button
                ActionButton(
                    label    = "delete",
                    color    = DeleteRed,
                    dimColor = DeleteRedDim,
                    onClick  = { showConfirm = true }
                )
            }
        }

        // ── Delete confirmation bar ───────────────────────────────────────
        AnimatedVisibility(
            visible = showConfirm,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeleteRedDim)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "delete this item?",
                    color      = DeleteRed,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp
                )
                Row {
                    ActionButton(
                        label    = "yes",
                        color    = DeleteRed,
                        dimColor = DeleteRedDim,
                        onClick  = {
                            onDelete(item)
                            showConfirm = false
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    ActionButton(
                        label    = "no",
                        color    = TextSecondary,
                        dimColor = Surface2,
                        onClick  = { showConfirm = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    label    : String,
    color    : Color,
    dimColor : Color,
    onClick  : () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(dimColor)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .then(Modifier.noRippleClickable(onClick))
    ) {
        Text(
            text       = label,
            color      = color,
            fontFamily = FontFamily.Monospace,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

// clickable without ripple effect
@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit) =
    this.then(
        Modifier.clickable(
            indication         = null,
            interactionSource  = remember { MutableInteractionSource() },
            onClick            = onClick
        )
    )