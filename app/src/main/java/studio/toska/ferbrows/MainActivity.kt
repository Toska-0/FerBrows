package studio.toska.ferbrows

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.ValueCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLEncoder

private val Background = Color(0xFF050705)
private val Panel = Color(0xFF111811)
private val PanelSoft = Color(0xFF172117)
private val Lime = Color(0xFFC6FF1A)
private val Green = Color(0xFF4ADE80)
private val Muted = Color(0xFF91A38F)
private val White = Color(0xFFEAF2E7)

data class BrowserTab(val id: Int, val title: String, val url: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(5, 7, 5)
        window.navigationBarColor = android.graphics.Color.rgb(5, 7, 5)
        setContent { FerBrowsApp() }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun FerBrowsApp() {
    val context = LocalContext.current
    var currentUrl by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pageTitle by remember { mutableStateOf("Новая вкладка") }
    var progress by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var canBack by remember { mutableStateOf(false) }
    var canForward by remember { mutableStateOf(false) }
    var tabDialog by remember { mutableStateOf(false) }
    var menuDialog by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var activeTabId by remember { mutableIntStateOf(1) }
    var nextTabId by remember { mutableIntStateOf(2) }
    val tabs = remember { mutableStateListOf(BrowserTab(1, "Новая вкладка", "")) }
    val history = remember { mutableStateListOf<String>() }
    val bookmarks = remember { mutableStateListOf("https://www.google.com", "https://www.youtube.com", "https://github.com") }
    val webViews = remember { mutableStateMapOf<Int, WebView>() }
    val preferences = remember { context.getSharedPreferences("ferbrows_state", Context.MODE_PRIVATE) }
    val pendingFileCallback = remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pendingFileCallback.value?.onReceiveValue(uri?.let { arrayOf(it) })
        pendingFileCallback.value = null
    }
    val savedBookmarks = remember { preferences.getStringSet("bookmarks", null) }
    val savedHistory = remember { preferences.getString("history", null) }
    if (savedBookmarks != null && bookmarks.size == 3) { bookmarks.clear(); bookmarks.addAll(savedBookmarks.toList()) }
    if (savedHistory != null && history.isEmpty()) history.addAll(savedHistory.split("\\n").filter { it.isNotBlank() }.take(100))

    fun updateTab(url: String, title: String) {
        currentUrl = url
        address = url
        pageTitle = title.ifBlank { "Новая вкладка" }
        val index = tabs.indexOfFirst { it.id == activeTabId }
        if (index >= 0) tabs[index] = tabs[index].copy(title = pageTitle, url = url)
    }

    fun createWebView(tabId: Int): WebView {
        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) settings.safeBrowsingEnabled = true
            setBackgroundColor(android.graphics.Color.rgb(5, 7, 5))
            setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                try {
                    val name = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType)
                    val request = DownloadManager.Request(Uri.parse(url)).setTitle(name)
                        .setDescription("Загрузка через FerBrows").setMimeType(mimeType)
                        .addRequestHeader("User-Agent", userAgent)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                    (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                    Toast.makeText(context, "Загрузка началась", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) { Toast.makeText(context, "Ошибка загрузки", Toast.LENGTH_SHORT).show() }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    pendingFileCallback.value?.onReceiveValue(null)
                    pendingFileCallback.value = filePathCallback
                    return try { filePicker.launch("*/*"); true }
                    catch (_: Exception) { pendingFileCallback.value = null; filePathCallback?.onReceiveValue(null); false }
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (activeTabId == tabId) {
                        progress = newProgress
                        loading = newProgress < 100
                        canBack = view?.canGoBack() == true
                        canForward = view?.canGoForward() == true
                    }
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (!title.isNullOrBlank()) {
                        val index = tabs.indexOfFirst { it.id == tabId }
                        if (index >= 0) tabs[index] = tabs[index].copy(title = title)
                        if (activeTabId == tabId) pageTitle = title
                    }
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val uri = request?.url ?: return false
                    val scheme = uri.scheme?.lowercase() ?: return false
                    if (scheme == "http" || scheme == "https") return false
                    return try {
                        val intent = if (scheme == "intent") Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME) else Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent); true
                    } catch (_: Exception) { true }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (url == "about:blank") {
                        if (activeTabId == tabId) { currentUrl = ""; address = ""; pageTitle = "Новая вкладка" }
                    } else if (!url.isNullOrBlank()) {
                        val index = tabs.indexOfFirst { it.id == tabId }
                        if (index >= 0) tabs[index] = tabs[index].copy(url = url)
                        if (activeTabId == tabId) { currentUrl = url; address = url; loading = true }
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    if (url == "about:blank") {
                        val index = tabs.indexOfFirst { it.id == tabId }
                        if (index >= 0) tabs[index] = tabs[index].copy(title = "Новая вкладка", url = "")
                        if (activeTabId == tabId) { currentUrl = ""; address = ""; pageTitle = "Новая вкладка" }
                    } else if (!url.isNullOrBlank()) {
                        val title = view?.title?.takeIf { it.isNotBlank() } ?: url
                        val index = tabs.indexOfFirst { it.id == tabId }
                        if (index >= 0) tabs[index] = tabs[index].copy(title = title, url = url)
                        if (url !in history) {
                            history.add(0, url); while (history.size > 100) history.removeAt(history.lastIndex)
                            preferences.edit().putString("history", history.joinToString("\\n")).apply()
                        }
                        if (activeTabId == tabId) { currentUrl = url; address = url; pageTitle = title }
                    }
                    if (activeTabId == tabId) { canBack = view?.canGoBack() == true; canForward = view?.canGoForward() == true; loading = false }
                }
            }
        }
    }

    val webView = remember(activeTabId) {
        webViews[activeTabId] ?: createWebView(activeTabId).also { created ->
            webViews[activeTabId] = created
            tabs.firstOrNull { it.id == activeTabId }?.url?.takeIf { it.isNotBlank() }?.let { created.loadUrl(it) }
        }
    }
    DisposableEffect(Unit) {
        onDispose { webViews.values.toList().forEach { it.stopLoading(); it.destroy() }; webViews.clear() }
    }
    fun persistBookmarks() { preferences.edit().putStringSet("bookmarks", bookmarks.toSet()).apply() }
    fun switchTo(tab: BrowserTab) { activeTabId = tab.id; updateTab(tab.url, tab.title) }
    BackHandler(enabled = tabDialog || menuDialog || showBookmarks || showHistory || webView.canGoBack() || tabs.size > 1) {
        when {
            tabDialog -> tabDialog = false
            menuDialog -> menuDialog = false
            showBookmarks -> showBookmarks = false
            showHistory -> showHistory = false
            webView.canGoBack() -> webView.goBack()
            tabs.size > 1 -> {
                val index = tabs.indexOfFirst { it.id == activeTabId }.coerceAtLeast(0)
                val removed = tabs.removeAt(index); webViews.remove(removed.id)?.destroy()
                val replacement = tabs.first(); activeTabId = replacement.id; updateTab(replacement.url, replacement.title)
            }
            else -> { currentUrl = ""; address = ""; pageTitle = "Новая вкладка"; webView.loadUrl("about:blank") }
        }
    }

    fun navigate(raw: String) {
        val input = raw.trim()
        if (input.isEmpty()) return
        val target = when {
            input.startsWith("https://") || input.startsWith("http://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "https://www.google.com/search?q=" + URLEncoder.encode(input, "UTF-8")
        }
        address = target
        currentUrl = target
        val index = tabs.indexOfFirst { it.id == activeTabId }
        if (index >= 0) tabs[index] = tabs[index].copy(url = target, title = "Загрузка…")
        webView.loadUrl(target)
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            ) {
                Header(
                    onNewTab = {
                        val id = nextTabId++
                        tabs.add(BrowserTab(id, "Новая вкладка", ""))
                        activeTabId = id
                        currentUrl = ""
                        address = ""
                        pageTitle = "Новая вкладка"
                    },
                    onTabs = { tabDialog = true },
                    onMenu = { menuDialog = true }
                )

                TabStrip(
                    tabs = tabs,
                    activeId = activeTabId,
                    onSelect = { tab ->
                        switchTo(tab)
                    },
                    onClose = { tab ->
                        if (tabs.size > 1) {
                            val oldIndex = tabs.indexOfFirst { it.id == tab.id }
                            tabs.remove(tab)
                            if (activeTabId == tab.id) {
                                val replacement = tabs.getOrNull((oldIndex - 1).coerceAtLeast(0)) ?: tabs.first()
                                activeTabId = replacement.id
                                updateTab(replacement.url, replacement.title)
                                if (replacement.url.isNotBlank()) webView.loadUrl(replacement.url) else webView.loadUrl("about:blank")
                            }
                        } else {
                            tabs[0] = BrowserTab(tab.id, "Новая вкладка", "")
                            activeTabId = tab.id
                            currentUrl = ""
                            address = ""
                            webView.loadUrl("about:blank")
                        }
                    }
                )

                AddressBar(
                    value = address,
                    loading = loading,
                    progress = progress,
                    onValueChange = { address = it },
                    onGo = { navigate(address) },
                    onRefresh = {
                        if (currentUrl.isNotBlank()) webView.reload()
                    },
                    onBookmark = {
                        if (currentUrl.isNotBlank() && currentUrl !in bookmarks) { bookmarks.add(currentUrl); persistBookmarks() }
                    }
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AndroidView(
                        factory = { webView },
                        modifier = Modifier.fillMaxSize().alpha(if (currentUrl.isBlank()) 0f else 1f)
                    )
                    Column(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(visible = currentUrl.isBlank(), modifier = Modifier.fillMaxSize()) {
                            HomeScreen(onSearch = { navigate(it) }, onOpen = { navigate(it) }, bookmarks = bookmarks)
                        }
                    }
                }

                BottomToolbar(
                    canBack = canBack,
                    canForward = canForward,
                    onBack = { if (webView.canGoBack()) webView.goBack() },
                    onForward = { if (webView.canGoForward()) webView.goForward() },
                    onHome = {
                        currentUrl = ""; address = ""; pageTitle = "Новая вкладка"
                        webView.loadUrl("about:blank")
                        val i = tabs.indexOfFirst { it.id == activeTabId }
                        if (i >= 0) tabs[i] = tabs[i].copy(title = "Новая вкладка", url = "")
                    },
                    onTabs = { tabDialog = true },
                    tabCount = tabs.size
                )
            }
        }
    }

    if (tabDialog) {
        AlertDialog(
            onDismissRequest = { tabDialog = false },
            containerColor = Panel,
            title = { Text("Вкладки", color = White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { tab ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .background(if (tab.id == activeTabId) PanelSoft else Color.Transparent)
                                .clickable {
                                    switchTo(tab)
                                    tabDialog = false
                                }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Language, null, tint = Green, modifier = Modifier.size(18.dp))
                            Text(tab.title, color = White, modifier = Modifier.weight(1f).padding(start = 10.dp), maxLines = 1)
                            IconButton(onClick = {
                                if (tabs.size > 1) {
                                    tabs.remove(tab); webViews.remove(tab.id)?.destroy()
                                    if (activeTabId == tab.id) {
                                        val replacement = tabs.first(); activeTabId = replacement.id
                                        updateTab(replacement.url, replacement.title)
                                    }
                                } else {
                                    tabs[0] = BrowserTab(tab.id, "Новая вкладка", "")
                                    currentUrl = ""; address = ""; pageTitle = "Новая вкладка"; webView.loadUrl("about:blank")
                                }
                            }) { Icon(Icons.Filled.Close, "Закрыть", tint = Muted) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = nextTabId++
                    tabs.add(BrowserTab(id, "Новая вкладка", ""))
                    activeTabId = id
                    currentUrl = ""
                    address = ""
                    pageTitle = "Новая вкладка"
                    tabDialog = false
                }) { Text("Новая вкладка", color = Lime) }
            }
        )
    }

    if (menuDialog) {
        AlertDialog(
            onDismissRequest = { menuDialog = false },
            containerColor = Panel,
            title = { Text("FerBrows", color = White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MenuRow("Закладки", "Сохранённые сайты") {
                        showBookmarks = true
                        menuDialog = false
                    }
                    MenuRow("История", "Недавно открытые страницы") {
                        showHistory = true
                        menuDialog = false
                    }
                    MenuRow("О браузере", "FerBrows 0.1.0 · Chromium WebView") {
                        menuDialog = false
                    }
                }
            },
            confirmButton = { TextButton(onClick = { menuDialog = false }) { Text("Закрыть", color = Lime) } }
        )
    }

    if (showBookmarks) {
        SimpleListDialog(
            title = "Закладки",
            items = bookmarks,
            emptyText = "Пока нет закладок",
            onDismiss = { showBookmarks = false },
            onSelect = { url -> showBookmarks = false; navigate(url) }
        )
    }

    if (showHistory) {
        SimpleListDialog(
            title = "История",
            items = history,
            emptyText = "История пока пуста",
            onDismiss = { showHistory = false },
            onSelect = { url -> showHistory = false; navigate(url) }
        )
    }
}

