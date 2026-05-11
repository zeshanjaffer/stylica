package com.example.stylica

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class AdminHubFragment : Fragment(R.layout.fragment_admin_hub) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnProductSearch).setOnClickListener { open(AdminProductSearchFragment()) }
        view.findViewById<View>(R.id.btnModeratorProducts).setOnClickListener { open(AdminModeratorProductsFragment()) }
        view.findViewById<View>(R.id.btnModeratorDirectory).setOnClickListener { open(AdminModeratorsFragment()) }
        view.findViewById<View>(R.id.btnAnnouncements).setOnClickListener { open(AdminAnnouncementsFragment()) }
        view.findViewById<View>(R.id.btnCouriers).setOnClickListener { open(AdminCouriersFragment()) }
        view.findViewById<View>(R.id.btnPaymentCompanies).setOnClickListener { open(AdminPaymentCompaniesFragment()) }
        view.findViewById<View>(R.id.btnEmployees).setOnClickListener { open(AdminEmployeesFragment()) }
        view.findViewById<View>(R.id.btnTransactions).setOnClickListener { open(AdminTransactionsFragment()) }
    }

    private fun open(frag: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, frag)
            .addToBackStack(null)
            .commit()
    }
}
