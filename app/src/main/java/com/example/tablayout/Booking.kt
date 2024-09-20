package com.example.tablayout

data class Booking(
    val car_id: Int,     // UUID of the car (foreign key)
    val date_time: String,  // DateTime in the format Supabase expects
    val user_email: String  // The email of the user booking the test drive
)
