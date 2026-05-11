package com.example.stylica

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            loadRootFragment(PendingProductsFragment())
            bottomNav.selectedItemId = R.id.nav_requests
        }

        bottomNav.setOnItemSelectedListener { item ->
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            when (item.itemId) {
                R.id.nav_requests -> loadRootFragment(PendingProductsFragment())
                R.id.nav_orders -> loadRootFragment(OrdersFragment())
                R.id.nav_admin_tools -> loadRootFragment(AdminHubFragment())
                R.id.nav_profile -> loadRootFragment(ProfileFragment())
            }
            true
        }
    }

    private fun loadRootFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
