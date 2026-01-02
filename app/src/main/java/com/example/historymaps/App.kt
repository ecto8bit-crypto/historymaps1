package com.example.historymaps

import android.app.Application
import org.osmdroid.config.Configuration

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
    }
}
