package com.paliaapk.phonefix.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_reports")
data class ScanReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val healthScore: Int,
    val scoreRating: String, // "Excellent", "Good Condition", "Needs Attention", "Critical"
    val checksCount: Int,
    val passedCount: Int,
    val attentionCount: Int,
    val failedCount: Int,
    val batteryPct: Int,
    val storageUsedPct: Int,
    val ramUsedPct: Int,
    val tempC: Float,
    val problemsJson: String = "",
    val summaryNotes: String = ""
)
