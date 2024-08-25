package com.example.softwareupdate.ui.fragments.deviceInfo

data class DeviceInfo(
    val deviceName: String,
    val deviceModel: String,
    val deviceBrand: String,
    val softwareId: String,
    val deviceVersion:String,
    val availableStorage: String,
    val deviceRam :String
)