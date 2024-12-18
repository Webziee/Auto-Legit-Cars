package com.legitautocars.app



import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.legitautocars.app.R
import com.legitautocars.app.databinding.ActivityHomeScreen2Binding
import com.legitautocars.app.databinding.ActivityMainBinding

/**
 * HomeScreen Activity for navigating between different fragments in the app.
 *
 * The following code structure has been adapted from common practices used
 * in Android app development and examples from:
 *
 * - Android Developers Documentation: https://developer.android.com/docs
 * - Fragment Transactions: https://developer.android.com/guide/fragments/fragmentmanager
 *
 * The implementation here handles the main screen of the app, which includes
 * a bottom navigation bar for switching between fragments like Buy, Sell, Profile,
 * and Settings. It also features a splash screen using Android's SplashScreen API.
 */

class HomeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityHomeScreen2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //splashscreen
        Thread.sleep(3000)
        installSplashScreen()

        binding = ActivityHomeScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Buy())
        binding.bottomNavigationView2.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.buy -> replaceFragment(Buy())
                R.id.sell -> replaceFragment(Sell())
                R.id.profile -> replaceFragment(Profile())
                R.id.settings -> replaceFragment(Settings())

                else -> {

                }
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}