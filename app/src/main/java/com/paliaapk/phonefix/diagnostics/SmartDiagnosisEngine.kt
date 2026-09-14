package com.paliaapk.phonefix.diagnostics

import com.paliaapk.phonefix.model.AodWakeSession
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType
import com.paliaapk.phonefix.model.SystemInfoData

data class SmartDiagnosisCase(
    val id: String,
    val problemTitle: String,
    val category: DiagnosticCategory,
    val severity: DiagnosticSeverity,
    val whyHappening: String,
    val recommendedAction: String,
    val fixActionType: FixActionType,
    val isHardwareInspectionRecommended: Boolean = false
)

class SmartDiagnosisEngine {

    fun analyze(
        results: List<DiagnosticResult>,
        systemInfo: SystemInfoData,
        aodSession: AodWakeSession?
    ): List<SmartDiagnosisCase> {
        val cases = mutableListOf<SmartDiagnosisCase>()

        // 1. Storage Depletion Rule
        if (systemInfo.storageUsedPercent >= 90) {
            cases.add(
                SmartDiagnosisCase(
                    id = "smart_storage_full",
                    problemTitle = "Critical Storage Depletion (${systemInfo.storageUsedPercent}%)",
                    category = DiagnosticCategory.STORAGE,
                    severity = DiagnosticSeverity.CRITICAL,
                    whyHappening = "Internal flash memory is at ${systemInfo.storageUsedPercent}% capacity. Android requires swap space and cache headroom to avoid write latency and database lockups.",
                    recommendedAction = "Free at least several GB of storage by removing unused media or clearing non-essential app caches.",
                    fixActionType = FixActionType.OPEN_STORAGE_SETTINGS
                )
            )
        }

        // 2. Battery Overheating + Charging Rule
        if (systemInfo.batteryTempC >= 43.0f && systemInfo.isCharging) {
            cases.add(
                SmartDiagnosisCase(
                    id = "smart_charging_heat",
                    problemTitle = "High Thermal Load During Charging (${"%.1f".format(systemInfo.batteryTempC)}°C)",
                    category = DiagnosticCategory.BATTERY,
                    severity = DiagnosticSeverity.HIGH,
                    whyHappening = "Fast charging combined with current device environment or background processes is elevating battery temperature beyond optimal operating ranges.",
                    recommendedAction = "Remove thick phone cases while fast charging, keep away from direct heat sources, and avoid high-framerate 3D gaming while plugged in.",
                    fixActionType = FixActionType.OPEN_BATTERY_SETTINGS
                )
            )
        }

        // 3. AOD Wake-Up Problem Correlation Rule
        if (aodSession != null && aodSession.suspiciousCyclesDetected) {
            cases.add(
                SmartDiagnosisCase(
                    id = "smart_aod_frequent_wake",
                    problemTitle = "Possible AOD / Screen Wake Loop Detected",
                    category = DiagnosticCategory.DISPLAY_AOD,
                    severity = DiagnosticSeverity.HIGH,
                    whyHappening = "Repeated rapid OFF → ON → OFF screen wake transitions were detected (${aodSession.wakeEventsCount} wakes in ${aodSession.sessionDurationSec}s). Likely triggers: persistent background notifications, high-sensitivity Lift-to-wake, pocket proximity sensor miscalibration, or a rogue wake-lock app.",
                    recommendedAction = "Inspect lock screen notification wake settings and verify Lift-to-wake / Tap-to-wake sensitivity in Display Settings.",
                    fixActionType = FixActionType.OPEN_AOD_SETTINGS
                )
            )
        }

        // 4. Insecure Screen Lock + ADB Active Rule
        val hasNoScreenLock = results.any { it.id == "sec_no_screen_lock" }
        val hasAdb = results.any { it.id == "sec_usb_debugging_active" }
        if (hasNoScreenLock && hasAdb) {
            cases.add(
                SmartDiagnosisCase(
                    id = "smart_high_exposure_security",
                    problemTitle = "High Device Exposure Risk (No PIN + ADB Active)",
                    category = DiagnosticCategory.SECURITY,
                    severity = DiagnosticSeverity.CRITICAL,
                    whyHappening = "Device has neither screen lock credentials nor ADB restrictions enabled. Physical access to this phone permits complete data extraction without password challenge.",
                    recommendedAction = "Immediately configure a secure lock screen PIN or fingerprint, and disable USB debugging in Developer Options.",
                    fixActionType = FixActionType.OPEN_SECURITY_SETTINGS
                )
            )
        } else if (hasNoScreenLock) {
            cases.add(
                SmartDiagnosisCase(
                    id = "smart_no_lock",
                    problemTitle = "Unprotected Device Lock Screen",
                    category = DiagnosticCategory.SECURITY,
                    severity = DiagnosticSeverity.HIGH,
                    whyHappening = "No PIN, password, or biometric lock is configured.",
                    recommendedAction = "Enable a secure screen lock in Android Security settings to protect your data.",
                    fixActionType = FixActionType.OPEN_SECURITY_SETTINGS
                )
            )
        }

        // 5. Memory Pressure Rule
        if (systemInfo.isLowMemory || systemInfo.ramUsedPercent >= 92) {
            cases.add(
                SmartDiagnosisCase(
                    id = "smart_memory_thrash",
                    problemTitle = "Severe Memory Contention (${systemInfo.ramUsedPercent}%)",
                    category = DiagnosticCategory.MEMORY,
                    severity = DiagnosticSeverity.MEDIUM,
                    whyHappening = "Physical RAM is under high load. Low Memory Killer may terminate background tasks unexpectedly.",
                    recommendedAction = "Reboot device or review recently launched multitasking apps. PHONEFIX does not recommend aggressive RAM killer utilities.",
                    fixActionType = FixActionType.OPEN_APP_SETTINGS
                )
            )
        }

        // 6. Prolonged Screen Sleep Rule
        val timeoutWarning = results.find { it.id == "display_timeout_warning" }
        if (timeoutWarning != null) {
            cases.add(
                SmartDiagnosisCase(
                    id = "smart_screen_timeout",
                    problemTitle = "Excessive Display Standby Timeout",
                    category = DiagnosticCategory.DISPLAY_AOD,
                    severity = DiagnosticSeverity.MEDIUM,
                    whyHappening = "Display is configured to stay awake for more than 5 minutes of inactivity, unnecessarily draining battery.",
                    recommendedAction = "Adjust screen sleep timeout to 30-60 seconds in Display Settings.",
                    fixActionType = FixActionType.OPEN_DISPLAY_SETTINGS
                )
            )
        }

        // 7. Battery Defect Check Rule
        val batteryDefect = results.find { it.id == "battery_health_defect" }
        if (batteryDefect != null) {
            cases.add(
                SmartDiagnosisCase(
                    id = "smart_battery_hw_fault",
                    problemTitle = "Battery Controller Electrical Anomaly",
                    category = DiagnosticCategory.BATTERY,
                    severity = DiagnosticSeverity.CRITICAL,
                    whyHappening = "Android kernel reports abnormal hardware battery condition (${batteryDefect.title}).",
                    recommendedAction = "Hardware/service inspection may be required. Software cannot repair depleted or damaged chemical battery cells.",
                    fixActionType = FixActionType.OPEN_BATTERY_SETTINGS,
                    isHardwareInspectionRecommended = true
                )
            )
        }

        // 8. Captive Portal / DNS Connectivity Rule
        val captiveNet = results.find { it.id == "net_internet_captive" }
        if (captiveNet != null) {
            cases.add(
                SmartDiagnosisCase(
                    id = "smart_net_captive",
                    problemTitle = "DNS Network Routing Stalled",
                    category = DiagnosticCategory.NETWORK,
                    severity = DiagnosticSeverity.HIGH,
                    whyHappening = "Wi-Fi link is connected to local router, but outbound internet requests cannot resolve external DNS packets.",
                    recommendedAction = "Open browser to sign in to guest network portal or reconnect to Wi-Fi.",
                    fixActionType = FixActionType.OPEN_WIFI_SETTINGS
                )
            )
        }

        return cases
    }
}
