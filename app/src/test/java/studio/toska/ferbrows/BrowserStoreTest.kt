package studio.toska.ferbrows

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class BrowserStoreTest {
    private lateinit var context: Context
    private lateinit var store: BrowserStore

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("ferbrows_state", Context.MODE_PRIVATE).edit().clear().commit()
        store = BrowserStore(context)
    }

    @Test
    fun persistsNormalTabsAndActiveTabButNeverPrivateTabs() {
        val tabs = listOf(
            BrowserTab(4, "Normal", "https://example.com"),
            BrowserTab(5, "Private", "https://private.example", isPrivate = true)
        )

        store.saveTabs(tabs, activeId = 5, nextId = 6)

        assertEquals(listOf(4), store.loadTabs().map { it.id })
        assertEquals(4, store.loadActiveTabId())
        assertEquals(6, store.loadNextTabId())
    }

    @Test
    fun privateOnlySessionDoesNotPersistAnyTab() {
        store.saveTabs(
            listOf(BrowserTab(8, "Private", "https://private.example", isPrivate = true)),
            activeId = 8,
            nextId = 9
        )

        assertTrue(store.loadTabs().isEmpty())
        assertEquals(1, store.loadActiveTabId())
        assertEquals(9, store.loadNextTabId())
    }

    @Test
    fun webViewStateCanBeSavedAndCleared() {
        store.saveWebViewState(3, "encoded-state")
        assertEquals("encoded-state", store.loadWebViewState(3))

        store.clearWebViewState(3)
        assertNull(store.loadWebViewState(3))
    }

    @Test
    fun legacyHistorySplitsOnActualNewlines() {
        context.getSharedPreferences("ferbrows_state", Context.MODE_PRIVATE)
            .edit().putString("history", "https://one.example\\nhttps://two.example").commit()

        assertEquals(
            listOf("https://one.example", "https://two.example"),
            store.loadHistory().map { it.url }
        )
    }
}
