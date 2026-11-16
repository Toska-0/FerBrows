package studio.toska.ferbrows

import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
fun Header(onNewTab: () -> Unit, onTabs: () -> Unit, onMenu: () -> Unit, countryName: String = "Определение…", countryFlag: String = "🌐", compact: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().animateContentSize().height(if (compact) 42.dp else 52.dp).padding(start = if (compact) 10.dp else 16.dp, end = 6.dp, top = if (compact) 0.dp else 4.dp, bottom = if (compact) 0.dp else 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = !compact,
            modifier = Modifier.weight(1f),
            enter = fadeIn(tween(220)) + expandHorizontally(animationSpec = tween(320), expandFrom = Alignment.End),
            exit = fadeOut(tween(150)) + shrinkHorizontally(animationSpec = tween(320), shrinkTowards = Alignment.End)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Lime), contentAlignment = Alignment.Center) {
                    Text("F", color = Background, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("FerBrows", color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                    Text("FAST · SIMPLE · YOURS", color = Muted, fontSize = 7.sp, letterSpacing = 1.2.sp)
                }
                Row(
                    Modifier.clip(RoundedCornerShape(20.dp)).background(PanelSoft.copy(alpha = 0.72f))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(countryFlag, fontSize = 13.sp)
                    Text(countryName, color = White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                Spacer(Modifier.width(4.dp))
            }
        }
        IconButton(onClick = onNewTab, modifier = Modifier.size(if (compact) 38.dp else 48.dp)) { Icon(Icons.Filled.Add, "Новая вкладка", tint = Lime, modifier = Modifier.size(if (compact) 19.dp else 24.dp)) }
        IconButton(onClick = onTabs, modifier = Modifier.size(if (compact) 38.dp else 48.dp)) { Icon(Icons.Filled.Tab, "Вкладки", tint = White, modifier = Modifier.size(if (compact) 19.dp else 24.dp)) }
        IconButton(
            onClick = onMenu,
            modifier = Modifier.padding(start = 2.dp).size(if (compact) 40.dp else 44.dp)
        ) {
            Icon(Icons.Filled.MoreVert, "Меню", tint = Lime, modifier = Modifier.size(23.dp))
        }
    }
}

