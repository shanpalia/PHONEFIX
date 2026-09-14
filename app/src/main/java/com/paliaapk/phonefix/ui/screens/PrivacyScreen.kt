package com.paliaapk.phonefix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.R
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
import com.paliaapk.phonefix.ui.theme.StatusExcellent
import com.paliaapk.phonefix.viewmodel.AppScreen
import com.paliaapk.phonefix.viewmodel.PhoneFixViewModel

@Composable
fun PrivacyScreen(
    viewModel: PhoneFixViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    Scaffold(
        topBar = { PhoneFixTopBar(onNavigate = onNavigate) },
        bottomBar = { PhoneFixBottomNav(currentScreen = AppScreen.PRIVACY, onNavigate = onNavigate) },
        containerColor = BrandNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("privacy_screen"),
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
                        Text("PRIVACY & SAFETY POLICY", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                        Text("Security Guarantee & Brand Credentials", fontSize = 12.sp, color = BrandCyanLight)
                    }
                }
            }

            // Brand & Developer Credit Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                    border = BorderStroke(1.dp, BrandNavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.dp, BrandCyan, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_phonefix_logo),
                                contentDescription = "PHONEFIX",
                                modifier = Modifier.size(72.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("PHONEFIX", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = BrandWhite, letterSpacing = 1.sp)
                        Text("By PaliaAPK HUB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandCyan)

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Scan • Diagnose • Fix", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandCyanLight)
                        Text("Your Phone’s Personal Doctor", fontSize = 12.sp, color = BrandTextSecondary)

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = BrandNavyCardLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Developed by SHANPALIA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandCyanLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Core Guarantees
            item {
                PrivacyPolicyCard(
                    title = "100% Local On-Device Processing",
                    content = "PHONEFIX performs all hardware, memory, battery, and diagnostic inspections locally on your phone. No telemetry, device serial numbers, or diagnostic logs are transmitted to external servers.",
                    icon = Icons.Default.Lock
                )
            }

            item {
                PrivacyPolicyCard(
                    title = "Zero Personal Data Collection",
                    content = "PHONEFIX does not access, read, or collect your personal photos, files, contacts, browsing history, or private messages. Diagnostics are strictly restricted to public Android operating system health APIs.",
                    icon = Icons.Default.PrivacyTip
                )
            }

            item {
                PrivacyPolicyCard(
                    title = "No Destructive Actions or Silent Alterations",
                    content = "PHONEFIX never silently force-stops apps, deletes user documents, disables device features, or triggers destructive actions. Every fix recommendation provides clear guidance and direct user choice.",
                    icon = Icons.Default.VerifiedUser
                )
            }

            item {
                PrivacyPolicyCard(
                    title = "Strict Hardware Repair Honesty",
                    content = "We do not claim software can fix cracked screens, physically dead pixels, burnt audio voice coils, or worn-out battery cells. If hardware failure is suspected, we transparently state: 'Hardware/service inspection may be required.'",
                    icon = Icons.Default.Security
                )
            }

            item {
                PrivacyPolicyCard(
                    title = "No Root Exploits Required",
                    content = "PHONEFIX operates fully within Android standard security architecture without requiring root access, bootloader unlocking, or risky system modifications.",
                    icon = Icons.Default.Code
                )
            }
        }
    }
}

@Composable
fun PrivacyPolicyCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = content, fontSize = 11.sp, color = BrandTextSecondary, lineHeight = 16.sp)
        }
    }
}
