package com.example.softwareupdate.adapters.version

import android.os.Parcel
import android.os.Parcelable

data class VersionsEntity(
    val versionResId: Int,
    val versionTitle: String?,
    val versionName: String?,
    val versionOverview: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(versionResId)
        parcel.writeString(versionTitle)
        parcel.writeString(versionName)
        parcel.writeString(versionOverview)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VersionsEntity> {
        override fun createFromParcel(parcel: Parcel): VersionsEntity {
            return VersionsEntity(parcel)
        }

        override fun newArray(size: Int): Array<VersionsEntity?> {
            return arrayOfNulls(size)
        }
    }
}