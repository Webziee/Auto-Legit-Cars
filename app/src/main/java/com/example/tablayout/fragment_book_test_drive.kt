import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
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
                Toast.makeText(requireContext(), "Car ID not available", Toast.LENGTH_SHORT).show()
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
        val whatsappMSG = "Good Day Sir/Madam,\nI am interested in your $car which I saw for sale on the app.\nWould you please provide me with more details on the vehicle.\n~ $email"

        // Set onClickListener to open WhatsApp with the defined phone number and message
        whatsappIcon.setOnClickListener {
            openWhatsappMessage(phone, whatsappMSG) // Pass phone number and message
        }

        return view
    }

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
            Toast.makeText(requireContext(), "Please Select A Booking Date And Time!", Toast.LENGTH_LONG).show()
        } else if (loggedInEmail?.email.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User Email Not Available!", Toast.LENGTH_LONG).show()
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
                            Toast.makeText(requireContext(), "Booking Created Successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to create booking", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error Creating Booking: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "User is not logged in!", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "WhatsApp is not installed on your device", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error: Unable to open WhatsApp", Toast.LENGTH_SHORT).show()
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
