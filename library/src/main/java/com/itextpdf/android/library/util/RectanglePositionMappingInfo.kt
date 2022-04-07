package com.itextpdf.android.library.util

import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.android.library.extensions.convertScreenRectToPdfPageRect
import com.itextpdf.android.library.extensions.getPageIndexAtScreenPoint
import com.itextpdf.kernel.geom.Rectangle

/**
 * Class that contains mapping-information regarding the initiating [screenRectangle] (in device-coordinates) and the corresponding [pdfRectangle] and [pdfPageIndex].
 *
 */
internal data class RectanglePositionMappingInfo(

    /**
     * The rectangle in the pdf coordinate system related to the [screenRectangle]
     */
    val pdfRectangle: Rectangle,

    /**
     * The corresponding (zero-based) pdf-page index where the [screenRectangle] occurred.
     */
    val pdfPageIndex: Int,

    /**
     * The initiating rectangle in screen coordinate system.
     */
    val screenRectangle: Rectangle
) {

    companion object Factory {
        fun createOrNull(screenRectangle: Rectangle, pdfView: PDFView): RectanglePositionMappingInfo? {

            // calculate the page index based on the top left corner of the rect
            val pdfPageIndex: Int = pdfView.getPageIndexAtScreenPoint(screenRectangle.x, screenRectangle.y) ?: return null
            val pdfRect = pdfView.convertScreenRectToPdfPageRect(screenRectangle) ?: return null

            return RectanglePositionMappingInfo(pdfRectangle = pdfRect, pdfPageIndex = pdfPageIndex, screenRectangle = screenRectangle)
        }
    }
}