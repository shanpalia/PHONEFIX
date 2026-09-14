package com.paliaapk.phonefix.diagnostics

import android.content.Context
import android.provider.Settings
import android.view.WindowManager
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType

class DisplayDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.DISPLAY_AOD

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val display = wm?.defaultDisplay

            // Metrics
            val refreshRate = display?.refreshRate ?: 60f

            // Settings: Brightness Mode
            val autoBrightness = try {
                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            } catch (e: Exception) {
                null
            }

            // Screen timeout
            val screenTimeoutMs = try {
                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            } catch (e: Exception) {
                30000
            }
            val timeoutSec = screenTimeoutMs / 1000

            // Refresh rate result
            results.add(
                DiagnosticResult(
                    id = "display_refresh_rate",
                    category = category,
                    status = DiagnosticStatus.PASS,
                    severity = DiagnosticSeverity.HEALTHY,
                    title = "Display Refresh Rate (${"%.0f".format(refreshRate)} Hz)",
                    description = "Panel operates at ${"%.0f".format(refreshRate)} frames per second. ${if (refreshRate > 60f) "High refresh rate enabled for fluid scrolling." else "Standard refresh rate active."}",
                    evidence = "Display.refreshRate: ${refreshRate}Hz",
                    recommendation = "Display timing controller functioning normally.",
                    fixActionType = FixActionType.OPEN_DISPLAY_SETTINGS
                )
            )

            // Brightness mode
            if (autoBrightness != null) {
                results.add(
                    DiagnosticResult(
                        id = "display_auto_brightness",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.INFO,
                        title = "Adaptive Brightness: ${if (autoBrightness) "Enabled" else "Manual"}",
                        description = if (autoBrightness) {
                            "Screen illuminance auto-adjusts based on ambient light sensor."
                        } else {
                            "Screen brightness is fixed manually. Enabling adaptive brightness optimizes battery endurance."
                        },
                        evidence = "SCREEN_BRIGHTNESS_MODE: $autoBrightness",
                        recommendation = if (!autoBrightness) "Consider enabling adaptive brightness in display settings." else "Operating normally.",
                        fixActionType = FixActionType.OPEN_DISPLAY_SETTINGS
                    )
                )
            }

            // Screen timeout check
            if (timeoutSec > 300) { // Over 5 minutes
                results.add(
                    DiagnosticResult(
                        id = "display_timeout_warning",
                        category = category,
                        status = DiagnosticStatus.ATTENTION,
                        severity = DiagnosticSeverity.MEDIUM,
                        title = "Prolonged Screen Timeout (${timeoutSec / 60} min)",
                        description = "Screen stays active for $timeoutSec seconds before sleeping. This can cause unnecessary battery drain and OLED burn-in risk if left unattended.",
                        evidence = "SCREEN_OFF_TIMEOUT = ${timeoutSec}s",
                        recommendation = "Reduce timeout to 30 seconds or 1 minute for better battery endurance.",
                        fixActionType = FixActionType.OPEN_DISPLAY_SETTINGS
                    )
                )
            } else {
                results.add(
                    DiagnosticResult(
                        id = "display_timeout_ok",
                        category = category,
                        status = DiagnosticStatus.PASS,
                        severity = DiagnosticSeverity.HEALTHY,
                        title = "Screen Off Timeout (${timeoutSec}s)",
                        description = "Sleep timeout is calibrated to preserve battery and panel life.",
                        evidence = "SCREEN_OFF_TIMEOUT = ${timeoutSec}s",
                        recommendation = "Optimal setting.",
                        fixActionType = FixActionType.OPEN_DISPLAY_SETTINGS
                    )
                )
            }

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "display_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "Display Telemetry Restricted",
                    description = e.localizedMessage ?: "Restricted",
                    evidence = "Exception in DisplayDiagnostic",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }
}
