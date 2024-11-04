package com.legitautocars.app


import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class CarViewModel(application: Application) : AndroidViewModel(application) {

    private val carDatabaseHelper = CarDatabaseHelper(application)
    private val _allCars = MutableLiveData<List<Car>>()
    val allCars: LiveData<List<Car>> get() = _allCars

    init {
        loadCarsFromDatabase()
    }

    private fun loadCarsFromDatabase() {
        _allCars.value = carDatabaseHelper.getAllCars()
    }

    fun updateLocalDatabase(cars: List<Car>) {
        CoroutineScope(Dispatchers.IO).launch {
            // Clear the local database and insert the new data
            carDatabaseHelper.clearDatabase()
            carDatabaseHelper.insertCars(cars)

            // Load the updated data from the database
            withContext(Dispatchers.Main) {
                loadCarsFromDatabase()
            }
        }
    }

    // Syncs the data with Supabase if there is internet connectivity
    fun syncCarsWithSupabase() {
        CoroutineScope(Dispatchers.IO).launch {
            if (isOnline()) {
                val supabaseCars = fetchCarsFromSupabase()  // Fetch from Supabase
                if (supabaseCars.isNotEmpty()) {
                    carDatabaseHelper.clearDatabase()        // Clear old data
                    carDatabaseHelper.insertCars(supabaseCars)  // Insert new data
                    withContext(Dispatchers.Main) {
                        loadCarsFromDatabase()  // Load into LiveData for UI
                    }
                }
            }
        }
    }

    // Check if the device is online
    public fun isOnline(): Boolean {
        val connectivityManager =
            getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Retrofit API to interact with Supabase
    private suspend fun fetchCarsFromSupabase(): List<Car> {
        return try {
            val response = SupabaseApi.retrofitService.getCars()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Retrofit interface for Supabase API
    interface SupabaseApiService {
        @GET("/rest/v1/cars")
        suspend fun getCars(
            @Query("select") select: String = "*"
        ): retrofit2.Response<List<Car>>
    }

    // Retrofit instance
    object SupabaseApi {
        private const val BASE_URL = "https://odbddwdwklhebnvgvwlv.supabase.co"

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val retrofitService: SupabaseApiService by lazy {
            retrofit.create(SupabaseApiService::class.java)
        }
    }

    fun getFilteredCarsOffline(filters: Map<String, String?>): List<Car> {
        return carDatabaseHelper.getFilteredCars(filters)
    }

    fun getFavoritesFromLocalDatabase(carIds: List<Int>): List<Car> {
        return carDatabaseHelper.getFavoriteCars(carIds)
    }

    // Fetch all cars from the local SQLite database
    fun getAllCarsFromLocalDatabase(): List<Car> {
        return carDatabaseHelper.getAllCars()
    }


}
