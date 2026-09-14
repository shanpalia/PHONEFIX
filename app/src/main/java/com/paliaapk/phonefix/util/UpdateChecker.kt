package com.paliaapk.phonefix.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private const val WEBSITE_URL = "https://shanpalia.github.io/WebsitePaliaAPK_V.2/"

 data class UpdateCheckResult(
    val reachable: Boolean,
    val currentVersion: String,
    val latestVersion: String?,
    val updateAvailable: Boolean,
    val message: String
)

object UpdateChecker {
    suspend fun check(currentVersion: String): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(WEBSITE_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "PHONEFIX/$currentVersion")
            }
            connection.connect()
            if (connection.responseCode !in 200..299) {
                return@withContext UpdateCheckResult(false, currentVersion, null, false, "Could not check the PaliaAPK website.")
            }
            val html = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val candidates = listOf(
                Regex("(?:latestVersion|latest_version|versionName|appVersion)\\s*[:=]\\s*[\\\"']?(\\d+(?:\\.\\d+){1,3})", RegexOption.IGNORE_CASE),
                Regex("PHONEFIX[^\\n]{0,120}?v(\\d+(?:\\.\\d+){1,3})", RegexOption.IGNORE_CASE),
                Regex("(?:href|src)=[\\\"'][^\\\"']*?v(\\d+(?:\\.\\d+){1,3})[^\\\"']*", RegexOption.IGNORE_CASE)
            )
            val remote = candidates.asSequence()
                .mapNotNull { it.find(html)?.groupValues?.getOrNull(1) }
                .map { it.trim() }
                .distinct()
                .maxWithOrNull(Comparator { a, b -> compareVersions(a, b) })

            if (remote == null) {
                return@withContext UpdateCheckResult(
                    reachable = true,
                    currentVersion = currentVersion,
                    latestVersion = null,
                    updateAvailable = false,
                    message = "Website reached, but no PHONEFIX version was published there yet."
                )
            }

            val newer = compareVersions(remote, currentVersion) > 0
            UpdateCheckResult(
                reachable = true,
                currentVersion = currentVersion,
                latestVersion = remote,
                updateAvailable = newer,
                message = if (newer) "PHONEFIX $remote is available." else "PHONEFIX is up to date."
            )
        } catch (_: Exception) {
            UpdateCheckResult(false, currentVersion, null, false, "No internet connection or website unavailable.")
        }
    }

    fun openWebsite(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL)))
    }

    private fun compareVersions(a: String, b: String): Int {
        val aa = versionParts(a)
        val bb = versionParts(b)
        for (i in 0 until maxOf(aa.size, bb.size)) {
            val x = aa.getOrElse(i) { 0 }
            val y = bb.getOrElse(i) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return 0
    }

    private fun versionParts(version: String): List<Int> =
        version.removePrefix("v").split('.').mapNotNull { it.toIntOrNull() }
}
