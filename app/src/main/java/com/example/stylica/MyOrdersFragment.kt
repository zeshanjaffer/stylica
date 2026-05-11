package com.example.stylica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class MyOrdersFragment : Fragment() {

    data class OrderItem(
        val orderId: Int,
        val productId: Int,
        val productName: String,
        val productPrice: Double,
        val imageUri: String?,
        val status: String,
        val size: String,
        val color: String,
        val courier: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_my_orders, container, false)
        val dbHelper = DatabaseHelper(requireContext())
        val gridView = view.findViewById<GridView>(R.id.myOrdersListView)

        val userEmail = requireActivity().intent.getStringExtra("EMAIL") ?: ""
        val cursor = dbHelper.getOrdersByUser(userEmail)
        val ordersList = ArrayList<OrderItem>()

        if (cursor.moveToFirst()) {
            do {
                val orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ID))
                val productId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_PRODUCT_ID))
                val status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS))
                
                val sizeIndex = cursor.getColumnIndex("selected_size")
                val size = if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) cursor.getString(sizeIndex) else ""
                
                val colorIndex = cursor.getColumnIndex("selected_color")
                val color = if (colorIndex != -1 && !cursor.isNull(colorIndex)) cursor.getString(colorIndex) else ""

                val courierIdx = cursor.getColumnIndex(DatabaseHelper.COL_ORDER_COURIER)
                val courier = if (courierIdx != -1 && !cursor.isNull(courierIdx)) cursor.getString(courierIdx) else ""

                // Fetch product details
                var productName = "Unknown"
                var productPrice = 0.0
                var imageUri: String? = null
                
                val pCursor = dbHelper.readableDatabase.rawQuery(
                    "SELECT ${DatabaseHelper.COL_PRODUCT_NAME}, ${DatabaseHelper.COL_PRICE}, ${DatabaseHelper.COL_PRODUCT_IMAGE} FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE ${DatabaseHelper.COL_PRODUCT_ID} = ?",
                    arrayOf(productId.toString())
                )
                if (pCursor.moveToFirst()) {
                    productName = pCursor.getString(0)
                    productPrice = pCursor.getDouble(1)
                    imageUri = pCursor.getString(2)
                }
                pCursor.close()

                ordersList.add(OrderItem(orderId, productId, productName, productPrice, imageUri, status, size, color, courier))
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = object : BaseAdapter() {
            override fun getCount() = ordersList.size
            override fun getItem(position: Int) = ordersList[position]
            override fun getItemId(position: Int) = ordersList[position].orderId.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val itemView = convertView ?: LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_user_product, parent, false)

                val nameTv = itemView.findViewById<TextView>(R.id.productNameTv)
                val categoryTv = itemView.findViewById<TextView>(R.id.productCategoryTv)
                val priceTv = itemView.findViewById<TextView>(R.id.productPriceTv)
                val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)
                val order = ordersList[position]

                nameTv.text = order.productName
                priceTv.text = "Rs. ${order.productPrice}"
                
                categoryTv.visibility = View.VISIBLE
                
                val infoList = mutableListOf("Status: ${order.status}")
                if (order.size.isNotEmpty()) infoList.add("Size: ${order.size}")
                if (order.color.isNotEmpty()) infoList.add("Color: ${order.color}")
                if (order.courier.isNotEmpty()) infoList.add("Courier: ${order.courier}")
                
                categoryTv.text = infoList.joinToString(" | ")

                if (!order.imageUri.isNullOrEmpty()) {
                    try {
                        val uriString = order.imageUri.split(",").firstOrNull() ?: ""
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

                itemView.setOnClickListener {
                    // Navigate to product details
                    val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
                    intent.putExtra("id", order.productId)
                    intent.putExtra("name", order.productName)
                    intent.putExtra("price", order.productPrice)
                    intent.putExtra("image", order.imageUri)
                    intent.putExtra("user_email", userEmail)
                    startActivity(intent)
                }

                return itemView
            }
        }

        gridView.adapter = adapter
        
        val tvEmptyOrders = view.findViewById<android.widget.TextView>(R.id.tvEmptyOrders)
        if (ordersList.isEmpty()) {
            tvEmptyOrders.visibility = View.VISIBLE
            gridView.visibility = View.GONE
        } else {
            tvEmptyOrders.visibility = View.GONE
            gridView.visibility = View.VISIBLE
        }
        
        return view
    }
}