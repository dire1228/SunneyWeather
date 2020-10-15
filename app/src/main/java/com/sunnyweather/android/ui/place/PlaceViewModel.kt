package com.sunnyweather.android.ui.place

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.LogUtil
import com.sunnyweather.android.logic.Respository
import com.sunnyweather.android.logic.model.Place

class PlaceViewModel : ViewModel() {

    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    val placeLiveData = Transformations.switchMap(searchLiveData) {query ->
        LogUtil.v("PlaceViewModel", "---placeLiveData的入参为：${query}")
        Respository.searchPlace(query)
    }

    fun searchPlace(query: String) {
        LogUtil.v("PlaceViewModel", "---searchLiveData.value设置为:${query}")
        searchLiveData.value = query
    }

}