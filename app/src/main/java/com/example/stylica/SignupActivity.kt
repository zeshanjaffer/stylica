

package com.example.stylica
import android.widget.RadioGroup
import android.widget.RadioButton
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import com.example.stylica.data.db.DatabaseHelper

class SignupActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()
        dbHelper = DatabaseHelper(this) // 👈 DATABASE CONNECTED

        val firstNameEt = findViewById<EditText>(R.id.firstnameInput)
        val lastNameEt = findViewById<EditText>(R.id.lastnameInput)
        val emailEt = findViewById<EditText>(R.id.emailInput)
        val passwordEt = findViewById<EditText>(R.id.passwordInput)
        val retypePasswordEt = findViewById<EditText>(R.id.retypePasswordInput)
        val roleGroup = findViewById<RadioGroup>(R.id.roleGroup)

        val signupBtn = findViewById<Button>(R.id.registerBtn)
        val loginBtn = findViewById<Button>(R.id.loginBtn)

        // 🔁 Back to login
        loginBtn.setOnClickListener {
            finish()
        }

        // ✅ SIGNUP LOGIC
        signupBtn.setOnClickListener {

            val firstName = firstNameEt.text.toString().trim()
            val lastName = lastNameEt.text.toString().trim()
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            val retypePassword = retypePasswordEt.text.toString().trim()

            // 🔐 VALIDATIONS
            if (firstName.isEmpty() ||
                lastName.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                retypePassword.isEmpty()
            ) {
                toast("All fields are required")
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

            // 📦 DATABASE INSERT
            val success = dbHelper.registerUser(
                firstName,
                lastName,
                email,
                password,
                role
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
