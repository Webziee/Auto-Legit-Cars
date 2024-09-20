package com.example.tablayout

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseUtils {

    // Add a car directly to Supabase
    fun addCarToSupabase(car: Car, onSuccess: (Int?) -> Unit, onFailure: (Exception) -> Unit) {
        val apiService = RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")

        // Remove `id` when sending to Supabase as it's auto-generated
        val carWithoutId = car.copy(id = null)

        val call = apiService.addCarToSupabase(carWithoutId)
        call.enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("SupabaseUtils", "Car added to Supabase successfully.")
                    onSuccess(null) // or pass the generated `id` if retrievable from the response
                } else {
                    Log.w("SupabaseUtils", "Failed to add car to Supabase. Response code: ${response.code()}")
                    onFailure(Exception("Failed to add car to Supabase"))
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SupabaseUtils", "Error adding car to Supabase: ${t.message}")
                onFailure(Exception(t))
            }
        })
    }


    // Sync all existing cars to Supabase (for one-time sync)
    fun syncExistingCarsToSupabase(cars: List<Car>) {
        val apiService = RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")

        for (car in cars) {
            val call = apiService.addCarToSupabase(car)
            call.enqueue(object : retrofit2.Callback<Void> {
                override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("SupabaseSync", "Car synced to Supabase.")
                    } else {
                        Log.w("SupabaseSync", "Failed to sync car to Supabase. Response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("SupabaseSync", "Error syncing car with Supabase: ${t.message}")
                }
            })
        }
    }

    object RetrofitClient {
        private var retrofit: Retrofit? = null

        fun getApiService(baseUrl: String): ApiServices {
            if (retrofit == null) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(SupabaseInterceptor()) // Ensure the API key is added
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(ApiServices::class.java)
        }
    }
}
