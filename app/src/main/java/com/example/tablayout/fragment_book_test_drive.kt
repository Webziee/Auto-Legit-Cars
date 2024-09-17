package com.example.tablayout

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.Time
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var booking_date_time_display: EditText
    private lateinit var car_selected_display: EditText
    private lateinit var booking_date_time: TextView
    private lateinit var submitButton: Button
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

            val carTitleVal = car_selected_display.text.toString()
            val dateVal = booking_date_time_display.text.toString()

            submitBooking(carTitleVal, dateVal)
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
        calendar.set(Calendar.HOUR_OF_DAY, Calendar.MINUTE)
        displayFormattedDate(calendar.timeInMillis)
    }

    private fun submitBooking(carTitle: String, dayTime: String)
    {
        val check = booking_date_time_display.text.toString()
        //Perform exception handling below before saving any data to the database. if required fields are empty then do
        if(check == "Click Above" || check.isEmpty())
        {
            //Below i will have a toast message telling the user what to do in order to book (Grier 2020)
            val toast = Toast.makeText(requireContext(), "Please Select A Booking Date And Time",Toast.LENGTH_LONG)
            toast.show()
        }
        //else we are going to save all our data in our firebase database. The following code was taken from
        else
        {
            //First we must get the firestore instance (minikate, 2019)
            val db = FirebaseFirestore.getInstance()

            /*Now we are going to get the current users email address as it will be stored for a booking made
            According to minikate (2019), the following needs to be done*/
            // Now we atre going to Initialize FirebaseAuth (Obregon, 2023)
            auth = FirebaseAuth.getInstance()
            val userEmail: FirebaseUser? = auth.currentUser

            if(userEmail != null)
            {
                val booking = mapOf(
                    "carTitle" to carTitle,
                    "dateTime" to dayTime,
                    "userEmail" to userEmail
                )

                //now we are going to add the booking to firebase under bookings collection
                db.collection("Bookings")
                    .add(booking)
                    .addOnSuccessListener {
                        // Success, booking created
                        Toast.makeText(requireContext(), "Booking created successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{ e ->
                        // Failure, show error message
                        Toast.makeText(requireContext(), "Error creating booking: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            else
            {
                // else if user not logged in show a message
                Toast.makeText(requireContext(), "User is not logged in!", Toast.LENGTH_SHORT).show()
            }
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