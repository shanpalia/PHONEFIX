package com.paliaapk.phonefix.diagnostics

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.paliaapk.phonefix.model.AodWakeEvent
import com.paliaapk.phonefix.model.AodWakeSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AodWakeMonitor(private val context: Context) {

    private val _sessionState = MutableStateFlow(AodWakeSession())
    val sessionState: StateFlow<AodWakeSession> = _sessionState.asStateFlow()

    private var sessionJob: Job? = null
    private var lastEventTimeMs: Long = 0L
    private val recordedEvents = mutableListOf<AodWakeEvent>()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            val now = System.currentTimeMillis()
            val action = intent?.action ?: return
            val eventType = when (action) {
                Intent.ACTION_SCREEN_ON -> "SCREEN_ON"
                Intent.ACTION_SCREEN_OFF -> "SCREEN_OFF"
                Intent.ACTION_USER_PRESENT -> "USER_PRESENT"
                else -> action
            }

            val interval = if (lastEventTimeMs > 0) now - lastEventTimeMs else 0L
            lastEventTimeMs = now

            val event = AodWakeEvent(
                timestamp = now,
                eventType = eventType,
                intervalSinceLastMs = interval,
                sensorNotes = if (interval in 500..8000 && eventType == "SCREEN_ON") {
                    "Rapid wake-up detected (${interval / 1000}s interval)"
                } else "Normal interval transition"
            )

            recordedEvents.add(event)
            updateSession()
        }
    }

    fun startMonitoring(testDurationSec: Int = 30) {
        stopMonitoring()
        recordedEvents.clear()
        lastEventTimeMs = System.currentTimeMillis()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        try {
            context.registerReceiver(screenReceiver, filter)
        } catch (e: Exception) {
            // catch
        }

        _sessionState.value = AodWakeSession(
            isRunning = true,
            sessionDurationSec = 0,
            wakeEventsCount = 0,
            eventsList = emptyList(),
            suspiciousCyclesDetected = false,
            possibleCauses = emptyList(),
            verdict = "Monitoring active. Keep phone locked with AOD enabled..."
        )

        sessionJob = scope.launch {
            var elapsed = 0
            while (elapsed < testDurationSec) {
                delay(1000)
                elapsed++
                val current = _sessionState.value
                _sessionState.value = current.copy(sessionDurationSec = elapsed)
            }
            evaluateSession()
            unregisterReceiverSafely()
        }
    }

    fun simulateWakeTest(simulateAnomalous: Boolean) {
        stopMonitoring()
        recordedEvents.clear()
        val now = System.currentTimeMillis()

        if (simulateAnomalous) {
            recordedEvents.add(AodWakeEvent(now - 18000, "SCREEN_OFF", 0, "Locked by user"))
            recordedEvents.add(AodWakeEvent(now - 14000, "SCREEN_ON", 4000, "Rapid wake-up detected (4s)"))
            recordedEvents.add(AodWakeEvent(now - 11000, "SCREEN_OFF", 3000, "Screen timeout"))
            recordedEvents.add(AodWakeEvent(now - 7000, "SCREEN_ON", 4000, "Rapid wake-up detected (4s)"))
            recordedEvents.add(AodWakeEvent(now - 4000, "SCREEN_OFF", 3000, "Screen timeout"))
            recordedEvents.add(AodWakeEvent(now - 1000, "SCREEN_ON", 3000, "Rapid wake-up detected (3s)"))
        } else {
            recordedEvents.add(AodWakeEvent(now - 25000, "SCREEN_OFF", 0, "Locked by user"))
            recordedEvents.add(AodWakeEvent(now - 1000, "SCREEN_OFF", 24000, "Idle standby sustained"))
        }

        val wakeCount = recordedEvents.count { it.eventType == "SCREEN_ON" }
        val suspicious = wakeCount >= 2

        val causes = if (suspicious) {
            listOf(
                "Lock screen notifications waking display repeatedly",
                "Lift-to-wake gesture triggering on subtle table or pocket movements",
                "Double-tap / single-tap wake sensitivity miscalibration",
                "Proximity sensor false trigger in pocket or bag",
                "Third-party background application acquiring partial WakeLock",
                "AOD system ambient firmware timeout anomaly"
            )
        } else emptyList()

        _sessionState.value = AodWakeSession(
            isRunning = false,
            sessionDurationSec = 30,
            wakeEventsCount = wakeCount,
            eventsList = recordedEvents.toList(),
            suspiciousCyclesDetected = suspicious,
            possibleCauses = causes,
            verdict = if (suspicious) {
                "Possible AOD Wake-Up Problem Detected (${wakeCount} wake events in 30s)"
            } else {
                "No Suspicious Wake Cycles Detected. Display remained in deep standby."
            }
        )
    }

    private fun updateSession() {
        val wakeCount = recordedEvents.count { it.eventType == "SCREEN_ON" }
        val current = _sessionState.value
        _sessionState.value = current.copy(
            wakeEventsCount = wakeCount,
            eventsList = recordedEvents.toList()
        )
    }

    private fun evaluateSession() {
        val wakeCount = recordedEvents.count { it.eventType == "SCREEN_ON" }
        val suspicious = wakeCount >= 2

        val causes = if (suspicious) {
            listOf(
                "Lock-screen notifications triggering wake pulses",
                "Lift-to-wake gesture sensitivity triggering in pocket",
                "Tap-to-wake false touches",
                "Proximity or motion sensor state changes",
                "Third-party background application holding partial WakeLock",
                "AOD display settings or software issue"
            )
        } else emptyList()

        val current = _sessionState.value
        _sessionState.value = current.copy(
            isRunning = false,
            wakeEventsCount = wakeCount,
            suspiciousCyclesDetected = suspicious,
            possibleCauses = causes,
            verdict = if (suspicious) {
                "Possible AOD Wake-Up Problem Detected ($wakeCount wakes during ${current.sessionDurationSec}s test)"
            } else {
                "Standby Behavior Normal ($wakeCount unexpected wakes during test)"
            }
        )
    }

    fun stopMonitoring() {
        sessionJob?.cancel()
        sessionJob = null
        unregisterReceiverSafely()
        val current = _sessionState.value
        if (current.isRunning) {
            _sessionState.value = current.copy(isRunning = false, verdict = "Test cancelled by user")
        }
    }

    private fun unregisterReceiverSafely() {
        try {
            context.unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // ignore
        }
    }
}