@Composable
fun TabStrip(tabs: List<BrowserTab>, activeId: Int, onSelect: (BrowserTab) -> Unit, onClose: (BrowserTab) -> Unit, reducedMotion: Boolean = false, compact: Boolean = false) {
    val scope = rememberCoroutineScope()
    val closingTabIds = remember { mutableStateListOf<Int>() }
    val tabWidth by animateDpAsState(
        targetValue = if (compact) 104.dp else 150.dp,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 320),
        label = "tabWidth"
    )
    val horizontalInset by animateDpAsState(if (compact) 7.dp else 12.dp, tween(if (reducedMotion) 0 else 320), label = "tabInset")
    val tabGap by animateDpAsState(if (compact) 5.dp else 8.dp, tween(if (reducedMotion) 0 else 320), label = "tabGap")
    val contentStart by animateDpAsState(if (compact) 5.dp else 9.dp, tween(if (reducedMotion) 0 else 320), label = "tabContentStart")
    val contentTop by animateDpAsState(if (compact) 2.dp else 6.dp, tween(if (reducedMotion) 0 else 320), label = "tabContentTop")
    val closeSize by animateDpAsState(if (compact) 19.dp else 22.dp, tween(if (reducedMotion) 0 else 320), label = "tabCloseSize")
    val dotSize by animateDpAsState(if (compact) 5.dp else 7.dp, tween(if (reducedMotion) 0 else 320), label = "tabDotSize")
    val textSize by animateDpAsState(if (compact) 10.dp else 11.dp, tween(if (reducedMotion) 0 else 320), label = "tabTextSize")

    Row(
        modifier = Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = horizontalInset),
        horizontalArrangement = Arrangement.spacedBy(tabGap)
    ) {
        tabs.forEach { tab ->
            key(tab.id) {
                val active = tab.id == activeId
                val isClosing = tab.id in closingTabIds
                val tabBackground by animateColorAsState(if (active) PanelSoft else Color(0xFF0A0E0A), tween(if (reducedMotion) 0 else 240), label = "tabBackground")
                val tabBorder by animateColorAsState(if (active) Lime.copy(alpha = 0.52f) else Color(0xFF1B241B), tween(if (reducedMotion) 0 else 240), label = "tabBorder")
                val tabDot by animateColorAsState(if (active) Lime else Muted, tween(if (reducedMotion) 0 else 240), label = "tabDot")
                AnimatedVisibility(
                    visible = !isClosing,
                    enter = if (reducedMotion) EnterTransition.None else fadeIn(tween(140)),
                    exit = if (reducedMotion) ExitTransition.None else
                        shrinkHorizontally(tween(240), shrinkTowards = Alignment.CenterHorizontally) +
                            fadeOut(tween(180))
                ) {
                    Row(
                        modifier = Modifier.width(tabWidth)
                            .animateContentSize(tween(if (reducedMotion) 0 else 320))
                            .clip(RoundedCornerShape(if (compact) 9.dp else 13.dp))
                            .background(tabBackground)
                            .border(1.dp, tabBorder, RoundedCornerShape(if (compact) 9.dp else 13.dp))
                            .clickable(enabled = !isClosing) { onSelect(tab) }
                            .padding(start = contentStart, end = 2.dp, top = contentTop, bottom = contentTop),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(dotSize).clip(CircleShape).background(tabDot))
                        Text(
                            (if (tab.isPrivate) "ИНКОГНИТО · " else "") + tab.title.ifBlank { "Новая вкладка" },
                            color = if (tab.isPrivate) Lime else if (active) White else Muted,
                            fontSize = if (compact) 10.sp else 11.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f).padding(start = if (compact) 5.dp else 7.dp)
                        )
                        IconButton(
                            enabled = !isClosing,
                            onClick = {
                                if (closingTabIds.add(tab.id)) {
                                    if (reducedMotion) {
                                        onClose(tab)
                                        closingTabIds.remove(tab.id)
                                    } else {
                                        scope.launch {
                                            delay(250L)
                                            onClose(tab)
                                            closingTabIds.remove(tab.id)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(closeSize)
                        ) {
                            Icon(Icons.Filled.Close, "Закрыть вкладку", tint = Muted, modifier = Modifier.size(if (compact) 11.dp else 13.dp))
                        }
                    }
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
    reducedMotion: Boolean = false,
    compact: Boolean = false
) {
    Column(Modifier.animateContentSize().padding(horizontal = 12.dp, vertical = if (compact) 2.dp else 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(if (compact) 38.dp else 44.dp).clip(RoundedCornerShape(if (compact) 14.dp else 17.dp))
                .background(Panel).border(1.dp, PanelSoft, RoundedCornerShape(17.dp)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Shield, if (isSecure) "Защищённое соединение HTTPS" else if (isInsecure) "Незащищённое соединение HTTP" else "Статус соединения", tint = if (isSecure) Green else if (isInsecure) Color(0xFFFFB454) else Muted, modifier = Modifier.size(18.dp))
            if (isInsecure) Text("HTTP", color = Color(0xFFFFB454), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            else if (isSecure) Text("HTTPS", color = Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
private fun BookmarkFavicon(url: String, sizeDp: Int) {
    val host = remember(url) { Uri.parse(url).host?.removePrefix("www.").orEmpty() }
    Box(
        Modifier.size(sizeDp.dp).clip(RoundedCornerShape(10.dp)).background(PanelSoft),
        contentAlignment = Alignment.Center
    ) {
        Text(host.take(1).uppercase().ifBlank { "?" }, color = Lime, fontWeight = FontWeight.Bold)
        if (host.isNotBlank()) {
            AsyncImage(
                model = "https://$host/favicon.ico",
                contentDescription = "Значок сайта $host",
                modifier = Modifier.fillMaxSize().padding(5.dp)
            )
        }
    }
}

@Composable
fun HomeScreen(onSearch: (String) -> Unit, onOpen: (String) -> Unit, bookmarks: List<String>, trackerProtectionEnabled: Boolean = true) {
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
            Spacer(Modifier.height(24.dp))
            Text("ТВОЁ ПРОСТРАНСТВО В СЕТИ", color = Lime, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(10.dp))
            Text("Твой интернет.", color = White, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            Text("Без лишнего шума.", color = Lime, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            Spacer(Modifier.height(8.dp))
            Text("Всё важное — в одном месте.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(22.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).border(1.dp, PanelSoft, RoundedCornerShape(18.dp)).padding(14.dp),
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
                    .background(Panel).border(1.dp, PanelSoft, RoundedCornerShape(16.dp)).clickable { onOpen(site.third) }.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookmarkFavicon(site.third, 38)
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
                        BookmarkFavicon(url, 30)
                        Text(host, color = White, fontSize = 12.sp, modifier = Modifier.weight(1f).padding(start = 10.dp), maxLines = 1)
                        Icon(Icons.Filled.ArrowForward, null, tint = Muted, modifier = Modifier.size(15.dp))
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Shield, null, tint = Green, modifier = Modifier.size(15.dp))
                Text(if (trackerProtectionEnabled) " Safe Browsing · блокировка известных трекеров включена" else " Safe Browsing · блокировка трекеров отключена", color = Muted, fontSize = 9.sp)
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
    tabCount: Int,
    compact: Boolean = false,
    reducedMotion: Boolean = false
) {
    val duration = if (reducedMotion) 0 else 300
    val verticalPadding by animateDpAsState(if (compact) 2.dp else 8.dp, tween(duration), label = "toolbarVerticalPadding")
    val horizontalPadding by animateDpAsState(if (compact) 12.dp else 22.dp, tween(duration), label = "toolbarHorizontalPadding")
    val buttonSize by animateDpAsState(if (compact) 36.dp else 50.dp, tween(duration), label = "toolbarButtonSize")
    val iconSize by animateDpAsState(if (compact) 18.dp else 25.dp, tween(duration), label = "toolbarIconSize")
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFF0B100B))
            .border(1.dp, PanelSoft)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .animateContentSize(tween(duration)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarButton(Icons.Filled.ArrowBack, "Назад", canBack, onBack, compact = compact, reducedMotion = reducedMotion)
        ToolbarButton(Icons.Filled.ArrowForward, "Вперёд", canForward, onForward, compact = compact, reducedMotion = reducedMotion)
        ToolbarButton(Icons.Filled.Home, "Домой", true, onHome, highlighted = true, compact = compact, reducedMotion = reducedMotion)
        IconButton(onClick = onTabs, modifier = Modifier.size(buttonSize)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Tab, "Вкладки", tint = White, modifier = Modifier.size(iconSize))
                Box(
                    Modifier.align(Alignment.TopEnd)
                        .offset(x = (-1).dp, y = 1.dp)
                        .defaultMinSize(minWidth = if (compact) 18.dp else 20.dp, minHeight = if (compact) 18.dp else 20.dp)
                        .clip(CircleShape)
                        .background(Lime)
                        .border(1.5.dp, Color(0xFF0B100B), CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (tabCount > 99) "99+" else tabCount.toString(),
                        color = Background,
                        fontSize = if (compact) 9.sp else 10.sp,
                        lineHeight = if (compact) 9.sp else 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ToolbarButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    highlighted: Boolean = false,
    compact: Boolean = false,
    reducedMotion: Boolean = false
) {
    val duration = if (reducedMotion) 0 else 300
    val buttonSize by animateDpAsState(if (compact) 36.dp else 50.dp, tween(duration), label = "toolbarActionSize")
    val iconSize by animateDpAsState(if (compact) 18.dp else 25.dp, tween(duration), label = "toolbarActionIconSize")
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(buttonSize)) {
        Icon(icon, description, tint = when {
            highlighted -> Lime
            enabled -> White
            else -> Color(0xFF455045)
        }, modifier = Modifier.size(iconSize))
    }
}
@Composable
fun MenuRow(title: String, subtitle: String, onClick: () -> Unit) {
    val icon = when {
        title.contains("Закладки") -> Icons.Filled.Bookmark
        title.contains("История") -> Icons.Filled.History
        title.contains("приват", ignoreCase = true) -> Icons.Filled.Lock
        title.contains("Загрузки") -> Icons.Filled.Download
        title.contains("Настройки") -> Icons.Filled.Settings
        title.contains("Политика") -> Icons.Filled.Shield
        title.contains("ошибке") -> Icons.Filled.BugReport
        else -> Icons.Filled.MoreHoriz
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).background(PanelSoft.copy(alpha = 0.55f))
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(Lime.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Lime, modifier = Modifier.size(19.dp))
        }
        Column(Modifier.weight(1f).padding(start = 11.dp)) {
            Text(title, color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, color = Muted, fontSize = 10.sp)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = Muted, modifier = Modifier.size(18.dp))
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
