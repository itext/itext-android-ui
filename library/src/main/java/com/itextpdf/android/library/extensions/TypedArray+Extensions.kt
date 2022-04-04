package com.itextpdf.android.library.extensions

import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import androidx.core.content.res.getBooleanOrThrow
import androidx.core.content.res.getIntegerOrThrow

internal fun TypedArray.getTextIfAvailable(@StyleableRes index: Int, block: (text: CharSequence) -> Unit) {
    if (hasValue(index)) {
        block.invoke(getText(index))
    }
}

internal fun TypedArray.getBooleanIfAvailable(@StyleableRes index: Int, block: (value: Boolean) -> Unit) {
    if (hasValue(index)) {
        block.invoke(getBooleanOrThrow(index))
    }
}

internal fun TypedArray.getIntegerIfAvailable(@StyleableRes index: Int, block: (value: Int) -> Unit) {
    if (hasValue(index)) {
        block.invoke(getIntegerOrThrow(index))
    }
}