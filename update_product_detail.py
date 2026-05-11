import re

with open("app/src/main/java/com/example/stylica/ProductDetailActivity.kt", "r") as f:
    content = f.read()

# Add view bindings
content = content.replace(
    "val orderBtn = findViewById<Button>(R.id.orderNowBtn)",
    """val orderBtn = findViewById<Button>(R.id.orderNowBtn)
        val selectionContainer = findViewById<View>(R.id.selectionContainer)
        val sizeRadioGroup = findViewById<android.widget.RadioGroup>(R.id.sizeRadioGroup)
        val colorRadioGroup = findViewById<android.widget.RadioGroup>(R.id.colorRadioGroup)
        var selectedSize = ""
        var selectedColor = ""
"""
)

# Show selection container
content = content.replace(
    """if (category == "Clothes" && size != "N/A" && size.isNotEmpty()) {
            productSize.visibility = View.VISIBLE
            productSize.text = "Size: $size"
        }""",
    """if (category == "Clothes") {
            selectionContainer.visibility = View.VISIBLE
            productSize.visibility = View.GONE
            productColor.visibility = View.GONE
        } else {
            selectionContainer.visibility = View.GONE
            if (category == "Clothes" && size != "N/A" && size.isNotEmpty()) {
                productSize.visibility = View.VISIBLE
                productSize.text = "Size: $size"
            }
        }"""
)

# RadioGroup listeners
content = content.replace(
    "// Add to Cart",
    """// Add to Cart
        sizeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val rb = findViewById<android.widget.RadioButton>(checkedId)
            selectedSize = rb?.text?.toString() ?: ""
        }
        colorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedColor = when (checkedId) {
                R.id.colorRed -> "Red"
                R.id.colorBlue -> "Blue"
                R.id.colorGreen -> "Green"
                R.id.colorBlack -> "Black"
                else -> ""
            }
        }
"""
)

# Add to Cart validation
content = content.replace(
    """        addToCartBtn.setOnClickListener {
            if (dbHelper.isProductInCart(productId, userEmail)) {
                dbHelper.removeFromCart(productId, userEmail)
                addToCartBtn.text = "Add to cart"
                Toast.makeText(this, "Removed from Cart", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.addToCart(productId, userEmail, quantity)
                addToCartBtn.text = "Added to Cart"
                Toast.makeText(this, "Added to Cart", Toast.LENGTH_SHORT).show()
            }
        }""",
    """        addToCartBtn.setOnClickListener {
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
        }"""
)

# Buy Now validation
content = content.replace(
    """        orderBtn.setOnClickListener {
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("product_id", productId)
            intent.putExtra("user_email", userEmail)
            intent.putExtra("quantity", quantity)
            startActivity(intent)
        }""",
    """        orderBtn.setOnClickListener {
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
        }"""
)

with open("app/src/main/java/com/example/stylica/ProductDetailActivity.kt", "w") as f:
    f.write(content)
