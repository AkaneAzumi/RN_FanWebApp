package com.rnfanbotwebapp

import android.app.Application
import android.content.Context
import com.facebook.soloader.SoLoader


class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
    }



}
