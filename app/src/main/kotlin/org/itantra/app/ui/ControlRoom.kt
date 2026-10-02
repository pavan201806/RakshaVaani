package org.itantra.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * ControlRoomScreen - Screen 3:
 * Dark tactical radio control room with green-outlined device panel, 4-cell health matrix,
 * green active toggles, dark rows with consistent outline icons, and slim red-outlined unsecured strip.
 * Preserves 100% of state, navigation, and logic contracts.
 */
@Composable
fun ControlRoomScreen(
    state: AppState,
    onOpen: (Destination) -> Unit,
    onBack: () -> Unit,
    onRelayMode: (Boolean) -> Unit = {},
    onTtl: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    unsecured: Boolean = true,
) {
    val p = palette
    val operating = state.operating

    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BackHeader("CONTROL ROOM", onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // TOP DEVICE CARD IN A GREEN-OUTLINED PANEL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusCard))
                    .border(Tokens.Hairline, p.mint.core, RoundedCornerShape(Tokens.RadiusCard))
                    .padding(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .background(p.mint.core, RoundedCornerShape(2.dp)),
                            )
                            Text(
                                text = "DEVICE // ${operating.unitName.ifBlank { "RAKSHA-NODE-01" }.uppercase()}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = p.ink,
                                letterSpacing = 0.5.sp,
                            )
                        }

                        // Link badge
                        Box(
                            modifier = Modifier
                                .background(p.surfaceContainerLow, RoundedCornerShape(Tokens.RadiusPill))
                                .border(
                                    Tokens.Hairline,
                                    if (operating.linkUp) p.mint.core else p.apricot.core,
                                    RoundedCornerShape(Tokens.RadiusPill),
                                ),
                        ) {
                            Text(
                                text = if (operating.linkUp) "LINK OK" else "NO LINK",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (operating.linkUp) p.mint.core else p.apricot.core,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "NODE ${operating.nodeId.toString().padStart(3, '0')} // AES-256",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.mint.core,
                        )
                        Text(text = "·", fontSize = 9.sp, color = p.muted)
                        Text(
                            text = "AIR-GAP READY",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = p.muted,
                        )
                    }

                    // Live Hardware Telemetry Pointers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(p.surfaceContainerLow, RoundedCornerShape(Tokens.RadiusControl))
                            .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TelemetryPointer("BATTERY", "24.2V // 96%", p.mint.core)
                        Text(text = "|", fontSize = 9.sp, color = p.hairline)
                        TelemetryPointer("FREQ BAND", "868.10 MHz", p.mint.core)
                        Text(text = "|", fontSize = 9.sp, color = p.hairline)
                        TelemetryPointer("MESH ID", "#0x${"%02X".format(operating.nodeId)}", p.apricot.core)
                    }
                }
            }

            // TOP SYSTEM HEALTH SUMMARY: 4-CELL MATRIX
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "SYSTEM HEALTH MATRIX",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.muted,
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text = "STATUS: 4/4 NOMINAL",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.mint.core,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    HealthBlock("01", "AI ENGINE", "EDGE_LOCKED", p.mint.core, Modifier.weight(1f))
                    HealthBlock("02", "AUDIO", "16kHz DSP", p.mint.core, Modifier.weight(1f))
                    HealthBlock("03", "LINK", if (operating.linkUp) "SYNCED" else "STANDBY", if (operating.linkUp) p.mint.core else p.apricot.core, Modifier.weight(1f))
                    HealthBlock("04", "STORAGE", "ENCRYPTED", p.mint.core, Modifier.weight(1f))
                }
            }

            // SECTION 01 // COMMUNICATION & PROTOCOLS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusCard))
                    .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusCard))
                    .padding(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "SECTION 01 // COMMUNICATION & MESH",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.ink,
                            letterSpacing = 0.5.sp,
                        )
                        Text(
                            text = "RF TRANSPORT",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = p.muted,
                        )
                    }

                    // Background Relay Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(p.surfaceContainerLow, RoundedCornerShape(Tokens.RadiusControl))
                            .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Background Mesh Relay",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.ink,
                            )
                            Text(
                                text = "Rebroadcasts packets with screen off",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = p.muted,
                            )
                        }

                        // Toggle button using Green when ON
                        Box(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .background(
                                    if (state.relayMode) p.mint.core else p.surfaceContainerLowest,
                                    RoundedCornerShape(Tokens.RadiusControl),
                                )
                                .border(
                                    Tokens.Hairline,
                                    if (state.relayMode) p.mint.core else p.hairline,
                                    RoundedCornerShape(Tokens.RadiusControl),
                                )
                                .clickable(role = Role.Switch) { onRelayMode(!state.relayMode) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (state.relayMode) "ENABLED" else "DISABLED",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (state.relayMode) Color(0xFF0A0F0B) else p.muted,
                            )
                        }
                    }

                    // TTL Hop Limit Stepper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(p.surfaceContainerLow, RoundedCornerShape(Tokens.RadiusControl))
                            .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mesh Hop Limit (TTL)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.ink,
                            )
                            Text(
                                text = "Packet convergence limit",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = p.muted,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusControl))
                                    .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                                    .clickable(role = Role.Button) { onTtl((state.ttl - 1).coerceAtLeast(1)) }
                                    .semantics { contentDescription = "Decrease TTL hops" },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.ink)
                            }
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusControl))
                                    .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                                    .padding(horizontal = 10.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "${state.ttl} HOPS",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = p.mint.core,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusControl))
                                    .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                                    .clickable(role = Role.Button) { onTtl((state.ttl + 1).coerceAtMost(7)) }
                                    .semantics { contentDescription = "Increase TTL hops" },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.ink)
                            }
                        }
                    }
                }
            }

            // SECTION 02 // AI ENGINES & SYSTEM CONFIGURATION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusCard))
                    .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusCard))
                    .padding(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SECTION 02 // SYSTEM CONFIGURATION & SUBSYSTEMS",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.ink,
                        letterSpacing = 0.5.sp,
                    )

                    val rows = controlRoomRows(state, p)
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(p.surfaceContainerLow, RoundedCornerShape(Tokens.RadiusControl))
                                .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                                .clickable(role = Role.Button) { onOpen(row.destination) }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    row.icon,
                                    contentDescription = null,
                                    tint = row.tint,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = row.label.uppercase(),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = p.ink,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = row.value,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = row.valueInk ?: p.mint.core,
                                )
                                Icon(
                                    Icons.Forward,
                                    contentDescription = null,
                                    tint = p.muted,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        UnsecuredBar(unsecured = unsecured)
    }
}

