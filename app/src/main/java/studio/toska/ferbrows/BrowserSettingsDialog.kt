package studio.toska.ferbrows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrowserSettingsDialog(
    settings: BrowserSettings,
    onSave: (BrowserSettings) -> Unit,
    onClearHistory: () -> Unit,
    onClearSiteData: () -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(settings) { mutableStateOf(settings) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = { Text("Настройки браузера", color = White) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Поисковая система", color = Muted)
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val engines = SearchEngine.values()
                        draft = draft.copy(searchEngine = engines[(engines.indexOf(draft.searchEngine) + 1) % engines.size])
                    }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(draft.searchEngine.label, color = White, modifier = Modifier.weight(1f))
                    Text("Изменить", color = Lime)
                }
                OutlinedTextField(
                    value = draft.homePage,
                    onValueChange = { draft = draft.copy(homePage = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Домашняя страница (пусто — новая вкладка)") },
                    singleLine = true
                )
                SettingSwitch("JavaScript", "Отключение может нарушить работу сайтов", draft.javaScriptEnabled) {
                    draft = draft.copy(javaScriptEnabled = it)
                }
                SettingSwitch("Блокировка известных трекеров", "Небольшой встроенный список доменов, не полная защита", draft.trackerProtectionEnabled) {
                    draft = draft.copy(trackerProtectionEnabled = it)
                }
                SettingSwitch("Уменьшить анимации", "Сокращает декоративные переходы интерфейса", draft.reducedMotion) {
                    draft = draft.copy(reducedMotion = it)
                }
                TextButton(onClick = onClearHistory) { Text("Очистить историю", color = Lime) }
                TextButton(onClick = onClearSiteData) { Text("Очистить кэш и данные сайтов", color = Lime) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft); onDismiss() }) { Text("Сохранить", color = Lime) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = Muted) }
        }
    )
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, color = White)
            Text(subtitle, color = Muted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
