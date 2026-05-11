package com.example.stylica

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.stylica.data.db.DatabaseHelper

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val dbHelper = DatabaseHelper(this)
        val paymentMethod = intent.getStringExtra("payment_method") ?: "Online Payment"
        val tvPaymentTitle = findViewById<TextView>(R.id.tvPaymentTitle)
        val etAccountNumber = findViewById<EditText>(R.id.etAccountNumber)
        val etPin = findViewById<EditText>(R.id.etPin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnPayNow = findViewById<Button>(R.id.btnPayNow)

        tvPaymentTitle.text = "Pay via $paymentMethod"

        when (paymentMethod) {
            "EasyPaisa" -> {
                etAccountNumber.hint = "EasyPaisa Mobile Number"
                etPin.hint = "5-Digit MPIN"
            }
            "JazzCash" -> {
                etAccountNumber.hint = "JazzCash Mobile Number"
                etPin.hint = "4-Digit MPIN"
            }
            else -> {
                etAccountNumber.hint = "Account / Card Number"
                etPin.hint = "PIN / CVV"
            }
        }

        btnPayNow.setOnClickListener {
            if (etAccountNumber.text.isEmpty() || etPin.text.isEmpty()) {
                Toast.makeText(this, "Please enter details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulate processing
            progressBar.visibility = View.VISIBLE
            btnPayNow.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show()

                setResult(RESULT_OK, Intent())
                finish()
            }, 2000) // 2 seconds delay
        }
    }
}
