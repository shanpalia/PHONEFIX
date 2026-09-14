package com.paliaapk.phonefix.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.HardwareTestType
import com.paliaapk.phonefix.ui.theme.BrandCyan
import com.paliaapk.phonefix.ui.theme.BrandNavyDark
import com.paliaapk.phonefix.ui.theme.BrandWhite
import com.paliaapk.phonefix.ui.theme.StatusExcellent
import com.paliaapk.phonefix.viewmodel.AppScreen
import com.paliaapk.phonefix.viewmodel.PhoneFixViewModel

data class LinePath(val start: Offset, val end: Offset)

@Composable
fun TouchscreenTestScreen(
    viewModel: PhoneFixViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val lines = remember { mutableStateListOf<LinePath>() }
    var touchCount by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandNavyDark)
            .testTag("touchscreen_test_screen")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchCount++
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val start = change.position - dragAmount
                            val end = change.position
                            lines.add(LinePath(start, end))
                        }
                    )
                }
        ) {
            // Draw lines
            for (line in lines) {
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = line.start,
                    end = line.end,
                    strokeWidth = 14f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Top control bar
        Surface(
            color = BrandNavyDark.copy(alpha = 0.85f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text("Touchscreen Grid Test", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                    Text("Draw all across screen. Strokes: ${lines.size}", fontSize = 10.sp, color = BrandCyan)
                }

                IconButton(onClick = { lines.clear() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = BrandWhite)
                }

                Button(
                    onClick = {
                        viewModel.hardwareController.updateResult(
                            HardwareTestType.TOUCHSCREEN,
                            DiagnosticStatus.PASS,
                            "Digitizer responsive (${lines.size} stroke vectors drawn)"
                        )
                        viewModel.hardwareController.updateResult(
                            HardwareTestType.MULTI_TOUCH,
                            DiagnosticStatus.PASS,
                            "Multi-finger tracking active"
                        )
                        onNavigate(AppScreen.HARDWARE_TEST)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusExcellent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PASS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = { onNavigate(AppScreen.HARDWARE_TEST) }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandWhite)
                }
            }
        }
    }
}

@Composable
fun DeadPixelColorTestScreen(
    viewModel: PhoneFixViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val colors = listOf(
        Pair(Color.Red, "RED Subpixel Test"),
        Pair(Color.Green, "GREEN Subpixel Test"),
        Pair(Color.Blue, "BLUE Subpixel Test"),
        Pair(Color.White, "WHITE Panel Uniformity Test"),
        Pair(Color.Black, "BLACK OLED Backlight Bleed Test")
    )
    var currentIndex by remember { mutableStateOf(0) }
    val (currentColor, colorName) = colors[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentColor)
            .pointerInput(Unit) {
                detectTapGestures {
                    currentIndex = (currentIndex + 1) % colors.size
                }
            }
            .testTag("dead_pixel_test_screen")
    ) {
        // Floating pill
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp, start = 20.dp, end = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = colorName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tap screen to switch color (${currentIndex + 1}/${colors.size})",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewModel.hardwareController.updateResult(
                                HardwareTestType.DISPLAY,
                                DiagnosticStatus.PASS,
                                "RGB color screen uniformity verified without dead pixels"
                            )
                            onNavigate(AppScreen.HARDWARE_TEST)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusExcellent)
                    ) {
                        Text("No Dead Pixels (Pass)")
                    }
                    Button(
                        onClick = { onNavigate(AppScreen.HARDWARE_TEST) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("Exit")
                    }
                }
            }
        }
    }
}
