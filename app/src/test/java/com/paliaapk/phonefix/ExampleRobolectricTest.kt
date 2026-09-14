package com.paliaapk.phonefix

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.paliaapk.phonefix.diagnostics.HealthScoreCalculator
import com.paliaapk.phonefix.diagnostics.SmartDiagnosisEngine
import com.paliaapk.phonefix.diagnostics.SystemDiagnostic
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun verifyAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PHONEFIX", appName)
    }

    @Test
    fun verifyHealthScoreCalculation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val systemInfo = SystemDiagnostic().gatherSystemInfo(context)
        val results = listOf(
            DiagnosticResult(
                id = "battery_1",
                category = DiagnosticCategory.BATTERY,
                status = DiagnosticStatus.PASS,
                severity = DiagnosticSeverity.HEALTHY,
                title = "Battery Health",
                description = "Healthy",
                evidence = "95% capacity",
                recommendation = ""
            ),
            DiagnosticResult(
                id = "storage_1",
                category = DiagnosticCategory.STORAGE,
                status = DiagnosticStatus.PASS,
                severity = DiagnosticSeverity.HEALTHY,
                title = "Storage Free",
                description = "Optimal",
                evidence = "45% free",
                recommendation = ""
            )
        )
        val calculator = HealthScoreCalculator()
        val report = calculator.calculateScore(results, systemInfo)
        assertTrue(report.overallScore >= 90)
        assertEquals(2, report.validChecksCount)
    }

    @Test
    fun verifySmartDiagnosisEngineRule() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val systemInfo = SystemDiagnostic().gatherSystemInfo(context).copy(
            totalStorageBytes = 100_000_000_000L,
            usedStorageBytes = 95_000_000_000L,
            freeStorageBytes = 5_000_000_000L
        )
        val results = listOf(
            DiagnosticResult(
                id = "storage_full",
                category = DiagnosticCategory.STORAGE,
                status = DiagnosticStatus.FAILED,
                severity = DiagnosticSeverity.CRITICAL,
                title = "Internal Storage",
                description = "95% storage occupied",
                evidence = "Less than 2GB free",
                recommendation = "Clean up space"
            )
        )
        val engine = SmartDiagnosisEngine()
        val cases = engine.analyze(results, systemInfo, null)
        assertTrue(cases.any { it.problemTitle.contains("Critical Storage Depletion") })
    }

    @Test
    fun verifySystemDiagnosticExtraction() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val info = SystemDiagnostic().gatherSystemInfo(context)
        assertNotNull(info.androidVersion)
        assertTrue(info.totalStorageBytes >= 0L)
        assertTrue(info.totalRamBytes >= 0L)
    }
}
