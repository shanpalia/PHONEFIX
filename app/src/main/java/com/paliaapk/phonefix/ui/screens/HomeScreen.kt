package com.paliaapk.phonefix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.model.SystemInfoData
import com.paliaapk.phonefix.ui.components.HealthScoreDial
import com.paliaapk.phonefix.ui.components.MetricChip
import com.paliaapk.phonefix.ui.components.PhoneFixBottomNav
import com.paliaapk.phonefix.ui.components.PhoneFixTopBar
import com.paliaapk.phonefix.ui.theme.BrandBlue
import com.paliaapk.phonefix.ui.theme.BrandBlueDark
import com.paliaapk.phonefix.ui.theme.BrandCyan
import com.paliaapk.phonefix.ui.theme.BrandCyanDark
import com.paliaapk.phonefix.ui.theme.BrandCyanLight
import com.paliaapk.phonefix.ui.theme.BrandNavyBorder
import com.paliaapk.phonefix.ui.theme.BrandNavyCard
import com.paliaapk.phonefix.ui.theme.BrandNavyCardLight
import com.paliaapk.phonefix.ui.theme.BrandNavyDark
import com.paliaapk.phonefix.ui.theme.BrandNavySurface
import com.paliaapk.phonefix.ui.theme.BrandTextMuted
import com.paliaapk.phonefix.ui.theme.BrandTextPrimary
import com.paliaapk.phonefix.ui.theme.BrandTextSecondary
import com.paliaapk.phonefix.ui.theme.BrandWhite
import com.paliaapk.phonefix.ui.theme.StatusAttention
import com.paliaapk.phonefix.ui.theme.StatusCritical
import com.paliaapk.phonefix.ui.theme.StatusExcellent
import com.paliaapk.phonefix.ui.theme.StatusGood
import com.paliaapk.phonefix.viewmodel.AppScreen
import com.paliaapk.phonefix.viewmodel.PhoneFixViewModel

data class DiagnosticCardItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val targetScreen: AppScreen
)

