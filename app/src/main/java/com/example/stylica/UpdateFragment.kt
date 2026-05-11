package com.example.stylica

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class UpdateFragment : Fragment(R.layout.fragment_update) {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnUpdate: Button
    
    private lateinit var updateProfileImage: ImageView
    private lateinit var btnSelectPhoto: Button
    private var selectedImageUri: String? = null

    private lateinit var email: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        dbHelper = DatabaseHelper(requireContext())

        // connect views
        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etPassword = view.findViewById(R.id.etPassword)
        btnUpdate = view.findViewById(R.id.btnUpdate)
        updateProfileImage = view.findViewById(R.id.updateProfileImage)
        btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto)

        // get email from Activity
        email = requireActivity().intent.getStringExtra("EMAIL") ?: ""

        val user = dbHelper.getUserByEmail(email)
        if (user != null) {
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etPassword.setText(user.password)
            if (!user.profileImage.isNullOrEmpty()) {
                selectedImageUri = user.profileImage
                try {
                    val uri = android.net.Uri.parse(user.profileImage)
                    updateProfileImage.setImageURI(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val pickImage = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    requireActivity().contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    selectedImageUri = uri.toString()
                    updateProfileImage.setImageURI(uri)
                }
            }
        }

        btnSelectPhoto.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(android.content.Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            pickImage.launch(intent)
        }

        btnUpdate.setOnClickListener {

            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if(firstName.isEmpty() && lastName.isEmpty() && password.isEmpty() && selectedImageUri == null){

                Toast.makeText(requireContext(),
                    "Enter at least one field or select a photo",
                    Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            val rowsUpdated = dbHelper.updateUser(
                email,
                firstName,
                lastName,
                password,
                selectedImageUri
            )

            if(rowsUpdated > 0){

                Toast.makeText(requireContext(),
                    "User Updated Successfully",
                    Toast.LENGTH_SHORT).show()

                // go back to ProfileFragment
                parentFragmentManager.popBackStack()

            } else {

                Toast.makeText(requireContext(),
                    "Update Failed",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}