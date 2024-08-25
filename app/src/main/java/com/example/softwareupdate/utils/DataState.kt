package com.example.softwareupdate.utils

/** This class will maintain my Data state in Whole app */
sealed class DataState<out R> {

    data class Success<out T>(val data: T) : DataState<T>()
    data class Error(val exception: String) : DataState<Nothing>()
    data class General<out T>(val data: T) : DataState<T>()
    object Loading : DataState<Nothing>()

}
