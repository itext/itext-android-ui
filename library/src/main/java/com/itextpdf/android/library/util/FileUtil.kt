package com.itextpdf.android.library.util

import android.content.Context
import android.content.res.AssetManager
import java.io.*

class FileUtil {
    companion object {

        /**
         * Loads a file with the given fileName from the assets folder to a location the app can access and
         * returns the absolute path to that file if the operation was successful or throws an IOException
         * if something went wrong.
         *
         * @param fileName  the name of the file that should be loaded from the assets folder
         * @return          the file
         * @throws IOException
         */
        @Throws(IOException::class)
        fun loadFileFromAssets(context: Context, fileName: String): File {
            // create file object to read and write on in the cache directory of the app
            val file = File(context.cacheDir, fileName)
            if (!file.exists()) {
                val assetManager: AssetManager = context.assets
                // copy pdf file from assets to location of the previously created file
                copyAsset(assetManager, fileName, file.absolutePath)
            }
            return file
        }

        /**
         * Utility function to copy a file from the assets folder to a provided path
         *
         * @param assetManager  the assetManager that is required to open files from the assets
         * @param fileName      the name of the file that should be copied from assets to the given path
         * @param toPath        the path where the file from the assets should be copied to
         * @return  true if the operation was successful, false if not
         */
        fun copyAsset(
            assetManager: AssetManager,
            fileName: String,
            toPath: String?
        ): Boolean {
            var `in`: InputStream?
            var out: OutputStream?
            return try {
                `in` = assetManager.open(fileName)
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

        /**
         * Copies a file from input to output stream
         *
         * @param in    the input stream
         * @param out   the output stream
         * @throws IOException
         */
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