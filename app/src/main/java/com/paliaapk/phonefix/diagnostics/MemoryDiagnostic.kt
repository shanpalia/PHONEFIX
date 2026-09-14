package com.paliaapk.phonefix.diagnostics

import android.app.ActivityManager
import android.content.Context
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType

class MemoryDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.MEMORY

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()

            if (actManager != null) {
                actManager.getMemoryInfo(memInfo)

                val totalGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
                val availGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
                val usedGb = (memInfo.totalMem - memInfo.availMem).coerceAtLeast(0) / (1024.0 * 1024.0 * 1024.0)
                val thresholdMb = memInfo.threshold / (1024.0 * 1024.0)

                val usedPercent = if (memInfo.totalMem > 0) {
                    (((memInfo.totalMem - memInfo.availMem) * 100) / memInfo.totalMem).toInt()
                } else 0

                if (memInfo.lowMemory || usedPercent >= 92) {
                    results.add(
                        DiagnosticResult(
                            id = "memory_critical_pressure",
                            category = category,
                            status = DiagnosticStatus.FAILED,
                            severity = DiagnosticSeverity.CRITICAL,
                            title = "High Memory Pressure ($usedPercent% in use)",
                            description = "Android Linux Low Memory Killer (LMK) is actively under memory pressure. Available RAM is near threshold (${"%.1f".format(availGb)} GB free). Heavy multitasking may cause background apps to restart.",
                            evidence = "Total RAM: ${"%.1f".format(totalGb)} GB, Free: ${"%.1f".format(availGb)} GB, LMK Flag: ${memInfo.lowMemory}",
                            recommendation = "Close unused background applications or restart device. Note: Android kernel manages RAM automatically.",
                            fixActionType = FixActionType.OPEN_APP_SETTINGS
                        )
                    )
                } else if (usedPercent >= 82) {
                    results.add(
                        DiagnosticResult(
                            id = "memory_moderate_load",
                            category = category,
                            status = DiagnosticStatus.ATTENTION,
                            severity = DiagnosticSeverity.MEDIUM,
                            title = "Moderate Memory Load ($usedPercent%)",
                            description = "RAM is ${usedPercent}% utilized. In Linux/Android, high RAM usage is normal for caching apps in memory for fast launching, provided the kernel does not signal memory thrashing.",
                            evidence = "RAM used: ${"%.1f".format(usedGb)} GB / ${"%.1f".format(totalGb)} GB (Available: ${"%.1f".format(availGb)} GB)",
                            recommendation = "No action required unless you observe frame drops in heavy games."
                        )
                    )
                } else {
                    results.add(
                        DiagnosticResult(
                            id = "memory_healthy",
                            category = category,
                            status = DiagnosticStatus.PASS,
                            severity = DiagnosticSeverity.HEALTHY,
                            title = "RAM Capacity Healthy (${"%.1f".format(availGb)} GB free)",
                            description = "Adequate physical RAM available for multitasking and app caching.",
                            evidence = "Total: ${"%.1f".format(totalGb)} GB, Free: ${"%.1f".format(availGb)} GB, Threshold: ${"%.0f".format(thresholdMb)} MB",
                            recommendation = "Memory subsystem is operating normally."
                        )
                    )
                }

                // Architecture explanation
                results.add(
                    DiagnosticResult(
                        id = "memory_architecture_info",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.INFO,
                        title = "Android RAM Management Fact",
                        description = "Unused RAM is wasted RAM in Android. The OS intentionally keeps recent apps cached in memory for instant switching. PHONEFIX never advertises 'RAM cleaners' that force-kill processes and waste battery on relaunches.",
                        evidence = "Kernel architecture: Linux cgroups & LMK",
                        recommendation = "Rely on Android's native task manager for genuine efficiency."
                    )
                )

            } else {
                results.add(
                    DiagnosticResult(
                        id = "memory_unavailable",
                        category = category,
                        status = DiagnosticStatus.UNAVAILABLE,
                        severity = DiagnosticSeverity.INFO,
                        title = "Memory Telemetry Restricted",
                        description = "ActivityManager service is unavailable on this device context.",
                        evidence = "getSystemService returned null",
                        recommendation = "Android does not allow PHONEFIX to access this information on this device."
                    )
                )
            }

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "memory_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "Memory Inspection Error",
                    description = e.localizedMessage ?: "Unknown restriction",
                    evidence = "Exception in MemoryDiagnostic",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }
}
