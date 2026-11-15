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
import java.io.ByteArrayInputStream
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.ValueCallback
import androidx.activity.ComponentActivity
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.ProfileStore
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
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

internal val Background = Color(0xFF050705)
internal val Panel = Color(0xFF111811)
internal val PanelSoft = Color(0xFF172117)
internal val Lime = Color(0xFFC6FF1A)
internal val Green = Color(0xFF4ADE80)
internal val Muted = Color(0xFF91A38F)
internal val White = Color(0xFFEAF2E7)
private const val PRIVATE_PROFILE_NAME = "FerBrowsPrivate"


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
            runCatching { ProfileStore.getInstance().deleteProfile(PRIVATE_PROFILE_NAME) }
        }
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
    val store = remember { BrowserStore(context) }
    val restoredTabs = remember { store.loadTabs() }
    var activeTabId by remember { mutableIntStateOf(store.loadActiveTabId().takeIf { id -> restoredTabs.any { it.id == id } } ?: restoredTabs.firstOrNull()?.id ?: 1) }
    var nextTabId by remember { mutableIntStateOf(maxOf(store.loadNextTabId(), (restoredTabs.maxOfOrNull { it.id } ?: 0) + 1)) }
    val tabs = remember { mutableStateListOf<BrowserTab>().apply { addAll(restoredTabs.ifEmpty { listOf(BrowserTab(1, "Новая вкладка", "")) }) } }
    val history = remember { mutableStateListOf<HistoryEntry>().apply { addAll(store.loadHistory().take(100)) } }
    val bookmarks = remember { mutableStateListOf<String>().apply { addAll(store.loadBookmarks()) } }
    val webViews = remember { mutableStateMapOf<Int, WebView>() }
    var pageError by remember { mutableStateOf<String?>(null) }
    val pendingFileCallback = remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pendingFileCallback.value?.onReceiveValue(uri?.let { arrayOf(it) })
        pendingFileCallback.value = null
    }
    fun persistTabs() = store.saveTabs(tabs.toList(), activeTabId, nextTabId)
    fun persistBookmarks() = store.saveBookmarks(bookmarks.toList())
    fun persistHistory() = store.saveHistory(history.toList())

    fun updateTab(url: String, title: String) {
        currentUrl = url
        address = url
        pageTitle = title.ifBlank { "Новая вкладка" }
        val index = tabs.indexOfFirst { it.id == activeTabId }
        if (index >= 0) tabs[index] = tabs[index].copy(title = pageTitle, url = url)
        persistTabs()
    }

    fun createWebView(tabId: Int, isPrivate: Boolean): WebView {
        return WebView(context).apply {
            if (isPrivate && WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
                WebViewCompat.setProfile(this, PRIVATE_PROFILE_NAME)
            }
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = true
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
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
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): android.webkit.WebResourceResponse? {
                    if (TrackerProtection.shouldBlock(request?.url?.host)) {
                        return android.webkit.WebResourceResponse(
                            "text/plain", "UTF-8", 204, "Blocked by FerBrows tracker protection",
                            emptyMap(), ByteArrayInputStream(ByteArray(0))
                        )
                    }
                    return super.shouldInterceptRequest(view, request)
                }

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
                    if (activeTabId == tabId) pageError = null
                    if (url == "about:blank") {
                        if (activeTabId == tabId) { currentUrl = ""; address = ""; pageTitle = "Новая вкладка" }
                    } else if (!url.isNullOrBlank()) {
                        val index = tabs.indexOfFirst { it.id == tabId }
                        if (index >= 0) tabs[index] = tabs[index].copy(url = url)
                        persistTabs()
                        if (activeTabId == tabId) { currentUrl = url; address = url; loading = true }
                    }
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    if (request?.isForMainFrame == true && activeTabId == tabId) {
                        pageError = "Не удалось открыть страницу. Проверь подключение и адрес."
                        loading = false
                    }
                }

                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: android.webkit.WebResourceResponse?) {
                    if (request?.isForMainFrame == true && activeTabId == tabId && (errorResponse?.statusCode ?: 200) >= 400) {
                        pageError = "Ошибка сервера: HTTP ${errorResponse?.statusCode}"
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
                        persistTabs()
                        val privatePage = tabs.firstOrNull { it.id == tabId }?.isPrivate == true
                        if (!privatePage) {
                            val existingHistoryIndex = history.indexOfFirst { it.url == url }
                            if (existingHistoryIndex >= 0) history.removeAt(existingHistoryIndex)
                            history.add(0, HistoryEntry(url, title, System.currentTimeMillis()))
                            while (history.size > 100) history.removeAt(history.lastIndex)
                            persistHistory()
                        }
                        if (activeTabId == tabId) { currentUrl = url; address = url; pageTitle = title; pageError = null }
                    }
                    if (activeTabId == tabId) { canBack = view?.canGoBack() == true; canForward = view?.canGoForward() == true; loading = false }
                }
            }
        }
    }

    LaunchedEffect(activeTabId) {
        val restored = tabs.firstOrNull { it.id == activeTabId }
        if (restored != null) {
            currentUrl = restored.url
            address = restored.url
            pageTitle = restored.title
            pageError = null
        }
    }

    val webView = remember(activeTabId) {
        webViews[activeTabId] ?: createWebView(activeTabId, tabs.firstOrNull { it.id == activeTabId }?.isPrivate == true).also { created ->
            webViews[activeTabId] = created
            tabs.firstOrNull { it.id == activeTabId }?.url?.takeIf { it.isNotBlank() }?.let { created.loadUrl(it) }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            webViews.values.toList().forEach { view ->
                (view.parent as? ViewGroup)?.removeView(view)
                view.stopLoading(); view.destroy()
            }
            webViews.clear()
            if (tabs.any { it.isPrivate } && WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
                runCatching { ProfileStore.getInstance().deleteProfile(PRIVATE_PROFILE_NAME) }
            }
        }
    }
    fun switchTo(tab: BrowserTab) {
        val state = BrowserTabReducer.select(BrowserTabsState(tabs.toList(), activeTabId, nextTabId), tab.id)
        activeTabId = state.activeTabId
        persistTabs()
        updateTab(tab.url, tab.title)
        val selected = webViews[tab.id]
        canBack = selected?.canGoBack() == true
        canForward = selected?.canGoForward() == true
        loading = false
        progress = 0
        pageError = null
    }

    fun addTab(isPrivate: Boolean = false) {
        val next = BrowserTabReducer.add(BrowserTabsState(tabs.toList(), activeTabId, nextTabId), isPrivate)
        tabs.clear(); tabs.addAll(next.tabs)
        activeTabId = next.activeTabId
        nextTabId = next.nextTabId
        currentUrl = ""; address = ""; pageTitle = "Новая вкладка"
        canBack = false; canForward = false; loading = false; progress = 0; pageError = null
        persistTabs()
    }

    fun closeTab(tab: BrowserTab) {
        val oldState = BrowserTabsState(tabs.toList(), activeTabId, nextTabId)
        if (oldState.tabs.none { it.id == tab.id }) return
        val next = BrowserTabReducer.close(oldState, tab.id)
        webViews.remove(tab.id)?.let { view ->
            (view.parent as? ViewGroup)?.removeView(view)
            view.stopLoading()
            view.destroy()
        }
        tabs.clear(); tabs.addAll(next.tabs)
        if (oldState.tabs.any { it.isPrivate } && next.tabs.none { it.isPrivate } &&
            WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
            runCatching { ProfileStore.getInstance().deleteProfile(PRIVATE_PROFILE_NAME) }
        }
        activeTabId = next.activeTabId
        nextTabId = next.nextTabId
        persistTabs()
        val selected = tabs.first { it.id == activeTabId }
        updateTab(selected.url, selected.title)
        canBack = webViews[selected.id]?.canGoBack() == true
        canForward = webViews[selected.id]?.canGoForward() == true
        loading = false; progress = 0; pageError = null
    }

    fun addPrivateTab() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
            Toast.makeText(context, "Приватный профиль не поддерживается текущим WebView", Toast.LENGTH_LONG).show()
            return
        }
        addTab(isPrivate = true)
        Toast.makeText(context, "Приватная вкладка: данные не сохраняются в истории и списке вкладок", Toast.LENGTH_SHORT).show()
    }

    BackHandler(enabled = tabDialog || menuDialog || showBookmarks || showHistory || webView.canGoBack() || tabs.size > 1) {
        when {
            tabDialog -> tabDialog = false
            menuDialog -> menuDialog = false
            showBookmarks -> showBookmarks = false
            showHistory -> showHistory = false
            webView.canGoBack() -> webView.goBack()
            tabs.size > 1 -> {
                closeTab(tabs.first { it.id == activeTabId })
            }
            else -> { currentUrl = ""; address = ""; pageTitle = "Новая вкладка"; webView.loadUrl("about:blank") }
        }
    }

    fun navigate(raw: String) {
        val input = raw.trim()
        if (input.isEmpty()) return
        val target = BrowserNavigation.resolveInput(input)
        if (target == null) {
            pageError = "Некорректный адрес или неподдерживаемая схема. Используй HTTP(S) или поисковый запрос."
            return
        }
        pageError = null
        address = target
        currentUrl = target
        val index = tabs.indexOfFirst { it.id == activeTabId }
        if (index >= 0) tabs[index] = tabs[index].copy(url = target, title = "Загрузка…")
        persistTabs()
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
                    onNewTab = { addTab() },
                    onTabs = { tabDialog = true },
                    onMenu = { menuDialog = true }
                )

                TabStrip(
                    tabs = tabs,
                    activeId = activeTabId,
                    onSelect = { tab ->
                        switchTo(tab)
                    },
                    onClose = { tab -> closeTab(tab) }
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
                        if (tabs.firstOrNull { it.id == activeTabId }?.isPrivate == true) {
                            Toast.makeText(context, "В приватном режиме закладки не сохраняются", Toast.LENGTH_SHORT).show()
                        } else if (currentUrl.startsWith("https://") || currentUrl.startsWith("http://")) {
                            if (currentUrl !in bookmarks) { bookmarks.add(currentUrl); persistBookmarks() }
                        }
                    }
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    key(activeTabId) {
                        AndroidView(
                            factory = { webView },
                            modifier = Modifier.fillMaxSize().alpha(if (currentUrl.isBlank()) 0f else 1f)
                        )
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(visible = currentUrl.isBlank(), modifier = Modifier.fillMaxSize()) {
                            HomeScreen(onSearch = { navigate(it) }, onOpen = { navigate(it) }, bookmarks = bookmarks)
                        }
                    }
                    if (pageError != null) {
                        Surface(
                            color = Panel,
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(pageError ?: "", color = White, fontSize = 12.sp, modifier = Modifier.padding(14.dp))
                        }
                    }
                }

                BottomToolbar(
                    canBack = canBack,
                    canForward = canForward,
                    onBack = { if (webView.canGoBack()) webView.goBack() },
                    onForward = { if (webView.canGoForward()) webView.goForward() },
                    onHome = {
                        currentUrl = ""; address = ""; pageTitle = "Новая вкладка"; pageError = null
                        webView.loadUrl("about:blank")
                        val i = tabs.indexOfFirst { it.id == activeTabId }
                        if (i >= 0) tabs[i] = tabs[i].copy(title = "Новая вкладка", url = "")
                        persistTabs()
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
                                    closeTab(tab)
                                } else {
                                    closeTab(tab)
                                }
                            }) { Icon(Icons.Filled.Close, "Закрыть", tint = Muted) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    addTab()
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
                    MenuRow("Новая приватная вкладка", "Отдельный профиль без сохранения истории") {
                        addPrivateTab()
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
            onSelect = { url -> showBookmarks = false; navigate(url) },
            onRemove = { url -> bookmarks.remove(url); persistBookmarks() },
            onMoveUp = { url ->
                val i = bookmarks.indexOf(url)
                if (i > 0) { val item = bookmarks.removeAt(i); bookmarks.add(i - 1, item); persistBookmarks() }
            },
            onMoveDown = { url ->
                val i = bookmarks.indexOf(url)
                if (i >= 0 && i < bookmarks.lastIndex) { val item = bookmarks.removeAt(i); bookmarks.add(i + 1, item); persistBookmarks() }
            }
        )
    }

    if (showHistory) {
        SimpleListDialog(
            title = "История",
            items = history.map { it.url },
            labels = history.associate { it.url to it.title },
            emptyText = "История пока пуста",
            onDismiss = { showHistory = false },
            onSelect = { url -> showHistory = false; navigate(url) }
        )
    }
}
