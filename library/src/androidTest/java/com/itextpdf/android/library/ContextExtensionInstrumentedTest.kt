package com.itextpdf.android.library

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.itextpdf.android.library.extensions.getFileName

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File
import java.lang.Exception
import androidx.core.content.FileProvider
import com.itextpdf.android.library.test.BuildConfig.APPLICATION_ID
import java.util.*

/**
 * Tests the functions in the Context+Extension
 */
@RunWith(AndroidJUnit4::class)
class ContextExtensionInstrumentedTest {
    @Test
    fun getFileName() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedFileName = "test.pdf"

        // test with fake path
        val testPath = "file://folder1/folder2/folder3/test.pdf"
        var fileName = appContext.getFileName(Uri.parse(testPath))
        assertEquals(expectedFileName, fileName)

        // create file in cache directory and use file uri that starts with "file://"
        val file = File(appContext.cacheDir, expectedFileName)
        try {
            file.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fileName = appContext.getFileName(Uri.fromFile(file))
        assertEquals(expectedFileName, fileName)

        // use fileProvider to get file uri that starts with "content://"
        val uri = FileProvider.getUriForFile(
            Objects.requireNonNull(appContext),
            appContext.packageName + ".provider", file);
        fileName = appContext.getFileName(uri)
        assertEquals(expectedFileName, fileName)
    }
}