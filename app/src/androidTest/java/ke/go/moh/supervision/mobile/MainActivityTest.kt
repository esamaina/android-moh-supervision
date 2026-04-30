package ke.go.moh.supervision.mobile

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.hamcrest.text.IsEmptyString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Before
    fun seedCredentials() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val session = SessionManager(context)
        session.saveBaseUrl("https://chw-supervision.echis.go.ke/")
        session.saveUsername("test-user")
        session.savePassword("test-password")
        session.saveRole("admin")
    }

    @Test
    fun syncCenterPrefillsNonEmptyDeviceId() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.deviceIdInput)).check(matches(isDisplayed()))
            onView(withId(R.id.deviceIdInput))
                .check(matches(withText(not(IsEmptyString.isEmptyOrNullString()))))
        }
    }

    @Test
    fun syncCenterPersistsManualDeviceIdOverride() {
        val customId = "android-test-manual-001"

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.deviceIdInput))
                .perform(replaceText(customId), closeSoftKeyboard())
            // Triggers SessionManager + DeviceIdProvider save path before network call.
            onView(withId(R.id.pushSyncBtn)).perform(click())
        }

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.deviceIdInput)).check(matches(withText(customId)))
        }
    }
}
