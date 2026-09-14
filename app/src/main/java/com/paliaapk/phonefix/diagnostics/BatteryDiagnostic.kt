package com.paliaapk.phonefix.diagnostics

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType

class BatteryDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.BATTERY

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            val batteryStatus: Intent? = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            if (batteryStatus == null) {
                results.add(
                    DiagnosticResult(
                        id = "battery_unavailable",
                        category = category,
                        status = DiagnosticStatus.UNAVAILABLE,
                        severity = DiagnosticSeverity.INFO,
                        title = "Battery Telemetry Restricted",
                        description = "Android battery status intent returned null on this device firmware.",
                        evidence = "Intent.ACTION_BATTERY_CHANGED returned null",
                        recommendation = "Check device power settings manually."
                    )
                )
                return results
            }

            // Level & Scale
            val level: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else -1

            // Status & Plugged
            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val plugSource = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Induction"
                else -> "Battery Discharging"
            }

            // Health
            val health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val healthText = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead / Damaged Cell"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure Reported"
                BatteryManager.BATTERY_HEALTH_COLD -> "Sub-zero Cold"
                else -> "Unknown / OEM Protected"
            }

            // Temperature & Voltage
            val tempTenths = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val tempC = tempTenths / 10.0f
            val voltageMv = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

            // Battery Saver
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isPowerSaveMode = powerManager?.isPowerSaveMode ?: false

            // Level check
            if (batteryPct in 0..15 && !isCharging) {
                results.add(
                    DiagnosticResult(
                        id = "battery_critical_level",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.HIGH,
                        title = "Critically Low Battery ($batteryPct%)",
                        description = "Your battery is below 15% and discharging. Connecting to a certified charger prevents unexpected shutdown.",
                        evidence = "Level: $batteryPct%, Status: $plugSource",
                        recommendation = "Plug in a certified charger or enable Battery Saver.",
                        fixActionType = FixActionType.OPEN_BATTERY_SAVER_SETTINGS
                    )
                )
            } else if (batteryPct > 0) {
                results.add(
                    DiagnosticResult(
                        id = "battery_level_ok",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.HEALTHY,
                        title = "Battery Charge Level ($batteryPct%)",
                        description = "Battery level is stable. Currently $plugSource.",
                        evidence = "Level: $batteryPct%, Source: $plugSource",
                        recommendation = "Maintain regular charging cycles between 20% and 80% for long-term health."
                    )
                )
            }

            // Health status check
            if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT || health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE || health == BatteryManager.BATTERY_HEALTH_DEAD) {
                results.add(
                    DiagnosticResult(
                        id = "battery_health_defect",
                        category = category,
                        status = DiagnosticStatus.FAILED,
                        severity = DiagnosticSeverity.CRITICAL,
                        title = "Abnormal Battery State: $healthText",
                        description = "Android kernel reports abnormal electrical battery health ($healthText). Hardware/service inspection may be required.",
                        evidence = "BatteryManager.EXTRA_HEALTH = $healthText, Voltage = ${voltageMv}mV",
                        recommendation = "Hardware/service inspection may be required. Unplug charger if device feels hot.",
                        fixActionType = FixActionType.OPEN_BATTERY_SETTINGS
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        id = "battery_health_status",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.HEALTHY,
                        title = "Battery Hardware Condition: $healthText",
                        description = "Android reports normal battery cell status. Note: Modern Android does not expose wear degradation cycles without OEM system privileges.",
                        evidence = "Reported state: $healthText, Voltage: ${voltageMv}mV",
                        recommendation = "No hardware faults reported by battery controller."
                    )
                )
            }

            // Thermal check on battery
            if (tempC >= 45.0f) {
                results.add(
                    DiagnosticResult(
                        id = "battery_thermal_critical",
                        category = category,
                        status = DiagnosticStatus.FAILED,
                        severity = DiagnosticSeverity.CRITICAL,
                        title = "Battery Temperature Critical (${tempC}°C)",
                        description = "Battery temperature is elevated above 45°C. Prolonged heat damages lithium polymer chemistry.",
                        evidence = "Temperature sensor: ${tempC}°C",
                        recommendation = "Stop fast charging, close 3D games or navigation apps, and allow phone to cool down.",
                        fixActionType = FixActionType.OPEN_BATTERY_SETTINGS
                    )
                )
            } else if (tempC >= 40.0f) {
                results.add(
                    DiagnosticResult(
                        id = "battery_thermal_warm",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.MEDIUM,
                        title = "Battery Running Warm (${tempC}°C)",
                        description = "Battery is moderately warm. Heavy charging or intensive background tasks may be active.",
                        evidence = "Temperature sensor: ${tempC}°C",
                        recommendation = "Keep phone off soft surfaces like blankets while charging.",
                        fixActionType = FixActionType.OPEN_BATTERY_SETTINGS
                    )
                )
            }

            // Power Saver note
            if (isPowerSaveMode) {
                results.add(
                    DiagnosticResult(
                        id = "battery_saver_active",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.INFO,
                        title = "Battery Saver is Active",
                        description = "System power saving mode is on, restricting background sync and vibration to preserve energy.",
                        evidence = "PowerManager.isPowerSaveMode = true",
                        recommendation = "Turn off if you notice delayed notifications.",
                        fixActionType = FixActionType.OPEN_BATTERY_SAVER_SETTINGS
                    )
                )
            }

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "battery_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "Battery Telemetry Restricted",
                    description = "Unable to read full battery info: ${e.localizedMessage ?: "OEM Restriction"}",
                    evidence = "Exception during inspection",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }
}
