package com.example.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BrainStatus
import com.example.data.PlatformTrackingStatus
import com.example.data.PlatformType
import com.example.ui.theme.DoomlyBlack
import com.example.ui.theme.DoomlyBorder
import com.example.ui.theme.DoomlyCharcoal
import com.example.ui.theme.DoomlyMutedGray
import com.example.ui.theme.DoomlyRed
import com.example.ui.theme.DoomlyRedBorder
import com.example.ui.theme.DoomlyRedContainer
import com.example.ui.theme.DoomlySubtleGray
import com.example.ui.theme.DoomlySurfaceElevated
import com.example.ui.theme.DoomlyWhite

/**
 * Clean, original DOOMLY geometric logo emblem (dark charcoal container with crimson scroll-brain mark).
 */
@Composable
fun DoomlyLogoMark(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(size * 0.26f),
        color = DoomlyCharcoal,
        border = BorderStroke(1.5.dp, DoomlyRed.copy(alpha = 0.85f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.18f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val stroke = (w * 0.095f).coerceAtLeast(3f)

            // Left brain hemisphere arc
            drawArc(
                color = DoomlyWhite,
                startAngle = 95f,
                sweepAngle = 170f,
                useCenter = false,
                topLeft = Offset(w * 0.06f, h * 0.10f),
                size = Size(w * 0.52f, h * 0.80f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Right brain hemisphere arc
            drawArc(
                color = DoomlyWhite,
                startAngle = 275f,
                sweepAngle = 170f,
                useCenter = false,
                topLeft = Offset(w * 0.42f, h * 0.10f),
                size = Size(w * 0.52f, h * 0.80f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Central crimson vertical scroll arrow channel
            drawLine(
                color = DoomlyRed,
                start = Offset(cx, h * 0.16f),
                end = Offset(cx, h * 0.84f),
                strokeWidth = stroke * 1.15f,
                cap = StrokeCap.Round
            )

            // Up chevron
            val upChevron = Path().apply {
                moveTo(cx - w * 0.14f, h * 0.28f)
                lineTo(cx, h * 0.12f)
                lineTo(cx + w * 0.14f, h * 0.28f)
            }
            drawPath(upChevron, color = DoomlyRed, style = Stroke(width = stroke, cap = StrokeCap.Round))

            // Down chevron
            val downChevron = Path().apply {
                moveTo(cx - w * 0.14f, h * 0.72f)
                lineTo(cx, h * 0.88f)
                lineTo(cx + w * 0.14f, h * 0.72f)
            }
            drawPath(downChevron, color = DoomlyRed, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
    }
}

/**
 * Top bar for secondary screens with 48dp accessible back button.
 */
@Composable
fun DoomlyTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    trailingAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("top_bar_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate back",
                tint = DoomlyWhite
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = DoomlyWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = DoomlyMutedGray
                )
            }
        }
        if (trailingAction != null) {
            trailingAction()
        }
    }
}

/**
 * Mandatory "Estimated scrolls" label paired with an interactive (i) info icon explaining
 * that Android cannot count exact Reels/Shorts and the scroll count is an estimate that may
 * vary slightly or change when Instagram, YouTube, or TikTok update.
 */
@Composable
fun EstimatedScrollsLabelWithInfo(
    modifier: Modifier = Modifier,
    labelText: String = stringResource(R.string.estimated_scrolls_label),
    textColor: Color = DoomlyMutedGray
) {
    var showExplanationDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = labelText,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
        IconButton(
            onClick = { showExplanationDialog = true },
            modifier = Modifier
                .size(48.dp)
                .testTag("estimated_scrolls_info_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "About estimated scrolls accuracy",
                tint = DoomlyMutedGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            containerColor = DoomlyCharcoal,
            titleContentColor = DoomlyWhite,
            textContentColor = DoomlyMutedGray,
            title = {
                Text(
                    text = "Why 'Estimated scrolls'?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.scroll_estimate_explanation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DoomlyWhite
                    )
                    Text(
                        text = "Privacy guarantee: DOOMLY never reads, stores, or transmits screen text, usernames, comments, or messages. Only swipe counts and timestamps are saved locally on your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DoomlyMutedGray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showExplanationDialog = false },
                    modifier = Modifier.testTag("close_estimate_info_dialog")
                ) {
                    Text("Got it", color = DoomlyRed)
                }
            }
        )
    }
}

/**
 * Animated count-up number for estimated scrolls, or honest "Tracking unavailable" state
 * with an action button when Accessibility permission is off.
 */
@Composable
fun AnimatedEstimatedScrollsHero(
    estimatedScrolls: Int?,
    isAccessibilityEnabled: Boolean,
    isMasterScrollTrackingEnabled: Boolean,
    onEnableTrackingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EstimatedScrollsLabelWithInfo()

        if (estimatedScrolls != null && isAccessibilityEnabled && isMasterScrollTrackingEnabled) {
            val animatedValue by animateIntAsState(
                targetValue = estimatedScrolls,
                animationSpec = tween(durationMillis = 450),
                label = "hero_scroll_counter"
            )
            Text(
                text = animatedValue.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = DoomlyRed,
                modifier = Modifier.testTag("hero_estimated_scrolls_value")
            )
        } else {
            Surface(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .testTag("hero_tracking_unavailable_card"),
                color = DoomlySurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DoomlyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.tracking_unavailable),
                        style = MaterialTheme.typography.headlineSmall,
                        color = DoomlyWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (!isAccessibilityEnabled) {
                            "Optional Accessibility scroll estimation is currently off. App usage time still works."
                        } else {
                            "Scroll tracking is paused in DOOMLY settings."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = DoomlyMutedGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onEnableTrackingClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DoomlyRed,
                            contentColor = DoomlyWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("enable_scroll_tracking_button")
                    ) {
                        Text(
                            text = if (!isAccessibilityEnabled) "Enable Scroll Estimation" else "Turn On Scroll Tracking",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status pill badge showing Not installed, Permission missing, Scroll tracking off, or Active.
 */
@Composable
fun TrackingStatusBadge(
    status: PlatformTrackingStatus,
    modifier: Modifier = Modifier
) {
    val isActive = status == PlatformTrackingStatus.ACTIVE
    val isWarning = status == PlatformTrackingStatus.PERMISSION_MISSING ||
        status == PlatformTrackingStatus.SCROLL_TRACKING_OFF

    val containerColor = when {
        isActive -> DoomlyRedContainer
        isWarning -> DoomlySurfaceElevated
        else -> DoomlyBlack
    }
    val borderColor = when {
        isActive -> DoomlyRedBorder
        else -> DoomlyBorder
    }
    val dotColor = when {
        isActive -> DoomlyRed
        isWarning -> DoomlyWhite
        else -> DoomlySubtleGray
    }
    val labelColor = when {
        isActive -> DoomlyWhite
        isWarning -> DoomlyWhite
        else -> DoomlyMutedGray
    }

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
        }
    }
}

/**
 * Playful Brain Status pill with non-medical styling.
 */
@Composable
fun BrainStatusPill(
    brainStatus: BrainStatus,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isElevatedWarning = brainStatus == BrainStatus.WORSE || brainStatus == BrainStatus.BAD
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .testTag("brain_status_pill"),
        color = if (isElevatedWarning) DoomlyRedContainer else DoomlySurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isElevatedWarning) DoomlyRed else DoomlyBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isElevatedWarning) DoomlyRed else DoomlyWhite)
            )
            Text(
                text = "Brain Status: ${brainStatus.label}",
                style = MaterialTheme.typography.titleSmall,
                color = DoomlyWhite
            )
            Text(
                text = "(${brainStatus.rangeText})",
                style = MaterialTheme.typography.labelSmall,
                color = DoomlyMutedGray
            )
        }
    }
}

