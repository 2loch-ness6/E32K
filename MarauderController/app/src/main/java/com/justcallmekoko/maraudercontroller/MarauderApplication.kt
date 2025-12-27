package com.justcallmekoko.maraudercontroller

import android.app.Application
import android.content.Context

class MarauderApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        private lateinit var instance: MarauderApplication
        
        fun getAppContext(): Context = instance.applicationContext
    }
}
