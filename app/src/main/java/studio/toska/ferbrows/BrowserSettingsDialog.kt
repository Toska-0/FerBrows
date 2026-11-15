package studio.toska.ferbrows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

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
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Настройки", color = White, fontWeight = FontWeight.Bold)
                Text("Настрой FerBrows под себя", color = Muted, fontSize = 11.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 470.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                SettingsLabel("ОСНОВНЫЕ")
                Surface(color = PanelSoft, shape = RoundedCornerShape(15.dp)) {
                    Column(Modifier.padding(horizontal = 13.dp, vertical = 5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val engines = SearchEngine.values()
                                draft = draft.copy(searchEngine = engines[(engines.indexOf(draft.searchEngine) + 1) % engines.size])
                            }.padding(vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Поисковая система", color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Для запросов из адресной строки", color = Muted, fontSize = 10.sp)
                            }
                            Text(draft.searchEngine.label + "  ›", color = Lime, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedTextField(
                            value = draft.homePage,
                            onValueChange = { draft = draft.copy(homePage = it) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            label = { Text("Домашняя страница") },
                            placeholder = { Text("Пусто — новая вкладка") },
                            singleLine = true
                        )
                    }
                }

                SettingsLabel("ПРИВАТНОСТЬ И САЙТЫ")
                Surface(color = PanelSoft, shape = RoundedCornerShape(15.dp)) {
                    Column(Modifier.padding(horizontal = 13.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        SettingSwitch("JavaScript", "Нужен большинству современных сайтов", draft.javaScriptEnabled) {
                            draft = draft.copy(javaScriptEnabled = it)
                        }
                        SettingSwitch("Защита от трекеров", "Блокирует известные домены из встроенного списка", draft.trackerProtectionEnabled) {
                            draft = draft.copy(trackerProtectionEnabled = it)
                        }
                    }
                }
                Surface(color = PanelSoft, shape = RoundedCornerShape(13.dp)) {
                    Text(
                        "Камера, микрофон и геолокация не выдаются сайтам автоматически. Настройка разрешений пока недоступна.",
                        color = Muted,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                SettingsLabel("ВНЕШНИЙ ВИД")
                Surface(color = PanelSoft, shape = RoundedCornerShape(15.dp)) {
                    SettingSwitch("Меньше анимаций", "Сократить декоративные переходы", draft.reducedMotion) {
                        draft = draft.copy(reducedMotion = it)
                    }
                }

                SettingsLabel("ДАННЫЕ")
                Surface(color = PanelSoft, shape = RoundedCornerShape(15.dp)) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 3.dp)) {
                        TextButton(onClick = onClearHistory) { Text("Очистить историю", color = Lime) }
                        TextButton(onClick = onClearSiteData) { Text("Очистить кэш и данные сайтов", color = Lime) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft); onDismiss() }) { Text("Сохранить", color = Lime, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = Muted) }
        }
    )
}

@Composable
private fun SettingsLabel(value: String) {
    Text(value, color = Lime, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
