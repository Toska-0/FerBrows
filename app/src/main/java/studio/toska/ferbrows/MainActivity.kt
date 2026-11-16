package studio.toska.ferbrows

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.util.Base64
import android.content.Intent
import android.content.ContentValues
import android.net.Uri
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicBoolean
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.ValueCallback
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.ProfileStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import org.json.JSONArray

internal val Background = Color(0xFF050705)
internal val Panel = Color(0xFF111811)
internal val PanelSoft = Color(0xFF172117)
private val LocalAccentColor = compositionLocalOf { Color(0xFFC6FF1A) }
internal val Lime: Color @Composable get() = LocalAccentColor.current
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
    val credentialVault = remember { CredentialVault(context) }
    var pendingCredential by remember { mutableStateOf<SavedCredential?>(null) }
    var showCredentials by remember { mutableStateOf(false) }
    var credentialListRefresh by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pageTitle by remember { mutableStateOf("Новая вкладка") }
    var progress by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var networkOnline by remember { mutableStateOf(hasValidatedInternet(context)) }
    var countryName by remember { mutableStateOf("Определение…") }
    var countryFlag by remember { mutableStateOf("🌐") }
    var countryRefreshToken by remember { mutableIntStateOf(0) }
    var canBack by remember { mutableStateOf(false) }
    var canForward by remember { mutableStateOf(false) }
    var tabDialog by remember { mutableStateOf(false) }
    var groupingTab by remember { mutableStateOf<BrowserTab?>(null) }
    var groupNameDraft by remember { mutableStateOf("") }
    var tabPreviews by remember { mutableStateOf<Map<Int, Bitmap>>(emptyMap()) }
    var menuDialog by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var personalizationOnly by remember { mutableStateOf(false) }
    var privacyOnly by remember { mutableStateOf(false) }
    var showDownloads by remember { mutableStateOf(false) }
    var latestUpdate by remember { mutableStateOf<FerBrowsUpdate?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateDownloadId by remember { mutableStateOf<Long?>(null) }
    var completedUpdateDownloadId by remember { mutableStateOf<Long?>(null) }
    var updateProgress by remember { mutableIntStateOf(0) }
    var updateFailed by remember { mutableStateOf(false) }
    var updateCheckRequested by remember { mutableIntStateOf(0) }
    var tabTransitionSnapshot by remember { mutableStateOf<Bitmap?>(null) }
    var tabTransitionDirection by remember { mutableIntStateOf(-1) }
    var tabTransitionToken by remember { mutableIntStateOf(0) }
    val tabTransitionProgress = remember { Animatable(1f) }
    val downloadsStore = remember { BrowserDownloadsStore(context) }
    var downloads by remember { mutableStateOf(downloadsStore.query()) }
    var showExitConfirm by remember { mutableStateOf(false) }
    var bookmarkEditing by remember { mutableStateOf<String?>(null) }
    val onboardingPrefs = remember { context.getSharedPreferences("ferbrows_onboarding", Context.MODE_PRIVATE) }
    var showOnboarding by remember { mutableStateOf(!onboardingPrefs.getBoolean("completed", false)) }
    val settingsStore = remember { BrowserSettingsStore(context) }
    var browserSettings by remember { mutableStateOf(settingsStore.load()) }
    val accentColor = remember(browserSettings.accentColor) {
        runCatching { Color(android.graphics.Color.parseColor(browserSettings.accentColor)) }
            .getOrDefault(Color(0xFFC6FF1A))
    }
    val trackerProtectionEnabled = remember { AtomicBoolean(browserSettings.trackerProtectionEnabled) }
    val store = remember { BrowserStore(context) }
    val connectivityManager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                networkOnline = hasValidatedInternet(context)
                countryRefreshToken++
            }
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                networkOnline = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                countryRefreshToken++
            }
            override fun onLost(network: Network) {
                networkOnline = hasValidatedInternet(context)
                countryRefreshToken++
            }
        }
        runCatching { connectivityManager.registerDefaultNetworkCallback(callback) }
        onDispose { runCatching { connectivityManager.unregisterNetworkCallback(callback) } }
    }
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
    val exportBookmarksLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            val json = JSONArray(bookmarks.toList()).toString(2)
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                ?: error("Не удалось открыть файл для записи")
            Toast.makeText(context, "Закладки экспортированы", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Не удалось экспортировать закладки", Toast.LENGTH_LONG).show()
        }
    }
    val importBookmarksLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("Не удалось прочитать файл")
            val json = JSONArray(raw)
            val imported = (0 until json.length()).mapNotNull { index ->
                val value = json.optString(index)
                BrowserNavigation.resolveInput(value)?.takeIf {
                    it.startsWith("https://", true) || it.startsWith("http://", true)
                }
            }
            val merged = (bookmarks.toList() + imported).distinct()
            bookmarks.clear()
            bookmarks.addAll(merged)
            store.saveBookmarks(merged)
            Toast.makeText(context, "Импортировано закладок: ${merged.size}", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Файл закладок повреждён или имеет неверный формат", Toast.LENGTH_LONG).show()
        }
    }
    fun persistTabs() = store.saveTabs(tabs.toList(), activeTabId, nextTabId)
    fun persistBookmarks() = store.saveBookmarks(bookmarks.toList())
    fun persistHistory() = store.saveHistory(history.toList())

    fun openTabOverview() {
        tabPreviews.values.forEach { runCatching { it.recycle() } }
        val snapshots = mutableMapOf<Int, Bitmap>()
        webViews.forEach { (id, view) ->
            runCatching {
                if (view.width > 0 && view.height > 0) {
                    val width = 360
                    val height = (view.height.toFloat() * width / view.width).toInt().coerceIn(180, 520)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.scale(width.toFloat() / view.width, height.toFloat() / view.height)
                    view.draw(canvas)
                    snapshots[id] = bitmap
                }
            }
        }
        tabPreviews = snapshots
        tabDialog = true
    }

    fun persistWebViewState(tabId: Int, view: WebView?) {
        if (view == null || tabs.firstOrNull { it.id == tabId }?.isPrivate == true) return
        runCatching {
            val bundle = Bundle()
            view.saveState(bundle)
            val parcel = Parcel.obtain()
            try {
                bundle.writeToParcel(parcel, 0)
                val bytes = parcel.marshall()
                if (bytes.size <= 500_000) store.saveWebViewState(tabId, Base64.encodeToString(bytes, Base64.NO_WRAP))
                else store.clearWebViewState(tabId)
            } finally { parcel.recycle() }
        }.onFailure { store.clearWebViewState(tabId) }
    }

    val lifecycleActivity = context as? ComponentActivity
    DisposableEffect(lifecycleActivity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                persistTabs()
                webViews.forEach { (tabId, view) -> persistWebViewState(tabId, view) }
            }
        }
        lifecycleActivity?.lifecycle?.addObserver(observer)
        onDispose { lifecycleActivity?.lifecycle?.removeObserver(observer) }
    }

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
            settings.javaScriptEnabled = browserSettings.javaScriptEnabled
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
            val credentialWebView = this
            if (!isPrivate) {
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun postMessage(payload: String) {
                        runCatching {
                            val message = org.json.JSONObject(payload)
                            if (message.optString("type") != "save") return
                            val origin = message.optString("origin")
                            val actualOrigin = CredentialVault.originOf(credentialWebView.url.orEmpty())
                            val username = message.optString("username")
                            val password = message.optString("password")
                            if (origin != actualOrigin || !origin.startsWith("https://") || username.isBlank() || password.isBlank()) return
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                if (tabs.firstOrNull { it.id == tabId }?.isPrivate != true) {
                                    pendingCredential = SavedCredential(origin, username, password, System.currentTimeMillis())
                                }
                            }
                        }
                    }
                }, "FerBrowsVault")
            }
            setBackgroundColor(android.graphics.Color.rgb(5, 7, 5))
            var gestureDownX = 0f
            var gestureDownY = 0f
            setOnTouchListener { touchedView, event ->
                val browser = touchedView as? WebView
                if (browser != null && browserSettings.swipeNavigationEnabled) {
                    val edge = 24f * context.resources.displayMetrics.density
                    val threshold = 88f * context.resources.displayMetrics.density
                    when (event.actionMasked) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            gestureDownX = event.x
                            gestureDownY = event.y
                        }
                        android.view.MotionEvent.ACTION_UP -> {
                            val dx = event.x - gestureDownX
                            val dy = event.y - gestureDownY
                            if (abs(dy) < threshold && abs(dx) > threshold) {
                                if (gestureDownX <= edge && dx > 0 && browser.canGoBack()) browser.goBack()
                                else if (gestureDownX >= browser.width - edge && dx < 0 && browser.canGoForward()) browser.goForward()
                            }
                        }
                    }
                }
                false
            }
            setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                try {
                    val downloadUri = Uri.parse(url)
                    val reportedMimeType = mimeType?.substringBefore(';')?.trim()?.takeIf { it.isNotBlank() }
                    val urlFileName = downloadUri.lastPathSegment.orEmpty()
                    val guessedName = android.webkit.URLUtil.guessFileName(url, contentDisposition, reportedMimeType ?: "application/octet-stream")

                    // Some servers label APK downloads as application/octet-stream (or even
                    // application/zip), which makes URLUtil append .bin/.zip. Prefer an APK
                    // filename from the URL or Content-Disposition and normalize its MIME type.
                    val dispositionApkName = contentDisposition
                        ?.let { android.webkit.URLUtil.guessFileName(url, it, "application/vnd.android.package-archive") }
                        ?.takeIf { it.substringBefore('?').endsWith(".apk", ignoreCase = true) }
                    val urlApkName = urlFileName.takeIf { it.substringBefore('?').endsWith(".apk", ignoreCase = true) }
                    val isApk = reportedMimeType.equals("application/vnd.android.package-archive", ignoreCase = true) ||
                        dispositionApkName != null || urlApkName != null
                    val preferredName = when {
                        dispositionApkName != null -> dispositionApkName
                        urlApkName != null -> urlApkName
                        isApk -> guessedName.substringBeforeLast('.', guessedName) + ".apk"
                        else -> guessedName
                    }
                    val safeMimeType = if (isApk) "application/vnd.android.package-archive"
                        else reportedMimeType ?: "application/octet-stream"
                    val safeName = preferredName
                        .filterNot { it.code < 32 || it.code in listOf(47, 92, 58, 42, 63, 34, 60, 62, 124) }
                        .trim()
                        .take(180)
                        .ifBlank { if (isApk) "download.apk" else "download" }
                        .let { name ->
                            if (isApk && !name.endsWith(".apk", ignoreCase = true)) {
                                name.substringBeforeLast('.', name) + ".apk"
                            } else name
                        }
                    val request = DownloadManager.Request(downloadUri)
                        .setTitle(safeName)
                        .setDescription("Загрузка через FerBrows")
                        .setMimeType(safeMimeType)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)
                        .also { downloadRequest ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
                            } else {
                                downloadRequest.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, safeName)
                            }
                        }
                        .addRequestHeader("User-Agent", userAgent.ifBlank { settings.userAgentString ?: "Mozilla/5.0" })

                    // Many download endpoints require the same session cookies as the opened page.
                    android.webkit.CookieManager.getInstance().getCookie(url)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { request.addRequestHeader("Cookie", it) }
                    request.addRequestHeader("Referer", this.url ?: url)

                    val downloadId = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                    downloadsStore.remember(downloadId)
                    downloads = downloadsStore.query()
                    Toast.makeText(context, "Загрузка началась: $safeName", Toast.LENGTH_SHORT).show()
                } catch (error: Exception) {
                    Toast.makeText(context, "Не удалось начать загрузку: ${error.localizedMessage ?: "ошибка"}", Toast.LENGTH_LONG).show()
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                    request?.deny()
                }

                override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: android.webkit.GeolocationPermissions.Callback?) {
                    callback?.invoke(origin, false, false)
                }

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
                    if (trackerProtectionEnabled.get() && TrackerProtection.shouldBlock(request?.url?.host)) {
                        return android.webkit.WebResourceResponse(
                            "text/plain", "UTF-8", 204, "Blocked by FerBrows tracker protection",
                            emptyMap(), ByteArrayInputStream(ByteArray(0))
                        )
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val uri = request?.url ?: return false
                    val scheme = uri.scheme?.lowercase() ?: return true
                    if (scheme == "http" || scheme == "https") return false
                    if (scheme !in setOf("mailto", "tel", "sms", "smsto", "geo", "intent")) return true
                    return try {
                        val intent = if (scheme == "intent") {
                            Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME).apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                                component = null
                                selector = null
                            }
                        } else Intent(Intent.ACTION_VIEW, uri)
                        if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                        true
                    } catch (_: Exception) { true }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (tabs.firstOrNull { it.id == tabId }?.isPrivate != true) store.clearWebViewState(tabId)
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
                        loading = false
                        progress = 100
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
                    if (url != "about:blank") {
                        if (tabs.firstOrNull { it.id == tabId }?.isPrivate != true) {
                            val origin = CredentialVault.originOf(url.orEmpty())
                            val saved = origin?.let { credentialVault.forOrigin(it) }
                            view?.evaluateJavascript(credentialAutofillScript(saved), null)
                        }
                        persistWebViewState(tabId, view)
                    }
                }
            }
        }
    }

    LaunchedEffect(showDownloads) {
        while (showDownloads) {
            downloads = downloadsStore.query()
            delay(800)
        }
    }

    LaunchedEffect(countryRefreshToken) {
        delay(500)
        val country = withContext(Dispatchers.IO) { FerBrowsNetworkInfo.lookupCountry() }
        if (country != null) {
            countryName = country.name
            countryFlag = country.flag
        } else {
            countryName = "Не определена"
            countryFlag = "🌐"
        }
    }

    LaunchedEffect(Unit) {
        val installedVersion = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        }.getOrDefault("0.0.0")
        latestUpdate = withContext(Dispatchers.IO) { FerBrowsUpdater.checkLatest(installedVersion) }
        if (latestUpdate != null) showUpdateDialog = true
    }

    LaunchedEffect(updateCheckRequested) {
        if (updateCheckRequested > 0) {
            val installedVersion = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
            }.getOrDefault("0.0.0")
            val found = withContext(Dispatchers.IO) { FerBrowsUpdater.checkLatest(installedVersion) }
            latestUpdate = found
            showUpdateDialog = found != null
            if (found == null) Toast.makeText(context, "Новых обновлений не найдено", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(updateDownloadId) {
        val id = updateDownloadId ?: return@LaunchedEffect
        while (true) {
            val status = withContext(Dispatchers.IO) { FerBrowsUpdater.progress(context, id) }
            updateProgress = status.percent
            if (status.failed) {
                updateFailed = true
                updateDownloadId = null
                break
            }
            if (status.finished) {
                updateProgress = 100
                completedUpdateDownloadId = id
                updateDownloadId = null
                val launched = FerBrowsUpdater.install(context, id)
                if (launched) showUpdateDialog = false
                break
            }
            delay(500)
        }
    }

    LaunchedEffect(tabTransitionToken) {
        val snapshot = tabTransitionSnapshot
        if (snapshot != null) {
            if (browserSettings.reducedMotion) {
                tabTransitionProgress.snapTo(1f)
            } else {
                tabTransitionProgress.snapTo(0f)
                tabTransitionProgress.animateTo(1f, animationSpec = tween(360))
            }
            delay(24)
            if (tabTransitionSnapshot === snapshot) {
                tabTransitionSnapshot = null
                snapshot.recycle()
            }
        }
    }

    LaunchedEffect(activeTabId, browserSettings.homePage) {
        val restored = tabs.firstOrNull { it.id == activeTabId }
        if (restored != null) {
            currentUrl = restored.url
            address = restored.url
            pageTitle = restored.title
            pageError = null
            if (restored.url.isBlank() && browserSettings.homePage.isNotBlank()) {
                val homepage = BrowserNavigation.resolveInput(browserSettings.homePage, browserSettings.searchEngine)
                if (homepage != null) webViews[activeTabId]?.loadUrl(homepage)
            }
        }
    }

    val webView = remember(activeTabId) {
        webViews[activeTabId] ?: createWebView(activeTabId, tabs.firstOrNull { it.id == activeTabId }?.isPrivate == true).also { created ->
            webViews[activeTabId] = created
            val isPrivateTab = tabs.firstOrNull { it.id == activeTabId }?.isPrivate == true
            val savedState = if (isPrivateTab) null else store.loadWebViewState(activeTabId)
            val restored = if (savedState.isNullOrBlank()) false else runCatching {
                val bytes = Base64.decode(savedState, Base64.NO_WRAP)
                val parcel = Parcel.obtain()
                try {
                    parcel.unmarshall(bytes, 0, bytes.size)
                    parcel.setDataPosition(0)
                    val bundle = Bundle.CREATOR.createFromParcel(parcel)
                    created.restoreState(bundle) != null
                } finally { parcel.recycle() }
            }.getOrDefault(false)
            if (!restored) tabs.firstOrNull { it.id == activeTabId }?.url?.takeIf { it.isNotBlank() }?.let { created.loadUrl(it) }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            webViews.forEach { (tabId, view) -> persistWebViewState(tabId, view) }
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
        if (tab.id == activeTabId) return
        val previousIndex = tabs.indexOfFirst { it.id == activeTabId }
        val targetIndex = tabs.indexOfFirst { it.id == tab.id }
        val previousView = webViews[activeTabId]
        val snapshot = if (currentUrl.isNotBlank() && previousView != null && previousView.width > 0 && previousView.height > 0) {
            runCatching {
                Bitmap.createBitmap(previousView.width, previousView.height, Bitmap.Config.ARGB_8888).also { bitmap ->
                    previousView.draw(Canvas(bitmap))
                }
            }.getOrNull()
        } else null
        tabTransitionSnapshot?.let { old ->
            if (old !== snapshot && !old.isRecycled) old.recycle()
        }
        tabTransitionSnapshot = snapshot
        tabTransitionDirection = if (targetIndex >= previousIndex) -1 else 1
        tabTransitionToken++
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
        store.clearWebViewState(tab.id)
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

    fun savePageScreenshot() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(context, "Сохранение снимка в галерею требует Android 10 или новее", Toast.LENGTH_LONG).show()
            return
        }
        if (webView.width <= 0 || webView.height <= 0) {
            Toast.makeText(context, "Страница ещё не готова для снимка", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
            webView.draw(Canvas(bitmap))
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "FerBrows_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FerBrows")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Не удалось создать файл снимка")
            val saved = context.contentResolver.openOutputStream(uri)?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            } ?: false
            bitmap.recycle()
            if (!saved) {
                context.contentResolver.delete(uri, null, null)
                error("Не удалось записать изображение")
            }
            Toast.makeText(context, "Снимок сохранён в Pictures/FerBrows", Toast.LENGTH_LONG).show()
        }.onFailure {
            Toast.makeText(context, "Не удалось сохранить снимок страницы", Toast.LENGTH_LONG).show()
        }
    }

    fun addPrivateTab() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
            Toast.makeText(context, "Приватный профиль не поддерживается текущим WebView", Toast.LENGTH_LONG).show()
            return
        }
        addTab(isPrivate = true)
        Toast.makeText(context, "Приватная вкладка: данные не сохраняются в истории и списке вкладок", Toast.LENGTH_SHORT).show()
    }

    BackHandler(enabled = true) {
        when {
            tabDialog -> tabDialog = false
            menuDialog -> menuDialog = false
            showBookmarks -> showBookmarks = false
            showHistory -> showHistory = false
            showSettings -> showSettings = false
            showDownloads -> showDownloads = false
            showExitConfirm -> showExitConfirm = false
            showOnboarding -> { showOnboarding = false; onboardingPrefs.edit().putBoolean("completed", true).apply() }
            webView.canGoBack() -> webView.goBack()
            tabs.size > 1 -> closeTab(tabs.first { it.id == activeTabId })
            else -> showExitConfirm = true
        }
    }

    fun navigate(raw: String) {
        val input = raw.trim()
        if (input.isEmpty()) return
        val target = BrowserNavigation.resolveInput(input, browserSettings.searchEngine)
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

    CompositionLocalProvider(LocalAccentColor provides accentColor) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            ) {
                val compactBrowserChrome = !tabDialog && !showSettings &&
                    !showBookmarks && !showHistory && !showDownloads &&
                    (browserSettings.compactInterface || currentUrl.isNotBlank())

                Header(
                    onNewTab = { addTab() },
                    onTabs = { openTabOverview() },
                    onMenu = { menuDialog = true },
                    countryName = countryName,
                    countryFlag = countryFlag,
                    compact = compactBrowserChrome
                )

                TabStrip(
                    tabs = tabs,
                    activeId = activeTabId,
                    onSelect = { tab -> switchTo(tab) },
                    onClose = { tab -> closeTab(tab) },
                    reducedMotion = browserSettings.reducedMotion,
                    compact = compactBrowserChrome
                )

                AddressBar(
                    value = address,
                    isSecure = currentUrl.startsWith("https://", ignoreCase = true) && !loading,
                    isInsecure = currentUrl.startsWith("http://", ignoreCase = true),
                    canBookmark = currentUrl.startsWith("https://", true) || currentUrl.startsWith("http://", true),
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
                    },
                    reducedMotion = browserSettings.reducedMotion,
                    compact = compactBrowserChrome
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    key(activeTabId) {
                        AndroidView(
                            factory = { webView },
                            modifier = Modifier.fillMaxSize()
                                .alpha(if (currentUrl.isBlank()) 0f else 1f)
                                .graphicsLayer {
                                    translationX = if (tabTransitionSnapshot != null) {
                                        -tabTransitionDirection * (1f - tabTransitionProgress.value) * size.width
                                    } else 0f
                                }
                        )
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = currentUrl.isBlank(),
                            modifier = Modifier.fillMaxSize(),
                            enter = if (browserSettings.reducedMotion) EnterTransition.None else fadeIn(),
                            exit = if (browserSettings.reducedMotion) ExitTransition.None else fadeOut()
                        ) {
                            HomeScreen(onSearch = { navigate(it) }, onOpen = { navigate(it) }, bookmarks = bookmarks, trackerProtectionEnabled = browserSettings.trackerProtectionEnabled)
                        }
                    }
                    tabTransitionSnapshot?.let { snapshot ->
                        Image(
                            bitmap = snapshot.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                translationX = tabTransitionDirection * tabTransitionProgress.value * size.width
                            }
                        )
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
                        if (browserSettings.homePage.isNotBlank()) navigate(browserSettings.homePage)
                        else {
                            currentUrl = ""; address = ""; pageTitle = "Новая вкладка"; pageError = null
                            webView.loadUrl("about:blank")
                            val i = tabs.indexOfFirst { it.id == activeTabId }
                            if (i >= 0) tabs[i] = tabs[i].copy(title = "Новая вкладка", url = "")
                            persistTabs()
                        }
                    },
                    onTabs = { openTabOverview() },
                    tabCount = tabs.size,
                    compact = compactBrowserChrome,
                    reducedMotion = browserSettings.reducedMotion
                )
            }
        }
    }

    if (tabDialog) {
        TabOverviewDialog(
            tabs = tabs.toList(),
            activeId = activeTabId,
            previews = tabPreviews,
            onSelect = { tab -> switchTo(tab); tabDialog = false; tabPreviews.values.forEach { runCatching { it.recycle() } }; tabPreviews = emptyMap() },
            onClose = { tab ->
                closeTab(tab)
                tabPreviews[tab.id]?.let { runCatching { it.recycle() } }
                tabPreviews = tabPreviews - tab.id
            },
            onNewTab = { addTab(); tabDialog = false; tabPreviews.values.forEach { runCatching { it.recycle() } }; tabPreviews = emptyMap() },
            onGroup = { tab -> groupingTab = tab; groupNameDraft = tab.groupName },
            onDismiss = { tabDialog = false; tabPreviews.values.forEach { runCatching { it.recycle() } }; tabPreviews = emptyMap() }
        )
    }

    if (groupingTab != null) {
        AlertDialog(
            onDismissRequest = { groupingTab = null },
            containerColor = Panel,
            title = { Text("Группа вкладок", color = White) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = groupNameDraft,
                    onValueChange = { groupNameDraft = it.take(32) },
                    singleLine = true,
                    label = { Text("Название группы") },
                    placeholder = { Text("Например, Работа") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val selected = groupingTab
                    val index = tabs.indexOfFirst { it.id == selected?.id }
                    if (index >= 0) {
                        tabs[index] = tabs[index].copy(groupName = groupNameDraft.trim())
                        persistTabs()
                    }
                    groupingTab = null
                }) { Text("Сохранить", color = Lime) }
            },
            dismissButton = {
                TextButton(onClick = { groupingTab = null }) { Text("Отмена", color = Muted) }
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
                    MenuRow("Сохранённые пароли", "Управление логинами и автозаполнением") {
                        showCredentials = true
                        menuDialog = false
                    }
                    MenuRow("Новая приватная вкладка", "Отдельный профиль без сохранения истории") {
                        addPrivateTab()
                        menuDialog = false
                    }
                    MenuRow("Загрузки", "Прогресс, открытие и удаление файлов") {
                        downloads = downloadsStore.query()
                        showDownloads = true
                        menuDialog = false
                    }
                    MenuRow("Импорт закладок", "Добавить сайты из JSON-файла") {
                        menuDialog = false
                        importBookmarksLauncher.launch(arrayOf("application/json", "text/*"))
                    }
                    MenuRow("Экспорт закладок", "Сохранить закладки в JSON-файл") {
                        menuDialog = false
                        exportBookmarksLauncher.launch("FerBrows-bookmarks.json")
                    }
                    MenuRow("Снимок страницы", "Сохранить открытую страницу в галерею") {
                        menuDialog = false
                        savePageScreenshot()
                    }
                    MenuRow("Перевести страницу", "Открыть перевод текущего сайта на русский") {
                        menuDialog = false
                        if (currentUrl.startsWith("http")) {
                            val source = URLEncoder.encode(currentUrl, "UTF-8")
                            navigate("https://translate.google.com/translate?sl=auto&tl=ru&u=$source")
                        } else {
                            Toast.makeText(context, "Сначала открой сайт для перевода", Toast.LENGTH_SHORT).show()
                        }
                    }
                    MenuRow("Персонализация", "Цвета, панели, анимации и жесты") {
                        personalizationOnly = true
                        privacyOnly = false
                        showSettings = true
                        menuDialog = false
                    }
                    MenuRow("Конфиденциальность", "Защита от трекеров и управление данными") {
                        personalizationOnly = false
                        privacyOnly = true
                        showSettings = true
                        menuDialog = false
                    }
                    MenuRow("Настройки", "Поиск, домашняя страница и основные параметры") {
                        personalizationOnly = false
                        privacyOnly = false
                        showSettings = true
                        menuDialog = false
                    }
                    MenuRow("Проверить обновления", "Проверить GitHub Releases") {
                        menuDialog = false
                        updateCheckRequested++
                    }
                    MenuRow("Политика конфиденциальности", "Как браузер обрабатывает данные") {
                        menuDialog = false
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Toska-0/FerBrows/blob/main/PRIVACY.md"))) }
                    }
                    MenuRow("Сообщить об ошибке", "Обратная связь и предложения") {
                        menuDialog = false
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Toska-0/FerBrows/issues"))) }
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
            },
            onEdit = { bookmarkEditing = it }
        )
    }

    if (showHistory) {
        SimpleListDialog(
            title = "История",
            items = history.map { it.url },
            labels = history.associate { it.url to it.title },
            emptyText = "История пока пуста",
            onDismiss = { showHistory = false },
            onSelect = { url -> showHistory = false; navigate(url) },
            searchable = true
        )
    }

    if (pendingCredential != null) {
        val candidate = pendingCredential!!
        AlertDialog(
            onDismissRequest = { pendingCredential = null },
            containerColor = Panel,
            title = { Text("Сохранить пароль?", color = White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(candidate.origin, color = Muted)
                    Text("Логин: ${candidate.username}", color = White)
                    Text("Пароль будет сохранён в зашифрованном хранилище устройства.", color = Muted)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { credentialVault.save(candidate.origin, candidate.username, candidate.password) }
                        .onSuccess { Toast.makeText(context, "Пароль сохранён", Toast.LENGTH_SHORT).show() }
                        .onFailure { Toast.makeText(context, "Не удалось сохранить пароль", Toast.LENGTH_SHORT).show() }
                    pendingCredential = null
                }) { Text("Сохранить", color = Lime) }
            },
            dismissButton = {
                TextButton(onClick = { pendingCredential = null }) { Text("Не сейчас", color = Muted) }
            }
        )
    }

    if (showCredentials) {
        val credentials = remember(showCredentials, credentialListRefresh) { credentialVault.all() }
        AlertDialog(
            onDismissRequest = { showCredentials = false },
            containerColor = Panel,
            title = { Text("Сохранённые пароли", color = White) },
            text = {
                if (credentials.isEmpty()) {
                    Text("Сохранённых учётных записей пока нет.", color = Muted)
                } else {
                    Column(
                        modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        credentials.forEach { credential ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(credential.origin, color = White, fontWeight = FontWeight.SemiBold)
                                Text(credential.username, color = Muted)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = {
                                        credentialVault.remove(credential.origin, credential.username)
                                        credentialListRefresh++
                                    }) { Text("Удалить", color = Color(0xFFFF7777)) }
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(PanelSoft))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCredentials = false }) { Text("Готово", color = Lime) } }
        )
    }
    



    if (showOnboarding) {
        WelcomeDialog(
            onStart = {
                showOnboarding = false
                onboardingPrefs.edit().putBoolean("completed", true).apply()
            },
            onPrivacyPolicy = {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Toska-0/FerBrows/blob/main/PRIVACY.md"))) }
            }
        )
    }

    if (showUpdateDialog && latestUpdate != null) {
        FerBrowsUpdateDialog(
            version = latestUpdate!!.version,
            downloading = updateDownloadId != null,
            progress = updateProgress,
            failed = updateFailed,
            onDownload = {
                runCatching {
                    updateFailed = false
                    updateProgress = 0
                    completedUpdateDownloadId = null
                    updateDownloadId = FerBrowsUpdater.enqueue(context, latestUpdate!!)
                }.onFailure {
                    updateFailed = true
                    Toast.makeText(context, "Не удалось начать загрузку обновления", Toast.LENGTH_LONG).show()
                }
            },
            onInstall = {
                val id = completedUpdateDownloadId
                if (id != null && FerBrowsUpdater.install(context, id)) showUpdateDialog = false
            },
            onDismiss = { if (updateDownloadId == null) showUpdateDialog = false }
        )
    }

    if (showDownloads) {
        DownloadsDialog(
            downloads = downloads,
            onOpen = { id ->
                runCatching { downloadsStore.open(id) }
                    .onFailure { Toast.makeText(context, "Не удалось открыть файл", Toast.LENGTH_SHORT).show() }
            },
            onRemove = { id ->
                downloadsStore.remove(id)
                downloads = downloadsStore.query()
            },
            onClearCompleted = {
                downloadsStore.clearCompleted()
                downloads = downloadsStore.query()
            },
            onDismiss = { showDownloads = false }
        )
    }

    if (bookmarkEditing != null) {
        BookmarkEditDialog(
            initialUrl = bookmarkEditing.orEmpty(),
            onSave = { raw ->
                val updated = if (raw.trim().contains(" ")) null else BrowserNavigation.resolveInput(raw)
                if (updated != null && (updated.startsWith("https://") || updated.startsWith("http://"))) {
                    val index = bookmarks.indexOf(bookmarkEditing)
                    if (index >= 0) {
                        bookmarks[index] = updated
                        persistBookmarks()
                    }
                    bookmarkEditing = null
                } else {
                    Toast.makeText(context, "Укажи корректный HTTP(S)-адрес", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { bookmarkEditing = null }
        )
    }

    if (showSettings) {
        BrowserSettingsDialog(
            settings = browserSettings,
            onSave = { updated ->
                browserSettings = updated
                trackerProtectionEnabled.set(updated.trackerProtectionEnabled)
                settingsStore.save(updated)
                webViews.values.forEach { it.settings.javaScriptEnabled = updated.javaScriptEnabled }
            },
            onClearHistory = {
                history.clear()
                webViews.values.forEach { it.clearHistory() }
                tabs.forEach { store.clearWebViewState(it.id) }
                persistHistory()
                Toast.makeText(context, "История очищена", Toast.LENGTH_SHORT).show()
            },
            onClearSiteData = {
                webViews.values.forEach { it.clearCache(true); it.clearFormData() }
                android.webkit.CookieManager.getInstance().removeAllCookies {
                    android.webkit.CookieManager.getInstance().flush()
                    android.webkit.WebStorage.getInstance().deleteAllData()
                    (context as? android.app.Activity)?.runOnUiThread {
                        Toast.makeText(context, "Кэш, cookies и данные сайтов очищены", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showSettings = false; personalizationOnly = false; privacyOnly = false },
            personalizationOnly = personalizationOnly,
            privacyOnly = privacyOnly
        )
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            containerColor = Panel,
            title = { Text("Выйти из FerBrows?", color = White) },
            text = { Text("Открытые обычные вкладки будут сохранены.", color = Muted) },
            confirmButton = {
                TextButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                    Text("Выйти", color = Lime)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) { Text("Остаться", color = Muted) }
            }
        )
    }
    }
}

private fun hasValidatedInternet(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}