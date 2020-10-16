package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class DailyResponse(val status: String, val result: Result) {

    data class Result(val daily: Daily)
    data class Daily(val temperature: List<Temperature>, val skycon: List<Skycon>,
                     @SerializedName("life_index") val lifeIndex: LifeIndex)
    data class Temperature(val max: Float, val min: Float)
    //备选。实际接口返回了4个字段的内容
//    data class Temperature(val date: Date, val max: Float, val min: Float, val avg: Float)
    data class Skycon(val value: String, val date: Date)
    data class LifeIndex(val coldRisk: List<LifeDescription>, val carWashing: List<LifeDescription>, val ultraviolet: List<LifeDescription>, val dressing: List<LifeDescription>)
    data class LifeDescription(val desc: String)
}