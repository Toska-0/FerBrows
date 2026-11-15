package studio.toska.ferbrows

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Header(onNewTab: () -> Unit, onTabs: () -> Unit, onMenu: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 10.dp, top = 12.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Lime), contentAlignment = Alignment.Center) {
            Text("F", color = Background, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text("FerBrows", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
            Text("FAST · SIMPLE · YOURS", color = Muted, fontSize = 8.sp, letterSpacing = 1.5.sp)
        }
        IconButton(onClick = onNewTab) { Icon(Icons.Filled.Add, "Новая вкладка", tint = Lime) }
        IconButton(onClick = onTabs) { Icon(Icons.Filled.Tab, "Вкладки", tint = White) }
        IconButton(onClick = onMenu) { Icon(Icons.Filled.MoreVert, "Меню", tint = White) }
    }
}

@Composable
fun TabStrip(tabs: List<BrowserTab>, activeId: Int, onSelect: (BrowserTab) -> Unit, onClose: (BrowserTab) -> Unit, reducedMotion: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().then(if (reducedMotion) Modifier else Modifier.animateContentSize()).horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        tabs.forEach { tab ->
            val active = tab.id == activeId
            Row(
                modifier = Modifier.width(150.dp).clip(RoundedCornerShape(topStart = 13.dp, topEnd = 13.dp))
                    .background(if (active) PanelSoft else Color(0xFF0A0E0A))
                    .clickable { onSelect(tab) }.padding(start = 11.dp, end = 5.dp, top = 9.dp, bottom = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (active) Lime else Muted))
                Text((if (tab.isPrivate) "ИНКОГНИТО · " else "") + tab.title.ifBlank { "Новая вкладка" }, color = if (tab.isPrivate) Lime else if (active) White else Muted,
                    fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(1f).padding(start = 7.dp))
                IconButton(onClick = { onClose(tab) }, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Filled.Close, "Закрыть вкладку", tint = Muted, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

@Composable
fun AddressBar(
    value: String,
    isSecure: Boolean,
    isInsecure: Boolean,
    canBookmark: Boolean,
    loading: Boolean,
    progress: Int,
    onValueChange: (String) -> Unit,
    onGo: () -> Unit,
    onRefresh: () -> Unit,
    onBookmark: () -> Unit,
    reducedMotion: Boolean = false
) {
    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(17.dp))
                .background(Panel).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Shield, null, tint = if (isSecure) Green else if (isInsecure) Color(0xFFFFB454) else Muted, modifier = Modifier.size(18.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                singleLine = true,
                textStyle = TextStyle(color = White, fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onGo() }),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) Text("Поиск или адрес сайта", color = Muted, fontSize = 13.sp)
                        inner()
                    }
                }
            )
            IconButton(onClick = onBookmark, modifier = Modifier.size(34.dp), enabled = canBookmark) {
                Icon(Icons.Filled.Star, "В закладки", tint = if (canBookmark) Lime else Muted, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.Refresh, "Обновить", tint = White, modifier = Modifier.size(19.dp))
            }
        }
        AnimatedVisibility(
            visible = loading,
            enter = if (reducedMotion) EnterTransition.None else fadeIn(),
            exit = if (reducedMotion) ExitTransition.None else fadeOut()
        ) {
            LinearProgressIndicator(
                progress = progress.coerceIn(0, 100) / 100f,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(2.dp).clip(CircleShape),
                color = Lime,
                trackColor = Panel
            )
        }
    }
}

