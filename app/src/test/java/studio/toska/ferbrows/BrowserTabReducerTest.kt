package studio.toska.ferbrows

import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserTabReducerTest {
    @Test fun addCreatesAndActivatesNewTab() {
        val initial = BrowserTabsState(listOf(BrowserTab(1, "Home", "")), 1, 2)
        val next = BrowserTabReducer.add(initial)
        assertEquals(listOf(1, 2), next.tabs.map { it.id })
        assertEquals(2, next.activeTabId)
        assertEquals(3, next.nextTabId)
    }

    @Test fun closingActiveTabSelectsPreviousTab() {
        val initial = BrowserTabsState(
            listOf(BrowserTab(1, "A", ""), BrowserTab(2, "B", ""), BrowserTab(3, "C", "")), 3, 4
        )
        val next = BrowserTabReducer.close(initial, 3)
        assertEquals(listOf(1, 2), next.tabs.map { it.id })
        assertEquals(2, next.activeTabId)
    }

    @Test fun closingInactiveTabKeepsActiveTab() {
        val initial = BrowserTabsState(listOf(BrowserTab(1, "A", ""), BrowserTab(2, "B", "")), 2, 3)
        assertEquals(2, BrowserTabReducer.close(initial, 1).activeTabId)
    }

    @Test fun closingLastTabCreatesFreshTab() {
        val initial = BrowserTabsState(listOf(BrowserTab(7, "A", "https://example.com")), 7, 8)
        val next = BrowserTabReducer.close(initial, 7)
        assertEquals(listOf(8), next.tabs.map { it.id })
        assertEquals(8, next.activeTabId)
        assertEquals(9, next.nextTabId)
    }
}
