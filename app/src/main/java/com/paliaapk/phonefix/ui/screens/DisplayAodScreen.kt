package com.paliaapk.phonefix.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
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
import com.paliaapk.phonefix.ui.theme.BrandNavySurface
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
fun DisplayAodScreen(
    viewModel: PhoneFixViewModel,
    systemInfo: SystemInfoData,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val aodSession by viewModel.aodWakeMonitor.sessionState.collectAsState()
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Scaffold(
        topBar = { PhoneFixTopBar(onNavigate = onNavigate) },
        bottomBar = { PhoneFixBottomNav(currentScreen = AppScreen.TOOLS, onNavigate = onNavigate) },
        containerColor = BrandNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("display_aod_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        Text("DISPLAY & AOD DIAGNOSTICS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                        Text("Screen Telemetry & Standby Wake Monitor", fontSize = 12.sp, color = BrandCyanLight)
                    }
                }
            }

            // Display Specifications Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                    border = BorderStroke(1.dp, BrandNavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DISPLAY SPECIFICATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Panel Resolution", fontSize = 11.sp, color = BrandTextMuted)
                                Text(systemInfo.screenResolution, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                            }
                            Column {
                                Text("Refresh Rate", fontSize = 11.sp, color = BrandTextMuted)
                                Text("${"%.0f".format(systemInfo.refreshRate)} Hz", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                            }
                            Column {
                                Text("Screen Density", fontSize = 11.sp, color = BrandTextMuted)
                                Text("${systemInfo.screenDensityDpi} DPI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.executeFixAction(context, FixActionType.OPEN_DISPLAY_SETTINGS) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandNavyCardLight),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandNavyBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Android Display Settings", color = BrandCyan, fontSize = 11.sp)
                        }
                    }
                }
            }

            // AOD Wake-Up Problem Test Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                    border = BorderStroke(1.5.dp, BrandCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(BrandCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Tv, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("AOD / SCREEN WAKE TEST", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = BrandWhite)
                                    Text("Standby cycle observer", fontSize = 11.sp, color = BrandCyanLight)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Keep the phone locked with AOD enabled. PHONEFIX will observe available screen, sensor, and broadcast activity for repeated OFF → ON → OFF wake transitions.",
                            fontSize = 12.sp,
                            color = BrandTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Status or monitoring timer
                        Surface(
                            color = BrandNavyCardLight,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandNavyBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Test Status", fontSize = 10.sp, color = BrandTextMuted)
                                    Text(
                                        text = if (aodSession.isRunning) "Observing lock events (${aodSession.sessionDurationSec}s)" else aodSession.verdict,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (aodSession.suspiciousCyclesDetected) StatusAttention else BrandWhite
                                    )
                                }

                                if (aodSession.isRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = BrandCyan,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Surface(
                                        color = if (aodSession.suspiciousCyclesDetected) StatusAttention.copy(alpha = 0.2f) else StatusExcellent.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${aodSession.wakeEventsCount} Wakes",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (aodSession.suspiciousCyclesDetected) StatusAttention else StatusExcellent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!aodSession.isRunning) {
                                Button(
                                    onClick = { viewModel.aodWakeMonitor.startMonitoring(30) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("start_aod_monitor_btn")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = BrandNavyDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start 30s Observer", color = BrandNavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.aodWakeMonitor.stopMonitoring() },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusCritical),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("stop_aod_monitor_btn")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = BrandWhite, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop Test", color = BrandWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            // Simulation trigger for testing without needing to lock device
                            OutlinedButton(
                                onClick = { viewModel.aodWakeMonitor.simulateWakeTest(simulateAnomalous = true) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BrandCyanLight),
                                modifier = Modifier.testTag("simulate_aod_wake_btn")
                            ) {
                                Text("Simulate Anomaly", color = BrandCyanLight, fontSize = 10.sp)
                            }
                        }

                        // Diagnosis findings
                        if (aodSession.suspiciousCyclesDetected && aodSession.possibleCauses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = StatusAttention.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, StatusAttention.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusAttention, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Possible AOD Wake-Up Problem Detected", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusAttention)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Possible Causes:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                                    aodSession.possibleCauses.forEach { cause ->
                                        Text("• $cause", fontSize = 11.sp, color = BrandTextSecondary)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.executeFixAction(context, FixActionType.OPEN_AOD_SETTINGS) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Fix AOD Settings", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.executeFixAction(context, FixActionType.OPEN_NOTIFICATION_SETTINGS) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Check Notifications", color = BrandWhite, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Recorded events list
                        if (aodSession.eventsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("TRANSITION LOG (${aodSession.eventsList.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                            Spacer(modifier = Modifier.height(6.dp))
                            aodSession.eventsList.takeLast(6).forEach { ev ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${timeFormat.format(Date(ev.timestamp))} • ${ev.eventType}", fontSize = 11.sp, color = BrandWhite, fontWeight = FontWeight.SemiBold)
                                    Text(ev.sensorNotes, fontSize = 10.sp, color = BrandTextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
