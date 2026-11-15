package com.example.androidpractices

import android.app.Application
import com.example.androidpractices.di.ServiceLocator
import com.example.androidpractices.notifications.ClassReminderNotifier

class BreweryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        ClassReminderNotifier.createChannel(this)
    }
}
