package com.chandanshakya.fuellog

import android.app.Application
import com.chandanshakya.fuellog.di.AppContainer

class FuelLogApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
