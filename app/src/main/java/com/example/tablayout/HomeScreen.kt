package com.example.tablayout

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.tablayout.databinding.ActivityHomeScreen2Binding
import com.example.tablayout.databinding.ActivityMainBinding

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