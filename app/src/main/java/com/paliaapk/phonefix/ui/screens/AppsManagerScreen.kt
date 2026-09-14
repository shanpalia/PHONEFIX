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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.diagnostics.AppDiagnostic
import com.paliaapk.phonefix.diagnostics.InstalledAppItem
import com.paliaapk.phonefix.model.FixActionType
import com.paliaapk.phonefix.ui.components.PhoneFixBottomNav
import com.paliaapk.phonefix.ui.components.PhoneFixTopBar
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
fun AppsManagerScreen(
    viewModel: PhoneFixViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val allApps = remember { AppDiagnostic().getInstalledAppsList(context) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: User Apps, 1: System Apps
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(allApps, selectedTab, searchQuery) {
        allApps.filter { app ->
            val matchesTab = if (selectedTab == 0) !app.isSystemApp else app.isSystemApp
            val matchesQuery = searchQuery.isEmpty() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesQuery
        }
    }

    Scaffold(
        topBar = { PhoneFixTopBar(onNavigate = onNavigate) },
        bottomBar = { PhoneFixBottomNav(currentScreen = AppScreen.TOOLS, onNavigate = onNavigate) },
        containerColor = BrandNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("apps_manager_screen"),
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
                        Text("APPS DIAGNOSTICS & MANAGER", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                        Text("Audit installed packages and permissions safely", fontSize = 12.sp, color = BrandCyanLight)
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search application name or package...", color = BrandTextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandCyan) },
                    modifier = Modifier.fillMaxWidth().testTag("app_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BrandNavyCard,
                        unfocusedContainerColor = BrandNavyCard,
                        focusedTextColor = BrandWhite,
                        unfocusedTextColor = BrandWhite,
                        focusedIndicatorColor = BrandCyan,
                        unfocusedIndicatorColor = BrandNavyBorder
                    ),
                    singleLine = true
                )
            }

            // Tabs: User Apps vs System Apps
            item {
                val userCount = allApps.count { !it.isSystemApp }
                val systemCount = allApps.count { it.isSystemApp }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BrandNavyCard,
                    contentColor = BrandCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BrandCyan
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("User Apps ($userCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("System Apps ($systemCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            if (filteredApps.isEmpty()) {
                item {
                    Text("No applications found matching query.", color = BrandTextMuted, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                }
            }

            items(filteredApps) { app ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                    border = BorderStroke(1.dp, BrandNavyBorder),
                    modifier = Modifier.fillMaxWidth().testTag("app_item_${app.packageName}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Android, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = app.appName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = app.packageName,
                                        fontSize = 10.sp,
                                        color = BrandTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                color = if (app.permissionCount > 25) StatusAttention.copy(alpha = 0.15f) else BrandNavyCardLight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${app.permissionCount} Perms",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (app.permissionCount > 25) StatusAttention else BrandTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Sensitive permission indicators
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (app.hasLocationPermission) {
                                SensitiveBadge("Location", Icons.Default.MyLocation)
                            }
                            if (app.hasCameraPermission) {
                                SensitiveBadge("Camera", Icons.Default.CameraAlt)
                            }
                            if (app.hasMicPermission) {
                                SensitiveBadge("Microphone", Icons.Default.Mic)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeFixAction(context, FixActionType.OPEN_APP_SETTINGS, app.packageName) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandNavyCardLight),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("App Info", color = BrandCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.executeFixAction(context, FixActionType.OPEN_APP_SETTINGS, app.packageName) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BrandNavyBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = BrandTextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Permissions", color = BrandTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensitiveBadge(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = BrandCyan.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(10.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(label, fontSize = 9.sp, color = BrandCyanLight, fontWeight = FontWeight.Medium)
        }
    }
}
