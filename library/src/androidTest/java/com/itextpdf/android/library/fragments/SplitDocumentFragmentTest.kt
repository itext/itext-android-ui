package com.itextpdf.android.library.fragments

import android.net.Uri
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.itextpdf.android.library.R
import com.itextpdf.android.library.util.FileUtil
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class SplitDocumentFragmentTest {

    private val fileUtil = FileUtil.getInstance()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val file = fileUtil.loadFileFromAssets(context, "sample_1.pdf")
    private val pdfUri = Uri.fromFile(file)
    private val pdfConfig = PdfConfig(pdfUri = pdfUri)
    private val fragmentArgs = bundleOf(SplitDocumentFragment.EXTRA_PDF_CONFIG to pdfConfig)

    @Test
    fun testRecreation() {

        val scenario: FragmentScenario<SplitDocumentFragment> = launchFragmentInContainer(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.Theme_MaterialComponents_DayNight
        )

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.recreate()
    }

    @Test
    fun testSelectFirstPage() {

        val scenario: FragmentScenario<SplitDocumentFragment> = launchFragmentInContainer(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.Theme_MaterialComponents_DayNight
        )

        // Click on "annotations" menu-item
        onView(allOf(withId(R.id.rvSplitDocument), isDisplayed()))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(allOf(withId(R.id.fabSplit), isDisplayed()))
            .perform(click())

    }

    @Test
    fun testCloseButton() {

        val scenario: FragmentScenario<SplitDocumentFragment> = launchFragmentInContainer(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.Theme_MaterialComponents_DayNight
        )

        onView(
            allOf(
                withParent(withId(R.id.tbSplitDocumentFragment)),
                withContentDescription(R.string.close)
            )
        ).perform(click());

    }

}