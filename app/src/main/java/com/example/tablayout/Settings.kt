package com.example.tablayout

import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.biometric.BiometricManager
// According to Obregon (2023), the following imports are needed when integrating firebase into the application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
// Import Firebase Firestore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

/**
 * The Settings Fragment allows the user to view their email and access various settings options.
 * The implementation includes Firebase Authentication to display the logged-in user's email.
 *
 * The structure of the fragment, including view initialization and button handling, was inspired
 * by various sources, including:
 *
 * - Firebase Authentication setup: Obregon, 2023, FirebaseAuth documentation.
 * - Fragment and view inflation: Raghunandan, 2013, Android fragment tutorials.
 * - Toast message implementation inside fragments: Grier, 2020, Android Toast documentation.
 * - General Firebase and Android resources used in the fragment setup.
 */

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Settings : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    /*Below i am declaring my Global lateinit variables for my email value and each button that
      will be used in my functions right at the bottom*/
    private lateinit var emailValue: EditText
    private lateinit var biometricButton: Button
    private lateinit var themeButton: Button
    private lateinit var languageButton: Button
    private lateinit var pushNotificationButton: Button
    //Below i am declaring my auth variable (Obregon, 2023)
    private lateinit var auth: FirebaseAuth
    //Declaring my firestore db variable
    private lateinit var db:FirebaseFirestore
    //declaring my variables for the user settings preference.
    private var biometricsEnabled: Boolean? = null
    private var language: String? = null
    private var pushNotificationsEnabled: Boolean? = null
    private var theme: String? = null


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


        // Now we are going to Initialize FirebaseAuth (Obregon, 2023)
        auth = FirebaseAuth.getInstance()

        //Initialise firestore (Obregon, 2023)
        db = FirebaseFirestore.getInstance()

        /*Now we are going to get the current users email address and display it in this settings fragment
          According to minikate (2019), the following needs to be done to achieve this objective: */

        // store current users email in the user variable
        val user: FirebaseUser? = auth.currentUser
        //declaring val to handle else
        val noUser = "!User Logged"
        // now if the user is not null we display the email to the user in the settings page
        if(user != null)
        {
            /*The following code is used to get data/search for data in firebase. This code was inspired from the following youtube video:
            Risky, A., 2019. Youtube, Add and Display Data Firestore — Kotlin Android Studio tutorial — Part 2. [Online]
            Available at: https://www.youtube.com/watch?v=7fkXdfaMRPw
            [Accessed 12 October 2024].
            */
            emailValue.setText(user.email)// if user logged then show their email
            val email = user.email
            val settingsRef = db.collection("Settings")
            settingsRef.whereEqualTo("Email", email).get()
                .addOnSuccessListener{ documents ->
                    if(!documents.isEmpty)
                    {
                        for (document in documents)
                        {
                            // Fetch individual fields directly from Firestore document
                            biometricsEnabled = document.getBoolean("BiometricsEnabled")
                            language = document.getString("Language")
                            pushNotificationsEnabled = document.getBoolean("PushNotificationsEnabled")
                            theme = document.getString("Theme")
                        }
                        if(biometricsEnabled == false)
                        {
                            val string = "OFF"
                            biometricButton.text = string
                        }
                        else
                        {
                            val string = "ON"
                            biometricButton.text = string
                        }
                        themeButton.text = theme
                        languageButton.text = language

                        if(pushNotificationsEnabled == false)
                        {
                            val string = "OFF"
                            pushNotificationButton.text = string
                        }
                        else
                        {
                            val string = "OFF"
                            pushNotificationButton.text = string
                        }
                        Toast.makeText(requireContext(),
                            "Settings Data Successfully Loaded", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        val email = user.email
                        Toast.makeText(requireContext(), "No Data For User, Attempting To Creating Default Settings: ", Toast.LENGTH_SHORT).show()
                        // No data found, create default settings
                        val defaultSettings = hashMapOf(
                            "BiometricsEnabled" to false,
                            "Language" to "ENG",
                            "PushNotificationsEnabled" to false,
                            "Theme" to "LIGHT",
                            "Email" to email)

                        settingsRef.add(defaultSettings).addOnSuccessListener {
                            Toast.makeText(requireContext(), "Default Settings Created For: $email", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener{
                            Toast.makeText(requireContext(), "Failed To Create Default Settings For: $email", Toast.LENGTH_SHORT).show()
                        }
                    }
            }.addOnFailureListener{ exception ->
                    Toast.makeText(requireContext(), "Failed To Load Settings Data", Toast.LENGTH_SHORT).show()
                }
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

    /*The following method handles the Biometrics Button On Click, This method will enable biometrics for the user only
      if their device can support it and disables accordingly. This code was inspired by the following video:
      Lackner, P., 2024. Youtube, How to Implement Biometric Auth in Your Android App. [Online]
      Available at: https://www.youtube.com/watch?v=_dCRQ9wta-I
      [Accessed 12 October 2024].*/
    private fun handleBiometricClick()
    {
        //On click for button
            biometricButton.setOnClickListener {
                //Check to see the current value of the biometrics field
                if(biometricsEnabled == false)
                {
                    // Use BiometricManager to check if biometric features are available and enabled
                    val biometricManager = BiometricManager.from(requireContext())
                    val canAuthenticate = biometricManager.canAuthenticate(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                                or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    //The following code checks to see if the users device can support biometrics before allows them to enable it
                    when(canAuthenticate)
                    {
                        BiometricManager.BIOMETRIC_SUCCESS ->{
                            //If biometrics is successfully checked and works then we can enable the settings in user preferences
                            enableBiometricFields()
                        }
                        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                            // Device does not have biometric hardware
                            Toast.makeText(requireActivity(), "Biometric Authentication Not Supported.",
                                Toast.LENGTH_LONG).show()
                        }
                        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
                            // Devices hardware is currently unavailable
                            Toast.makeText(requireContext(), "Biometric Hardware Is Currently Unavailable.",
                                Toast.LENGTH_LONG).show()
                        }
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
                            // Devices supports biometrics but user has now set it up yet
                            Toast.makeText(requireContext(), "Biometrics Not Set, Please Set In Phone Settings",
                                Toast.LENGTH_LONG).show()
                        }
                        else ->{
                            // Unknown error
                            Toast.makeText(
                                requireContext(),
                                "Unexpected Error Occurred. Please Try Again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                else if(biometricsEnabled == true)
                {
                    //If field is already enabled this means that their device supports biometrics and we can then just disable it
                    disableBiometricFields()
                }

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

    /*The following method enables the users biometrics preference in firestore, this code was inspired from the following video:
        The following code was inspired from the following youtube video:
        Risky, A., 2019. Youtube, Add and Display Data Firestore — Kotlin Android Studio tutorial — Part 2. [Online]
        Available at: https://www.youtube.com/watch?v=7fkXdfaMRPw
        [Accessed 12 October 2024].*/
    private fun enableBiometricFields()
    {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // Store current user's email
        val user: FirebaseUser? = auth.currentUser
        val email = user?.email

        if (email != null) {
            val settingsRef = db.collection("Settings")
            settingsRef.whereEqualTo("Email", email).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            // Update biometricsEnabled field in Firestore to false
                            document.reference.update("BiometricsEnabled", true)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Biometrics Enabled Successfully", // Update success message
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Update our local biometricsEnabled variable to false
                                    biometricsEnabled = true
                                    // Update our button text accordingly
                                    biometricButton.text = "ON" // Set button text to OFF
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed To Update Biometric Setting.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Failed To Load User Settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(
                requireContext(),
                "No User Logged In",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /*The following method disables the users biometrics preference in firestore, this code was inspired from the following video:
        The following code was inspired from the following youtube video:
        Risky, A., 2019. Youtube, Add and Display Data Firestore — Kotlin Android Studio tutorial — Part 2. [Online]
        Available at: https://www.youtube.com/watch?v=7fkXdfaMRPw
        [Accessed 12 October 2024].*/
    private fun disableBiometricFields()
    {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // Store current user's email
        val user: FirebaseUser? = auth.currentUser
        val email = user?.email

        if (email != null) {
            val settingsRef = db.collection("Settings")
            settingsRef.whereEqualTo("Email", email).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            // Update biometricsEnabled field in Firestore to false
                            document.reference.update("BiometricsEnabled", false)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Biometrics Disabled Successfully", // Update success message
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Update our local biometricsEnabled variable to false
                                    biometricsEnabled = false
                                    // Update our button text accordingly
                                    biometricButton.text = "OFF" // Set button text to OFF
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed To Update Biometric Setting.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Failed To Load User Settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(
                requireContext(),
                "No User Logged In",
                Toast.LENGTH_SHORT
            ).show()
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