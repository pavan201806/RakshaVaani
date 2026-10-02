package org.itantra.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Tactical Radio Log-Style Message Row.
 * - Log-style flat lines, not chat bubbles.
 * - Incoming: Solid Green #39FF6A left edge accent.
 * - Outgoing: Solid Amber #FFB000 right edge accent.
 * - Monospace metadata line (RX/TX, unit, timestamp, status).
 * - Clean sans-serif typography for speech message text with full Indic line-height support.
 */
@Composable
internal fun MessageBubble(
    message: LoggedMessage,
    /** True while this handset is saying this message out loud. */
    speaking: Boolean = false,
    /** The log shows a clock time; the operating thread shows an age. */
    stamp: String = message.age,
    onReplay: (String) -> Unit,
) {
    if (message.isAlert) {
        AlertMessageCard(message, stamp)
        return
    }

    val p = palette
    val mine = message.delivery != LoggedMessage.Delivery.RECEIVED
    val queued = message.delivery == LoggedMessage.Delivery.PENDING
    val failed = message.delivery == LoggedMessage.Delivery.FAILED
    val accentColor = if (mine) Color(0xFFFFB000) else Color(0xFF39FF6A)
    val shape = RoundedCornerShape(Tokens.RadiusCard)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (queued || failed) Modifier.alpha(0.72f) else Modifier)
            .background(p.paper, shape)
            .border(Tokens.Hairline, if (speaking) Color(0xFF39FF6A) else p.hairline, shape)
            .semantics(mergeDescendants = true) { contentDescription = Spoken.messageRow(message) },
    ) {
        // Left accent bar for incoming, right accent bar for outgoing
        if (!mine) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .matchParentSize()
                    .background(Color(0xFF39FF6A), RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)),
            )
        } else {
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .width(3.dp)
                    .matchParentSize()
                    .background(Color(0xFFFFB000), RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (!mine) 12.dp else 10.dp,
                    end = if (mine) 12.dp else 10.dp,
                    top = 8.dp,
                    bottom = 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                // Tiny Meta Line: RX/TX, Unit, Time, Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // RX / TX Tag
                    Text(
                        text = if (mine) "TX" else "RX",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 0.5.sp,
                    )

                    Text(
                        text = "·",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )

                    // Sender Unit
                    Text(
                        text = (if (mine) "YOU" else message.from).uppercase(Locale.ROOT),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.ink,
                    )

                    Text(
                        text = "·",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )

                    // Timestamp
                    Text(
                        text = stamp,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )

                    // Payload Bytes & Status
                    Text(
                        text = "· ${message.frameBytes}B",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )

                    if (message.wasTemplate && !mine) {
                        Text(
                            text = "TEMPLATE",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB000),
                            modifier = Modifier
                                .background(Color(0x24FFB000), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }

                    if (mine) {
                        DeliveryMark(message.delivery)
                    } else {
                        message.confidence?.let { ConfidenceRun(it, Color(0xFF39FF6A)) }
                    }

                    if (speaking) {
                        Icon(
                            Icons.Speaking,
                            contentDescription = null,
                            tint = Color(0xFF39FF6A),
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }

                // Message Text in Clean Sans-Serif with full Indic line-height
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    color = p.ink,
                    lineHeight = 20.sp,
                )
            }

            // Replay control for received messages
            if (!mine && !queued) {
                Spacer(Modifier.width(8.dp))
                ReplayDisc(p.periwinkle) { onReplay(message.text) }
            }
        }
    }
}

/**
 * Delivery indicator glyph and label for tactical log line.
 */
@Composable
private fun DeliveryMark(delivery: LoggedMessage.Delivery) {
    val p = palette
    when (delivery) {
        LoggedMessage.Delivery.DELIVERED ->
            Icon(Icons.TickDouble, contentDescription = null, tint = Color(0xFF39FF6A), modifier = Modifier.size(13.dp))
        LoggedMessage.Delivery.SENT ->
            Icon(Icons.Tick, contentDescription = null, tint = p.muted, modifier = Modifier.size(13.dp))
        LoggedMessage.Delivery.FAILED ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(Icons.Cross, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(12.dp))
                Text(
                    "FAILED",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF3B30),
                )
            }
        LoggedMessage.Delivery.PENDING ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Box(Modifier.size(6.dp).border(1.dp, Color(0xFFFFB000), CircleShape))
                Text(
                    "QUEUED",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB000),
                )
            }
        LoggedMessage.Delivery.REFUSED ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(Icons.Cross, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(12.dp))
                Text(
                    "REFUSED",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF3B30),
                )
            }
        LoggedMessage.Delivery.RECEIVED -> Unit
    }
}

/**
 * Alert log card in dark tactical style with Red #FF3B30 highlight.
 */
@Composable
private fun AlertMessageCard(
    message: LoggedMessage,
    stamp: String,
) {
    val p = palette
    val shape = RoundedCornerShape(Tokens.RadiusCard)

    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF131A15), shape)
            .border(1.dp, Color(0xFFFF3B30), shape)
            .semantics(mergeDescendants = true) { contentDescription = Spoken.messageRow(message) },
    ) {
        // Red Top Strip
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0x30FF3B30))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Alert, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(14.dp))
            Text(
                "ALERT SOS // ${message.from.uppercase(Locale.ROOT)}",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF3B30),
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                stamp,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF3B30),
            )
        }

        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = message.text,
                fontSize = 15.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                lineHeight = 21.sp,
                color = p.ink,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "${message.frameBytes}B" + if (message.wasTemplate) " TEMPLATE" else "",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFB000),
                )
                DeliveryMark(message.delivery)
                message.sentInLanguage?.let {
                    Text(
                        "· sent in $it",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )
                }
            }
        }
    }
}

/** Four 5dp discs for recogniser confidence. */
@Composable
private fun ConfidenceRun(
    level: Int,
    colour: Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.decorative()) {
        repeat(4) { index ->
            Box(
                Modifier
                    .size(5.dp)
                    .then(
                        if (index < level) {
                            Modifier.background(colour, CircleShape)
                        } else {
                            Modifier.border(0.8.dp, colour, CircleShape)
                        },
                    ),
            )
        }
    }
}

/** Tactical Replay Button with >=48dp touch target. */
@Composable
private fun ReplayDisc(
    family: ItantraPalette.Family,
    onClick: () -> Unit,
) {
    val p = palette
    Box(
        Modifier
            .sizeIn(minWidth = Tokens.TouchTarget, minHeight = Tokens.TouchTarget)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = "Replay message" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .background(p.surfaceContainerLow, RoundedCornerShape(4.dp))
                .border(Tokens.Hairline, p.hairline, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Replay, contentDescription = null, tint = p.muted, modifier = Modifier.size(16.dp))
        }
    }
}

/** Tactical Day Divider for message log. */
@Composable
internal fun DayDivider(label: String) {
    val p = palette
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "— $label —",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = p.muted,
            letterSpacing = 1.sp,
        )
    }
}

/** Dashed border helper. */
internal fun Modifier.dashedEdge(
    colour: Color,
    radius: androidx.compose.ui.unit.Dp,
): Modifier =
    drawBehind {
        val stroke = Tokens.SignalBorder.toPx()
        val r = radius.toPx()
        drawRoundRect(
            color = colour,
            cornerRadius = CornerRadius(r, r),
            style =
                Stroke(
                    width = stroke,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 3, stroke * 2), 0f),
                ),
        )
    }

