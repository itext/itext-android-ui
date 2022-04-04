package com.itextpdf.android.library.fragments

import android.graphics.Color
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

/**
 * @param pdfUri The uri of the pdf that should be displayed. This is the only required param
 * @param fileName The name of the file that should be displayed
 * @param displayFileName A boolean flag that defines if the given file name should be displayed in the toolbar. Default: false
 * @param pageSpacing The spacing in px between the pdf pages. Default: 20
 * @param enableThumbnailNavigationView A boolean flag to enable/disable pdf thumbnail navigation view. Default: true
 * @param enableSplitView A boolean flag to enable/disable pdf split view. Default: true
 * @param enableAnnotationRendering A boolean flag to enable/disable annotation rendering. Default: true
 * @param enableDoubleTapZoom A boolean flag to enable/disable double tap to zoom. Default: true
 * @param showScrollIndicator A boolean flag to enable/disable a scrolling indicator at the right of the page, that can be used fast scrolling. Default: true
 * @param showScrollIndicatorPageNumber A boolean flag to enable/disable the page number while the scroll indicator is tabbed. Default: true
 * @param primaryColor A color string to set the primary color of the view (affects: scroll indicator, navigation thumbnails and loading indicator). Default: #FF9400
 * @param secondaryColor A color string to set the secondary color of the view (affects: scroll indicator and navigation thumbnails). Default: #FFEFD8
 * @param backgroundColor A color string to set the background of the pdf view that will be visible between the pages if pageSpacing > 0. Default: #EAEAEA@
 * @param enableHelpDialog A boolean flag to enable/disable the help dialog on the split view
 * @param helpDialogTitle The title of the help dialog on the split view. If this is null but help dialog is displayed, a default title is used.
 * @param helpDialogText The text of the help dialog on the split view. If this is null but help dialog is displayed, a default text is used.
 */
@Parcelize
data class PdfConfig(
    val pdfUri: Uri,
    val fileName: String? = FILE_NAME,
    val displayFileName: Boolean = DISPLAY_FILE_NAME,
    val pageSpacing: Int = PAGE_SPACING,
    val enableThumbnailNavigationView: Boolean = ENABLE_THUMBNAIL_NAVIGATION_VIEW,
    val enableSplitView: Boolean = ENABLE_SPLITVIEW,
    val enableAnnotationRendering: Boolean = ENABLE_ANNOTATION_RENDERING,
    val enableDoubleTapZoom: Boolean = ENABLE_DOUBLE_TAP_ZOOM,
    val showScrollIndicator: Boolean = SHOW_SCROLL_INDICATOR,
    val showScrollIndicatorPageNumber: Boolean = SHOW_SCROLL_INDICATOR_PAGE_NUMBER,
    val primaryColor: String = PRIMARY_COLOR,
    val secondaryColor: String = SECONDARY_COLOR,
    val backgroundColor: String = BACKGROUND_COLOR,
    val enableHelpDialog: Boolean = ENABLE_HELP_DIALOG,
    val helpDialogTitle: String? = HELP_DIALOG_TITLE,
    val helpDialogText: String? = HELP_DIALOG_TEXT,
    val enableHighlightView: Boolean = ENABLE_HIGHLIGHT_VIEW,
    val enableAnnotationView: Boolean = ENABLE_ANNOTATION_VIEW
) : Parcelable {

    private constructor(builder: Builder) : this(
        pdfUri = builder.pdfUri ?: throw IllegalArgumentException("PDF uri not set. Make sure to specify PDF uri."),
        fileName = builder.fileName,
        displayFileName = builder.displayFileName,
        pageSpacing = builder.pageSpacing,
        enableThumbnailNavigationView = builder.enableThumbnailNavigationView,
        enableSplitView = builder.enableSplitView,
        enableAnnotationRendering = builder.enableAnnotationRendering,
        enableDoubleTapZoom = builder.enableDoubleTapZoom,
        showScrollIndicator = builder.showScrollIndicator,
        showScrollIndicatorPageNumber = builder.showScrollIndicatorPageNumber,
        primaryColor = builder.primaryColor,
        secondaryColor = builder.secondaryColor,
        backgroundColor = builder.backgroundColor,
        enableHelpDialog = builder.enableHelpDialog,
        helpDialogTitle = builder.helpDialogTitle,
        helpDialogText = builder.helpDialogText,
        enableHighlightView = builder.enableHighlightView,
        enableAnnotationView = builder.enableAnnotationView
    )

    @ColorInt
    fun getPrimaryColorInt(): Int {
        return Color.parseColor(primaryColor)
    }

    companion object {

        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()

        private val FILE_NAME: String? = null
        private const val PAGE_SPACING = 10
        private const val PRIMARY_COLOR = "#FF9400"
        private const val SECONDARY_COLOR = "#FFEFD8"
        private const val BACKGROUND_COLOR = "#EAEAEA"
        private const val DISPLAY_FILE_NAME = false
        private const val ENABLE_THUMBNAIL_NAVIGATION_VIEW = true
        private const val ENABLE_SPLITVIEW = true
        private const val ENABLE_ANNOTATION_RENDERING = true
        private const val ENABLE_DOUBLE_TAP_ZOOM = true
        private const val SHOW_SCROLL_INDICATOR = true
        private const val SHOW_SCROLL_INDICATOR_PAGE_NUMBER = true
        private const val ENABLE_HELP_DIALOG: Boolean = true
        private val HELP_DIALOG_TITLE: String? = null
        private val HELP_DIALOG_TEXT: String? = null
        private const val ENABLE_HIGHLIGHT_VIEW: Boolean = true
        private const val ENABLE_ANNOTATION_VIEW: Boolean = true

    }

    class Builder {

        var pdfUri: Uri? = null
        var fileName: String? = FILE_NAME
        var displayFileName: Boolean = DISPLAY_FILE_NAME
        var pageSpacing: Int = PAGE_SPACING
        var enableThumbnailNavigationView: Boolean = ENABLE_THUMBNAIL_NAVIGATION_VIEW
        var enableSplitView: Boolean = ENABLE_SPLITVIEW
        var enableAnnotationRendering: Boolean = ENABLE_ANNOTATION_RENDERING
        var enableDoubleTapZoom: Boolean = ENABLE_DOUBLE_TAP_ZOOM
        var showScrollIndicator: Boolean = SHOW_SCROLL_INDICATOR
        var showScrollIndicatorPageNumber: Boolean = SHOW_SCROLL_INDICATOR_PAGE_NUMBER
        var primaryColor: String = PRIMARY_COLOR
        var secondaryColor: String = SECONDARY_COLOR
        var backgroundColor: String = BACKGROUND_COLOR
        var enableHelpDialog: Boolean = ENABLE_HELP_DIALOG
        var helpDialogTitle: String? = HELP_DIALOG_TITLE
        var helpDialogText: String? = HELP_DIALOG_TEXT
        var enableHighlightView: Boolean = ENABLE_HIGHLIGHT_VIEW
        var enableAnnotationView: Boolean = ENABLE_ANNOTATION_VIEW

        fun build() = PdfConfig(this)

    }

}