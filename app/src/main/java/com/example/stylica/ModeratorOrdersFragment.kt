package com.example.stylica

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class ModeratorOrdersFragment : Fragment() {

    data class ModOrderItem(
        val orderId: Int,
        val productName: String,
        val productPrice: Double,
        val imageUri: String?,
        val status: String,
        val userEmail: String,
        val size: String,
        val color: String,
        val paymentMethod: String,
        val courier: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)
        val dbHelper = DatabaseHelper(requireContext())
        val ordersListView = view.findViewById<ListView>(R.id.ordersListView)
        val moderatorEmail = requireActivity().intent.getStringExtra("EMAIL") ?: ""

        val ordersCursor = dbHelper.getOrdersForModerator(moderatorEmail)
        val ordersList = ArrayList<ModOrderItem>()

        if (ordersCursor.moveToFirst()) {
            do {
                val orderId = ordersCursor.getInt(ordersCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ID))
                val productId = ordersCursor.getInt(ordersCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_PRODUCT_ID))
                val userEmail = ordersCursor.getString(ordersCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_USER_EMAIL))
                val status = ordersCursor.getString(ordersCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS))

                val sizeIndex = ordersCursor.getColumnIndex("selected_size")
                val size = if (sizeIndex != -1 && !ordersCursor.isNull(sizeIndex)) ordersCursor.getString(sizeIndex) else ""

                val colorIndex = ordersCursor.getColumnIndex("selected_color")
                val color = if (colorIndex != -1 && !ordersCursor.isNull(colorIndex)) ordersCursor.getString(colorIndex) else ""

                val paymentIndex = ordersCursor.getColumnIndex(DatabaseHelper.COL_ORDER_PAYMENT)
                val paymentMethod = if (paymentIndex != -1 && !ordersCursor.isNull(paymentIndex)) ordersCursor.getString(paymentIndex) else "Unknown"

                val courierIdx = ordersCursor.getColumnIndex(DatabaseHelper.COL_ORDER_COURIER)
                val courier = if (courierIdx != -1 && !ordersCursor.isNull(courierIdx)) ordersCursor.getString(courierIdx) else ""

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

                ordersList.add(
                    ModOrderItem(orderId, productName, price, imageUri, status, userEmail, size, color, paymentMethod, courier)
                )
            } while (ordersCursor.moveToNext())
        }
        ordersCursor.close()

        val adapter = object : BaseAdapter() {
            override fun getCount() = ordersList.size
            override fun getItem(position: Int) = ordersList[position]
            override fun getItemId(position: Int) = ordersList[position].orderId.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val itemView = convertView ?: layoutInflater.inflate(R.layout.item_order, parent, false)

                val productNameTv = itemView.findViewById<TextView>(R.id.productNameTv)
                val productPriceTv = itemView.findViewById<TextView>(R.id.productPriceTv)
                val orderInfoTv = itemView.findViewById<TextView>(R.id.orderInfoTv)
                val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)

                val acceptBtn = itemView.findViewById<Button>(R.id.acceptBtn)
                val rejectBtn = itemView.findViewById<Button>(R.id.rejectBtn)
                val deliverBtn = itemView.findViewById<Button>(R.id.deliverBtn)

                val order = ordersList[position]

                productNameTv.text = order.productName
                productPriceTv.text = "Rs. ${order.productPrice}"

                val infoList = mutableListOf("Order #${order.orderId}")
                infoList.add("Buyer: ${order.userEmail}")
                infoList.add("Status: ${order.status}")
                if (order.size.isNotEmpty()) infoList.add("Size: ${order.size}")
                if (order.color.isNotEmpty()) infoList.add("Color: ${order.color}")
                val paymentDisplay = if (order.paymentMethod == "Cash on Delivery") "Cash on Delivery" else "Payment done"
                infoList.add("Payment: $paymentDisplay")
                if (order.courier.isNotEmpty()) infoList.add("Courier: ${order.courier}")
                orderInfoTv.text = infoList.joinToString(" | ")

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

                acceptBtn.setOnClickListener {
                    dbHelper.updateOrderStatus(order.orderId, "Accepted")
                    Toast.makeText(context, "Order Accepted", Toast.LENGTH_SHORT).show()
                    requireActivity().recreate()
                }

                rejectBtn.setOnClickListener {
                    dbHelper.updateOrderStatus(order.orderId, "Rejected")
                    Toast.makeText(context, "Order Rejected", Toast.LENGTH_SHORT).show()
                    requireActivity().recreate()
                }

                deliverBtn.setOnClickListener {
                    dbHelper.updateOrderStatus(order.orderId, "Delivered")
                    Toast.makeText(context, "Order Delivered", Toast.LENGTH_SHORT).show()
                    requireActivity().recreate()
                }

                return itemView
            }
        }

        ordersListView.adapter = adapter

        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyAdminOrders)
        if (ordersList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            ordersListView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            ordersListView.visibility = View.VISIBLE
        }

        return view
    }
}
