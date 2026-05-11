package com.example.stylica

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper
import android.content.Intent
import android.net.Uri
import androidx.core.widget.addTextChangedListener

class HomeFragment : Fragment(R.layout.fragment_home_screen) {



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = DatabaseHelper(requireContext())
        val listView = view.findViewById<GridView>(R.id.approvedListView)
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        val btnCategoryClothes = view.findViewById<LinearLayout>(R.id.btnCategoryClothes)
        val btnCategoryBeauty = view.findViewById<LinearLayout>(R.id.btnCategoryBeauty)

        btnCategoryClothes.setOnClickListener {
            val intent = Intent(requireContext(), CategoryProductsActivity::class.java)
            intent.putExtra("category", "Clothes")
            startActivity(intent)
        }

        btnCategoryBeauty.setOnClickListener {
            val intent = Intent(requireContext(), CategoryProductsActivity::class.java)
            intent.putExtra("category", "Makeup")
            startActivity(intent)
        }

        val cursor = dbHelper.getApprovedProducts()

        val products = ArrayList<ProductItem>()
        val filteredList = ArrayList<ProductItem>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_ID)
                )

                val name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_NAME)
                )

                val price = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE)
                )

                val description = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_DESC)
                )

                val category = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY)
                )

                val status = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATUS)
                )

                val imageUri = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_IMAGE)
                )


                products.add(ProductItem(id, name, price,description,category,status,imageUri))

            } while (cursor.moveToNext())
        }

        cursor.close()
        filteredList.addAll(products)

        val adapter = object : BaseAdapter() {

            override fun getCount() = filteredList.size
            override fun getItem(position: Int) = filteredList[position]
            override fun getItemId(position: Int) = filteredList[position].id.toLong()


            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

                val itemView = convertView ?: LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_user_product, parent, false)

                val nameTv = itemView.findViewById<TextView>(R.id.productNameTv)
                val priceTv = itemView.findViewById<TextView>(R.id.productPriceTv)
                val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)
                val product = filteredList[position]
                itemView.setOnClickListener {

                    val userEmail = requireActivity()
                        .intent
                        .getStringExtra("EMAIL") ?: ""
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
                        val uri = Uri.parse(product.imageUri)
                        var inputStream = itemView.context.contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            // First decode with inJustDecodeBounds=true to check dimensions
                            val options = android.graphics.BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
                            inputStream.close()

                            // Calculate inSampleSize
                            options.inSampleSize = calculateInSampleSize(options, 200, 200) // GridView thumbnail size
                            options.inJustDecodeBounds = false

                            // Decode bitmap with inSampleSize set
                            inputStream = itemView.context.contentResolver.openInputStream(uri)
                            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
                            productImageView.setImageBitmap(bitmap)
                            inputStream?.close()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return itemView
            }
        }

        listView.adapter = adapter

        searchEditText.addTextChangedListener {

            val query = it.toString()

            filteredList.clear()

            if (query.isEmpty()) {
                filteredList.addAll(products)
            } else {
                for (product in products) {
                    if (product.name.contains(query, ignoreCase = true)) {
                        filteredList.add(product)
                    }
                }
            }

            adapter.notifyDataSetChanged()

            searchEditText.addTextChangedListener {

                val query = it.toString()

                filteredList.clear()

                if (query.isEmpty()) {
                    filteredList.addAll(products)
                } else {
                    for (product in products) {
                        if (product.name.contains(query, ignoreCase = true)) {
                            filteredList.add(product)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }

        }


    }

    private fun calculateInSampleSize(options: android.graphics.BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    data class ProductItem(
        val id: Int,
        val name: String,
        val price: Double,
        val description: String,
        val category: String,
        val status: String,
        val imageUri: String?
    )
}