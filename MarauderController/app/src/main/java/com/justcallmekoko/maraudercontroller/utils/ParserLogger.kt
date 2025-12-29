package com.justcallmekoko.maraudercontroller.utils

import android.util.Log

interface ParserLogger {
    fun w(tag: String, message: String, throwable: Throwable? = null)
}

class AndroidParserLogger : ParserLogger {
    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }
}

class ConsoleParserLogger : ParserLogger {
    override fun w(tag: String, message: String, throwable: Throwable?) {
        println("WARN [$tag]: $message")
        throwable?.printStackTrace()
    }
}
