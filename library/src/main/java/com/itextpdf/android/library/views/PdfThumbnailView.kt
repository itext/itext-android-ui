package com.itextpdf.android.library.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.android.library.R
import java.io.File

/**
 * View that easily allows to display a thumbnail for a pdf file by setting it as file or uri.
 *
 * @param context   the context
 * @param attrs     the attributes for the view
 */
open class PdfThumbnailView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val pdfThumbnailView: PDFView

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_pdf_thumbnail, this, true)

//        val a = context.obtainStyledAttributes(
//            attrs,
//            R.styleable.TextInputView, 0, 0
//        )

        pdfThumbnailView = findViewById(R.id.pdfThumbnailView)

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
            pdfThumbnailView.fromFile(file).pages(pageIndex).load()
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
            pdfThumbnailView.fromUri(uri).pages(pageIndex).load()
        }
    }
}