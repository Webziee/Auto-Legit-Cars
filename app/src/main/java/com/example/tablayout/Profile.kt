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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Profile : Fragment() {

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