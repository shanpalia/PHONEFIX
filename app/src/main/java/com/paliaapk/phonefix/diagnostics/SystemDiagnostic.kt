package com.paliaapk.phonefix.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.view.WindowManager
import com.paliaapk.phonefix.model.SystemInfoData
import java.io.File

class SystemDiagnostic {

    fun gatherSystemInfo(context: Context): SystemInfoData {
        // Device info
        val model = Build.MODEL ?: "Unknown"
        val manufacturer = Build.MANUFACTURER ?: "Android"
        val brand = Build.BRAND ?: "Generic"
        val board = Build.BOARD ?: "Generic"
        val hardware = Build.HARDWARE ?: "Generic"
        val androidVer = Build.VERSION.RELEASE ?: "14"
        val apiLevel = Build.VERSION.SDK_INT
        val securityPatch = Build.VERSION.SECURITY_PATCH ?: "N/A"
        val buildId = Build.DISPLAY ?: Build.ID ?: ""
        val uptimeHours = "${SystemClock.elapsedRealtime() / (1000 * 60 * 60)}h ${((SystemClock.elapsedRealtime() / (1000 * 60)) % 60)}m"

        // RAM info
        var totalRam = 0L
        var availRam = 0L
        var isLowMem = false
        var ramThreshold = 0L
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            if (actManager != null) {
                actManager.getMemoryInfo(memInfo)
                totalRam = memInfo.totalMem
                availRam = memInfo.availMem
                isLowMem = memInfo.lowMemory
                ramThreshold = memInfo.threshold
            }
        } catch (e: Exception) {
            // ignore
        }

        // Storage info
        var totalStorage = 0L
        var freeStorage = 0L
        var usedStorage = 0L
        try {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            totalStorage = totalBlocks * blockSize
            freeStorage = availableBlocks * blockSize
            usedStorage = (totalStorage - freeStorage).coerceAtLeast(0L)
        } catch (e: Exception) {
            // ignore
        }

        // Battery info
        var batteryPct = -1
        var isCharging = false
        var chargePlug = "Discharging"
        var batteryHealth = "Good"
        var batteryTempC = 0f
        var batteryVoltageMv = 0
        var batteryTech = "Li-ion"
        var isPowerSave = false
        try {
            val batteryStatus: Intent? = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryPct = ((level / scale.toFloat()) * 100).toInt()
                }

                val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                chargePlug = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charger"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> "Battery Discharging"
                }

                val health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                batteryHealth = when (health) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Damaged"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    else -> "Normal"
                }

                batteryTempC = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
                batteryVoltageMv = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                batteryTech = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
            }

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            isPowerSave = powerManager?.isPowerSaveMode ?: false
        } catch (e: Exception) {
            // ignore
        }

        // Display info
        var res = "1080x2400"
        var refresh = 60f
        var density = 420
        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val display = wm?.defaultDisplay
            if (display != null) {
                refresh = display.refreshRate
                val metrics = context.resources.displayMetrics
                density = metrics.densityDpi
                res = "${metrics.widthPixels} x ${metrics.heightPixels}"
            }
        } catch (e: Exception) {
            // ignore
        }

        // CPU info
        val cores = Runtime.getRuntime().availableProcessors()
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

        // Network info
        var isWifi = false
        var isCellular = false
        var isVpn = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val net = cm?.activeNetwork
            val caps = if (net != null) cm.getNetworkCapabilities(net) else null
            if (caps != null) {
                isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            }
        } catch (e: Exception) {
            // ignore
        }

        return SystemInfoData(
            deviceModel = model,
            manufacturer = manufacturer,
            brand = brand,
            board = board,
            hardware = hardware,
            androidVersion = androidVer,
            apiLevel = apiLevel,
            securityPatch = securityPatch,
            buildId = buildId,
            uptimeHours = uptimeHours,
            totalRamBytes = totalRam,
            availRamBytes = availRam,
            isLowMemory = isLowMem,
            ramThresholdBytes = ramThreshold,
            totalStorageBytes = totalStorage,
            freeStorageBytes = freeStorage,
            usedStorageBytes = usedStorage,
            batteryPct = batteryPct,
            isCharging = isCharging,
            chargePlug = chargePlug,
            batteryHealth = batteryHealth,
            batteryTempC = batteryTempC,
            batteryVoltageMv = batteryVoltageMv,
            batteryTechnology = batteryTech,
            isPowerSaveMode = isPowerSave,
            screenResolution = res,
            refreshRate = refresh,
            screenDensityDpi = density,
            cpuCores = cores,
            cpuAbi = abi,
            isWifiConnected = isWifi,
            isCellularConnected = isCellular,
            isVpnActive = isVpn
        )
    }
}
