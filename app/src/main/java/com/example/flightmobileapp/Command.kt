package com.example.flightmobileapp

import com.squareup.moshi.Json

data class Command(

    @Json(name = "aileron") var aileron: Float,
    @Json(name = "rudder") var rudder: Float,
    @Json(name = "elevator") var elevator: Float,
    @Json(name = "throttle") var throttle: Float

)