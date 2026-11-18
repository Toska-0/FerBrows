package studio.toska.ferbrows

import android.content.Context

enum class BrowserTheme(val label: String) {
    SYSTEM("Как в системе"), LIGHT("Светлая"), DARK("Тёмная")
}

enum class AdBlockLevel(val label: String, val description: String) {
    LOW("Слабая защита", "Блокирует основные рекламные сети, минимально влияет на сайты"),
    MEDIUM("Средняя защита", "Блокирует популярную рекламу и рекламные трекеры"),
    MAXIMUM("Максимальная защита", "Более строгая фильтрация; некоторые сайты могут работать некорректно")
}

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
    val adBlockLevel: AdBlockLevel = AdBlockLevel.MEDIUM,
    val reducedMotion: Boolean = false,
    val compactInterface: Boolean = false,
    val swipeNavigationEnabled: Boolean = true,
    val accentColor: String = "#C6FF1A",
    val theme: BrowserTheme = BrowserTheme.DARK,
    val streetArtEnabled: Boolean = false,
    val streetArtDrawings: Boolean = true,
    val streetArtTypography: Boolean = true,
    val streetArtAnimations: Boolean = true,
    val streetArtIntensity: Int = 2,
    val streetArtFont: String = "CONDENSED"
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
        adBlockLevel = runCatching { AdBlockLevel.valueOf(prefs.getString("ad_block_level", AdBlockLevel.MEDIUM.name)!!) }.getOrDefault(AdBlockLevel.MEDIUM),
        reducedMotion = prefs.getBoolean("reduced_motion", false),
        compactInterface = prefs.getBoolean("compact_interface", false),
        swipeNavigationEnabled = prefs.getBoolean("swipe_navigation_enabled", true),
        accentColor = prefs.getString("accent_color", "#C6FF1A") ?: "#C6FF1A",
        theme = runCatching { BrowserTheme.valueOf(prefs.getString("browser_theme", BrowserTheme.DARK.name)!!) }.getOrDefault(BrowserTheme.DARK),
        streetArtEnabled = prefs.getBoolean("street_art_enabled", false),
        streetArtDrawings = prefs.getBoolean("street_art_drawings", true),
        streetArtTypography = prefs.getBoolean("street_art_typography", true),
        streetArtAnimations = prefs.getBoolean("street_art_animations", true),
        streetArtIntensity = prefs.getInt("street_art_intensity", 2).coerceIn(1, 3),
        streetArtFont = prefs.getString("street_art_font", "CONDENSED") ?: "CONDENSED"
    )

    fun save(settings: BrowserSettings) {
        prefs.edit()
            .putString("search_engine", settings.searchEngine.name)
            .putString("home_page", settings.homePage.trim())
            .putBoolean("javascript_enabled", settings.javaScriptEnabled)
            .putBoolean("tracker_protection_enabled", settings.trackerProtectionEnabled)
            .putString("ad_block_level", settings.adBlockLevel.name)
            .putBoolean("reduced_motion", settings.reducedMotion)
            .putBoolean("compact_interface", settings.compactInterface)
            .putBoolean("swipe_navigation_enabled", settings.swipeNavigationEnabled)
            .putString("accent_color", settings.accentColor)
            .putString("browser_theme", settings.theme.name)
            .putBoolean("street_art_enabled", settings.streetArtEnabled)
            .putBoolean("street_art_drawings", settings.streetArtDrawings)
            .putBoolean("street_art_typography", settings.streetArtTypography)
            .putBoolean("street_art_animations", settings.streetArtAnimations)
            .putInt("street_art_intensity", settings.streetArtIntensity)
            .putString("street_art_font", settings.streetArtFont)
            .apply()
    }
}
