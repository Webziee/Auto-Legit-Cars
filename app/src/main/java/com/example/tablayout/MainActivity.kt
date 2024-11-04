@file:Suppress("DEPRECATION")

package com.example.tablayout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
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
import java.util.Locale

class MainActivity : AppCompatActivity() {
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
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    // User settings preference variables
    private var biometricsEnabled: Boolean? = null
    private var language: String? = null
    private var pushNotificationsEnabled: Boolean? = null
    private var theme: String? = null

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up language preference from SharedPreferences
        val preferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val languageCode = preferences.getString("Language", "en")
        setLocale(this, languageCode ?: "en")

        installSplashScreen()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        checkAndRequestNotificationPermission()
        createNotificationChannel()
        initializeViews()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            retrieveUserSettings(currentUser.email)
        } else {
            Toast.makeText(this, getString(R.string.Toast23), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val postNotificationPermission = "android.permission.POST_NOTIFICATIONS"
            if (ContextCompat.checkSelfPermission(this, postNotificationPermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(postNotificationPermission),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "car_listing_channel",
                "Car Listings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for car listings"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.Toast101), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.Toast102), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retrieveUserSettings(email: String?) {
        val db = FirebaseFirestore.getInstance()
        val settingsRef = db.collection("Settings")
        settingsRef.whereEqualTo("Email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        biometricsEnabled = document.getBoolean("BiometricsEnabled")
                        language = document.getString("Language")
                        pushNotificationsEnabled = document.getBoolean("PushNotificationsEnabled")
                        theme = document.getString("Theme")
                    }
                    setupBiometricPrompt()
                }
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.Toast22), Toast.LENGTH_SHORT).show()
            }
    }

    private fun initializeViews() {
        // Initialize UI components and Google Sign-In client
        signUp = findViewById(R.id.singUp)
        logIn = findViewById(R.id.logIn)
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

        setupGoogleSignIn()
        setupLayoutSwitch()
        setupButtonListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            } else {
                Log.e("GoogleSignIn", "Result code: ${result.resultCode}")
                Toast.makeText(this, getString(R.string.Toast24), Toast.LENGTH_SHORT).show()
            }
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun setupLayoutSwitch() {
        signUp.setOnClickListener {
            switchToSignUp()
        }

        logIn.setOnClickListener {
            switchToLogIn()
        }
    }

    private fun setupButtonListeners() {
        signUpBtn.setOnClickListener {
            handleSignUp()
        }

        loginBtn.setOnClickListener {
            handleLogin()
        }
    }

    private fun switchToSignUp() {
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

    private fun switchToLogIn() {
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

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, getString(R.string.Toast25) + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, HomeScreen::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.Toast27) + task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignUp() {
        val email = SignupTextEmail.text.toString().trim()
        val password = SignupTextPassword.text.toString().trim()
        val confirmpassword = signupConfirmPassword.text.toString().trim()
        if (email.isNotEmpty() && password.isNotEmpty() && confirmpassword.isNotEmpty() && password == confirmpassword) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showCustomToast(getString(R.string.Toast28), R.drawable.success)
                } else {
                    Toast.makeText(this, getString(R.string.Toast29) + task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleLogin() {
        val email = LoginTextEmail.text.toString()
        val password = LoginTextPassword.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(applicationContext, HomeScreen::class.java))
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.Toast33) + task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCustomToast(message: String, iconResId: Int) {
        val inflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.customtoast, findViewById(R.id.custom_toast_root))
        val toastMessage: TextView = layout.findViewById(R.id.toast_message)
        val toastIcon: ImageView = layout.findViewById(R.id.toast_icon)
        toastMessage.text = message
        toastIcon.setImageResource(iconResId)
        Toast(applicationContext).apply {
            duration = Toast.LENGTH_LONG
            view = layout
        }.show()
    }

    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                startActivity(Intent(this@MainActivity, HomeScreen::class.java))
                finish()
            }
            override fun onAuthenticationFailed() {
                Toast.makeText(this@MainActivity, getString(R.string.Toast33), Toast.LENGTH_SHORT).show()
            }
        })
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.Toast37))
            .setSubtitle(getString(R.string.Toast38))
            .setNegativeButtonText(getString(R.string.Toast39))
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    fun setLocale(context: Context, lanCode: String) {
        val locale = Locale(lanCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
