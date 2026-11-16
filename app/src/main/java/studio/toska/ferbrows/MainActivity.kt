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
import java.util.concurrent.atomic.AtomicReference
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.ValueCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.webkit.WebViewCompat
import androidx.webkit.WebSettingsCompat
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLEncoder
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import org.json.JSONArray

internal data class BrowserPalette(
    val background: Color, val panel: Color, val panelSoft: Color,
    val text: Color, val muted: Color, val green: Color
)
private val DarkPalette = BrowserPalette(Color(0xFF050705), Color(0xFF111811), Color(0xFF172117), Color(0xFFEAF2E7), Color(0xFF91A38F), Color(0xFF4ADE80))
private val LightPalette = BrowserPalette(Color(0xFFF5F7F5), Color(0xFFFFFFFF), Color(0xFFE8EEE8), Color(0xFF172117), Color(0xFF647267), Color(0xFF16834A))
private val LocalBrowserPalette = compositionLocalOf { DarkPalette }
private val LocalAccentColor = compositionLocalOf { Color(0xFFC6FF1A) }
internal val Background: Color @Composable get() = LocalBrowserPalette.current.background
internal val Panel: Color @Composable get() = LocalBrowserPalette.current.panel
internal val PanelSoft: Color @Composable get() = LocalBrowserPalette.current.panelSoft
internal val Lime: Color @Composable get() = LocalAccentColor.current
internal val Green: Color @Composable get() = LocalBrowserPalette.current.green
internal val Muted: Color @Composable get() = LocalBrowserPalette.current.muted
internal val White: Color @Composable get() = LocalBrowserPalette.current.text

