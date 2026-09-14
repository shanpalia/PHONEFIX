package com.paliaapk.phonefix.model

enum class HardwareTestType(val title: String, val description: String) {
    DISPLAY("Display Colors", "Inspect RGB and white/black screens for dead or stuck pixels"),
    TOUCHSCREEN("Touchscreen", "Single touch tracking across screen coordinates"),
    MULTI_TOUCH("Multi-Touch", "Detect multiple simultaneous touch points"),
    SPEAKER("Main Speaker", "Audio tone playback test through media speaker"),
    EARPIECE("Earpiece Receiver", "Audio tone test through call receiver"),
    MICROPHONE("Microphone", "Live decibel recording and audio level test"),
    VIBRATION("Vibration Motor", "Haptic pulse feedback test"),
    FRONT_CAMERA("Front Camera", "Front lens optical characteristics & sensor check"),
    REAR_CAMERA("Rear Camera", "Rear lens optical characteristics & sensor check"),
    FLASH("Camera Flash / Torch", "LED flashlight torch toggle test"),
    PROXIMITY("Proximity Sensor", "Check object distance detection (near/far)"),
    ACCELEROMETER("Accelerometer", "3-axis tilt and gravity acceleration test"),
    GYROSCOPE("Gyroscope", "3-axis angular rotational velocity sensor"),
    COMPASS("Magnetic Compass", "Magnetometer heading azimuth and field strength"),
    LIGHT_SENSOR("Ambient Light", "Illuminance lux level sensor response"),
    FINGERPRINT("Biometrics", "Biometric/fingerprint authentication hardware check"),
    CHARGING_USB("Charging & USB", "Power connection and USB interface status")
}

data class HardwareTestResult(
    val type: HardwareTestType,
    val status: DiagnosticStatus = DiagnosticStatus.SKIPPED,
    val details: String = "Not tested yet",
    val measurement: String = ""
)
