package com.paliaapk.phonefix.diagnostics

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val permissionCount: Int,
    val hasLocationPermission: Boolean,
    val hasCameraPermission: Boolean,
    val hasMicPermission: Boolean
)

class AppDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.APPS

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            val pm = context.packageManager
            val flags = PackageManager.GET_PERMISSIONS
            val packages: List<PackageInfo> = try {
                pm.getInstalledPackages(flags)
            } catch (e: Exception) {
                emptyList()
            }

            var userAppsCount = 0
            var systemAppsCount = 0
            val heavyPermissionApps = mutableListOf<String>()

            for (pkg in packages) {
                val isSystem = (pkg.applicationInfo != null && (pkg.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
                if (isSystem) {
                    systemAppsCount++
                } else {
                    userAppsCount++
                    val perms = pkg.requestedPermissions
                    if (perms != null && perms.size > 25) {
                        val name = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
                        heavyPermissionApps.add(name)
                    }
                }
            }

            results.add(
                DiagnosticResult(
                    id = "apps_inventory",
                    category = category,
                    status = DiagnosticStatus.PASS,
                    severity = DiagnosticSeverity.HEALTHY,
                    title = "Installed Apps Audit ($userAppsCount User Apps)",
                    description = "Detected $userAppsCount third-party user applications and $systemAppsCount pre-installed system packages.",
                    evidence = "Total scanned packages: ${packages.size}",
                    recommendation = "Review installed apps in the Apps Manager to reclaim storage space.",
                    fixActionType = FixActionType.OPEN_APP_SETTINGS
                )
            )

            if (heavyPermissionApps.isNotEmpty()) {
                val sampleNames = heavyPermissionApps.take(3).joinToString(", ")
                results.add(
                    DiagnosticResult(
                        id = "apps_heavy_permissions",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.MEDIUM,
                        title = "${heavyPermissionApps.size} Apps with Extensive Permissions",
                        description = "Several installed apps ($sampleNames...) hold access to numerous device subsystem permissions.",
                        evidence = "Apps holding >25 permissions: ${heavyPermissionApps.size}",
                        recommendation = "Review app permissions in Settings and revoke unused privileges.",
                        fixActionType = FixActionType.OPEN_APP_SETTINGS
                    )
                )
            }

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "apps_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "App Inventory Restricted",
                    description = e.localizedMessage ?: "Restricted",
                    evidence = "PackageManager query returned error",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }

    fun getInstalledAppsList(context: Context): List<InstalledAppItem> {
        val pm = context.packageManager
        val list = mutableListOf<InstalledAppItem>()
        try {
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            for (pkg in packages) {
                val isSystem = (pkg.applicationInfo != null && (pkg.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
                val label = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
                val perms = pkg.requestedPermissions?.toList() ?: emptyList()
                val hasLoc = perms.any { it.contains("LOCATION", ignoreCase = true) }
                val hasCam = perms.any { it.contains("CAMERA", ignoreCase = true) }
                val hasMic = perms.any { it.contains("RECORD_AUDIO", ignoreCase = true) }

                list.add(
                    InstalledAppItem(
                        appName = label,
                        packageName = pkg.packageName,
                        versionName = pkg.versionName ?: "1.0",
                        isSystemApp = isSystem,
                        permissionCount = perms.size,
                        hasLocationPermission = hasLoc,
                        hasCameraPermission = hasCam,
                        hasMicPermission = hasMic
                    )
                )
            }
        } catch (e: Exception) {
            // gracefully catch
        }
        return list.sortedBy { it.appName.lowercase() }
    }
}
