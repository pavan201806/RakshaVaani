package org.itantra.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.itantra.app.platform.InstallIndex
import org.itantra.app.platform.ModelDownloader
import org.itantra.app.platform.ModelStore

/**
 * Metadata and current installation status for a language pack.
 */
data class LanguagePackInfo(
    val languageCode: String,
    val englishName: String,
    val nativeName: String,
    val scriptName: String,
    val hasStt: Boolean,
    val hasTts: Boolean,
    val sttBytes: Long,
    val ttsBytes: Long,
    val isSttInstalled: Boolean,
    val isTtsInstalled: Boolean,
) {
    val totalBytes: Long get() = sttBytes + ttsBytes
    val isFullyInstalled: Boolean get() = isSttInstalled && (!hasTts || isTtsInstalled)
}

/**
 * Screen 2: AI Model Setup - Dark Tactical Compact List.
 * - Compact list rows: Language code, name + native name, total size, amber outline install button, live progress, green check when installed.
 * - Header displays "X OF 10 INSTALLED".
 * - Preserves 100% of real ModelDownloader/ModelStore lifecycle.
 */
@Composable
fun ModelSetupScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val p = palette
    val scope = rememberCoroutineScope()

    var diskRevision by remember { mutableIntStateOf(0) }

    val languages = remember(diskRevision) {
        getAvailableLanguagePacks(context)
    }

    val availableStorageBytes = remember(diskRevision) {
        ModelDownloader.getAvailableStorageBytes(context)
    }

    val totalInstalledBytes = remember(diskRevision) {
        val store = ModelStore(context)
        store.installedPacks().sumOf { it.bytes }
    }

    val fullyInstalledCount = languages.count { it.isFullyInstalled }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F0B))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // TOP HEADER: X OF 10 INSTALLED
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131A15), RoundedCornerShape(6.dp))
                .border(Tokens.Hairline, Color(0xFF1E2A21), RoundedCornerShape(6.dp))
                .padding(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(Modifier.size(6.dp).background(Color(0xFF39FF6A), CircleShape))
                        Text(
                            text = "AI SPEECH ENGINE DIRECTORY",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF39FF6A),
                            letterSpacing = 0.5.sp,
                        )
                    }

                    // Header: "X OF 10 INSTALLED"
                    Text(
                        text = "$fullyInstalledCount OF ${languages.size} INSTALLED",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = if (fullyInstalledCount > 0) Color(0xFF39FF6A) else Color(0xFFFFB000),
                        letterSpacing = 0.5.sp,
                    )
                }

                Text(
                    text = "OFFLINE AI MODELS",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE8F5EB),
                    letterSpacing = 0.5.sp,
                )

                Text(
                    text = "ON-DEVICE ASR RECOGNITION & TTS VOICES // AIR-GAPPED",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF6E8A75),
                )
            }
        }

        // STORAGE GAUGE STRIP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131A15), RoundedCornerShape(6.dp))
                .border(Tokens.Hairline, Color(0xFF1E2A21), RoundedCornerShape(6.dp))
                .padding(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "STORAGE ALLOCATION",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6E8A75),
                    )
                    Text(
                        text = "FREE: ${formatBytes(availableStorageBytes)} // USED: ${formatBytes(totalInstalledBytes)}",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE8F5EB),
                    )
                }

                val totalCapacity = (totalInstalledBytes + availableStorageBytes).coerceAtLeast(1L)
                val usedRatio = (totalInstalledBytes.toFloat() / totalCapacity.toFloat()).coerceIn(0.01f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF0A0F0B), RoundedCornerShape(2.dp))
                        .border(Tokens.Hairline, Color(0xFF1E2A21), RoundedCornerShape(2.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(usedRatio)
                            .height(6.dp)
                            .background(Color(0xFF39FF6A), RoundedCornerShape(2.dp)),
                    )
                }
            }
        }

        // COMPACT LIST SECTION HEADER
        Text(
            text = "LANGUAGE PACK CATALOG (${languages.size})",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6E8A75),
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 2.dp, top = 2.dp),
        )

        // COMPACT LIST ROWS
        languages.forEach { pack ->
            CompactLanguagePackRow(
                pack = pack,
                onInstallClick = {
                    scope.launch {
                        val success = ModelDownloader.installLanguage(context, pack.languageCode)
                        if (success) {
                            diskRevision++
                        }
                    }
                },
                onCancelClick = {
                    ModelDownloader.cancelDownload(pack.languageCode)
                },
                onRemoveClick = {
                    ModelDownloader.removeLanguage(context, pack.languageCode)
                    diskRevision++
                },
            )
        }

        Spacer(Modifier.height(4.dp))

        // CONTINUE BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = Tokens.TouchTarget)
                .background(Color(0xFF39FF6A), RoundedCornerShape(6.dp))
                .clickable(onClick = onContinue)
                .semantics(mergeDescendants = true) { contentDescription = "Continue to RakshaVaani" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "CONTINUE TO CONSOLE ›",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0A0F0B),
                letterSpacing = 1.sp,
            )
        }
    }
}

