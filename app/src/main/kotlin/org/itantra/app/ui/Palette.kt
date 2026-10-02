package org.itantra.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Dark Tactical Radio Design Palette for RakshaVaani.
 * Strict specification:
 * - Background: #0A0F0B
 * - Surface: #131A15
 * - Border: #1E2A21
 * - Text Primary: #E8F5EB
 * - Text Secondary: #6E8A75
 * - Neon Green: #39FF6A (live / OK / PTT / incoming)
 * - Amber: #FFB000 (user actions / language / warnings)
 * - Red: #FF3B30 (ALERT / SOS / danger only)
 * - Always dark theme, regardless of system setting.
 */
@Immutable
data class ItantraPalette(
    val fieldMode: Boolean,
    val paper: Color,
    val ground: Color,
    val sunken: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    val ink: Color,
    val muted: Color,
    val onAccent: Color,
    val periwinkle: Family,
    val aqua: Family,
    val sky: Family,
    val blush: Family,
    val orchid: Family,
    val mint: Family,
    val apricot: Family,
    val butter: Family,
    val fuchsia: Family,
) {
    @Immutable
    data class Family(
        val tint: Color,
        val mid: Color,
        val track: Color,
        val core: Color,
        val deep: Color,
    )

    companion object {
        val DarkTactical =
            ItantraPalette(
                fieldMode = false,
                paper = Color(0xFF131A15), // Surface #131A15
                ground = Color(0xFF0A0F0B), // Background #0A0F0B
                sunken = Color(0xFF0D140E), // Inset Surface
                hairline = Color(0xFF1E2A21), // Border #1E2A21
                hairlineStrong = Color(0xFF2B3D2F), // Elevated Border
                ink = Color(0xFFE8F5EB), // Text Primary #E8F5EB
                muted = Color(0xFF6E8A75), // Text Secondary #6E8A75
                onAccent = Color(0xFF0A0F0B), // On-accent dark text
                // Primary / Neon Green (#39FF6A)
                periwinkle =
                    Family(
                        tint = Color(0x2439FF6A),
                        mid = Color(0xFF39FF6A),
                        track = Color(0x4039FF6A),
                        core = Color(0xFF39FF6A),
                        deep = Color(0xFF39FF6A),
                    ),
                // Live Link / Incoming / OK Neon Green (#39FF6A)
                aqua =
                    Family(
                        tint = Color(0x2439FF6A),
                        mid = Color(0xFF39FF6A),
                        track = Color(0x4039FF6A),
                        core = Color(0xFF39FF6A),
                        deep = Color(0xFF39FF6A),
                    ),
                // Telemetry / Secondary Monospace
                sky =
                    Family(
                        tint = Color(0x246E8A75),
                        mid = Color(0xFF6E8A75),
                        track = Color(0x406E8A75),
                        core = Color(0xFF6E8A75),
                        deep = Color(0xFFE8F5EB),
                    ),
                // SOS / ALERT / Danger ONLY Red (#FF3B30)
                blush =
                    Family(
                        tint = Color(0x28FF3B30),
                        mid = Color(0xFFFF3B30),
                        track = Color(0x50FF3B30),
                        core = Color(0xFFFF3B30),
                        deep = Color(0xFFFF3B30),
                    ),
                // User actions / Controls Amber (#FFB000)
                orchid =
                    Family(
                        tint = Color(0x24FFB000),
                        mid = Color(0xFFFFB000),
                        track = Color(0x40FFB000),
                        core = Color(0xFFFFB000),
                        deep = Color(0xFFFFB000),
                    ),
                // Nominal / OK Green (#39FF6A)
                mint =
                    Family(
                        tint = Color(0x2439FF6A),
                        mid = Color(0xFF39FF6A),
                        track = Color(0x4039FF6A),
                        core = Color(0xFF39FF6A),
                        deep = Color(0xFF39FF6A),
                    ),
                // Warnings / Language / Actions Amber (#FFB000)
                apricot =
                    Family(
                        tint = Color(0x24FFB000),
                        mid = Color(0xFFFFB000),
                        track = Color(0x40FFB000),
                        core = Color(0xFFFFB000),
                        deep = Color(0xFFFFB000),
                    ),
                // Queued / User Actions Amber (#FFB000)
                butter =
                    Family(
                        tint = Color(0x24FFB000),
                        mid = Color(0xFFFFB000),
                        track = Color(0x40FFB000),
                        core = Color(0xFFFFB000),
                        deep = Color(0xFFFFB000),
                    ),
                // Translation / Language Tag Amber (#FFB000)
                fuchsia =
                    Family(
                        tint = Color(0x24FFB000),
                        mid = Color(0xFFFFB000),
                        track = Color(0x40FFB000),
                        core = Color(0xFFFFB000),
                        deep = Color(0xFFFFB000),
                    ),
            )

        val Spectrum = DarkTactical
        val Field = DarkTactical
    }
}

val LocalPalette = staticCompositionLocalOf { ItantraPalette.DarkTactical }

@Composable
fun ItantraTheme(
    fieldMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPalette provides ItantraPalette.DarkTactical,
    ) {
        content()
    }
}

val palette: ItantraPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalPalette.current

val ItantraPalette.surfaceContainerLowest: Color get() = paper
val ItantraPalette.surfaceContainerLow: Color get() = sunken
val ItantraPalette.surfaceContainer: Color get() = ground
val ItantraPalette.surfaceContainerHigh: Color get() = sunken
val ItantraPalette.surfaceContainerHighest: Color get() = hairline

