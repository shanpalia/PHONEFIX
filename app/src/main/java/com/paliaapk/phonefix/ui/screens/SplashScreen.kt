package com.paliaapk.phonefix.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.R
import com.paliaapk.phonefix.ui.theme.BrandBlue
import com.paliaapk.phonefix.ui.theme.BrandCyan
import com.paliaapk.phonefix.ui.theme.BrandCyanLight
import com.paliaapk.phonefix.ui.theme.BrandNavyDark
import com.paliaapk.phonefix.ui.theme.BrandNavySurface
import com.paliaapk.phonefix.ui.theme.BrandTextMuted
import com.paliaapk.phonefix.ui.theme.BrandTextSecondary
import com.paliaapk.phonefix.ui.theme.BrandWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val logoScale = remember { Animatable(0.7f) }
    val logoAlpha = remember { Animatable(0f) }
    val brandAlpha = remember { Animatable(0f) }
    val progressAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Logo enters
        logoAlpha.animateTo(1f, animationSpec = tween(400))
        logoScale.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))

        // Step 2: Brand enters
        brandAlpha.animateTo(1f, animationSpec = tween(350))

        // Step 3: Loading animation
        progressAlpha.animateTo(1f, animationSpec = tween(250))
        progress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))

        delay(150)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrandNavyDark, BrandNavySurface, BrandNavyDark)
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // PHONEFIX Circular Logo
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .clip(CircleShape)
                    .border(2.5.dp, BrandCyan, CircleShape)
                    .testTag("splash_logo")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_phonefix_logo),
                    contentDescription = "PHONEFIX Brand Logo",
                    modifier = Modifier.size(118.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(brandAlpha.value)
            ) {
                Text(
                    text = "PHONEFIX",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandWhite,
                    letterSpacing = 2.sp
                )

                Surface(
                    color = BrandCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "By PaliaAPK HUB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Tagline
                Text(
                    text = "Scan • Diagnose • Fix",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandCyanLight,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Secondary Tagline
                Text(
                    text = "Your Phone’s Personal Doctor",
                    fontSize = 13.sp,
                    color = BrandTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Loading / Progress bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(progressAlpha.value)
                    .width(180.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = BrandCyan,
                    trackColor = BrandNavySurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Initializing diagnostics engine...",
                    fontSize = 10.sp,
                    color = BrandTextMuted
                )
            }
        }

        // Developer Credit at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .alpha(brandAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Developed by SHANPALIA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = BrandTextMuted,
                letterSpacing = 0.5.sp
            )
        }
    }
}
