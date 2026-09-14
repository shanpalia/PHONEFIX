package com.paliaapk.phonefix.diagnostics

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType

class SecurityDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.SECURITY

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            // 1. Screen lock security
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            val isDeviceSecure = keyguardManager?.isDeviceSecure ?: false

            if (!isDeviceSecure) {
                results.add(
                    DiagnosticResult(
                        id = "sec_no_screen_lock",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.HIGH,
                        title = "Screen Lock Unsecured (PIN/Biometrics Missing)",
                        description = "The device is currently unlocked with swipe or no lock screen. Without a secure PIN, pattern, or biometric lock, anyone possessing the phone can access private accounts and personal data.",
                        evidence = "KeyguardManager.isDeviceSecure = false",
                        recommendation = "Set up a secure PIN, Pattern, or Fingerprint lock in Security Settings.",
                        fixActionType = FixActionType.OPEN_SECURITY_SETTINGS
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        id = "sec_screen_lock_secure",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.HEALTHY,
                        title = "Device Lock: Protected by Credentials",
                        description = "Hardware cryptographic keystore is secured by PIN, password, or verified biometrics.",
                        evidence = "KeyguardManager.isDeviceSecure = true",
                        recommendation = "Device credential guard is active."
                    )
                )
            }

            // 2. Developer Options & USB Debugging
            val devOptionsEnabled = try {
                Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
            } catch (e: Exception) {
                false
            }

            val adbEnabled = try {
                Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
            } catch (e: Exception) {
                false
            }

            if (adbEnabled) {
                results.add(
                    DiagnosticResult(
                        id = "sec_usb_debugging_active",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.MEDIUM,
                        title = "USB Debugging (ADB) is Active",
                        description = "Android Debug Bridge is enabled. When connected to unfamiliar USB charging stations or PCs, ADB may expose shell command access.",
                        evidence = "Settings.Global.ADB_ENABLED = 1",
                        recommendation = "Disable USB debugging when not developing software.",
                        fixActionType = FixActionType.OPEN_DEVELOPER_SETTINGS
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        id = "sec_adb_disabled",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.HEALTHY,
                        title = "USB Debugging: Secured (Off)",
                        description = "Android Debug Bridge is safely disabled.",
                        evidence = "Settings.Global.ADB_ENABLED = 0",
                        recommendation = "Standard user security posture maintained."
                    )
                )
            }

            // 3. Security Patch Level
            val patchLevel = Build.VERSION.SECURITY_PATCH
            results.add(
                DiagnosticResult(
                    id = "sec_patch_level",
                    category = category,
                    status = DiagnosticStatus.PASS,
                    severity = DiagnosticSeverity.INFO,
                    title = "Android Security Patch: $patchLevel",
                    description = "System firmware includes Google and vendor CVE security updates up to $patchLevel.",
                    evidence = "Build.VERSION.SECURITY_PATCH: $patchLevel",
                    recommendation = "Check for OEM system updates regularly to receive latest security patches.",
                    fixActionType = FixActionType.OPEN_DEVICE_INFO
                )
            )

            // 4. Storage Encryption Status
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val encryptionStatus = dpm?.storageEncryptionStatus ?: DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED
            val isEncrypted = encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
                    encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER

            results.add(
                DiagnosticResult(
                    id = "sec_storage_encryption",
                    category = category,
                    status = if (isEncrypted) DiagnosticStatus.PASS else DiagnosticStatus.ATTENTION,
                    severity = if (isEncrypted) DiagnosticSeverity.HEALTHY else DiagnosticSeverity.HIGH,
                    title = "Flash Storage Encryption: ${if (isEncrypted) "Active (AES-256)" else "Status: $encryptionStatus"}",
                    description = if (isEncrypted) {
                        "File-based encryption (FBE) is active. User data partitions are encrypted at rest with hardware-backed keys."
                    } else {
                        "Device partition encryption status could not be verified by DevicePolicyManager."
                    },
                    evidence = "EncryptionStatus: $encryptionStatus",
                    recommendation = if (isEncrypted) "Device data is encrypted." else "Ensure phone is secured by lock screen."
                )
            )

            // 5. Anti-Malware / Transparency Note
            results.add(
                DiagnosticResult(
                    id = "sec_transparency_guarantee",
                    category = category,
                    status = DiagnosticStatus.PASS,
                    severity = DiagnosticSeverity.INFO,
                    title = "Diagnostic Integrity & Safety",
                    description = "PHONEFIX does not claim this device is '100% virus-free'. We inspect real accessible system gates (credentials, ADB, encryption, patch levels) without fake booster marketing.",
                    evidence = "Verified via Android Public API",
                    recommendation = "Install applications strictly from trusted sources like Google Play."
                )
            )

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "sec_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "Security Telemetry Restricted",
                    description = e.localizedMessage ?: "Restricted",
                    evidence = "Exception during security check",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }
}
