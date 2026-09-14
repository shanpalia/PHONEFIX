package com.paliaapk.phonefix.diagnostics

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType
import java.io.File

class StorageDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.STORAGE

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)

            val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val usedGb = usedBytes / (1024.0 * 1024.0 * 1024.0)
            val freeGb = freeBytes / (1024.0 * 1024.0 * 1024.0)

            val usedPercent = if (totalBytes > 0) ((usedBytes * 100) / totalBytes).toInt() else 0

            // Cache inspection for app data
            val cacheSize = context.cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            val cacheMb = cacheSize / (1024.0 * 1024.0)

            if (usedPercent >= 92) {
                results.add(
                    DiagnosticResult(
                        id = "storage_critical",
                        category = category,
                        status = DiagnosticStatus.FAILED,
                        severity = DiagnosticSeverity.CRITICAL,
                        title = "Storage Critically Full ($usedPercent%)",
                        description = "Internal flash storage is almost exhausted ($usedPercent% used, only ${"%.1f".format(freeGb)} GB free). Android requires free space for swap, caching, app updates, and crash prevention.",
                        evidence = "Used: ${"%.1f".format(usedGb)} GB / Total: ${"%.1f".format(totalGb)} GB (Free: ${"%.1f".format(freeGb)} GB)",
                        recommendation = "Free at least several GB by uninstalling unused apps or backing up large media files.",
                        fixActionType = FixActionType.OPEN_STORAGE_SETTINGS
                    )
                )
            } else if (usedPercent >= 85) {
                results.add(
                    DiagnosticResult(
                        id = "storage_warning",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.HIGH,
                        title = "Storage Space Running Low ($usedPercent%)",
                        description = "Storage is above 85% capacity. Apps and system updates may encounter slowdowns or write delays.",
                        evidence = "Used: ${"%.1f".format(usedGb)} GB / Total: ${"%.1f".format(totalGb)} GB (Free: ${"%.1f".format(freeGb)} GB)",
                        recommendation = "Review downloaded files, duplicate photos, or clear app caches.",
                        fixActionType = FixActionType.OPEN_STORAGE_SETTINGS
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        id = "storage_healthy",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.HEALTHY,
                        title = "Storage Capacity Healthy ($usedPercent% used)",
                        description = "Sufficient internal flash storage is available for smooth read/write input-output operations.",
                        evidence = "Free: ${"%.1f".format(freeGb)} GB out of ${"%.1f".format(totalGb)} GB (${100 - usedPercent}% available)",
                        recommendation = "Storage condition is optimal."
                    )
                )
            }

            // Internal cache status
            results.add(
                DiagnosticResult(
                    id = "storage_cache_info",
                    category = category,
                    status = DiagnosticStatus.PASS,
                    severity = DiagnosticSeverity.INFO,
                    title = "App Cache Footprint",
                    description = "Temporary application cached files occupy ${"%.2f".format(cacheMb)} MB in local sandbox storage.",
                    evidence = "Cache directory size: ${"%.2f".format(cacheMb)} MB",
                    recommendation = "PHONEFIX provides safe cache clearance without touching your photos, messages, or files.",
                    fixActionType = FixActionType.RUN_INTERNAL_CLEANUP
                )
            )

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "storage_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "Storage Telemetry Restricted",
                    description = "Unable to read storage metrics: ${e.localizedMessage ?: "Restricted"}",
                    evidence = "StatFs exception",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }
}
