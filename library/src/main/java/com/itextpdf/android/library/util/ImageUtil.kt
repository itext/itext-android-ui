package com.itextpdf.android.library.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream

object ImageUtil {

    fun getResourceAsByteArray(context: Context, @DrawableRes resId: Int, imageSize: Int, tintColor: String): ByteArray? {
        val d = AppCompatResources.getDrawable(
            context,
            resId
        )?.constantState?.newDrawable()?.mutate()
        if (d != null) {
            val wrappedDrawable = DrawableCompat.wrap(d)
            DrawableCompat.setTint(
                wrappedDrawable,
                Color.parseColor(tintColor)
            )

            val bitmap = d.toBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }
        return null
    }
}