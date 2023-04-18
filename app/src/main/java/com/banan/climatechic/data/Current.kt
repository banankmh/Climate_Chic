package com.banan.climatechic.data

import com.google.gson.annotations.SerializedName

data class Current(
    @SerializedName("temperature") val temperature: Int,
    @SerializedName("weather_descriptions") val weatherDescriptions: List<String>,
    @SerializedName("wind_speed") val windSpeed: Int,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int
    )
