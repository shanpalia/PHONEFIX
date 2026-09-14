package com.paliaapk.phonefix.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.BuildConfig
import com.paliaapk.phonefix.R
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.FixActionType
import com.paliaapk.phonefix.ui.theme.BrandBlue
import com.paliaapk.phonefix.ui.theme.BrandBlueDark
import com.paliaapk.phonefix.ui.theme.BrandCyan
import com.paliaapk.phonefix.ui.theme.BrandCyanLight
import com.paliaapk.phonefix.ui.theme.BrandNavyBorder
import com.paliaapk.phonefix.ui.theme.BrandNavyCard
import com.paliaapk.phonefix.ui.theme.BrandNavyCardLight
import com.paliaapk.phonefix.ui.theme.BrandNavyDark
import com.paliaapk.phonefix.ui.theme.BrandTextMuted
import com.paliaapk.phonefix.ui.theme.BrandTextPrimary
import com.paliaapk.phonefix.ui.theme.BrandTextSecondary
import com.paliaapk.phonefix.ui.theme.BrandWhite
import com.paliaapk.phonefix.ui.theme.StatusAttention
import com.paliaapk.phonefix.ui.theme.StatusCritical
import com.paliaapk.phonefix.ui.theme.StatusExcellent
import com.paliaapk.phonefix.ui.theme.StatusGood
import com.paliaapk.phonefix.ui.theme.StatusNeutral
import com.paliaapk.phonefix.viewmodel.AppScreen
import com.paliaapk.phonefix.util.UpdateChecker

@Composable
fun PhoneFixTopBar(
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showProDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var updateAvailable by remember { mutableStateOf(false) }
    var checkingUpdate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onNavigate(AppScreen.HOME) }
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, BrandCyan, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_phonefix_logo),
                    contentDescription = "PHONEFIX Logo",
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "PHONEFIX",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "By PaliaAPK HUB",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandCyanLight
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // PRO Badge Button
            Surface(
                onClick = { showProDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = BrandCyan.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, BrandCyan),
                modifier = Modifier.testTag("pro_badge_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "PRO",
                        tint = BrandCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PRO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan
                    )
                }
            }

            // Notification Icon
            IconButton(
                onClick = { showNotificationDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .testTag("notification_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = BrandTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Settings Icon
            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = BrandTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showProDialog) {
        AlertDialog(
            onDismissRequest = { showProDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = BrandCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PHONEFIX PRO Edition", color = BrandWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "All professional diagnostic tools, interactive hardware tests, and AOD cycle monitors are fully unlocked for this device.",
                        color = BrandTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Brand: By PaliaAPK HUB", color = BrandCyanLight, fontSize = 12.sp)
                    Text("• Developer: Developed by SHANPALIA", color = BrandCyanLight, fontSize = 12.sp)
                    Text("• Motto: Scan • Diagnose • Fix", color = BrandTextMuted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showProDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Got It")
                }
            },
            containerColor = BrandNavyCard
        )
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Diagnostic Alerts", color = BrandWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "System health is currently being monitored locally. No anomalous background crashes or critical security events logged.",
                    color = BrandTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Close", color = BrandCyan)
                }
            },
            containerColor = BrandNavyCard
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("PHONEFIX Preferences", color = BrandWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("PHONEFIX v1.0", fontWeight = FontWeight.Bold, color = BrandWhite)
                    Text("By PaliaAPK HUB", color = BrandCyanLight, fontSize = 12.sp)
                    Text("Developed by SHANPALIA", color = BrandTextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Tagline: Scan • Diagnose • Fix\nSecondary: Your Phone's Personal Doctor",
                        color = BrandTextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            checkingUpdate = true
                            updateMessage = null
                            scope.launch {
                                val result = UpdateChecker.check(BuildConfig.VERSION_NAME)
                                checkingUpdate = false
                                updateAvailable = result.updateAvailable
                                updateMessage = result.message
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !checkingUpdate
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (checkingUpdate) "Checking..." else "Check for Update", color = BrandBlue)
                    }
                    updateMessage?.let { message ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = if (updateAvailable) StatusAttention.copy(alpha = 0.12f) else StatusExcellent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(message, color = if (updateAvailable) StatusAttention else StatusExcellent, fontSize = 12.sp)
                                if (updateAvailable) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(onClick = { UpdateChecker.openWebsite(context = context) }) {
                                        Text("Open PaliaAPK Website", color = BrandBlue)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showSettingsDialog = false
                            onNavigate(AppScreen.TOOLS)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("System Check & Settings", color = BrandBlue)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showSettingsDialog = false
                            onNavigate(AppScreen.PRIVACY)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Privacy & Permission Policy", color = BrandCyan)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Done", color = BrandCyan)
                }
            },
            containerColor = BrandNavyCard
        )
    }
}

