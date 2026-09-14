package com.paliaapk.phonefix

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paliaapk.phonefix.ui.screens.AppsManagerScreen
import com.paliaapk.phonefix.ui.screens.DeadPixelColorTestScreen
import com.paliaapk.phonefix.ui.screens.DisplayAodScreen
import com.paliaapk.phonefix.ui.screens.FixProblemsScreen
import com.paliaapk.phonefix.ui.screens.HardwareTestScreen
import com.paliaapk.phonefix.ui.screens.HomeScreen
import com.paliaapk.phonefix.ui.screens.PrivacyScreen
import com.paliaapk.phonefix.ui.screens.ReportScreen
import com.paliaapk.phonefix.ui.screens.ScanScreen
import com.paliaapk.phonefix.ui.screens.SmartDiagnosisScreen
import com.paliaapk.phonefix.ui.screens.SplashScreen
import com.paliaapk.phonefix.ui.screens.ToolsScreen
import com.paliaapk.phonefix.ui.screens.TouchscreenTestScreen
import com.paliaapk.phonefix.ui.theme.BrandNavyDark
import com.paliaapk.phonefix.ui.theme.PhoneFixTheme
import com.paliaapk.phonefix.viewmodel.AppScreen
import com.paliaapk.phonefix.viewmodel.PhoneFixViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setStatusBarColor(android.graphics.Color.WHITE)
        window.setNavigationBarColor(android.graphics.Color.WHITE)
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).hide(
            androidx.core.view.WindowInsetsCompat.Type.statusBars()
        )
        setContent {
            PhoneFixTheme {
                val viewModel: PhoneFixViewModel = viewModel()
                PhoneFixApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PhoneFixApp(viewModel: PhoneFixViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("phonefix_preferences", android.content.Context.MODE_PRIVATE)
        if (!prefs.getBoolean("initial_permissions_prompted", false)) {
            prefs.edit().putBoolean("initial_permissions_prompted", true).apply()
            val requested = listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).filter { androidx.core.content.ContextCompat.checkSelfPermission(context, it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
            if (requested.isNotEmpty()) permissionLauncher.launch(requested.toTypedArray())
        }
    }
    val currentScreen by viewModel.currentScreen.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val currentTest by viewModel.currentScanTest.collectAsState()
    val allResults by viewModel.allResults.collectAsState()
    val healthScore by viewModel.healthScoreReport.collectAsState()
    val smartCases by viewModel.smartCases.collectAsState()

    // Back button handling
    if (currentScreen != AppScreen.HOME && currentScreen != AppScreen.SPLASH) {
        BackHandler {
            if (currentScreen == AppScreen.HARDWARE_TOUCH_TEST || currentScreen == AppScreen.HARDWARE_DISPLAY_COLOR_TEST) {
                viewModel.navigateTo(AppScreen.HARDWARE_TEST)
            } else {
                viewModel.navigateTo(AppScreen.HOME)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandNavyDark)
    ) {
        when (currentScreen) {
            AppScreen.SPLASH -> {
                SplashScreen(
                    onSplashComplete = {
                        viewModel.navigateTo(AppScreen.HOME)
                    }
                )
            }
            AppScreen.HOME -> {
                HomeScreen(
                    viewModel = viewModel,
                    systemInfo = systemInfo,
                    healthScore = healthScore.overallScore,
                    ratingLabel = healthScore.ratingLabel,
                    scoreExplanation = healthScore.explanation,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.SCAN -> {
                ScanScreen(
                    viewModel = viewModel,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    currentTest = currentTest,
                    results = allResults,
                    healthScore = healthScore.overallScore,
                    ratingLabel = healthScore.ratingLabel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.HARDWARE_TEST -> {
                HardwareTestScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.HARDWARE_TOUCH_TEST -> {
                TouchscreenTestScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.HARDWARE_DISPLAY_COLOR_TEST -> {
                DeadPixelColorTestScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.DISPLAY_AOD -> {
                DisplayAodScreen(
                    viewModel = viewModel,
                    systemInfo = systemInfo,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.APPS_MANAGER -> {
                AppsManagerScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.FIX_PROBLEMS -> {
                FixProblemsScreen(
                    viewModel = viewModel,
                    results = allResults,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.SMART_DIAGNOSIS -> {
                SmartDiagnosisScreen(
                    viewModel = viewModel,
                    smartCases = smartCases,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.REPORT -> {
                ReportScreen(
                    viewModel = viewModel,
                    systemInfo = systemInfo,
                    healthScore = healthScore,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.TOOLS -> {
                ToolsScreen(
                    viewModel = viewModel,
                    systemInfo = systemInfo,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            AppScreen.PRIVACY -> {
                PrivacyScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    }
}
