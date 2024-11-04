package com.legitautocars.app


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.legitautocars.app.Car
import  com.legitautocars.app.CarAdapter
import com.legitautocars.app.CarViewModel
import com.legitautocars.app.SupabaseUtils
import com.legitautocars.app.R
import retrofit2.Response

/**
 * Buy fragment to manage car listing and filtering.
 *
 * This class allows users to filter through a list of cars and book test drives.
 * The car data is fetched from Supabase using Retrofit API requests.
 *
 * The following implementation was inspired by the following resources:
 *
 * - Supabase Documentation: https://supabase.com/docs
 * - Retrofit Documentation: https://square.github.io/retrofit/
 * - Android Developer Documentation: https://developer.android.com
 *
 * This fragment uses RecyclerView for listing cars and spinners for filtering options.
 */

class Buy : Fragment() {

    private val carList = mutableListOf<Car>()
    private lateinit var carAdapter: CarAdapter
    private lateinit var makeSpinner: Spinner
    private lateinit var modelSpinner: Spinner
    private lateinit var minPriceSpinner: Spinner
    private lateinit var maxPriceSpinner: Spinner
    private lateinit var locationSpinner: Spinner
    private lateinit var bodyTypeSpinner: Spinner
    private lateinit var minYearSpinner: Spinner
    private lateinit var maxYearSpinner: Spinner
    private lateinit var transmissionSpinner: Spinner
    private lateinit var maxMileageSpinner: Spinner
    private lateinit var resetFilter: TextView
    private lateinit var searchButton: Button
    private lateinit var viewFavButton: Button
    private lateinit var comparePricesButton: Button
    private lateinit var makeList: MutableList<String>
    private lateinit var modelList: MutableList<String>
    private lateinit var makeAdapter: ArrayAdapter<String>
    private lateinit var modelAdapter: ArrayAdapter<String>
    private lateinit var carViewModel: CarViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_buy, container, false)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        carAdapter = CarAdapter(carList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = carAdapter

        // Initialize Spinners for Make and Model
        makeSpinner = view.findViewById(R.id.car_make)
        modelSpinner = view.findViewById(R.id.car_model)
        makeList = mutableListOf("Select Make")
        modelList = mutableListOf("Select Model")
        makeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, makeList)
        modelAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, modelList)
        makeSpinner.adapter = makeAdapter
        modelSpinner.adapter = modelAdapter

        // Fetch car makes and populate the spinner
        fetchCarMakes()

        // Handle Make selection and update Model spinner based on selected make
        makeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    val selectedMake = makeList[position]
                    fetchCarModels(selectedMake)
                } else {
                    modelList.clear()
                    modelList.add(getString(R.string.Toast85))
                    modelAdapter.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Initialize other Spinners
        minPriceSpinner = view.findViewById(R.id.min_price)
        maxPriceSpinner = view.findViewById(R.id.max_price)
        locationSpinner = view.findViewById(R.id.location)
        bodyTypeSpinner = view.findViewById(R.id.body_type)
        minYearSpinner = view.findViewById(R.id.min_year)
        maxYearSpinner = view.findViewById(R.id.max_year)
        transmissionSpinner = view.findViewById(R.id.transmission)
        maxMileageSpinner = view.findViewById(R.id.max_mileage)

        // Setup Spinners
        setupSpinner(minPriceSpinner, R.array.min_price_items)
        setupSpinner(maxPriceSpinner, R.array.max_price_items)
        setupSpinner(locationSpinner, R.array.location_items)
        setupSpinner(bodyTypeSpinner, R.array.body_type_items)
        setupSpinner(minYearSpinner, R.array.min_year_items)
        setupSpinner(maxYearSpinner, R.array.max_year_items)
        setupSpinner(transmissionSpinner, R.array.car_transmission)
        setupSpinner(maxMileageSpinner, R.array.max_mileage_items)

        // Reset Filter Button Logic
        resetFilter = view.findViewById(R.id.btnReset)
        resetFilter.setOnClickListener { resetFilters() }

        // Search Button Logic
        searchButton = view.findViewById(R.id.btnSearch)
        searchButton.setOnClickListener { performSearch() }

        //View Favourites Button Logic
        viewFavButton = view.findViewById(R.id.btnFavSearch)
        viewFavButton.setOnClickListener {
            displayFavourites()
        }

        //Compare Prices Button Logic
        comparePricesButton = view.findViewById(R.id.btnPriceCompare)
        comparePricesButton.setOnClickListener {
            comparePrices()
        }


        // Initialize Test Drive Button (inside RecyclerView car items)
        // im handling it inside the adapter, hence i must add a listener to handle test drive booking
        carAdapter.setOnTestDriveClickListener { selectedCar ->
            if (selectedCar != null) {
                val carId = selectedCar.id ?: 0  // Use carId; default to 0 if null
                val carTitle = selectedCar.title
                val bundle = Bundle().apply {
                    putString("carTitle", carTitle)
                    putInt("car_id", carId)  // Pass car_id to the booking fragment
                }

                val fragment = fragment_book_test_drive()
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), getString(R.string.Toast1), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Initialize ViewModel using ViewModelProvider
        carViewModel = ViewModelProvider(this).get(CarViewModel::class.java)

        // Observe local data from SQLite and update the RecyclerView
        carViewModel.allCars.observe(viewLifecycleOwner) { cars ->
            carAdapter.updateData(cars)
        }

        // Sync data with Supabase when online
        carViewModel.syncCarsWithSupabase()


        // Fetch default car list from Supabase
        fetchCarsFromSupabase()

        return view
    }


    private fun resetFilters() {
        // Reset all filter spinners to their default positions
        makeSpinner.setSelection(0)
        modelSpinner.setSelection(0)
        minPriceSpinner.setSelection(0)
        maxPriceSpinner.setSelection(0)
        locationSpinner.setSelection(0)
        bodyTypeSpinner.setSelection(0)
        minYearSpinner.setSelection(0)
        maxYearSpinner.setSelection(0)
        transmissionSpinner.setSelection(0)
        maxMileageSpinner.setSelection(0)

        // Clear the car list and refresh adapter
        carList.clear()
        carAdapter.notifyDataSetChanged()

        // Fetch data depending on network connectivity
        if (carViewModel.isOnline()) {
            fetchCarsFromSupabase()  // Online fetch from Supabase
        } else {
            val allLocalCars =
                carViewModel.getAllCarsFromLocalDatabase()  // Offline fetch from SQLite
            carList.addAll(allLocalCars)
            carAdapter.notifyDataSetChanged()
        }
    }

    private fun fetchCarsFromSupabase() {
        val apiService =
            SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")
        val call = apiService.getFilteredCars(
            make = null, model = null, year = null, mileage = null,
            transmission = null, price = null, location = null, bodytype = null,
            condition = null, dealership = null, fuelType = null
        )

        call.enqueue(object : retrofit2.Callback<List<Car>> {
            override fun onResponse(
                call: Call<List<Car>>,
                response: retrofit2.Response<List<Car>>
            ) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    carList.clear()  // Clear current data
                    if (!cars.isNullOrEmpty()) {
                        carList.addAll(cars)  // Add all cars to the list
                        carViewModel.updateLocalDatabase(cars)  // Store in SQLite for offline access
                        carAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, getString(R.string.Toast2), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(context, getString(R.string.Toast3), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Toast.makeText(
                    context,
                    getString(R.string.Toast4) + ": " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    // Fetch unique car makes from Supabase
    private fun fetchCarMakes() {
        val apiService =
            SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")

        val call = apiService.getFilteredCars(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        call.enqueue(object : retrofit2.Callback<List<Car>> {
            override fun onResponse(
                call: Call<List<Car>>,
                response: retrofit2.Response<List<Car>>
            ) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    if (cars != null) {
                        makeList.clear()
                        makeList.add(getString(R.string.Toast84)) // Add default option

                        val uniqueMakes = cars.map { it.make }.distinct()  // Get unique makes
                        makeList.addAll(uniqueMakes)
                        makeAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.w("Supabase", "Failed to retrieve makes from Supabase")
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Log.e("Supabase", "Error fetching car makes: ${t.message}")
            }
        })
    }

    // Fetch car models for the selected make from Supabase
    private fun fetchCarModels(selectedMake: String) {
        val apiService =
            SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")

        // Correct filter format: make=eq.<value>
        val encodedMake = URLEncoder.encode("eq.$selectedMake", StandardCharsets.UTF_8.toString())

        val call = apiService.getFilteredCars(
            make = encodedMake,
            model = null,
            year = null,
            mileage = null,
            transmission = null,
            price = null,
            location = null,
            bodytype = null,
            condition = null,
            dealership = null,
            fuelType = null
        )

        call.enqueue(object : retrofit2.Callback<List<Car>> {
            override fun onResponse(
                call: Call<List<Car>>,
                response: retrofit2.Response<List<Car>>
            ) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    if (cars != null) {
                        modelList.clear()
                        modelList.add(getString(R.string.Toast85)) // Add default option

                        // Extract distinct models for the selected make
                        val uniqueModels = cars.map { it.model }
                            .distinct()  // Get unique models for the selected make
                        modelList.addAll(uniqueModels)
                        modelAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, getString(R.string.Toast5), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.w("Supabase", "Failed to retrieve models from Supabase")
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Log.e("Supabase", "Error fetching car models: ${t.message}")
            }
        })
    }

    private fun performSearch() {
        val filters = mutableMapOf<String, String?>()

        // Retrieve selected filters
        val selectedMake = makeSpinner.selectedItem.toString()
        val selectedModel = modelSpinner.selectedItem.toString()
        val minPrice = minPriceSpinner.selectedItem.toString().toDoubleOrNull()
        val maxPrice = maxPriceSpinner.selectedItem.toString().toDoubleOrNull()
        val location = locationSpinner.selectedItem.toString()
        val bodytype = bodyTypeSpinner.selectedItem.toString()
        val minYear = minYearSpinner.selectedItem.toString().toIntOrNull()
        val maxYear = maxYearSpinner.selectedItem.toString().toIntOrNull()
        val transmission = transmissionSpinner.selectedItem.toString()
        val maxMileage = maxMileageSpinner.selectedItem.toString().toIntOrNull()

        // Populate filter map
        if (selectedMake != getString(R.string.Toast84)) filters["make"] = "eq.$selectedMake"
        if (selectedModel != getString(R.string.Toast85)) filters["model"] = "eq.$selectedModel"
        if (minPrice != null) filters["price.gte"] = minPrice.toString()
        if (maxPrice != null) filters["price.lte"] = maxPrice.toString()
        if (location.isNotEmpty()) filters["location"] = "eq.$location"
        if (bodytype.isNotEmpty()) filters["bodytype"] = "eq.$bodytype"
        if (minYear != null) filters["year.gte"] = minYear.toString()
        if (maxYear != null) filters["year.lte"] = maxYear.toString()
        if (transmission.isNotEmpty()) filters["transmission"] = "eq.$transmission"
        if (maxMileage != null) filters["mileage.lte"] = maxMileage.toString()

        if (carViewModel.isOnline()) {
            // Online filtering with Supabase API
            val apiService =
                SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")
            val call = apiService.getFilteredCars(
                make = filters["make"],
                model = filters["model"],
                year = filters["year"],
                mileage = filters["mileage"],
                transmission = filters["transmission"],
                price = filters["price"],
                location = filters["location"],
                bodytype = filters["bodytype"],
                condition = null,
                dealership = null,
                fuelType = null
            )

            call.enqueue(object : retrofit2.Callback<List<Car>> {
                override fun onResponse(
                    call: Call<List<Car>>,
                    response: retrofit2.Response<List<Car>>
                ) {
                    if (response.isSuccessful) {
                        val filteredCars = response.body()
                        carList.clear()
                        if (!filteredCars.isNullOrEmpty()) {
                            carList.addAll(filteredCars)
                            carViewModel.updateLocalDatabase(filteredCars)  // Update SQLite for offline access
                            carAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(context, getString(R.string.Toast7), Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.Toast8), Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                    Toast.makeText(
                        context,
                        getString(R.string.Toast9) + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            // Offline filtering using SQLite database
            val localFilteredCars = carViewModel.getFilteredCarsOffline(filters)
            carList.clear()
            carList.addAll(localFilteredCars)
            carAdapter.notifyDataSetChanged()
        }
    }

    private fun setupSpinner(spinner: Spinner, arrayId: Int) {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            arrayId,
            R.layout.custom_spinner_item
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setPopupBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.rounded_dropdown_background)
        )
        spinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.spinner_border)
    }

    /*Now lets create a method for the compare prices button, this button will display all the vehicles in an
    ascending order, so that users can compare the prices of all our vehicles
    This method was achieved with the help of the following videos:
    Shukert, T., 2023. Youtube, Getting started with Android and Supabase. [Online]
    Available at: https://www.youtube.com/watch?v=_iXUVJ6HTHU
    [Accessed 01 November 2024].
    */
    private fun comparePrices() {
        val apiService = SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")
        val call = apiService.getFilteredCars(
            make = null, model = null, year = null, mileage = null,
            transmission = null, price = null, location = null, bodytype = null,
            condition = null, dealership = null, fuelType = null
        )

        call.enqueue(object : retrofit2.Callback<List<Car>> {
            override fun onResponse(
                call: Call<List<Car>>,
                response: retrofit2.Response<List<Car>>
            ) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    if (!cars.isNullOrEmpty()) {
                        Log.d("comparePrices", "Original cars list:")
                        cars.forEach { car -> Log.d("comparePrices", "Car price: ${car.price}") }

                        // Sort cars by price in ascending order
                        val sortedCars = cars.filter { it.price != null }.sortedBy { it.price }

                        Log.d("comparePrices", "Sorted cars list:")
                        sortedCars.forEach { car -> Log.d("comparePrices", "Car price: ${car.price}") }

                        // Update adapter with sorted list and notify
                        carAdapter.updateData(sortedCars)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.Toast95),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, getString(R.string.Toast2), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, getString(R.string.Toast3), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Toast.makeText(context, getString(R.string.Toast4) + ": " + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    /*Now lets fetch the favourite cars from superbase based on the Car ID and display them to the user
          This method was achieved with the help of the following video:
          Shukert, T., 2023. Youtube, Getting started with Android and Supabase. [Online]
          Available at: https://www.youtube.com/watch?v=_iXUVJ6HTHU
          [Accessed 01 November 2024].*/
    private fun displayFavourites()
    {
        //Lets first get the favourites from firebase
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        // List to store non-zero car IDs
        val carIDs = mutableListOf<Int>()

        if (userEmail != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("Favourites").whereEqualTo("Email", userEmail).get()
                .addOnSuccessListener { documents ->
                    for (document in documents)
                    {
                        // Retrieving each CarID and adding them to la ist if it's not zero
                        listOf(
                            document.getLong("CarID1")?.toInt() ?: 0,
                            document.getLong("CarID2")?.toInt() ?: 0,
                            document.getLong("CarID3")?.toInt() ?: 0,
                            document.getLong("CarID4")?.toInt() ?: 0,
                            document.getLong("CarID5")?.toInt() ?: 0
                        ).filter { it != 0 }.forEach { carIDs.add(it) }
                    }
                    if (carIDs.isNotEmpty())
                    {
                        fetchFavCarsFromSupabase(carIDs)
                    }
                    else
                    {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.Toast93),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), getString(R.string.Toast92), Toast.LENGTH_LONG)
                        .show()
                }
        }
        else
        {
            Toast.makeText(requireContext(), getString(R.string.Toast79), Toast.LENGTH_LONG).show()
        }
    }

    /*Now lets fetch the favourite cars from superbase based on the Car ID and display them to the user
  This method was achieved with the help of the following video:
  Shukert, T., 2023. Youtube, Getting started with Android and Supabase. [Online]
  Available at: https://www.youtube.com/watch?v=_iXUVJ6HTHU
  [Accessed 01 November 2024].*/
    private fun fetchFavCarsFromSupabase(carIds: List<Int>)
    {
        // Ensure the provided carIds list is not empty
        if (carIds.isEmpty())
        {
            Toast.makeText(context, getString(R.string.Toast89), Toast.LENGTH_SHORT).show()
            return
        }

        // Use Supabase API to fetch the complete car list
        val apiService =
            SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")
        val call = apiService.getFilteredCars(
            make = null, model = null, year = null, mileage = null,
            transmission = null, price = null, location = null, bodytype = null,
            condition = null, dealership = null, fuelType = null
        )

        call.enqueue(object : retrofit2.Callback<List<Car>> {
            override fun onResponse(
                call: Call<List<Car>>,
                response: retrofit2.Response<List<Car>>
            ) {
                if (response.isSuccessful)
                {
                    val cars = response.body()
                    carList.clear() // Clear current data
                    if (!cars.isNullOrEmpty())
                    {
                        // Filter cars by IDs in the provided carIds list
                        val filteredCars = cars.filter { carIds.contains(it.id) }
                        if (filteredCars.isNotEmpty())
                        {
                            carList.addAll(filteredCars) // Add filtered cars to the list
                            carViewModel.updateLocalDatabase(filteredCars)
                            carAdapter.notifyDataSetChanged() // Notify the adapter of data changes
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.Toast94),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else
                        {
                            Toast.makeText(context, getString(R.string.Toast93), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    else
                    {
                        Toast.makeText(context, getString(R.string.Toast2), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                else
                {
                    Toast.makeText(context, getString(R.string.Toast3), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Toast.makeText(
                    context,
                    getString(R.string.Toast4) + ": " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
