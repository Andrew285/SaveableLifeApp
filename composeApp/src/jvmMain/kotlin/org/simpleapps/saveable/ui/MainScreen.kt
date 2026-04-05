import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.domain.autocomplete.Suggestion
import org.simpleapps.saveable.ui.ItemCard
import org.simpleapps.saveable.ui.MainStateHolder

// ── Palette ────────────────────────────────────────────────────────────────
private val BgDark        = Color(0xFF0E0E10)
val Surface1      = Color(0xFF1A1A1E)
val Surface2      = Color(0xFF242428)
private val SurfaceHover  = Color(0xFF2E2E34)
val AccentGreen   = Color(0xFF00FF85)
val AccentDim     = Color(0xFF00FF8520)
val TextPrimary   = Color(0xFFF0F0F0)
val TextSecondary = Color(0xFF888890)
private val ErrorColor    = Color(0xFFFF4D6A)

@Composable
fun MainScreen() {
    val stateHolder = koinInject<MainStateHolder>()
    val textState   = rememberTextFieldState()
    val state       = stateHolder.uiState

    LaunchedEffect(Unit) {
        snapshotFlow { textState.text.toString() }
            .collect { stateHolder.onInputChanged(it) }
    }

    // sync insertText back into the text field when suggestion selected
    LaunchedEffect(state.inputText) {
        val current = textState.text.toString()
        if (state.inputText != current) {
            textState.edit { replace(0, length, state.inputText) }
        }
    }

    DisposableEffect(Unit) { onDispose { stateHolder.onDispose() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 48.dp, vertical = 40.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Header()
            Spacer(Modifier.height(32.dp))

            // Input + autocomplete dropdown stacked together
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    InputBar(
                        textState  = textState,
                        isLoading  = state.isLoading,
                        onSubmit   = stateHolder::onSubmit,
                        onNavigate = stateHolder::onSuggestionNavigate
                    )

                    // Dropdown appears directly below input
                    AnimatedVisibility(
                        visible = state.suggestions.isNotEmpty(),
                        enter   = fadeIn() + slideInVertically { -it / 2 },
                        exit    = fadeOut()
                    ) {
                        SuggestionsDropdown(
                            suggestions      = state.suggestions,
                            selectedIndex    = state.selectedSuggestion,
                            onSelect         = stateHolder::onSuggestionSelected
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text       = "↑↓ navigate  ·  Tab / Enter select  ·  Enter submit",
                color      = TextSecondary,
                fontSize   = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = state.successMessage.isNotEmpty(),
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                FeedbackBanner(state.successMessage, isError = false)
            }

            AnimatedVisibility(
                visible = state.isError && state.errorMessage.isNotEmpty(),
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                FeedbackBanner(state.errorMessage, isError = true)
            }

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = state.items.isNotEmpty(),
                enter   = fadeIn() + slideInVertically()
            ) {
                ItemList(
                    items = state.items,
                    onEditItem = { item, newValue ->
                        stateHolder.onEditItem(item, newValue)
                    },
                    onDeleteItem = { item ->
                        stateHolder.onDeleteItem(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun InputBar(
    textState  : androidx.compose.foundation.text.input.TextFieldState,
    isLoading  : Boolean,
    onSubmit   : () -> Unit,
    onNavigate : (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface1)
            .border(1.dp, Surface2, RoundedCornerShape(10.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(">", color = AccentGreen, fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                state     = textState,
                modifier  = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { keyEvent ->
                        when {
                            keyEvent.type != KeyEventType.KeyDown -> false
                            keyEvent.key  == Key.Enter -> { onSubmit(); true }
                            keyEvent.key  == Key.Tab   -> { onSubmit(); true }
                            keyEvent.key  == Key.DirectionDown -> { onNavigate(true);  true }
                            keyEvent.key  == Key.DirectionUp   -> { onNavigate(false); true }
                            else -> false
                        }
                    },
                textStyle = LocalTextStyle.current.copy(
                    color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 14.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor          = AccentGreen
                ),
                placeholder = {
                    Text("/add notes Buy milk", color = TextSecondary,
                        fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                }
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), color = AccentGreen, strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
            } else {
                Text("↵", color = TextSecondary, fontSize = 16.sp)
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun SuggestionsDropdown(
    suggestions  : List<Suggestion>,
    selectedIndex: Int,
    onSelect     : (Suggestion) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
            .background(Surface1)
            .border(1.dp, Surface2, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
            .padding(vertical = 4.dp)
    ) {
        suggestions.forEachIndexed { index, suggestion ->
            val isSelected = index == selectedIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) SurfaceHover else Color.Transparent)
                    .clickable { onSelect(suggestion) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = if (isSelected) "▸" else " ",
                    color      = AccentGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp,
                    modifier   = Modifier.width(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = suggestion.displayText,
                    color      = if (isSelected) TextPrimary else TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    modifier   = Modifier.weight(1f)
                )
                Text(
                    text       = suggestion.description,
                    color      = TextSecondary.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp
                )
            }
        }
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(AccentGreen))
        Spacer(Modifier.width(10.dp))
        Text("SAVEABLE", color = TextPrimary, fontSize = 13.sp,
            fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 4.sp)
        Spacer(Modifier.width(16.dp))
        Text("personal vault", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun FeedbackBanner(message: String, isError: Boolean) {
    val bg     = if (isError) Color(0x26FF4D6A) else AccentDim
    val border = if (isError) ErrorColor else AccentGreen
    val text   = if (isError) ErrorColor else AccentGreen
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (isError) "✗" else "✓", color = text,
            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.width(10.dp))
        Text(message, color = text, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}

@Composable
private fun ItemList(
    items: List<SaveableItem>,
    onEditItem: (SaveableItem, String) -> Unit,
    onDeleteItem: (SaveableItem) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)) {
            Text("RESULTS", color = TextSecondary, fontSize = 10.sp,
                fontFamily = FontFamily.Monospace, letterSpacing = 3.sp)
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp))
                .background(AccentDim).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text("${items.size}", color = AccentGreen, fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(items) { item ->
                ItemCard(
                    item     = item,
                    onEdit   = { it, newContent -> onEditItem(it, newContent) },
                    onDelete = { onDeleteItem(it) }
                )
            }
        }
    }
}
