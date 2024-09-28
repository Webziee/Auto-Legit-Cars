package com.example.tablayout

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServices {

    // Add car to Supabase
    @POST("/rest/v1/cars")
    fun addCarToSupabase(@Body car: Car): Call<Void>

    // Update car in Supabase
    @PUT("/rest/v1/cars")
    fun updateCarInSupabase(@Body car: Car): Call<Void>

    // Delete car from Supabase
    @DELETE("/rest/v1/cars/{id}")
    fun deleteCarFromSupabase(@Path("id") carId: String): Call<Void>

    // Get filtered cars from Supabase
    @GET("/rest/v1/cars")
    fun getFilteredCars(
        @Query("make") make: String?,
        @Query("model") model: String?,
        @Query("year") year: String?,
        @Query("mileage") mileage: String?,
        @Query("transmission") transmission: String?,
        @Query("price") price: String?,
        @Query("location") location: String?,
        @Query("bodytype") bodytype: String?,
        @Query("condition") condition: String? = null,  // Optional
        @Query("dealership") dealership: String? = null,  // Optional
        @Query("fuelType") fuelType: String? = null  // Optional
    ): Call<List<Car>>

    // Get a list of cars based on the car title
    @GET("/rest/v1/cars")
    fun getCars(
        @Query("carTitle") carTitle: String
    ): Call<List<Car>>

    // Create a booking and store it in Supabase
    @POST("/rest/v1/bookings")
    fun createBooking(
        @Body booking: Booking
    ): Call<Void>
}
