package com.paliaapk.phonefix.model

enum class DiagnosticCategory(val displayName: String, val iconName: String) {
    BATTERY("Battery Health", "battery"),
    STORAGE("Storage Health", "storage"),
    MEMORY("RAM & Memory", "memory"),
    PERFORMANCE("CPU & Performance", "cpu"),
    TEMPERATURE("Thermal State", "thermostat"),
    NETWORK("Network & Wireless", "wifi"),
    SENSORS("Sensors & Telemetry", "sensors"),
    DISPLAY_AOD("Display & AOD", "display"),
    HARDWARE("Hardware Components", "hardware"),
    APPS("Applications Manager", "apps"),
    SECURITY("Security & Protection", "security"),
    SYSTEM("System & OS", "system")
}

enum class DiagnosticSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    INFO,
    HEALTHY
}

enum class DiagnosticStatus {
    PASS,
    ATTENTION,
    FAILED,
    SKIPPED,
    UNAVAILABLE
}

enum class FixActionType {
    OPEN_STORAGE_SETTINGS,
    OPEN_BATTERY_SETTINGS,
    OPEN_BATTERY_SAVER_SETTINGS,
    OPEN_DISPLAY_SETTINGS,
    OPEN_AOD_SETTINGS,
    OPEN_WIFI_SETTINGS,
    OPEN_BLUETOOTH_SETTINGS,
    OPEN_APP_SETTINGS,
    OPEN_SECURITY_SETTINGS,
    OPEN_DEVICE_INFO,
    OPEN_DEVELOPER_SETTINGS,
    OPEN_NOTIFICATION_SETTINGS,
    RUN_INTERNAL_CLEANUP,
    TEST_AGAIN,
    NONE
}

data class DiagnosticResult(
    val id: String,
    val category: DiagnosticCategory,
    val status: DiagnosticStatus,
    val severity: DiagnosticSeverity,
    val title: String,
    val description: String,
    val evidence: String,
    val recommendation: String,
    val fixActionType: FixActionType = FixActionType.NONE,
    val targetPackage: String? = null,
    val isFixed: Boolean = false,
    val isIgnored: Boolean = false
)
