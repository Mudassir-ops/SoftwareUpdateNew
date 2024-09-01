package com.example.softwareupdate.utils

import android.util.Log

class Logs {

    private val instance: Logs? = null

    companion object {
        val STACK_TRACE_LEVELS_UP_LINE = 3
        val STACK_TRACE_LEVELS_UP_CLASS = 3
        val testing: Boolean = true
        var stopLogging: Boolean = false
    }

    @Synchronized
    fun setStopLogging(stopLogging: Boolean) {
        Logs.stopLogging = stopLogging
    }

    fun createCompleteLog(tag: String, message: String) {
        val maxLogSize = 1000
        for (i in 0..message.length / maxLogSize) {
            val start = i * maxLogSize
            val end = if ((i + 1) * maxLogSize > message.length) message.length else (i + 1) * maxLogSize
            createLog(tag, message.substring(start, end))
        }
    }

    fun getInstance(): Logs? {
        if (Logs().instance == null) {
            Logs().instance ?: Logs()
        }
        return Logs().instance
    }

    fun createLog(logDetails: String) {
        if (testing) {
            val stack = Thread.currentThread().stackTrace
            val linenr = stack[Logs.STACK_TRACE_LEVELS_UP_LINE].lineNumber
            val classname = stack[Logs.STACK_TRACE_LEVELS_UP_CLASS].fileName
            Log.e("Log output", "$linenr  $classname $logDetails")
        }
    }

    fun createLog(Tag: String, logDetails: String) {
        if (testing) {
            val stack = Thread.currentThread().stackTrace
            val linenr = stack[STACK_TRACE_LEVELS_UP_LINE].lineNumber
            val classname = stack[STACK_TRACE_LEVELS_UP_CLASS].fileName
            if (Tag == "") {
                Log.d("Log Output", "$linenr  $classname $logDetails")
            } else {
                Log.d(Tag, "$linenr  $classname $logDetails")
            }
        }
    }
}