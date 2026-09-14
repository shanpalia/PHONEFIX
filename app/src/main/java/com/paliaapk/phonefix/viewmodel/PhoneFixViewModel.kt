package com.paliaapk.phonefix.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paliaapk.phonefix.data.PhoneFixDatabase
import com.paliaapk.phonefix.data.ReportRepository
import com.paliaapk.phonefix.data.ScanReportEntity
import com.paliaapk.phonefix.diagnostics.AodDiagnostic
import com.paliaapk.phonefix.diagnostics.AodWakeMonitor
import com.paliaapk.phonefix.diagnostics.AppDiagnostic
import com.paliaapk.phonefix.diagnostics.BatteryDiagnostic
import com.paliaapk.phonefix.diagnostics.DisplayDiagnostic
import com.paliaapk.phonefix.diagnostics.HardwareTestController
import com.paliaapk.phonefix.diagnostics.HealthScoreCalculator
import com.paliaapk.phonefix.diagnostics.HealthScoreReport
import com.paliaapk.phonefix.diagnostics.MemoryDiagnostic
import com.paliaapk.phonefix.diagnostics.NetworkDiagnostic
import com.paliaapk.phonefix.diagnostics.PerformanceDiagnostic
import com.paliaapk.phonefix.diagnostics.SecurityDiagnostic
import com.paliaapk.phonefix.diagnostics.SensorDiagnostic
import com.paliaapk.phonefix.diagnostics.SmartDiagnosisCase
import com.paliaapk.phonefix.diagnostics.SmartDiagnosisEngine
import com.paliaapk.phonefix.diagnostics.StorageDiagnostic
import com.paliaapk.phonefix.diagnostics.SystemDiagnostic
import com.paliaapk.phonefix.diagnostics.TemperatureDiagnostic
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType
import com.paliaapk.phonefix.model.SystemInfoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppScreen {
    SPLASH,
    HOME,
    SCAN,
    HARDWARE_TEST,
    HARDWARE_TOUCH_TEST,
    HARDWARE_DISPLAY_COLOR_TEST,
    DISPLAY_AOD,
    APPS_MANAGER,
    FIX_PROBLEMS,
    SMART_DIAGNOSIS,
    REPORT,
    TOOLS,
    PRIVACY
}

class PhoneFixViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PhoneFixDatabase.getInstance(application)
    val reportRepository = ReportRepository(db.scanReportDao())

    val hardwareController = HardwareTestController(application)
    val aodWakeMonitor = AodWakeMonitor(application)
    private val systemDiagnostic = SystemDiagnostic()
    private val smartEngine = SmartDiagnosisEngine()
    private val scoreCalculator = HealthScoreCalculator()

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // System Info
    private val _systemInfo = MutableStateFlow(SystemInfoData())
    val systemInfo: StateFlow<SystemInfoData> = _systemInfo.asStateFlow()

    // Diagnostic Results
    private val _allResults = MutableStateFlow<List<DiagnosticResult>>(emptyList())
    val allResults: StateFlow<List<DiagnosticResult>> = _allResults.asStateFlow()

    // Smart Cases
    private val _smartCases = MutableStateFlow<List<SmartDiagnosisCase>>(emptyList())
    val smartCases: StateFlow<List<SmartDiagnosisCase>> = _smartCases.asStateFlow()

    // Scan progress
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _currentScanTest = MutableStateFlow("Ready to Scan")
    val currentScanTest: StateFlow<String> = _currentScanTest.asStateFlow()

    // Health Score
    private val _healthScoreReport = MutableStateFlow(
        HealthScoreReport(
            overallScore = 95,
            ratingLabel = "Good Condition",
            validChecksCount = 4,
            passedCount = 4,
            attentionCount = 0,
            failedCount = 0,
            explanation = "Initial baseline telemetry based on accessible hardware parameters."
        )
    )
    val healthScoreReport: StateFlow<HealthScoreReport> = _healthScoreReport.asStateFlow()

    // Scan History
    val scanHistory: StateFlow<List<ScanReportEntity>> = reportRepository.allReports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshSystemInfo()
    }

    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.HARDWARE_TEST) {
            hardwareController.startSensorMonitoring()
        } else if (_currentScreen.value == AppScreen.HARDWARE_TEST && screen != AppScreen.HARDWARE_TOUCH_TEST && screen != AppScreen.HARDWARE_DISPLAY_COLOR_TEST) {
            hardwareController.stopSensorMonitoring()
        }
        _currentScreen.value = screen
    }

    fun refreshSystemInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val info = systemDiagnostic.gatherSystemInfo(getApplication())
            _systemInfo.value = info
            if (_allResults.value.isEmpty()) {
                val initialScore = scoreCalculator.calculateScore(emptyList(), info)
                _healthScoreReport.value = initialScore
            }
        }
    }

    // --- FULL SYSTEM SCAN ---
    fun startFullSystemScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        _scanProgress.value = 0f
        _currentScreen.value = AppScreen.SCAN

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val collectedResults = mutableListOf<DiagnosticResult>()

            data class ScanStep(val label: String, val action: suspend () -> List<DiagnosticResult>)
            val scanSteps = listOf(
                ScanStep("Checking Battery...") { BatteryDiagnostic().inspect(context) },
                ScanStep("Checking Storage...") { StorageDiagnostic().inspect(context) },
                ScanStep("Checking Memory...") { MemoryDiagnostic().inspect(context) },
                ScanStep("Checking Performance & CPU...") { PerformanceDiagnostic().inspect(context) },
                ScanStep("Checking Temperature...") { TemperatureDiagnostic().inspect(context) },
                ScanStep("Checking Network & Wireless...") { NetworkDiagnostic().inspect(context) },
                ScanStep("Checking Apps...") { AppDiagnostic().inspect(context) },
                ScanStep("Checking Display...") { DisplayDiagnostic().inspect(context) },
                ScanStep("Checking AOD...") { AodDiagnostic().inspect(context) },
                ScanStep("Checking Sensors...") { SensorDiagnostic().inspect(context) },
                ScanStep("Checking Security...") { SecurityDiagnostic().inspect(context) }
            )

            val totalSteps = scanSteps.size
            for ((index, step) in scanSteps.withIndex()) {
                _currentScanTest.value = step.label
                _scanProgress.value = (index.toFloat() / totalSteps)
                delay(350) // Non-blocking smooth animation step

                try {
                    val stepResults = step.action()
                    collectedResults.addAll(stepResults)
                } catch (e: Exception) {
                    // Handled gracefully without crash
                }
            }

            _scanProgress.value = 1.0f
            _currentScanTest.value = "Scan Complete"
            delay(300)

            _allResults.value = collectedResults

            // Update System Info
            val updatedInfo = systemDiagnostic.gatherSystemInfo(context)
            _systemInfo.value = updatedInfo

            // Compute score
            val report = scoreCalculator.calculateScore(collectedResults, updatedInfo)
            _healthScoreReport.value = report

            // Run Smart Diagnosis Correlation
            val smart = smartEngine.analyze(collectedResults, updatedInfo, aodWakeMonitor.sessionState.value)
            _smartCases.value = smart

            // Persist report in Room
            val entity = ScanReportEntity(
                healthScore = report.overallScore,
                scoreRating = report.ratingLabel,
                checksCount = report.validChecksCount,
                passedCount = report.passedCount,
                attentionCount = report.attentionCount,
                failedCount = report.failedCount,
                batteryPct = updatedInfo.batteryPct,
                storageUsedPct = updatedInfo.storageUsedPercent,
                ramUsedPct = updatedInfo.ramUsedPercent,
                tempC = updatedInfo.batteryTempC,
                summaryNotes = report.explanation
            )
            reportRepository.saveReport(entity)

            _isScanning.value = false
        }
    }

    // --- Action execution ---
    fun executeFixAction(context: Context, action: FixActionType, targetPackage: String? = null) {
        try {
            when (action) {
                FixActionType.OPEN_STORAGE_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    }
                }
                FixActionType.OPEN_BATTERY_SETTINGS -> {
                    val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    }
                }
                FixActionType.OPEN_BATTERY_SAVER_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    }
                }
                FixActionType.OPEN_DISPLAY_SETTINGS, FixActionType.OPEN_AOD_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
                FixActionType.OPEN_WIFI_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
                FixActionType.OPEN_BLUETOOTH_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
                FixActionType.OPEN_SECURITY_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
                FixActionType.OPEN_DEVICE_INFO -> {
                    val intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
                FixActionType.OPEN_DEVELOPER_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    }
                }
                FixActionType.OPEN_APP_SETTINGS -> {
                    if (targetPackage != null) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", targetPackage, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                }
                FixActionType.OPEN_NOTIFICATION_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    }
                }
                FixActionType.RUN_INTERNAL_CLEANUP -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        val cacheDir = context.cacheDir
                        var freedBytes = 0L
                        cacheDir.walkBottomUp().forEach { file ->
                            if (file != cacheDir) {
                                val len = file.length()
                                if (file.delete()) freedBytes += len
                            }
                        }
                        val freedMb = freedBytes / (1024.0 * 1024.0)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Safely freed ${"%.2f".format(freedMb)} MB of temporary cache", Toast.LENGTH_SHORT).show()
                        }
                        refreshSystemInfo()
                    }
                }
                FixActionType.TEST_AGAIN -> {
                    startFullSystemScan()
                }
                FixActionType.NONE -> {}
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open system screen: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun markIssueFixed(id: String) {
        val list = _allResults.value.map {
            if (it.id == id) it.copy(isFixed = true, status = DiagnosticStatus.PASS, severity = DiagnosticSeverity.HEALTHY) else it
        }
        _allResults.value = list
        _healthScoreReport.value = scoreCalculator.calculateScore(list, _systemInfo.value)
    }

    fun markIssueIgnored(id: String) {
        val list = _allResults.value.map {
            if (it.id == id) it.copy(isIgnored = true) else it
        }
        _allResults.value = list
    }

    fun shareReport(context: Context) {
        val score = _healthScoreReport.value
        val info = _systemInfo.value
        val text = buildString {
            appendLine("═════════════════════════════════════")
            appendLine("       PHONEFIX HEALTH REPORT        ")
            appendLine("         By PaliaAPK HUB             ")
            appendLine("═════════════════════════════════════")
            appendLine("Health Score: ${score.overallScore}/100 (${score.ratingLabel})")
            appendLine("Device: ${info.manufacturer} ${info.deviceModel} (Android ${info.androidVersion})")
            appendLine("Battery: ${info.batteryPct}% (${info.chargePlug}, ${"%.1f".format(info.batteryTempC)}°C)")
            appendLine("Storage: ${info.storageUsedPercent}% used")
            appendLine("RAM: ${info.ramUsedPercent}% used")
            appendLine("Valid Checks: ${score.validChecksCount} (${score.passedCount} Passed, ${score.attentionCount} Attention, ${score.failedCount} Critical)")
            appendLine("─────────────────────────────────────")
            appendLine("Scan • Diagnose • Fix")
            appendLine("Your Phone's Personal Doctor")
            appendLine("Developed by SHANPALIA")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(Intent.createChooser(sendIntent, "Share PHONEFIX Health Report"))
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onCleared() {
        super.onCleared()
        hardwareController.stopSensorMonitoring()
        aodWakeMonitor.stopMonitoring()
    }
}
