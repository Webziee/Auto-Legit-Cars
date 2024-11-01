package com.example.tablayout

import androidx.room.PrimaryKey

data class Car(
 val id: Int? = null,  // Make `id` nullable as it will be auto-generated by Supabase
 val title: String = "",
 val maincarimage: String = "",  // Main image URL
 val condition: String = "",  // Condition of the car (e.g., Certified Pre-Owned)
 val mileage: Int = 0,  // Mileage of the car
 val price: Int = 0,  // Price of the car
 val bodytype: String = "",  // Body type (e.g., SUV, Sedan)
 val make: String = "",  // Car make (e.g., BMW, Toyota)
 val model: String = "",  // Car model (e.g., X5, Corolla)
 val year: Int = 0,  // Change year to Int as it is an integer in the database
 val fueltype: String = "",  // Fuel type (e.g., Diesel, Petrol)
 val dealership: String = "",  // Dealership name
 val location: String = "",  // Location (e.g., Sandton)
 val transmission: String = "",  // Transmission type (e.g., Automatic)
 val imageresourcelist: List<String> = listOf(),  // List of image URLs
)



//data class Car(
//    val maincarimage: String = "",
//    val title: String = "",
//    val condition: String = "",
//    val mileage: Int = 0,
//    val price: Int = 0,
//    val bodytype: String = "",
//    val make: String = "",
//    val model: String = "",
//    val year: String = "",
//    val fueltype: String = "",
//    val dealership: String = "",
//    val location: String = "",
//    val transmission: String = "",
//
//    val price: Int = 0,
//
//)