private fun applyWebTheme(view: WebView, dark: Boolean) {
    val settings = view.settings
    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
        WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, dark)
    } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
        WebSettingsCompat.setForceDark(settings, if (dark) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF)
        if (dark && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(settings, WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING)
        }
    }
}
private const val PRIVATE_PROFILE_NAME = "FerBrowsPrivate"


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val savedTheme = getSharedPreferences("ferbrows_settings", Context.MODE_PRIVATE)
            .getString("browser_theme", BrowserTheme.DARK.name)
        AppCompatDelegate.setDefaultNightMode(when (runCatching { BrowserTheme.valueOf(savedTheme ?: BrowserTheme.DARK.name) }.getOrDefault(BrowserTheme.DARK)) {
            BrowserTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            BrowserTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            BrowserTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        })
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
    var showCredentials by remember { mutableStateOf(false) }
    var credentialListRefresh by remember { mutableIntStateOf(0) }
    var addingCredential by remember { mutableStateOf(false) }
    var credentialTitleDraft by remember { mutableStateOf("") }
    var credentialWebsiteDraft by remember { mutableStateOf("") }
    var credentialUsernameDraft by remember { mutableStateOf("") }
    var credentialPasswordDraft by remember { mutableStateOf("") }
    var credentialNoteDraft by remember { mutableStateOf("") }
    var revealedCredentialIds by remember { mutableStateOf(setOf<String>()) }
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
    val downloadsStore = remember { BrowserDownloadsStore(context) }
    var downloads by remember { mutableStateOf(downloadsStore.query()) }
    var showExitConfirm by remember { mutableStateOf(false) }
    var bookmarkEditing by remember { mutableStateOf<String?>(null) }
    val onboardingPrefs = remember { context.getSharedPreferences("ferbrows_onboarding", Context.MODE_PRIVATE) }
    var showOnboarding by remember { mutableStateOf(!onboardingPrefs.getBoolean("completed", false)) }
    val settingsStore = remember { BrowserSettingsStore(context) }
    var browserSettings by remember { mutableStateOf(settingsStore.load()) }
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (browserSettings.theme) { BrowserTheme.DARK -> true; BrowserTheme.LIGHT -> false; BrowserTheme.SYSTEM -> systemDarkTheme }
    val accentColor = remember(browserSettings.accentColor, darkTheme) {
        if (!darkTheme && browserSettings.accentColor.equals("#C6FF1A", ignoreCase = true)) Color(0xFF557F00)
        else runCatching { Color(android.graphics.Color.parseColor(browserSettings.accentColor)) }
            .getOrDefault(Color(0xFFC6FF1A))
    }
    val trackerProtectionEnabled = remember { AtomicBoolean(browserSettings.trackerProtectionEnabled) }
    val adBlockLevel = remember { AtomicReference(browserSettings.adBlockLevel) }
    val browserFieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        focusedTextColor = White, unfocusedTextColor = White,
        focusedLabelColor = Lime, unfocusedLabelColor = Muted,
        focusedPlaceholderColor = Muted, unfocusedPlaceholderColor = Muted,
        cursorColor = Lime, focusedBorderColor = Lime, unfocusedBorderColor = PanelSoft,
        focusedContainerColor = Panel, unfocusedContainerColor = Panel
    )
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
    var homeVisibleTabId by remember { mutableIntStateOf(activeTabId.takeIf { id -> tabs.firstOrNull { it.id == id }?.url.isNullOrBlank() } ?: -1) }
    val tabTransition = remember { Animatable(0f) }
    val tabTransitionScope = rememberCoroutineScope()
    val tabTransitionJob = remember { mutableStateOf<Job?>(null) }
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

    val lifecycleActivity = context as? AppCompatActivity
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

    // Temporarily disable the new filter-list engine while investigating startup crashes.

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
            applyWebTheme(this, darkTheme)
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
            setBackgroundColor(if (darkTheme) android.graphics.Color.rgb(5, 7, 5) else android.graphics.Color.rgb(245, 247, 245))
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
                    val requestUri = request?.url
                    if ((trackerProtectionEnabled.get() && TrackerProtection.shouldBlock(requestUri?.host)) ||
                        AdBlocker.shouldBlock(requestUri?.host, adBlockLevel.get()) ||
                        false) {
                        return android.webkit.WebResourceResponse(
                            "text/plain", "UTF-8", 204, "Blocked by FerBrows",
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
                        if (activeTabId == tabId) { currentUrl = ""; address = ""; pageTitle = "Новая вкладка"; homeVisibleTabId = tabId }
                    } else if (!url.isNullOrBlank()) {
                        val index = tabs.indexOfFirst { it.id == tabId }
                        if (index >= 0) tabs[index] = tabs[index].copy(url = url)
                        persistTabs()
                        if (activeTabId == tabId) { currentUrl = url; address = url; loading = true }
                    }
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    if (activeTabId == tabId && !url.isNullOrBlank() && url != "about:blank") homeVisibleTabId = -1
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
                        if (activeTabId == tabId) { currentUrl = ""; address = ""; pageTitle = "Новая вкладка"; homeVisibleTabId = tabId }
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
                        if (activeTabId == tabId) { currentUrl = url; address = url; pageTitle = title; pageError = null; homeVisibleTabId = -1 }
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
        if (browserSettings.reducedMotion) {
            tabTransitionJob.value?.cancel()
            tabTransitionJob.value = tabTransitionScope.launch { tabTransition.snapTo(0f) }
            val state = BrowserTabReducer.select(BrowserTabsState(tabs.toList(), activeTabId, nextTabId), tab.id)
            activeTabId = state.activeTabId
            homeVisibleTabId = if (tab.url.isBlank()) tab.id else -1
            persistTabs()
            updateTab(tab.url, tab.title)
            val selected = webViews[tab.id]
            canBack = selected?.canGoBack() == true
            canForward = selected?.canGoForward() == true
            loading = false
            progress = 0
            pageError = null
            return
        }
        tabTransitionJob.value?.cancel()
        tabTransitionJob.value = tabTransitionScope.launch {
            // Cover the web-content area before swapping WebViews. This avoids exposing
            // the empty frame Compose can produce while AndroidView detaches/reattaches.
            tabTransition.animateTo(0.84f, animationSpec = tween(135))
            val state = BrowserTabReducer.select(BrowserTabsState(tabs.toList(), activeTabId, nextTabId), tab.id)
            activeTabId = state.activeTabId
            homeVisibleTabId = if (tab.url.isBlank()) tab.id else -1
            persistTabs()
            updateTab(tab.url, tab.title)
            val selected = webViews[tab.id]
            canBack = selected?.canGoBack() == true
            canForward = selected?.canGoForward() == true
            loading = false
            progress = 0
            pageError = null
            // Let the new AndroidView attach and draw behind the dark layer first.
            delay(34)
            tabTransition.animateTo(0f, animationSpec = tween(250))
        }
    }

    fun addTab(isPrivate: Boolean = false) {
        val next = BrowserTabReducer.add(BrowserTabsState(tabs.toList(), activeTabId, nextTabId), isPrivate)
        tabs.clear(); tabs.addAll(next.tabs)
        activeTabId = next.activeTabId
        nextTabId = next.nextTabId
        currentUrl = ""; address = ""; pageTitle = "Новая вкладка"
        homeVisibleTabId = activeTabId
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
        homeVisibleTabId = if (selected.url.isBlank()) selected.id else -1
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

    SideEffect {
        val activity = context as? AppCompatActivity
        activity?.window?.let { window ->
            val barColor = if (darkTheme) android.graphics.Color.rgb(5, 7, 5) else android.graphics.Color.rgb(245, 247, 245)
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (darkTheme) 0 else
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }
    CompositionLocalProvider(LocalBrowserPalette provides if (darkTheme) DarkPalette else LightPalette, LocalAccentColor provides accentColor) {
    MaterialTheme(colorScheme = if (darkTheme) androidx.compose.material3.darkColorScheme() else androidx.compose.material3.lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .navigationBarsPadding()
                    .imePadding()
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
                                .alpha(if (homeVisibleTabId == activeTabId) 0f else 1f)
                        )
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = homeVisibleTabId == activeTabId,
                            modifier = Modifier.fillMaxSize(),
                            enter = if (browserSettings.reducedMotion) EnterTransition.None else fadeIn(),
                            exit = if (browserSettings.reducedMotion) ExitTransition.None else fadeOut()
                        ) {
                            HomeScreen(onSearch = { navigate(it) }, onOpen = { navigate(it) }, bookmarks = bookmarks, trackerProtectionEnabled = browserSettings.trackerProtectionEnabled)
                        }
                    }
                    if (tabTransition.value > 0.001f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = tabTransition.value))
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
                            currentUrl = ""; address = ""; pageTitle = "Новая вкладка"; pageError = null; homeVisibleTabId = activeTabId
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
                            colors = browserFieldColors,
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
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MenuRow("Закладки", "Сохранённые сайты") {
                        showBookmarks = true
                        menuDialog = false
                    }
                    MenuRow("История", "Недавно открытые страницы") {
                        showHistory = true
                        menuDialog = false
                    }
                    MenuRow("Сохранённые пароли", "Логины, пароли и личные заметки") {
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

    if (showCredentials) {
        val credentials = remember(showCredentials, credentialListRefresh) { credentialVault.all() }
        AlertDialog(
            onDismissRequest = { showCredentials = false; addingCredential = false },
            containerColor = Panel,
            title = { Text(if (addingCredential) "Новая запись" else "Сохранённые пароли", color = White) },
            text = {
                if (addingCredential) {
                    Column(
                        modifier = Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        androidx.compose.material3.OutlinedTextField(
                            colors = browserFieldColors,
                            value = credentialTitleDraft, onValueChange = { credentialTitleDraft = it.take(80) },
                            label = { Text("Название записи *") }, placeholder = { Text("Например, Google") },
                            singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        androidx.compose.material3.OutlinedTextField(
                            colors = browserFieldColors,
                            value = credentialWebsiteDraft, onValueChange = { credentialWebsiteDraft = it.take(240) },
                            label = { Text("Сайт (необязательно)") }, placeholder = { Text("google.com") },
                            singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        androidx.compose.material3.OutlinedTextField(
                            colors = browserFieldColors,
                            value = credentialUsernameDraft, onValueChange = { credentialUsernameDraft = it.take(240) },
                            label = { Text("Логин или почта *") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        androidx.compose.material3.OutlinedTextField(
                            colors = browserFieldColors,
                            value = credentialPasswordDraft, onValueChange = { credentialPasswordDraft = it },
                            label = { Text("Пароль *") }, singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        androidx.compose.material3.OutlinedTextField(
                            colors = browserFieldColors,
                            value = credentialNoteDraft, onValueChange = { credentialNoteDraft = it.take(2000) },
                            label = { Text("Заметка") }, placeholder = { Text("Дополнительная информация") },
                            modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4
                        )
                        Text("Все поля записи будут зашифрованы на устройстве.", color = Muted, fontSize = 12.sp)
                    }
                } else if (credentials.isEmpty()) {
                    Text("Записей пока нет. Добавь логин, почту и пароль вручную.", color = Muted)
                } else {
                    Column(
                        modifier = Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        credentials.forEach { credential ->
                            val revealed = credential.id in revealedCredentialIds
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(credential.title, color = White, fontWeight = FontWeight.SemiBold)
                                if (credential.origin.isNotBlank()) Text(credential.origin, color = Muted, fontSize = 12.sp)
                                Text("Логин: ${credential.username}", color = White, fontSize = 13.sp)
                                if (revealed) Text("Пароль: ${credential.password}", color = White, fontSize = 13.sp)
                                if (credential.note.isNotBlank()) Text(credential.note, color = Muted, fontSize = 12.sp)
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        TextButton(onClick = {
                                            revealedCredentialIds = if (revealed) revealedCredentialIds - credential.id else revealedCredentialIds + credential.id
                                        }) { Text(if (revealed) "Скрыть пароль" else "Показать пароль", color = Lime) }
                                        TextButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Логин", credential.username))
                                            Toast.makeText(context, "Логин скопирован", Toast.LENGTH_SHORT).show()
                                        }) { Text("Копировать логин", color = Muted) }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        TextButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Пароль", credential.password))
                                            Toast.makeText(context, "Пароль скопирован", Toast.LENGTH_SHORT).show()
                                        }) { Text("Копировать пароль", color = Muted) }
                                        TextButton(onClick = {
                                            credentialVault.remove(credential.id)
                                            credentialListRefresh++
                                            revealedCredentialIds = revealedCredentialIds - credential.id
                                        }) { Text("Удалить", color = Color(0xFFFF7777)) }
                                    }
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(PanelSoft))
                        }
                    }
                }
            },
            confirmButton = {
                if (addingCredential) {
                    TextButton(onClick = {
                        if (credentialTitleDraft.isBlank() || credentialUsernameDraft.isBlank() || credentialPasswordDraft.isBlank()) {
                            Toast.makeText(context, "Заполни название, логин и пароль", Toast.LENGTH_SHORT).show()
                        } else {
                            runCatching {
                                credentialVault.add(
                                    credentialTitleDraft, credentialWebsiteDraft, credentialUsernameDraft,
                                    credentialPasswordDraft, credentialNoteDraft
                                )
                            }.onSuccess {
                                credentialListRefresh++
                                addingCredential = false
                                credentialTitleDraft = ""; credentialWebsiteDraft = ""; credentialUsernameDraft = ""
                                credentialPasswordDraft = ""; credentialNoteDraft = ""
                                Toast.makeText(context, "Запись сохранена в зашифрованном хранилище", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                Toast.makeText(context, "Проверь адрес сайта и попробуй ещё раз", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) { Text("Сохранить", color = Lime) }
                } else {
                    TextButton(onClick = {
                        addingCredential = true
                        credentialTitleDraft = ""; credentialWebsiteDraft = ""; credentialUsernameDraft = ""
                        credentialPasswordDraft = ""; credentialNoteDraft = ""
                    }) { Text("+ Добавить", color = Lime) }
                    TextButton(onClick = { showCredentials = false }) { Text("Готово", color = Muted) }
                }
            },
            dismissButton = {
                if (addingCredential) TextButton(onClick = { addingCredential = false }) { Text("Назад", color = Muted) }
            }
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
            releaseNotes = latestUpdate!!.releaseNotes,
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
                adBlockLevel.set(updated.adBlockLevel)
                settingsStore.save(updated)
                AppCompatDelegate.setDefaultNightMode(when (updated.theme) {
                    BrowserTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    BrowserTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    BrowserTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                })
                val updatedDark = when (updated.theme) { BrowserTheme.DARK -> true; BrowserTheme.LIGHT -> false; BrowserTheme.SYSTEM -> systemDarkTheme }
                webViews.values.forEach { view -> view.settings.javaScriptEnabled = updated.javaScriptEnabled; applyWebTheme(view, updatedDark) }
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
                TextButton(onClick = { (context as? AppCompatActivity)?.finish() }) {
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