package com.banan.climatechic

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.banan.climatechic.data.Clothing
import com.banan.climatechic.data.Weather
import com.banan.climatechic.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.Int.Companion.MIN_VALUE

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var clothingList: List<Clothing>
    val winterClothes = listOf(
        Clothing(R.drawable.winter_pic_a),
        Clothing(R.drawable.winter_pic_b),
        Clothing(R.drawable.winter_pic_man_a),
        Clothing(R.drawable.winter_pic_man_b)
    )
    val summerClothes = listOf(
        Clothing(R.drawable.summer_pic_a), Clothing(R.drawable.summer_pic_b),
        Clothing(R.drawable.summer_pic), Clothing(R.drawable.summer_pic_man)
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        clothingList = if (isWinter()) {
            winterClothes
        } else {
            summerClothes
        }
        makeRequest()
    }


    @SuppressLint("SuspiciousIndentation")
    private fun makeRequest() {
        val url =
            "http://api.weatherstack.com/current?access_key=${BuildConfig.API_KEY}&query=ramallah"
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                binding.viewsGroup.visibility = View.INVISIBLE
                binding.lottieNoInternet.visibility = View.VISIBLE
                addSnackBarWhileNoInternet()
                Log.e("Banan", "No internet ")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    val weather = Gson().fromJson(jsonString, Weather::class.java)
                    runOnUiThread {
                        setViewsFromApi(weather)
                        setClothingImage(weather)
                    }
                }
            }
        })
    }

    private fun isWinter(): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)
        val yesterdayDateString = dateFormat.format(yesterday.time)
        val yesterdayWeatherJson = sharedPreferences.getString(yesterdayDateString, null)
        val todayWeatherJson = sharedPreferences.getString(today, null)
        if (yesterdayWeatherJson == null || todayWeatherJson == null) {
            return false
        }
        val yesterdayWeather = Gson().fromJson(yesterdayWeatherJson, Weather::class.java)
        val todayWeather = Gson().fromJson(todayWeatherJson, Weather::class.java)

        return yesterdayWeather.current.temperature < WINTER_MAX_TEMP
                && todayWeather.current.temperature < WINTER_MAX_TEMP
    }


    private fun saveDate(date: String) {
        val editor = sharedPreferences.edit()
        editor.putString("MyPrefs", date)
        editor.apply()
    }

    private fun setClothingImage(weatherResponse: Weather) {
        val temperature = weatherResponse.current.temperature
        val currentDate ="2023-04-21 //weatherResponse.location.localtime.take(10) "
        val lastDate = sharedPreferences.getString("MyPrefs", "")
        if (lastDate == currentDate && sharedPreferences.contains("lastImageResourceId")) {
            val lastImageResourceId = sharedPreferences.getInt("lastImageResourceId", 0)
            binding.imageViewOutfit.setImageResource(lastImageResourceId)
        } else {

            val image = getRandomImage(temperature).imageResourceId
            binding.imageViewOutfit.setImageResource(image)
            sharedPreferences.edit().putInt("lastImageResourceId", image).apply()
            saveDate(currentDate)
        }

    }

    private fun getRandomImage(tempreture: Int): Clothing {
        if (tempreture in MIN_VALUE..WINTER_MAX_TEMP) {
            return winterClothes.random()
        } else {
            return summerClothes.random()
        }
    }

    private fun setViewsFromApi(weather: Weather) {
        "${weather.location.name} ${weather.location.country}".also {
            binding.textViewCityCountryName.text = it
        }
        "${weather.current.temperature}°C".also { binding.textViewTempreture.text = it }
        "${weather.current.windSpeed} Km/h".also { binding.textViewWindSpeed.text = it }
        binding.textViewHumidityNo.text = weather.current.humidity.toString()
        binding.textViewPressureNo.text = weather.current.pressure.toString()
        getWeatherDescriptions(weather.current.weatherDescriptions)
    }

    private fun getWeatherDescriptions(weatherDescriptions: List<String>) {
        val stringBuilder = StringBuilder()
        for ((index, description) in weatherDescriptions.withIndex()) {
            stringBuilder.append(description)
            if (index != weatherDescriptions.lastIndex) {
                stringBuilder.append(", ")
            }
        }
        binding.textViewWethearDescription.text = stringBuilder.toString()
    }

    private fun addSnackBarWhileNoInternet() {
        if (!isInternetAvailable()) {
            Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_INDEFINITE)
                .setAction("Try again") {
                    binding.lottieNoInternet.visibility = View.VISIBLE
                    makeRequest()
                }
                .show()
            return
        } else {
            binding.lottieNoInternet.visibility = View.INVISIBLE
            binding.viewsGroup.visibility = View.VISIBLE
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    companion object {
        const val WINTER_MAX_TEMP = 20
    }

}