/**
 * Compact Language Pack Row:
 * - Language code badge
 * - Name + Native name with Indic support
 * - Total size
 * - One install button (amber outline #FFB000), live progress state, or green check #39FF6A when installed
 */
@Composable
private fun CompactLanguagePackRow(
    pack: LanguagePackInfo,
    onInstallClick: () -> Unit,
    onCancelClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val p = palette
    val progressState by ModelDownloader.getProgressFlow(pack.languageCode).collectAsState()
    var showRemoveConfirm by remember { mutableStateOf(false) }

    val isDownloading = progressState.stage == ModelDownloader.Stage.DOWNLOADING ||
        progressState.stage == ModelDownloader.Stage.CONNECTING ||
        progressState.stage == ModelDownloader.Stage.CHECKING_STORAGE ||
        progressState.stage == ModelDownloader.Stage.VERIFYING_CHECKSUM ||
        progressState.stage == ModelDownloader.Stage.PREPARING_METADATA

    val isReady = pack.isFullyInstalled && !isDownloading

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131A15), RoundedCornerShape(6.dp))
            .border(
                width = 1.dp,
                color = if (isReady) Color(0xFF1E2A21) else Color(0xFF1E2A21),
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${pack.englishName} (${pack.nativeName}) language pack, ${formatBytes(pack.totalBytes)}. " +
                    if (isReady) "Installed" else if (isDownloading) "Downloading" else "Not installed"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left: Code Badge + Language Name (English + Native) + Total Size
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Code Badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            if (isReady) Color(0x2439FF6A) else Color(0xFF0A0F0B),
                            RoundedCornerShape(4.dp),
                        )
                        .border(
                            1.dp,
                            if (isReady) Color(0xFF39FF6A) else Color(0xFF1E2A21),
                            RoundedCornerShape(4.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = pack.languageCode.uppercase(java.util.Locale.ROOT),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isReady) Color(0xFF39FF6A) else Color(0xFFE8F5EB),
                    )
                }

                // Name + Native Name + Footprint
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = pack.englishName.uppercase(java.util.Locale.ROOT),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE8F5EB),
                        )
                        if (pack.nativeName != pack.englishName) {
                            Text(
                                text = "· ${pack.nativeName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6E8A75),
                            )
                        }
                    }

                    Text(
                        text = "${formatBytes(pack.totalBytes)} // ${if (pack.hasTts) "ASR + TTS" else "ASR ONLY"}",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6E8A75),
                    )
                }
            }

            // Right: Status / Action Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isDownloading) {
                    // Cancel button
                    Box(
                        modifier = Modifier
                            .background(Color(0x24FF3B30), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFFF3B30), RoundedCornerShape(4.dp))
                            .clickable(onClick = onCancelClick)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "CANCEL",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF3B30),
                        )
                    }
                } else if (isReady) {
                    // Green check indicator when installed
                    Row(
                        modifier = Modifier
                            .background(Color(0x2439FF6A), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF39FF6A), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            Icons.Tick,
                            contentDescription = null,
                            tint = Color(0xFF39FF6A),
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "READY",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF39FF6A),
                        )
                    }

                    // Remove action
                    if (showRemoveConfirm) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF0A0F0B), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFF1E2A21), RoundedCornerShape(4.dp))
                                    .clickable { showRemoveConfirm = false }
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                            ) {
                                Text("NO", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF6E8A75))
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFF3B30), RoundedCornerShape(4.dp))
                                    .clickable {
                                        showRemoveConfirm = false
                                        onRemoveClick()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                            ) {
                                Text("DEL", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .sizeIn(minWidth = 32.dp, minHeight = 32.dp)
                                .clickable { showRemoveConfirm = true }
                                .semantics { contentDescription = "Delete ${pack.englishName} pack" },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Bin, contentDescription = null, tint = Color(0xFF6E8A75), modifier = Modifier.size(13.dp))
                        }
                    }
                } else {
                    // One install button with amber outline (#FFB000)
                    Box(
                        modifier = Modifier
                            .background(Color(0x24FFB000), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFFFB000), RoundedCornerShape(4.dp))
                            .clickable(onClick = onInstallClick)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "INSTALL",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB000),
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
            }
        }

        // Live Download Progress State
        if (isDownloading) {
            val animatedFraction by animateFloatAsState(
                targetValue = progressState.totalFraction,
                label = "download_progress",
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0F0B), RoundedCornerShape(4.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = when (progressState.stage) {
                            ModelDownloader.Stage.CHECKING_STORAGE -> "VERIFYING STORAGE..."
                            ModelDownloader.Stage.CONNECTING -> "CONNECTING..."
                            ModelDownloader.Stage.DOWNLOADING -> "DOWNLOADING ${progressState.fileIndex}/${progressState.totalFiles}"
                            ModelDownloader.Stage.VERIFYING_CHECKSUM -> "VERIFYING SHA-256..."
                            ModelDownloader.Stage.PREPARING_METADATA -> "PREPARING METADATA..."
                            else -> "INSTALLING..."
                        },
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFFB000),
                    )
                    Text(
                        text = "${(animatedFraction * 100).toInt()}%",
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB000),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color(0xFF131A15), RoundedCornerShape(2.dp))
                        .border(Tokens.Hairline, Color(0xFF1E2A21), RoundedCornerShape(2.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction.coerceIn(0.02f, 1f))
                            .height(4.dp)
                            .background(Color(0xFFFFB000), RoundedCornerShape(2.dp)),
                    )
                }
            }
        }
    }
}

