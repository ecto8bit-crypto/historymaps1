package com.example.historymaps

import android.app.Application
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration
import java.io.File

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val ctx = applicationContext

        // SharedPreferences для osmdroid
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        Configuration.getInstance().load(ctx, prefs)

        // Безопасный путь для кэша карт
        val osmBasePath = File(ctx.getExternalFilesDir(null), "osmdroid")
        val osmTileCache = File(osmBasePath, "tiles")

        osmTileCache.mkdirs()

        Configuration.getInstance().apply {
            osmdroidBasePath = osmBasePath
            osmdroidTileCache = osmTileCache
            userAgentValue = BuildConfig.APPLICATION_ID
        }
    }
}
