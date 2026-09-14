package com.paliaapk.phonefix.diagnostics

import android.content.Context
import com.paliaapk.phonefix.model.DiagnosticCategory
import com.paliaapk.phonefix.model.DiagnosticResult

interface DiagnosticModule {
    val category: DiagnosticCategory
    suspend fun inspect(context: Context): List<DiagnosticResult>
}