@Composable
fun HomeScreen(
    viewModel: PhoneFixViewModel,
    systemInfo: SystemInfoData,
    healthScore: Int,
    ratingLabel: String,
    scoreExplanation: String,
    onNavigate: (AppScreen) -> Unit
) {
    val diagnosticCards = listOf(
        DiagnosticCardItem("Device Info", "${systemInfo.manufacturer} ${systemInfo.deviceModel}", Icons.Default.PhoneAndroid, BrandCyan, AppScreen.TOOLS),
        DiagnosticCardItem("Hardware Test", "17 Interactive Tests", Icons.Default.Handyman, BrandBlue, AppScreen.HARDWARE_TEST),
        DiagnosticCardItem("Storage Cleaner", if (systemInfo.totalStorageBytes > 0) "${100 - systemInfo.storageUsedPercent}% free space" else "Not scanned yet", Icons.Default.CleaningServices, BrandCyanLight, AppScreen.TOOLS),
        DiagnosticCardItem("Boost Performance", "Safe memory & CPU options", Icons.Default.Bolt, Color(0xFFF59E0B), AppScreen.TOOLS),
        DiagnosticCardItem("Battery Health", if (systemInfo.batteryPct >= 0) "${systemInfo.batteryPct}% • ${systemInfo.chargePlug}" else "Not scanned yet", Icons.Default.BatteryChargingFull, StatusExcellent, AppScreen.TOOLS),
        DiagnosticCardItem("Network & Wi-Fi", if (systemInfo.isWifiConnected) "Connected" else "Cellular / Idle", Icons.Default.NetworkCheck, BrandCyanDark, AppScreen.TOOLS),
        DiagnosticCardItem("Security Check", if (systemInfo.securityPatch.isNotBlank()) "Patch ${systemInfo.securityPatch}" else "Not scanned yet", Icons.Default.Security, Color(0xFF10B981), AppScreen.TOOLS),
        DiagnosticCardItem("System Settings", if (systemInfo.androidVersion.isNotBlank()) "Android ${systemInfo.androidVersion} Settings" else "Not scanned yet", Icons.Default.Settings, Color(0xFF818CF8), AppScreen.TOOLS),
        DiagnosticCardItem("Apps Manager", "Inspect installed applications", Icons.Default.AppShortcut, Color(0xFFEC4899), AppScreen.APPS_MANAGER),
        DiagnosticCardItem("Display & AOD", "Screen & Wake Diagnostic", Icons.Default.Tv, BrandCyan, AppScreen.DISPLAY_AOD),
        DiagnosticCardItem("Fix Problems", "Safe guided solutions", Icons.Default.BugReport, StatusCritical, AppScreen.FIX_PROBLEMS),
        DiagnosticCardItem("Reports", "Scan history & insights", Icons.Default.QueryStats, Color(0xFF06B6D4), AppScreen.REPORT),
        DiagnosticCardItem("Smart Diagnosis", "Correlation analysis", Icons.Default.Psychology, Color(0xFFA855F7), AppScreen.SMART_DIAGNOSIS)
    )

    Scaffold(
        topBar = {
            PhoneFixTopBar(onNavigate = onNavigate)
        },
        bottomBar = {
            PhoneFixBottomNav(
                currentScreen = AppScreen.HOME,
                onNavigate = onNavigate
            )
        },
        containerColor = BrandNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen_content"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // MAIN CARD: Phone Health __/100
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                    border = BorderStroke(1.dp, BrandNavyBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_health_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PHONE HEALTH",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandCyan,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${systemInfo.manufacturer} ${systemInfo.deviceModel}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { viewModel.refreshSystemInfo() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = BrandTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Score Dial & Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            HealthScoreDial(score = healthScore, ratingLabel = ratingLabel)

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MetricChip(
                                    label = "RAM Status",
                                    value = if (systemInfo.totalRamBytes > 0) "${systemInfo.ramUsedPercent}% Used" else "Not scanned",
                                    icon = Icons.Default.Memory,
                                    accentColor = BrandCyan
                                )
                                MetricChip(
                                    label = "Storage",
                                    value = if (systemInfo.totalStorageBytes > 0) "${systemInfo.storageUsedPercent}% Used" else "Not scanned",
                                    icon = Icons.Default.Folder,
                                    accentColor = BrandCyanLight
                                )
                                MetricChip(
                                    label = "Battery",
                                    value = if (systemInfo.batteryPct >= 0) "${systemInfo.batteryPct}% (${if (systemInfo.isCharging) "Charging" else "Unplugged"})" else "Not scanned",
                                    icon = Icons.Default.BatteryChargingFull,
                                    accentColor = StatusExcellent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sub stats row: Temp, Android Ver, Device
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (systemInfo.batteryTempC > 0f) "🌡️ Temp: ${"%.1f".format(systemInfo.batteryTempC)}°C" else "🌡️ Temp: --",
                                fontSize = 11.sp,
                                color = BrandTextSecondary
                            )
                            Text(
                                text = if (systemInfo.androidVersion.isNotBlank()) "🤖 Android ${systemInfo.androidVersion} (API ${systemInfo.apiLevel})" else "🤖 Android --",
                                fontSize = 11.sp,
                                color = BrandTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = scoreExplanation,
                            fontSize = 11.sp,
                            color = BrandTextMuted
                        )
                    }
                }
            }

            // MAIN CTA: FULL SYSTEM SCAN
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandNavyCardLight
                    ),
                    border = BorderStroke(1.5.dp, BrandCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.startFullSystemScan() }
                        .testTag("full_system_scan_cta")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        BrandBlueDark.copy(alpha = 0.7f),
                                        BrandNavyCardLight,
                                        BrandCyanDark.copy(alpha = 0.4f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(BrandCyan)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "FULL SYSTEM SCAN",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandWhite,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Scan your phone for problems and get safe solutions",
                                    fontSize = 12.sp,
                                    color = BrandCyanLight
                                )
                            }

                            Button(
                                onClick = { viewModel.startFullSystemScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("start_scan_btn")
                            ) {
                                Text(
                                    text = "SCAN",
                                    color = BrandNavyDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = BrandNavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION TITLE: Diagnostic Modules
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIAGNOSTIC MODULES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "13 Modules",
                        fontSize = 11.sp,
                        color = BrandTextMuted
                    )
                }
            }

            // Diagnostic cards grid (2 columns)
            val chunkedCards = diagnosticCards.chunked(2)
            chunkedCards.forEach { pair ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { cardItem ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                                border = BorderStroke(1.dp, BrandNavyBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(cardItem.targetScreen) }
                                    .testTag("card_${cardItem.title.replace(" ", "_").lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(cardItem.accentColor.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = cardItem.icon,
                                            contentDescription = cardItem.title,
                                            tint = cardItem.accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = cardItem.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = cardItem.subtitle,
                                        fontSize = 11.sp,
                                        color = BrandTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Bottom Brand Footnote
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PHONEFIX • By PaliaAPK HUB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandCyanLight
                    )
                    Text(
                        text = "Scan • Diagnose • Fix • Developed by SHANPALIA",
                        fontSize = 10.sp,
                        color = BrandTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
