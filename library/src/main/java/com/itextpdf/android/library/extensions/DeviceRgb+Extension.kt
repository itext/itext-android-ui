package com.itextpdf.android.library.extensions

import com.itextpdf.kernel.colors.DeviceRgb

fun DeviceRgb.getHexString(): String {
    return String.format(
        "#%02x%02x%02x",
        (colorValue[0] * 255).toInt(),
        (colorValue[1] * 255).toInt(),
        (colorValue[2] * 255).toInt()
    )
}