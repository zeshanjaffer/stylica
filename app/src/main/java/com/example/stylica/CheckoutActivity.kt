package com.example.stylica

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.stylica.data.db.DatabaseHelper

class CheckoutActivity : AppCompatActivity() {

    private lateinit var paymentLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val dbHelper = DatabaseHelper(this)

        val isCart = intent.getBooleanExtra("is_cart", false)
        val userEmail = intent.getStringExtra("user_email") ?: ""
        val productId = intent.getIntExtra("product_id", -1)
        val quantity = intent.getIntExtra("quantity", 1)
        val selectedSize = intent.getStringExtra("selected_size") ?: ""
        val selectedColor = intent.getStringExtra("selected_color") ?: ""

        // 🔥 Show product summary
        val tvProductName = findViewById<TextView>(R.id.tvProductName)
        val tvProductPrice = findViewById<TextView>(R.id.tvProductPrice)

        if (isCart) {
            val cursor = dbHelper.getUserCart(userEmail)
            var totalPrice = 0.0
            var itemCount = 0
            while (cursor.moveToNext()) {
                val pId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"))
                val price = dbHelper.getProductPriceById(pId)
                totalPrice += price
                itemCount++
            }
            cursor.close()
            
            tvProductName.text = "Cart Checkout ($itemCount items)"
            tvProductPrice.text = "Total: Rs. $totalPrice"
        } else {
            // Fetch product from database
            val cursor = dbHelper.readableDatabase.rawQuery(
                "SELECT product_name, price FROM products WHERE product_id = ?",
                arrayOf(productId.toString())
            )

            if (cursor.moveToFirst()) {
                val productName = cursor.getString(0)
                val productPrice = cursor.getDouble(1)

                tvProductName.text = "Product: $productName"
                tvProductPrice.text = "Price: Rs. $productPrice"
            }
            cursor.close()
        }

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val spinnerPayment = findViewById<Spinner>(R.id.spinnerPayment)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        // Payment options
        val paymentOptions = arrayOf("Cash on Delivery", "Bank Transfer", "EasyPaisa", "JazzCash")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            paymentOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPayment.adapter = adapter

        // Launcher for PaymentActivity
        paymentLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Payment was successful, process the order
                val name = etName.text.toString()
                val phone = etPhone.text.toString()
                val address = etAddress.text.toString()
                val paymentMethod = spinnerPayment.selectedItem.toString()
                
                processOrder(isCart, userEmail, productId, quantity, name, phone, address, paymentMethod, "Confirmed", selectedSize, selectedColor, dbHelper)
            } else {
                Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        btnConfirm.setOnClickListener {

            val name = etName.text.toString()
            val phone = etPhone.text.toString()
            val address = etAddress.text.toString()
            val paymentMethod = spinnerPayment.selectedItem.toString()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (paymentMethod != "Cash on Delivery") {
                // Launch mock payment screen
                val intent = android.content.Intent(this, PaymentActivity::class.java)
                intent.putExtra("payment_method", paymentMethod)
                paymentLauncher.launch(intent)
            } else {
                // Process order immediately for COD
                processOrder(isCart, userEmail, productId, quantity, name, phone, address, paymentMethod, "Confirmed", selectedSize, selectedColor, dbHelper)
            }
        }
    }

    private fun processOrder(
        isCart: Boolean,
        userEmail: String,
        productId: Int,
        quantity: Int,
        name: String,
        phone: String,
        address: String,
        paymentMethod: String,
        status: String,
        size: String,
        color: String,
        dbHelper: DatabaseHelper
    ) {
        if (isCart) {
            val cursor = dbHelper.getUserCart(userEmail)
            while (cursor.moveToNext()) {
                val pId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"))
                val qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                
                val sizeIndex = cursor.getColumnIndex("selected_size")
                val itemSize = if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) cursor.getString(sizeIndex) else ""
                
                val colorIndex = cursor.getColumnIndex("selected_color")
                val itemColor = if (colorIndex != -1 && !cursor.isNull(colorIndex)) cursor.getString(colorIndex) else ""
                
                dbHelper.insertOrder(
                    pId,
                    userEmail,
                    name,
                    phone,
                    address,
                    paymentMethod,
                    status,
                    itemSize,
                    itemColor
                )
                dbHelper.reduceInventory(pId, qty)
            }
            cursor.close()
            dbHelper.clearCart(userEmail)
        } else {
            dbHelper.insertOrder(
                productId,
                userEmail,
                name,
                phone,
                address,
                paymentMethod,
                status,
                size,
                color
            )
            dbHelper.reduceInventory(productId, quantity)
            dbHelper.removeFromCart(productId, userEmail)
        }
        
        Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
        finish()
    }
}