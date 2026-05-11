package com.example.stylica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stylica.data.db.DatabaseHelper

class CreateProductFragment : Fragment(R.layout.fragment_create_product) {

    private var selectedImageUri: Uri? = null
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var productImageView: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        val moderatorEmail = requireActivity().intent.getStringExtra("EMAIL") ?: ""

        val nameEt = view.findViewById<EditText>(R.id.productNameEt)
        val categorySpinner = view.findViewById<Spinner>(R.id.categorySpinner)
        val priceEt = view.findViewById<EditText>(R.id.priceEt)
        val descriptionEt = view.findViewById<EditText>(R.id.descriptionEt)
        val addBtn = view.findViewById<Button>(R.id.addProductBtn)
        val colorEt = view.findViewById<EditText>(R.id.colorEt)
        val sizeEt = view.findViewById<EditText>(R.id.sizeEt)
        val inventoryEt = view.findViewById<EditText>(R.id.inventoryEt)
        productImageView = view.findViewById(R.id.productImageView)

        val categories = arrayOf("Clothes", "Makeup")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        val targetAudienceSpinner = view.findViewById<Spinner>(R.id.targetAudienceSpinner)
        val audiences = arrayOf("All", "Men", "Women", "Juniors")
        val audienceAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, audiences)
        targetAudienceSpinner.adapter = audienceAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (categories[position] == "Clothes") {
                    colorEt.visibility = View.VISIBLE
                    sizeEt.visibility = View.VISIBLE
                    targetAudienceSpinner.visibility = View.VISIBLE
                } else {
                    colorEt.visibility = View.GONE
                    colorEt.text.clear()
                    sizeEt.visibility = View.GONE
                    sizeEt.text.clear()
                    targetAudienceSpinner.visibility = View.GONE
                    targetAudienceSpinner.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        productImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        addBtn.setOnClickListener {
            val name = nameEt.text.toString().trim()
            val category = categorySpinner.selectedItem.toString()
            val color = if (category == "Clothes") colorEt.text.toString().trim() else ""
            val size = if (category == "Clothes") sizeEt.text.toString().trim() else "N/A"
            val targetAudience = if (category == "Clothes") targetAudienceSpinner.selectedItem.toString() else "All"
            val inventoryText = inventoryEt.text.toString().trim()
            val inventory = if (inventoryText.isNotEmpty()) inventoryText.toIntOrNull() ?: 10 else 10
            val priceText = priceEt.text.toString().trim()
            val description = descriptionEt.text.toString().trim()

            if (name.isEmpty() || category.isEmpty() || priceText.isEmpty() || inventoryText.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceText.toDouble()

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Please select product image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedUri = saveImageToInternalStorage(selectedImageUri!!)
            if (savedUri == null) {
                Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = dbHelper.addProduct(
                name,
                category,
                price,
                description,
                savedUri.toString(),
                moderatorEmail,
                color,
                targetAudience,
                inventory,
                size
            )
            
            if (success) {
                Toast.makeText(requireContext(), "Product Added (Waiting for Admin Approval)", Toast.LENGTH_LONG).show()
                nameEt.text.clear()
                categorySpinner.setSelection(0)
                targetAudienceSpinner.setSelection(0)
                colorEt.text.clear()
                sizeEt.text.clear()
                inventoryEt.text.clear()
                priceEt.text.clear()
                descriptionEt.text.clear()
                productImageView.setImageResource(android.R.drawable.ic_menu_gallery)
                selectedImageUri = null
            } else {
                Toast.makeText(requireContext(), "Failed to add product", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = requireActivity().contentResolver.openInputStream(uri)
            val fileName = "product_img_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(requireContext().filesDir, fileName)
            val outputStream = java.io.FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == android.app.Activity.RESULT_OK) {
            selectedImageUri = data?.data
            productImageView.setImageURI(selectedImageUri)
        }
    }
}
