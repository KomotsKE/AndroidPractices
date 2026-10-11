package com.example.androidpractices

import android.app.Application
import com.example.androidpractices.di.ServiceLocator

class BreweryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
