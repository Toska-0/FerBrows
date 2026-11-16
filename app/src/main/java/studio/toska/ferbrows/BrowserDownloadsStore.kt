package studio.toska.ferbrows

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri

data class BrowserDownload(
    val id: Long,
    val title: String,
    val mimeType: String?,
    val status: Int,
    val downloadedBytes: Long,
    val totalBytes: Long
) {
    val progress: Int
        get() = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100) else 0
}

class BrowserDownloadsStore(context: Context) {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val prefs = appContext.getSharedPreferences("ferbrows_downloads", Context.MODE_PRIVATE)

    fun remember(id: Long) {
        val ids = prefs.getStringSet("ids", emptySet()).orEmpty().toMutableSet()
        ids.add(id.toString())
        prefs.edit().putStringSet("ids", ids).apply()
    }

    fun query(): List<BrowserDownload> {
        val ids = prefs.getStringSet("ids", emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }
        return ids.mapNotNull { id ->
            runCatching {
                manager.query(DownloadManager.Query().setFilterById(id)).use { cursor ->
                    if (!cursor.moveToFirst()) return@use null
                    fun long(column: String): Long = cursor.getLong(cursor.getColumnIndexOrThrow(column))
                    fun string(column: String): String? {
                        val index = cursor.getColumnIndex(column)
                        return if (index >= 0) cursor.getString(index) else null
                    }
                    BrowserDownload(
                        id = id,
                        title = string(DownloadManager.COLUMN_TITLE) ?: "Файл",
                        mimeType = string(DownloadManager.COLUMN_MEDIA_TYPE),
                        status = long(DownloadManager.COLUMN_STATUS).toInt(),
                        downloadedBytes = long(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
                        totalBytes = long(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )
                }
            }.getOrNull()
        }.sortedByDescending { it.id }
    }

    fun open(id: Long) {
        val uri: Uri = manager.getUriForDownloadedFile(id) ?: return
        val mime = manager.getMimeTypeForDownloadedFile(id) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    fun remove(id: Long) {
        manager.remove(id)
        val ids = prefs.getStringSet("ids", emptySet()).orEmpty().toMutableSet()
        ids.remove(id.toString())
        prefs.edit().putStringSet("ids", ids).apply()
    }

    fun clearCompleted() {
        val completedIds = query()
            .filter { it.status == DownloadManager.STATUS_SUCCESSFUL || it.status == DownloadManager.STATUS_FAILED }
            .map { it.id }
        if (completedIds.isEmpty()) return
        completedIds.forEach { manager.remove(it) }
        val ids = prefs.getStringSet("ids", emptySet()).orEmpty().toMutableSet()
        completedIds.forEach { ids.remove(it.toString()) }
        prefs.edit().putStringSet("ids", ids).apply()
    }

    companion object {
        const val STATUS_PENDING = DownloadManager.STATUS_PENDING
        const val STATUS_RUNNING = DownloadManager.STATUS_RUNNING
        const val STATUS_PAUSED = DownloadManager.STATUS_PAUSED
        const val STATUS_SUCCESSFUL = DownloadManager.STATUS_SUCCESSFUL
        const val STATUS_FAILED = DownloadManager.STATUS_FAILED
    }
}
