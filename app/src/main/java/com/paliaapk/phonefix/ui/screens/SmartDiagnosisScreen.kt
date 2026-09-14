package com.paliaapk.phonefix.ui.screens

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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.diagnostics.SmartDiagnosisCase
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.FixActionType
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

@Composable
fun SmartDiagnosisScreen(
    viewModel: PhoneFixViewModel,
    smartCases: List<SmartDiagnosisCase>,
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
                .testTag("smart_diagnosis_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigate(AppScreen.HOME) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandWhite)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("SMART DIAGNOSIS ENGINE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                        Text("Multi-Signal Correlation Analysis", fontSize = 12.sp, color = BrandCyanLight)
                    }
                }
            }

            // Engine description
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandCyan.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Rules-Based Intelligence", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                            Text(
                                "Combines battery, memory, storage, thermal, and wake parameters into structured diagnoses with clear causality.",
                                fontSize = 11.sp,
                                color = BrandTextSecondary
                            )
                        }
                    }
                }
            }

            if (smartCases.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No Cross-Module Anomalies", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("The correlation engine found no compounded failure patterns between storage, thermal, battery, and wake systems.", fontSize = 12.sp, color = BrandTextMuted)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.startFullSystemScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                            ) {
                                Text("Re-Analyze Device Telemetry", color = BrandNavyDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(smartCases) { sc ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                        border = BorderStroke(1.dp, BrandNavyBorder),
                        modifier = Modifier.fillMaxWidth().testTag("smart_case_${sc.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sc.problemTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sc.severity == DiagnosticSeverity.CRITICAL) StatusCritical else StatusAttention,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = BrandCyan.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = sc.category.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Step 1: Why it may be happening
                            Text("WHY IT MAY BE HAPPENING:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandTextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(sc.whyHappening, fontSize = 12.sp, color = BrandTextSecondary)

                            Spacer(modifier = Modifier.height(10.dp))

                            // Step 2: Recommended action
                            Text("RECOMMENDED ACTION:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(sc.recommendedAction, fontSize = 12.sp, color = BrandWhite)

                            if (sc.isHardwareInspectionRecommended) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = StatusCritical.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Hardware/service inspection may be required.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusCritical,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Step 3: Fix / Open Settings / Test Again
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (sc.fixActionType != FixActionType.NONE) {
                                    Button(
                                        onClick = { viewModel.executeFixAction(context, sc.fixActionType) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, tint = BrandNavyDark, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Fix / Open Settings", color = BrandNavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { viewModel.startFullSystemScan() },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BrandCyanLight),
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = BrandCyanLight, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Again", color = BrandCyanLight, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
