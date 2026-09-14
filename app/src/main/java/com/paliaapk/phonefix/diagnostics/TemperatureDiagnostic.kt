package com.paliaapk.phonefix.diagnostics

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType

class TemperatureDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.TEMPERATURE

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            val batteryStatus: Intent? = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val batteryTempC = tempTenths / 10.0f

            val categoryLabel: String
            val status: DiagnosticStatus
            val severity: DiagnosticSeverity
            val advice: String

            when {
                batteryTempC > 48.0f -> {
                    categoryLabel = "Critical"
                    status = DiagnosticStatus.FAILED
                    severity = DiagnosticSeverity.CRITICAL
                    advice = "Immediately stop high load tasks or charging. Move phone to cooler ambient temperature. Hardware/service inspection may be required if overheating persists while idle."
                }
                batteryTempC >= 43.0f -> {
                    categoryLabel = "High"
                    status = DiagnosticStatus.ATTENTION
                    severity = DiagnosticSeverity.HIGH
                    advice = "Device is running hot. Avoid intense gaming and direct sunlight exposure."
                }
                batteryTempC >= 38.0f -> {
                    categoryLabel = "Warm"
                    status = DiagnosticStatus.PASS
                    severity = DiagnosticSeverity.MEDIUM
                    advice = "Device temperature is slightly elevated, normal during charging or GPS usage."
                }
                batteryTempC > 0f -> {
                    categoryLabel = "Normal"
                    status = DiagnosticStatus.PASS
                    severity = DiagnosticSeverity.HEALTHY
                    advice = "Hardware thermal sensors indicate comfortable operating temperatures."
                }
                else -> {
                    categoryLabel = "Unavailable"
                    status = DiagnosticStatus.UNAVAILABLE
                    severity = DiagnosticSeverity.INFO
                    advice = "Android does not allow PHONEFIX to access this information on this device."
                }
            }

            results.add(
                DiagnosticResult(
                    id = "thermal_sensor_reading",
                    category = category,
                    status = status,
                    severity = severity,
                    title = "Thermal Zone Classification: $categoryLabel",
                    description = if (batteryTempC > 0) {
                        "Internal thermal probe reading: ${"%.1f".format(batteryTempC)}°C ($categoryLabel range)."
                    } else {
                        "Thermal sensor reading was not exposed by firmware."
                    },
                    evidence = "Measured: ${"%.1f".format(batteryTempC)}°C",
                    recommendation = advice,
                    fixActionType = if (status != DiagnosticStatus.PASS) FixActionType.OPEN_BATTERY_SETTINGS else FixActionType.NONE
                )
            )

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "temp_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "Thermal Telemetry Restricted",
                    description = e.localizedMessage ?: "Restricted",
                    evidence = "Exception in TemperatureDiagnostic",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }
}
