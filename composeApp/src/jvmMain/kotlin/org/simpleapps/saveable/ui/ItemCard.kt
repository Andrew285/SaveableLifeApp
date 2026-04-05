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
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.util.TimeFormatter
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.Base64
import javax.imageio.ImageIO

private val DeleteRed     = Color(0xFFFF4D6A)
private val DeleteRedDim  = Color(0x26FF4D6A)
private val EditBlue      = Color(0xFF4D9EFF)
private val EditBlueDim   = Color(0x264D9EFF)
private val CopyPurple    = Color(0xFFB97FFF)
private val CopyPurpleDim = Color(0x26B97FFF)

// ── Clipboard helpers ──────────────────────────────────────────────────────

/**
 * Transferable that wraps a BufferedImage.
 * This is what allows Ctrl+V in any app (Telegram, browser, Paint…)
 * to receive a real image instead of text.
 */
private class ImageTransferable(private val image: BufferedImage) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)
    override fun isDataFlavorSupported(flavor: DataFlavor) = flavor == DataFlavor.imageFlavor
    override fun getTransferData(flavor: DataFlavor): Any {
        if (!isDataFlavorSupported(flavor)) throw UnsupportedFlavorException(flavor)
        return image
    }
}

/**
 * Decodes [base64] and puts the resulting [BufferedImage] on the system clipboard.
 * Any application that accepts image paste (Telegram, Slack, GIMP, etc.) will
 * receive the actual pixels — not "[image]" text.
 *
 * Falls back to copying the Base64 string if decoding fails.
 */
private fun copyImageToClipboard(base64: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    try {
        val bytes  = Base64.getDecoder().decode(base64)
        val awtImg = ImageIO.read(ByteArrayInputStream(bytes))
        if (awtImg != null) {
            clipboard.setContents(ImageTransferable(awtImg), null)
            return
        }
    } catch (_: Exception) { /* fall through to text fallback */ }
    val sel = StringSelection(base64)
    clipboard.setContents(sel, sel)
}

private fun copyTextToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val sel = StringSelection(text)
    clipboard.setContents(sel, sel)
}

// ── Image decode ───────────────────────────────────────────────────────────

private fun decodeBase64Image(base64: String): ImageBitmap? = try {
    val bytes  = Base64.getDecoder().decode(base64)
    val awtImg = ImageIO.read(ByteArrayInputStream(bytes))
    awtImg?.toComposeImageBitmap()
} catch (_: Exception) { null }

// ── Composable ─────────────────────────────────────────────────────────────

@Composable
fun ItemCard(
    item    : SaveableItem,
    onEdit  : (SaveableItem, String) -> Unit,
    onDelete: (SaveableItem) -> Unit
) {
    var isEditing   by remember { mutableStateOf(false) }
    var editText    by remember { mutableStateOf(item.content) }
    var showConfirm by remember { mutableStateOf(false) }
    var copyFlash   by remember { mutableStateOf(false) }

    val imageBitmap: ImageBitmap? = remember(item.imageBase64) {
        item.imageBase64?.let { decodeBase64Image(it) }
    }

    LaunchedEffect(copyFlash) {
        if (copyFlash) {
            kotlinx.coroutines.delay(1200)
            copyFlash = false
        }
    }

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
                                keyEvent.key == Key.Enter  -> { onEdit(item, editText); isEditing = false; true }
                                keyEvent.key == Key.Escape -> { editText = item.content; isEditing = false; true }
                                else -> false
                            }
                        }
                )
            } else {
                Text(
                    text       = item.content.ifEmpty { if (imageBitmap != null) "📷 image" else "" },
                    color      = TextPrimary,
                    fontSize   = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier   = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.width(12.dp))
            Text(
                text       = TimeFormatter.formatTime(item.createdAt),
                color      = TextSecondary,
                fontSize   = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.width(16.dp))

            // ── Action buttons ──────────────────────────────────────────
            if (isEditing) {
                ActionButton(label = "save",   color = AccentGreen,   dimColor = AccentDim,
                    onClick = { onEdit(item, editText); isEditing = false })
                Spacer(Modifier.width(6.dp))
                ActionButton(label = "cancel", color = TextSecondary, dimColor = Surface2,
                    onClick = { editText = item.content; isEditing = false })
            } else {
                ActionButton(
                    label    = if (copyFlash) "copied!" else "copy",
                    color    = if (copyFlash) AccentGreen else CopyPurple,
                    dimColor = if (copyFlash) AccentDim   else CopyPurpleDim,
                    onClick  = {
                        // Image item → put real BufferedImage on clipboard (pasteable everywhere)
                        // Text item  → put plain string on clipboard
                        if (item.imageBase64 != null) {
                            copyImageToClipboard(item.imageBase64)
                        } else {
                            copyTextToClipboard(item.content)
                        }
                        copyFlash = true
                    }
                )
                Spacer(Modifier.width(6.dp))
                ActionButton(label = "edit",   color = EditBlue,  dimColor = EditBlueDim,
                    onClick = { isEditing = true })
                Spacer(Modifier.width(6.dp))
                ActionButton(label = "delete", color = DeleteRed, dimColor = DeleteRedDim,
                    onClick = { showConfirm = true })
            }
        }

        // ── Embedded image ─────────────────────────────────────────────────
        if (imageBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Surface2, RoundedCornerShape(6.dp))
            ) {
                Image(
                    bitmap             = imageBitmap,
                    contentDescription = "Attached image",
                    contentScale       = ContentScale.FillWidth,
                    modifier           = Modifier.fillMaxWidth()
                )
            }
        }

        // ── Delete confirmation ────────────────────────────────────────────
        AnimatedVisibility(visible = showConfirm, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeleteRedDim)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("delete this item?", color = DeleteRed,
                    fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Row {
                    ActionButton(label = "yes", color = DeleteRed,    dimColor = DeleteRedDim,
                        onClick = { onDelete(item); showConfirm = false })
                    Spacer(Modifier.width(8.dp))
                    ActionButton(label = "no",  color = TextSecondary, dimColor = Surface2,
                        onClick = { showConfirm = false })
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    label   : String,
    color   : Color,
    dimColor: Color,
    onClick : () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(dimColor)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .noRippleClickable(onClick)
    ) {
        Text(
            text          = label,
            color         = color,
            fontFamily    = FontFamily.Monospace,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit) = this.then(
    Modifier.clickable(
        indication        = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick           = onClick
    )
)