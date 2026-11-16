package studio.toska.ferbrows

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class BrowserSettingsStoreTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("ferbrows_settings", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun defaultsToDarkThemeForExistingInstallations() {
        assertEquals(BrowserTheme.DARK, BrowserSettingsStore(context).load().theme)
    }

    @Test
    fun persistsPersonalizationPreferences() {
        val store = BrowserSettingsStore(context)
        val expected = BrowserSettings(
            reducedMotion = true,
            compactInterface = true,
            swipeNavigationEnabled = false,
            accentColor = "#55D6FF",
            theme = BrowserTheme.LIGHT
        )

        store.save(expected)

        val restored = store.load()
        assertEquals(expected.reducedMotion, restored.reducedMotion)
        assertEquals(expected.compactInterface, restored.compactInterface)
        assertEquals(expected.swipeNavigationEnabled, restored.swipeNavigationEnabled)
        assertEquals(expected.accentColor, restored.accentColor)
        assertEquals(expected.theme, restored.theme)
    }
}