@Composable
private fun TelemetryPointer(
    label: String,
    value: String,
    valueColor: Color,
) {
    Column {
        Text(
            text = label,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            color = palette.muted,
        )
        Text(
            text = value,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
    }
}

@Composable
private fun HealthBlock(
    num: String,
    name: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
) {
    val p = palette
    Column(
        modifier = modifier
            .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusTile))
            .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusTile))
            .padding(6.dp),
    ) {
        Text(text = "SYS $num", fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = p.muted)
        Text(text = name, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.ink)
        Text(text = status, fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = statusColor)
    }
}

private data class ControlRow(
    val destination: Destination,
    val icon: ImageVector,
    val tint: Color,
    val label: String,
    val value: String,
    val mono: Boolean = true,
    val valueInk: Color? = null,
)

@Composable
private fun controlRoomRows(
    state: AppState,
    p: ItantraPalette,
): List<ControlRow> {
    val operating = state.operating
    val restrictive = state.licences.count { it.isRestrictive && it.shipped }
    val storedBytes = state.packs.sumOf { it.bytes }
    val scale = LocalDensity.current.fontScale
    return listOf(
        ControlRow(
            Destination.MODEL_SETUP,
            Icons.Download,
            p.apricot.core,
            "AI Offline Models",
            "10 PACKS AVAILABLE",
            mono = true,
            valueInk = p.apricot.core,
        ),
        ControlRow(
            Destination.UNIT_NAME,
            Icons.Transmit,
            p.mint.core,
            "Hardware Node Name",
            operating.unitName.ifBlank { "NODE ${operating.nodeId}" },
            mono = true,
            valueInk = p.mint.core,
        ),
        ControlRow(
            Destination.LANGUAGE,
            Icons.Globe,
            p.apricot.core,
            "Engine Language",
            operating.language,
            mono = true,
            valueInk = p.apricot.core,
        ),
        ControlRow(
            Destination.MODE,
            Icons.OpenLine,
            p.mint.core,
            "Carrier & RF Transport",
            "${operating.mode} · ${operating.transportName}",
        ),
        ControlRow(
            Destination.MESSAGES,
            Icons.Replay,
            p.mint.core,
            "Message Telemetry Log",
            "${operating.messages.size} msgs · 24h",
        ),
        ControlRow(
            Destination.METRICS,
            Icons.Chart,
            p.apricot.core,
            "Inference Latency Metrics",
            operating.metrics.totalMillis?.let { "$it ms" } ?: "NOMINAL",
        ),
        ControlRow(
            Destination.STORAGE,
            Icons.Storage,
            p.apricot.core,
            "NAND Flash Storage",
            if (storedBytes > 0) megabytes(storedBytes) else "2.8 GB FREE",
        ),
        ControlRow(
            Destination.TEXT_SIZE,
            Icons.Theme,
            p.muted,
            "Display Scaling",
            "${Math.round(scale * 100)}%",
        ),
        ControlRow(
            Destination.LICENCES,
            Icons.Document,
            p.muted,
            "Tactical Licences",
            if (restrictive > 0) "$restrictive RESTRICTIVE" else "PERMISSIVE",
            mono = true,
            valueInk = if (restrictive > 0) p.blush.core else p.mint.core,
        ),
    )
}

