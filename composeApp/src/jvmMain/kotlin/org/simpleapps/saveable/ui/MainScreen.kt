import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import org.simpleapps.saveable.ui.ItemCard
import org.simpleapps.saveable.ui.MainStateHolder

// ── Palette ────────────────────────────────────────────────────────────────
private val BgDark        = Color(0xFF0E0E10)
val Surface1      = Color(0xFF1A1A1E)
val Surface2      = Color(0xFF242428)
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

    // sync text field → state holder
    LaunchedEffect(Unit) {
        snapshotFlow { textState.text.toString() }
            .collect { stateHolder.onInputChanged(it) }
    }

    // clear text field when state holder clears inputText
    LaunchedEffect(state.inputText) {
        if (state.inputText.isEmpty()) {
            textState.edit { replace(0, length, "") }
        }
    }

    DisposableEffect(Unit) {
        onDispose { stateHolder.onDispose() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 48.dp, vertical = 40.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ──────────────────────────────────────────────────────
            Header()
            Spacer(Modifier.height(32.dp))

            // ── Input ───────────────────────────────────────────────────────
            InputBar(
                textState    = textState,
                onSubmit     = stateHolder::onSubmit,
                isLoading    = state.isLoading
            )
            Spacer(Modifier.height(12.dp))

            // ── Command hint ─────────────────────────────────────────────
            Text(
                text  = "Type /add <category> <content>  ·  /list <category>  ·  /search <query>",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(8.dp))

            // ── Feedback messages ────────────────────────────────────────
            AnimatedVisibility(
                visible = state.successMessage.isNotEmpty(),
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                FeedbackBanner(
                    message = state.successMessage,
                    isError = false
                )
            }
            AnimatedVisibility(
                visible = state.isError && state.errorMessage.isNotEmpty(),
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                FeedbackBanner(
                    message = state.errorMessage,
                    isError = true
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Item list ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = state.items.isNotEmpty(),
                enter   = fadeIn() + slideInVertically()
            ) {
                ItemList(items = state.items)
            }
        }
    }
}

// ── Header ──────────────────────────────────────────────────────────────────
@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AccentGreen)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text       = "SAVEABLE",
            color      = TextPrimary,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text     = "personal vault",
            color    = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ── Input bar ────────────────────────────────────────────────────────────────
@Composable
private fun InputBar(
    textState : androidx.compose.foundation.text.input.TextFieldState,
    onSubmit  : () -> Unit,
    isLoading : Boolean
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
            Text(
                text       = ">",
                color      = AccentGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                state  = textState,
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.key  == Key.Enter &&
                            keyEvent.type == KeyEventType.KeyDown
                        ) {
                            onSubmit()
                            true
                        } else false
                    },
                textStyle = LocalTextStyle.current.copy(
                    color      = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 14.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor          = AccentGreen
                ),
                placeholder = {
                    Text(
                        "/add notes Buy milk  or  /list notes",
                        color      = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 14.sp
                    )
                }
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(16.dp),
                    color     = AccentGreen,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
            } else {
                Text(
                    text     = "↵",
                    color    = TextSecondary,
                    fontSize = 16.sp
                )
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

// ── Feedback banner ──────────────────────────────────────────────────────────
@Composable
private fun FeedbackBanner(message: String, isError: Boolean) {
    val bg     = if (isError) Color(0xFFFF4D6A15) else AccentDim
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
        Text(
            text       = if (isError) "✗" else "✓",
            color      = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize   = 12.sp
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text       = message,
            color      = text,
            fontFamily = FontFamily.Monospace,
            fontSize   = 12.sp
        )
    }
}

// ── Item list ─────────────────────────────────────────────────────────────────
@Composable
private fun ItemList(items: List<SaveableItem>) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text       = "RESULTS",
                color      = TextSecondary,
                fontSize   = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AccentDim)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text       = "${items.size}",
                    color      = AccentGreen,
                    fontSize   = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items) { item ->
                ItemCard(item)
            }
        }
    }
}
