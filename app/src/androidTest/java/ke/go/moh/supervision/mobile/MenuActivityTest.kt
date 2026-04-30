package ke.go.moh.supervision.mobile

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuActivityTest {
    @Before
    fun seedSession() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val session = SessionManager(context)
        session.saveBaseUrl("https://chw-supervision.echis.go.ke/")
        session.saveUsername("test-user")
        session.savePassword("test-password")
        session.saveRole("admin")
    }

    @Test
    fun menuButtons_areVisible() {
        ActivityScenario.launch(MenuActivity::class.java).use {
            onView(withId(R.id.supervisionBtn)).check(matches(isDisplayed()))
            onView(withId(R.id.reportsBtn)).check(matches(isDisplayed()))
            onView(withId(R.id.dashboardBtn)).check(matches(isDisplayed()))
            onView(withId(R.id.historyBtn)).check(matches(isDisplayed()))
            onView(withId(R.id.syncBtn)).check(matches(isDisplayed()))
            onView(withId(R.id.logoutBtn)).check(matches(isDisplayed()))
        }
    }
}
