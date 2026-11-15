package studio.toska.ferbrows

data class BrowserTabsState(
    val tabs: List<BrowserTab>,
    val activeTabId: Int,
    val nextTabId: Int
)

object BrowserTabReducer {
    fun select(state: BrowserTabsState, id: Int): BrowserTabsState =
        if (state.tabs.any { it.id == id }) state.copy(activeTabId = id) else state

    fun add(state: BrowserTabsState): BrowserTabsState {
        val tab = BrowserTab(state.nextTabId, "Новая вкладка", "")
        return state.copy(tabs = state.tabs + tab, activeTabId = tab.id, nextTabId = state.nextTabId + 1)
    }

    fun close(state: BrowserTabsState, id: Int): BrowserTabsState {
        val index = state.tabs.indexOfFirst { it.id == id }
        if (index < 0) return state
        val remaining = state.tabs.filterNot { it.id == id }
        if (remaining.isEmpty()) {
            val fresh = BrowserTab(state.nextTabId, "Новая вкладка", "")
            return BrowserTabsState(listOf(fresh), fresh.id, state.nextTabId + 1)
        }
        val active = if (state.activeTabId == id) remaining.getOrNull((index - 1).coerceAtLeast(0))?.id ?: remaining.first().id else state.activeTabId
        return state.copy(tabs = remaining, activeTabId = active)
    }
}
