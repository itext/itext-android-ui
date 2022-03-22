package com.itextpdf.android.library.util

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import java.io.*

class FileUtilImpl : FileUtil {

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
    override fun loadFileFromAssets(context: Context, fileName: String): File {
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
    private fun copyAsset(
        assetManager: AssetManager,
        fileName: String,
        toPath: String?
    ): Boolean {
        var inputStream: InputStream?
        var outputStream: OutputStream?
        return try {
            inputStream = assetManager.open(fileName)
            File(toPath).createNewFile()
            outputStream = FileOutputStream(toPath)
            copyFile(inputStream, outputStream)
            inputStream.close()
            inputStream = null
            outputStream.flush()
            outputStream.close()
            outputStream = null
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
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    override fun overrideFile(fileToSave: File, destinationUri: Uri): File {
        val existingFile = File(destinationUri.path)
        FileOutputStream(existingFile, false).use { overWrite ->
            overWrite.write(fileToSave.readBytes())
            overWrite.flush()
        }
        return existingFile
    }

    override fun createTempCopy(context: Context, originalFile: File): File {
        val storageFolderPath =
            (context.externalCacheDir ?: context.cacheDir).absolutePath
        return File("$storageFolderPath/temp_${originalFile.name}")
    }
}