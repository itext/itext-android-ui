package com.itextpdf.android.app.util

import android.content.res.AssetManager
import java.io.*
import java.lang.Exception

class FileUtil {
    companion object {
        fun copyAsset(
            assetManager: AssetManager,
            fromAssetPath: String?,
            toPath: String?
        ): Boolean {
            var `in`: InputStream?
            var out: OutputStream?
            return try {
                `in` = assetManager.open(fromAssetPath!!)
                File(toPath).createNewFile()
                out = FileOutputStream(toPath)
                copyFile(`in`, out)
                `in`.close()
                `in` = null
                out.flush()
                out.close()
                out = null
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        @Throws(IOException::class)
        fun copyFile(`in`: InputStream, out: OutputStream) {
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
        }
    }
}