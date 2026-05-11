package com.example.stylica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        val userEmail = intent.getStringExtra("EMAIL") ?: ""
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)


//

        bottomNav.setOnItemSelectedListener { item ->

            val selectedFragment = when (item.itemId) {

                R.id.nav_home -> HomeFragment()

              //  R.id.nav_profile -> ProfileFragment()
                R.id.nav_orders -> MyOrdersFragment()
                R.id.nav_cart -> {
                    val cartFragment = CartFragment()
                    val bundle = Bundle()
                    bundle.putString("user_email", userEmail)
                    cartFragment.arguments = bundle
                    cartFragment
                }

                R.id.nav_settings -> ProfileFragment()  // change later if you want separate SettingsFragment

                R.id.nav_favorites -> FavoritesFragment()



                else -> null
            }

            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, it)
                    .commit()
            }

            true
        }    }
}