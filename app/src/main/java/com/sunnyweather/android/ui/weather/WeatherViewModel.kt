package com.sunnyweather.android.ui.weather


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.LogUtil
import com.sunnyweather.android.logic.Respository
import com.sunnyweather.android.logic.model.Location


class WeatherViewModel : ViewModel() {

    //LiveData。可以包含任何类型的数据，并且在数据变化的时候，通知观察者
    private val locationLiveData = MutableLiveData<Location>()
    var locationLng = ""
    var locationLat = ""
    var placeName = ""
    //
    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        LogUtil.v("WeatherViewModel", "---weatherLiveData lng为${location.lng}, lat为${location.lat}")
        Respository.refreshWeather(location.lng, location.lat)
    }

    fun refreshWeather(lng: String, lat: String) {
        LogUtil.v("WeatherViewModel", "---refreshWeather入参 lng为${lng}, lat为${lat}")
        //给LiveData设置数据
        locationLiveData.value = Location(lng, lat)
    }
}