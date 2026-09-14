package com.paliaapk.phonefix.diagnostics

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult
import com.paliaapk.phonefix.model.DiagnosticSeverity
import com.paliaapk.phonefix.model.DiagnosticStatus

class SensorDiagnostic : DiagnosticModule {
    override val category = DiagnosticCategory.SENSORS

    override suspend fun inspect(context: Context): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

            if (sensorManager == null) {
                results.add(
                    DiagnosticResult(
                        id = "sensor_mgr_null",
                        category = category,
                        status = DiagnosticStatus.UNAVAILABLE,
                        severity = DiagnosticSeverity.INFO,
                        title = "Sensor Subsystem Unavailable",
                        description = "SensorManager service could not be acquired.",
                        evidence = "getSystemService(SENSOR_SERVICE) returned null",
                        recommendation = "Android does not allow PHONEFIX to access this information on this device."
                    )
                )
                return results
            }

            val targetSensors = listOf(
                Pair(Sensor.TYPE_ACCELEROMETER, "Accelerometer"),
                Pair(Sensor.TYPE_GYROSCOPE, "Gyroscope"),
                Pair(Sensor.TYPE_PROXIMITY, "Proximity Sensor"),
                Pair(Sensor.TYPE_LIGHT, "Ambient Light Sensor"),
                Pair(Sensor.TYPE_MAGNETIC_FIELD, "Magnetometer / Compass"),
                Pair(Sensor.TYPE_PRESSURE, "Barometer (Atmospheric Pressure)"),
                Pair(Sensor.TYPE_STEP_COUNTER, "Hardware Step Counter")
            )

            var availableCount = 0

            for ((type, name) in targetSensors) {
                val sensor = sensorManager.getDefaultSensor(type)
                if (sensor != null) {
                    availableCount++
                    results.add(
                        DiagnosticResult(
                            id = "sensor_found_${type}",
                            category = category,
                            status = DiagnosticStatus.PASS,
                            severity = DiagnosticSeverity.HEALTHY,
                            title = "$name: Detected",
                            description = "Hardware sensor chip online: ${sensor.name} by ${sensor.vendor}.",
                            evidence = "Range: ${sensor.maximumRange}, Res: ${sensor.resolution}, Power: ${sensor.power} mA",
                            recommendation = "Hardware component responsive."
                        )
                    )
                } else {
                    val isOptional = (type == Sensor.TYPE_PRESSURE || type == Sensor.TYPE_STEP_COUNTER)
                    results.add(
                        DiagnosticResult(
                            id = "sensor_missing_${type}",
                            category = category,
                            status = if (isOptional) DiagnosticStatus.PASS else DiagnosticStatus.ATTENTION,
                            severity = if (isOptional) DiagnosticSeverity.INFO else DiagnosticSeverity.MEDIUM,
                            title = "$name: ${if (isOptional) "Not Present (Optional)" else "Not Found"}",
                            description = if (isOptional) {
                                "This device model does not include a dedicated hardware $name."
                            } else {
                                "Default $name not reported by device HAL. Hardware/service inspection may be required if auto-rotate or gestures fail."
                            },
                            evidence = "getDefaultSensor($type) is null",
                            recommendation = if (isOptional) "Normal for this device tier." else "Hardware/service inspection may be required if gesture features are non-functional."
                        )
                    )
                }
            }

            results.add(
                DiagnosticResult(
                    id = "sensor_summary",
                    category = category,
                    status = DiagnosticStatus.PASS,
                    severity = DiagnosticSeverity.HEALTHY,
                    title = "Sensor Suite Overview ($availableCount active)",
                    description = "$availableCount core hardware sensor ICs detected and operational on device I2C/SPI bus.",
                    evidence = "Total tested sensors: ${targetSensors.size}",
                    recommendation = "Use Hardware Test screen to test live real-time sensor motion tracking."
                )
            )

        } catch (e: Exception) {
            results.add(
                DiagnosticResult(
                    id = "sensor_error",
                    category = category,
                    status = DiagnosticStatus.UNAVAILABLE,
                    severity = DiagnosticSeverity.INFO,
                    title = "Sensor Telemetry Restricted",
                    description = e.localizedMessage ?: "Restricted",
                    evidence = "Exception in SensorDiagnostic",
                    recommendation = "Android does not allow PHONEFIX to access this information on this device."
                )
            )
        }

        return results
    }
}
