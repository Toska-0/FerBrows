package studio.toska.ferbrows

import android.content.Context

enum class SearchEngine(val label: String, val template: String) {
    GOOGLE("Google", "https://www.google.com/search?q=%s"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=%s"),
    BING("Bing", "https://www.bing.com/search?q=%s")
}

data class BrowserSettings(
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val homePage: String = "",
    val javaScriptEnabled: Boolean = true,
    val trackerProtectionEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val compactInterface: Boolean = false,
    val swipeNavigationEnabled: Boolean = true,
    val accentColor: String = "#C6FF1A"
)

class BrowserSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("ferbrows_settings", Context.MODE_PRIVATE)

    fun load(): BrowserSettings = BrowserSettings(
        searchEngine = runCatching {
            SearchEngine.valueOf(prefs.getString("search_engine", SearchEngine.GOOGLE.name)!!)
        }.getOrDefault(SearchEngine.GOOGLE),
        homePage = prefs.getString("home_page", "").orEmpty(),
        javaScriptEnabled = prefs.getBoolean("javascript_enabled", true),
        trackerProtectionEnabled = prefs.getBoolean("tracker_protection_enabled", true),
        reducedMotion = prefs.getBoolean("reduced_motion", false),
        compactInterface = prefs.getBoolean("compact_interface", false),
        swipeNavigationEnabled = prefs.getBoolean("swipe_navigation_enabled", true),
        accentColor = prefs.getString("accent_color", "#C6FF1A") ?: "#C6FF1A"
    )

    fun save(settings: BrowserSettings) {
        prefs.edit()
            .putString("search_engine", settings.searchEngine.name)
            .putString("home_page", settings.homePage.trim())
            .putBoolean("javascript_enabled", settings.javaScriptEnabled)
            .putBoolean("tracker_protection_enabled", settings.trackerProtectionEnabled)
            .putBoolean("reduced_motion", settings.reducedMotion)
            .putBoolean("compact_interface", settings.compactInterface)
            .putBoolean("swipe_navigation_enabled", settings.swipeNavigationEnabled)
            .putString("accent_color", settings.accentColor)
            .apply()
    }
}
