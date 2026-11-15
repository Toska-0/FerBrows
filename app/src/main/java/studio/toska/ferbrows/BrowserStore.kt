package studio.toska.ferbrows

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class BrowserTab(val id: Int, val title: String, val url: String, val isPrivate: Boolean = false)
data class HistoryEntry(val url: String, val title: String, val visitedAt: Long)

class BrowserStore(context: Context) {
    private val prefs = context.getSharedPreferences("ferbrows_state", Context.MODE_PRIVATE)

    fun loadTabs(): List<BrowserTab> = runCatching {
        val array = JSONArray(prefs.getString("tabs_v2", "[]"))
        (0 until array.length()).mapNotNull { i ->
            array.optJSONObject(i)?.let { BrowserTab(it.optInt("id"), it.optString("title", "Новая вкладка"), it.optString("url")) }
        }.filter { it.id > 0 }
    }.getOrDefault(emptyList())

    fun saveTabs(tabs: List<BrowserTab>, activeId: Int, nextId: Int) {
        val array = JSONArray()
        tabs.filterNot { it.isPrivate }.forEach { tab -> array.put(JSONObject().put("id", tab.id).put("title", tab.title).put("url", tab.url)) }
        val safeActiveId = tabs.firstOrNull { it.id == activeId && !it.isPrivate }?.id
            ?: tabs.firstOrNull { !it.isPrivate }?.id ?: 1
        prefs.edit().putString("tabs_v2", array.toString())
            .putInt("active_tab_id", safeActiveId).putInt("next_tab_id", nextId).apply()
    }

    fun loadActiveTabId(): Int = prefs.getInt("active_tab_id", 1)
    fun loadNextTabId(): Int = prefs.getInt("next_tab_id", 2)

    fun loadBookmarks(): List<String> = runCatching {
        val raw = prefs.getString("bookmarks_v2", null)
        if (raw == null) {
            val legacy = prefs.getStringSet("bookmarks", null)?.toList()
            return legacy?.sorted() ?: listOf("https://www.google.com", "https://www.youtube.com", "https://github.com")
        }
        val array = JSONArray(raw)
        (0 until array.length()).map { array.optString(it) }.filter { it.startsWith("http://") || it.startsWith("https://") }
    }.getOrDefault(listOf("https://www.google.com", "https://www.youtube.com", "https://github.com"))

    fun saveBookmarks(items: List<String>) {
        val array = JSONArray()
        items.forEach { array.put(it) }
        prefs.edit().putString("bookmarks_v2", array.toString()).apply()
    }

    fun loadHistory(): List<HistoryEntry> = runCatching {
        val raw = prefs.getString("history_v2", null)
        if (raw == null) {
            return prefs.getString("history", null)?.split("\\n")?.filter { it.startsWith("http") }
                ?.distinct()?.take(100)?.map { HistoryEntry(it, it, 0L) } ?: emptyList()
        }
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { i ->
            array.optJSONObject(i)?.let { HistoryEntry(it.optString("url"), it.optString("title", it.optString("url")), it.optLong("visitedAt")) }
        }
    }.getOrDefault(emptyList())

    fun saveHistory(items: List<HistoryEntry>) {
        val array = JSONArray()
        items.forEach { array.put(JSONObject().put("url", it.url).put("title", it.title).put("visitedAt", it.visitedAt)) }
        prefs.edit().putString("history_v2", array.toString()).apply()
    }
}
