package com.itextpdf.android.library.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.itextpdf.android.library.R
import com.itextpdf.android.library.PdfActivity
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
internal class PdfActivityTest {

    @Rule
    @JvmField
    var activityScenarioRule = activityScenarioRule<PdfActivity>()

    private val context = getInstrumentation().targetContext

    @Test
    fun testNavigatePdf() {
        onView(withId(R.id.action_navigate_pdf))
            .perform(click())
    }

    @Test
    fun testHighlight() {
        onView(withId(R.id.action_highlight))
            .perform(click())
    }

    @Test
    fun testAnnotations() {

        openActionBarOverflowOrOptionsMenu(context)
        onView(withText(context.getString(R.string.split_document)))
            .perform(click())
    }


    @Test
    fun testSplitPdf() {

        openActionBarOverflowOrOptionsMenu(context)
        onView(withText(context.getString(R.string.add_annotation)))
            .perform(click())
    }

}