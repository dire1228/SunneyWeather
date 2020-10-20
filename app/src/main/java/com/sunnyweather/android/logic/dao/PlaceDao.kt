package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

/**
 * 记录选中的城市
 */
object PlaceDao {

    /**
     * 存储位置
     */
    fun savePlace(place: Place) {
        sharedPreferences().edit() {
            putString("place", Gson().toJson(place))
        }
    }

    /**
     * 获取存储的位置
     */
    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    /**
     * 是否有存储
     */
    fun isPlaceSaved() = sharedPreferences().contains("pl.ace")

    /**
     * 创建SharePreference
     */
    private fun sharedPreferences() = SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)
}
