package com.example.tablayout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Buy : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var firestore: FirebaseFirestore
    private val carList = mutableListOf<Car>()
    private lateinit var carAdapter: CarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_buy, container, false)

        // Find the button by its ID
        val searchButton: Button = view.findViewById(R.id.btnSignOut)

        firestore = FirebaseFirestore.getInstance()

        // Set the OnClickListener for the button
        searchButton.setOnClickListener {


        }
        // Fetch data from Firestore
        fetchCarsFromFirestore()

        fun setupSpinner(spinner: Spinner, arrayId: Int) {
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

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        spinner.setSelection(0)
                    } else {
                        val selectedItem = parent.getItemAtPosition(position).toString()
                        // Perform actions with selectedItem if needed
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
        }

        setupSpinner(view.findViewById(R.id.max_price), R.array.max_price_items)
        setupSpinner(view.findViewById(R.id.min_price), R.array.min_price_items)
        setupSpinner(view.findViewById(R.id.location), R.array.location_items)
        setupSpinner(view.findViewById(R.id.body_type), R.array.body_type_items)
        setupSpinner(view.findViewById(R.id.min_year), R.array.min_year_items)
        setupSpinner(view.findViewById(R.id.max_year), R.array.max_year_items)
        setupSpinner(view.findViewById(R.id.transmission), R.array.car_transmission)
        setupSpinner(view.findViewById(R.id.max_mileage), R.array.max_mileage_items)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        val paginationLayout: LinearLayout = view.findViewById(R.id.pagination_layout)

        carAdapter = CarAdapter(carList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = carAdapter
        recyclerView.isNestedScrollingEnabled = false

        val pageSize = 10
        var currentPage = 1

        fun getCarsForPage(page: Int): List<Car> {
            val start = (page - 1) * pageSize
            val end = Math.min(start + pageSize, carList.size)
            return carList.subList(start, end)
        }

        fun createPaginationButtons(totalPages: Int) {
            paginationLayout.removeAllViews()

            for (i in 1..totalPages) {
                val button = Button(requireContext()).apply {
                    text = i.toString()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                button.setOnClickListener {
                    currentPage = i
                    carAdapter.updateData(getCarsForPage(currentPage))
                    recyclerView.scrollToPosition(0)
                }

                paginationLayout.addView(button)
            }
        }

        fetchCarsFromFirestore {
            val totalPages = Math.ceil(carList.size / pageSize.toDouble()).toInt()
            createPaginationButtons(totalPages)
            carAdapter.updateData(getCarsForPage(currentPage))
        }

        return view
    }

////manually add to database
//    private fun addSampleCar() {
//        val car = Car(
//            maincarimage = "https://img.autotrader.co.za/32490502", // URL of the main car image
//            title = "2024 BMW M3 Competition",
//            condition = "New",
//            mileage = 100,
//            transmisson = "Automatic",
//            dealership = "BMW Sandton Motors",
//            location = "Sandton, Johannesburg",
//            imageResourceList = listOf(
//                "https://img.autotrader.co.za/32490503/Crop800x600",
//                "https://img.autotrader.co.za/32490504/Crop800x600",
//                "https://img.autotrader.co.za/32490505/Crop800x600",
//                "https://img.autotrader.co.za/32490506/Crop800x600",
//                "https://img.autotrader.co.za/32490507/Crop800x600",
//                "https://img.autotrader.co.za/32490508/Crop800x600",
//                "https://img.autotrader.co.za/32490509/Crop800x600",
//            )
//        )
//
//        FirestoreUtils.addCar(car,
//            onSuccess = { documentId ->
//                // Handle success (e.g., show a toast or update the UI)
//                Log.d("BuyFragment", "Car added successfully with ID: $documentId")
//            },
//            onFailure = { exception ->
//                // Handle failure (e.g., show an error message)
//                Log.e("BuyFragment", "Failed to add car", exception)
//            }
//        )
//    }

    private fun fetchCarsFromFirestore(onComplete: () -> Unit = {}) {
        firestore.collection("cars")
            .get()
            .addOnSuccessListener { result ->
                carList.clear()
                for (document in result.documents) {
                    val car = document.toObject(Car::class.java)
                    car?.let { carList.add(it) }
                }
                carAdapter.notifyDataSetChanged()
                onComplete()
            }
            .addOnFailureListener { exception ->
                // Handle error
                exception.printStackTrace()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Buy().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
