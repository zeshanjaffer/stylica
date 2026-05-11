package com.example.stylica

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var nameTv: TextView
    private lateinit var emailTv: TextView
    private lateinit var updateBtn: Button
    private lateinit var changePasswordBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var logoutBtn: Button
    private lateinit var profileImage: ImageView
    private lateinit var tvProfileMeta: TextView

    private lateinit var email: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        nameTv = view.findViewById(R.id.tvName)
        emailTv = view.findViewById(R.id.tvEmail)
        updateBtn = view.findViewById(R.id.updateBtn)
        changePasswordBtn = view.findViewById(R.id.changePasswordBtn)
        deleteBtn = view.findViewById(R.id.deleteBtn)
        logoutBtn = view.findViewById(R.id.logoutBtn)
        profileImage = view.findViewById(R.id.profileImage)
        tvProfileMeta = view.findViewById(R.id.tvProfileMeta)

        email = requireActivity().intent.getStringExtra("EMAIL")!!

        updateBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, UpdateFragment())
                .addToBackStack(null)
                .commit()
        }

        changePasswordBtn.setOnClickListener {
            val dialogView = android.view.LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
            val oldPasswordInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editOldPassword)
            val newPasswordInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editNewPassword)
            val btnSave = dialogView.findViewById<Button>(R.id.btnSavePassword)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelPassword)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSave.setOnClickListener {
                val oldPass = oldPasswordInput.text.toString().trim()
                val newPass = newPasswordInput.text.toString().trim()

                if (oldPass.isEmpty() || newPass.isEmpty()) {
                    Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                } else if (newPass.length < 6) {
                    Toast.makeText(requireContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                } else {
                    val success = dbHelper.changePassword(email, oldPass, newPass)
                    if (success) {
                        Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Incorrect Old Password", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            dialog.show()
        }

        deleteBtn.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    dbHelper.deleteUser(email)
                    val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
                    sharedPref.edit().clear().apply()
                    
                    Toast.makeText(requireContext(), "User Deleted", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(requireContext(), MainActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        logoutBtn.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            
            val intent = android.content.Intent(requireContext(), MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        loadUserData()
    }

    private fun loadUserData() {
        val user = dbHelper.getUserByEmail(email)

        if(user != null){
            nameTv.text = "Name: ${user.firstName} ${user.lastName}"
            emailTv.text = "Email: ${user.email}"
            val meta = buildList {
                user.gender?.takeIf { it.isNotBlank() }?.let { add("Gender: $it") }
                user.phone?.takeIf { it.isNotBlank() }?.let { add("Contact: $it") }
                user.address?.takeIf { it.isNotBlank() }?.let { add("Address: $it") }
                if (user.role == "moderator") {
                    user.domain?.takeIf { it.isNotBlank() }?.let { add("Domain: $it") }
                }
                user.registeredAt?.takeIf { it.isNotBlank() }?.let { add("Registered: $it") }
            }
            tvProfileMeta.text = meta.joinToString("\n")
            tvProfileMeta.visibility = if (meta.isEmpty()) View.GONE else View.VISIBLE

            if (!user.profileImage.isNullOrEmpty()) {
                try {
                    val uri = android.net.Uri.parse(user.profileImage)
                    profileImage.setImageURI(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}