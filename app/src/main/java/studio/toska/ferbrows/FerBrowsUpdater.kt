package studio.toska.ferbrows

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class FerBrowsCountry(val code: String, val name: String, val flag: String)
data class FerBrowsUpdate(val version: String, val downloadUrl: String, val fileName: String)
data class FerBrowsDownloadProgress(val downloaded: Long, val total: Long, val percent: Int, val finished: Boolean, val failed: Boolean)

object FerBrowsNetworkInfo {
    fun lookupCountry(): FerBrowsCountry? {
        return runCatching {
            val connection = (URL("https://ipapi.co/json/").openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "FerBrows/Android")
            }
            try {
                val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                val code = json.optString("country_code").uppercase(Locale.ROOT)
                    .takeIf { it.matches(Regex("[A-Z]{2}")) } ?: error("Country code missing")
                val name = Locale("", code).getDisplayCountry(Locale("ru")).ifBlank { code }
                val flag = code.map { Character.toChars(0x1F1E6 + it.code - 65).concatToString() }.joinToString("")
                FerBrowsCountry(code, name, flag)
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }
}

object FerBrowsUpdater {
    fun checkLatest(installedVersion: String): FerBrowsUpdate? {
        return runCatching {
            val connection = (URL("https://api.github.com/repos/Toska-0/FerBrows/releases/latest").openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "FerBrows-Android-Updater")
            }
            try {
                val release = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                if (release.optBoolean("draft", false) || release.optBoolean("prerelease", false)) null
                else {
                    val version = release.optString("tag_name").removePrefix("v").removePrefix("V")
                    val assets = release.optJSONArray("assets")
                    if (version.isBlank() || compareVersions(version, installedVersion) <= 0 || assets == null) null
                    else {
                        (0 until assets.length()).asSequence()
                            .mapNotNull { assets.optJSONObject(it) }
                            .map { it.optString("name") to it.optString("browser_download_url") }
                            .firstOrNull { (name, url) -> name.endsWith(".apk", true) && url.startsWith("https://") }
                            ?.let { (name, url) -> FerBrowsUpdate(version, url, name) }
                    }
                }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private fun compareVersions(left: String, right: String): Int {
        val a = left.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
        val b = right.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val diff = a.getOrElse(i) { 0 }.compareTo(b.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }

    fun enqueue(context: Context, update: FerBrowsUpdate): Long {
        val request = DownloadManager.Request(Uri.parse(update.downloadUrl))
            .setTitle("FerBrows ${update.version}")
            .setDescription("Загрузка обновления браузера")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, update.fileName)
        return (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }

    fun progress(context: Context, id: Long): FerBrowsDownloadProgress {
        val cursor = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
            .query(DownloadManager.Query().setFilterById(id))
        cursor.use {
            if (it == null || !it.moveToFirst()) return FerBrowsDownloadProgress(0, 0, 0, false, true)
            val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
            val finished = status == DownloadManager.STATUS_SUCCESSFUL
            val failed = status == DownloadManager.STATUS_FAILED ||
                (status == DownloadManager.STATUS_PAUSED && reason != 0)
            val percent = if (total > 0) ((downloaded * 100L) / total).toInt().coerceIn(0, 100) else 0
            return FerBrowsDownloadProgress(downloaded, total, percent, finished, failed)
        }
    }

    fun install(context: Context, id: Long): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
                false
            } else {
                val uri = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                    .getUriForDownloadedFile(id) ?: return false
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
                true
            }
        }.getOrDefault(false)
    }
}
