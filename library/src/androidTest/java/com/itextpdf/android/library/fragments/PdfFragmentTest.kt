package com.itextpdf.android.library.fragments

import android.net.Uri
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.itextpdf.android.library.R
import com.itextpdf.android.library.util.FileUtil
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PdfFragmentTest {

    private val fileUtil = FileUtil.getInstance()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testLongPress() {

        val file = fileUtil.loadFileFromAssets(context, "sample_1.pdf")
        val pdfUri = Uri.fromFile(file)

        val pdfConfig = PdfConfig(pdfUri = pdfUri)

        val fragmentArgs = bundleOf(PdfFragment.EXTRA_PDF_CONFIG to pdfConfig)

        val scenario: FragmentScenario<PdfFragment> = launchFragmentInContainer(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.Theme_MaterialComponents_DayNight
        )

        // Click on "annotations" menu-item
        onView(Matchers.allOf(ViewMatchers.withId(R.id.pdfView), ViewMatchers.isDisplayed()))
            .perform(ViewActions.longClick())

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