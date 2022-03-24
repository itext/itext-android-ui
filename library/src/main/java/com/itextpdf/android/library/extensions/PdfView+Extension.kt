package com.itextpdf.android.library.extensions

import android.graphics.PointF
import android.view.MotionEvent
import com.github.barteksc.pdfviewer.PDFView
import com.shockwave.pdfium.util.SizeF

fun PDFView.convertScreenPointToPdfPagePoint(e: MotionEvent): PointF? {
    val x = e.x
    val y = e.y

    if (pdfFile == null) return null
    val mappedX = -currentXOffset + x
    val mappedY = -currentYOffset + y
    val page =
        pdfFile.getPageAtOffset(if (isSwipeVertical) mappedY else mappedX, zoom)
    val pageSize: SizeF = pdfFile.getScaledPageSize(page, zoom)
    val pageX: Int
    val pageY: Int
    if (isSwipeVertical) {
        pageX = pdfFile.getSecondaryPageOffset(page, zoom).toInt()
        pageY = pdfFile.getPageOffset(page, zoom).toInt()
    } else {
        pageY = pdfFile.getSecondaryPageOffset(page, zoom).toInt()
        pageX = pdfFile.getPageOffset(page, zoom).toInt()
    }
    return pdfFile.mapDeviceCoordsToPage(
        page,
        pageX,
        pageY,
        pageSize.width.toInt(),
        pageSize.height.toInt(),
        0, //TODO: use real rotation
        mappedX.toInt(),
        mappedY.toInt()
    )
}