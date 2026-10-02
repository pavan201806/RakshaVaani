package org.itantra.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.itantra.audio.EngineState
import java.util.Locale

/**
 * Screen 1: Main (PTT) - Dark Tactical Radio Console.
 * - Top row: device name + "NODE 133 / AES-256" on left, link status chip on right (green outlined "LINK OK" / amber "NO LINK").
 * - Row of 3 info tiles: MODE (PTT/Phone toggle), LANG (opens modal bottom sheet), UNITS (count).
 * - Message area: log-style lines with green/amber edges, tiny meta line (RX/TX, unit, time, status).
 * - Slim dismissible banner for errors / bluetooth state at the top of message area.
 * - Bottom: caption "HOLD TO TALK / VOL DOWN"; ALERT (red, left), 112dp green circular PTT button with pulse ring, LOCATE (amber, right).
 * - STT/LINK/TTS/RTF/CPU debug strip behind developer toggle (hidden by default).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatingScreen(
    state: OperatingState,
    onTransmitChange: (Boolean) -> Unit,
    onAlert: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onMenu: () -> Unit,
    onReplay: (String) -> Unit = {},
    onModeChange: (String) -> Unit = {},
    onLocate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val p = palette
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showDevMetrics by remember { mutableStateOf(false) }
    var dismissedBannerReason by remember { mutableStateOf<EngineState.Degraded.Reason?>(null) }

    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F0B))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // TOP ROW: Device Name + Node ID / AES-256 + Link Status Chip
        TacticalTopRow(state = state, onMenu = onMenu)

        // ROW OF 3 INFO TILES: MODE, LANG, UNITS
        InfoTilesRow(
            state = state,
            onModeToggle = {
                val nextMode = if (state.mode.equals("Phone", ignoreCase = true)) "PTT" else "Phone"
                onModeChange(nextMode)
            },
            onOpenLangSheet = { showLanguageSheet = true },
        )

        // SLIM DISMISSIBLE BANNER FOR SYSTEM NOTICES / ERRORS
        val currentDegraded = state.degraded
        if (currentDegraded != null && currentDegraded != dismissedBannerReason) {
            TacticalDegradedBanner(
                reason = currentDegraded,
                onDismiss = { dismissedBannerReason = currentDegraded },
            )
        }

        // LIVE SPEECH PARTIAL / LISTENING INDICATOR (WHEN TRANSMITTING OR PARTIAL DETECTED)
        if (state.transmitting || state.partial != null) {
            LiveSpeechBox(state)
        }
        state.speechNote?.let { SpeechNote(it) }

        // LOG-STYLE MESSAGE THREAD AREA
        ThreadPane(
            state = state,
            onReplay = onReplay,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        // DEVELOPER METRICS STRIP (Behind Collapsible Toggle)
        DevMetricsSection(
            metrics = state.metrics,
            live = state.transmitting,
            listening = state.listening,
            expanded = showDevMetrics,
            onToggle = { showDevMetrics = !showDevMetrics },
        )

        // BOTTOM TACTICAL PTT CONTROL DECK
        BottomPttDeck(
            state = state,
            onTransmitChange = onTransmitChange,
            onAlert = onAlert,
            onLocate = onLocate,
        )
    }

    // MODAL BOTTOM SHEET FOR LANGUAGE SELECTION
    if (showLanguageSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF131A15),
            contentColor = Color(0xFFE8F5EB),
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        ) {
            LanguageBottomSheetContent(
                state = state,
                onSelectLanguage = { code ->
                    showLanguageSheet = false
                    onLanguageSelected(code)
                },
                onClose = { showLanguageSheet = false },
            )
        }
    }
}

/** Everything the screen needs, and nothing about how it was obtained. */
data class OperatingState(
    val unitName: String,
    val nodeId: Int,
    val peerCount: Int,
    val linkUp: Boolean,
    val transportName: String,
    val mode: String,
    val audience: String,
    val language: String,
    val languageCode: String = "",
    val languages: List<LanguageOption> = emptyList(),
    val transmitting: Boolean = false,
    val listening: Boolean = false,
    val partial: String? = null,
    val confidence: Int? = null,
    val level: Float = 0f,
    val speechNote: String? = null,
    val messages: List<LoggedMessage> = emptyList(),
    val degraded: EngineState.Degraded.Reason? = null,
    val metrics: BandFMetrics = BandFMetrics(),
    val queued: Int = 0,
    val speakingFrom: String? = null,
    val units: List<UnitInfo> = emptyList(),
    val openLinePaused: Boolean = false,
)

