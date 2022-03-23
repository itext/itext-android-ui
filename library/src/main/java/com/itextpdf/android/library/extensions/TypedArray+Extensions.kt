package com.itextpdf.android.library.extensions

import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import androidx.core.content.res.getBooleanOrThrow
import androidx.core.content.res.getIntegerOrThrow

fun TypedArray.getTextIfAvailable(@StyleableRes index: Int, block: (text: CharSequence) -> Unit) {
    if (hasValue(index)) {
        block.invoke(getText(index))
    }
}

fun TypedArray.getBooleanIfAvailable(@StyleableRes index: Int, block: (value: Boolean) -> Unit) {
    if (hasValue(index)) {
        block.invoke(getBooleanOrThrow(index))
    }
}

fun TypedArray.getIntegerIfAvailable(@StyleableRes index: Int, block: (value: Int) -> Unit) {
    if (hasValue(index)) {
        block.invoke(getIntegerOrThrow(index))
    }
}