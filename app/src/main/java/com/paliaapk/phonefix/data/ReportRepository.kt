package com.paliaapk.phonefix.data

import kotlinx.coroutines.flow.Flow

class ReportRepository(private val dao: ScanReportDao) {
    val allReports: Flow<List<ScanReportEntity>> = dao.getAllReports()
    val latestReport: Flow<ScanReportEntity?> = dao.getLatestReport()

    suspend fun saveReport(report: ScanReportEntity): Long = dao.insertReport(report)
    suspend fun deleteReport(reportId: Long) = dao.deleteReport(reportId)
    suspend fun clearHistory() = dao.clearAllReports()
}
