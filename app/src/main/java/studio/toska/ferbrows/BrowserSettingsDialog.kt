package studio.toska.ferbrows

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun BrowserSettingsDialog(
    settings: BrowserSettings,
    onSave: (BrowserSettings) -> Unit,
    onClearHistory: () -> Unit,
    onClearSiteData: () -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(settings) { mutableStateOf(settings) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        BoxWithConstraints(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.ime)),
            contentAlignment = Alignment.Center
        ) {
        Surface(Modifier.fillMaxWidth().fillMaxHeight(0.96f).heightIn(max = maxHeight), color = Background) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 8.dp, end = 18.dp, top = 5.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.ArrowBack, "Назад", tint = White) }
                    Column(Modifier.weight(1f).padding(start = 8.dp)) {
                        Text("Настройки", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Настрой FerBrows под себя", color = Muted, fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.Settings, null, tint = Lime, modifier = Modifier.size(22.dp))
                }
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsGroup("ОСНОВНЫЕ", Icons.Filled.Language) {
                        SettingsAction("Поисковая система", draft.searchEngine.label, "Поиск из адресной строки", onClick = {
                            val engines = SearchEngine.values()
                            draft = draft.copy(searchEngine = engines[(engines.indexOf(draft.searchEngine) + 1) % engines.size])
                        })
                        HorizontalDivider(color = PanelSoft)
                        Column(Modifier.fillMaxWidth().padding(vertical = 13.dp)) {
                            Text("Домашняя страница", color = White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Оставь пустым, чтобы открывать новую вкладку", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp, bottom = 9.dp))
                            OutlinedTextField(
                                value = draft.homePage,
                                onValueChange = { draft = draft.copy(homePage = it) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("https://example.com") },
                                shape = RoundedCornerShape(13.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Lime,
                                    unfocusedBorderColor = PanelSoft,
                                    focusedTextColor = White,
                                    unfocusedTextColor = White,
                                    cursorColor = Lime
                                )
                            )
                        }
                    }
                    SettingsGroup("ПРИВАТНОСТЬ И БЕЗОПАСНОСТЬ", Icons.Filled.PrivacyTip) {
                        SettingsToggle("Защита от трекеров", "Блокировка известных отслеживающих доменов", draft.trackerProtectionEnabled) {
                            draft = draft.copy(trackerProtectionEnabled = it)
                        }
                        HorizontalDivider(color = PanelSoft)
                        SettingsToggle("JavaScript", "Необходим для работы многих сайтов", draft.javaScriptEnabled) {
                            draft = draft.copy(javaScriptEnabled = it)
                        }
                        Text(
                            "Сайты не получают доступ к камере, микрофону и геолокации автоматически.",
                            color = Muted, fontSize = 11.sp, lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 5.dp)
                        )
                    }
                    SettingsGroup("ВНЕШНИЙ ВИД", Icons.Filled.Palette) {
                        SettingsToggle("Меньше анимаций", "Сократить декоративные переходы", draft.reducedMotion) {
                            draft = draft.copy(reducedMotion = it)
                        }
                    }
                    SettingsGroup("ДАННЫЕ И ХРАНИЛИЩЕ", Icons.Filled.Storage) {
                        SettingsAction("Очистить историю", "Удалить список посещённых страниц", "Действие нельзя отменить", onClearHistory, destructive = true)
                        HorizontalDivider(color = PanelSoft)
                        SettingsAction("Очистить данные сайтов", "Кэш, cookies и локальные данные", "Может привести к выходу из аккаунтов", onClearSiteData, destructive = true)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("FerBrows · настройки браузера", color = Muted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(10.dp))
                }
                Surface(color = Panel, shadowElevation = 8.dp) {
                    Row(
                        Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(start = 18.dp, end = 18.dp, top = 9.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                            Text("Отмена", color = Muted)
                        }
                        Button(onClick = { onSave(draft); onDismiss() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Background), shape = RoundedCornerShape(14.dp)) {
                            Text("Сохранить", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Lime.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Lime, modifier = Modifier.size(16.dp))
            }
            Text(title, color = Lime, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp, modifier = Modifier.padding(start = 9.dp))
        }
        Surface(color = Panel, shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.animateContentSize().padding(horizontal = 15.dp, vertical = 5.dp), content = content)
        }
    }
}

@Composable
private fun SettingsAction(title: String, subtitle: String, detail: String, onClick: () -> Unit, destructive: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, color = if (destructive) Color(0xFFFF8A80) else White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
            Text(detail, color = Muted.copy(alpha = 0.75f), fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(if (destructive) Icons.Filled.DeleteSweep else Icons.Filled.ChevronRight, null, tint = if (destructive) Color(0xFFFF8A80) else Muted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, color = White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedThumbColor = Background, checkedTrackColor = Lime, uncheckedThumbColor = Muted, uncheckedTrackColor = PanelSoft))
    }
}