/** The instrument strip's numbers, from the utterance that just happened. */
data class BandFMetrics(
    val sttMillis: Long? = null,
    val linkMillis: Long? = null,
    val ttsMillis: Long? = null,
    val totalMillis: Long? = null,
    val realTimeFactor: Double? = null,
    val cpuCores: Double? = null,
    val cpuCoreCount: Int? = null,
    val lastFrameBytes: Int? = null,
    val audioMillis: Long? = null,
) {
    val compressionRatio: Int?
        get() {
            val bytes = lastFrameBytes?.takeIf { it > 0 } ?: return null
            val audio = audioMillis?.takeIf { it > 0 }?.let { it * BYTES_PER_SECOND / 1000.0 }
            return Math.round((audio ?: RAW_AUDIO_BYTES) / bytes).toInt()
        }

    private companion object {
        const val RAW_AUDIO_BYTES = 96_000.0
        const val BYTES_PER_SECOND = 32_000
    }
}

// ── TOP ROW ──────────────────────────────────────────────────────────────────

@Composable
private fun TacticalTopRow(
    state: OperatingState,
    onMenu: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131A15))
            .border(Tokens.Hairline, Color(0xFF1E2A21))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: Menu Icon + Device Name + NODE 133 / AES-256
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .sizeIn(minWidth = Tokens.TouchTarget, minHeight = Tokens.TouchTarget)
                    .clickable(onClick = onMenu)
                    .semantics(mergeDescendants = true) { contentDescription = "Settings Menu" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Grid,
                    contentDescription = null,
                    tint = Color(0xFF39FF6A),
                    modifier = Modifier.size(20.dp),
                )
            }

            Column {
                Text(
                    text = state.unitName.ifBlank { "UNIT_ALPHA" }.uppercase(Locale.ROOT),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE8F5EB),
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = "NODE ${"%02d".format(state.nodeId)} / AES-256",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6E8A75),
                    letterSpacing = 0.5.sp,
                )
            }
        }

        // Right: Link Status Chip (green outlined "LINK OK" / amber "NO LINK")
        val isLinkOk = state.linkUp
        val chipColor = if (isLinkOk) Color(0xFF39FF6A) else Color(0xFFFFB000)

        Row(
            modifier = Modifier
                .background(Color(0xFF0A0F0B), RoundedCornerShape(4.dp))
                .border(1.dp, chipColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(if (isLinkOk) pulseAlpha else 1f)
                    .background(chipColor, CircleShape),
            )
            Text(
                text = if (isLinkOk) "LINK OK" else "NO LINK",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = chipColor,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

// ── ROW OF 3 INFO TILES ──────────────────────────────────────────────────────

@Composable
private fun InfoTilesRow(
    state: OperatingState,
    onModeToggle: () -> Unit,
    onOpenLangSheet: () -> Unit,
) {
    val phone = state.mode.equals("Phone", ignoreCase = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Tile 1: MODE (PTT / Phone Toggle)
        InfoTile(
            label = "MODE",
            value = if (phone) "PHONE" else "PTT",
            subValue = if (phone) "DUPLEX" else "SIMPLEX",
            accentColor = Color(0xFF39FF6A),
            onClick = onModeToggle,
            modifier = Modifier.weight(1f),
            actionHint = "Toggle",
        )

        // Tile 2: LANG (Opens Modal Bottom Sheet)
        InfoTile(
            label = "LANG",
            value = state.languageCode.ifBlank { "HI" }.uppercase(Locale.ROOT),
            subValue = state.language.take(8).uppercase(Locale.ROOT),
            accentColor = Color(0xFFFFB000),
            onClick = onOpenLangSheet,
            modifier = Modifier.weight(1f),
            actionHint = "Select",
        )

        // Tile 3: UNITS (Count)
        InfoTile(
            label = "UNITS",
            value = "${state.peerCount} PEER${if (state.peerCount == 1) "" else "S"}",
            subValue = if (state.linkUp) "MESH ACTV" else "ISOLATED",
            accentColor = if (state.peerCount > 0) Color(0xFF39FF6A) else Color(0xFF6E8A75),
            onClick = null,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InfoTile(
    label: String,
    value: String,
    subValue: String,
    accentColor: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actionHint: String? = null,
) {
    Box(
        modifier = modifier
            .background(Color(0xFF131A15), RoundedCornerShape(6.dp))
            .border(Tokens.Hairline, Color(0xFF1E2A21), RoundedCornerShape(6.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6E8A75),
                    letterSpacing = 0.5.sp,
                )
                if (actionHint != null) {
                    Text(
                        text = "▾",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor,
                    )
                }
            }

            Text(
                text = value,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
            )

            Text(
                text = subValue,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF6E8A75),
                maxLines = 1,
            )
        }
    }
}

// ── SLIM DISMISSIBLE BANNER ──────────────────────────────────────────────────

@Composable
private fun TacticalDegradedBanner(
    reason: EngineState.Degraded.Reason,
    onDismiss: () -> Unit,
) {
    val advice = adviceFor(reason)
    val isRed = reason == EngineState.Degraded.Reason.BLUETOOTH_OFF ||
        reason == EngineState.Degraded.Reason.PERMISSION_DENIED
    val bannerBorder = if (isRed) Color(0xFFFF3B30) else Color(0xFFFFB000)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .background(Color(0xFF131A15), RoundedCornerShape(4.dp))
            .border(1.dp, bannerBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                advice.icon,
                contentDescription = null,
                tint = bannerBorder,
                modifier = Modifier.size(16.dp),
            )
            Column {
                Text(
                    text = reason.message.uppercase(Locale.ROOT),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = bannerBorder,
                )
                Text(
                    text = advice.doThis,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFFE8F5EB),
                    maxLines = 1,
                )
            }
        }

        Box(
            modifier = Modifier
                .sizeIn(minWidth = 36.dp, minHeight = 36.dp)
                .clickable(onClick = onDismiss)
                .semantics { contentDescription = "Dismiss notice" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✕",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6E8A75),
            )
        }
    }
}

