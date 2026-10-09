# RC-Remort

An Android remote controller application built with **Kotlin** and **Jetpack Compose** designed for remote vehicle control over Bluetooth RFCOMM (Serial Port Profile - SPP).

## Features

- **Dual Virtual Joysticks**:
  - **Left Joystick**: Steering control (Left, Center, Right).
  - **Right Joystick**: Throttle & Direction control (Forward, Neutral, Reverse).
- **Vehicle Status HUD**: Real-time telemetry feedback, active command status, and adjustable speed limiter.
- **Emergency Stop (E-Stop)**: Instant fail-safe braking command (`S`) with haptic vibration feedback.
- **Bluetooth SPP Manager**: Connect to target robotic platform / Raspberry Pi via standard Bluetooth Serial Port Profile.
- **Configurable Settings**: Dead-zone calibration, haptic vibration toggle, and target MAC configuration.
- **Landscape Immersive UI**: Sticky fullscreen UI built with modern Material 3 and custom dark tech design.

## Tech Stack

- **Platform**: Android (minSdk 35, targetSdk 37)
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose + Material 3
- **Communication**: Android Bluetooth RFCOMM / SPP (`00001101-0000-1000-8000-00805F9B34FB`)
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`)

## Building and Running

1. Open project in **Android Studio**.
2. Sync Gradle files.
3. Build and run on an Android device or emulator running Android 15+.
