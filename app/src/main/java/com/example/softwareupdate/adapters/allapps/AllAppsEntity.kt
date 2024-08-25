package com.example.softwareupdate.adapters.allapps

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import java.io.ByteArrayOutputStream

data class AllAppsEntity(
    val appName: String? = "",
    val pName: String? = "",
    val versionName: String? = "",
    val versionCode: Int = 0,
    val icon: Drawable?,
    val installationDate: String?,
    var isSelected: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        getDrawableFromBase64(parcel.readString()),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appName)
        parcel.writeString(pName)
        parcel.writeString(versionName)
        parcel.writeInt(versionCode)
        parcel.writeString(getBase64FromDrawable(icon))
        parcel.writeString(installationDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AllAppsEntity> {
        override fun createFromParcel(parcel: Parcel): AllAppsEntity {
            return AllAppsEntity(parcel)
        }

        override fun newArray(size: Int): Array<AllAppsEntity?> {
            return arrayOfNulls(size)
        }

        private fun getDrawableFromBase64(base64: String?): Drawable? {
            if (base64.isNullOrBlank()) {
                return null
            }
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = Bitmap.createBitmap(
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, null)
            )
            return BitmapDrawable(Resources.getSystem(), bitmap)
        }

        private fun getBase64FromDrawable(drawable: Drawable?): String? {
            if (drawable == null) {
                return null
            }
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (drawable) {
                    is BitmapDrawable -> drawable.bitmap
                    is AdaptiveIconDrawable -> {
                        val width = drawable.intrinsicWidth
                        val height = drawable.intrinsicHeight
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bitmap
                    }

                    else -> null
                }
            } else {
                if (drawable is BitmapDrawable) {
                    drawable.bitmap
                } else {
                    null
                }
            }
            return bitmap?.let {
                val byteArrayOutputStream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        }
    }
}
