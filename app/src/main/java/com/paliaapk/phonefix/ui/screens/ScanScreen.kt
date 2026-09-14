package com.paliaapk.phonefix.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.ui.components.HealthScoreDial
import com.paliaapk.phonefix.ui.components.PhoneFixBottomNav
import com.paliaapk.phonefix.ui.components.PhoneFixTopBar
import com.paliaapk.phonefix.ui.components.StatusBadge
import com.paliaapk.phonefix.ui.theme.BrandBlue
import com.paliaapk.phonefix.ui.theme.BrandCyan
import com.paliaapk.phonefix.ui.theme.BrandCyanLight
import com.paliaapk.phonefix.ui.theme.BrandNavyBorder
import com.paliaapk.phonefix.ui.theme.BrandNavyCard
import com.paliaapk.phonefix.ui.theme.BrandNavyCardLight
import com.paliaapk.phonefix.ui.theme.BrandNavyDark
import com.paliaapk.phonefix.ui.theme.BrandNavySurface
import com.paliaapk.phonefix.ui.theme.BrandTextMuted
import com.paliaapk.phonefix.ui.theme.BrandTextSecondary
import com.paliaapk.phonefix.ui.theme.BrandWhite
import com.paliaapk.phonefix.ui.theme.StatusAttention
import com.paliaapk.phonefix.ui.theme.StatusCritical
import com.paliaapk.phonefix.ui.theme.StatusExcellent
import com.paliaapk.phonefix.viewmodel.AppScreen
import com.paliaapk.phonefix.viewmodel.PhoneFixViewModel

@Composable
fun ScanScreen(
    viewModel: PhoneFixViewModel,
    isScanning: Boolean,
    scanProgress: Float,
    currentTest: String,
    results: List<DiagnosticResult>,
    healthScore: Int,
    ratingLabel: String,
    onNavigate: (AppScreen) -> Unit
) {
    val passedCount = results.count { it.status == DiagnosticStatus.PASS }
    val attentionCount = results.count { it.status == DiagnosticStatus.ATTENTION }
    val failedCount = results.count { it.status == DiagnosticStatus.FAILED }

    val animatedScanProgress by animateFloatAsState(
        targetValue = scanProgress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "scanProgressAnim"
    )

    Scaffold(
        topBar = {
            PhoneFixTopBar(onNavigate = onNavigate)
        },
        bottomBar = {
            PhoneFixBottomNav(
                currentScreen = AppScreen.SCAN,
                onNavigate = onNavigate
            )
        },
        containerColor = BrandNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("scan_screen_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FULL SYSTEM SCAN",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandWhite
                        )
                        Text(
                            text = if (isScanning) "Inspecting subsystems..." else "Diagnostic scan results",
                            fontSize = 12.sp,
                            color = BrandCyanLight
                        )
                    }

                    if (!isScanning) {
                        Button(
                            onClick = { viewModel.startFullSystemScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("rescan_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = BrandNavyDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-Scan", color = BrandNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Scanning progress or Finished card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                    border = BorderStroke(1.dp, BrandNavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isScanning) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(120.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { animatedScanProgress },
                                    modifier = Modifier.size(120.dp),
                                    color = BrandCyan,
                                    strokeWidth = 8.dp,
                                    trackColor = BrandNavySurface
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(animatedScanProgress * 100).toInt()}%",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandWhite
                                    )
                                    Text(
                                        text = "Scanning",
                                        fontSize = 10.sp,
                                        color = BrandCyanLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = currentTest,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandWhite
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { animatedScanProgress },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = BrandCyan,
                                trackColor = BrandNavySurface
                            )
                        } else {
                            // Scan completed summary
                            HealthScoreDial(score = healthScore, ratingLabel = ratingLabel)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "System Health Score: $healthScore/100",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandWhite
                            )
                            Text(
                                text = "Diagnostic sweep verified ${results.size} device parameters",
                                fontSize = 12.sp,
                                color = BrandTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Results counter row: Passed / Attention / Failed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ResultCounterChip(
                                count = passedCount,
                                label = "Passed",
                                color = StatusExcellent,
                                icon = Icons.Default.CheckCircle
                            )
                            ResultCounterChip(
                                count = attentionCount,
                                label = "Attention",
                                color = StatusAttention,
                                icon = Icons.Default.Warning
                            )
                            ResultCounterChip(
                                count = failedCount,
                                label = "Critical",
                                color = StatusCritical,
                                icon = Icons.Default.Error
                            )
                        }

                        if (!isScanning && results.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onNavigate(AppScreen.FIX_PROBLEMS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("fix_problems_btn")
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = BrandNavyDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("FIX PROBLEMS", color = BrandNavyDark, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onNavigate(AppScreen.REPORT) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BrandCyanLight),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("view_report_btn")
                                ) {
                                    Icon(Icons.Default.QueryStats, contentDescription = null, tint = BrandCyanLight, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("VIEW REPORT", color = BrandCyanLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Results list header
            if (results.isNotEmpty()) {
                item {
                    Text(
                        text = "DETAILED FINDINGS (${results.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan,
                        letterSpacing = 1.sp
                    )
                }

                items(results) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandNavyCardLight),
                        border = BorderStroke(1.dp, BrandNavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandWhite,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = item.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = BrandTextSecondary
                            )

                            if (item.evidence.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Telemetry: ${item.evidence}",
                                    fontSize = 10.sp,
                                    color = BrandCyanLight
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
fun ResultCounterChip(
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = "$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                Text(text = label, fontSize = 9.sp, color = BrandTextMuted)
            }
        }
    }
}
