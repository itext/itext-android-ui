package com.itextpdf.android.library.paging

class Page<T>(
    val content: List<T>,
    val size: Int,
    val number: Int,
    val totalPages: Int
)