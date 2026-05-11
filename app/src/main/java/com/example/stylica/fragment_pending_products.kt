package com.example.stylica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import android.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class PendingProductsFragment : Fragment() {

    data class PendingProduct(
        val id: Int,
        val name: String,
        val price: Double,
        val description: String,
        val category: String,
        val status: String,
        val subCategory: String,
        val imageUri: String?,
        val targetAudience: String,
        val inventory: Int,
        val size: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_pending_products, container, false)

        val dbHelper = DatabaseHelper(requireContext())
        val gridView = view.findViewById<GridView>(R.id.pendingListView)

        val cursor = dbHelper.getPendingProducts()
        val products = ArrayList<PendingProduct>()

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
                
                val subCategoryIndex = cursor.getColumnIndex(DatabaseHelper.COL_SUBCATEGORY)
                val subCategory = if (subCategoryIndex != -1 && !cursor.isNull(subCategoryIndex)) cursor.getString(subCategoryIndex) else ""
                
                val imageIndex = cursor.getColumnIndex(DatabaseHelper.COL_PRODUCT_IMAGE)
                val imageUri = if (imageIndex != -1 && !cursor.isNull(imageIndex)) cursor.getString(imageIndex) else null

                val targetAudienceIndex = cursor.getColumnIndex(DatabaseHelper.COL_TARGET_AUDIENCE)
                val targetAudience = if (targetAudienceIndex != -1 && !cursor.isNull(targetAudienceIndex)) cursor.getString(targetAudienceIndex) else "All"

                val inventoryIndex = cursor.getColumnIndex(DatabaseHelper.COL_INVENTORY)
                val inventory = if (inventoryIndex != -1 && !cursor.isNull(inventoryIndex)) cursor.getInt(inventoryIndex) else 10
                
                val sizeIndex = cursor.getColumnIndex(DatabaseHelper.COL_SIZE)
                val size = if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) cursor.getString(sizeIndex) else "N/A"

                products.add(PendingProduct(id, name, price, description, category, status, subCategory, imageUri, targetAudience, inventory, size))
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
                
                val product = products[position]

                nameTv.text = product.name
                priceTv.text = "Rs. ${product.price}"

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
                    dbHelper.approveProduct(product.id)
                    Toast.makeText(context, "Product Approved!", Toast.LENGTH_SHORT).show()
                    requireActivity().recreate()
                }

                editBtn.setOnClickListener {
                    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_product, null)
                    
                    val nameInput = dialogView.findViewById<EditText>(R.id.editProductName)
                    val priceInput = dialogView.findViewById<EditText>(R.id.editProductPrice)
                    val descInput = dialogView.findViewById<EditText>(R.id.editProductDesc)
                    val colorInput = dialogView.findViewById<EditText>(R.id.editProductColor)
                    val sizeInput = dialogView.findViewById<EditText>(R.id.editProductSize)
                    val inventoryInput = dialogView.findViewById<EditText>(R.id.editProductInventory)
                    val btnSave = dialogView.findViewById<Button>(R.id.btnSaveEdit)
                    val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelEdit)

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
                        .setView(dialogView)
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
                                requireActivity().recreate()
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

                deleteBtn.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Product")
                        .setMessage("Are you sure you want to delete '${product.name}'?")
                        .setPositiveButton("Delete") { _, _ ->
                            if (dbHelper.deleteProduct(product.id)) {
                                Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                                requireActivity().recreate()
                            } else {
                                Toast.makeText(requireContext(), "Failed to delete product", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                row.setOnClickListener {
                    val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
                    intent.putExtra("id", product.id)
                    intent.putExtra("name", product.name)
                    intent.putExtra("description", product.description)
                    intent.putExtra("price", product.price)
                    intent.putExtra("category", product.category)
                    intent.putExtra("status", product.status)
                    intent.putExtra("image", product.imageUri)
                    intent.putExtra("subcategory", product.subCategory)
                    
                    val adminEmail = requireActivity().intent.getStringExtra("EMAIL") ?: ""
                    intent.putExtra("user_email", adminEmail)
                    startActivity(intent)
                }

                return row
            }
        }

        gridView.adapter = adapter
        
        val tvEmptyPending = view.findViewById<TextView>(R.id.tvEmptyPending)
        if (products.isEmpty()) {
            tvEmptyPending.visibility = View.VISIBLE
            gridView.visibility = View.GONE
        } else {
            tvEmptyPending.visibility = View.GONE
            gridView.visibility = View.VISIBLE
        }
        
        return view
    }
}