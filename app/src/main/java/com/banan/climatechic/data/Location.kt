package com.banan.climatechic.data

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String
)
