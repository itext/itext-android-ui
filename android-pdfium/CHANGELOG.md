## 1.9.0 (2018-06-29)
* Updated Pdfium library to 7.1.2_r36
* Changed `gnustl_static` to `c++_shared`
* Update Gradle plugins
* Update compile SDK and support library to 26
* Change minimum SDK to 14
* Add support for mips64

## 1.8.2 (2017-12-15)
* Merge pull request by [mcsong](https://github.com/mcsong) fixing potential NPE when getting links

## 1.8.1 (2017-11-15)
* Handle `PdfiumCore#getPageSize()` errors and return `Size(0, 0)`

## 1.8.0 (2017-11-11)
* Add method for reading links from given page
* Add method for mapping page coordinates to screen coordinates
* Add `PdfiumCore#getPageSize(...)` method, which does not require page to be opened
* Add `Size` and `SizeF` utility classes
* Add javadoc comments to `PdfiumCore`

## 1.7.1 (2017-10-28)
* Merge pull request by [Phaestion](https://github.com/Phaestion) which prevents `UnsatisfiedLinkError`

## 1.7.0 (2017-06-21)
* Add rendering bitmap in RGB 565 format, which reduces memory usage (about twice)

## 1.6.1 (2017-06-09)
* Fix bug from 1.6.0 - not embedded fonts was not rendered

## 1.6.0 (2017-03-22)
* Pdfium updated to newest version, from Android 7.1.1.
It should fix many rendering issues and (thanks to freetype support) fix problems with fonts.

## 1.5.0 (2016-11-18)
* Add method `PdfiumCore#newDocument(byte[])` for reading PDF documents from memory
* Cleanup AndroidManifest.xml to solve problems with manifest merger

## 1.4.0 (2016-07-12)
* merge pull request by [usef](https://github.com/usef) with added support for rendering annotations. Due to limitations of _Pdfium_, messages from comments cannot be read and are rendered only as speech balloons.

## 1.3.1 (2016-07-11)
* `PdfiumCore#newDocument()` may throw `PdfPasswordException` to help with recognition of password requirement or incorrect password.

## 1.3.0 (2016-07-10)
* added support for opening documents with password
* fixed bug with SIGSEV when closing document

## 1.2.0 (2016-07-06)
* `libmodpdfium` compiled with methods for retrieving bookmarks and metadata
* added `PdfiumCore#getDocumentMeta()` for retrieving document metadata
* added `PdfiumCore#getTableOfContents()` for reading whole tree of bookmarks
* comment out native rendering debug

## 1.1.0 (2016-06-27)
* fixed rendering multiple PDFs at the same time thanks to synchronization between instances
* compile Pdfium with SONAME `libmodpdfium` to prevent loading `libpdfium` from Lollipop and higher
* `newDocument()` requires `ParcelFileDescriptor` instead of `FileDescriptor` to keep file descriptor open
* changed method of loading PDFs, which should be more stable

## 1.0.3 (2016-06-17)
* probably fixed bug when pdf should open as normal but was throwing exception
* added much more descriptive exception messages

## 1.0.2 (2016-06-04)
* `newDocument()` throws IOException

## 1.0.1 (2016-06-04)
* fix loading `libpdfium` on devices with < Lollipop

## Initial changes
* Added method for rendering PDF page on bitmap

    ``` java
    void renderPageBitmap(PdfDocument doc, Bitmap bitmap, int pageIndex,
                          int startX, int startY, int drawSizeX, int drawSizeY);
    ```
* Added methods to get width and height of page in points (1/72") (like in `PdfRenderer.Page` class):
    * `int getPageWidthPoint(PdfDocument doc, int index);`
    * `int getPageHeightPoint(PdfDocument doc, int index);`
