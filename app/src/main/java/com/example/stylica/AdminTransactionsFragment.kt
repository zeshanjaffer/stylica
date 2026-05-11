package com.example.stylica

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class AdminTransactionsFragment : Fragment() {

    private data class T(
        val orderId: Int,
        val productName: String,
        val productPrice: Double,
        val imageUri: String?,
        val status: String,
        val userEmail: String,
        val payment: String,
        val courier: String,
        val placedAt: String?
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_admin_transactions, container, false)
        val db = DatabaseHelper(requireContext())
        val lv = view.findViewById<ListView>(R.id.lvTransactions)
        val empty = view.findViewById<TextView>(R.id.tvEmptyTxn)
        val etUser = view.findViewById<EditText>(R.id.etTxnUser)
        val etDate = view.findViewById<EditText>(R.id.etTxnDate)
        val list = ArrayList<T>()

        val adapter = object : BaseAdapter() {
            override fun getCount() = list.size
            override fun getItem(p: Int) = list[p]
            override fun getItemId(p: Int) = list[p].orderId.toLong()
            override fun getView(p: Int, cv: View?, parent: ViewGroup?): View {
                val itemView = cv ?: layoutInflater.inflate(R.layout.item_order, parent, false)
                val o = list[p]
                itemView.findViewById<TextView>(R.id.productNameTv).text = o.productName
                itemView.findViewById<TextView>(R.id.productPriceTv).text = "Rs. ${o.productPrice}"
                val info = mutableListOf("Order #${o.orderId}", o.userEmail, "Status: ${o.status}", "Pay: ${o.payment}")
                if (o.courier.isNotEmpty()) info.add("Courier: ${o.courier}")
                if (!o.placedAt.isNullOrEmpty()) info.add("At: ${o.placedAt}")
                itemView.findViewById<TextView>(R.id.orderInfoTv).text = info.joinToString(" | ")
                itemView.findViewById<Button>(R.id.acceptBtn).visibility = View.GONE
                itemView.findViewById<Button>(R.id.rejectBtn).visibility = View.GONE
                itemView.findViewById<Button>(R.id.deliverBtn).visibility = View.GONE
                val iv = itemView.findViewById<ImageView>(R.id.productImageView)
                if (!o.imageUri.isNullOrEmpty()) {
                    try {
                        val u = o.imageUri.split(",").firstOrNull() ?: ""
                        if (u.isNotEmpty()) {
                            val stream = itemView.context.contentResolver.openInputStream(Uri.parse(u))
                            if (stream != null) {
                                iv.setImageBitmap(android.graphics.BitmapFactory.decodeStream(stream))
                                stream.close()
                            }
                        }
                    } catch (_: Exception) { }
                }
                return itemView
            }
        }
        lv.adapter = adapter

        fun load() {
            list.clear()
            val c = db.searchOrdersForAdmin(etUser.text.toString(), etDate.text.toString())
            if (c.moveToFirst()) {
                do {
                    val orderId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ID))
                    val productId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_PRODUCT_ID))
                    val userEmail = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_USER_EMAIL))
                    val status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS))
                    val payI = c.getColumnIndex(DatabaseHelper.COL_ORDER_PAYMENT)
                    val pay = if (payI != -1 && !c.isNull(payI)) c.getString(payI) else ""
                    val crI = c.getColumnIndex(DatabaseHelper.COL_ORDER_COURIER)
                    val courier = if (crI != -1 && !c.isNull(crI)) c.getString(crI) else ""
                    val atI = c.getColumnIndex(DatabaseHelper.COL_ORDER_PLACED_AT)
                    val at = if (atI != -1 && !c.isNull(atI)) c.getString(atI) else null
                    var productName = "Unknown"
                    var price = 0.0
                    var imageUri: String? = null
                    val pCur = db.readableDatabase.rawQuery(
                        "SELECT ${DatabaseHelper.COL_PRODUCT_NAME}, ${DatabaseHelper.COL_PRICE}, ${DatabaseHelper.COL_PRODUCT_IMAGE} FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE ${DatabaseHelper.COL_PRODUCT_ID} = ?",
                        arrayOf(productId.toString())
                    )
                    if (pCur.moveToFirst()) {
                        productName = pCur.getString(0)
                        price = pCur.getDouble(1)
                        imageUri = pCur.getString(2)
                    }
                    pCur.close()
                    list.add(T(orderId, productName, price, imageUri, status, userEmail, pay, courier, at))
                } while (c.moveToNext())
            }
            c.close()
            adapter.notifyDataSetChanged()
            empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            lv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        lv.setOnItemClickListener { _, _, pos, _ ->
            val o = list[pos]
            val etPay = EditText(requireContext()).apply {
                setText(o.payment)
                setPadding(48, 32, 48, 32)
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Order #${o.orderId} — payment / delete")
                .setView(etPay)
                .setPositiveButton("Update payment") { _, _ ->
                    db.updateOrderPayment(o.orderId, etPay.text.toString().trim())
                    Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                    load()
                }
                .setNeutralButton("Delete") { _, _ ->
                    db.deleteOrder(o.orderId)
                    load()
                }
                .setNegativeButton("Close", null)
                .show()
        }

        view.findViewById<Button>(R.id.btnTxnSearch).setOnClickListener { load() }
        load()
        return view
    }
}