@Composable
fun HomeScreen(onSearch: (String) -> Unit, onOpen: (String) -> Unit, bookmarks: List<String>) {
    var query by remember { mutableStateOf("") }
    val sites = listOf(
        Triple("Google", "Поиск в интернете", "https://www.google.com"),
        Triple("YouTube", "Видео и музыка", "https://www.youtube.com"),
        Triple("GitHub", "Разработка", "https://github.com"),
        Triple("Wikipedia", "Энциклопедия", "https://wikipedia.org")
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background).padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Spacer(Modifier.height(30.dp))
            Box(Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(Lime), contentAlignment = Alignment.Center) {
                Text("F", color = Background, fontSize = 34.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(22.dp))
            Text("Твой интернет.", color = White, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            Text("Без лишнего шума.", color = Lime, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            Spacer(Modifier.height(10.dp))
            Text("Быстрый доступ к тому, что важно.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(26.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, null, tint = Lime)
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                    singleLine = true,
                    textStyle = TextStyle(color = White, fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) Text("Что будем искать?", color = Muted, fontSize = 14.sp)
                            inner()
                        }
                    }
                )
                IconButton(onClick = { onSearch(query) }, modifier = Modifier.size(30.dp).clip(CircleShape).background(Lime)) {
                    Icon(Icons.Filled.ArrowForward, null, tint = Background, modifier = Modifier.size(17.dp))
                }
            }
            Spacer(Modifier.height(30.dp))
            Text("БЫСТРЫЙ ДОСТУП", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
            Spacer(Modifier.height(13.dp))
        }
        items(sites.size) { index ->
            val site = sites[index]
            Row(
                Modifier.fillMaxWidth().padding(bottom = 9.dp).clip(RoundedCornerShape(16.dp))
                    .background(Panel).clickable { onOpen(site.third) }.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(PanelSoft), contentAlignment = Alignment.Center) {
                    Text(site.first.take(1), color = Lime, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(site.first, color = White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(site.second, color = Muted, fontSize = 11.sp)
                }
                Icon(Icons.Filled.ArrowForward, null, tint = Muted, modifier = Modifier.size(17.dp))
            }
        }
        item {
            if (bookmarks.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text("ИЗБРАННОЕ", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
                Spacer(Modifier.height(10.dp))
                bookmarks.take(6).forEach { url ->
                    val host = Uri.parse(url).host?.removePrefix("www.") ?: url
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 7.dp).clip(RoundedCornerShape(12.dp))
                            .background(Panel).clickable { onOpen(url) }.padding(horizontal = 13.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(PanelSoft), contentAlignment = Alignment.Center) {
                            Text(host.take(1).uppercase(), color = Lime, fontWeight = FontWeight.Bold)
                        }
                        Text(host, color = White, fontSize = 12.sp, modifier = Modifier.weight(1f).padding(start = 10.dp), maxLines = 1)
                        Icon(Icons.Filled.ArrowForward, null, tint = Muted, modifier = Modifier.size(15.dp))
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Shield, null, tint = Green, modifier = Modifier.size(15.dp))
                Text(" Safe Browsing · блокировка известных трекеров", color = Muted, fontSize = 9.sp)
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun BottomToolbar(
    canBack: Boolean,
    canForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onTabs: () -> Unit,
    tabCount: Int
) {
    Row(
        Modifier.fillMaxWidth().background(Color(0xFF0B100B)).padding(horizontal = 22.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarButton(Icons.Filled.ArrowBack, "Назад", canBack, onBack)
        ToolbarButton(Icons.Filled.ArrowForward, "Вперёд", canForward, onForward)
        ToolbarButton(Icons.Filled.Home, "Домой", true, onHome, highlighted = true)
        Box(contentAlignment = Alignment.Center) {
            IconButton(onClick = onTabs) { Icon(Icons.Filled.Tab, "Вкладки", tint = White) }
            Box(Modifier.padding(start = 15.dp, top = 13.dp).size(15.dp).clip(CircleShape).background(Lime), contentAlignment = Alignment.Center) {
                Text(tabCount.toString(), color = Background, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ToolbarButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit, highlighted: Boolean = false) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(icon, description, tint = when {
            highlighted -> Lime
            enabled -> White
            else -> Color(0xFF455045)
        })
    }
}

@Composable
fun MenuRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).clickable(onClick = onClick).padding(12.dp)
    ) {
        Text(title, color = White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(subtitle, color = Muted, fontSize = 11.sp)
    }
}

@Composable
fun SimpleListDialog(
    title: String,
    items: List<String>,
    emptyText: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    labels: Map<String, String> = emptyMap(),
    onRemove: ((String) -> Unit)? = null,
    onMoveUp: ((String) -> Unit)? = null,
    onMoveDown: ((String) -> Unit)? = null,
    onEdit: ((String) -> Unit)? = null,
    searchable: Boolean = false
) {
    var query by remember(title) { mutableStateOf("") }
    val visibleItems = if (searchable && query.isNotBlank()) items.filter {
        it.contains(query, ignoreCase = true) || labels[it].orEmpty().contains(query, ignoreCase = true)
    } else items
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = { Text(title, color = White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (searchable) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Поиск по истории") }
                    )
                }
                if (visibleItems.isEmpty()) Text(if (items.isEmpty()) emptyText else "Ничего не найдено", color = Muted)
                visibleItems.take(100).forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).clickable { onSelect(item) }.padding(vertical = 7.dp)) {
                            Text(labels[item] ?: item, color = White, fontSize = 12.sp, maxLines = 1)
                            if (labels[item] != null) Text(item, color = Muted, fontSize = 9.sp, maxLines = 1)
                        }
                        if (onEdit != null) IconButton(onClick = { onEdit(item) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Filled.Edit, "Изменить", tint = Muted)
                        }
                        if (onMoveUp != null && index > 0) IconButton(onClick = { onMoveUp(item) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Filled.KeyboardArrowUp, "Выше", tint = Muted)
                        }
                        if (onMoveDown != null && index < items.lastIndex) IconButton(onClick = { onMoveDown(item) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Filled.KeyboardArrowDown, "Ниже", tint = Muted)
                        }
                        if (onRemove != null) IconButton(onClick = { onRemove(item) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Filled.Delete, "Удалить", tint = Muted)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово", color = Lime) } }
    )
}
