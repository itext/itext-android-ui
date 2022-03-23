package com.itextpdf.android.library.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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

    @Test
    fun testInflateFragmentViaActivityLayoutXML() {

        onView(allOf(ViewMatchers.withId(R.id.pdfView), isDisplayed()))
            .perform(longClick())
    }

}