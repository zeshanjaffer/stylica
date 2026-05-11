package com.example.stylica

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class UpdateFragment : Fragment(R.layout.fragment_update) {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var etDomain: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var labelDomain: TextView
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

        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etPassword = view.findViewById(R.id.etPassword)
        etPhone = view.findViewById(R.id.etPhone)
        etAddress = view.findViewById(R.id.etAddress)
        etDomain = view.findViewById(R.id.etDomain)
        spinnerGender = view.findViewById(R.id.spinnerGenderUpdate)
        labelDomain = view.findViewById(R.id.labelDomainUpdate)
        btnUpdate = view.findViewById(R.id.btnUpdate)
        updateProfileImage = view.findViewById(R.id.updateProfileImage)
        btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto)

        email = requireActivity().intent.getStringExtra("EMAIL") ?: ""

        val genders = arrayOf("Prefer not to say", "Male", "Female", "Other")
        spinnerGender.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genders)

        val user = dbHelper.getUserByEmail(email)
        if (user != null) {
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etPassword.setText(user.password)
            etPhone.setText(user.phone.orEmpty())
            etAddress.setText(user.address.orEmpty())
            etDomain.setText(user.domain.orEmpty())
            val isModerator = user.role == "moderator"
            etDomain.visibility = if (isModerator) View.VISIBLE else View.GONE
            labelDomain.visibility = if (isModerator) View.VISIBLE else View.GONE
            val genderList = listOf("Prefer not to say", "Male", "Female", "Other")
            val idx = genderList.indexOf(user.gender ?: "Prefer not to say").coerceAtLeast(0)
            spinnerGender.setSelection(idx)
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
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val domain = etDomain.text.toString().trim()
            val genderRaw = spinnerGender.selectedItem?.toString()?.trim().orEmpty()
            val gender = if (genderRaw == "Prefer not to say") "" else genderRaw

            if (firstName.isEmpty() && lastName.isEmpty() && password.isEmpty() && selectedImageUri == null &&
                gender.isEmpty() && phone.isEmpty() && address.isEmpty() && domain.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Enter at least one field or select a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val u = dbHelper.getUserByEmail(email)
            val domainArg = if (u?.role == "moderator") domain else null

            val rowsUpdated = dbHelper.updateUser(
                email = email,
                firstName = firstName.ifEmpty { null },
                lastName = lastName.ifEmpty { null },
                password = password.ifEmpty { null },
                profileImage = selectedImageUri,
                gender = gender.ifEmpty { null },
                phone = phone.ifEmpty { null },
                address = address.ifEmpty { null },
                domain = domainArg?.ifEmpty { null }
            )

            if (rowsUpdated > 0) {
                Toast.makeText(requireContext(), "User Updated Successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