// ── LIVE SPEECH BOX ──────────────────────────────────────────────────────────

@Composable
private fun LiveSpeechBox(state: OperatingState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(Color(0xFF131A15), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF39FF6A), RoundedCornerShape(6.dp))
            .padding(8.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = state.partial?.let { "Speech: $it" } ?: "Transmitting audio..."
            },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(Modifier.size(5.dp).background(Color(0xFF39FF6A), CircleShape))
                    Text(
                        text = "LIVE SPEECH ASR // 16kHz",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF39FF6A),
                    )
                }
                state.confidence?.let { conf ->
                    Text(
                        text = "CONF: $conf/4",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF39FF6A),
                    )
                }
            }
            Text(
                text = state.partial?.let { "“$it”" } ?: "Transmitting voice stream...",
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE8F5EB),
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun SpeechNote(note: String) {
    Text(
        text = note,
        fontSize = 8.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFFFB000),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x20FFB000))
            .padding(horizontal = 10.dp, vertical = 3.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

// ── THREAD PANE ─────────────────────────────────────────────────────────────

@Composable
private fun ThreadPane(
    state: OperatingState,
    onReplay: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.messages.isEmpty()) {
        EmptyState(
            icon = Icons.Transmit,
            title = "CHANNEL QUIET",
            body = "Hold the green PTT button below to speak on the mesh channel.",
            modifier = modifier,
        )
        return
    }

    val list = rememberLazyListState()
    val reduced = reducedMotion
    LaunchedEffect(state.messages.size) {
        val last = state.messages.lastIndex
        if (last < 0) return@LaunchedEffect
        if (reduced) list.scrollToItem(last) else list.animateScrollToItem(last)
    }

    LazyColumn(
        modifier = modifier,
        state = list,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(state.messages) { message ->
            MessageBubble(
                message = message,
                speaking = state.speakingFrom != null && state.speakingFrom == message.from,
                onReplay = onReplay,
            )
        }
    }
}

