package com.example.stylica

import android.content.Intent
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
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = DatabaseHelper(requireContext())
        val listView = view.findViewById<ListView>(R.id.favoritesListView)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyFavorites)

        val userEmail = requireActivity().intent.getStringExtra("EMAIL") ?: ""

        val cursor = dbHelper.getUserFavorites(userEmail)

        val products = ArrayList<HomeFragment.ProductItem>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_NAME))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE))
                
                val descIndex = cursor.getColumnIndex(DatabaseHelper.COL_PRODUCT_DESC)
                val description = if (descIndex != -1 && !cursor.isNull(descIndex)) cursor.getString(descIndex) else ""
                
                val categoryIndex = cursor.getColumnIndex(DatabaseHelper.COL_CATEGORY)
                val category = if (categoryIndex != -1 && !cursor.isNull(categoryIndex)) cursor.getString(categoryIndex) else ""
                
                val statusIndex = cursor.getColumnIndex(DatabaseHelper.COL_STATUS)
                val status = if (statusIndex != -1 && !cursor.isNull(statusIndex)) cursor.getString(statusIndex) else ""
                
                val imageIndex = cursor.getColumnIndex(DatabaseHelper.COL_PRODUCT_IMAGE)
                val imageUri = if (imageIndex != -1 && !cursor.isNull(imageIndex)) cursor.getString(imageIndex) else null

                products.add(HomeFragment.ProductItem(id, name, price, description, category, status, imageUri))
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (products.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            listView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            listView.visibility = View.VISIBLE

            val adapter = object : BaseAdapter() {
                override fun getCount() = products.size
                override fun getItem(position: Int) = products[position]
                override fun getItemId(position: Int) = products[position].id.toLong()

                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val itemView = convertView ?: LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_user_product, parent, false)

                    val nameTv = itemView.findViewById<TextView>(R.id.productNameTv)
                    val priceTv = itemView.findViewById<TextView>(R.id.productPriceTv)
                    val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)
                    val product = products[position]

                    itemView.setOnClickListener {
                        val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
                        intent.putExtra("id", product.id)
                        intent.putExtra("name", product.name)
                        intent.putExtra("description", product.description)
                        intent.putExtra("price", product.price)
                        intent.putExtra("category", product.category)
                        intent.putExtra("status", product.status)
                        intent.putExtra("image", product.imageUri)
                        intent.putExtra("user_email", userEmail)
                        startActivity(intent)
                    }

                    nameTv.text = product.name
                    priceTv.text = "Rs. ${product.price}"

                    if (!product.imageUri.isNullOrEmpty()) {
                        try {
                            val uriString = product.imageUri.split(",").firstOrNull() ?: ""
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

                    return itemView
                }
            }
            listView.adapter = adapter
        }
    }
}
