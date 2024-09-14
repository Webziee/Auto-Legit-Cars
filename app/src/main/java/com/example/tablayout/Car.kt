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
    val transmisson: String = "",
    val imageResourceList: List<String> = listOf()
)
