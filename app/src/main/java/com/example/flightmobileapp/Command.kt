package com.example.flightmobileapp

import com.squareup.moshi.Json

data class Command(

    @Json(name = "aileron") var aileron: Float,
    @Json(name = "rudder") var rudder: Float,
    @Json(name = "elevator") var elevator: Float,
    @Json(name = "throttle") var throttle: Float

)

//  //  private var rudder: Float,
//   // private var throttle: Float,
//   // private var elevator: Float,
//   // private var aileron: Float
//) {
//    override fun toString(): String {
//        return "aileron: ${this.aileron}, rudder: ${this.rudder}," +
//                " elevator: ${this.elevator}, throttle: ${this.throttle}"
//    }
//
//    fun setRudderValue(value: Float) {
//        rudder = value
//    }
//
//    fun setThrottleValue(value: Float) {
//        throttle = value
//    }
//
//    fun setElevatorValue(value: Float) {
//        elevator = value
//    }
//
//    fun setAileronValue(value: Float) {
//        aileron = value
//    }
//
//}