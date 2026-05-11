package com.example.stylica

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.Toast
import android.widget.EditText
import android.widget.LinearLayout
import android.content.Intent
import android.app.AlertDialog
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_CATEGORY
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_MODERATOR_EMAIL
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_PRICE
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_PRODUCT_DESC
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_PRODUCT_ID
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_PRODUCT_IMAGE
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_PRODUCT_NAME
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_STATUS
import com.example.stylica.data.db.DatabaseHelper.Companion.COL_SUBCATEGORY
import com.example.stylica.data.db.Product

class ModeratorProductsFragment : Fragment(R.layout.fragment_moderator_products), OnProductActionListener {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var productsListView: ListView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        productsListView = view.findViewById(R.id.productsListView)
        
        // Remove standard divider for card layout
        productsListView.divider = null
        productsListView.dividerHeight = 0
        
        loadProducts()
    }

    private fun loadProducts() {
        val moderatorEmail = requireActivity().intent.getStringExtra("EMAIL") ?: ""
        val cursor = dbHelper.getProductsByModerator(moderatorEmail)
        
        val productsList = mutableListOf<Product>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME))
                val descIndex = cursor.getColumnIndex(COL_PRODUCT_DESC)
                val desc = if (descIndex != -1 && !cursor.isNull(descIndex)) cursor.getString(descIndex) else ""
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE))
                val categoryIndex = cursor.getColumnIndex(COL_CATEGORY)
                val category = if (categoryIndex != -1 && !cursor.isNull(categoryIndex)) cursor.getString(categoryIndex) else ""
                val subCategoryIndex = cursor.getColumnIndex(COL_SUBCATEGORY)
                val subCategory = if (subCategoryIndex != -1 && !cursor.isNull(subCategoryIndex)) cursor.getString(subCategoryIndex) else ""
                val status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS))
                
                val modEmailIndex = cursor.getColumnIndex(COL_MODERATOR_EMAIL)
                val modEmail = if (modEmailIndex != -1 && !cursor.isNull(modEmailIndex)) cursor.getString(modEmailIndex) else ""

                val imageIndex = cursor.getColumnIndex(COL_PRODUCT_IMAGE)
                val imageUri = if (imageIndex != -1 && !cursor.isNull(imageIndex)) cursor.getString(imageIndex) else null

                val targetAudienceIndex = cursor.getColumnIndex(DatabaseHelper.COL_TARGET_AUDIENCE)
                val targetAudience = if (targetAudienceIndex != -1 && !cursor.isNull(targetAudienceIndex)) cursor.getString(targetAudienceIndex) else "All"
                
                val inventoryIndex = cursor.getColumnIndex(DatabaseHelper.COL_INVENTORY)
                val inventory = if (inventoryIndex != -1 && !cursor.isNull(inventoryIndex)) cursor.getInt(inventoryIndex) else 10
                
                val sizeIndex = cursor.getColumnIndex(DatabaseHelper.COL_SIZE)
                val size = if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) cursor.getString(sizeIndex) else "N/A"

                val product = Product(id, name, desc, price, category, subCategory, targetAudience, inventory, size, status, modEmail, imageUri)
                productsList.add(product)
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = ModeratorProductAdapter(requireContext(), productsList, this)
        productsListView.adapter = adapter
        
        val tvEmptyModeratorProducts = view?.findViewById<TextView>(R.id.tvEmptyModeratorProducts)
        if (productsList.isEmpty()) {
            tvEmptyModeratorProducts?.visibility = View.VISIBLE
            productsListView.visibility = View.GONE
        } else {
            tvEmptyModeratorProducts?.visibility = View.GONE
            productsListView.visibility = View.VISIBLE
        }
        
        productsListView.setOnItemClickListener { _, _, position, _ ->
            val product = productsList[position]
            val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
            intent.putExtra("id", product.id)
            intent.putExtra("name", product.name)
            intent.putExtra("description", product.description)
            intent.putExtra("price", product.price)
            intent.putExtra("category", product.category)
            intent.putExtra("status", product.status)
            intent.putExtra("image", product.imageUri)
            intent.putExtra("user_email", moderatorEmail)
            intent.putExtra("subcategory", product.subCategory)
            startActivity(intent)
        }
    }

    override fun onDeleteClick(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete '${product.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                if (dbHelper.deleteProduct(product.id)) {
                    Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                    loadProducts()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete product", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onEditClick(product: Product) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_product, null)
        
        val nameInput = view.findViewById<EditText>(R.id.editProductName)
        val priceInput = view.findViewById<EditText>(R.id.editProductPrice)
        val descInput = view.findViewById<EditText>(R.id.editProductDesc)
        val colorInput = view.findViewById<EditText>(R.id.editProductColor)
        val sizeInput = view.findViewById<EditText>(R.id.editProductSize)
        val inventoryInput = view.findViewById<EditText>(R.id.editProductInventory)
        val btnSave = view.findViewById<Button>(R.id.btnSaveEdit)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelEdit)

        nameInput.setText(product.name)
        priceInput.setText(product.price.toString())
        descInput.setText(product.description)
        inventoryInput.setText(product.inventory.toString())
        
        if (product.category == "Clothes") {
            colorInput.visibility = View.VISIBLE
            sizeInput.visibility = View.VISIBLE
            colorInput.setText(product.subCategory)
            sizeInput.setText(product.size)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val newName = nameInput.text.toString().trim()
            val newPrice = priceInput.text.toString().toDoubleOrNull() ?: product.price
            val newDesc = descInput.text.toString().trim()
            val newColor = if (product.category == "Clothes") colorInput.text.toString().trim() else ""
            val newSize = if (product.category == "Clothes") sizeInput.text.toString().trim() else "N/A"
            val newInventory = inventoryInput.text.toString().toIntOrNull() ?: product.inventory

            if (newName.isNotEmpty()) {
                val success = dbHelper.updateProduct(
                    product.id, newName, product.category, newPrice, newDesc, newColor, product.targetAudience, newInventory, newSize
                )
                if (success) {
                    Toast.makeText(requireContext(), "Product updated", Toast.LENGTH_SHORT).show()
                    loadProducts()
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Failed to update product", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }
}
