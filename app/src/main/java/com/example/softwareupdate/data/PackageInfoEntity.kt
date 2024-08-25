package com.example.softwareupdate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packageInfo_table")
data class PackageInfoEntity(

    @PrimaryKey(autoGenerate = false)
    val appName: String = "",
    val pName: String = "",
    val versionName: String = "",
    val versionCode: Int = 0,
    val iconByteArray: ByteArray,
    val installationDate: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PackageInfoEntity
        if (appName != other.appName) return false
        if (pName != other.pName) return false
        if (versionName != other.versionName) return false
        if (versionCode != other.versionCode) return false
        if (!iconByteArray.contentEquals(other.iconByteArray)) return false
        if (installationDate != other.installationDate) return false
        return true
    }

    override fun hashCode(): Int {
        var result = appName.hashCode()
        result = 31 * result + pName.hashCode()
        result = 31 * result + versionName.hashCode()
        result = 31 * result + versionCode
        result = 31 * result + iconByteArray.contentHashCode()
        result = 31 * result + (installationDate?.hashCode() ?: 0)
        return result
    }
}