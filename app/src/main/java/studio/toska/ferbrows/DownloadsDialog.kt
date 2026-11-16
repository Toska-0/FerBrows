package studio.toska.ferbrows

import android.app.DownloadManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DownloadsDialog(
    downloads: List<BrowserDownload>,
    onOpen: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onClearCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = { Text("Загрузки", color = White) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (downloads.any { it.status == DownloadManager.STATUS_SUCCESSFUL || it.status == DownloadManager.STATUS_FAILED }) {
                    TextButton(onClick = onClearCompleted) { Text("Удалить завершённые файлы", color = Muted, fontSize = 11.sp) }
                }
                if (downloads.isEmpty()) Text("Загрузок пока нет", color = Muted)
                downloads.forEach { item ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, color = White, maxLines = 1)
                                Text(statusLabel(item), color = Muted, fontSize = 11.sp)
                            }
                            if (item.status == DownloadManager.STATUS_SUCCESSFUL) {
                                IconButton(onClick = { onOpen(item.id) }) {
                                    Icon(Icons.Filled.OpenInNew, "Открыть файл", tint = Lime)
                                }
                            }
                            IconButton(onClick = { onRemove(item.id) }) {
                                Icon(Icons.Filled.Delete, "Удалить загрузку", tint = Muted)
                            }
                        }
                        if (item.status == DownloadManager.STATUS_RUNNING || item.status == DownloadManager.STATUS_PENDING) {
                            LinearProgressIndicator(
                                progress = if (item.totalBytes > 0) item.progress / 100f else 0f,
                                modifier = Modifier.fillMaxWidth(),
                                color = Lime,
                                trackColor = PanelSoft
                            )
                            if (item.totalBytes > 0) {
                                Text("${item.progress}%", color = Muted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово", color = Lime) } }
    )
}

private fun statusLabel(item: BrowserDownload): String = when (item.status) {
    DownloadManager.STATUS_PENDING -> "Ожидание"
    DownloadManager.STATUS_RUNNING -> if (item.totalBytes > 0) "Загружается · ${item.progress}%" else "Загружается"
    DownloadManager.STATUS_PAUSED -> "Приостановлено"
    DownloadManager.STATUS_SUCCESSFUL -> "Загрузка завершена"
    DownloadManager.STATUS_FAILED -> "Ошибка загрузки"
    else -> "Состояние неизвестно"
}
