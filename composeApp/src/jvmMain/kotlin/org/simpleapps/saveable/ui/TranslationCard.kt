package org.simpleapps.saveable.ui

import AccentDim
import AccentGreen
import Surface1
import Surface2
import TextPrimary
import TextSecondary
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.simpleapps.saveable.domain.translation.Definition
import org.simpleapps.saveable.domain.translation.TranslationResult

// ── Extra palette tokens ───────────────────────────────────────────────────
private val AccentBlue      = Color(0xFF4D9EFF)
private val AccentBlueDim   = Color(0x264D9EFF)
private val AccentYellow    = Color(0xFFFFD166)
private val AccentYellowDim = Color(0x26FFD166)
private val PosColor        = Color(0xFFFF9F51)   // part-of-speech badge
private val PosDim          = Color(0x26FF9F51)
private val SynColor        = Color(0xFFB97FFF)
private val SynDim          = Color(0x26B97FFF)
private val DividerColor    = Color(0xFF2A2A30)

@Composable
fun TranslationCard(
    result    : TranslationResult,
    onDismiss : () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface1)
            .border(1.dp, AccentGreen.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
    ) {

        // ── Header row ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentDim)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source word + phonetic
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text       = result.sourceText,
                        color      = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                    if (!result.phonetic.isNullOrBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "/${result.phonetic}/",
                            color      = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize   = 11.sp
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = result.destinationText,
                    color      = AccentGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp
                )
            }

            // Collapse / expand toggle
            SmallActionButton(
                label    = if (expanded) "▲ collapse" else "▼ expand",
                color    = TextSecondary,
                dimColor = Surface2,
                onClick  = { expanded = !expanded }
            )
            Spacer(Modifier.width(6.dp))
            SmallActionButton(
                label    = "✕ dismiss",
                color    = Color(0xFFFF4D6A),
                dimColor = Color(0x26FF4D6A),
                onClick  = onDismiss
            )
        }

        // ── Hint ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentYellowDim)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💡 ", fontSize = 11.sp)
            Text(
                text       = "Type  /add <list>  then Enter  to save this word",
                color      = AccentYellow,
                fontFamily = FontFamily.Monospace,
                fontSize   = 11.sp
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter   = fadeIn() + slideInVertically { -it / 4 }
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                // ── Possible translations ─────────────────────────────────
                if (result.possibleTranslations.isNotEmpty()) {
                    SectionLabel("TRANSLATIONS")
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.possibleTranslations.forEach { t ->
                            Chip(text = t, color = AccentBlue, dimColor = AccentBlueDim)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                // ── Definitions ───────────────────────────────────────────
                if (result.definitions.isNotEmpty()) {
                    SectionLabel("DEFINITIONS")
                    Spacer(Modifier.height(8.dp))

                    result.definitions.forEachIndexed { index, def ->
                        DefinitionBlock(def)
                        if (index < result.definitions.lastIndex) {
                            HorizontalDivider(
                                modifier  = Modifier.padding(vertical = 10.dp),
                                thickness = 1.dp,
                                color     = DividerColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DefinitionBlock(def: Definition) {
    Column {
        // Part-of-speech badge
        if (def.partOfSpeech.isNotBlank()) {
            Chip(text = def.partOfSpeech, color = PosColor, dimColor = PosDim)
            Spacer(Modifier.height(6.dp))
        }

        // Definition text
        Text(
            text       = def.definition,
            color      = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize   = 13.sp,
            lineHeight = 18.sp
        )

        // Example sentence
        if (!def.example.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            val annotated = buildAnnotatedString {
                withStyle(SpanStyle(color = TextSecondary, fontStyle = FontStyle.Italic)) {
                    append("e.g. ")
                }
                withStyle(SpanStyle(color = TextSecondary.copy(alpha = 0.85f), fontStyle = FontStyle.Italic)) {
                    // Strip any HTML bold tags from the API response
                    append(def.example.replace(Regex("</?b>"), ""))
                }
            }
            Text(
                text       = annotated,
                fontFamily = FontFamily.Monospace,
                fontSize   = 12.sp,
                lineHeight = 17.sp
            )
        }

        // Synonyms
        if (def.synonyms.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            SectionLabel("SYNONYMS", size = 9)
            Spacer(Modifier.height(4.dp))
            // Wrap synonyms in a horizontal scroll row, show up to 10
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                def.synonyms.take(10).forEach { syn ->
                    Chip(text = syn, color = SynColor, dimColor = SynDim, fontSize = 10)
                }
                if (def.synonyms.size > 10) {
                    Chip(
                        text     = "+${def.synonyms.size - 10} more",
                        color    = TextSecondary,
                        dimColor = Surface2,
                        fontSize = 10
                    )
                }
            }
        }
    }
}

// ── Small reusable pieces ──────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, size: Int = 10) {
    Text(
        text          = text,
        color         = TextSecondary,
        fontSize      = size.sp,
        fontFamily    = FontFamily.Monospace,
        fontWeight    = FontWeight.Bold,
        letterSpacing = 2.sp
    )
}

@Composable
private fun Chip(
    text    : String,
    color   : Color,
    dimColor: Color,
    fontSize: Int = 11
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(dimColor)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text       = text,
            color      = color,
            fontSize   = fontSize.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SmallActionButton(
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
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            )
    ) {
        Text(
            text       = label,
            color      = color,
            fontSize   = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}