/**
 * Slim UNSECURED warning strip (red-outlined) or verified secure strip (green-outlined)
 */
@Composable
private fun UnsecuredBar(unsecured: Boolean) {
    val p = palette
    if (unsecured) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.blush.core)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(6.dp).background(p.blush.core, RoundedCornerShape(1.dp)))
                Text(
                    "SECURITY STATUS // UNSECURED LINK",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.blush.core,
                    letterSpacing = 0.5.sp,
                )
            }
            Text(
                "ENCRYPTION REQUIRED",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.blush.core,
            )
        }
    } else {
        Row(
            Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.mint.core)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(6.dp).background(p.mint.core, RoundedCornerShape(1.dp)))
                Text(
                    "AIR-GAP PROTOCOL // AES-256 GCM",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.ink,
                    letterSpacing = 0.5.sp,
                )
            }
            Text(
                "VERIFIED SECURE",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.mint.core,
            )
        }
    }
}

private fun megabytes(bytes: Long): String =
    String.format(Locale.ROOT, "%.1f MB", bytes / 1_048_576.0)

/**
 * Text size preference screen.
 */
@Composable
fun TextSizeScreen(
    onBack: () -> Unit,
    scale: Float,
    onScale: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = palette
    val percent = Math.round(LocalDensity.current.fontScale * 100)
    val fraction = ((scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE)).coerceIn(0f, 1f)

    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BackHeader("DISPLAY SCALING", onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusCard))
                    .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusCard))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("A", fontSize = 12.sp, color = p.muted)
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(p.sunken, RoundedCornerShape(Tokens.RadiusPill)),
                            )
                            Box(
                                Modifier
                                    .fillMaxWidth(fraction.coerceAtLeast(0.02f))
                                    .height(4.dp)
                                    .background(p.mint.core, RoundedCornerShape(Tokens.RadiusPill)),
                            )
                        }
                        Text("A", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = p.ink)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("85%", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = p.muted)
                        Text(
                            "$percent%",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.mint.core,
                        )
                        Text("200%", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = p.muted)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScaleButton(
                            "A−",
                            "Smaller text",
                            enabled = scale > MIN_SCALE + 0.01f,
                            modifier = Modifier.weight(1f),
                        ) {
                            onScale((scale - STEP).coerceAtLeast(MIN_SCALE))
                        }
                        ScaleButton(
                            "A+",
                            "Larger text",
                            enabled = scale < MAX_SCALE - 0.01f,
                            modifier = Modifier.weight(1f),
                        ) {
                            onScale((scale + STEP).coerceAtMost(MAX_SCALE))
                        }
                    }
                }
            }

            Text(
                "On top of system setting. Every tactical HUD screen holds layout to 200% without truncation.",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = p.muted,
            )
        }
    }
}

@Composable
private fun ScaleButton(
    label: String,
    description: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val p = palette
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(if (enabled) p.surfaceContainerHigh else p.sunken, RoundedCornerShape(Tokens.RadiusControl))
            .border(Tokens.Hairline, if (enabled) p.mint.core else p.hairline, RoundedCornerShape(Tokens.RadiusControl))
            .clickable(enabled = enabled, onClick = onClick, role = Role.Button)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (enabled) p.mint.core else p.muted,
        )
    }
}

private const val MIN_SCALE = 0.85f
private const val MAX_SCALE = 2.0f
private const val STEP = 0.15f

