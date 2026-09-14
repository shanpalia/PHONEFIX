package com.paliaapk.phonefix.diagnostics

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType

class PerformanceDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.PERFORMANCE

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            val cores = Runtime.getRuntime().availableProcessors()
            val abis = Build.SUPPORTED_ABIS.joinToString(", ")
            val uptimeHours = SystemClock.elapsedRealtime() / (1000 * 60 * 60)

            // Thermal state
            var thermalStatusText = "Normal"
            var thermalSeverity = DiagnosticSeverity.HEALTHY
            var thermalDiagStatus = DiagnosticStatus.PASS

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                val thermalStatus = powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE

                when (thermalStatus) {
                    PowerManager.THERMAL_STATUS_NONE -> {
                        thermalStatusText = "Nominal (No Throttling)"
                    }
                    PowerManager.THERMAL_STATUS_LIGHT -> {
                        thermalStatusText = "Light Throttling"
                        thermalSeverity = DiagnosticSeverity.MEDIUM
                        thermalDiagStatus = DiagnosticStatus.ATTENTION
                    }
                    PowerManager.THERMAL_STATUS_MODERATE -> {
                        thermalStatusText = "Moderate Throttling"
                        thermalSeverity = DiagnosticSeverity.HIGH
                        thermalDiagStatus = DiagnosticStatus.ATTENTION
                    }
                    PowerManager.THERMAL_STATUS_SEVERE -> {
                        thermalStatusText = "Severe Throttling (FPS drops likely)"
                        thermalSeverity = DiagnosticSeverity.CRITICAL
                        thermalDiagStatus = DiagnosticStatus.FAILED
                    }
                    PowerManager.THERMAL_STATUS_CRITICAL -> {
                        thermalStatusText = "Critical Thermal State"
                        thermalSeverity = DiagnosticSeverity.CRITICAL
                        thermalDiagStatus = DiagnosticStatus.FAILED
                    }
                    PowerManager.THERMAL_STATUS_EMERGENCY -> {
                        thermalStatusText = "Emergency Thermal Shutdown Imminent"
                        thermalSeverity = DiagnosticSeverity.CRITICAL
                        thermalDiagStatus = DiagnosticStatus.FAILED
                    }
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> {
                        thermalStatusText = "Device Overheating Shutdown Triggered"
                        thermalSeverity = DiagnosticSeverity.CRITICAL
                        thermalDiagStatus = DiagnosticStatus.FAILED
                    }
                }
            } else {
                thermalStatusText = "Thermal Status API requires Android 10+"
            }

            // CPU cores check
            results.add(
                DiagnosticResult(
                    id = "cpu_cores_ok",
                    category = category,
                    status = DiagnosticStatus.PASS,
                    severity = DiagnosticSeverity.HEALTHY,
                    title = "CPU Core Architecture ($cores Cores)",
                    description = "Detected $cores hardware CPU execution cores. Hardware architecture: $abis.",
                    evidence = "Core count: $cores, ABIs: $abis, Board: ${Build.BOARD}",
                    recommendation = "Multi-core processor subsystem is initialized."
                )
            )

            // Thermal throttling result
            results.add(
                DiagnosticResult(
                    id = "thermal_throttling_status",
                    category = category,
                    status = thermalDiagStatus,
                    severity = thermalSeverity,
                    title = "CPU Thermal Throttle: $thermalStatusText",
                    description = if (thermalDiagStatus == DiagnosticStatus.PASS) {
                        "CPU is running without active thermal frequency throttling."
                    } else {
                        "SoC governor has engaged thermal protection to reduce core temperatures. Performance and frame rates may temporarily dip."
                    },
                    evidence = "Thermal state: $thermalStatusText",
                    recommendation = if (thermalDiagStatus == DiagnosticStatus.PASS) {
                        "Device temperature is balanced."
                    } else {
                        "Allow device to cool down away from direct sunlight."
                    }
                )
            )

            // Uptime check
            if (uptimeHours >= 24 * 7) { // 7 days without reboot
                results.add(
                    DiagnosticResult(
                        id = "perf_high_uptime",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.MEDIUM,
                        title = "High Continuous Uptime (${uptimeHours} hours)",
                        description = "Device has been continuously running for over ${uptimeHours / 24} days without a reboot. Periodic reboots release cached kernel heap structures.",
                        evidence = "Elapsed uptime: $uptimeHours hours",
                        recommendation = "A quick phone restart once a week clears residual zombie threads."
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        id = "perf_uptime_ok",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.HEALTHY,
                        title = "System Uptime ($uptimeHours hours)",
                        description = "Operating system uptime is within optimal parameters.",
                        evidence = "Uptime: $uptimeHours hours",
                        recommendation = "No restart needed at this time."
                    )
                )
            }

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "perf_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "Performance Telemetry Restricted",
                    description = e.localizedMessage ?: "Restricted",
                    evidence = "Exception during performance query",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }
}
