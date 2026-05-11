package com.example.stylica

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class ModeratorQualityFragment : Fragment() {

    data class QaProduct(
        val id: Int,
        val name: String,
        val price: Double,
        val imageUri: String?
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_moderator_qa, container, false)
        val dbHelper = DatabaseHelper(requireContext())
        val gridView = view.findViewById<GridView>(R.id.moderatorQaGrid)
        val moderatorEmail = requireActivity().intent.getStringExtra("EMAIL") ?: ""

        val cursor = dbHelper.getPendingQaForModerator(moderatorEmail)
        val products = ArrayList<QaProduct>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_NAME))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE))
                val imgIdx = cursor.getColumnIndex(DatabaseHelper.COL_PRODUCT_IMAGE)
                val imageUri = if (imgIdx != -1 && !cursor.isNull(imgIdx)) cursor.getString(imgIdx) else null
                products.add(QaProduct(id, name, price, imageUri))
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = object : BaseAdapter() {
            override fun getCount() = products.size
            override fun getItem(position: Int) = products[position]
            override fun getItemId(position: Int) = products[position].id.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val row = convertView ?: layoutInflater.inflate(R.layout.item_pending_product, parent, false)
                val nameTv = row.findViewById<TextView>(R.id.productNameTv)
                val priceTv = row.findViewById<TextView>(R.id.productPriceTv)
                val productImageView = row.findViewById<ImageView>(R.id.productImageView)
                val approveBtn = row.findViewById<Button>(R.id.approveBtn)
                val editBtn = row.findViewById<ImageView>(R.id.editBtn)
                val deleteBtn = row.findViewById<ImageView>(R.id.deleteBtn)
                editBtn.visibility = View.GONE
                deleteBtn.visibility = View.GONE

                val product = products[position]
                nameTv.text = product.name
                priceTv.text = "Rs. ${product.price}"
                approveBtn.text = "Approve quality"

                if (!product.imageUri.isNullOrEmpty()) {
                    try {
                        val uriString = product.imageUri.split(",").firstOrNull() ?: ""
                        if (uriString.isNotEmpty()) {
                            val uri = Uri.parse(uriString)
                            val inputStream = row.context.contentResolver.openInputStream(uri)
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

                approveBtn.setOnClickListener {
                    val n = dbHelper.moderatorApproveProductQuality(product.id, moderatorEmail)
                    if (n > 0) {
                        Toast.makeText(context, "Sent to admin for final approval", Toast.LENGTH_SHORT).show()
                        requireActivity().recreate()
                    } else {
                        Toast.makeText(context, "Could not update product", Toast.LENGTH_SHORT).show()
                    }
                }

                return row
            }
        }
        gridView.adapter = adapter

        val empty = view.findViewById<TextView>(R.id.tvEmptyModeratorQa)
        if (products.isEmpty()) {
            empty.visibility = View.VISIBLE
            gridView.visibility = View.GONE
        } else {
            empty.visibility = View.GONE
            gridView.visibility = View.VISIBLE
        }

        return view
    }
}
