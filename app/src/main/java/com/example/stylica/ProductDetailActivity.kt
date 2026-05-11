package com.example.stylica

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.stylica.data.db.DatabaseHelper

class ProductDetailsActivity : AppCompatActivity() {

    private var quantity = 1
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val dbHelper = DatabaseHelper(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnFavorite = findViewById<ImageView>(R.id.btnFavorite)
        val viewPager = findViewById<ViewPager2>(R.id.productImageViewPager)

        val productName = findViewById<TextView>(R.id.productName)
        val productDescription = findViewById<TextView>(R.id.productDescription)
        val productPrice = findViewById<TextView>(R.id.productPrice)
        val productCategory = findViewById<TextView>(R.id.productCategory)
        val productColor = findViewById<TextView>(R.id.productColor)
        val productStatus = findViewById<TextView>(R.id.productStatus)
        
        val btnMinus = findViewById<ImageView>(R.id.btnMinus)
        val btnPlus = findViewById<ImageView>(R.id.btnPlus)
        val tvQuantity = findViewById<TextView>(R.id.tvQuantity)

        val productSize = findViewById<TextView>(R.id.productSize)
        val productStock = findViewById<TextView>(R.id.productStock)
        val creatorInfoLayout = findViewById<View>(R.id.creatorInfoLayout)
        val creatorImageView = findViewById<ImageView>(R.id.creatorImageView)
        val creatorNameTv = findViewById<TextView>(R.id.creatorNameTv)

        val addToCartBtn = findViewById<Button>(R.id.addToCartBtn)
        val orderBtn = findViewById<Button>(R.id.orderNowBtn)
        val selectionContainer = findViewById<View>(R.id.selectionContainer)
        val sizeRadioGroup = findViewById<android.widget.RadioGroup>(R.id.sizeRadioGroup)
        val colorRadioGroup = findViewById<android.widget.RadioGroup>(R.id.colorRadioGroup)
        var selectedSize = ""
        var selectedColor = ""


        val productId = intent.getIntExtra("id", -1)
        val userEmail = intent.getStringExtra("user_email") ?: ""
        
        var name = ""
        var description = ""
        var price = 0.0
        var category = ""
        var status = ""
        var subCategory = ""
        var imageUriString = ""
        var targetAudience = "All"
        var inventory = 10
        var size = "N/A"
        var moderatorEmail = ""

        val cursor = dbHelper.getProductById(productId)
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUCT_NAME))
            val descIndex = cursor.getColumnIndex(DatabaseHelper.COL_PRODUCT_DESC)
            description = if (descIndex != -1 && !cursor.isNull(descIndex)) cursor.getString(descIndex) else ""
            price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PRICE))
            val catIndex = cursor.getColumnIndex(DatabaseHelper.COL_CATEGORY)
            category = if (catIndex != -1 && !cursor.isNull(catIndex)) cursor.getString(catIndex) else ""
            status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STATUS))
            
            val subCatIndex = cursor.getColumnIndex(DatabaseHelper.COL_SUBCATEGORY)
            subCategory = if (subCatIndex != -1 && !cursor.isNull(subCatIndex)) cursor.getString(subCatIndex) else ""
            
            val audIndex = cursor.getColumnIndex(DatabaseHelper.COL_TARGET_AUDIENCE)
            targetAudience = if (audIndex != -1 && !cursor.isNull(audIndex)) cursor.getString(audIndex) else "All"
            
            val invIndex = cursor.getColumnIndex(DatabaseHelper.COL_INVENTORY)
            inventory = if (invIndex != -1 && !cursor.isNull(invIndex)) cursor.getInt(invIndex) else 10
            
            val sizeIndex = cursor.getColumnIndex(DatabaseHelper.COL_SIZE)
            size = if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) cursor.getString(sizeIndex) else "N/A"
            
            val imgIndex = cursor.getColumnIndex(DatabaseHelper.COL_PRODUCT_IMAGE)
            imageUriString = if (imgIndex != -1 && !cursor.isNull(imgIndex)) cursor.getString(imgIndex) else ""
            
            val modEmailIndex = cursor.getColumnIndex(DatabaseHelper.COL_MODERATOR_EMAIL)
            moderatorEmail = if (modEmailIndex != -1 && !cursor.isNull(modEmailIndex)) cursor.getString(modEmailIndex) else ""
        }
        cursor.close()

        // Set Data
        productName.text = name
        productDescription.text = description
        productPrice.text = "Rs. $price"
        productCategory.text = category
        
        if (!subCategory.isNullOrEmpty()) {
            productColor.visibility = View.VISIBLE
            productColor.text = "Color: $subCategory"
        }
        
        if (category == "Clothes") {
            selectionContainer.visibility = View.VISIBLE
            productSize.visibility = View.GONE
            productColor.visibility = View.GONE

            // Populate Sizes
            val sizesList = size.split(",").map { it.trim() }.filter { it.isNotEmpty() && it != "N/A" }
            sizesList.forEachIndexed { index, s ->
                val rb = android.widget.RadioButton(this)
                rb.id = View.generateViewId()
                rb.text = s
                
                val layoutParams = android.widget.RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._32sdp)
                )
                if (index < sizesList.size - 1) {
                    layoutParams.marginEnd = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp)
                }
                rb.layoutParams = layoutParams
                rb.setPadding(resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._12sdp), 0, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._12sdp), 0)
                
                rb.background = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.selector_chip_size)
                rb.buttonDrawable = null
                rb.setTextColor(androidx.core.content.ContextCompat.getColorStateList(this, R.color.selector_chip_text))
                rb.gravity = android.view.Gravity.CENTER
                rb.setTypeface(null, android.graphics.Typeface.BOLD)
                sizeRadioGroup.addView(rb)
            }

            // Populate Colors
            val colorsList = subCategory.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            colorsList.forEachIndexed { index, c ->
                val rb = android.widget.RadioButton(this)
                rb.id = View.generateViewId()
                rb.text = c
                
                val layoutParams = android.widget.RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._32sdp)
                )
                if (index < colorsList.size - 1) {
                    layoutParams.marginEnd = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp)
                }
                rb.layoutParams = layoutParams
                rb.setPadding(resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._12sdp), 0, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._12sdp), 0)
                
                rb.background = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.selector_chip_size)
                rb.buttonDrawable = null
                rb.setTextColor(androidx.core.content.ContextCompat.getColorStateList(this, R.color.selector_chip_text))
                rb.gravity = android.view.Gravity.CENTER
                rb.setTypeface(null, android.graphics.Typeface.BOLD)
                colorRadioGroup.addView(rb)
            }

        } else {
            selectionContainer.visibility = View.GONE
            if (category == "Clothes" && size != "N/A" && size.isNotEmpty()) {
                productSize.visibility = View.VISIBLE
                productSize.text = "Size: $size"
            }
        }

        if (inventory > 0) {
            productStock.text = "Stock: $inventory"
        } else {
            productStock.text = "Out of Stock"
            productStock.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
            addToCartBtn.isEnabled = false
            orderBtn.isEnabled = false
            addToCartBtn.alpha = 0.5f
            orderBtn.alpha = 0.5f
        }

        val creatorUser = dbHelper.getUserByEmail(moderatorEmail)
        if (creatorUser != null) {
            creatorNameTv.text = "${creatorUser.firstName} ${creatorUser.lastName}"
            if (!creatorUser.profileImage.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(creatorUser.profileImage)
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        creatorImageView.setImageBitmap(bitmap)
                        inputStream.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            creatorInfoLayout.visibility = View.GONE
        }

        val currentUser = dbHelper.getUserByEmail(userEmail)
        if (currentUser?.role == "admin" || currentUser?.role == "moderator") {
            addToCartBtn.visibility = View.GONE
            orderBtn.visibility = View.GONE
            btnFavorite.visibility = View.GONE
        }

        // Image Slider
        val imageUris = if (imageUriString.isNotEmpty()) {
            imageUriString.split(",")
        } else {
            emptyList()
        }

        val adapter = ImageSliderAdapter(imageUris) { clickedUri ->
            showFullScreenImage(clickedUri)
        }
        viewPager.adapter = adapter

        // Back button
        btnBack.setOnClickListener { finish() }

        // Favorites
        isFavorite = dbHelper.isFavorite(productId, userEmail)
        updateFavoriteIcon(btnFavorite)

        btnFavorite.setOnClickListener {
            if (isFavorite) {
                dbHelper.removeFromFavorites(productId, userEmail)
                isFavorite = false
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.addToFavorites(productId, userEmail)
                isFavorite = true
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
            }
            updateFavoriteIcon(btnFavorite)
        }

        // Quantity
        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
            }
        }
        btnPlus.setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
        }

        // Add to Cart
        sizeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val rb = findViewById<android.widget.RadioButton>(checkedId)
            selectedSize = rb?.text?.toString() ?: ""
        }
        colorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val rb = findViewById<android.widget.RadioButton>(checkedId)
            selectedColor = rb?.text?.toString() ?: ""
        }

        addToCartBtn.setOnClickListener {
            if (category == "Clothes" && (selectedSize.isEmpty() || selectedColor.isEmpty())) {
                Toast.makeText(this, "Please select Size and Color", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dbHelper.isProductInCart(productId, userEmail)) {
                dbHelper.removeFromCart(productId, userEmail)
                addToCartBtn.text = "Add to cart"
                Toast.makeText(this, "Removed from Cart", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.addToCart(productId, userEmail, quantity, selectedSize, selectedColor)
                addToCartBtn.text = "Added to Cart"
                Toast.makeText(this, "Added to Cart", Toast.LENGTH_SHORT).show()
            }
        }

        // Check if already in cart
        if (dbHelper.isProductInCart(productId, userEmail)) {
            addToCartBtn.text = "Added to Cart"
        }

        // Buy Now
        orderBtn.setOnClickListener {
            if (category == "Clothes" && (selectedSize.isEmpty() || selectedColor.isEmpty())) {
                Toast.makeText(this, "Please select Size and Color", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("product_id", productId)
            intent.putExtra("user_email", userEmail)
            intent.putExtra("quantity", quantity)
            intent.putExtra("selected_size", selectedSize)
            intent.putExtra("selected_color", selectedColor)
            startActivity(intent)
        }
    }

    private fun updateFavoriteIcon(btnFavorite: ImageView) {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.baseline_favorite_24)
        } else {
            btnFavorite.setImageResource(R.drawable.outline_favorite_border_24)
        }
    }

    private fun showFullScreenImage(imageUri: String) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val imageView = ImageView(this)
        imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        
        try {
            val uri = Uri.parse(imageUri)
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        imageView.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(imageView)
        dialog.show()
    }

    inner class ImageSliderAdapter(private val uris: List<String>, private val onImageClick: (String) -> Unit) : RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {
        inner class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val imageView = ImageView(parent.context)
            imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            return ImageViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val uriString = uris[position]
            try {
                val uri = Uri.parse(uriString)
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    holder.imageView.setImageBitmap(bitmap)
                    inputStream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            holder.imageView.setOnClickListener { onImageClick(uriString) }
        }

        override fun getItemCount(): Int = uris.size
    }
}