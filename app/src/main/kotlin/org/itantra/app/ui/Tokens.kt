package org.itantra.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dark Tactical Radio Design Tokens for RakshaVaani.
 * - Flat surfaces, 1dp borders, small radius (4-10dp), no shadows or gradients
 * - Monospace for labels/metadata, clean sans for message content
 * - Neon green #39FF6A, Amber #FFB000, Red #FF3B30
 */
object Tokens {
    // ── Dark Tactical Ground Plane ───────────────────────────────────────────

    val Ground = Color(0xFF0A0F0B)       // Background #0A0F0B
    val Surface = Color(0xFF131A15)      // Surface #131A15
    val Border = Color(0xFF1E2A21)       // Border #1E2A21
    val BorderElevated = Color(0xFF2B3D2F)

    val Ink = Color(0xFFE8F5EB)          // Text Primary #E8F5EB
    val Paper = Color(0xFF131A15)        // Surface #131A15
    val Muted = Color(0xFF6E8A75)        // Text Secondary #6E8A75
    val Rule = Color(0xFF1E2A21)         // Border #1E2A21
    val InkPaper = Color(0xFFE8F5EB)

    // ── State Signals ────────────────────────────────────────────────────────

    val Ok = Color(0xFF39FF6A)           // Neon Green #39FF6A (Live/OK/PTT/Incoming)
    val Warn = Color(0xFFFFB000)         // Amber #FFB000 (User actions, language, warnings)
    val Alert = Color(0xFFFF3B30)        // Red #FF3B30 (ALERT/SOS/Danger ONLY)
    val AlertField = Color(0x30FF3B30)   // Dark Red Container
    val Cyan = Color(0xFF39FF6A)         // Neon Green
    val Blue = Color(0xFF39FF6A)         // Green

    // ── Layout Metrics ───────────────────────────────────────────────────────

    val Grid: Dp = 8.dp
    val ScreenMargin: Dp = 12.dp
    val TouchTarget: Dp = 48.dp
    val SecondaryAction: Dp = 56.dp
    val StatusBand: Dp = 52.dp
    val ModeBand: Dp = 44.dp
    val InstrumentBand: Dp = 38.dp

    const val TRANSMIT_FRACTION = 0.33f

    // ── Typography Scale ─────────────────────────────────────────────────────

    val Title: TextUnit = 20.sp
    val Body: TextUnit = 15.sp
    val Status: TextUnit = 13.sp
    val Instrument: TextUnit = 11.sp
    val Icon: TextUnit = 24.sp

    const val INDIC_LINE_HEIGHT = 1.4f

    val Display: TextUnit = 32.sp
    val Headline: TextUnit = 24.sp
    val Figure: TextUnit = 20.sp
    val Subtitle: TextUnit = 17.sp
    val Callout: TextUnit = 15.sp
    val BodySmall: TextUnit = 13.sp
    val Label: TextUnit = 11.sp
    val Caption: TextUnit = 9.sp

    // ── Shape Metrics (Small Tactical Radius: 4-10dp) ────────────────────────

    val RadiusPill: Dp = 4.dp
    val RadiusDock: Dp = 8.dp
    val RadiusCard: Dp = 6.dp
    val RadiusTile: Dp = 6.dp
    val RadiusControl: Dp = 6.dp
    val RadiusInset: Dp = 4.dp

    val FlatShape: Shape = RoundedCornerShape(6.dp)
    val CardShape: Shape = RoundedCornerShape(6.dp)
    val PillShape: Shape = RoundedCornerShape(4.dp)
    val ZeroShape: Shape = RoundedCornerShape(0.dp)

    val Hairline: Dp = 1.dp
    val SignalBorder: Dp = 1.dp
    val AlertBorder: Dp = 1.dp

    // ── Dock / PTT Array ─────────────────────────────────────────────────────

    val TransmitCircle: Dp = 112.dp      // Large circular PTT button ~112dp
    val DockFlank: Dp = 48.dp

    // ── Motion Constants ─────────────────────────────────────────────────────

    const val HALO_MILLIS = 1_600
    const val EQ_MILLIS = 720
    const val EQ_STAGGER_MILLIS = 90
    const val EQ_BARS = 7
    const val PULSE_MILLIS = 2_000
    const val ARC_MILLIS = 1_400
    const val SHIMMER_MILLIS = 1_600
    const val BREATHE_MILLIS = 2_600
    const val TRANSITION_MILLIS = 250
}

