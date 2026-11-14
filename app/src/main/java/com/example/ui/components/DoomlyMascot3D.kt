package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.BrainStatus
import com.example.ui.theme.DoomlyBorder
import com.example.ui.theme.DoomlyCharcoal
import com.example.ui.theme.DoomlyMutedGray
import com.example.ui.theme.DoomlyRed
import com.example.ui.theme.DoomlyRedContainer
import com.example.ui.theme.DoomlySurfaceElevated
import com.example.ui.theme.DoomlyWhite
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * DOOMLY's original 3D-styled Brain Mascot character ("Cortex").
 *
 * - Reacts visually to the user's daily [brainStatus] (calm & happy when Fresh Brain,
 *   progressively more tired and stressed as the status reaches A Bit Bad, Worse, and Bad).
 * - Performs a counter-related pulse & hop animation whenever [scrollCount] increases.
 * - Shows a short playful awareness message when tapped (and invokes [onMascotTapped]).
 */
@Composable
fun DoomlyMascot3D(
    brainStatus: BrainStatus,
    scrollCount: Int?,
    modifier: Modifier = Modifier,
    mascotSize: Dp = 156.dp,
    showSpeechBubbleOnTap: Boolean = true,
    initialSpeechVisible: Boolean = false,
    onMascotTapped: ((String) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    // Tap reaction & counter increment spring animatables
    val tapScale = remember { Animatable(1f) }
    val counterPulse = remember { Animatable(0f) }
    var lastObservedScrollCount by remember { mutableStateOf(scrollCount) }

    var messageIndex by remember(brainStatus) { mutableIntStateOf(0) }
    var speechBubbleVisible by remember { mutableStateOf(initialSpeechVisible) }
    val currentMessage = remember(brainStatus, messageIndex) {
        val messages = brainStatus.mascotTapMessages
        messages[messageIndex % messages.size]
    }

    // Trigger counter-related animation each time the scroll count goes up
    LaunchedEffect(scrollCount) {
        val previous = lastObservedScrollCount
        if (scrollCount != null && previous != null && scrollCount > previous) {
            counterPulse.snapTo(1f)
            launch {
                tapScale.animateTo(
                    targetValue = 1.14f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                )
                tapScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
            }
            launch {
                counterPulse.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                )
            }
        }
        lastObservedScrollCount = scrollCount
    }

    // Continuous subtle 3D floating bob & breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (brainStatus) {
                    BrainStatus.FRESH_BRAIN -> 2200
                    BrainStatus.A_BIT_BAD -> 1800
                    BrainStatus.WORSE -> 1300
                    BrainStatus.BAD -> 950
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    val eyeGlowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600),
            repeatMode = RepeatMode.Restart
        ),
        label = "eye_phase"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(mascotSize)
                .testTag("doomly_mascot_3d")
                .semantics {
                    contentDescription = "DOOMLY 3D Brain Mascot in ${brainStatus.label} state. Tap for a playful awareness message."
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button
                ) {
                    messageIndex = (messageIndex + 1) % brainStatus.mascotTapMessages.size
                    speechBubbleVisible = true
                    onMascotTapped?.invoke(brainStatus.mascotTapMessages[messageIndex])
                    scope.launch {
                        tapScale.snapTo(0.88f)
                        tapScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }
                .graphicsLayer {
                    scaleX = tapScale.value
                    scaleY = tapScale.value
                    translationY = floatOffset
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                draw3DBrainCharacter(
                    brainStatus = brainStatus,
                    counterPulse = counterPulse.value,
                    phase = eyeGlowPhase
                )
            }
        }

        if (showSpeechBubbleOnTap) {
            AnimatedVisibility(
                visible = speechBubbleVisible,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(180))
            ) {
                Surface(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .testTag("mascot_speech_bubble"),
                    color = if (brainStatus == BrainStatus.WORSE || brainStatus == BrainStatus.BAD) {
                        DoomlyRedContainer
                    } else {
                        DoomlySurfaceElevated
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (brainStatus == BrainStatus.WORSE || brainStatus == BrainStatus.BAD) {
                            DoomlyRed.copy(alpha = 0.6f)
                        } else {
                            DoomlyBorder
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DoomlyWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap mascot for another reaction",
                            style = MaterialTheme.typography.labelSmall,
                            color = DoomlyMutedGray
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.draw3DBrainCharacter(
    brainStatus: BrainStatus,
    counterPulse: Float,
    phase: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.48f

    // 1. Counter-increment expandir ring when a new scroll is recorded
    if (counterPulse > 0.01f) {
        val ringRadius = (w * 0.36f) + (w * 0.14f * (1f - counterPulse))
        drawCircle(
            color = DoomlyRed.copy(alpha = counterPulse * 0.55f),
            radius = ringRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 4.dp.toPx())
        )
    }

    // 2. Ground 3D drop shadow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent),
            center = Offset(cx, h * 0.89f),
            radius = w * 0.34f
        ),
        topLeft = Offset(cx - w * 0.32f, h * 0.83f),
        size = Size(w * 0.64f, h * 0.11f)
    )

    // 3. 3D Little Legs / Feet under the brain
    val footColor = Color(0xFF282834)
    drawRoundRect(
        color = footColor,
        topLeft = Offset(cx - w * 0.18f, cy + h * 0.25f),
        size = Size(w * 0.11f, h * 0.10f),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
    )
    drawRoundRect(
        color = footColor,
        topLeft = Offset(cx + w * 0.07f, cy + h * 0.25f),
        size = Size(w * 0.11f, h * 0.10f),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
    )

    // Determine 3D volumetric colors based on BrainStatus mood
    val (highlightColor, midColor, deepShadowColor) = when (brainStatus) {
        BrainStatus.FRESH_BRAIN -> Triple(
            Color(0xFFF3F3F8),
            Color(0xFFB9BAC8),
            Color(0xFF474858)
        )
        BrainStatus.A_BIT_BAD -> Triple(
            Color(0xFFF2D6D8),
            Color(0xFFB88A8E),
            Color(0xFF4E3438)
        )
        BrainStatus.WORSE -> Triple(
            Color(0xFFF49896),
            Color(0xFFC94A48),
            Color(0xFF5A1B1B)
        )
        BrainStatus.BAD -> Triple(
            Color(0xFFFF6663),
            Color(0xFFE53935),
            Color(0xFF6B1110)
        )
    }

    // 4. Volumetric 3D Brain Lobes (layered spheres with radial 3D lighting)
    val lobePositions = listOf(
        Triple(cx - w * 0.19f, cy - h * 0.11f, w * 0.19f),
        Triple(cx + w * 0.19f, cy - h * 0.11f, w * 0.19f),
        Triple(cx - w * 0.23f, cy + h * 0.04f, w * 0.18f),
        Triple(cx + w * 0.23f, cy + h * 0.04f, w * 0.18f),
        Triple(cx - w * 0.13f, cy - h * 0.18f, w * 0.17f),
        Triple(cx + w * 0.13f, cy - h * 0.18f, w * 0.17f),
        Triple(cx - w * 0.14f, cy + h * 0.14f, w * 0.17f),
        Triple(cx + w * 0.14f, cy + h * 0.14f, w * 0.17f),
        Triple(cx, cy - h * 0.02f, w * 0.25f)
    )

    // Base dark ambient occlusion outline
    lobePositions.forEach { (lx, ly, r) ->
        drawCircle(
            color = deepShadowColor,
            radius = r * 1.04f,
            center = Offset(lx, ly + h * 0.015f)
        )
    }

    // Shaded 3D spheres
    lobePositions.forEach { (lx, ly, r) ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(highlightColor, midColor, deepShadowColor),
                center = Offset(lx - r * 0.3f, ly - r * 0.35f),
                radius = r * 1.35f
            ),
            radius = r,
            center = Offset(lx, ly)
        )
    }

    // 5. Central longitudinal fissure & cortical folds (gyri/sulci) for unmistakable 3D brain look
    val sulcusColor = deepShadowColor.copy(alpha = 0.72f)
    val strokeW = (w * 0.024f).coerceAtLeast(3f)

    // Center seam at top
    drawLine(
        color = sulcusColor,
        start = Offset(cx, cy - h * 0.31f),
        end = Offset(cx, cy - h * 0.12f),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )

    // Left hemisphere sulci curves
    val leftSulcus = Path().apply {
        moveTo(cx - w * 0.31f, cy - h * 0.06f)
        quadraticTo(cx - w * 0.20f, cy - h * 0.14f, cx - w * 0.10f, cy - h * 0.09f)
        moveTo(cx - w * 0.33f, cy + h * 0.08f)
        quadraticTo(cx - w * 0.21f, cy + h * 0.02f, cx - w * 0.14f, cy + h * 0.11f)
    }
    drawPath(leftSulcus, color = sulcusColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

    // Right hemisphere sulci curves
    val rightSulcus = Path().apply {
        moveTo(cx + w * 0.31f, cy - h * 0.06f)
        quadraticTo(cx + w * 0.20f, cy - h * 0.14f, cx + w * 0.10f, cy - h * 0.09f)
        moveTo(cx + w * 0.33f, cy + h * 0.08f)
        quadraticTo(cx + w * 0.21f, cy + h * 0.02f, cx + w * 0.14f, cy + h * 0.11f)
    }
    drawPath(rightSulcus, color = sulcusColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

    // 3D Glossy Specular Highlights on top lobes
    drawOval(
        color = Color.White.copy(alpha = 0.38f),
        topLeft = Offset(cx - w * 0.24f, cy - h * 0.27f),
        size = Size(w * 0.11f, h * 0.055f)
    )
    drawOval(
        color = Color.White.copy(alpha = 0.28f),
        topLeft = Offset(cx + w * 0.11f, cy - h * 0.26f),
        size = Size(w * 0.09f, h * 0.045f)
    )

    // 6. Expressive 3D Eyes & Mood Features
    val leftEyeCenter = Offset(cx - w * 0.125f, cy + h * 0.01f)
    val rightEyeCenter = Offset(cx + w * 0.125f, cy + h * 0.01f)
    val eyeRadius = w * 0.078f

    // Eye socket shadow
    drawCircle(color = deepShadowColor, radius = eyeRadius * 1.14f, center = leftEyeCenter)
    drawCircle(color = deepShadowColor, radius = eyeRadius * 1.14f, center = rightEyeCenter)

    // Sclera (white of eye)
    val scleraColor = if (brainStatus == BrainStatus.BAD) Color(0xFFFFE5E5) else Color(0xFFFAFAFC)
    drawCircle(color = scleraColor, radius = eyeRadius, center = leftEyeCenter)
    drawCircle(color = scleraColor, radius = eyeRadius, center = rightEyeCenter)

    when (brainStatus) {
        BrainStatus.FRESH_BRAIN -> {
            // Calm, happy, focused pupils with bright specular catchlights
            val pupilOffset = Offset(sin(phase) * 2f, 0f)
            drawCircle(
                color = Color(0xFF111116),
                radius = eyeRadius * 0.54f,
                center = leftEyeCenter + pupilOffset
            )
            drawCircle(
                color = Color(0xFF111116),
                radius = eyeRadius * 0.54f,
                center = rightEyeCenter + pupilOffset
            )
            // Catchlights
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.22f,
                center = leftEyeCenter + Offset(-eyeRadius * 0.2f, -eyeRadius * 0.22f)
            )
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.22f,
                center = rightEyeCenter + Offset(-eyeRadius * 0.2f, -eyeRadius * 0.22f)
            )

            // Happy confident smile
            val smilePath = Path().apply {
                moveTo(cx - w * 0.08f, cy + h * 0.11f)
                quadraticTo(cx, cy + h * 0.175f, cx + w * 0.08f, cy + h * 0.11f)
            }
            drawPath(
                path = smilePath,
                color = Color(0xFF14141B),
                style = Stroke(width = strokeW * 1.1f, cap = StrokeCap.Round)
            )
        }

        BrainStatus.A_BIT_BAD -> {
            // Slightly dizzy / wide pupils + half-lowered upper eyelids
            drawCircle(
                color = Color(0xFF16161E),
                radius = eyeRadius * 0.46f,
                center = leftEyeCenter + Offset(0f, -eyeRadius * 0.08f)
            )
            drawCircle(
                color = Color(0xFF16161E),
                radius = eyeRadius * 0.46f,
                center = rightEyeCenter + Offset(0f, eyeRadius * 0.08f)
            )
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.18f,
                center = leftEyeCenter + Offset(-eyeRadius * 0.18f, -eyeRadius * 0.2f)
            )
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.18f,
                center = rightEyeCenter + Offset(-eyeRadius * 0.18f, -eyeRadius * 0.2f)
            )

            // Slightly furrowed eyebrows
            drawLine(
                color = Color(0xFF181820),
                start = Offset(leftEyeCenter.x - eyeRadius, leftEyeCenter.y - eyeRadius * 1.15f),
                end = Offset(leftEyeCenter.x + eyeRadius * 0.8f, leftEyeCenter.y - eyeRadius * 0.9f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF181820),
                start = Offset(rightEyeCenter.x - eyeRadius * 0.8f, rightEyeCenter.y - eyeRadius * 1.15f),
                end = Offset(rightEyeCenter.x + eyeRadius, rightEyeCenter.y - eyeRadius * 0.85f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Wavy uncertain mouth
            val wavyMouth = Path().apply {
                moveTo(cx - w * 0.085f, cy + h * 0.13f)
                quadraticTo(cx - w * 0.03f, cy + h * 0.105f, cx + w * 0.01f, cy + h * 0.13f)
                quadraticTo(cx + w * 0.05f, cy + h * 0.155f, cx + w * 0.085f, cy + h * 0.12f)
            }
            drawPath(
                path = wavyMouth,
                color = Color(0xFF181820),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }

        BrainStatus.WORSE -> {
            // Tired heavy eyelids + crimson-ringed stressed pupils
            drawCircle(
                color = DoomlyRed,
                radius = eyeRadius * 0.56f,
                center = leftEyeCenter
            )
            drawCircle(
                color = DoomlyRed,
                radius = eyeRadius * 0.56f,
                center = rightEyeCenter
            )
            drawCircle(
                color = Color(0xFF111116),
                radius = eyeRadius * 0.30f,
                center = leftEyeCenter
            )
            drawCircle(
                color = Color(0xFF111116),
                radius = eyeRadius * 0.30f,
                center = rightEyeCenter
            )

            // Heavy tired upper eyelids
            drawArc(
                color = deepShadowColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(leftEyeCenter.x - eyeRadius, leftEyeCenter.y - eyeRadius),
                size = Size(eyeRadius * 2f, eyeRadius * 1.85f)
            )
            drawArc(
                color = deepShadowColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(rightEyeCenter.x - eyeRadius, rightEyeCenter.y - eyeRadius),
                size = Size(eyeRadius * 2f, eyeRadius * 1.85f)
            )

            // Stressed grimace mouth
            val grimace = Path().apply {
                moveTo(cx - w * 0.09f, cy + h * 0.15f)
                quadraticTo(cx, cy + h * 0.095f, cx + w * 0.09f, cy + h * 0.15f)
            }
            drawPath(
                path = grimace,
                color = Color(0xFF1A0A0B),
                style = Stroke(width = strokeW * 1.15f, cap = StrokeCap.Round)
            )
        }

        BrainStatus.BAD -> {
            // Overloaded dizzy concentric rings in eyes + steam/warning lines above brain
            for (eyeCenter in listOf(leftEyeCenter, rightEyeCenter)) {
                drawCircle(
                    color = DoomlyRed,
                    radius = eyeRadius * 0.68f,
                    center = eyeCenter,
                    style = Stroke(width = strokeW * 0.85f)
                )
                drawCircle(
                    color = Color(0xFF140708),
                    radius = eyeRadius * 0.34f,
                    center = eyeCenter + Offset(cos(phase) * 2.5f, sin(phase) * 2.5f)
                )
            }

            // Sharp stressed V-brows
            drawLine(
                color = Color(0xFF1F0607),
                start = Offset(leftEyeCenter.x - eyeRadius, leftEyeCenter.y - eyeRadius * 1.15f),
                end = Offset(leftEyeCenter.x + eyeRadius * 0.9f, leftEyeCenter.y - eyeRadius * 0.65f),
                strokeWidth = strokeW * 1.2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF1F0607),
                start = Offset(rightEyeCenter.x + eyeRadius, rightEyeCenter.y - eyeRadius * 1.15f),
                end = Offset(rightEyeCenter.x - eyeRadius * 0.9f, rightEyeCenter.y - eyeRadius * 0.65f),
                strokeWidth = strokeW * 1.2f,
                cap = StrokeCap.Round
            )

            // Exhausted open oval mouth
            drawRoundRect(
                color = Color(0xFF1F0607),
                topLeft = Offset(cx - w * 0.065f, cy + h * 0.105f),
                size = Size(w * 0.13f, h * 0.065f),
                cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
            )

            // Overload ticks above head
            drawLine(
                color = DoomlyRed,
                start = Offset(cx - w * 0.16f, cy - h * 0.37f),
                end = Offset(cx - w * 0.21f, cy - h * 0.43f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = DoomlyRed,
                start = Offset(cx, cy - h * 0.38f),
                end = Offset(cx, cy - h * 0.45f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = DoomlyRed,
                start = Offset(cx + w * 0.16f, cy - h * 0.37f),
                end = Offset(cx + w * 0.21f, cy - h * 0.43f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }
}
