package com.ratu.resep_in

import android.app.Application
import com.cloudinary.android.MediaManager

class AppController : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "cloud_name" to "dh3kykrlb"
        )
        MediaManager.init(this, config)
    }
}