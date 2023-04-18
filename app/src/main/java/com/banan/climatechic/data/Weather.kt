package com.banan.climatechic.data

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("location") val location: Location,
    @SerializedName("current") val current: Current
    )
