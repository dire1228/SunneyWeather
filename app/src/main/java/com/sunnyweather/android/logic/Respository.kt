package com.sunnyweather.android.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.sunnyweather.android.LogUtil
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

/**
 * 仓库层
 * 仓库层的主要工作，主要是判断调用方请求的数据是应该从本地获取还是从网络中获取，并将获得数据返回
 * 有一个汇总的作用，所有logic以外的，只调用这个单例类中的方法即可
 * 把内部接口封装下，提供对外的接口
 */
object Respository {

    fun searchPlace(query: String) = fire(Dispatchers.IO) {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                LogUtil.v("Respository", "---place是${places}")
                Result.success(places)
            } else {
                Result.failure(RuntimeException("报错：response status is ${placeResponse.status}"))
            }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {

            coroutineScope {
                val deferredRealtime = async {
                    LogUtil.v("Respository", "---lng为${lng}, lat为${lat}")
                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
                }
                val deferredDaily = async {
                    SunnyWeatherNetwork.getDailWeather(lng, lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                LogUtil.v("Respository", "---dailyResponse是${dailyResponse}")
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                    val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                    LogUtil.v("Respository", "--- realtime:${realtimeResponse.result.realtime} \n daily:${dailyResponse.result.daily}")
                    Result.success(weather)
                } else {
                    Result.failure(
                        RuntimeException(
                            "===== realtime response status is ${realtimeResponse.status}"+
                            "===== daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
    }


    //所有的方法都使用fire调用
    /**
     * 提取共同点，封装成一个方法
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception){
                Result.failure<T>(e)
            }
            emit(result)
        }

    /**
     * 对PlaceDao层的接口封装
     * savePlace保存位置
     * getSavePlace获取保存位置
     * isPlaceSaved判断案是否保存
     */
    fun savePlace(place: Place) = PlaceDao.savePlace(place)
    fun getSavePlace() = PlaceDao.getSavedPlace()
    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

}