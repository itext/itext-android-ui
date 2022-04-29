package com.itextpdf.android.app.ui


import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.itextpdf.android.app.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun mainActivityTest() {

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Click on first PDF entry in list
        onView(withId(R.id.rvPdfList))
            .perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Click on "annotations" menu-item
        onView(allOf(withId(R.id.action_annotations), isDisplayed()))
            .perform(click())

        // Check that no-annotations title is shown
        onView(withId(R.id.no_annotations_title))
            .check(
                matches(
                    allOf(
                        withText(context.getString(R.string.no_annotations_title)),
                        isDisplayed()
                    )
                )
            )

        // Check that no-annotations description is shown
        onView(withId(R.id.no_annotations_message))
            .check(
                matches(
                    allOf(
                        withText(context.getString(R.string.no_annotations_description)),
                        isDisplayed()
                    )
                )
            )


        // Click navigate-pdf menu item
        onView(allOf(withId(R.id.action_navigate_pdf), isDisplayed()))
            .perform(click())

        // Check that pdf-page thumbnails are shown in bottom-sheet
        onView(allOf(withId(R.id.rvPdfPages)))
            .check(matches(isDisplayed()))

    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
