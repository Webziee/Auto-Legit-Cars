package com.example.tablayout

data class Car(
    val maincarimage: String = "",
    val title: String = "",
    val condition: String = "",
    val mileage: Int = 0,
    val bodytype: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val fueltype: String = "",
    val dealership: String = "",
    val location: String = "",
    val transmission: String = "",
    val special: String = "",
    val price: Int = 0,
    val imageResourceList: List<String> = listOf()
)

