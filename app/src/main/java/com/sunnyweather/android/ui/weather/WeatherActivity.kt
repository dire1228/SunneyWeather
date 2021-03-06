package com.sunnyweather.android.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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

    val viewModel by lazy { ViewModelProviders.of(this).get(WeatherViewModel::class.java) }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //将背景图与UI融合在一起
        val decorView = window.decorView
        //布局会显示在状态栏上
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        //将状态栏设置为透明色
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_weather)
        //从intent中取数据，赋值
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
            //标记不刷新
            swipeRefresh.isRefreshing = false
        })
        swipeRefresh.setColorSchemeColors(R.color.colorPrimary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        //监听刷新按钮
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        //监听滑动菜单被隐藏
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerOpened(drawerView: View) {}
        })
    }

    /**
     * 刷新天气，并标记已刷新
     */
    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather) {
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        LogUtil.v("WeatherActivity", "---daily是${daily}")
        //填充now.xml布局
        val currentTempText = "${realtime.temperature.toInt()}℃"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = currentTempText
        LogUtil.v("WeatherActivity", "---currentPM25Text是${currentPM25Text}")
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        LogUtil.v("WeatherActivity", "---填充now.xml布局 完毕")
        //填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        LogUtil.v("WeatherActivity", "---forecastLayout.removeAllViews() 完毕")
        val days = daily.skycon.size
        LogUtil.v("WeatherActivity", "---daily.skycon是 ${daily.skycon}")
        LogUtil.v("WeatherActivity", "---daily.skycon.size是 ${days}")
        //抛了一个java.lang.IllegalArgumentException异常，没有被捕获
//        try {
            for (i in 0 until days) {
                LogUtil.v("WeatherActivity", "---填充forecast.xml布局中的数据--循环开始 ${i}")
                val skycon = daily.skycon[i]
                val temperature = daily.temperature[i]
                val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
                val dateInfo = view.findViewById(R.id.dateInfo) as TextView
                val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
                val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
                val skyInfo = view.findViewById(R.id.skyInfo) as TextView
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateInfo.text = simpleDateFormat.format(skycon.date)
                val sky = getSky(skycon.value)
                skyIcon.setImageResource(sky.icon)
                LogUtil.v("WeatherActivity", "---sky.info是${sky.info}")
                LogUtil.v("WeatherActivity", "---skyInfo是${skyInfo}")
                LogUtil.v("WeatherActivity", "---temperature是${temperatureInfo}")
                skyInfo.text = sky.info
//            skyInfo.text = "晴"
                LogUtil.v("WeatherActivity", "---skyInfo是${skyInfo.text}")
                val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
                LogUtil.v("WeatherActivity", "---tempText是${tempText}")
//            val tempText = "${temperature.min} ~ ${temperature.max} ℃"
                temperatureInfo.text = tempText
                forecastLayout.addView(view)
            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

        //填充Life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE
    }
}