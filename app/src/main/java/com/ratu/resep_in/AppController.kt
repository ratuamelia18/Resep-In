package com.ratu.resep_in

import android.app.Application
import com.cloudinary.android.MediaManager

class AppController : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "cloud_name" to "dh3kykrlb",
            "api_key" to "622123481599881",
            "api_secret" to "dIA5TC04OKe-S-oQcdrpPmgTw0A"

        )
        MediaManager.init(this, config)
    }
}