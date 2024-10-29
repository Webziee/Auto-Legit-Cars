@file:Suppress("DEPRECATION")

package com.example.tablayout

import android.app.VoiceInteractor.Prompt
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricPrompt.PromptInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class MainActivity : AppCompatActivity()
{
    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var signUp: TextView
    private lateinit var logIn: TextView
    private lateinit var LoginTextEmail: TextInputEditText
    private lateinit var LoginTextPassword: TextInputEditText
    private lateinit var SignupTextEmail: TextInputEditText
    private lateinit var SignupTextPassword: TextInputEditText
    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var signUpLayout: LinearLayout
    private lateinit var logInLayout: LinearLayout
    private lateinit var signupConfirmPassword: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private lateinit var signupwithtext: TextView
    private lateinit var loginwithtext: TextView
    private lateinit var googleSignInButton: com.google.android.gms.common.SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var promptText: TextView
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    //declaring my variables for the user settings preference
    private var biometricsEnabled: Boolean? = null
    private var language: String? = null
    private var pushNotificationsEnabled: Boolean? = null
    private var theme: String? = null

    /*For biometrics we will have a private val promptManager which is made by lazy so that we initialise it
      as soon as we access it the fist time (Lackner, 2024)*/
    private val promptManager by lazy {
        BiometricPromptManager(this)
    }

    // ActivityResultLauncher for Google Sign-In
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Check if user is already signed in
        if (currentUser != null)
        {
            //Initialise firestore (Obregon, 2023)
            val db = FirebaseFirestore.getInstance()

            //if user is not null then we get their email and then check their settings preference
            val email = currentUser.email
            /*The following code is used to get data/search for data in firebase. This code was inspired from the following youtube video:
              Risky, A., 2019. Youtube, Add and Display Data Firestore — Kotlin Android Studio tutorial — Part 2. [Online]
              Available at: https://www.youtube.com/watch?v=7fkXdfaMRPw
              [Accessed 12 October 2024].
             */
            val settingsRef = db.collection("Settings")
            settingsRef.whereEqualTo("Email", email).get()
                .addOnSuccessListener { documents ->
                    if(!documents.isEmpty)
                    {
                        for(document in documents)
                        {
                            //Fetch the user settings field directly from the firestore document
                            biometricsEnabled = document.getBoolean("BiometricsEnabled")
                            language = document.getString("Language")
                            pushNotificationsEnabled = document.getBoolean("PushNotificationsEnabled")
                            theme = document.getString("Theme")
                        }
                        if(biometricsEnabled == true)
                        {
                            Toast.makeText(this, "Use Your Biometrics To Login", Toast.LENGTH_LONG).show()
                            setupBiometricPrompt()
                        }
                        else if (biometricsEnabled == false)
                        {
                            Toast.makeText(this, "Enable Biometrics In Settings For Easy Login", Toast.LENGTH_LONG).show()
                        }
                        else
                        {
                            Toast.makeText(this, "Login Error, Please Log In Again", Toast.LENGTH_LONG).show()
                        }
                    }
                }.addOnFailureListener{ exception ->
                    Toast.makeText(this, "Failed To Load Settings Data", Toast.LENGTH_SHORT).show()
                }
        }
        else
        {
            Toast.makeText(this, "User Not Logged In, Please Log In", Toast.LENGTH_SHORT).show()
        }
        setContentView(R.layout.activity_main)
        initializeViews()
    }

    private fun initializeViews()
    {
        // Initialize views
        logIn = findViewById(R.id.logIn)
        signUp = findViewById(R.id.singUp)
        signUpLayout = findViewById(R.id.signUpLayout)
        logInLayout = findViewById(R.id.logInLayout)
        loginBtn = findViewById(R.id.logInBtn)
        signUpBtn = findViewById(R.id.signup_Button)
        LoginTextEmail = findViewById(R.id.Login_eMail)
        LoginTextPassword = findViewById(R.id.login_password)
        SignupTextEmail = findViewById(R.id.signUp_eMail)
        SignupTextPassword = findViewById(R.id.signUp_password)
        signupConfirmPassword = findViewById(R.id.confirm_password)
        progressBar = findViewById(R.id.progressbar)
        signupwithtext = findViewById(R.id.sign_up_with_text)
        loginwithtext = findViewById(R.id.login_in_with_text)
        googleSignInButton = findViewById(R.id.googleSignInButton)
        promptText = findViewById(R.id.tvPrompt)


        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this is your client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleSignInResult(task)
                } else {
                    // Log the result code to understand if any specific code is being returned
                    Log.e("GoogleSignIn", "Result code: ${result.resultCode}")
                    Toast.makeText(this, "Google Sign-In canceled", Toast.LENGTH_SHORT).show()
                }
            }

        // Set up the Google Sign-In button click listener
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        // Handle switching between Log In and Sign Up layouts
        signUp.setOnClickListener {
            signUp.background = ContextCompat.getDrawable(this, R.drawable.switch_trcks)
            signUp.setTextColor(ContextCompat.getColor(this, R.color.textColor))
            logIn.background = null
            signUpLayout.visibility = View.VISIBLE
            logInLayout.visibility = View.GONE
            logIn.setTextColor(ContextCompat.getColor(this, R.color.auto_red))
            loginBtn.visibility = View.GONE
            signUpBtn.visibility = View.VISIBLE
            signupwithtext.visibility = View.VISIBLE
            loginwithtext.visibility = View.GONE
        }

        logIn.setOnClickListener {
            logIn.background = ContextCompat.getDrawable(this, R.drawable.switch_trcks)
            logIn.setTextColor(ContextCompat.getColor(this, R.color.textColor))
            signUp.background = null
            logInLayout.visibility = View.VISIBLE
            signUpLayout.visibility = View.GONE
            signUp.setTextColor(ContextCompat.getColor(this, R.color.auto_red))
            loginBtn.visibility = View.VISIBLE
            signUpBtn.visibility = View.GONE
            signupwithtext.visibility = View.GONE
            loginwithtext.visibility = View.VISIBLE
        }

        signUpBtn.setOnClickListener {
            handleSignUp()
        }

        loginBtn.setOnClickListener {
            handleLogin()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // Retrieve the Google account
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d("GoogleSignIn", "firebaseAuthWithGoogle: " + account.id)

            // Pass the ID token to Firebase authentication
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Handle sign-in failure
            Log.e("GoogleSignIn", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        // Ensure the ID token is not null
        if (idToken == null) {
            Log.e("FirebaseAuth", "ID token is null, cannot authenticate with Firebase")
            return
        }

        // Use the token to create Firebase credentials
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Authenticate with Firebase using the Google credentials
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    Log.d("FirebaseAuth", "signInWithCredential:success, user: ${user?.email}")
                    Toast.makeText(this, "Google sign-in successful", Toast.LENGTH_SHORT).show()

                    // You can now navigate to another screen (HomeScreen or MainActivity)
                    startActivity(Intent(this, HomeScreen::class.java))
                    finish()
                } else {
                    // Sign in fails
                    Log.e("FirebaseAuth", "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        this,
                        "Firebase authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun handleSignUp() {
        progressBar.visibility = View.VISIBLE
        val email = SignupTextEmail.text.toString().trim()
        val password = SignupTextPassword.text.toString().trim()
        val confirmpassword = signupConfirmPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty() && confirmpassword.isNotEmpty()) {
            if (password == confirmpassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            showCustomToast("Registration successful!", R.drawable.success)
                            SignupTextEmail.text?.clear()
                            SignupTextPassword.text?.clear()
                            signupConfirmPassword.text?.clear()
                        } else {
                            Toast.makeText(
                                this,
                                "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    this,
                    "Passwords do not match. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }

    private fun handleLogin() {
        progressBar.visibility = View.VISIBLE
        val email = LoginTextEmail.text.toString()
        val password = LoginTextPassword.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        showCustomToast("Login Successful", R.drawable.success)
                        val intent = Intent(applicationContext, HomeScreen::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showCustomToast(
                            "Authentication failed: ${task.exception?.message}",
                            R.drawable.error
                        )
                    }
                }
        } else {
            showCustomToast("Fill in the required fields", R.drawable.error)
            progressBar.visibility = View.GONE
        }
    }

    private fun showCustomToast(message: String, iconResId: Int)
    {
        val inflater = layoutInflater
        val layout: View =
            inflater.inflate(R.layout.customtoast, findViewById(R.id.custom_toast_root))
        val toastMessage: TextView = layout.findViewById(R.id.toast_message)
        val toastIcon: ImageView = layout.findViewById(R.id.toast_icon)

        toastMessage.text = message
        toastIcon.setImageResource(iconResId)

        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }

    /*The following biometrics code displays the biometric prompt to the user and contains actions for the various results
      such as success and failure, this code was inspired by the following video:
      Lackner, P., 2024. Youtube, How to Implement Biometric Auth in Your Android App. [Online]
      Available at: https://www.youtube.com/watch?v=_dCRQ9wta-I
      [Accessed 12 October 2024].*/
    private fun setupBiometricPrompt()
    {
        val executor = ContextCompat.getMainExecutor(this@MainActivity)
        biometricPrompt = androidx.biometric.BiometricPrompt(this@MainActivity, executor,
            object : AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@MainActivity, "Authentication Error", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        this@MainActivity,
                        "Authentication Successful, Welcome",
                        Toast.LENGTH_LONG
                    ).show()
                    //If auth successful then log the user in (proceed to main home screen)
                    startActivity(Intent(this@MainActivity, HomeScreen::class.java))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@MainActivity, "Authentication Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        promptInfo = PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Login Using Your Biometric Credential")
            .setNegativeButtonText("Cancel")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}

