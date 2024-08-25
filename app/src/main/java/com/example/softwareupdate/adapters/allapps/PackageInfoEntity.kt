package com.example.softwareupdate.adapters.allapps

import android.graphics.drawable.Drawable

data class PackageInfoEntity(
    val appName: String = "",
    val pName: String = "",
    val versionName: String = "",
    val versionCode: Int = 0,
    val icon: Drawable?,
    val installationDate: String?,
)
