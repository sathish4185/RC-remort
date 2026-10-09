package com.example.rc_remort

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.rc_remort.components.*
import com.example.rc_remort.ui.theme.RCRemortTheme
import com.example.rc_remort.ui.theme.TechDarkBackground
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.Executors

private const val DEFAULT_PI_ADDRESS = "E4:5F:01:BB:F8:B4"

// Standard Serial Port Profile (SPP) UUID for RFCOMM connection
private val SPP_UUID: UUID =
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

class MainActivity : ComponentActivity() {

    private val bluetoothExecutor = Executors.newSingleThreadExecutor()

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private var updateStatusCallback: ((String) -> Unit)? = null

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val connectGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: true
            if (connectGranted) {
                connectBluetooth(currentTargetMac)
            } else {
                updateStatusCallback?.invoke("Bluetooth permission denied")
            }
        }

    private var currentTargetMac = DEFAULT_PI_ADDRESS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock to landscape orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Keep screen on during RC driving session
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Immersive sticky fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            RCRemortTheme {
                var status by remember { mutableStateOf("Not connected") }
                var lastCommand by remember { mutableStateOf("None") }
                var movementState by remember { mutableStateOf("Neutral") }
                var speedPercent by remember { mutableFloatStateOf(50f) }
                var targetMac by remember { mutableStateOf(DEFAULT_PI_ADDRESS) }

                // Dialog states
                var isBluetoothDialogOpen by remember { mutableStateOf(false) }
                var isSettingsDialogOpen by remember { mutableStateOf(false) }

                // Settings states
                var deadZone by remember { mutableFloatStateOf(0.15f) }
                var vibrationEnabled by remember { mutableStateOf(true) }

                // State tracker for compound movements
                var steerDirection by remember { mutableStateOf("CENTER") } // LEFT, RIGHT, CENTER
                var throttleDirection by remember { mutableStateOf("NEUTRAL") } // FORWARD, REVERSE, NEUTRAL
                var activeCmdCode by remember { mutableStateOf("S") }

                fun triggerVibrate(durationMs: Long = 40) {
                    if (!vibrationEnabled) return
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                            manager?.defaultVibrator?.vibrate(
                                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            val vib = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                            @Suppress("DEPRECATION")
                            vib?.vibrate(durationMs)
                        }
                    } catch (_: Exception) {}
                }

                fun evaluateAndSendCommand() {
                    val code = when {
                        throttleDirection == "FORWARD" && steerDirection == "LEFT" -> "G"
                        throttleDirection == "FORWARD" && steerDirection == "RIGHT" -> "I"
                        throttleDirection == "FORWARD" -> "F"

                        throttleDirection == "REVERSE" && steerDirection == "LEFT" -> "H"
                        throttleDirection == "REVERSE" && steerDirection == "RIGHT" -> "J"
                        throttleDirection == "REVERSE" -> "B"

                        steerDirection == "LEFT" -> "L"
                        steerDirection == "RIGHT" -> "R"
                        else -> "S"
                    }

                    movementState = when (code) {
                        "G" -> "Fwd-Left"
                        "I" -> "Fwd-Right"
                        "F" -> "Forward"
                        "H" -> "Rev-Left"
                        "J" -> "Rev-Right"
                        "B" -> "Reverse"
                        "L" -> "Left"
                        "R" -> "Right"
                        else -> "Neutral"
                    }

                    if (code != activeCmdCode) {
                        activeCmdCode = code
                        lastCommand = sendCommand(code)
                    }
                }

                fun emergencyStop() {
                    triggerVibrate(120)
                    steerDirection = "CENTER"
                    throttleDirection = "NEUTRAL"
                    activeCmdCode = "S"
                    movementState = "Stopped"
                    lastCommand = sendCommand("S")
                }

                SideEffect {
                    updateStatusCallback = { newStatus ->
                        runOnUiThread {
                            status = newStatus
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TechDarkBackground
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. TOP STATUS BAR
                        TopStatusBar(
                            status = status,
                            targetMac = targetMac,
                            onBluetoothClick = { isBluetoothDialogOpen = true },
                            onSettingsClick = { isSettingsDialogOpen = true }
                        )

                        // 2. MAIN CONTROLLER STATIONS (LEFT STEERING - CENTER HUD - RIGHT MOVEMENT)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Joystick: Steering
                            VirtualJoystick(
                                label = "STEERING",
                                size = 200.dp,
                                knobSize = 78.dp,
                                isSteering = true,
                                deadZone = deadZone,
                                onValueChange = { x, _ ->
                                    val newSteer = when {
                                        x < -deadZone -> "LEFT"
                                        x > deadZone -> "RIGHT"
                                        else -> "CENTER"
                                    }
                                    if (newSteer != steerDirection) {
                                        steerDirection = newSteer
                                        evaluateAndSendCommand()
                                    }
                                },
                                onRelease = {
                                    steerDirection = "CENTER"
                                    evaluateAndSendCommand()
                                }
                            )

                            // Central Vehicle Status Panel
                            VehicleStatusPanel(
                                status = status,
                                movementState = movementState,
                                lastCommand = lastCommand,
                                speedPercent = speedPercent,
                                onSpeedChange = { newSpeed ->
                                    speedPercent = newSpeed
                                    val pwm = (newSpeed * 2.55f).toInt()
                                    lastCommand = sendCommand("V:$pwm")
                                },
                                onEmergencyStop = { emergencyStop() }
                            )

                            // Right Joystick: Movement / Throttle
                            VirtualJoystick(
                                label = "MOVEMENT",
                                size = 200.dp,
                                knobSize = 78.dp,
                                isMovement = true,
                                deadZone = deadZone,
                                onValueChange = { _, y ->
                                    val newThrottle = when {
                                        y < -deadZone -> "FORWARD"
                                        y > deadZone -> "REVERSE"
                                        else -> "NEUTRAL"
                                    }
                                    if (newThrottle != throttleDirection) {
                                        throttleDirection = newThrottle
                                        evaluateAndSendCommand()
                                    }
                                },
                                onRelease = {
                                    throttleDirection = "NEUTRAL"
                                    evaluateAndSendCommand()
                                }
                            )
                        }
                    }

                    // Bluetooth Connection Modal Dialog
                    BluetoothDialog(
                        isOpen = isBluetoothDialogOpen,
                        status = status,
                        targetMac = targetMac,
                        onTargetMacChange = { newMac ->
                            targetMac = newMac
                            currentTargetMac = newMac
                        },
                        onConnect = { macToConnect ->
                            currentTargetMac = macToConnect
                            requestBluetoothPermissionsAndConnect()
                        },
                        onDisconnect = {
                            sendCommand("S")
                            disconnectBluetooth()
                            status = "Disconnected"
                            movementState = "Neutral"
                        },
                        onDismiss = { isBluetoothDialogOpen = false }
                    )

                    // System Configuration Settings Dialog
                    SettingsDialog(
                        isOpen = isSettingsDialogOpen,
                        deadZone = deadZone,
                        onDeadZoneChange = { deadZone = it },
                        vibrationEnabled = vibrationEnabled,
                        onVibrationToggle = { vibrationEnabled = it },
                        targetMac = targetMac,
                        status = status,
                        onDismiss = { isSettingsDialogOpen = false }
                    )
                }
            }
        }
    }

    private fun requestBluetoothPermissionsAndConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val needsConnect = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED

            val needsScan = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED

            if (needsConnect || needsScan) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
                return
            }
        }
        connectBluetooth(currentTargetMac)
    }

    @SuppressLint("MissingPermission")
    private fun connectBluetooth(macAddress: String) {
        val adapter = BluetoothAdapter.getDefaultAdapter()

        if (adapter == null) {
            updateStatusCallback?.invoke("Bluetooth not supported")
            return
        }

        if (!adapter.isEnabled) {
            updateStatusCallback?.invoke("Please enable Bluetooth")
            return
        }

        updateStatusCallback?.invoke("Connecting to $macAddress...")

        bluetoothExecutor.execute {
            try {
                disconnectBluetooth()

                val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()

                bluetoothSocket = socket
                outputStream = socket.outputStream

                updateStatusCallback?.invoke("Connected")

            } catch (e: Exception) {
                bluetoothSocket?.close()
                bluetoothSocket = null
                outputStream = null

                updateStatusCallback?.invoke("Connection failed: ${e.message}")
            }
        }
    }

    private fun sendCommand(command: String): String {
        val stream = outputStream ?: return "Send failed: no output stream"

        return try {
            stream.write((command + "\n").toByteArray(Charsets.UTF_8))
            stream.flush()
            updateStatusCallback?.invoke("Connected")
            command
        } catch (e: IOException) {
            updateStatusCallback?.invoke("Send error: ${e.message ?: "I/O error"}")
            "Send failed: ${e.message ?: "I/O error"}"
        }
    }

    private fun disconnectBluetooth() {
        try {
            bluetoothSocket?.close()
        } catch (_: IOException) {}
        bluetoothSocket = null
        outputStream = null
    }

    override fun onDestroy() {
        disconnectBluetooth()
        bluetoothExecutor.shutdownNow()
        super.onDestroy()
    }
}