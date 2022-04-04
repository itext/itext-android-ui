package com.itextpdf.android.library.fragments

import android.net.Uri
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.itextpdf.android.library.R
import com.itextpdf.android.library.util.FileUtil
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class PdfFragmentTest {

    private val fileUtil = FileUtil.getInstance()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val file = fileUtil.loadFileFromAssets(context, "sample_1.pdf")
    private val pdfUri = Uri.fromFile(file)
    private val pdfConfig = PdfConfig(pdfUri = pdfUri)
    private val fragmentArgs = bundleOf(PdfFragment.EXTRA_PDF_CONFIG to pdfConfig)

    @Test
    fun testRecreation() {
        val scenario: FragmentScenario<PdfFragment> = launchFragmentInContainer(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.Theme_MaterialComponents_DayNight
        )

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.recreate()
    }

    @Test
    fun testLongPress() {

        val scenario: FragmentScenario<PdfFragment> = launchFragmentInContainer(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.Theme_MaterialComponents_DayNight
        )

        // Click on "annotations" menu-item
        onView(allOf(withId(R.id.pdfView), isDisplayed()))
            .perform(longClick())

        onView(allOf(withId(R.id.etTextAnnotation)))
            .check(matches(allOf(isDisplayed())))
            .perform(typeText("Lorem Ipsum"))

        onView(allOf(withId(R.id.btnSaveAnnotation)))
            .check(matches(allOf(isDisplayed())))
            .perform(click())

        onView(allOf(withId(R.id.pdfView), isDisplayed()))
            .perform(swipeUp(), click())

        waitForBottomSheetToOpen()

        onView(withId(R.id.rvAnnotations))
            .perform(click())

        onView(withId(R.id.ivMore)).perform(click())

        onView(allOf(withText(context.getString(R.string.edit))))
            .perform(click())
    }

    private fun waitForBottomSheetToOpen() {

        // TODO: We need to wait for the bottom sheet to open and almost all (technically feasible) tasks did not succeed.
        //  This is why we're currently using the dirty Thread.sleep() which must be removed
        //  Things I've tried already:
        //  - Disable windowAnimation-, transitionAnimation- and animatorDuration-scale on emulator: https://developer.android.com/training/testing/espresso/setup
        //  - Disable animations in build.gradle via testOptions.animationsDisabled = true
        Thread.sleep(1000)

    }


    /**
     * The file names of the pdf files that are stored in the assets folder.
     */
    private val pdfFileNames =
        mutableListOf(
            "sample_1.pdf",
            "sample_2.pdf",
            "sample_3.pdf",
            "sample_4.pdf",
            "sample_3.pdf",
            "sample_2.pdf"
        )

}