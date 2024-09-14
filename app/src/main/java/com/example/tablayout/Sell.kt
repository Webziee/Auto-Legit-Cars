package com.example.tablayout

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.InputStream

class Sell : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUris: List<Uri> = mutableListOf() // To handle multiple images
    private lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            firestore = FirebaseFirestore.getInstance() // Initialize Firestore
            storage = FirebaseStorage.getInstance() // Initialize Firebase Storage

            getContent = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
                uris?.let {
                    imageUris = it
                    displaySelectedImages()
                }
            }
        } catch (e: Exception) {
            Log.e("SellFragment", "Error initializing Firebase", e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sell, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val carMakes = arrayOf(
            "Audi", "BMW", "Mercedes", "Ford", "Toyota",
            "Honda", "Chevrolet", "Volkswagen", "Nissan", "Hyundai",
            "Kia", "Mazda", "Lexus", "Jaguar", "Volvo",
            "Subaru", "Mitsubishi", "Land Rover", "Porsche", "Tesla"
        )

        val modelsMap = mapOf(
            "Audi" to arrayOf("A4", "Q7", "R8", "A3", "Q5"),
            "BMW" to arrayOf("X5", "3 Series", "i8", "5 Series", "X3"),
            "Mercedes" to arrayOf("C-Class", "E-Class", "S-Class", "GLA", "GLE"),
            "Ford" to arrayOf("Fiesta", "Focus", "Mustang", "Ranger", "Explorer"),
            "Toyota" to arrayOf("Corolla", "Camry", "RAV4", "Hilux", "Land Cruiser"),
            "Honda" to arrayOf("Civic", "Accord", "CR-V", "HR-V", "Jazz"),
            "Chevrolet" to arrayOf("Spark", "Cruze", "Malibu", "Equinox", "Tahoe"),
            "Volkswagen" to arrayOf("Golf", "Polo", "Tiguan", "Passat", "Jetta"),
            "Nissan" to arrayOf("Altima", "Rogue", "GT-R", "Navara", "Sentra"),
            "Hyundai" to arrayOf("Elantra", "Tucson", "Santa Fe", "i20", "Kona"),
            "Kia" to arrayOf("Seltos", "Sorento", "Sportage", "Rio", "Optima"),
            "Mazda" to arrayOf("Mazda3", "CX-5", "MX-5", "CX-3", "Mazda6"),
            "Lexus" to arrayOf("IS", "RX", "NX", "ES", "GX"),
            "Jaguar" to arrayOf("F-Pace", "XE", "XF", "F-Type", "E-Pace"),
            "Volvo" to arrayOf("XC60", "XC90", "S90", "V60", "XC40"),
            "Subaru" to arrayOf("Impreza", "Outback", "Forester", "WRX", "Legacy"),
            "Mitsubishi" to arrayOf("Pajero", "Eclipse Cross", "Outlander", "ASX", "Triton"),
            "Land Rover" to arrayOf("Range Rover", "Discovery", "Defender", "Evoque", "Velar"),
            "Porsche" to arrayOf("911", "Cayenne", "Macan", "Panamera", "Taycan"),
            "Tesla" to arrayOf("Model S", "Model X", "Model 3", "Model Y", "Cybertruck")
        )

        val years = arrayOf("2024", "2023", "2022", "2021", "2020", "2019", "2018", "2017", "2016", "2015")
        val transmissions = arrayOf("Automatic", "Manual", "CVT", "Dual-clutch")
        val fuelTypes = arrayOf("Petrol", "Diesel", "Electric", "Hybrid")
        val bodyTypes = arrayOf("Sedan", "SUV", "Hatchback", "Coupe", "Convertible", "Wagon", "Truck")
        val conditions = arrayOf("New", "Used", "Certified Pre-Owned", "Salvage")

        val makeSpinner: Spinner = view.findViewById(R.id.sell_car_make)
        val modelSpinner: Spinner = view.findViewById(R.id.sell_car_model)
        val yearSpinner: Spinner = view.findViewById(R.id.sell_car_year)
        val transmissionSpinner: Spinner = view.findViewById(R.id.sell_car_transmission)
        val fuelTypeSpinner: Spinner = view.findViewById(R.id.sell_car_fuel_type)
        val bodyTypeSpinner: Spinner = view.findViewById(R.id.sell_car_body_type)
        val conditionSpinner: Spinner = view.findViewById(R.id.sell_car_condition)
        val imageContainer: LinearLayout? = view.findViewById(R.id.imageContainer)
        val buttonUpload: Button = view.findViewById(R.id.buttonUpload)
        val buttonSell: Button = view.findViewById(R.id.sell_button)

        // Set up spinners with custom layouts
        setupSpinner(yearSpinner, years)
        setupSpinner(transmissionSpinner, transmissions)
        setupSpinner(fuelTypeSpinner, fuelTypes)
        setupSpinner(bodyTypeSpinner, bodyTypes)
        setupSpinner(conditionSpinner, conditions)

        makeSpinner.adapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_item, carMakes).apply {
                setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            }

        makeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedMake = carMakes[position]
                val modelAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.custom_spinner_item,
                    modelsMap[selectedMake] ?: arrayOf()
                )
                modelAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
                modelSpinner.adapter = modelAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle no selection case if needed
            }
        }

        buttonUpload.setOnClickListener {
            getContent.launch("image/*")
        }

        buttonSell.setOnClickListener {
            val make = makeSpinner.selectedItem.toString()
            val model = modelSpinner.selectedItem.toString()
            val year = yearSpinner.selectedItem.toString()
            val transmission = transmissionSpinner.selectedItem.toString()
            val fuelType = fuelTypeSpinner.selectedItem.toString()
            val bodyType = bodyTypeSpinner.selectedItem.toString()
            val condition = conditionSpinner.selectedItem.toString()

            if (make.isEmpty() || model.isEmpty() || year.isEmpty() || transmission.isEmpty() ||
                fuelType.isEmpty() || bodyType.isEmpty() || condition.isEmpty() || imageUris.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Please fill all fields and select images", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convert selected images to Base64 strings
            val base64Images = imageUris.mapNotNull { uri ->
                convertImageUriToBase64(uri)
            }

            val car = mapOf(
                "make" to make,
                "model" to model,
                "year" to year,
                "transmission" to transmission,
                "fuelType" to fuelType,
                "bodyType" to bodyType,
                "condition" to condition,
                "maincarimage" to base64Images.firstOrNull(), // Use the first image as the main image
                "imageResourceList" to base64Images
            )

            firestore.collection("cars")
                .add(car)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Car listed successfully!", Toast.LENGTH_SHORT).show()
                    makeSpinner.setSelection(0)
                    modelSpinner.setSelection(0)
                    yearSpinner.setSelection(0)
                    transmissionSpinner.setSelection(0)
                    fuelTypeSpinner.setSelection(0)
                    bodyTypeSpinner.setSelection(0)
                    conditionSpinner.setSelection(0)
                    imageUris = emptyList() // Clear selected images
                    imageContainer?.removeAllViews() // Clear displayed images
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error listing car: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupSpinner(spinner: Spinner, items: Array<String>) {
        spinner.adapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_item, items).apply {
                setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            }
    }

    private fun displaySelectedImages() {
        // Clear existing images
        val imageContainer: LinearLayout? = view?.findViewById(R.id.imageContainer)
        imageContainer?.removeAllViews()

        for (uri in imageUris) {
            val imageView = ImageView(requireContext())
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            imageView.layoutParams = layoutParams
            imageContainer?.addView(imageView)

            // Load the image using the uri
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun convertImageUriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("SellFragment", "Error converting image to Base64", e)
            null
        }
    }
}
