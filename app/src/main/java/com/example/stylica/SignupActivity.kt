

package com.example.stylica
import android.widget.RadioGroup
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.example.stylica.data.db.DatabaseHelper

class SignupActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()
        dbHelper = DatabaseHelper(this)

        val firstNameEt = findViewById<EditText>(R.id.firstnameInput)
        val lastNameEt = findViewById<EditText>(R.id.lastnameInput)
        val emailEt = findViewById<EditText>(R.id.emailInput)
        val passwordEt = findViewById<EditText>(R.id.passwordInput)
        val retypePasswordEt = findViewById<EditText>(R.id.retypePasswordInput)
        val phoneEt = findViewById<EditText>(R.id.phoneInput)
        val addressEt = findViewById<EditText>(R.id.addressInput)
        val domainEt = findViewById<EditText>(R.id.domainInput)
        val domainLayout = findViewById<LinearLayout>(R.id.domainLayout)
        val genderSpinner = findViewById<Spinner>(R.id.spinnerGender)
        val roleGroup = findViewById<RadioGroup>(R.id.roleGroup)

        val genders = arrayOf("Prefer not to say", "Male", "Female", "Other")
        genderSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)

        fun refreshDomainVisibility() {
            val isMod = roleGroup.checkedRadioButtonId == R.id.radioModerator
            domainLayout.visibility = if (isMod) android.view.View.VISIBLE else android.view.View.GONE
            if (!isMod) domainEt.text.clear()
        }
        roleGroup.setOnCheckedChangeListener { _, _ -> refreshDomainVisibility() }
        refreshDomainVisibility()

        val signupBtn = findViewById<Button>(R.id.registerBtn)
        val loginBtn = findViewById<Button>(R.id.loginBtn)

        loginBtn.setOnClickListener {
            finish()
        }

        signupBtn.setOnClickListener {

            val firstName = firstNameEt.text.toString().trim()
            val lastName = lastNameEt.text.toString().trim()
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            val retypePassword = retypePasswordEt.text.toString().trim()
            val phone = phoneEt.text.toString().trim()
            val address = addressEt.text.toString().trim()
            val genderRaw = genderSpinner.selectedItem?.toString()?.trim().orEmpty()
            val gender = if (genderRaw == "Prefer not to say" || genderRaw.isEmpty()) null else genderRaw
            val domain = domainEt.text.toString().trim()

            if (firstName.isEmpty() ||
                lastName.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                retypePassword.isEmpty() ||
                phone.isEmpty() ||
                address.isEmpty()
            ) {
                toast("All fields including phone and address are required")
                return@setOnClickListener
            }

            if (password != retypePassword) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Invalid email format")
                return@setOnClickListener
            }

            val selectedRoleId = roleGroup.checkedRadioButtonId

            val role = when (selectedRoleId) {
                R.id.radioAdmin -> "admin"
                R.id.radioModerator -> "moderator"
                else -> "user"
            }

            if (role == "moderator" && domain.isEmpty()) {
                toast("Moderators must enter a product domain")
                return@setOnClickListener
            }

            val success = dbHelper.registerUser(
                firstName,
                lastName,
                email,
                password,
                role,
                gender,
                phone,
                address,
                if (role == "moderator") domain else null
            )

            if (success) {
                toast("User Registered Successfully")
                finish()
            } else {
                toast("User already registered")
            }
        }
    }
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}
