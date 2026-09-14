package com.paliaapk.phonefix.diagnostics

import android.content.Context
import android.provider.Settings
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType

class AodDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.DISPLAY_AOD

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            // Check DOZE_ENABLED / AOD where accessible
            val dozeEnabled = try {
                Settings.Secure.getInt(context.contentResolver, "doze_enabled", -1)
            } catch (e: Exception) {
                -1
            }

            val dozeAlwaysOn = try {
                Settings.Secure.getInt(context.contentResolver, "doze_always_on", -1)
            } catch (e: Exception) {
                -1
            }

            val dozePulseOnPickUp = try {
                Settings.Secure.getInt(context.contentResolver, "doze_pulse_on_pick_up", -1)
            } catch (e: Exception) {
                -1
            }

            val dozePulseOnDoubleTap = try {
                Settings.Secure.getInt(context.contentResolver, "doze_pulse_on_double_tap", -1)
            } catch (e: Exception) {
                -1
            }

            // Always On Display setting
            if (dozeAlwaysOn == 1) {
                results.add(
                    DiagnosticResult(
                        id = "aod_always_on_active",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.INFO,
                        title = "Always-On Display (AOD): Active",
                        description = "Always-On Display is currently enabled. On AMOLED panels this uses low-power display modes to show clock and notifications.",
                        evidence = "doze_always_on = 1",
                        recommendation = "If experiencing unexpected screen wake-ups in pocket, run the AOD / Screen Wake Test.",
                        fixActionType = FixActionType.OPEN_AOD_SETTINGS
                    )
                )
            } else if (dozeAlwaysOn == 0) {
                results.add(
                    DiagnosticResult(
                        id = "aod_always_on_disabled",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.HEALTHY,
                        title = "Always-On Display: Standby / Disabled",
                        description = "Always-On ambient screen is disabled or set to show on tap, conserving standby power.",
                        evidence = "doze_always_on = 0",
                        recommendation = "No unexpected idle wake battery consumption.",
                        fixActionType = FixActionType.OPEN_AOD_SETTINGS
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        id = "aod_oem_managed",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.INFO,
                        title = "AOD Configured via OEM Layer",
                        description = "Manufacturer specific Always-On Display layer active (OnePlus/OxygenOS, Samsung OneUI, or HyperOS).",
                        evidence = "AOD setting managed in vendor subsystem",
                        recommendation = "You can inspect wake behavior via PHONEFIX AOD Screen Wake Test.",
                        fixActionType = FixActionType.OPEN_AOD_SETTINGS
                    )
                )
            }

            // Wake gestures: Lift to check
            if (dozePulseOnPickUp == 1) {
                results.add(
                    DiagnosticResult(
                        id = "aod_lift_wake_active",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.INFO,
                        title = "Lift-To-Wake Gesture: Enabled",
                        description = "Accelerometer monitors for phone pickup motions to illuminate display.",
                        evidence = "doze_pulse_on_pick_up = 1",
                        recommendation = "Disable if phone accidentally wakes up while walking or cycling.",
                        fixActionType = FixActionType.OPEN_DISPLAY_SETTINGS
                    )
                )
            }

            // Wake gestures: Double tap
            if (dozePulseOnDoubleTap == 1) {
                results.add(
                    DiagnosticResult(
                        id = "aod_tap_wake_active",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.INFO,
                        title = "Tap-To-Wake Gesture: Enabled",
                        description = "Digitizer continuously scans for double taps while screen is off.",
                        evidence = "doze_pulse_on_double_tap = 1",
                        recommendation = "Operating normally.",
                        fixActionType = FixActionType.OPEN_DISPLAY_SETTINGS
                    )
                )
            }

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "aod_telemetry_restricted",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "AOD Settings Protected",
                    description = "Android does not allow PHONEFIX to access this information directly without vendor framework permissions.",
                    evidence = "Settings.Secure read restricted",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device.",
                    fixActionType = FixActionType.OPEN_AOD_SETTINGS
                )
            )
        }

        return results
    }
}
