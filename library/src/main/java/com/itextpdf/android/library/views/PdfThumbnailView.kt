package com.itextpdf.android.library.views

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.itextpdf.android.library.R
import com.shockwave.pdfium.PdfiumCore
import java.io.File


/**
 * View that easily allows to display a thumbnail for a pdf file by setting it as file or uri.
 *
 * @param context   the context
 * @param attrs     the attributes for the view
 */
open class PdfThumbnailView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val pdfImageView: ImageView

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_pdf_thumbnail, this, true)

//        val a = context.obtainStyledAttributes(
//            attrs,
//            R.styleable.TextInputView, 0, 0
//        )

        pdfImageView = findViewById(R.id.pdfThumbnailView)

//        a.recycle() // recycle for re-use (required)
    }

    /**
     * Sets a pdf file as the source of this thumbnail view that is rendered and displayed. By setting
     * a pageIndex, it is possible to define which page of the pdf file should be used for the thumbnail.
     *
     * @param file      the pdf file
     * @param pageIndex the index of the page that should be used as thumbnail. default: 0
     */
    fun set(file: File, pageIndex: Int = 0) {

        post {
            val fileDescriptor: ParcelFileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            setImageViewWithFileDescriptor(fileDescriptor, pageIndex)
        }
    }

    /**
     * Sets an uri of a pdf file as the source of this thumbnail view that is rendered and displayed. By setting
     * a pageIndex, it is possible to define which page of the pdf file should be used for the thumbnail.
     *
     * @param uri      the uri of the pdf file
     * @param pageIndex the index of the page that should be used as thumbnail. default: 0
     */
    fun set(uri: Uri, pageIndex: Int = 0) {

        post {
            val fileDescriptor: ParcelFileDescriptor? =
                context.contentResolver.openFileDescriptor(uri, "r")
            fileDescriptor?.let {
                setImageViewWithFileDescriptor(fileDescriptor, pageIndex)
            }
        }
    }

    private fun setImageViewWithFileDescriptor(
        fileDescriptor: ParcelFileDescriptor,
        pageIndex: Int
    ) {
        val pdfiumCore = PdfiumCore(context)
        try {
            val pdfDocument = pdfiumCore.newDocument(fileDescriptor)
            pdfiumCore.openPage(pdfDocument, pageIndex)

            val width = pdfiumCore.getPageWidthPoint(pdfDocument, pageIndex)
            val height = pdfiumCore.getPageHeightPoint(pdfDocument, pageIndex)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageIndex, 0, 0, width, height, true)

            pdfImageView.setImageBitmap(bitmap)
            pdfiumCore.closeDocument(pdfDocument)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
