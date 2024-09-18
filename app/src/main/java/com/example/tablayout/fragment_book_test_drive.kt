package com.example.tablayout

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.text.format.Time
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
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.SimpleFormatter

/*The following date picker code was taken from teachings by Domenic
Domenic, 2022. Youtube - The Android Factory. [Online]
Available at: https://www.youtube.com/watch?v=-u4w_-x_3_I
[Accessed 17 September 2024].

The following time picker code was taken from teachings by Domenic
Domenic, 2022. Youtube - The Android Factory. [Online]
Available at: https://www.youtube.com/watch?v=-u4w_-x_3_I
[Accessed 17 September 2024]
*/

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/*Below we going to allow this fragment to be the listener for the date and time, this is done by implementing
 the OnDateSetListener and OnTimeSetListener(Domenic, 2022)*/
class fragment_book_test_drive : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    /*Below i am declaring my Global lateinit variables*/
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var booking_date_time_display: EditText
    private lateinit var car_selected_display: EditText
    private lateinit var booking_date_time: TextView
    private lateinit var submitButton: Button
    private lateinit var whatsappIcon: ImageView
    //Below i am declaring my auth variable (Obregon, 2023)
    private lateinit var auth: FirebaseAuth
    //making use of calender to get the year day and day of month (Domenic, 2022)
    private val calendar = Calendar.getInstance()
    //we will also be making use of the date formatter to format the date and time so that it is readable Domenic(2022)
    private val formatter = SimpleDateFormat("dd, MMM, yyyy hh:mm a", Locale.UK)
    private var selected_date = null
    private var param1: String? = null
    private var param2: String? = null
    private val handleNullCarTitle = "No Car Selected!!"

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
        //Below we are Inflating the layout for the settings fragment (Raghunandan, 2013).
        val view =  inflater.inflate(R.layout.fragment_book_test_drive, container, false)
        val carTitle = arguments?.getString("carTitle")

        //Below i am initialising all my lateinit variables, using my view variable (Raghunandan, 2013)
        booking_date_time_display = view.findViewById(R.id.booking_date_time)
        booking_date_time = view.findViewById(R.id.select_date_time_textview)
        submitButton = view.findViewById(R.id.book_test_drive)
        car_selected_display = view.findViewById(R.id.booking_car_selected)
        whatsappIcon = view.findViewById(R.id.whatsapp_icon)

        if(carTitle.isNullOrBlank())
        {
            //we will also display the car title here one time
            car_selected_display.setText(handleNullCarTitle)
        }
        else
        {
            //we will also display the car title here one time
            car_selected_display.setText(carTitle)
        }

        booking_date_time.setOnClickListener{
            showDatePickerDialog()

        }

        submitButton.setOnClickListener{

            submitBooking()
        }
        /*Now we are going to get the current users email address According to minikate (2019), the
        following needs to be done: we are going to get the user email
        and assign it to a variable using auth.currentUser*/
        try {
            firestore = FirebaseFirestore.getInstance() // Initialize Firestore
            storage = FirebaseStorage.getInstance() // Initialize Firebase Storage
            auth = FirebaseAuth.getInstance()//Initialize FirebaseAuth
        } catch (e: Exception) {
            Log.e("Book_Test_Drive Fragment", "Error initializing Firebase", e)
        }

        val car = car_selected_display.text.toString()
        val loggedInEmail: FirebaseUser? = auth.currentUser
        val email = loggedInEmail?.email
        val phone = "0812420589"
        val whatsappMSG = "Good Day Sir/Madam,\nI am interested in your $car which i saw for sale on the app.\nWould you please provide me with more details on the vehicle.\n" +
                "~ $email"
        whatsappIcon.setOnClickListener{
            openWhatsappMessage(phone, whatsappMSG)
        }


        return view
    }

    //According to Domenic (2022), the following function will show the user the date picker and it is set to the current date
    private fun showDatePickerDialog()
    {
        //launch dialog by creating date picker dialog
        DatePickerDialog(requireContext(), this,calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
    //This function will be invoked with the date selected by the user (Domenic, 2022)
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        //once we get the date from the user we set it on the calender
        calendar.set(year, month, dayOfMonth)
        displayFormattedDate(calendar.timeInMillis)
        /*According to Domenic (2022), instead of having the user click on many UI elements to set date and time we must
          have them click on one to do both functions for better flow*/
        TimePickerDialog(requireContext(), this,calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), false).show()
    }
    //Now we create a function to format the calendar date and display Domenic(2022)
    private fun displayFormattedDate(timestamp: Long)
    {
        booking_date_time_display.setText(formatter.format(timestamp))
    }
    //This function will be invoked with the time selected by the user (Domenic, 2022)
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        //Now we are going to do the same as the date picker and set the data once collected
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        displayFormattedDate(calendar.timeInMillis)
    }

    private fun submitBooking()
    {
        //below we are going to attempt to initialize auth, firestore and
        //firebase using a try catch instance (Obregon, 2023).(minikate, 2019)
        try {
            firestore = FirebaseFirestore.getInstance() // Initialize Firestore
            storage = FirebaseStorage.getInstance() // Initialize Firebase Storage
            auth = FirebaseAuth.getInstance()//Initialize FirebaseAuth
        } catch (e: Exception) {
            Log.e("Book_Test_Drive Fragment", "Error initializing Firebase", e)
        }

        //declaring local variables
        val dateTime = booking_date_time_display.text.toString()
        val carTitle = car_selected_display.text.toString()

        /*Now we are going to get the current users email address as it will be stored for a booking made
        According to minikate (2019), the following needs to be done: we are going to get the user email
        and assign it to a variable using auth.currentUser*/
        val loggedInEmail: FirebaseUser? = auth.currentUser

        //Perform exception handling below before saving any data to the database. if required
        // fields are empty then do: show message, else continue
        if(dateTime == "Click Above" || dateTime.isEmpty())
        {
            //Below i will have a toast message telling the user what to do in order to book (Grier 2020)
            val toast = Toast.makeText(requireContext(), "Please Select A Booking Date And Time!",Toast.LENGTH_LONG)
            toast.show()
        }
        //now we perform exception handling on the carTitle variable
        else if(carTitle.isEmpty())
        {
            //Below i will have a toast message telling the user what to do in order to book (Grier 2020)
            val toast = Toast.makeText(requireContext(), "Please Select A Vehicle!",Toast.LENGTH_LONG)
            toast.show()
        }
        //and lastly we are going to perform exception handling on the user email
        else if(loggedInEmail?.email.isNullOrEmpty())
        {
            //Below i will have a toast message telling the user what to do in order to book (Grier 2020)
            val toast = Toast.makeText(requireContext(), "User Email Not Available!",Toast.LENGTH_LONG)
            toast.show()
        }
        else
        {
            val email = loggedInEmail?.email

            if(email != null)
            {
                val booking = mapOf(
                    "carTitle" to carTitle,
                    "dateTime" to dateTime,
                    "userEmail" to email
                )

                //now we are going to add the booking to firebase under bookings collection
                firestore.collection("Bookings")
                    .add(booking)
                    .addOnSuccessListener {
                        // Success, booking created
                        Toast.makeText(requireContext(), "Booking Created Successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{ e ->
                        // Failure, show error message
                        Toast.makeText(requireContext(), "Error Creating Booking: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            else
            {
                // else if user not logged in show a message
                Toast.makeText(requireContext(), "User is not logged in!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*The following function will open a whatapp chat with a predetermined message, this function will accept a number
      reference for the following whatsapp integration code:(Evan, 2020), (GeeksforGeeks, 2020)*/
    private fun openWhatsappMessage(phoneNumber: String, message: String)
    {
        // below we will be getting the package manager to check if the WhatsApp app is installed
        val packageManager = requireContext().packageManager

        try{
            val whatsappPackage = "com.whatsapp"
            val whatsappPackageB = "com.whatsapp.w4b"
            //Now we can check if whatsapp or whatsapp business is installed in the users device
            val whatsappInstalled = try {
                packageManager.getPackageInfo(whatsappPackage, PackageManager.GET_ACTIVITIES)
                true // WhatsApp is installed, set true
            }
            catch (e: PackageManager.NameNotFoundException)
            {
                try
                {
                    packageManager.getPackageInfo(whatsappPackageB, PackageManager.GET_ACTIVITIES)
                    true// WhatsApp business is installed, set true
                }catch (e: PackageManager.NameNotFoundException)
                {
                    false // WhatsApp / whatsapp business is not installed, set false
                }
            }

            /*below we will create a URL that uses WhatsApp's API link for opening a chat with a specific
            phone number
            The format is: https://wa.me/<country_code><phone_number>
            but, before we do this we have to format the number by removing the 0 as whatsapp api makes use
            of the code of the country*/
            val formatNumber = phoneNumber.removePrefix("0")
            //set the message we want to sent
            val encodedMessage = Uri.encode(message)
            val url = "https://wa.me/27$formatNumber?text=$encodedMessage"
            val text = "ji"

            // If WhatsApp is installed, create the intent to open the chat
            if (whatsappInstalled)
            {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse(url))
                // Launch WhatsApp with the intent
                startActivity(intent)
            }
            else
            {
                // Show a message if WhatsApp is not installed
                Toast.makeText(requireContext(), "WhatsApp is not installed on your device", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception)
        {
            // other exception handling
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error: Unable to open WhatsApp", Toast.LENGTH_SHORT).show()
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