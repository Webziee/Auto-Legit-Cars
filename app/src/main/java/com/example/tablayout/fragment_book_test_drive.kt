import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.example.tablayout.Booking
import com.example.tablayout.R
import com.example.tablayout.SupabaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

// Fragment initialization parameters
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/* This fragment listens for the date and time selection */
class fragment_book_test_drive : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // Declare global lateinit variables
    private lateinit var booking_date_time_display: EditText
    private lateinit var car_selected_display: EditText
    private lateinit var booking_date_time: TextView
    private lateinit var submitButton: Button
    private lateinit var whatsappIcon: ImageView
    //Declaring my firestore db variable
    private lateinit var db:FirebaseFirestore
    private lateinit var favourites: ImageView
    private var carId: Int = 0  // Initialize carId

    private lateinit var auth: FirebaseAuth  // FirebaseAuth for user authentication

    private val calendar = Calendar.getInstance() // Calendar instance for date and time
    private val formatter = SimpleDateFormat("dd, MMM, yyyy hh:mm a", Locale.UK)  // Date formatter
    private val handleNullCarTitle = "No Car Selected!!"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the fragment
        val view = inflater.inflate(R.layout.fragment_book_test_drive, container, false)

        // Retrieve arguments passed from the previous fragment
        carId = arguments?.getInt("car_id", 0) ?: 0
        val carTitle = arguments?.getString("carTitle")

        // Initialize views
        booking_date_time_display = view.findViewById(R.id.booking_date_time)
        booking_date_time = view.findViewById(R.id.select_date_time_textview)
        submitButton = view.findViewById(R.id.book_test_drive)
        car_selected_display = view.findViewById(R.id.booking_car_selected)
        whatsappIcon = view.findViewById(R.id.whatsapp_icon)
        favourites = view.findViewById(R.id.favourites)

        // Display the car title, or a fallback message if not available
        if (carTitle.isNullOrBlank()) {
            car_selected_display.setText(handleNullCarTitle)
        } else {
            car_selected_display.setText(carTitle)
        }

        booking_date_time.setOnClickListener {
            showDatePickerDialog()
        }

        submitButton.setOnClickListener {
            if (carId != 0) {
                submitBooking(carId)  // Pass car_id to booking function
            } else {
                Toast.makeText(requireContext(), getString(R.string.Toast10), Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize FirebaseAuth
        try {
            auth = FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("Book_Test_Drive Fragment", "Error initializing Firebase", e)
        }

        // Prepare WhatsApp message
        val car = car_selected_display.text.toString()
        val loggedInEmail: FirebaseUser? = auth.currentUser
        val email = loggedInEmail?.email
        val phone = "0812420589" // Define the phone number properly
        val whatsappMSG = "Good Day Legit Auto Cars,\nI am interested in your $car which I saw for sale on the app.\nWould you please provide me with more details on the vehicle.\n~ $email"

        // Set onClickListener to open WhatsApp with the defined phone number and message
        whatsappIcon.setOnClickListener {
            openWhatsappMessage(phone, whatsappMSG) // Pass phone number and message
        }

        // Now we are going to Initialize FirebaseAuth (Obregon, 2023)
        auth = FirebaseAuth.getInstance()

        //Initialise firestore (Obregon, 2023)
        db = FirebaseFirestore.getInstance()

        //Before allowing user to add favourites first handle issues with regards to them having the collection or if their document is full
        handleEmptyFavourites()

        //Set an onclick for the favourites icon and call the addtofavourites method
        favourites.setOnClickListener{
            addToFavourites(carId)
        }
        return view
    }
    // end

    private fun showDatePickerDialog() {
        // Launch date picker dialog
        DatePickerDialog(requireContext(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(year, month, dayOfMonth)
        displayFormattedDate(calendar.timeInMillis)
        // Show time picker after date is selected
        TimePickerDialog(requireContext(), this, calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), false).show()
    }

    private fun displayFormattedDate(timestamp: Long) {
        // Format and display the selected date and time
        booking_date_time_display.setText(formatter.format(timestamp))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        displayFormattedDate(calendar.timeInMillis)
    }

    private fun submitBooking(carId: Int) {
        val dateTime = booking_date_time_display.text.toString()
        val loggedInEmail: FirebaseUser? = auth.currentUser

        if (dateTime.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.Toast11), Toast.LENGTH_LONG).show()
        } else if (loggedInEmail?.email.isNullOrEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.Toast12), Toast.LENGTH_LONG).show()
        } else {
            val email = loggedInEmail?.email

            if (email != null) {
                val booking = Booking(
                    car_id = carId,
                    date_time = dateTime,
                    user_email = email
                )

                val apiService = SupabaseUtils.RetrofitClient.getApiService("https://your-supabase-url.supabase.co")
                apiService.createBooking(booking).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), getString(R.string.Toast13), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.Toast14), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(requireContext(), getString(R.string.Toast15) + t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), getString(R.string.Toast16), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openWhatsappMessage(phoneNumber: String, message: String) {
        val packageManager = requireContext().packageManager
        try {
            // Format phone number (remove leading 0 and replace it with country code)
            val formatNumber = phoneNumber.removePrefix("0")
            val encodedMessage = Uri.encode(message) // Encode the message to handle special characters
            val url = "https://wa.me/27$formatNumber?text=$encodedMessage" // Correct URL format with dynamic values

            // Check if either WhatsApp or WhatsApp Business is installed
            val whatsappInstalled = isAppInstalled(packageManager, "com.whatsapp") ||
                    isAppInstalled(packageManager, "com.whatsapp.w4b")

            if (!whatsappInstalled) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url) // Set the correct WhatsApp URL
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), getString(R.string.Toast17), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), getString(R.string.Toast18), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAppInstalled(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /*The following method checks to see how many favourite slots the current user has open and displays toast messages
      accordingly it also creating default vals if no items found, this firebase manipulation code comes from the following video:
      Risky, A., 2019. Youtube, Add and Display Data Firestore — Kotlin Android Studio tutorial — Part 2. [Online]
      Available at: https://www.youtube.com/watch?v=7fkXdfaMRPw
      [Accessed 12 October 2024].*/
    private fun handleEmptyFavourites()
    {
        val loggedInEmail = auth.currentUser?.email
        if (loggedInEmail != null)
        {
            // Referencing the Favourites collection in Firestore
            val favouritesRef = db.collection("Favourites")

            // querying the collection to find the document that matches the user's email
            favouritesRef.whereEqualTo("Email", loggedInEmail).get()
                .addOnSuccessListener { documents ->
                    // Checking if the document exists
                    if (!documents.isEmpty) {
                        // Search the document to see if the user has added any favourites
                        val document = documents.first()
                        var count = 0

                        // creating a list of car ID fields to check
                        val carIDFields = listOf("CarID1", "CarID2", "CarID3", "CarID4", "CarID5")
                        carIDFields.forEach { field ->
                            // get the car ID from the document
                            val carID = document.getLong(field)
                            // count the number of non-empty car IDs
                            if (carID != null && carID != 0L) {
                                count++
                            }
                        }

                        if (count == 5)
                        {
                            Toast.makeText(requireContext(), getString(R.string.Toast87), Toast.LENGTH_LONG).show()
                        }
                        else
                        {
                            Toast.makeText(requireContext(), getString(R.string.Toast88) + count, Toast.LENGTH_LONG).show()
                        }
                    }
                    else
                    {
                        // else Create a default favourite document if none exists for the user
                        val defaultFav = hashMapOf(
                            "Email" to loggedInEmail,
                            "CarID1" to 0,
                            "CarID2" to 0,
                            "CarID3" to 0,
                            "CarID4" to 0,
                            "CarID5" to 0
                        )
                        // adding the default favourites document to Firestore
                        favouritesRef.add(defaultFav)
                            .addOnSuccessListener {
                                Log.d("Favourites", "Default favourites document created.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Favourites", "Error creating default favourites document", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), getString(R.string.Toast73), Toast.LENGTH_LONG).show()
                }
        }
        else
        {
            Toast.makeText(requireContext(), getString(R.string.Toast79), Toast.LENGTH_LONG).show()
        }
    }

    /*The following method adds the current car to the users favourites collection, but checks are done first
     to see if their is space to add a favourite. This firebase manipulation code comes from the following video:
      Risky, A., 2019. Youtube, Add and Display Data Firestore — Kotlin Android Studio tutorial — Part 2. [Online]
      Available at: https://www.youtube.com/watch?v=7fkXdfaMRPw
      [Accessed 12 October 2024].*/
    private fun addToFavourites(carID: Int?)
    {
        if (carID == null || carID == 0)
        {  // check for null or 0, meaning no car id found
            Toast.makeText(requireContext(), getString(R.string.Toast89), Toast.LENGTH_LONG).show()
            return
        }

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: run{
            Toast.makeText(requireContext(), getString(R.string.Toast79), Toast.LENGTH_LONG).show()
            return
        }

        val favRef = FirebaseFirestore.getInstance().collection("Favourites")

        // Query for the document where Email matches userEmail
        favRef.whereEqualTo("Email", userEmail).get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    // Find the first empty CarID slot
                    val carSlots = listOf("CarID1", "CarID2", "CarID3", "CarID4", "CarID5")
                    val emptySlot = carSlots.firstOrNull { document.getLong(it) == 0L } // Check for zero

                    if (emptySlot != null)
                    {
                        // Update the document with the car ID in the first available slot
                        favRef.document(document.id).update(emptySlot, carID.toLong()) // Convert carID to Long
                            .addOnSuccessListener {
                                //Show message on Success
                                Toast.makeText(requireContext(), getString(R.string.Toast90), Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { e ->
                                //show message on failure
                                Toast.makeText(requireContext(), getString(R.string.Toast91), Toast.LENGTH_LONG).show()
                            }
                    }
                    else
                    {
                        Toast.makeText(requireContext(), getString(R.string.Toast87), Toast.LENGTH_LONG).show()
                    }
                }
                else
                {
                    Toast.makeText(requireContext(), getString(R.string.Toast72), Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), getString(R.string.Toast72), Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragment_book_test_drive().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
