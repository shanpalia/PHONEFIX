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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType
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
import com.paliaapk.phonefix.ui.theme.BrandTextMuted
import com.paliaapk.phonefix.ui.theme.BrandTextSecondary
import com.paliaapk.phonefix.ui.theme.BrandWhite
import com.paliaapk.phonefix.ui.theme.StatusAttention
import com.paliaapk.phonefix.ui.theme.StatusCritical
import com.paliaapk.phonefix.ui.theme.StatusExcellent
import com.paliaapk.phonefix.viewmodel.AppScreen
import com.paliaapk.phonefix.viewmodel.PhoneFixViewModel

@Composable
fun FixProblemsScreen(
    viewModel: PhoneFixViewModel,
    results: List<DiagnosticResult>,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current

    val activeIssues = results.filter {
        !it.isIgnored && !it.isFixed &&
                (it.status == DiagnosticStatus.FAILED || it.status == DiagnosticStatus.ATTENTION || it.fixActionType != FixActionType.NONE)
    }

    val criticalIssues = activeIssues.filter { it.severity == DiagnosticSeverity.CRITICAL }
    val recommendedIssues = activeIssues.filter { it.severity == DiagnosticSeverity.HIGH || it.severity == DiagnosticSeverity.MEDIUM }
    val optionalIssues = activeIssues.filter { it.severity == DiagnosticSeverity.INFO || it.severity == DiagnosticSeverity.HEALTHY }

    Scaffold(
        topBar = { PhoneFixTopBar(onNavigate = onNavigate) },
        bottomBar = { PhoneFixBottomNav(currentScreen = AppScreen.TOOLS, onNavigate = onNavigate) },
        containerColor = BrandNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("fix_problems_screen"),
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
                        Text("FIX PROBLEMS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                        Text(
                            text = if (activeIssues.isEmpty()) "No unresolved issues detected" else "${activeIssues.size} Actionable Recommendations",
                            fontSize = 12.sp,
                            color = BrandCyanLight
                        )
                    }
                }
            }

            // Hardware Repair Transparency Banner
            item {
                Surface(
                    color = BrandNavyCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BrandNavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Transparent Policy: Software cannot repair physical hardware defects. If an electrical component or sensor is physically damaged, professional hardware inspection is required.",
                            fontSize = 11.sp,
                            color = BrandTextSecondary
                        )
                    }
                }
            }

            if (activeIssues.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusExcellent, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("All Inspected Gates Healthy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No unresolved storage, memory, or security flags need fixing at this time.", fontSize = 12.sp, color = BrandTextMuted)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.startFullSystemScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                            ) {
                                Text("Run Full Scan Again", color = BrandNavyDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Group: Critical
            if (criticalIssues.isNotEmpty()) {
                item {
                    Text("CRITICAL ISSUES (${criticalIssues.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusCritical, letterSpacing = 1.sp)
                }
                items(criticalIssues) { issue ->
                    ProblemCard(issue = issue, onFix = { viewModel.executeFixAction(context, issue.fixActionType) }, onResolve = { viewModel.markIssueFixed(issue.id) }, onIgnore = { viewModel.markIssueIgnored(issue.id) })
                }
            }

            // Group: Recommended
            if (recommendedIssues.isNotEmpty()) {
                item {
                    Text("RECOMMENDED ACTIONS (${recommendedIssues.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusAttention, letterSpacing = 1.sp)
                }
                items(recommendedIssues) { issue ->
                    ProblemCard(issue = issue, onFix = { viewModel.executeFixAction(context, issue.fixActionType) }, onResolve = { viewModel.markIssueFixed(issue.id) }, onIgnore = { viewModel.markIssueIgnored(issue.id) })
                }
            }

            // Group: Optional / Info
            if (optionalIssues.isNotEmpty()) {
                item {
                    Text("OPTIONAL OPTIMIZATIONS (${optionalIssues.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandCyan, letterSpacing = 1.sp)
                }
                items(optionalIssues) { issue ->
                    ProblemCard(issue = issue, onFix = { viewModel.executeFixAction(context, issue.fixActionType) }, onResolve = { viewModel.markIssueFixed(issue.id) }, onIgnore = { viewModel.markIssueIgnored(issue.id) })
                }
            }
        }
    }
}

@Composable
fun ProblemCard(
    issue: DiagnosticResult,
    onFix: () -> Unit,
    onResolve: () -> Unit,
    onIgnore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
        border = BorderStroke(1.dp, BrandNavyBorder),
        modifier = Modifier.fillMaxWidth().testTag("problem_card_${issue.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = issue.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandWhite,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = issue.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = issue.description, fontSize = 12.sp, color = BrandTextSecondary)

            if (issue.recommendation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = BrandNavyCardLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Solution: ${issue.recommendation}",
                        fontSize = 11.sp,
                        color = BrandCyanLight,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (issue.fixActionType != FixActionType.NONE) {
                    Button(
                        onClick = onFix,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f).testTag("fix_btn_${issue.id}")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = BrandNavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fix / Settings", color = BrandNavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                OutlinedButton(
                    onClick = onResolve,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StatusExcellent),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusExcellent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resolved", color = StatusExcellent, fontSize = 11.sp)
                }

                IconButton(
                    onClick = onIgnore,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Ignore", tint = BrandTextMuted)
                }
            }
        }
    }
}
