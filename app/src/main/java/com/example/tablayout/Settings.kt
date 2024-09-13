package com.example.tablayout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
// According to Obregon (2023), the following imports are needed when integrating firebase into the application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Settings : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //Below i am going to initialise the firebaseAuth


    /*Below i am declaring my Global lateinit variables for my email value and each button that
      will be used in my functions right at the bottom*/
    private lateinit var emailValue: EditText
    private lateinit var biometricButton: Button
    private lateinit var themeButton: Button
    private lateinit var languageButton: Button
    private lateinit var pushNotificationButton: Button
    //Below i am declaring my auth variable (Obregon, 2023)
    private lateinit var auth: FirebaseAuth


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
        val view =  inflater.inflate(R.layout.fragment_settings, container, false)


        //Below i am initialising all my lateinit variables, using my view variable (Raghunandan, 2013)
        emailValue = view.findViewById(R.id.settings_email)
        biometricButton = view.findViewById(R.id.biometrics_enabled)
        themeButton = view.findViewById(R.id.theme_selection)
        languageButton = view.findViewById(R.id.language_selection)
        pushNotificationButton = view.findViewById(R.id.push_notifications_selection)

        // Now we atre going to Initialize FirebaseAuth (Obregon, 2023)
        auth = FirebaseAuth.getInstance()

        /*Now we are going to get the current users email address and display it in this settings fragment
          According to minikate (2019), the following needs to be done to achieve this objective: */

        // store current users email in the user variable
        val user: FirebaseUser? = auth.currentUser
        //declaring val to handle else
        val noUser = "!User Logged"
        // now if the user is not null we display the email to the user in the settings page
        if(user != null)
        {
            emailValue.setText(user.email)// if user logged then show their email
        }
        else
        {
            emailValue.setText(noUser)//Show handle val is no user logged
        }

        //Now we call all our functions to handle onClick events
        handleBiometricClick()
        handleLangaugeClick()
        handlePushNotificationClick()
        handleThemeClick()
        return view
    }

    /*The following functions will display a toast message to the user when the biometrics_enables button is clicked
      (Pachigolla, 2013)*/
    private fun handleBiometricClick()
    {
        biometricButton.setOnClickListener{
            //According to Grier (2020), this is how we create a toast message inside of a fragment
            Toast.makeText(requireContext(),"Feature Currently Unavailable, Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun handleLangaugeClick()
    {
        languageButton.setOnClickListener{
            //According to Grier (2020), this is how we create a toast message inside of a fragment
            Toast.makeText(requireContext(),"Feature Currently Unavailable, Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun handlePushNotificationClick()
    {
        pushNotificationButton.setOnClickListener{
            //According to Grier (2020), this is how we create a toast message inside of a fragment
            Toast.makeText(requireContext(),"Feature Currently Unavailable, Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun handleThemeClick()
    {
        themeButton.setOnClickListener{
            //According to Grier (2020), this is how we create a toast message inside of a fragment
            Toast.makeText(requireContext(),"Feature Currently Unavailable, Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Settings().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}