// ── DEVELOPER METRICS SECTION (COLLAPSIBLE) ──────────────────────────────────

@Composable
private fun DevMetricsSection(
    metrics: BandFMetrics,
    live: Boolean,
    listening: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1511))
            .border(Tokens.Hairline, Color(0xFF1E2A21)),
    ) {
        // Toggle bar with minimum 48dp touch target
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 32.dp)
                .clickable(onClick = onToggle)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "DEV DIAGNOSTICS",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6E8A75),
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = if (expanded) "▲ HIDE" else "▼ SHOW",
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF39FF6A),
                )
            }

            Text(
                text = "RTF: ${metrics.realTimeFactor?.let { "%.2f".format(Locale.ROOT, it) } ?: "—"} // CPU: ${cpuLabel(metrics.cpuCores, metrics.cpuCoreCount)}",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF6E8A75),
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .semantics { contentDescription = spokenMetrics(metrics) },
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (live) {
                    Instrument(if (listening) "LISTENING // RAW PCM STREAM" else "HOLDING FLOOR // MIC SEIZED")
                    Instrument("CPU: ${cpuLabel(metrics.cpuCores, metrics.cpuCoreCount)}")
                } else {
                    Instrument("STT: ${ms(metrics.sttMillis)} // LINK: ${ms(metrics.linkMillis)} // TTS: ${ms(metrics.ttsMillis)}")
                    Instrument(
                        buildString {
                            append("TOTAL: ${ms(metrics.totalMillis)}")
                            append(" // RTF: ${metrics.realTimeFactor?.let { "%.2f".format(Locale.ROOT, it) } ?: "—"}")
                            append(" // CPU: ${cpuLabel(metrics.cpuCores, metrics.cpuCoreCount)}")
                            metrics.lastFrameBytes?.let { append(" // $it B (${metrics.compressionRatio}×)") }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun Instrument(text: String) {
    Text(
        text = text,
        fontSize = 8.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF6E8A75),
    )
}

// ── BOTTOM TACTICAL PTT CONTROL DECK ─────────────────────────────────────────

@Composable
private fun BottomPttDeck(
    state: OperatingState,
    onTransmitChange: (Boolean) -> Unit,
    onAlert: () -> Unit,
    onLocate: () -> Unit,
) {
    val isTransmitting = state.transmitting
    val dock = dockStateOf(state)

    // Pulse animation ring for PTT button
    val infiniteTransition = rememberInfiniteTransition(label = "ptt_pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring_scale",
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring_alpha",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131A15))
            .border(Tokens.Hairline, Color(0xFF1E2A21))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Caption: "HOLD TO TALK / VOL DOWN"
        Text(
            text = "HOLD TO TALK / VOL DOWN",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isTransmitting) Color(0xFF39FF6A) else Color(0xFF6E8A75),
            letterSpacing = 1.sp,
        )

        // Action Row: ALERT (red, left) | Circular PTT (~112dp, green, center) | LOCATE (amber, right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // LEFT: ALERT BUTTON (Red #FF3B30)
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 52.dp)
                    .background(Color(0x30FF3B30), RoundedCornerShape(6.dp))
                    .border(1.5.dp, Color(0xFFFF3B30), RoundedCornerShape(6.dp))
                    .clickable(onClick = onAlert)
                    .semantics(mergeDescendants = true) { contentDescription = "Emergency SOS Alert" },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        Icons.Alert,
                        contentDescription = null,
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "ALERT",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF3B30),
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            // CENTER: LARGE SOLID GREEN CIRCULAR PTT BUTTON (~112dp)
            Box(
                modifier = Modifier.size(112.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Expanding Ring Pulse when transmitting or active
                if (isTransmitting) {
                    Box(
                        modifier = Modifier
                            .size(112.dp * ringScale)
                            .border(2.dp, Color(0xFF39FF6A).copy(alpha = ringAlpha), CircleShape),
                    )
                }

                // Outer border guide ring
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .border(
                            width = 2.dp,
                            color = if (isTransmitting) Color(0xFF39FF6A) else Color(0xFF1E2A21),
                            shape = CircleShape,
                        ),
                )

                // Solid circular PTT trigger
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (isTransmitting) Color(0xFF22C55E) else Color(0xFF39FF6A))
                        .pointerInput(dock) {
                            if (dock == DockState.BUSY) return@pointerInput
                            detectTapGestures(
                                onPress = {
                                    onTransmitChange(true)
                                    tryAwaitRelease()
                                    onTransmitChange(false)
                                },
                            )
                        }
                        .semantics {
                            contentDescription = if (isTransmitting) "Transmitting speech" else "Hold to talk"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            Icons.Transmit,
                            contentDescription = null,
                            tint = Color(0xFF0A0F0B),
                            modifier = Modifier.size(30.dp),
                        )
                        Text(
                            text = if (isTransmitting) "LIVE" else "PTT",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0A0F0B),
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
            }

            // RIGHT: LOCATE BUTTON (Amber #FFB000)
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 52.dp)
                    .background(Color(0x24FFB000), RoundedCornerShape(6.dp))
                    .border(1.5.dp, Color(0xFFFFB000), RoundedCornerShape(6.dp))
                    .clickable(onClick = onLocate)
                    .semantics(mergeDescendants = true) { contentDescription = "Locate nearby nodes" },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        Icons.Globe,
                        contentDescription = null,
                        tint = Color(0xFFFFB000),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "LOCATE",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFB000),
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }
    }
}

