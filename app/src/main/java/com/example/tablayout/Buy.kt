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
    private lateinit var recyclerView: RecyclerView
    private lateinit var paginationLayout: LinearLayout

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

        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view)
        paginationLayout = view.findViewById(R.id.pagination_layout)

        carAdapter = CarAdapter(carList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = carAdapter
        recyclerView.isNestedScrollingEnabled = false

        // Set up pagination
        setupPagination()

        // Set up spinners
        setupSpinner(view.findViewById(R.id.max_price), R.array.max_price_items)
        setupSpinner(view.findViewById(R.id.min_price), R.array.min_price_items)
        setupSpinner(view.findViewById(R.id.location), R.array.location_items)
        setupSpinner(view.findViewById(R.id.body_type), R.array.body_type_items)
        setupSpinner(view.findViewById(R.id.min_year), R.array.min_year_items)
        setupSpinner(view.findViewById(R.id.max_year), R.array.max_year_items)
        setupSpinner(view.findViewById(R.id.transmission), R.array.car_transmission)
        setupSpinner(view.findViewById(R.id.max_mileage), R.array.max_mileage_items)

        return view
    }

    private fun setupPagination() {
        fetchCarsFromFirestore {
            val pageSize = 10
            val totalPages = Math.ceil(carList.size / pageSize.toDouble()).toInt()
            if (totalPages > 1) {
                createPaginationButtons(totalPages, pageSize)
            }
            carAdapter.updateData(getCarsForPage(1, pageSize))
        }
    }

    private fun getCarsForPage(page: Int, pageSize: Int): List<Car> {
        val start = (page - 1) * pageSize
        val end = Math.min(start + pageSize, carList.size)
        return carList.subList(start, end)
    }

    private fun createPaginationButtons(totalPages: Int, pageSize: Int) {
        paginationLayout.removeAllViews()

        for (i in 1..totalPages) {
            val button = Button(requireContext()).apply {
                text = i.toString()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
             //   setBackgroundResource(R.drawable.pagination_button_bg) // Add a background resource for better UI
            }

            button.setOnClickListener {
                carAdapter.updateData(getCarsForPage(i, pageSize))
                recyclerView.scrollToPosition(0)
            }

            paginationLayout.addView(button)
        }
    }

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

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    spinner.setSelection(0)
                } else {
                    val selectedItem = parent.getItemAtPosition(position).toString()
                    // Perform actions with selectedItem if needed
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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
