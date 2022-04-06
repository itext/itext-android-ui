package com.itextpdf.android.library.fragments

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * The result that is returned by e.g. [PdfFragment] or [SplitDocumentFragment].
 */
sealed class PdfResult : Parcelable {

    /**
     * Determines that the action has been cancelled by the user.
     */
    @Parcelize
    object CancelledByUser : PdfResult()

    /**
     * Determines that the user has edited the PDF file and the result is stored in the given [file].
     *
     * @property file The file where the edited PDF is stored.
     */
    @Parcelize
    class PdfEdited(val file: File) : PdfResult()

    /**
     * The user has split the given PDF file into two parts.
     * The user-selected pages have been stored to a new PDF file at [fileContainingSelectedPages].
     * The unselected pages have been stored to a new PDF file at [fileContainingUnselectedPages].
     *
     * @property fileContainingSelectedPages File that contains the selected pages. Will contain all pages of the original PDF if user did not select any page.
     * @property fileContainingUnselectedPages This will be null if user selected all or no pages.
     */
    @Parcelize
    class PdfSplit(
        val fileContainingSelectedPages: Uri,
        val fileContainingUnselectedPages: Uri?
    ) : PdfResult()
}