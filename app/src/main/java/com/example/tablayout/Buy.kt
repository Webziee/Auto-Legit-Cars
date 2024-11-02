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
import com.google.firebase.firestore.FirebaseFirestore
import fragment_book_test_drive
import retrofit2.Call
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import retrofit2.Callback
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

        // Initialize ViewModel using ViewModelProvider
        carViewModel = ViewModelProvider(this).get(CarViewModel::class.java)

        // Observe local data from SQLite and update the RecyclerView
        carViewModel.allCars.observe(viewLifecycleOwner) { cars ->
            carList.clear()
            carList.addAll(cars)
            carAdapter.notifyDataSetChanged()
            Log.d("BuyFragment", "Loaded ${cars.size} cars from local database.")
        }


        //View Favourites Button Logic
        viewFavButton = view.findViewById(R.id.btnFavSearch)

        viewFavButton.setOnClickListener{
            displayFavourites()
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
            }
            else
            {
                Toast.makeText(requireContext(), getString(R.string.Toast1), Toast.LENGTH_SHORT).show()
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
        val apiService = SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")
        val call = apiService.getFilteredCars(
            make = null, model = null, year = null, mileage = null,
            transmission = null, price = null, location = null, bodytype = null,
            condition = null, dealership = null, fuelType = null
        )

        call.enqueue(object : Callback<List<Car>> {
            override fun onResponse(call: Call<List<Car>>, response: Response<List<Car>>) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    carList.clear()
                    if (!cars.isNullOrEmpty()) {
                        carList.addAll(cars)
                        carViewModel.updateLocalDatabase(cars) // Store in SQLite for offline access
                        carAdapter.notifyDataSetChanged()
                        Log.d("BuyFragment", "Fetched ${cars.size} cars from Supabase.")
                    } else {
                        Log.d("BuyFragment", "No cars received from Supabase.")
                    }
                } else {
                    Log.e("BuyFragment", "Failed to retrieve cars from Supabase.")
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Log.e("BuyFragment", "Error fetching cars from Supabase: ${t.message}")
            }
        })
    }


    // Fetch unique car makes from Supabase
    private fun fetchCarMakes() {
        val apiService = SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")
        val call = apiService.getFilteredCars(null, null, null, null, null, null, null, null, null, null, null)

        call.enqueue(object : Callback<List<Car>> {
            override fun onResponse(call: Call<List<Car>>, response: Response<List<Car>>) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    if (!cars.isNullOrEmpty()) {
                        makeList.clear()
                        makeList.add(getString(R.string.Toast84)) // Add default option
                        val uniqueMakes = cars.map { it.make }.distinct()
                        makeList.addAll(uniqueMakes)
                        makeAdapter.notifyDataSetChanged()
                    } else {
                        Log.w("BuyFragment", "No makes found from Supabase response.")
                    }
                } else {
                    Log.w("BuyFragment", "Failed to retrieve makes from Supabase.")
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Log.e("BuyFragment", "Error fetching car makes: ${t.message}")
            }
        })
    }

    // Fetch car models for the selected make from Supabase
    private fun fetchCarModels(selectedMake: String) {
        val apiService = SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")
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

        call.enqueue(object : Callback<List<Car>> {
            override fun onResponse(call: Call<List<Car>>, response: Response<List<Car>>) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    if (!cars.isNullOrEmpty()) {
                        modelList.clear()
                        modelList.add(getString(R.string.Toast85))
                        val uniqueModels = cars.map { it.model }.distinct()
                        modelList.addAll(uniqueModels)
                        modelAdapter.notifyDataSetChanged()
                    } else {
                        Log.w("BuyFragment", "No models found for make: $selectedMake")
                    }
                } else {
                    Log.w("BuyFragment", "Failed to retrieve models for make: $selectedMake")
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Log.e("BuyFragment", "Error fetching car models: ${t.message}")
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
        if (selectedMake != getString(R.string.Toast84)) {
            filters["make"] = "eq.$selectedMake"
        }
        if (selectedModel != getString(R.string.Toast85)) {
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
                        Toast.makeText(context, getString(R.string.Toast6) + filteredCars.size, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, getString(R.string.Toast7), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, getString(R.string.Toast8), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Toast.makeText(context,getString(R.string.Toast9) + t.message, Toast.LENGTH_SHORT).show()
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

    private fun displayFavourites()
    {
        //Lets first get the favourites from firebase
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        // List to store non-zero car IDs
        val carIDs = mutableListOf<Int>()

        if(userEmail != null)
        {
            val db = FirebaseFirestore.getInstance()
            db.collection("Favourites").whereEqualTo("Email", userEmail).get()
                .addOnSuccessListener { documents ->
                    for(document in documents)
                    {
                        // Retrieve each CarID and add to list if it's not zero
                        listOf(
                            document.getLong("CarID1")?.toInt() ?: 0,
                            document.getLong("CarID2")?.toInt() ?: 0,
                            document.getLong("CarID3")?.toInt() ?: 0,
                            document.getLong("CarID4")?.toInt() ?: 0,
                            document.getLong("CarID5")?.toInt() ?: 0
                        ).filter { it != 0 }.forEach { carIDs.add(it) }
                    }

                    if(carIDs.isNotEmpty())
                    {

                    }
                    else
                    {
                        Toast.makeText(requireContext(), getString(R.string.Toast93), Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener{
                    Toast.makeText(requireContext(), getString(R.string.Toast92), Toast.LENGTH_LONG).show()
                }
        }
        else
        {
            Toast.makeText(requireContext(), getString(R.string.Toast79), Toast.LENGTH_LONG).show()
        }
    }
}