@Composable
private fun Header(onNewTab: () -> Unit, onTabs: () -> Unit, onMenu: () -> Unit) {
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
private fun TabStrip(tabs: List<BrowserTab>, activeId: Int, onSelect: (BrowserTab) -> Unit, onClose: (BrowserTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp),
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
                Text(tab.title.ifBlank { "Новая вкладка" }, color = if (active) White else Muted,
                    fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(1f).padding(start = 7.dp))
                IconButton(onClick = { onClose(tab) }, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Filled.Close, "Закрыть вкладку", tint = Muted, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

@Composable
private fun AddressBar(
    value: String,
    loading: Boolean,
    progress: Int,
    onValueChange: (String) -> Unit,
    onGo: () -> Unit,
    onRefresh: () -> Unit,
    onBookmark: () -> Unit
) {
    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(17.dp))
                .background(Panel).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Shield, null, tint = Green, modifier = Modifier.size(18.dp))
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
            IconButton(onClick = onBookmark, modifier = Modifier.size(34.dp), enabled = value.startsWith("http")) {
                Icon(Icons.Filled.Star, "В закладки", tint = if (value.startsWith("http")) Lime else Muted, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.Refresh, "Обновить", tint = White, modifier = Modifier.size(19.dp))
            }
        }
        AnimatedVisibility(visible = loading) {
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
private fun HomeScreen(onSearch: (String) -> Unit, onOpen: (String) -> Unit, bookmarks: List<String>) {
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
                Text(" Safe Browsing включён · защита от трекеров в разработке", color = Muted, fontSize = 9.sp)
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun BottomToolbar(
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
private fun ToolbarButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit, highlighted: Boolean = false) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(icon, description, tint = when {
            highlighted -> Lime
            enabled -> White
            else -> Color(0xFF455045)
        })
    }
}

@Composable
private fun MenuRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).clickable(onClick = onClick).padding(12.dp)
    ) {
        Text(title, color = White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(subtitle, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun SimpleListDialog(
    title: String,
    items: List<String>,
    emptyText: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = { Text(title, color = White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (items.isEmpty()) Text(emptyText, color = Muted)
                items.take(30).forEach { item ->
                    Text(item, color = White, fontSize = 12.sp, maxLines = 1,
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(item) }.padding(vertical = 7.dp))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово", color = Lime) } }
    )
}
