package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.example.ui.JarvisState
import com.example.ui.theme.JarvisAccentPurple
import com.example.ui.theme.JarvisPrimaryCyan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Class representing floating sci-fi neon particles
data class Particle(
    var x: Float,
    var y: Float,
    val speedX: Float,
    val speedY: Float,
    val size: Float,
    val color: Color,
    var alpha: Float
)

@Composable
fun JarvisOrb(
    state: JarvisState,
    isLiteMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_anim")

    // Slow continuous Rotation for idle holographic rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Fast counter-clockwise rotation
    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    // Breathing pulse for neon rings
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Voice modulation wave scale used when J.A.R.V.I.S. speaks
    val speakWavelength by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice_waveform"
    )

    // Listening wave scale used when microphone is listening
    val listeningPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listening_pulse"
    )

    // Floating micro-particles container
    val particles = remember { mutableStateListOf<Particle>() }
    
    // Spawn particles initially
    LaunchedEffect(key1 = isLiteMode) {
        if (!isLiteMode && particles.isEmpty()) {
            for (i in 0..15) {
                particles.add(
                    Particle(
                        x = Random.nextFloat() * 400f,
                        y = Random.nextFloat() * 400f,
                        speedX = (Random.nextFloat() - 0.5f) * 1.5f,
                        speedY = (Random.nextFloat() - 0.5f) * 1.5f,
                        size = Random.nextFloat() * 4f + 2f,
                        color = if (Random.nextBoolean()) JarvisPrimaryCyan else JarvisAccentPurple,
                        alpha = Random.nextFloat() * 0.6f + 0.2f
                    )
                )
            }
        } else if (isLiteMode) {
            particles.clear()
        }
    }

    // Tick/animate particles in frame
    if (!isLiteMode) {
        LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(16) // ~60 FPS
                for (p in particles) {
                    p.x += p.speedX
                    p.y += p.speedY
                    p.alpha -= 0.002f
                    if (p.alpha <= 0f || p.x < 0 || p.x > 500f || p.y < 0 || p.y > 500f) {
                        // Reset particle
                        p.x = 200f + (Random.nextFloat() - 0.5f) * 60f
                        p.y = 200f + (Random.nextFloat() - 0.5f) * 60f
                        p.alpha = Random.nextFloat() * 0.7f + 0.3f
                    }
                }
            }
        }
    }

    val finalOrbColor = when (state) {
        JarvisState.IDLE -> JarvisPrimaryCyan
        JarvisState.LISTENING -> JarvisPrimaryCyan
        JarvisState.PROCESSING -> JarvisAccentPurple
        JarvisState.SPEAKING -> JarvisPrimaryCyan
    }

    val finalOuterColor = when (state) {
        JarvisState.IDLE -> JarvisAccentPurple
        JarvisState.LISTENING -> JarvisAccentPurple
        JarvisState.PROCESSING -> JarvisPrimaryCyan
        JarvisState.SPEAKING -> JarvisAccentPurple
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .size(240.dp)
            .focusable(interactionSource = interactionSource)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) JarvisPrimaryCyan else Color.Transparent,
                shape = CircleShape
            )
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width / 4.2f

            // Adjust scale metrics based on J.A.R.V.I.S. active processing states
            val activeScale = when (state) {
                JarvisState.IDLE -> pulseScale
                JarvisState.LISTENING -> listeningPulse
                JarvisState.PROCESSING -> pulseScale * 1.05f
                JarvisState.SPEAKING -> speakWavelength
            }

            // Draw particles behind the rings
            if (!isLiteMode) {
                particles.forEach { p ->
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha),
                        radius = p.size,
                        center = Offset(p.x, p.y)
                    )
                }
            }

            // Outer Orbit Glow aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(finalOrbColor.copy(alpha = 0.25f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 2.2f * activeScale
                ),
                radius = baseRadius * 2.2f * activeScale,
                center = center
            )

            // Inner Orb Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(finalOrbColor.copy(alpha = 0.55f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 0.9f * activeScale
                ),
                radius = baseRadius * 0.9f * activeScale,
                center = center
            )

            // 1. Core reactor glowing center sphere
            drawCircle(
                color = finalOrbColor.copy(alpha = 0.85f),
                radius = baseRadius * 0.42f * (if (state == JarvisState.SPEAKING) speakWavelength else pulseScale),
                center = center
            )

            // 2. Rotary Ring 1: Cyan holographic dotted sweep (Clockwise)
            withTransform({
                rotate(if (state == JarvisState.PROCESSING) rotationAngle * 3f else rotationAngle, center)
            }) {
                drawCircle(
                    color = finalOrbColor,
                    radius = baseRadius * 0.85f * activeScale,
                    center = center,
                    style = Stroke(
                        width = 4f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(15f, 25f), 0f
                        )
                    )
                )
            }

            // 3. Rotary Ring 2: Purple outer solid ring (Counter-clockwise)
            withTransform({
                rotate(if (state == JarvisState.PROCESSING) counterRotationAngle * 3.5f else counterRotationAngle, center)
            }) {
                drawCircle(
                    color = finalOuterColor,
                    radius = baseRadius * 1.15f * activeScale,
                    center = center,
                    style = Stroke(
                        width = 1.8f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(60f, 40f, 15f, 40f), 0f
                        )
                    )
                )
            }

            // 4. Multi-frequency ring 3 (Outer fine alignment scope)
            if (!isLiteMode) {
                withTransform({
                    rotate(rotationAngle * 0.4f, center)
                }) {
                    drawCircle(
                        color = JarvisPrimaryCyan.copy(alpha = 0.4f),
                        radius = baseRadius * 1.45f * activeScale,
                        center = center,
                        style = Stroke(
                            width = 1f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(80f, 20f, 3f, 20f), 0f
                            )
                        )
                    )
                }

                // Cross-hair scopes representing high-tech vision target lines
                drawLine(
                    color = JarvisPrimaryCyan.copy(alpha = 0.35f),
                    start = Offset(center.x - baseRadius * 1.6f, center.y),
                    end = Offset(center.x - baseRadius * 1.2f, center.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = JarvisPrimaryCyan.copy(alpha = 0.35f),
                    start = Offset(center.x + baseRadius * 1.2f, center.y),
                    end = Offset(center.x + baseRadius * 1.6f, center.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = JarvisPrimaryCyan.copy(alpha = 0.35f),
                    start = Offset(center.x, center.y - baseRadius * 1.6f),
                    end = Offset(center.x, center.y - baseRadius * 1.2f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = JarvisPrimaryCyan.copy(alpha = 0.35f),
                    start = Offset(center.x, center.y + baseRadius * 1.2f),
                    end = Offset(center.x, center.y + baseRadius * 1.6f),
                    strokeWidth = 2f
                )
            }
        }
    }
}
