package com.example.tablayout

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Profile : Fragment() {

    private lateinit var bookTestDriveButton: Button
    private lateinit var signOutButton: Button
    private lateinit var auth: FirebaseAuth

    private lateinit var emailvalue: TextView
    private lateinit var namevalue: TextView
    private lateinit var passwordvalue: TextView

    // TODO: Rename and change types of parameters
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
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Find the sign-out button
        bookTestDriveButton = view.findViewById(R.id.book_test_drive)
        signOutButton = view.findViewById(R.id.btnSignOut)
        emailvalue = view.findViewById(R.id.email_value)
        namevalue = view.findViewById(R.id.name_value)
        passwordvalue = view.findViewById(R.id.password_value)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        //current logged in user email
        val user: FirebaseUser? = auth.currentUser

        if(user != null){
            emailvalue.text = user.email

            //default password
            passwordvalue.text = "..................."

            //default user name
            namevalue.setText(user.displayName ?: "")
        }

        //now we set an onclick listener for the book test drive button and call our method (Mourya, 2023).
        bookTestDriveButton.setOnClickListener{
            //inside this we just call our method
            navigateTestDrive()
        }

        // Set a click listener for the sign-out button
        signOutButton.setOnClickListener {
            signOutUser()
        }

            return view
    }

    private fun signOutUser() {
        // Sign out from Firebase
        auth.signOut()

        // Optionally, sign out from Google if you use Google Sign-In
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut()

        // Navigate back to the login screen (MainAct)
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    //the following function will navigate the user to the buy page to book a test drive when clicked (Mourya, 2023).
    private fun navigateTestDrive()
    {
        //Below i will have a toast message telling the user what to do in order to book (Grier 2020)
        val toast = Toast.makeText(requireContext(), "Please Select A Car From Buy Page To Make A Booking",Toast.LENGTH_LONG)
        toast.show()
        // Creating an instance of the fragment that i want to navigate to
        val buyFragement = Buy()
        //now we will perform the navigation from current fragment to buy fragment
        parentFragmentManager.beginTransaction().replace(R.id.frame_layout, buyFragement).addToBackStack(null).commit()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}