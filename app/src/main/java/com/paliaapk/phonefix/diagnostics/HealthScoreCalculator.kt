package com.paliaapk.phonefix.diagnostics

import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.SystemInfoData

data class HealthScoreReport(
    val overallScore: Int,
    val ratingLabel: String, // "Excellent", "Good Condition", "Needs Attention", "Critical"
    val validChecksCount: Int,
    val passedCount: Int,
    val attentionCount: Int,
    val failedCount: Int,
    val explanation: String
)

class HealthScoreCalculator {

    fun calculateScore(results: List<DiagnosticResult>, systemInfo: SystemInfoData): HealthScoreReport {
        if (results.isEmpty()) {
            // Baseline before full scan, based on initial system info
            var score = 100
            var checks = 0

            // Battery check
            if (systemInfo.batteryPct in 0..15 && !systemInfo.isCharging) {
                score -= 10
            }
            if (systemInfo.batteryTempC >= 43.0f) {
                score -= 20
            } else if (systemInfo.batteryTempC >= 39.0f) {
                score -= 8
            }
            checks++

            // Storage check
            if (systemInfo.totalStorageBytes > 0) {
                checks++
                when {
                    systemInfo.storageUsedPercent >= 92 -> score -= 30
                    systemInfo.storageUsedPercent >= 85 -> score -= 15
                    systemInfo.storageUsedPercent >= 75 -> score -= 5
                }
            }

            // RAM check
            if (systemInfo.totalRamBytes > 0) {
                checks++
                if (systemInfo.isLowMemory) score -= 20
                else if (systemInfo.ramUsedPercent >= 90) score -= 10
            }

            val finalScore = score.coerceIn(10, 100)
            val rating = when {
                finalScore >= 90 -> "Excellent"
                finalScore >= 75 -> "Good Condition"
                finalScore >= 50 -> "Needs Attention"
                else -> "Critical"
            }

            return HealthScoreReport(
                overallScore = finalScore,
                ratingLabel = rating,
                validChecksCount = checks,
                passedCount = checks,
                attentionCount = 0,
                failedCount = 0,
                explanation = "Your initial health score is based on $checks core system measurements."
            )
        }

        // Full Scan completed calculation based on actual diagnostic findings
        val scannableResults = results.filter { it.status != DiagnosticStatus.SKIPPED && it.status != DiagnosticStatus.UNAVAILABLE }
        val validChecks = scannableResults.size
        val passed = scannableResults.count { it.status == DiagnosticStatus.PASS }
        val attention = scannableResults.count { it.status == DiagnosticStatus.ATTENTION }
        val failed = scannableResults.count { it.status == DiagnosticStatus.FAILED }

        // Start from 100 and deduct according to confirmed problem severities
        var score = 100
        for (item in scannableResults) {
            when (item.severity) {
                DiagnosticSeverity.CRITICAL -> score -= 22
                DiagnosticSeverity.HIGH -> score -= 14
                DiagnosticSeverity.MEDIUM -> score -= 6
                DiagnosticSeverity.INFO -> score -= 0
                DiagnosticSeverity.HEALTHY -> score += 0
            }
        }

        val clampedScore = score.coerceIn(15, 100)
        val rating = when {
            clampedScore >= 90 -> "Excellent"
            clampedScore >= 75 -> "Good Condition"
            clampedScore >= 50 -> "Needs Attention"
            else -> "Critical"
        }

        return HealthScoreReport(
            overallScore = clampedScore,
            ratingLabel = rating,
            validChecksCount = validChecks,
            passedCount = passed,
            attentionCount = attention,
            failedCount = failed,
            explanation = "Your score is based on $validChecks verified device hardware and OS checks ($passed passed, $attention attention, $failed critical/defects)."
        )
    }
}
