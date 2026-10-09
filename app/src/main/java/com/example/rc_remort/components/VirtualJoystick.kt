package com.example.rc_remort.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rc_remort.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.min

@Composable
fun VirtualJoystick(
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    knobSize: Dp = 76.dp,
    isSteering: Boolean = false,
    isMovement: Boolean = false,
    deadZone: Float = 0.15f,
    onValueChange: (x: Float, y: Float) -> Unit,
    onRelease: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val knobOffsetX = remember { Animatable(0f) }
    val knobOffsetY = remember { Animatable(0f) }

    var isDragging by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // Joystick Header Tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(bottom = 6.dp)
                .background(TechDarkPanel, RoundedCornerShape(4.dp))
                .border(1.dp, TechBorderCyan, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isDragging) NeonCyan else TextDim, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label.uppercase(),
                color = if (isDragging) NeonCyan else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .size(size)
                .shadow(16.dp, CircleShape, spotColor = NeonCyanGlow)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TechDarkSurface,
                            TechDarkBackground
                        )
                    )
                )
                .border(2.dp, if (isDragging) NeonCyan else TechBorder, CircleShape)
                .pointerInput(Unit) {
                    val maxRadius = (size.toPx() - knobSize.toPx()) / 2f

                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val centerX = size.toPx() / 2f
                            val centerY = size.toPx() / 2f
                            val rawDx = offset.x - centerX
                            val rawDy = offset.y - centerY

                            val dx = if (isMovement) 0f else rawDx
                            val dy = if (isSteering) 0f else rawDy

                            val distance = hypot(dx, dy)
                            val clampedDistance = min(distance, maxRadius)
                            val factor = if (distance > 0f) clampedDistance / distance else 0f

                            val clampedX = dx * factor
                            val clampedY = dy * factor

                            coroutineScope.launch {
                                knobOffsetX.snapTo(clampedX)
                                knobOffsetY.snapTo(clampedY)
                            }

                            val normX = if (maxRadius > 0f) clampedX / maxRadius else 0f
                            val normY = if (maxRadius > 0f) clampedY / maxRadius else 0f

                            val filteredX = if (kotlin.math.abs(normX) < deadZone) 0f else normX
                            val filteredY = if (kotlin.math.abs(normY) < deadZone) 0f else normY

                            onValueChange(filteredX, filteredY)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val curX = knobOffsetX.value + (if (isMovement) 0f else dragAmount.x)
                            val curY = knobOffsetY.value + (if (isSteering) 0f else dragAmount.y)

                            val distance = hypot(curX, curY)
                            val clampedDistance = min(distance, maxRadius)
                            val factor = if (distance > 0f) clampedDistance / distance else 0f

                            val clampedX = curX * factor
                            val clampedY = curY * factor

                            coroutineScope.launch {
                                knobOffsetX.snapTo(clampedX)
                                knobOffsetY.snapTo(clampedY)
                            }

                            val normX = if (maxRadius > 0f) clampedX / maxRadius else 0f
                            val normY = if (maxRadius > 0f) clampedY / maxRadius else 0f

                            val filteredX = if (kotlin.math.abs(normX) < deadZone) 0f else normX
                            val filteredY = if (kotlin.math.abs(normY) < deadZone) 0f else normY

                            onValueChange(filteredX, filteredY)
                        },
                        onDragEnd = {
                            isDragging = false
                            coroutineScope.launch {
                                launch {
                                    knobOffsetX.animateTo(
                                        0f,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )
                                }
                                launch {
                                    knobOffsetY.animateTo(
                                        0f,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )
                                }
                            }
                            onRelease()
                        },
                        onDragCancel = {
                            isDragging = false
                            coroutineScope.launch {
                                launch { knobOffsetX.animateTo(0f) }
                                launch { knobOffsetY.animateTo(0f) }
                            }
                            onRelease()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Background Crosshair & Reticle lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                val radius = size.toPx() / 2f

                // Outer guide circle
                drawCircle(
                    color = TechBorder,
                    radius = radius * 0.72f,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Deadzone indicator circle
                drawCircle(
                    color = if (isDragging) NeonCyan.copy(alpha = 0.25f) else TechBorder.copy(alpha = 0.5f),
                    radius = radius * deadZone,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Center crosshair lines
                drawLine(
                    color = TechBorder,
                    start = Offset(center.x - radius * 0.85f, center.y),
                    end = Offset(center.x + radius * 0.85f, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = TechBorder,
                    start = Offset(center.x, center.y - radius * 0.85f),
                    end = Offset(center.x, center.y + radius * 0.85f),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Directional Arrow Indicators
            if (isSteering) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Left",
                        tint = if (knobOffsetX.value < -10f) NeonCyan else TextDim,
                        modifier = Modifier.size(24.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Right",
                        tint = if (knobOffsetX.value > 10f) NeonCyan else TextDim,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isMovement) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Forward",
                        tint = if (knobOffsetY.value < -10f) NeonCyan else TextDim,
                        modifier = Modifier.size(24.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Reverse",
                        tint = if (knobOffsetY.value > 10f) NeonCyan else TextDim,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Movable Center Knob
            Box(
                modifier = Modifier
                    .offset(
                        x = knobOffsetX.value.dp * (1f / androidx.compose.ui.platform.LocalDensity.current.density),
                        y = knobOffsetY.value.dp * (1f / androidx.compose.ui.platform.LocalDensity.current.density)
                    )
                    .size(knobSize)
                    .shadow(12.dp, CircleShape, spotColor = if (isDragging) NeonCyan else Color.Black)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2A3648),
                                Color(0xFF141A24)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = if (isDragging) listOf(NeonCyan, NeonCyanLight) else listOf(TechBorderCyan, TechBorder)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner Grip styling
                Box(
                    modifier = Modifier
                        .size(knobSize * 0.45f)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1520))
                        .border(1.dp, if (isDragging) NeonCyan else TechBorder, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .align(Alignment.Center)
                            .background(if (isDragging) NeonCyan else TextDim, CircleShape)
                    )
                }
            }
        }
    }
}
