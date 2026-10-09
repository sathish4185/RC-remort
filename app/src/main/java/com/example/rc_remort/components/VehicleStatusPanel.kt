package com.example.rc_remort.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rc_remort.ui.theme.*

@Composable
fun VehicleStatusPanel(
    status: String,
    movementState: String,
    lastCommand: String,
    speedPercent: Float,
    onSpeedChange: (Float) -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = status == "Connected"
    val isConnecting = status.startsWith("Connecting")
    val linkColor = when {
        isConnected -> StatusGreen
        isConnecting -> StatusYellow
        else -> StatusRed
    }

    Column(
        modifier = modifier
            .widthIn(min = 240.dp, max = 300.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper HUD Card
        Card(
            colors = CardDefaults.cardColors(containerColor = TechDarkPanel),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TechBorderCyan, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Header with Vehicle Name & Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F1E33)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.rc_remort.R.drawable.app_logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VEHICLE: ROVER-01",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Mode Badge
                    Box(
                        modifier = Modifier
                            .background(TechDarkSurface, RoundedCornerShape(4.dp))
                            .border(1.dp, TechBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MANUAL",
                            color = NeonCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = TechBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(6.dp))

                // Grid of Telemetry Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Link Status Column
                    Column {
                        Text(
                            text = "LINK",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(linkColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isConnected) "CONNECTED" else if (isConnecting) "WAIT..." else "OFFLINE",
                                color = linkColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Movement State Column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MOVEMENT",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = movementState.uppercase(),
                            color = if (movementState == "Stopped" || movementState == "Neutral") TextWhite else NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Last Command Column
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LAST CMD",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = lastCommand.take(8),
                            color = if (lastCommand.startsWith("Send failed")) StatusRed else NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Speed Throttle Selector Card
        Card(
            colors = CardDefaults.cardColors(containerColor = TechDarkPanel),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(1.dp, TechBorder, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "THROTTLE LIMIT",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "${speedPercent.toInt()}% ${if (speedPercent >= 100f) "(TURBO)" else ""}",
                        color = if (speedPercent >= 100f) StatusRed else NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = speedPercent,
                    onValueChange = onSpeedChange,
                    valueRange = 20f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (speedPercent >= 100f) StatusRed else NeonCyan,
                        activeTrackColor = if (speedPercent >= 100f) StatusRed else NeonCyan,
                        inactiveTrackColor = Color(0xFF0F1E33)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                )

                // Quick Speed Preset Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val presets = listOf(30f, 50f, 75f, 100f)
                    presets.forEach { preset ->
                        val isSelected = (speedPercent - preset).let { kotlin.math.abs(it) < 4f }
                        val isTurbo = preset == 100f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) {
                                        if (isTurbo) StatusRed else NeonCyan
                                    } else Color(0xFF0C131D)
                                )
                                .border(
                                    1.dp,
                                    if (isTurbo) StatusRed.copy(alpha = 0.6f) else TechBorder,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isTurbo) "TURBO" else "${preset.toInt()}%",
                                color = if (isSelected) Color.Black else (if (isTurbo) StatusRed else TextMuted),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Emergency STOP Button at the lower center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            EmergencyStopButton(
                onClick = onEmergencyStop,
                size = 62.dp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "EMERGENCY BRAKE",
                color = TextDim,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}
