package com.itextpdf.android.library.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.itextpdf.android.library.R
import com.itextpdf.android.library.PdfActivity
import com.itextpdf.android.library.SplitPdfActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
internal class SplitPdfActivityTest {

    @Rule
    @JvmField
    var activityScenarioRule = activityScenarioRule<SplitPdfActivity>()

    @Test
    fun testHelpDialog() {
        onView(withId(R.id.action_split_help))
            .perform(click())
    }

}