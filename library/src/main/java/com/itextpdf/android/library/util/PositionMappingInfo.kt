package com.itextpdf.android.library.util

import android.graphics.PointF
import android.view.MotionEvent
import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.android.library.extensions.convertMotionEventPointToPdfPagePoint
import com.itextpdf.android.library.extensions.getPageNumberForClickPosition

/**
 * Class that contains mapping-information regarding the initiating [motionEvent] (in device-coordinates) and the corresponding [pdfCoordinates] and [pdfPageNumber].
 *
 */
data class PositionMappingInfo(

    /**
     * The pdf-coordinates of the related [motionEvent]
     */
    val pdfCoordinates: PointF,

    /**
     * The corresponding pdf-page number where the [motionEvent] occurred.
     */
    val pdfPageNumber: Int,

    /**
     * The initiating motion-event.
     */
    val motionEvent: MotionEvent
) {

    companion object Factory {
        fun createOrNull(event: MotionEvent, pdfView: PDFView): PositionMappingInfo? {

            val pdfCoordinates: PointF = pdfView.convertMotionEventPointToPdfPagePoint(event) ?: return null
            val pdfPageNumber: Int = pdfView.getPageNumberForClickPosition(event) ?: return null

            return PositionMappingInfo(pdfCoordinates = pdfCoordinates, motionEvent = event, pdfPageNumber = pdfPageNumber)
        }
    }

}