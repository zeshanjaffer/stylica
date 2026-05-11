package com.example.stylica

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.stylica.data.db.Product

interface OnProductActionListener {
    fun onEditClick(product: Product)
    fun onDeleteClick(product: Product)
}

class ModeratorProductAdapter(
    context: Context,
    private val products: List<Product>,
    private val listener: OnProductActionListener
) : ArrayAdapter<Product>(context, 0, products) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_moderator_product, parent, false)

        val product = products[position]

        val nameTv = view.findViewById<TextView>(R.id.productNameTv)
        val categoryTv = view.findViewById<TextView>(R.id.productCategoryTv)
        val priceTv = view.findViewById<TextView>(R.id.productPriceTv)
        val statusTv = view.findViewById<TextView>(R.id.productStatusTv)
        val imageView = view.findViewById<ImageView>(R.id.productImageView)
        
        val btnEdit = view.findViewById<Button>(R.id.btnEditProduct)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteProduct)

        nameTv.text = product.name
        categoryTv.text = "Category: ${product.category}"
        priceTv.text = "$${product.price}"
        
        statusTv.text = product.status.uppercase()
        when (product.status.lowercase()) {
            "approved" -> statusTv.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            "pending" -> statusTv.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            "rejected" -> statusTv.setTextColor(android.graphics.Color.parseColor("#F44336"))
            else -> statusTv.setTextColor(android.graphics.Color.parseColor("#000000"))
        }

        if (!product.imageUri.isNullOrEmpty()) {
            try {
                imageView.setImageURI(Uri.parse(product.imageUri))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        btnEdit.setOnClickListener {
            listener.onEditClick(product)
        }

        btnDelete.setOnClickListener {
            listener.onDeleteClick(product)
        }

        return view
    }
}
