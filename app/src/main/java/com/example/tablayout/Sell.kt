package com.example.tablayout

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Response
import java.util.UUID

/**
 * Sell Fragment to handle car listings.
 *
 * This code allows users to upload car details, images, and save them to Supabase via a Retrofit API.
 * It also integrates Firebase Storage for handling image uploads.
 *
 * The following code structure has been adapted based on various tutorials and resources, including:
 *
 * - Supabase Documentation: https://supabase.com/docs
 * - Retrofit Documentation: https://square.github.io/retrofit/
 * - Firebase Storage Documentation: https://firebase.google.com/docs/storage
 * - Picasso Documentation: https://square.github.io/picasso/
 *
 * The UI elements and interaction handling, such as spinners for car details and image upload workflows,
 * were built to align with best practices for Android development.
 *
 * Additional concepts on working with Kotlin Fragments, RecyclerViews, and uploading images to Firebase
 * were drawn from:
 *
 * - Android Developers: https://developer.android.com/docs
 * - StackOverflow contributions on handling multiple images and spinners.
 */

class Sell : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImages: MutableList<Uri> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var mainImageView: ImageView
    private lateinit var progressBar: ProgressBar
    private var mainImageUri: Uri? = null

    val getContent: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null && uris.isNotEmpty()) {
            selectedImages.clear()
            selectedImages.addAll(uris)
            imageAdapter.updateImageList(selectedImages)
            displaySelectedImages()

            // Optionally, set the first image as the main image if no main image is selected yet
            if (mainImageUri == null && selectedImages.isNotEmpty()) {
                mainImageUri = selectedImages[0]
                displayMainImage(mainImageUri!!)
            }
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

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        mainImageView = view.findViewById(R.id.imageContainer) // Ensure you have the right ID
        progressBar = view.findViewById(R.id.progress_bar)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        imageAdapter = ImageAdapter(selectedImages) { selectedUri ->
            mainImageUri = selectedUri
            displayMainImage(selectedUri)
            showCustomToast("Main image selected", R.drawable.success)
        }

        recyclerView.adapter = imageAdapter

        view.findViewById<Button>(R.id.btnPickImages).setOnClickListener {
            getContent.launch("image/*")
        }

        view.findViewById<Button>(R.id.sell_button).setOnClickListener {
            showLoading(true)
            saveCarData()
        }

        setupSpinners(view)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupSpinners(view: View) {
        val carMakes = arrayOf(
            "Car Make",
            "Audi", "BMW", "Mercedes", "Ford", "Toyota",
            "Honda", "Chevrolet", "Volkswagen", "Nissan", "Hyundai",
            "Kia", "Mazda", "Lexus", "Jaguar", "Volvo",
            "Subaru", "Mitsubishi", "Land Rover", "Porsche", "Tesla"
        )

        val modelsMap = mapOf(
            "Car Make" to arrayOf("Car Model"),
            "Audi" to arrayOf("A4", "Q7", "R8", "A3", "Q5", "RSQ8"),
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
            "Porsche" to arrayOf("911", "Cayenne", "Macan", "Panamera", "Taycan", "GT3rs"),
            "Tesla" to arrayOf("Model S", "Model X", "Model 3", "Model Y", "Cybertruck")
        )

        val years = arrayOf("Year","2024", "2023", "2022", "2021", "2020", "2019", "2018", "2017", "2016", "2015")
        val transmissions = arrayOf("Transmission","Automatic", "Manual", "CVT", "Dual-clutch")
        val fuelTypes = arrayOf("Fuel Type","Petrol", "Diesel", "Electric", "Hybrid")
        val bodyTypes = arrayOf("Body Type","Sedan", "SUV", "Hatchback", "Coupe", "Convertible", "Wagon", "Truck")
        val conditions = arrayOf("Condition","New", "Used", "Certified Pre-Owned", "Salvage")
        val dealership = arrayOf("Dealership","Legit Auto Cars")

        val makeSpinner: Spinner = view.findViewById(R.id.sell_car_make)
        val modelSpinner: Spinner = view.findViewById(R.id.sell_car_model)
        val yearSpinner: Spinner = view.findViewById(R.id.sell_car_year)
        val transmissionSpinner: Spinner = view.findViewById(R.id.sell_car_transmission)
        val fuelTypeSpinner: Spinner = view.findViewById(R.id.sell_car_fuel_type)
        val bodyTypeSpinner: Spinner = view.findViewById(R.id.sell_car_body_type)
        val conditionSpinner: Spinner = view.findViewById(R.id.sell_car_condition)
        val dealershipSpinner: Spinner = view.findViewById(R.id.sell_dealership)

        setupSpinner(makeSpinner, carMakes)
        setupSpinner(modelSpinner, modelsMap[makeSpinner.selectedItem.toString()] ?: arrayOf())
        setupSpinner(yearSpinner, years)
        setupSpinner(transmissionSpinner, transmissions)
        setupSpinner(fuelTypeSpinner, fuelTypes)
        setupSpinner(bodyTypeSpinner, bodyTypes)
        setupSpinner(conditionSpinner, conditions)
        setupSpinner(dealershipSpinner, dealership)

        makeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedMake = carMakes[position]
//                if(selectedMake == "Car Make") {
//                    showCustomToast("Please select a car make", R.drawable.success)
//                }
                val modelAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.custom_spinner_item,
                    modelsMap[selectedMake] ?: arrayOf()
                )
                modelAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
                modelSpinner.adapter = modelAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSpinner(spinner: Spinner, items: Array<String>) {
        spinner.adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, items).apply {
            setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            spinner.setPopupBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.rounded_dropdown_background)
            )
            spinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.spinner_border)

        }
    }

    private fun displayMainImage(uri: Uri) {
        Picasso.get()
            .load(uri)
            .error(R.drawable.error)
            .into(mainImageView)
    }

    private fun displaySelectedImages() {
        // This method now only updates the RecyclerView
        imageAdapter.notifyDataSetChanged()
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri, onComplete: (String?) -> Unit) {
        Log.d("UploadStart", "Starting upload for URI: $imageUri")

        val storageReference = FirebaseStorage.getInstance().reference.child("car_images/${UUID.randomUUID()}")
        Log.d("StoragePath", "Storage path is: $storageReference")

        val uploadTask = storageReference.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            Log.d("UploadSuccess", "Upload successful")
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                Log.d("DownloadUrl", "Download URL: $uri")
                onComplete(uri.toString())
            }
        }.addOnFailureListener { e ->
            Log.e("UploadError", "Failed to upload image", e)
            onComplete(null)
        }
    }

    private fun validateSpinners(): Boolean {
        val makeSpinner: Spinner = view?.findViewById(R.id.sell_car_make) ?: return false
        val modelSpinner: Spinner = view?.findViewById(R.id.sell_car_model) ?: return false
        val yearSpinner: Spinner = view?.findViewById(R.id.sell_car_year) ?: return false
        val transmissionSpinner: Spinner = view?.findViewById(R.id.sell_car_transmission) ?: return false
        val fuelTypeSpinner: Spinner = view?.findViewById(R.id.sell_car_fuel_type) ?: return false
        val bodyTypeSpinner: Spinner = view?.findViewById(R.id.sell_car_body_type) ?: return false
        val conditionSpinner: Spinner = view?.findViewById(R.id.sell_car_condition) ?: return false
        val dealershipSpinner : Spinner = view?.findViewById(R.id.sell_dealership) ?: return false

        if (makeSpinner.selectedItem == "Car Make") {
            showCustomToast("Please select a car make", R.drawable.error)
            return false
        }

        if (modelSpinner.selectedItem == "Car Model") {
            showCustomToast("Please select a car model", R.drawable.error)
            return false
        }

        if (yearSpinner.selectedItem == "Year") {
            showCustomToast("Please select a year", R.drawable.error)
            return false
        }

        if (transmissionSpinner.selectedItem == "Transmission") {
            showCustomToast("Please select a transmission", R.drawable.error)
            return false
        }

        if (fuelTypeSpinner.selectedItem == "Fuel Type") {
            showCustomToast("Please select a fuel type", R.drawable.error)
            return false
        }

        if (bodyTypeSpinner.selectedItem == "Body Type") {
            showCustomToast("Please select a body type", R.drawable.error)
            return false
        }

        if (conditionSpinner.selectedItem == "Condition") {
            showCustomToast("Please select a condition", R.drawable.error)
            return false
        }
        if (dealershipSpinner.selectedItem == "Dealership") {
            showCustomToast("Please select a Dealership", R.drawable.error)
            return false
        }


        return true
    }

    private fun saveCarData() {
        showLoading(true)

        if (!validateSpinners()) {
            showLoading(false)
            return
        }

        val make = view?.findViewById<Spinner>(R.id.sell_car_make)?.selectedItem.toString()
        val model = view?.findViewById<Spinner>(R.id.sell_car_model)?.selectedItem.toString()
        val year = view?.findViewById<Spinner>(R.id.sell_car_year)?.selectedItem.toString()
        val transmission =
            view?.findViewById<Spinner>(R.id.sell_car_transmission)?.selectedItem.toString()
        val fuelType = view?.findViewById<Spinner>(R.id.sell_car_fuel_type)?.selectedItem.toString()
        val bodyType = view?.findViewById<Spinner>(R.id.sell_car_body_type)?.selectedItem.toString()
        val condition =
            view?.findViewById<Spinner>(R.id.sell_car_condition)?.selectedItem.toString()
        val mileage =
            view?.findViewById<EditText>(R.id.sell_mileage)?.text.toString().toIntOrNull() ?: 0
        val location = view?.findViewById<EditText>(R.id.sell_location)?.text.toString()
        val dealership = view?.findViewById<Spinner>(R.id.sell_dealership)?.selectedItem.toString()
        val price =
            view?.findViewById<EditText>(R.id.sell_price)?.text.toString().toIntOrNull() ?: 0
        val newPrice = (price * 0.10) + price


        if (mainImageUri == null || selectedImages.isEmpty()) {
            Toast.makeText(requireContext(), "Please select images", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }

        // Upload main image and get URL
        uploadImageToFirebaseStorage(mainImageUri!!) { mainImageUrl ->
            if (mainImageUrl == null) {
                Toast.makeText(requireContext(), "Failed to upload main image", Toast.LENGTH_SHORT)
                    .show()
                showLoading(false)
                return@uploadImageToFirebaseStorage
            }

            // Upload additional images and get URLs
            val imageUrls = mutableListOf<String>()
            val uploadTasks = selectedImages.map { imageUri ->
                val taskCompletionSource = TaskCompletionSource<String>()
                uploadImageToFirebaseStorage(imageUri) { imageUrl ->
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl)
                    }
                    taskCompletionSource.setResult(imageUrl)
                }
                taskCompletionSource.task
            }

            Tasks.whenAllSuccess<String>(uploadTasks).addOnSuccessListener {
                // Create Car object for Supabase
                val car = Car(
                    make = make,
                    model = model,
                    year = year.toInt(),
                    transmission = transmission,
                    fueltype = fuelType,
                    bodytype = bodyType,
                    condition = condition,
                    mileage = mileage.toInt(),
                    dealership = dealership,
                    location = location,
                    price = newPrice.toInt(),
                    maincarimage = mainImageUrl,
                    imageresourcelist = imageUrls,
                    title = "$make $model"  // Combined make and model as title
                )

                // Save car data to Supabase
                val apiService =
                    SupabaseUtils.RetrofitClient.getApiService("https://odbddwdwklhebnvgvwlv.supabase.co")
                val call = apiService.addCarToSupabase(car)

                call.enqueue(object : retrofit2.Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            showCustomToast("Car listed successfully", R.drawable.success)
                            showLoading(false)
                            if (isAdded && !isRemoving) {
                                clearForm()
                            }
                        } else {
                            showCustomToast("Failed to list car", R.drawable.error)
                            showLoading(false)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("SellFragment", "Error listing car", t)
                        Toast.makeText(
                            requireContext(),
                            "Error: ${t.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                        showLoading(false)
                    }
                })
            }.addOnFailureListener { e ->
                Log.e("UploadError", "Failed to upload images", e)
                Toast.makeText(requireContext(), "Failed to upload images", Toast.LENGTH_SHORT)
                    .show()
                showLoading(false)
            }

        }
    }

    private fun clearForm() {
        try {
            view?.findViewById<Spinner>(R.id.sell_car_make)?.setSelection(0)
            view?.findViewById<Spinner>(R.id.sell_car_model)?.setSelection(0)
            view?.findViewById<Spinner>(R.id.sell_car_year)?.setSelection(0)
            view?.findViewById<Spinner>(R.id.sell_car_transmission)?.setSelection(0)
            view?.findViewById<Spinner>(R.id.sell_car_fuel_type)?.setSelection(0)
            view?.findViewById<Spinner>(R.id.sell_car_body_type)?.setSelection(0)
            view?.findViewById<Spinner>(R.id.sell_car_condition)?.setSelection(0)
            view?.findViewById<EditText>(R.id.sell_mileage)?.text?.clear()
            view?.findViewById<Spinner>(R.id.sell_dealership)?.setSelection(0)
            view?.findViewById<EditText>(R.id.sell_price)?.text?.clear()
            view?.findViewById<EditText>(R.id.sell_location)?.text?.clear()

            selectedImages.clear()
            recyclerView.adapter?.notifyDataSetChanged()

            mainImageView.setImageDrawable(null)

            Log.d("ClearForm", "Form cleared successfully")
        } catch (e: Exception) {
            Log.e("ClearFormError", "Error clearing form: ${e.localizedMessage}", e)
            Toast.makeText(requireContext(), "Error clearing form", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCustomToast(message: String, iconResId: Int) {
        val inflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.customtoast, view?.findViewById(R.id.custom_toast_root))

        val toastMessage: TextView = layout.findViewById(R.id.toast_message)
        val toastIcon: ImageView = layout.findViewById(R.id.toast_icon)

        toastMessage.text = message
        toastIcon.setImageResource(iconResId)

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }
}
