package com.rajveer.cultureconnect.core

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CultureConnectApp : Application(){
    override fun onCreate() {
        super.onCreate()
        // 🔥 Force init Firebase here
        FirebaseApp.initializeApp(this)
    }
}

