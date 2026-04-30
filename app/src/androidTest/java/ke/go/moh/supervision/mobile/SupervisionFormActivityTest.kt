package ke.go.moh.supervision.mobile

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ke.go.moh.supervision.mobile.data.QuestionnaireSchemaManager
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SupervisionFormActivityTest {
    @Test
    fun sectionNavigationShowsExpectedQuestionnaireBlocks() {
        ActivityScenario.launch(SupervisionFormActivity::class.java).use {
            onView(withId(R.id.sectionView)).check(matches(withText("Section: Location")))
            onView(withId(R.id.countyInput)).check(matches(isDisplayed()))
            onView(withId(R.id.respondentTypeInput))
                .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))

            onView(withId(R.id.nextSectionBtn)).perform(scrollTo(), click())
            onView(withId(R.id.sectionView)).check(matches(withText("Section: Respondent")))
            onView(withId(R.id.respondentTypeInput)).check(matches(isDisplayed()))
            onView(withId(R.id.commentsInput))
                .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))

            onView(withId(R.id.nextSectionBtn)).perform(scrollTo(), click())
            onView(withId(R.id.sectionView)).check(matches(withText("Section: Assessment")))
            onView(withId(R.id.commentsInput)).check(matches(isDisplayed()))
            onView(withId(R.id.recordStatusSpinner)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun countyLevelKeepsLowerLocationFieldsHiddenAfterSectionRoundTrip() {
        ActivityScenario.launch(SupervisionFormActivity::class.java).use {
            onView(withId(R.id.levelSpinner)).perform(scrollTo(), click())
            onData(allOf(`is`("county"))).perform(click())

            onView(withId(R.id.subCountyInput))
                .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))
            onView(withId(R.id.chuInput))
                .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))
            onView(withId(R.id.facilityInput))
                .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))

            onView(withId(R.id.nextSectionBtn)).perform(scrollTo(), click())
            onView(withId(R.id.prevSectionBtn)).perform(scrollTo(), click())

            onView(withId(R.id.subCountyInput))
                .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))
            onView(withId(R.id.chuInput))
                .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))
            onView(withId(R.id.facilityInput))
                .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))
        }
    }

    @Test
    fun submitBlockedWhenSchemaCoverageGapDetected() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val session = SessionManager(context)
        session.saveBaseUrl("https://chw-supervision.echis.go.ke/")
        session.saveUsername("test-user")
        session.savePassword("test-password")
        session.saveRole("admin")
        QuestionnaireSchemaManager(context).saveVersion(
            version = "schema-gap-test",
            coverageOk = false
        )

        ActivityScenario.launch(SupervisionFormActivity::class.java).use {
            onView(withId(R.id.submitBtn)).perform(scrollTo(), click())
            onView(withId(R.id.statusView)).check(
                matches(withText("Questionnaire coverage gap detected. Sync latest schema before submitting."))
            )
        }
    }
}
