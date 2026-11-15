package studio.toska.ferbrows

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrowserSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun firstRunIntroductionCanBeDismissed() {
        composeRule.onNodeWithText("Добро пожаловать в FerBrows").assertExists()
        composeRule.onNodeWithText("Начать работу").performClick()
        composeRule.onNodeWithText("Что будем искать?").assertExists()
    }

    @Test
    fun browserHeaderIsVisible() {
        composeRule.onNodeWithText("FerBrows").assertExists()
    }
}