private fun getAvailableLanguagePacks(context: android.content.Context): List<LanguagePackInfo> {
    val store = ModelStore(context)
    val index = InstallIndex(context)

    val definitions = listOf(
        Triple("en", "English" to "English", "Latin"),
        Triple("hi", "Hindi" to "हिन्दी", "Devanagari"),
        Triple("te", "Telugu" to "తెలుగు", "Telugu"),
        Triple("bn", "Bengali" to "বাংলা", "Bengali"),
        Triple("mr", "Marathi" to "मराठी", "Devanagari"),
        Triple("ta", "Tamil" to "தமிழ்", "Tamil"),
        Triple("gu", "Gujarati" to "ગુજરાતી", "Gujarati"),
        Triple("kn", "Kannada" to "ಕನ್ನಡ", "Kannada"),
        Triple("ml", "Malayalam" to "മലയാളം", "Malayalam"),
        Triple("or", "Odia" to "ଓଡ଼ିଆ", "Odia"),
    )

    return definitions.map { (code, names, script) ->
        val items = index.downloadableFor(code)
        val asrItem = items.firstOrNull { it.kind == "recogniser" }
        val ttsItem = items.firstOrNull { it.kind == "voice" }

        val hasStt = asrItem != null
        val hasTts = ttsItem != null

        val sttBytes = asrItem?.bytes ?: 0L
        val ttsBytes = ttsItem?.bytes ?: 0L

        val isSttInstalled = store.hasPack(code)
        val isTtsInstalled = if (hasTts) store.hasVoice(code) else false

        LanguagePackInfo(
            languageCode = code,
            englishName = names.first,
            nativeName = names.second,
            scriptName = script,
            hasStt = hasStt,
            hasTts = hasTts,
            sttBytes = sttBytes,
            ttsBytes = ttsBytes,
            isSttInstalled = isSttInstalled,
            isTtsInstalled = isTtsInstalled,
        )
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024L * 1024L * 1024L -> "%.1f GB".format(java.util.Locale.ROOT, bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024L * 1024L -> "%.0f MB".format(java.util.Locale.ROOT, bytes / (1024.0 * 1024.0))
        bytes >= 1024L -> "%.0f KB".format(java.util.Locale.ROOT, bytes / 1024.0)
        else -> "$bytes B"
    }
}

private fun formatSpeed(bytesPerSecond: Long): String {
    return when {
        bytesPerSecond >= 1024L * 1024L -> "%.1f MB/s".format(java.util.Locale.ROOT, bytesPerSecond / (1024.0 * 1024.0))
        bytesPerSecond >= 1024L -> "%.0f KB/s".format(java.util.Locale.ROOT, bytesPerSecond / 1024.0)
        else -> "$bytesPerSecond B/s"
    }
}

