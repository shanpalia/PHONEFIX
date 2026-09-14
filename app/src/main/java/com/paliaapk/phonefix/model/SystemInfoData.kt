package com.paliaapk.phonefix.model

data class SystemInfoData(
    val deviceModel: String = "",
    val manufacturer: String = "",
    val brand: String = "",
    val board: String = "",
    val hardware: String = "",
    val androidVersion: String = "",
    val apiLevel: Int = 0,
    val securityPatch: String = "",
    val buildId: String = "",
    val uptimeHours: String = "",
    
    // RAM
    val totalRamBytes: Long = 0L,
    val availRamBytes: Long = 0L,
    val isLowMemory: Boolean = false,
    val ramThresholdBytes: Long = 0L,
    
    // Storage
    val totalStorageBytes: Long = 0L,
    val freeStorageBytes: Long = 0L,
    val usedStorageBytes: Long = 0L,
    
    // Battery
    val batteryPct: Int = -1,
    val isCharging: Boolean = false,
    val chargePlug: String = "Unplugged",
    val batteryHealth: String = "Good",
    val batteryTempC: Float = 0f,
    val batteryVoltageMv: Int = 0,
    val batteryTechnology: String = "",
    val isPowerSaveMode: Boolean = false,
    
    // Display
    val screenResolution: String = "",
    val refreshRate: Float = 60f,
    val screenDensityDpi: Int = 0,
    
    // CPU
    val cpuCores: Int = 1,
    val cpuAbi: String = "",
    
    // Network
    val isWifiConnected: Boolean = false,
    val wifiSsid: String = "",
    val isCellularConnected: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val isVpnActive: Boolean = false
) {
    val usedRamBytes: Long get() = (totalRamBytes - availRamBytes).coerceAtLeast(0L)
    val ramUsedPercent: Int get() = if (totalRamBytes > 0) ((usedRamBytes * 100) / totalRamBytes).toInt() else 0
    val storageUsedPercent: Int get() = if (totalStorageBytes > 0) ((usedStorageBytes * 100) / totalStorageBytes).toInt() else 0
}

data class AodWakeEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // "SCREEN_ON", "SCREEN_OFF", "USER_PRESENT"
    val intervalSinceLastMs: Long = 0L,
    val sensorNotes: String = ""
)

data class AodWakeSession(
    val isRunning: Boolean = false,
    val sessionDurationSec: Int = 0,
    val wakeEventsCount: Int = 0,
    val eventsList: List<AodWakeEvent> = emptyList(),
    val suspiciousCyclesDetected: Boolean = false,
    val possibleCauses: List<String> = emptyList(),
    val verdict: String = "Test not started yet"
)
