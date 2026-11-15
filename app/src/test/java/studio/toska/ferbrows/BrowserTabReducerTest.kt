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

    @Test fun selectingExistingTabChangesOnlyActiveId() {
        val initial = BrowserTabsState(
            listOf(BrowserTab(1, "A", "https://a.example"), BrowserTab(2, "B", "https://b.example")),
            1,
            3
        )
        val next = BrowserTabReducer.select(initial, 2)
        assertEquals(2, next.activeTabId)
        assertEquals(initial.tabs, next.tabs)
        assertEquals(initial.nextTabId, next.nextTabId)
    }

    @Test fun selectingUnknownTabLeavesStateUnchanged() {
        val initial = BrowserTabsState(listOf(BrowserTab(1, "A", "")), 1, 2)
        assertEquals(initial, BrowserTabReducer.select(initial, 99))
    }

    @Test fun privateTabIsMarkedByReducer() {
        val initial = BrowserTabsState(listOf(BrowserTab(1, "A", "")), 1, 2)
        val next = BrowserTabReducer.add(initial, isPrivate = true)
        assertEquals(true, next.tabs.last().isPrivate)
        assertEquals(2, next.activeTabId)
        assertEquals(3, next.nextTabId)
    }

    @Test fun closingInactivePrivateTabKeepsNormalActiveTab() {
        val initial = BrowserTabsState(
            listOf(BrowserTab(1, "Normal", ""), BrowserTab(2, "Private", "", true)),
            1,
            3
        )
        val next = BrowserTabReducer.close(initial, 2)
        assertEquals(1, next.activeTabId)
        assertEquals(listOf(1), next.tabs.map { it.id })
    }
}
