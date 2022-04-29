package com.itextpdf.android.library.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream

internal object ImageUtil {

    fun getResourceAsBitmap(context: Context, @DrawableRes resId: Int, imageSize: Int, @ColorInt tintColor: Int): Bitmap? {
        val d = AppCompatResources.getDrawable(
            context,
            resId
        )?.constantState?.newDrawable()?.mutate()
        if (d != null) {
            val wrappedDrawable = DrawableCompat.wrap(d)
            DrawableCompat.setTint(wrappedDrawable, tintColor)

            return d.toBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
        }
        return null
    }

    fun getResourceAsByteArray(context: Context, @DrawableRes resId: Int, imageSize: Int, @ColorInt tintColor: Int): ByteArray? {
        val bitmap = getResourceAsBitmap(context, resId, imageSize, tintColor)
        return if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        } else {
            null
        }
    }
}