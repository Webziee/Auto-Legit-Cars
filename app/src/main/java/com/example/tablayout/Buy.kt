package com.example.tablayout

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
import fragment_book_test_drive
import retrofit2.Call
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
    private lateinit var makeList: MutableList<String>
    private lateinit var modelList: MutableList<String>
    private lateinit var makeAdapter: ArrayAdapter<String>
    private lateinit var modelAdapter: ArrayAdapter<String>

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
        modelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, modelList)
        makeSpinner.adapter = makeAdapter
        modelSpinner.adapter = modelAdapter

        // Fetch car makes and populate the spinner
        fetchCarMakes()

        // Handle Make selection and update Model spinner based on selected make
        makeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    val selectedMake = makeList[position]
                    fetchCarModels(selectedMake)
                } else {
                    modelList.clear()
                    modelList.add("Select Model")
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

        // Initialize Test Drive Button (inside RecyclerView car items)
        // Assuming you're handling it inside the adapter, add a listener to handle test drive booking
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
                Toast.makeText(requireContext(), "Please select a car to book a test drive", Toast.LENGTH_SHORT).show()
            }
        }


        // Fetch default car list from Supabase
        fetchCarsFromSupabase()

        return view
    }


    private fun resetFilters() {
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
        fetchCarsFromSupabase()
    }

    private fun fetchCarsFromSupabase() {
        // Use Supabase API to fetch car list
        val apiService = SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")
        val call = apiService.getFilteredCars(
            make = null, model = null, year = null, mileage = null,
            transmission = null, price = null, location = null, bodytype = null,
            condition = null, dealership = null, fuelType = null
        )

        call.enqueue(object : retrofit2.Callback<List<Car>> {
            override fun onResponse(call: Call<List<Car>>, response: retrofit2.Response<List<Car>>) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    carList.clear()
                    if (cars != null) {
                        // Filter out cars where the dealership is "Private"
                        val filteredCars = cars.filter { car -> car.dealership != "Private" }
                        if (filteredCars.isNotEmpty()) {
                            carList.addAll(filteredCars)
                        } else {
                            Toast.makeText(context, "No cars available to display", Toast.LENGTH_SHORT).show()
                        }
                    }
                    carAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "Failed to load cars", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Toast.makeText(context, "Error fetching cars: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fetch unique car makes from Supabase
    private fun fetchCarMakes() {
        val apiService = SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")

        val call = apiService.getFilteredCars(null, null, null, null, null, null, null, null, null, null, null)

        call.enqueue(object : retrofit2.Callback<List<Car>> {
            override fun onResponse(call: Call<List<Car>>, response: retrofit2.Response<List<Car>>) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    if (cars != null) {
                        makeList.clear()
                        makeList.add("Select Make") // Add default option

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
        val apiService = SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")

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
            override fun onResponse(call: Call<List<Car>>, response: retrofit2.Response<List<Car>>) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    if (cars != null) {
                        modelList.clear()
                        modelList.add("Select Model") // Add default option

                        // Extract distinct models for the selected make
                        val uniqueModels = cars.map { it.model }.distinct()  // Get unique models for the selected make
                        modelList.addAll(uniqueModels)
                        modelAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "No models found for the selected make", Toast.LENGTH_SHORT).show()
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
        // Create a map for query parameters
        val filters = mutableMapOf<String, String?>()

        // Get selected make and model
        val selectedMake = makeSpinner.selectedItem.toString()
        val selectedModel = modelSpinner.selectedItem.toString()

        // Add make and model to filters if selected
        if (selectedMake != "Select Make") {
            filters["make"] = "eq.$selectedMake"
        }
        if (selectedModel != "Select Model") {
            filters["model"] = "eq.$selectedModel"
        }

        // Get other filter criteria
        val minPrice = minPriceSpinner.selectedItem.toString().toDoubleOrNull()
        val maxPrice = maxPriceSpinner.selectedItem.toString().toDoubleOrNull()
        val location = locationSpinner.selectedItem.toString()
        val bodytype = bodyTypeSpinner.selectedItem.toString()
        val minYear = minYearSpinner.selectedItem.toString().toIntOrNull()
        val maxYear = maxYearSpinner.selectedItem.toString().toIntOrNull()
        val transmission = transmissionSpinner.selectedItem.toString()
        val maxMileage = maxMileageSpinner.selectedItem.toString().toIntOrNull()

        // Add year range to filters
        if (minYear != null) {
            filters["year"] = "gte.$minYear"  // Greater than or equal to min year
        }
        if (maxYear != null) {
            filters["year"] = "lte.$maxYear"  // Less than or equal to max year
        }

        // Add price range to filters
        if (minPrice != null) {
            filters["price"] = "gte.$minPrice"  // Greater than or equal to min price
        }
        if (maxPrice != null) {
            filters["price"] = "lte.$maxPrice"  // Less than or equal to max price
        }

        // Add transmission, location, bodytype to filters if selected
        if (transmission.isNotEmpty()) {
            filters["transmission"] = "eq.$transmission"
        }
        if (location.isNotEmpty()) {
            filters["location"] = "eq.$location"
        }
        if (bodytype.isNotEmpty()) {
            filters["bodytype"] = "eq.$bodytype"
        }

        // Initialize Retrofit API service for Supabase
        val apiService = SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")

        // Call the API with dynamic filters
        val call = apiService.getFilteredCars(
            make = filters["make"],
            model = filters["model"],
            year = filters["year"],
            mileage = maxMileage?.let { "lte.$it" },
            transmission = filters["transmission"],
            price = filters["price"],
            location = filters["location"],
            bodytype = filters["bodytype"],
            condition = null,
            dealership = null,
            fuelType = null
        )

        // Handle the API response
        call.enqueue(object : retrofit2.Callback<List<Car>> {
            override fun onResponse(call: Call<List<Car>>, response: retrofit2.Response<List<Car>>) {
                if (response.isSuccessful) {
                    val filteredCars = response.body()
                    carList.clear()
                    if (!filteredCars.isNullOrEmpty()) {
                        carList.addAll(filteredCars)
                        carAdapter.notifyDataSetChanged()
                        Toast.makeText(context, "${filteredCars.size} car(s) found", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No cars found with the given filters", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to retrieve data from API", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Toast.makeText(context, "Search failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
}
