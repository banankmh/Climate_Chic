package com.banan.climatechic

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private lateinit var binding: ActivityMainBinding
    val preferences by lazy {
        getSharedPreferences(TOKEN, MODE_PRIVATE)
    }
    private val winterClothes = mutableListOf<Int>()
    private val summerClothes = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        makeRequest()

    }

    init {
        winterClothes.apply {
            add(0,R.drawable.winter_pic_a)
            add(1,R.drawable.winter_pic_b)
        }
        summerClothes.apply {
            add(0,R.drawable.summer_pic_a)
            add(1,R.drawable.summer_pic_b)
        }
    }

    private fun getImageId(temperature:Int):Int{
        return when(temperature){
            in Int.MIN_VALUE..WINTER_MAX_TEMP -> winterClothes.random()
            else -> summerClothes.random()
        }
    }

    private fun saveImage(drawableId: Int) {
        val editor = preferences.edit()
        editor.putInt("imageId", drawableId)
        editor.apply()
    }

    private fun saveDate(date:String) {
        val editor = preferences.edit()
        editor.putString(TOKEN, date)
        editor.apply()
    }

    fun checkIsImageSaved(): Boolean {
        return preferences.contains("imageId")
    }

    fun checkIsDateSaved(date: String?): Boolean {
        return (date == getSavedDate())
    }

    private fun getSavedImage():Int{
        return preferences.getInt(TOKEN,0)
    }

    private fun getSavedDate():String?{
        return preferences.getString(TOKEN,"")
    }

    private fun makeRequest() {
            val url = "http://api.weatherstack.com/current?access_key=${BuildConfig.API_KEY}&query=ramallah"
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                binding.viewsGroup.visibility= View.INVISIBLE
                binding.lottieNoInternet.visibility=View.VISIBLE
                addSnackBarWhileNoInternet()
                Log.e("Banan", "No interent ")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    val weather = Gson().fromJson(jsonString, Weather::class.java)
                    runOnUiThread {
                        setViewsFromApi(weather)
                        val currentDate=weather.location.localtime.take(11)
                        Log.e("banan",currentDate)
                        saveDate(currentDate)
                    }
                }
            }
        })
    }

    private fun setViewsFromApi(weather: Weather) {
        "${weather.location.name} ${weather.location.country}".also { binding.textViewCityCountryName.text = it }
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

    private fun addSnackBarWhileNoInternet(){
        if (!isInternetAvailable()) {
            Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_INDEFINITE)
                .setAction("Try again") {
                    binding.lottieNoInternet.visibility=View.VISIBLE
                    makeRequest()
                }
                .show()
            return
        }
        else
            binding.viewsGroup.visibility= View.VISIBLE
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    companion object {
        const val WINTER_MAX_TEMP = 20
        const val TOKEN = "Climate Chic"
    }

    }








