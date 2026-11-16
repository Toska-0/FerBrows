package studio.toska.ferbrows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FerBrowsUpdateDialog(
    version: String,
    releaseNotes: String,
    downloading: Boolean,
    progress: Int,
    failed: Boolean,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!downloading) onDismiss() },
        containerColor = Panel,
        title = { Text("Доступно обновление", color = White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("FerBrows $version уже доступен. Обновление будет установлено поверх текущей версии.", color = Muted)
                Text("Что нового", color = White, fontWeight = FontWeight.SemiBold)
                Text(releaseNotes.ifBlank { "Исправления ошибок и улучшения стабильности." }, color = Muted)
                if (downloading) {
                    LinearProgressIndicator(
                        progress = progress.coerceIn(0, 100) / 100f,
                        modifier = Modifier.fillMaxWidth().height(5.dp),
                        color = Lime,
                        trackColor = PanelSoft
                    )
                    Text(if (progress > 0) "Загрузка: $progress%" else "Подготовка загрузки…", color = White)
                }
                if (failed) Text("Не удалось скачать APK. Проверь подключение и попробуй ещё раз.", color = androidx.compose.ui.graphics.Color(0xFFFFB454))
                if (!downloading && !failed) Text("После загрузки Android попросит подтвердить установку.", color = Muted)
            }
        },
        confirmButton = {
            TextButton(onClick = if (downloading) onDismiss else if (progress >= 100) onInstall else onDownload, enabled = !downloading) {
                Text(if (progress >= 100) "Установить" else "Обновить", color = Lime)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !downloading) { Text("Позже", color = Muted) }
        }
    )
}
