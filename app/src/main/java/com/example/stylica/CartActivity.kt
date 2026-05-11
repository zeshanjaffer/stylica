package com.example.stylica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper
import android.widget.TextView
import android.widget.Button
import android.widget.ListView
import android.widget.BaseAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.net.Uri
import android.widget.ImageView

class CartFragment : Fragment(R.layout.activity_cart) {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userEmail: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        // 🔥 Get email from arguments instead of intent
        userEmail = arguments?.getString("user_email") ?: ""

        val placeOrderBtn = view.findViewById<Button>(R.id.placeOrderBtn)
        placeOrderBtn.setOnClickListener {
            val cursor = dbHelper.getUserCart(userEmail)
            val itemCount = cursor.count
            cursor.close()
            
            if (itemCount > 0) {
                val intent = Intent(requireContext(), CheckoutActivity::class.java)
                intent.putExtra("is_cart", true)
                intent.putExtra("user_email", userEmail)
                startActivity(intent)
            } else {
                android.widget.Toast.makeText(requireContext(), "Cart is empty", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadCartItems(it) }
    }

    data class CartItem(
        val productId: Int,
        val productName: String,
        val productPrice: Double,
        val imageUri: String?,
        var quantity: Int,
        val size: String,
        val color: String
    )

    private fun loadCartItems(view: View) {

        val cursor = dbHelper.getUserCart(userEmail)

        val cartItems = ArrayList<CartItem>()
        var totalPrice = 0.0

        while (cursor.moveToNext()) {

            val productId = cursor.getInt(
                cursor.getColumnIndexOrThrow("product_id")
            )
            
            val qtyIndex = cursor.getColumnIndex("quantity")
            val quantity = if (qtyIndex != -1) cursor.getInt(qtyIndex) else 1

            val sizeIndex = cursor.getColumnIndex("selected_size")
            val size = if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) cursor.getString(sizeIndex) else ""

            val colorIndex = cursor.getColumnIndex("selected_color")
            val color = if (colorIndex != -1 && !cursor.isNull(colorIndex)) cursor.getString(colorIndex) else ""

            var productName = "Unknown"
            var price = 0.0
            var imageUri: String? = null
            
            val pCursor = dbHelper.readableDatabase.rawQuery(
                "SELECT ${DatabaseHelper.COL_PRODUCT_NAME}, ${DatabaseHelper.COL_PRICE}, ${DatabaseHelper.COL_PRODUCT_IMAGE} FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE ${DatabaseHelper.COL_PRODUCT_ID} = ?",
                arrayOf(productId.toString())
            )
            if (pCursor.moveToFirst()) {
                productName = pCursor.getString(0)
                price = pCursor.getDouble(1)
                imageUri = pCursor.getString(2)
            }
            pCursor.close()

            totalPrice += (price * quantity)

            cartItems.add(CartItem(productId, productName, price, imageUri, quantity, size, color))
        }

        cursor.close()

        val listView = view.findViewById<ListView>(R.id.cartListView)
        listView.adapter = object : BaseAdapter() {
            override fun getCount() = cartItems.size
            override fun getItem(position: Int) = cartItems[position]
            override fun getItemId(position: Int) = cartItems[position].productId.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val itemView = convertView ?: LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_cart_product, parent, false)

                val nameTv = itemView.findViewById<TextView>(R.id.productNameTv)
                val priceTv = itemView.findViewById<TextView>(R.id.productPriceTv)
                val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)
                val quantityTv = itemView.findViewById<TextView>(R.id.quantityTv)
                val minusBtn = itemView.findViewById<ImageView>(R.id.minusBtn)
                val plusBtn = itemView.findViewById<ImageView>(R.id.plusBtn)
                val removeBtn = itemView.findViewById<ImageView>(R.id.removeBtn)
                val selectionTv = itemView.findViewById<TextView>(R.id.productSelectionTv)
                
                val item = cartItems[position]

                nameTv.text = item.productName
                priceTv.text = "Rs. ${item.productPrice}"
                quantityTv.text = item.quantity.toString()

                if (item.size.isNotEmpty() || item.color.isNotEmpty()) {
                    selectionTv.visibility = View.VISIBLE
                    val selectionText = mutableListOf<String>()
                    if (item.size.isNotEmpty()) selectionText.add("Size: ${item.size}")
                    if (item.color.isNotEmpty()) selectionText.add("Color: ${item.color}")
                    selectionTv.text = selectionText.joinToString(" | ")
                } else {
                    selectionTv.visibility = View.GONE
                }

                if (!item.imageUri.isNullOrEmpty()) {
                    try {
                        val uriString = item.imageUri.split(",").firstOrNull() ?: ""
                        if (uriString.isNotEmpty()) {
                            val uri = Uri.parse(uriString)
                            val inputStream = itemView.context.contentResolver.openInputStream(uri)
                            if (inputStream != null) {
                                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                productImageView.setImageBitmap(bitmap)
                                inputStream.close()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                plusBtn.setOnClickListener {
                    val newQty = item.quantity + 1
                    dbHelper.updateCartQuantity(item.productId, userEmail, newQty)
                    loadCartItems(view)
                }

                minusBtn.setOnClickListener {
                    if (item.quantity > 1) {
                        val newQty = item.quantity - 1
                        dbHelper.updateCartQuantity(item.productId, userEmail, newQty)
                        loadCartItems(view)
                    }
                }

                removeBtn.setOnClickListener {
                    dbHelper.removeFromCart(item.productId, userEmail)
                    loadCartItems(view)
                }

                return itemView
            }
        }

        val totalText = view.findViewById<TextView>(R.id.totalPriceText)
        totalText.text = "Total: Rs. $totalPrice"
    }
}