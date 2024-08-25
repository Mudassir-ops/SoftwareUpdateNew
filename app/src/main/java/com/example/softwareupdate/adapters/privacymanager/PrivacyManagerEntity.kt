package com.example.softwareupdate.adapters.privacymanager

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

data class PrivacyManagerEntity(
    val appName: String? = "",
    var pName: String? = "",
    val versionName: String? = "",
    val versionCode: Int = 0,
    val icon: Drawable?,
    val installationDate: String?,
    var isSelected: Boolean = false,
    var protectionValue: Int? = 0,
    val permissionList: ArrayList<AppPermissions>? = arrayListOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        getDrawableFromBase64(parcel.readString()),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.createTypedArrayList(AppPermissions.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appName)
        parcel.writeString(pName)
        parcel.writeString(versionName)
        parcel.writeInt(versionCode)
        parcel.writeString(getBase64FromDrawable(icon))
        parcel.writeString(installationDate)
        parcel.writeByte(if (isSelected) 1 else 0)
        parcel.writeValue(protectionValue)
        parcel.writeList(permissionList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PrivacyManagerEntity> {
        override fun createFromParcel(parcel: Parcel): PrivacyManagerEntity {
            return PrivacyManagerEntity(parcel)
        }

        override fun newArray(size: Int): Array<PrivacyManagerEntity?> {
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

        fun createListFromParcel(parcel: Parcel): ArrayList<PrivacyManagerEntity>? {
            val list = arrayListOf<PrivacyManagerEntity>()
            parcel.readTypedList(list, CREATOR)
            return list
        }

        fun List<PrivacyManagerEntity>.writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeTypedList(this)
        }

    }
}

data class AppPermissions(
    var permissionName: String? = "",
    var permissionDetail: String? = "",
    var isDangerous: Boolean? = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(permissionName)
        parcel.writeString(permissionDetail)
        parcel.writeValue(isDangerous)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppPermissions> {
        override fun createFromParcel(parcel: Parcel): AppPermissions {
            return AppPermissions(parcel)
        }

        override fun newArray(size: Int): Array<AppPermissions?> {
            return arrayOfNulls(size)
        }
    }
}
