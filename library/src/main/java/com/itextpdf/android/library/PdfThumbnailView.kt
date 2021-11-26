package com.itextpdf.android.library

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import java.io.File


open class PdfThumbnailView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val thumbnailImageView: ImageView

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_pdf_thumbnail, this, true)

//        val a = context.obtainStyledAttributes(
//            attrs,
//            R.styleable.TextInputView, 0, 0
//        )

        thumbnailImageView = findViewById(R.id.imageViewThumbnail)

//        a.recycle() // recycle for re-use (required)
    }

    fun set(file: File, index: Int = 0) {

        post {
            // create a new renderer
            val renderer =
                PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))

            val page = renderer.openPage(index)

            val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            // say we render for showing on the screen
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            // close the renderer
            renderer.close()

            thumbnailImageView.setImageBitmap(bitmap)
        }
    }
}