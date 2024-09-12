package com.example.tablayout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Buy : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

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

        val view = inflater.inflate(com.example.tablayout.R.layout.fragment_buy, container, false)

        // Function to set up spinner with its adapter and background
        fun setupSpinner(spinner: Spinner, arrayId: Int) {
            val adapter = ArrayAdapter.createFromResource(
                requireContext(),
                arrayId,
                com.example.tablayout.R.layout.custom_spinner_item
            )
            adapter.setDropDownViewResource(com.example.tablayout.R.layout.custom_spinner_dropdown_item)
            spinner.adapter = adapter
//            spinner.background = ContextCompat.getDrawable(requireContext(), com.example.tablayout.R.drawable.spinner_border)

            // Apply custom background for the Spinner dropdown
            spinner.setPopupBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.rounded_dropdown_background)
            )

            spinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.spinner_border)  // Spinner border


            // Set listener to check if placeholder is selected
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        // This is the placeholder item, reset selection to the placeholder
                        spinner.setSelection(0)
                    } else {
                        // Handle valid selection
                        val selectedItem = parent.getItemAtPosition(position).toString()
                        // Perform actions with selectedItem if needed
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Optional: Handle case when nothing is selected
                }
            }
        }

        // max price spinner
        val maxspinner: Spinner = view.findViewById(com.example.tablayout.R.id.max_price)
        setupSpinner(maxspinner, com.example.tablayout.R.array.max_price_items)

        // min price spinner
        val minspinner: Spinner = view.findViewById(com.example.tablayout.R.id.min_price)
        setupSpinner(minspinner, com.example.tablayout.R.array.min_price_items)

        // location spinner
        val locationspinner: Spinner = view.findViewById(com.example.tablayout.R.id.location)
        setupSpinner(locationspinner, com.example.tablayout.R.array.location_items)

        // body type spinner
        val bodytypespinner: Spinner = view.findViewById(com.example.tablayout.R.id.body_type)
        setupSpinner(bodytypespinner, com.example.tablayout.R.array.body_type_items)

        // min year spinner
        val minyearspinner: Spinner = view.findViewById(com.example.tablayout.R.id.min_year)
        setupSpinner(minyearspinner, com.example.tablayout.R.array.min_year_items)

        // max year spinner
        val maxyearspinner: Spinner = view.findViewById(com.example.tablayout.R.id.max_year)
        setupSpinner(maxyearspinner, com.example.tablayout.R.array.max_year_items)

        // transmisson spinner
        val transmissonspinner: Spinner = view.findViewById(com.example.tablayout.R.id.transmission)
        setupSpinner(transmissonspinner, com.example.tablayout.R.array.transmisson_items)

        // mileage spinner
        val mileagespinner: Spinner = view.findViewById(com.example.tablayout.R.id.max_mileage)
        setupSpinner(mileagespinner, com.example.tablayout.R.array.max_mileage_items)

        return view
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
