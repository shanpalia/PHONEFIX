package com.paliaapk.phonefix.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanReportDao {
    @Query("SELECT * FROM scan_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ScanReportEntity>>

    @Query("SELECT * FROM scan_reports ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReport(): Flow<ScanReportEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ScanReportEntity): Long

    @Query("DELETE FROM scan_reports WHERE id = :reportId")
    suspend fun deleteReport(reportId: Long)

    @Query("DELETE FROM scan_reports")
    suspend fun clearAllReports()
}