/**
 * Data item for [DoomlyBarChart].
 */
data class BarChartEntry(
    val label: String,
    val value: Float,
    val displayValue: String,
    val isHighlighted: Boolean = false
)

/**
 * Clean, custom bar chart with labels and honest empty state.
 */
@Composable
fun DoomlyBarChart(
    entries: List<BarChartEntry>,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 160.dp,
    showValuesAboveBars: Boolean = true
) {
    val maxVal = remember(entries) { (entries.maxOfOrNull { it.value } ?: 0f).coerceAtLeast(1f) }
    val allZero = remember(entries) { entries.isEmpty() || entries.all { it.value <= 0f } }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DoomlyCharcoal,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DoomlyBorder)
    ) {
        if (allZero) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DoomlyMutedGray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    entries.forEach { entry ->
                        val fraction = (entry.value / maxVal).coerceIn(0.04f, 1f)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            if (showValuesAboveBars && entries.size <= 10) {
                                Text(
                                    text = entry.displayValue,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (entry.isHighlighted) DoomlyRed else DoomlyWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (entries.size > 14) 0.78f else 0.62f)
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        when {
                                            entry.value <= 0f -> DoomlyBorder
                                            entry.isHighlighted -> DoomlyRed
                                            else -> DoomlyWhite.copy(alpha = 0.85f)
                                        }
                                    )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val labelStep = if (entries.size > 12) (entries.size / 6).coerceAtLeast(1) else 1
                    entries.forEachIndexed { idx, entry ->
                        val showLabel = idx % labelStep == 0 || idx == entries.lastIndex
                        Text(
                            text = if (showLabel) entry.label else "",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = DoomlyMutedGray,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formats foreground usage milliseconds into an honest, readable string.
 */
fun formatUsageDuration(durationMs: Long?): String {
    if (durationMs == null) return "Permission missing"
    if (durationMs <= 0L) return "0m"
    val totalSeconds = durationMs / 1000L
    if (totalSeconds in 1..59) return "< 1m"
    val totalMinutes = totalSeconds / 60L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

/**
 * Clean non-medical Brain Status disclaimer card required across awareness views.
 */
@Composable
fun BrainStatusDisclaimerCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DoomlySurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DoomlyBorder)
    ) {
        Text(
            text = stringResource(R.string.brain_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = DoomlyMutedGray,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            textAlign = TextAlign.Center
        )
    }
}
