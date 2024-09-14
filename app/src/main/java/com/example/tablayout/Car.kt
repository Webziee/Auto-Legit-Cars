package com.example.tablayout

data class Car(
    val maincarimage: String = "",
    val title: String = "",
    val condition: String = "",
    val mileage: Int = 0,
    val dealership: String = "",
    val location : String = "",
    val transmisson: String =  "",
    val imageResourceList: List< String> = listOf()// List of scrollable images in the car card
)