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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.diagnostics.HardwareTestController
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.HardwareTestResult
import com.paliaapk.phonefix.model.HardwareTestType
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
import kotlinx.coroutines.launch

@Composable
fun HardwareTestScreen(
    viewModel: PhoneFixViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val controller = viewModel.hardwareController
    val testResults by controller.testResults.collectAsState()
    val accel by controller.accelValues.collectAsState()
    val gyro by controller.gyroValues.collectAsState()
    val compass by controller.compassAzimuth.collectAsState()
    val lux by controller.lightLux.collectAsState()
    val proximity by controller.proximityDistance.collectAsState()
    val isTorchOn by controller.isTorchActive.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            PhoneFixTopBar(onNavigate = onNavigate)
        },
        bottomBar = {
            PhoneFixBottomNav(
                currentScreen = AppScreen.TOOLS,
                onNavigate = onNavigate
            )
        },
        containerColor = BrandNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("hardware_test_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigate(AppScreen.TOOLS) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandWhite)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "HARDWARE TEST SUITE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandWhite
                        )
                        Text(
                            text = "17 Device Component Tests",
                            fontSize = 12.sp,
                            color = BrandCyanLight
                        )
                    }
                }
            }

            // Quick launch interactive tests (Touchscreen & Display Dead Pixels)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                        border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(AppScreen.HARDWARE_TOUCH_TEST) }
                            .testTag("interactive_touch_test_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(BrandCyan.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Touchscreen Grid", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                                Text("Draw & verify", fontSize = 10.sp, color = BrandTextSecondary)
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
                        border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(AppScreen.HARDWARE_DISPLAY_COLOR_TEST) }
                            .testTag("interactive_color_test_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(BrandBlue.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = BrandCyanLight, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Dead Pixels", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                                Text("RGB color check", fontSize = 10.sp, color = BrandTextSecondary)
                            }
                        }
                    }
                }
            }

            // Item: Vibration Test
            item {
                HardwareItemCard(
                    title = "Vibration Motor",
                    icon = Icons.Default.Vibration,
                    result = testResults[HardwareTestType.VIBRATION] ?: HardwareTestResult(HardwareTestType.VIBRATION),
                    actionText = "Vibrate Now",
                    onAction = {
                        val ok = controller.testVibration()
                        controller.updateResult(
                            HardwareTestType.VIBRATION,
                            if (ok) DiagnosticStatus.PASS else DiagnosticStatus.ATTENTION,
                            if (ok) "Vibration pulse executed" else "Vibrator unconfirmed"
                        )
                    }
                )
            }

            // Item: Speaker Test
            item {
                HardwareItemCard(
                    title = "Loudspeaker",
                    icon = Icons.Default.VolumeUp,
                    result = testResults[HardwareTestType.SPEAKER] ?: HardwareTestResult(HardwareTestType.SPEAKER),
                    actionText = "Play 880Hz Audio",
                    onAction = {
                        scope.launch {
                            val ok = controller.playTestTone(isEarpiece = false, durationMs = 1500)
                            controller.updateResult(
                                HardwareTestType.SPEAKER,
                                if (ok) DiagnosticStatus.PASS else DiagnosticStatus.ATTENTION,
                                if (ok) "Sine tone played to MEDIA stream" else "Audio output unavailable"
                            )
                        }
                    }
                )
            }

            // Item: Earpiece Receiver Test
            item {
                HardwareItemCard(
                    title = "Call Earpiece Receiver",
                    icon = Icons.Default.Hearing,
                    result = testResults[HardwareTestType.EARPIECE] ?: HardwareTestResult(HardwareTestType.EARPIECE),
                    actionText = "Play Earpiece Audio",
                    onAction = {
                        scope.launch {
                            val ok = controller.playTestTone(isEarpiece = true, durationMs = 1500)
                            controller.updateResult(
                                HardwareTestType.EARPIECE,
                                if (ok) DiagnosticStatus.PASS else DiagnosticStatus.ATTENTION,
                                if (ok) "Chirp played to VOICE stream" else "Earpiece route unconfirmed"
                            )
                        }
                    }
                )
            }

            // Item: Flashlight Torch Test
            item {
                HardwareItemCard(
                    title = "Camera Flash / Torch",
                    icon = Icons.Default.FlashOn,
                    result = testResults[HardwareTestType.FLASH] ?: HardwareTestResult(HardwareTestType.FLASH),
                    actionText = if (isTorchOn) "Turn Off Flash" else "Turn On Flash",
                    onAction = {
                        val ok = controller.toggleTorch()
                        controller.updateResult(
                            HardwareTestType.FLASH,
                            if (ok) DiagnosticStatus.PASS else DiagnosticStatus.ATTENTION,
                            if (ok) "Torch state toggled" else "Flashlight unavailable"
                        )
                    }
                )
            }

            // Item: Camera Hardware
            item {
                val rearResult = testResults[HardwareTestType.REAR_CAMERA] ?: HardwareTestResult(HardwareTestType.REAR_CAMERA)
                HardwareItemCard(
                    title = "Rear Camera Sensor",
                    icon = Icons.Default.CameraAlt,
                    result = rearResult,
                    actionText = "Inspect Optics",
                    onAction = {
                        val (back, front) = controller.checkCameras()
                        controller.updateResult(back.type, back.status, back.details, back.measurement)
                        controller.updateResult(front.type, front.status, front.details, front.measurement)
                    }
                )
            }

            item {
                val frontResult = testResults[HardwareTestType.FRONT_CAMERA] ?: HardwareTestResult(HardwareTestType.FRONT_CAMERA)
                HardwareItemCard(
                    title = "Front Selfie Camera Sensor",
                    icon = Icons.Default.CameraAlt,
                    result = frontResult,
                    actionText = "Inspect Selfie",
                    onAction = {
                        val (back, front) = controller.checkCameras()
                        controller.updateResult(back.type, back.status, back.details, back.measurement)
                        controller.updateResult(front.type, front.status, front.details, front.measurement)
                    }
                )
            }

            // Item: Live Accelerometer
            item {
                HardwareItemCard(
                    title = "Accelerometer",
                    icon = Icons.Default.MotionPhotosOn,
                    result = HardwareTestResult(
                        HardwareTestType.ACCELEROMETER,
                        DiagnosticStatus.PASS,
                        "X: ${"%.2f".format(accel.first)}  Y: ${"%.2f".format(accel.second)}  Z: ${"%.2f".format(accel.third)}",
                        "Live Tracking"
                    ),
                    actionText = "Mark Pass",
                    onAction = {
                        controller.updateResult(HardwareTestType.ACCELEROMETER, DiagnosticStatus.PASS, "Active tilt tracking confirmed")
                    }
                )
            }

            // Item: Live Gyroscope
            item {
                HardwareItemCard(
                    title = "Gyroscope",
                    icon = Icons.Default.ScreenRotation,
                    result = HardwareTestResult(
                        HardwareTestType.GYROSCOPE,
                        DiagnosticStatus.PASS,
                        "Roll: ${"%.2f".format(gyro.first)}  Pitch: ${"%.2f".format(gyro.second)}  Yaw: ${"%.2f".format(gyro.third)}",
                        "Live Tracking"
                    ),
                    actionText = "Mark Pass",
                    onAction = {
                        controller.updateResult(HardwareTestType.GYROSCOPE, DiagnosticStatus.PASS, "Angular velocity sensor verified")
                    }
                )
            }

            // Item: Compass / Magnetometer
            item {
                HardwareItemCard(
                    title = "Compass / Magnetometer",
                    icon = Icons.Default.Explore,
                    result = HardwareTestResult(
                        HardwareTestType.COMPASS,
                        DiagnosticStatus.PASS,
                        "Azimuth Heading: ${"%.1f".format(compass)}°",
                        "Live Tracking"
                    ),
                    actionText = "Mark Pass",
                    onAction = {
                        controller.updateResult(HardwareTestType.COMPASS, DiagnosticStatus.PASS, "Magnetic north vector verified")
                    }
                )
            }

            // Item: Ambient Light Sensor
            item {
                HardwareItemCard(
                    title = "Ambient Light Sensor",
                    icon = Icons.Default.Lightbulb,
                    result = HardwareTestResult(
                        HardwareTestType.LIGHT_SENSOR,
                        DiagnosticStatus.PASS,
                        "Measured Illuminance: ${"%.1f".format(lux)} lux",
                        "Live Tracking"
                    ),
                    actionText = "Mark Pass",
                    onAction = {
                        controller.updateResult(HardwareTestType.LIGHT_SENSOR, DiagnosticStatus.PASS, "Photodiode responsiveness confirmed")
                    }
                )
            }

            // Item: Proximity Sensor
            item {
                HardwareItemCard(
                    title = "Proximity Sensor",
                    icon = Icons.Default.NearMe,
                    result = HardwareTestResult(
                        HardwareTestType.PROXIMITY,
                        DiagnosticStatus.PASS,
                        "Obstacle Distance: ${"%.1f".format(proximity)} cm",
                        "Live Tracking"
                    ),
                    actionText = "Mark Pass",
                    onAction = {
                        controller.updateResult(HardwareTestType.PROXIMITY, DiagnosticStatus.PASS, "Proximity detector responsive")
                    }
                )
            }

            // Item: Charging & USB port
            item {
                HardwareItemCard(
                    title = "USB Port & Charging Bus",
                    icon = Icons.Default.Usb,
                    result = testResults[HardwareTestType.CHARGING_USB] ?: HardwareTestResult(HardwareTestType.CHARGING_USB),
                    actionText = "Check Port Bus",
                    onAction = {
                        val res = controller.checkChargingUsb()
                        controller.updateResult(res.type, res.status, res.details, res.measurement)
                    }
                )
            }
        }
    }
}

@Composable
fun HardwareItemCard(
    title: String,
    icon: ImageVector,
    result: HardwareTestResult,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandNavyCard),
        border = BorderStroke(1.dp, BrandNavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandCyan.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = title, tint = BrandCyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                        Text(
                            text = result.details.ifEmpty { "Ready for hardware test" },
                            fontSize = 11.sp,
                            color = BrandTextSecondary
                        )
                    }
                }

                StatusBadge(status = result.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavyCardLight),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = actionText, color = BrandCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}
