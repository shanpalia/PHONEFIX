package com.paliaapk.phonefix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.model.FixActionType
import com.paliaapk.phonefix.model.SystemInfoData
import com.paliaapk.phonefix.ui.components.PhoneFixBottomNav
import com.paliaapk.phonefix.ui.components.PhoneFixTopBar
import com.paliaapk.phonefix.ui.theme.BrandBlue
import com.paliaapk.phonefix.ui.theme.BrandCyan
import com.paliaapk.phonefix.ui.theme.BrandCyanLight
import com.paliaapk.phonefix.ui.theme.BrandNavyBorder
import com.paliaapk.phonefix.ui.theme.BrandNavyCard
import com.paliaapk.phonefix.ui.theme.BrandNavyCardLight
import com.paliaapk.phonefix.ui.theme.BrandNavyDark
import com.paliaapk.phonefix.ui.theme.BrandTextMuted
import com.paliaapk.phonefix.ui.theme.BrandTextSecondary
import com.paliaapk.phonefix.ui.theme.BrandWhite
import com.paliaapk.phonefix.ui.theme.StatusExcellent
import com.paliaapk.phonefix.viewmodel.AppScreen
import com.paliaapk.phonefix.viewmodel.PhoneFixViewModel

@Composable
fun ToolsScreen(
    viewModel: PhoneFixViewModel,
    systemInfo: SystemInfoData,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = { PhoneFixTopBar(onNavigate = onNavigate) },
        bottomBar = { PhoneFixBottomNav(currentScreen = AppScreen.TOOLS, onNavigate = onNavigate) },
        containerColor = BrandNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("tools_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text("OPTIMIZATION & TOOLS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                    Text("Safe system maintenance without snake-oil RAM killers", fontSize = 12.sp, color = BrandCyanLight)
                }
            }

            // Real facts banner
            item {
                Surface(
                    color = BrandNavyCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BrandNavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PHONEFIX strictly complies with Android architecture: We provide direct paths to native Android storage, battery, display, and cache tools rather than killing system background threads.",
                            fontSize = 11.sp,
                            color = BrandTextSecondary
                        )
                    }
                }
            }

            // Tool 1: Cache & Temp Storage Cleanup
            item {
                ToolActionCard(
                    title = "Safe App Cache Cleanup",
                    description = "Clears sandbox temporary file caches without altering personal data, photos, messages, or accounts.",
                    icon = Icons.Default.CleaningServices,
                    actionText = "Clean Sandbox Cache",
                    onAction = { viewModel.executeFixAction(context, FixActionType.RUN_INTERNAL_CLEANUP) }
                )
            }

            // Tool 2: Storage Manager
            item {
                ToolActionCard(
                    title = "Android Storage Manager",
                    description = "Inspect large media files, downloads, uninstalled residual files, and storage quotas (${systemInfo.storageUsedPercent}% currently used).",
                    icon = Icons.Default.Folder,
                    actionText = "Open Storage Settings",
                    onAction = { viewModel.executeFixAction(context, FixActionType.OPEN_STORAGE_SETTINGS) }
                )
            }

            // Tool 3: Battery Saver
            item {
                ToolActionCard(
                    title = "Battery Saver Mode",
                    description = "Restrict background data and reduce refresh rates when power is below optimal thresholds.",
                    icon = Icons.Default.BatterySaver,
                    actionText = "Configure Battery Saver",
                    onAction = { viewModel.executeFixAction(context, FixActionType.OPEN_BATTERY_SAVER_SETTINGS) }
                )
            }

            // Tool 4: Display Sleep Timeout
            item {
                ToolActionCard(
                    title = "Screen Off Sleep Timeout",
                    description = "Calibrate screen standby inactivity timeout to reduce OLED display power consumption.",
                    icon = Icons.Default.Timer,
                    actionText = "Display Timeout Settings",
                    onAction = { viewModel.executeFixAction(context, FixActionType.OPEN_DISPLAY_SETTINGS) }
                )
            }

            // Tool 5: Network & Wi-Fi Troubleshooting
            item {
                ToolActionCard(
                    title = "Network & Connectivity Reset",
                    description = "Reset wireless routes or troubleshoot captive portal and DNS latency.",
                    icon = Icons.Default.NetworkCheck,
                    actionText = "Network Settings",
                    onAction = { viewModel.executeFixAction(context, FixActionType.OPEN_WIFI_SETTINGS) }
                )
            }

            // Tool 6: Hardware Testing Suite Shortcut
            item {
                ToolActionCard(
                    title = "17 Interactive Hardware Tests",
                    description = "Touchscreen grid, display dead pixel test, speakers, microphones, vibration, cameras, sensors.",
                    icon = Icons.Default.Handyman,
                    actionText = "Launch Hardware Suite",
                    onAction = { onNavigate(AppScreen.HARDWARE_TEST) }
                )
            }

            // Tool 7: AOD & Screen Wake Monitor
            item {
                ToolActionCard(
                    title = "AOD & Screen Wake Observer",
                    description = "Detect suspicious background notification loops and rapid screen cycling on lock screen.",
                    icon = Icons.Default.Tv,
                    actionText = "Open AOD Diagnostic",
                    onAction = { onNavigate(AppScreen.DISPLAY_AOD) }
                )
            }

            // Tool 8: Apps & Permissions Manager
            item {
                ToolActionCard(
                    title = "Installed Apps Audit",
                    description = "Inspect third-party apps holding camera, microphone, or background location access.",
                    icon = Icons.Default.AppShortcut,
                    actionText = "Open Apps Manager",
                    onAction = { onNavigate(AppScreen.APPS_MANAGER) }
                )
            }
        }
    }
}

@Composable
fun ToolActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
        border = BorderStroke(1.dp, BrandNavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = BrandCyan.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.padding(8.dp).size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, fontSize = 12.sp, color = BrandTextSecondary)

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavyCardLight),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = actionText, color = BrandCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(14.dp))
            }
        }
    }
}
