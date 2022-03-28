package com.itextpdf.android.library.util

import android.content.Context
import android.net.Uri
import java.io.File

interface FileUtil {

    fun createTempCopy(context: Context, originalFile: File): File
    fun createTempCopyIfNotExists(context: Context, originalFile: File): File

    fun overrideFile(fileToSave: File, destinationUri: Uri): File
    fun loadFileFromAssets(context: Context, fileName: String): File

    companion object {
        private lateinit var instance: FileUtil

        fun getInstance(): FileUtil {
            if (!this::instance.isInitialized) {
                instance = FileUtilImpl()
            }
            return instance
        }
    }
}