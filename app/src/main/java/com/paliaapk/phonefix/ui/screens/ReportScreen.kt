package com.paliaapk.phonefix.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.data.ScanReportEntity
import com.paliaapk.phonefix.diagnostics.HealthScoreReport
import com.paliaapk.phonefix.model.SystemInfoData
import com.paliaapk.phonefix.ui.components.HealthScoreDial
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
import com.paliaapk.phonefix.ui.theme.StatusAttention
import com.paliaapk.phonefix.ui.theme.StatusCritical
import com.paliaapk.phonefix.ui.theme.StatusExcellent
import com.paliaapk.phonefix.viewmodel.AppScreen
import com.paliaapk.phonefix.viewmodel.PhoneFixViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(
    viewModel: PhoneFixViewModel,
    systemInfo: SystemInfoData,
    healthScore: HealthScoreReport,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val history by viewModel.scanHistory.collectAsState()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())

    Scaffold(
        topBar = { PhoneFixTopBar(onNavigate = onNavigate) },
        bottomBar = { PhoneFixBottomNav(currentScreen = AppScreen.REPORT, onNavigate = onNavigate) },
        containerColor = BrandNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("report_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DIAGNOSTIC REPORT", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                        Text("Certified Health Telemetry Summary", fontSize = 12.sp, color = BrandCyanLight)
                    }

                    Button(
                        onClick = { viewModel.shareReport(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("share_report_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = BrandNavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = BrandNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Current Health Certificate Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                    border = BorderStroke(1.dp, BrandNavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HealthScoreDial(score = healthScore.overallScore, ratingLabel = healthScore.ratingLabel)

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("DEVICE IDENTITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                                Text("${systemInfo.manufacturer} ${systemInfo.deviceModel}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                                Text("Android ${systemInfo.androidVersion} (API ${systemInfo.apiLevel})", fontSize = 11.sp, color = BrandTextSecondary)
                                Text("Patch: ${systemInfo.securityPatch}", fontSize = 10.sp, color = BrandTextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = BrandNavyBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ReportItem("Passed", "${healthScore.passedCount}", StatusExcellent)
                            ReportItem("Attention", "${healthScore.attentionCount}", StatusAttention)
                            ReportItem("Critical", "${healthScore.failedCount}", StatusCritical)
                            ReportItem("Checks", "${healthScore.validChecksCount}", BrandCyan)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BrandNavyBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Battery Level: ${systemInfo.batteryPct}%", fontSize = 11.sp, color = BrandTextSecondary)
                            Text("Temp: ${"%.1f".format(systemInfo.batteryTempC)}°C", fontSize = 11.sp, color = BrandTextSecondary)
                            Text("Storage: ${systemInfo.storageUsedPercent}% used", fontSize = 11.sp, color = BrandTextSecondary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = healthScore.explanation,
                            fontSize = 11.sp,
                            color = BrandCyanLight
                        )
                    }
                }
            }

            // Local History Section (Room DB persistence)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("LOCAL SCAN HISTORY (${history.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandCyan, letterSpacing = 1.sp)
                }
            }

            if (history.isEmpty()) {
                item {
                    Text("No previous saved scan sessions recorded yet. Run a full scan to record history.", color = BrandTextMuted, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                }
            } else {
                items(history) { record ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                        border = BorderStroke(1.dp, BrandNavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dateFormat.format(Date(record.timestamp)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${record.passedCount} Passed • ${record.attentionCount} Attention • ${record.failedCount} Critical",
                                    fontSize = 10.sp,
                                    color = BrandTextSecondary
                                )
                            }

                            Surface(
                                color = BrandCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${record.healthScore}/100",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = BrandTextMuted)
    }
}
