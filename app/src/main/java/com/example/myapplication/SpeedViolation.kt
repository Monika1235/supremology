package com.example.myapplication

data class SpeedViolation(
    val speed: Float,
    val limit:Float,
    val userID: String ="",
    val status:String =""
)
