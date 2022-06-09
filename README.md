# Table of Contents
1. [Setup](#setup)
   1. [Toubleshooting](#troubleshooting)
2. [Using the SDK](#third-example)
   1. [Create PDFFragment via code](#create-pdffragment-via-code)
   2. [Inflate PDFFragment via XML](#inflate-pdffragment-via-xml)
   3. [Split PDF](#split-pdf)
   4. [Manipulate PDF directly (without UI)](#manipulate-pdf-directly-without-ui)
   5. [Receiving results](#receiving-pdf-results)

# Setup

The SDK depends on forked versions of [PdfiumAndroid](https://github.com/itext/PdfiumAndroid) and 
[AndroidPdfViewer](https://github.com/itext/AndroidPdfViewer) whose artifacts are stored on 
[iText Artifactory](https://repo.itextsupport.com/ui/repos/tree/General/android/com/itextpdf/android). Artifacts will
be loaded during the SDK build by gradle.

If you want to build SDK with custom version of PdfiumAndroid or\and AndroidPdfViewer you need to clone repositories, 
make necessary changes and then publish them to local maven storage. So SDK will use custom local artifacts as dependencies.

## Troubleshooting

### No whitespaces in your paths
Do not use spaces in your local paths/directories, as it can lead to all sorts of errors related to Android NDK, ndk-build or cmake.

### NDK setup
If you are having problems related to Android NDK, make sure to follow the correct setup procedure:
https://developer.android.com/studio/projects/install-ndk


# Using the SDK

The SDK provides different Fragments and classes to manipulate PDF files.

## Create PDFFragment via code

You can create a new instance of PdfFragment via code:

```kotlin
private fun showPdfFragment(pdfUri: Uri) {
    
    // See PdfConfig for all available customization options
    val config = PdfConfig(pdfUri = pdfUri, showScrollIndicator = true)
    
    // Create PdfFragment
    val pdfFragment = PdfFragment.newInstance(pdfConfig)

    // show fragment, e.g. via supportFragmentManager

}
```

## Inflate PDFFragment via XML

You can also inflate a PDF fragment via XML. In thise case, the fragment is customizable via different styleables, such as app:enable_split_view, etc.

```xml

    <!-- The PDF fragment can be configured in XML via different styleables, such as app:enable_split_view, etc. -->
    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/pdf_fragment_container"
        android:name="com.itextpdf.android.library.fragments.PdfFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:background_color="#F4EFBB"
        app:display_file_name="true"
        app:file_name="sample_1.pdf"
        app:file_uri="file:///storage/emulated/0/Android/data/com.itextpdf.android.app/cache/sample_1_copy.pdf"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:enable_split_view="false"
        app:page_spacing="400"
        app:primary_color="#217C12"
        app:secondary_color="#C6E6C1" />
```

## Split PDF via Fragment

You can directly launch fragment to split PDF documents via...

```kotlin
private fun showSplitFragment(pdfUri: Uri) {

    val config = PdfConfig(pdfUri = pdfUri, showScrollIndicator = true)
    val splitFragment = SplitDocumentFragment.newInstance(config)
    // show fragment, e.g. via supportFragmentManager
}
```

## Manipulate PDF directly (without UI)

You can directly manipulate PDF files without showing a Fragment-UI by using the PDFManipulator:

```kotlin
val manipulator = PdfManipulator.create(requireContext(), pdfUri)

manipulator.addTextAnnotationToPdf(...)
manipulator.splitPdfWithSelection(...) 
manipulator.addMarkupAnnotationToPdf(...) 
// etc...
```

## Receiving PDF results

You can receive fragment results by registering a fragment result listener to your fragmentManager.

```kotlin
private fun listenForPdfFragmentResult(fragmentManager: FragmentManager) {
    
    fragmentManager.setFragmentResultListener(PdfFragment.REQUEST_KEY, this) { requestKey: String, bundle: Bundle ->

        // Retrieve fragment result from bundle
        val result: PdfResult? = bundle.getParcelable(PdfFragment.RESULT_FILE)

        when (result) {
            is PdfResult.CancelledByUser -> // ...
            is PdfResult.PdfEdited -> // ...
            is PdfResult.PdfSplit -> // ...
            is PdfResult.NoChanges -> // ...
            null -> // ...
        }
    }
}
```