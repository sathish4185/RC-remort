package com.example.rc_remort.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rc_remort.ui.theme.*

@Composable
fun SettingsDialog(
    isOpen: Boolean,
    deadZone: Float,
    onDeadZoneChange: (Float) -> Unit,
    vibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    targetMac: String,
    status: String,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TechDarkPanel),
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .fillMaxHeight(0.88f)
                .border(1.5.dp, TechBorderCyan, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F263B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SYSTEM CONFIGURATION",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Controller, Telemetry & Link Parameters",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = TechBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Controller & App Settings
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Controller Settings Group
                        SettingsSectionHeader("CONTROLLER SETTINGS")

                        Card(
                            colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                            modifier = Modifier.border(1.dp, TechBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Joystick Deadzone",
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${(deadZone * 100).toInt()}%",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Slider(
                                    value = deadZone,
                                    onValueChange = onDeadZoneChange,
                                    valueRange = 0.05f..0.35f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonCyan,
                                        activeTrackColor = NeonCyan,
                                        inactiveTrackColor = TechDarkPanel
                                    ),
                                    modifier = Modifier.height(28.dp)
                                )
                                Text(
                                    text = "Filters out micro-jitters around center position.",
                                    color = TextDim,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // App Settings Group
                        SettingsSectionHeader("APPLICATION SETTINGS")

                        Card(
                            colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                            modifier = Modifier.border(1.dp, TechBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Haptic Vibration",
                                            color = TextWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Vibrate on button taps & joystick edge",
                                            color = TextDim,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Switch(
                                        checked = vibrationEnabled,
                                        onCheckedChange = onVibrationToggle,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Black,
                                            checkedTrackColor = NeonCyan,
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = TechDarkPanel
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: Connection & About Group
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsSectionHeader("HARDWARE LINK")

                        Card(
                            colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                            modifier = Modifier.border(1.dp, TechBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                InfoRow("Target MAC", targetMac)
                                InfoRow("Link Status", status)
                                InfoRow("Protocol", "Bluetooth Classic RFCOMM (SPP)")
                                InfoRow("UUID", "00001101-0000-1000-8000-00805F9B34FB")
                            }
                        }

                        SettingsSectionHeader("ABOUT SYSTEM")

                        Card(
                            colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                            modifier = Modifier.border(1.dp, TechBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                InfoRow("Application", "RC COMMANDER")
                                InfoRow("Version", "1.0.0 (Production)")
                                InfoRow("Vehicle Type", "Four-Wheel-Drive (4WD) RC Rover")
                                InfoRow("On-Board Host", "Raspberry Pi 4B (Linux)")
                                InfoRow("Motor Controller", "Arduino Uno R3 + L298N H-Bridge")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = NeonCyan,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = TextWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