@Composable
fun PhoneFixBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = BrandNavyDark,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onNavigate(AppScreen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandNavyDark,
                selectedTextColor = BrandCyan,
                indicatorColor = BrandCyan,
                unselectedIconColor = BrandTextMuted,
                unselectedTextColor = BrandTextMuted
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.SCAN,
            onClick = { onNavigate(AppScreen.SCAN) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Scan") },
            label = { Text("Scan", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandNavyDark,
                selectedTextColor = BrandCyan,
                indicatorColor = BrandCyan,
                unselectedIconColor = BrandTextMuted,
                unselectedTextColor = BrandTextMuted
            ),
            modifier = Modifier.testTag("nav_scan")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.TOOLS || currentScreen == AppScreen.HARDWARE_TEST || currentScreen == AppScreen.DISPLAY_AOD,
            onClick = { onNavigate(AppScreen.TOOLS) },
            icon = { Icon(Icons.Default.Build, contentDescription = "Tools") },
            label = { Text("Tools", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandNavyDark,
                selectedTextColor = BrandCyan,
                indicatorColor = BrandCyan,
                unselectedIconColor = BrandTextMuted,
                unselectedTextColor = BrandTextMuted
            ),
            modifier = Modifier.testTag("nav_tools")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.REPORT,
            onClick = { onNavigate(AppScreen.REPORT) },
            icon = { Icon(Icons.Default.QueryStats, contentDescription = "Report") },
            label = { Text("Report", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandNavyDark,
                selectedTextColor = BrandCyan,
                indicatorColor = BrandCyan,
                unselectedIconColor = BrandTextMuted,
                unselectedTextColor = BrandTextMuted
            ),
            modifier = Modifier.testTag("nav_report")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.PRIVACY || currentScreen == AppScreen.SMART_DIAGNOSIS,
            onClick = { onNavigate(AppScreen.PRIVACY) },
            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
            label = { Text("More", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandNavyDark,
                selectedTextColor = BrandCyan,
                indicatorColor = BrandCyan,
                unselectedIconColor = BrandTextMuted,
                unselectedTextColor = BrandTextMuted
            ),
            modifier = Modifier.testTag("nav_more")
        )
    }
}

@Composable
fun HealthScoreDial(
    score: Int,
    ratingLabel: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (score >= 0) score / 100f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "scoreDial"
    )

    val scoreColor = when {
        score < 0 -> StatusNeutral
        score >= 90 -> StatusExcellent
        score >= 75 -> StatusGood
        score >= 50 -> StatusAttention
        else -> StatusCritical
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(140.dp)
    ) {
        // Outer track background
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(140.dp),
            color = BrandNavyCardLight,
            strokeWidth = 10.dp
        )
        // Score value arc
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(140.dp),
            color = scoreColor,
            strokeWidth = 10.dp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (score >= 0) "$score" else "--",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandWhite,
                letterSpacing = (-1).sp
            )
            Text(
                text = "/100",
                fontSize = 12.sp,
                color = BrandTextMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                color = scoreColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = ratingLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: DiagnosticStatus,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, fgColor) = when (status) {
        DiagnosticStatus.PASS -> Triple("PASS", StatusExcellent.copy(alpha = 0.18f), StatusExcellent)
        DiagnosticStatus.ATTENTION -> Triple("ATTENTION", StatusAttention.copy(alpha = 0.18f), StatusAttention)
        DiagnosticStatus.FAILED -> Triple("FAILED", StatusCritical.copy(alpha = 0.18f), StatusCritical)
        DiagnosticStatus.SKIPPED -> Triple("SKIPPED", StatusNeutral.copy(alpha = 0.18f), StatusNeutral)
        DiagnosticStatus.UNAVAILABLE -> Triple("UNAVAILABLE", BrandTextMuted.copy(alpha = 0.18f), BrandTextMuted)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, fgColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fgColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color = BrandCyan,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BrandNavyCardLight,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BrandNavyBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = BrandTextMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 12.sp,
                    color = BrandWhite,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
