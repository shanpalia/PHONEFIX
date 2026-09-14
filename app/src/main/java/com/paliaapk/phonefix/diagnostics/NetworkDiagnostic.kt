package com.paliaapk.phonefix.diagnostics

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType

class NetworkDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.NETWORK

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            val capabilities = if (activeNetwork != null) cm.getNetworkCapabilities(activeNetwork) else null

            val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            val isValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
            val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val isCellular = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
            val isVpn = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true ||
                    capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN) == false

            // Internet Connectivity
            if (hasInternet && isValidated) {
                val transportName = when {
                    isWifi -> "Wi-Fi"
                    isCellular -> "Mobile Cellular Data"
                    else -> "Active Interface"
                }
                results.add(
                    DiagnosticResult(
                        id = "net_internet_validated",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.HEALTHY,
                        title = "Internet Gateway Reachable ($transportName)",
                        description = "Network interface has active IP routing with verified DNS packet validation.",
                        evidence = "Transport: $transportName, Validated: true, VPN: $isVpn",
                        recommendation = "Data connection is stable."
                    )
                )
            } else if (hasInternet) {
                results.add(
                    DiagnosticResult(
                        id = "net_internet_captive",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.HIGH,
                        title = "Internet Connection Unvalidated",
                        description = "Connected to network, but DNS packet validation failed. You may be on a captive portal (hotel/cafe login) or experiencing DNS timeout.",
                        evidence = "NET_CAPABILITY_VALIDATED is false",
                        recommendation = "Open browser to verify captive portal login or toggle Airplane mode.",
                        fixActionType = FixActionType.OPEN_WIFI_SETTINGS
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        id = "net_no_connection",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.MEDIUM,
                        title = "No Active Internet Connection",
                        description = "Neither Wi-Fi nor Mobile Cellular Data is currently connected.",
                        evidence = "Active network is null",
                        recommendation = "Connect to Wi-Fi or enable Mobile Data in Quick Settings.",
                        fixActionType = FixActionType.OPEN_WIFI_SETTINGS
                    )
                )
            }

            // Wi-Fi details
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val isWifiEnabled = wifiManager?.isWifiEnabled ?: false
            results.add(
                DiagnosticResult(
                    id = "net_wifi_state",
                    category = category,
                    status = DiagnosticStatus.PASS,
                    severity = DiagnosticSeverity.HEALTHY,
                    title = "Wi-Fi Adapter: ${if (isWifiEnabled) "Enabled" else "Disabled"}",
                    description = if (isWifiEnabled) {
                        "Wi-Fi radio is active. ${if (isWifi) "Connected to wireless access point." else "Scanning for known networks."}"
                    } else {
                        "Wi-Fi radio is turned off."
                    },
                    evidence = "Wi-Fi radio status: $isWifiEnabled",
                    recommendation = if (!isWifiEnabled && !isCellular) "Enable Wi-Fi to avoid cellular data charges." else "Operating normally.",
                    fixActionType = FixActionType.OPEN_WIFI_SETTINGS
                )
            )

            // Bluetooth state
            try {
                val btAdapter = BluetoothAdapter.getDefaultAdapter()
                val isBtEnabled = btAdapter?.isEnabled ?: false
                results.add(
                    DiagnosticResult(
                        id = "net_bluetooth_state",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.INFO,
                        title = "Bluetooth Radio: ${if (isBtEnabled) "Active" else "Off"}",
                        description = if (isBtEnabled) {
                            "Bluetooth controller is turned on for wireless audio, watches, or accessories."
                        } else {
                            "Bluetooth controller is turned off."
                        },
                        evidence = "Bluetooth enabled: $isBtEnabled",
                        recommendation = if (isBtEnabled) "Turn off when not in use to conserve battery." else "Normal.",
                        fixActionType = FixActionType.OPEN_BLUETOOTH_SETTINGS
                    )
                )
            } catch (e: Exception) {
                // Bluetooth permission on Android 12+
            }

            // VPN detection
            if (isVpn) {
                results.add(
                    DiagnosticResult(
                        id = "net_vpn_active",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.INFO,
                        title = "VPN Tunnel Active",
                        description = "An encrypted virtual private network tunnel is routing network traffic.",
                        evidence = "Transport: VPN",
                        recommendation = "Ensure trusted VPN provider if experiencing unexpected latency."
                    )
                )
            }

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "net_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "Network Telemetry Restricted",
                    description = e.localizedMessage ?: "Restricted",
                    evidence = "Exception in NetworkDiagnostic",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }
}
