package com.example.stylica

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ModeratorDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moderator_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            loadFragment(CreateProductFragment())
            bottomNav.selectedItemId = R.id.nav_add_product
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_product -> loadFragment(CreateProductFragment())
                R.id.nav_my_products -> loadFragment(ModeratorProductsFragment())
                R.id.nav_moderator_qa -> loadFragment(ModeratorQualityFragment())
                R.id.nav_moderator_orders -> loadFragment(ModeratorOrdersFragment())
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
