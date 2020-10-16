package com.sunnyweather.android.ui.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sunnyweather.android.LogUtil

import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.forecast_item.*
import kotlinx.android.synthetic.main.life_index.*
import kotlinx.android.synthetic.main.now.*
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
            LogUtil.v("WeatherActivity", "---lat为空，设置值${viewModel.locationLng}")
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
            LogUtil.v("WeatherActivity", "---lng为空，设置值${viewModel.locationLat}")
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
            LogUtil.v("WeatherActivity", "---place为空，设置值${viewModel.placeName}")
        }
        //LiveData。数据变化时，回调接口Observe
        viewModel.weatherLiveData.observe(this, Observer {  result ->
            val weather = result.getOrNull()
            if (weather != null) {
                LogUtil.v("WeatherActivity", "---调用showWeatherInfo展示天气")
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
    }

    private fun showWeatherInfo(weather: Weather) {
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        //填充now.xml布局
        val currentTempText = "${realtime.temperature.toInt()}℃"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = currentTempText
        currentSky.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        LogUtil.v("WeatherActivity", "---填充now.xml布局 完毕")
        //填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        LogUtil.v("WeatherActivity", "---forecastLayout.removeAllViews() 完毕")
        val days = daily.skycon.size
        //抛了一个java.lang.IllegalArgumentException异常，没有被捕获
        try {
            for (i in 0 until days) {
                LogUtil.v("WeatherActivity", "---填充forecast.xml布局中的数据--循环开始 ${i}")
                val skycon = daily.skycon[i]
                val temperature = daily.temperature[i]
                val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
                val dateInfo = view.findViewById(R.id.dateInfo) as TextView
                val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
                val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateInfo.text = simpleDateFormat.format(skycon.date)
                val sky = getSky(skycon.value)
                skyIcon.setImageResource(sky.icon)
                LogUtil.v("WeatherActivity", "---sky.info是${sky.info}")
                skyInfo.text = sky.info
//            skyInfo.text = "晴"
                LogUtil.v("WeatherActivity", "---skyInfo是${skyInfo.text}")
                val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
                LogUtil.v("WeatherActivity", "---tempText是${tempText}")
//            val tempText = "${temperature.min} ~ ${temperature.max} ℃"
                temperatureInfo.text = tempText
                forecastLayout.addView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //填充Life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE
    }
}