package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/*
1.全局context
2.token是令牌值
 */

class SunnyWeatherApplication : Application() {

    companion object {

        //令牌值
        const val TOKEN = "填入申请的令牌值"

        //注解标记不会内存泄露
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}