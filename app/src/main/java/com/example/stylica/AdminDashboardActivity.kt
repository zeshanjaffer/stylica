package com.example.stylica

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Initial fragment
        if (savedInstanceState == null) {
            loadFragment(PendingProductsFragment())
            bottomNav.selectedItemId = R.id.nav_requests
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_requests -> loadFragment(PendingProductsFragment())
                R.id.nav_orders -> loadFragment(OrdersFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}