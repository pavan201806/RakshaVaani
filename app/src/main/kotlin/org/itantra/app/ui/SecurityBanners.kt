package org.itantra.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// The two warnings that must never be silent. Tasks W6.14 and W6.15.
//
// Neither has a dismiss control, and that is the design rather than an omission. A banner
// an operator can dismiss is a banner an operator will dismiss, and both of these describe
// conditions where the system is doing something other than what its user believes.

/**
 * Task **W6.14**. Shown whenever frames are travelling unencrypted.
 *
 * ## Why there is no way to hide this
 *
 * An operator who believes a channel is encrypted, and speaks accordingly, is in a worse
 * position than one who knows it is not. The banner is red-bordered, permanent and undismissable,
 * and there is **no silent path** to this state — a unit cannot end up unencrypted
 * without the person holding it being told, continuously, for as long as it lasts.
 */
@Composable
fun UnsecuredBanner(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(Color(0xFF131A15), RoundedCornerShape(Tokens.RadiusControl))
            .border(Tokens.Hairline, Color(0xFFFF3B30), RoundedCornerShape(Tokens.RadiusControl))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(Color(0xFFFF3B30), RoundedCornerShape(1.dp)),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "SECURITY ALERT // UNSECURED CHANNEL",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF3B30),
                letterSpacing = 0.5.sp,
            )
            Text(
                "Messages are not encrypted. Anyone in radio range can intercept.",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF6E8A75),
            )
        }
    }
}

/**
 * Task **W6.15**, risk **S-06**. Shown when a peer's template profile digest differs.
 */
@Composable
fun TemplateMismatchBanner(
    peerName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(Color(0xFF131A15), RoundedCornerShape(Tokens.RadiusControl))
            .border(Tokens.Hairline, Color(0xFFFFB000), RoundedCornerShape(Tokens.RadiusControl))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(Color(0xFFFFB000), RoundedCornerShape(1.dp)),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "WARNING // TEMPLATE MISMATCH",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB000),
                letterSpacing = 0.5.sp,
            )
            Text(
                "$peerName alert table differs. Template alerts disabled; speak message directly.",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF6E8A75),
            )
        }
    }
}

/**
 * The banner stack, in severity order.
 */
@Composable
fun SecurityBanners(
    unsecured: Boolean,
    templateMismatchWith: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (unsecured) {
            UnsecuredBanner()
        }
        if (templateMismatchWith != null) {
            TemplateMismatchBanner(templateMismatchWith)
        }
    }
}