// ── MODAL BOTTOM SHEET CONTENT FOR LANGUAGE SELECTION ────────────────────────

@Composable
private fun LanguageBottomSheetContent(
    state: OperatingState,
    onSelectLanguage: (String) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SELECT ENGINE LANGUAGE",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB000),
                letterSpacing = 0.5.sp,
            )
            Text(
                text = "${state.languages.size} SUPPORTED",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF6E8A75),
            )
        }

        Spacer(Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(state.languages) { opt ->
                val isSelected = opt.code == state.languageCode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) Color(0x24FFB000) else Color(0xFF0F1511), RoundedCornerShape(6.dp))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFFFB000) else Color(0xFF1E2A21),
                            shape = RoundedCornerShape(6.dp),
                        )
                        .clickable { onSelectLanguage(opt.code) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(if (isSelected) Color(0xFFFFB000) else Color(0xFF131A15), RoundedCornerShape(4.dp))
                                .border(1.dp, if (isSelected) Color(0xFFFFB000) else Color(0xFF1E2A21), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = opt.code.uppercase(Locale.ROOT),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF0A0F0B) else Color(0xFFE8F5EB),
                            )
                        }

                        Column {
                            Text(
                                text = opt.nativeName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE8F5EB),
                            )
                            if (opt.englishName != opt.nativeName) {
                                Text(
                                    text = opt.englishName,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF6E8A75),
                                )
                            }
                        }
                    }

                    if (isSelected) {
                        Text(
                            text = "● ACTIVE",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB000),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
    }
}

private fun ms(value: Long?): String = value?.let { "$it ms" } ?: "—"

internal fun cpuLabel(
    cores: Double?,
    of: Int?,
): String {
    if (cores == null) return "—"
    val used = "%.2f".format(Locale.ROOT, cores)
    return if (of != null && of > 0) "$used/$of cores" else "$used cores"
}

internal fun spokenMetrics(m: BandFMetrics): String {
    val parts = ArrayList<String>()
    m.totalMillis?.let { parts += "Total $it milliseconds" }
    m.sttMillis?.let { parts += "Recognition $it" }
    m.linkMillis?.let { parts += "Link $it" }
    m.ttsMillis?.let { parts += "Speech $it" }
    m.lastFrameBytes?.let {
        parts += "Last frame $it bytes, ${m.compressionRatio} times smaller than audio"
    }
    m.realTimeFactor?.let { parts += "Real time factor ${"%.2f".format(Locale.ROOT, it)}" }
    m.cpuCores?.let {
        val used = "%.2f".format(Locale.ROOT, it)
        parts += m.cpuCoreCount
            ?.let { n -> "Processor $used of $n cores" }
            ?: "Processor $used cores"
    }
    if (parts.isEmpty()) return "Nothing measured yet."
    return "Latency. " + parts.joinToString(". ") + "."
}

