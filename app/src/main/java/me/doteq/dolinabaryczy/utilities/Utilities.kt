package me.doteq.dolinabaryczy.utilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import me.doteq.dolinabaryczy.R
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object Utilities {

    //TODO: Wydaje mi się że tu można użyć daggera do resources
    fun isDarkThemeOn(resources: Resources): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    fun hasLocationPermissions(context: Context) =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    fun Float.toDistanceString(): String {
        val df = DecimalFormat("#.#")
        return if (this > 1000) "${df.format(this / 1000)}km"
        else "${this.roundToInt()}m"
    }

    fun Array<Double>.circularAverage(): Double = atan2(
        this.sumOf { sin(it) } / this.size,
        this.sumOf { cos(it) } / this.size
    )

    @DrawableRes
    fun getPointDrawable(id: String): Int? {
        return when (id) {
            "1" -> R.drawable.pic_1
            "2" -> R.drawable.pic_2
            "3" -> R.drawable.pic_3
            "4" -> R.drawable.pic_4
            "5" -> R.drawable.pic_5
            "6" -> R.drawable.pic_6
            "7" -> R.drawable.pic_7
            "8" -> R.drawable.pic_8
            "9" -> R.drawable.pic_9
            else -> null
        }
    }
}
