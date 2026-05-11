package com.example.stylica

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import com.example.stylica.data.db.DatabaseHelper
import android.widget.TextView
import android.database.Cursor
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var captchaAnswer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        installSplashScreen()
        supportActionBar?.hide()
        //  Database connection
        dbHelper = DatabaseHelper(this)

        //  Input fields
        val emailEt = findViewById<EditText>(R.id.emailInput)
        val passwordEt = findViewById<EditText>(R.id.passwordInput)
        val captchaQuestion = findViewById<TextView>(R.id.captchaQuestion)
        val captchaInput = findViewById<EditText>(R.id.captchaInput)

        // 🔘 Buttons
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val num1 = (1..9).random()
        val num2 = (1..9).random()

        captchaAnswer = num1 + num2

        captchaQuestion.text = "$num1 + $num2 = ?"

        val sharedPref = getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)

        // AUTO-LOGIN LOGIC
        if (sharedPref.getBoolean("is_logged_in", false)) {
            val email = sharedPref.getString("email", "") ?: ""
            val firstName = sharedPref.getString("first_name", "") ?: ""
            val lastName = sharedPref.getString("last_name", "") ?: ""
            val role = sharedPref.getString("role", "") ?: "user"

            val intent = when (role) {
                "admin" -> Intent(this, AdminDashboardActivity::class.java)
                "moderator" -> Intent(this, ModeratorDashboardActivity::class.java)
                else -> Intent(this, HomeActivity::class.java)
            }
            intent.putExtra("FIRST", firstName)
            intent.putExtra("LAST", lastName)
            intent.putExtra("EMAIL", email)

            startActivity(intent)
            finish()
            return
        }

        //  LOGIN LOGIC
        loginBtn.setOnClickListener {

            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            val captcha = captchaInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Email and Password required")
                return@setOnClickListener
            }

            if (captcha.isEmpty()) {
                toast("Enter captcha")
                return@setOnClickListener
            }

            if (captcha.toInt() != captchaAnswer) {
                toast("Captcha incorrect")
                return@setOnClickListener
            }

            dbHelper.loginUser(email, password)?.use { cursor ->
                if (cursor.moveToFirst()) {

                    val firstName = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME)
                    )

                    val lastName = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME)
                    )

                    val role = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE)
                    )

                    // Save Session
                    with(sharedPref.edit()) {
                        putBoolean("is_logged_in", true)
                        putString("email", email)
                        putString("first_name", firstName)
                        putString("last_name", lastName)
                        putString("role", role)
                        apply()
                    }

                    toast("Welcome $firstName $lastName")
                    val intent = when (role) {
                        "admin" -> Intent(this, AdminDashboardActivity::class.java)
                        "moderator" -> Intent(this, ModeratorDashboardActivity::class.java)
                        else -> Intent(this, HomeActivity::class.java)                }
                    intent.putExtra("FIRST", firstName)
                    intent.putExtra("LAST", lastName)
                    intent.putExtra("EMAIL", email)

                    startActivity(intent)
                    finish()

                } else {

                    toast("No such record exists")

                }
            } ?: toast("No such record exists")

        }
        //  REGISTER NAVIGATION (THIS YOU ALREADY HAD)
        registerBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
