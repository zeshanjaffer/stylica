package com.example.stylica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import com.example.stylica.data.db.DatabaseHelper

class CategoryProductsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_products)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val categoryName = intent.getStringExtra("category") ?: ""
        val categoryTitleTextView = findViewById<TextView>(R.id.categoryTitleTextView)
        categoryTitleTextView.text = "$categoryName Products"

        val dbHelper = DatabaseHelper(this)
        val listView = findViewById<GridView>(R.id.categoryProductsListView)

        val cursor = dbHelper.getApprovedProductsByCategory(categoryName)
        val products = ArrayList<ProductItem>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_NAME))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_DESC))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY))
                val status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATUS))
                val imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_IMAGE))
                
                val targetAudienceIndex = cursor.getColumnIndex(DatabaseHelper.COL_TARGET_AUDIENCE)
                val targetAudience = if (targetAudienceIndex != -1) cursor.getString(targetAudienceIndex) else "All"

                products.add(ProductItem(id, name, price, description, category, targetAudience, status, imageUri))
            } while (cursor.moveToNext())
        }

        cursor.close()

        val filteredList = ArrayList<ProductItem>()
        filteredList.addAll(products)

        var currentSearchQuery = ""
        var currentTargetAudience = "All"

        val adapter = object : BaseAdapter() {
            override fun getCount() = filteredList.size
            override fun getItem(position: Int) = filteredList[position]
            override fun getItemId(position: Int) = filteredList[position].id.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val itemView = convertView ?: LayoutInflater.from(this@CategoryProductsActivity)
                    .inflate(R.layout.item_user_product, parent, false)

                val nameTv = itemView.findViewById<TextView>(R.id.productNameTv)
                val priceTv = itemView.findViewById<TextView>(R.id.productPriceTv)
                val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)
                val product = filteredList[position]

                itemView.setOnClickListener {
                    val userEmail = intent.getStringExtra("user_email") ?: ""
                    val intent = Intent(this@CategoryProductsActivity, ProductDetailsActivity::class.java)
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
                        val inputStream = itemView.context.contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                            productImageView.setImageBitmap(bitmap)
                            inputStream.close()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                return itemView
            }
        }

        listView.adapter = adapter

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val filterLayout = findViewById<HorizontalScrollView>(R.id.targetAudienceFilterLayout)
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterMen = findViewById<TextView>(R.id.filterMen)
        val filterWomen = findViewById<TextView>(R.id.filterWomen)
        val filterJuniors = findViewById<TextView>(R.id.filterJuniors)

        if (categoryName.equals("Clothes", ignoreCase = true)) {
            filterLayout.visibility = View.VISIBLE
        } else {
            filterLayout.visibility = View.GONE
        }

        fun applyFilters() {
            filteredList.clear()
            for (p in products) {
                val matchesSearch = currentSearchQuery.isEmpty() || p.name.contains(currentSearchQuery, ignoreCase = true)
                val matchesAudience = currentTargetAudience == "All" || p.targetAudience.equals(currentTargetAudience, ignoreCase = true)
                if (matchesSearch && matchesAudience) {
                    filteredList.add(p)
                }
            }
            adapter.notifyDataSetChanged()
        }

        searchEditText.addTextChangedListener {
            currentSearchQuery = it.toString()
            applyFilters()
        }

        fun updateFilterUI(selectedView: TextView) {
            val unselectedBg = R.drawable.bg_edit_text
            val selectedBg = R.drawable.bg_btn_dark_16
            
            filterAll.setBackgroundResource(unselectedBg)
            filterAll.setTextColor(resources.getColor(R.color.black, null))
            filterMen.setBackgroundResource(unselectedBg)
            filterMen.setTextColor(resources.getColor(R.color.black, null))
            filterWomen.setBackgroundResource(unselectedBg)
            filterWomen.setTextColor(resources.getColor(R.color.black, null))
            filterJuniors.setBackgroundResource(unselectedBg)
            filterJuniors.setTextColor(resources.getColor(R.color.black, null))

            selectedView.setBackgroundResource(selectedBg)
            selectedView.setTextColor(resources.getColor(R.color.white, null))
        }

        filterAll.setOnClickListener {
            currentTargetAudience = "All"
            updateFilterUI(filterAll)
            applyFilters()
        }
        filterMen.setOnClickListener {
            currentTargetAudience = "Men"
            updateFilterUI(filterMen)
            applyFilters()
        }
        filterWomen.setOnClickListener {
            currentTargetAudience = "Women"
            updateFilterUI(filterWomen)
            applyFilters()
        }
        filterJuniors.setOnClickListener {
            currentTargetAudience = "Juniors"
            updateFilterUI(filterJuniors)
            applyFilters()
        }
    }

    data class ProductItem(
        val id: Int,
        val name: String,
        val price: Double,
        val description: String,
        val category: String,
        val targetAudience: String,
        val status: String,
        val imageUri: String?
    )
}
