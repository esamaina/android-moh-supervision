package ke.go.moh.supervision.mobile

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandingActivityTest {
    @get:Rule
    val scenarioRule = ActivityScenarioRule(LandingActivity::class.java)

    @Test
    fun getStartedButton_isVisible() {
        onView(withId(R.id.getStartedBtn)).check(matches(isDisplayed()))
    }
}
