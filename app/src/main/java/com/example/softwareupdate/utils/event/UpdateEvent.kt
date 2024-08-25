package com.example.softwareupdate.utils.event

data class UpdateEvent(
    var count: Int,
    var currentAppCheckIn: Int,
    var totalAppSize: Int,
    var isAllAppCheckFinished: Boolean
)