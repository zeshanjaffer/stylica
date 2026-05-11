package com.example.stylica.data.db

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val subCategory: String,
    val targetAudience: String,
    val inventory: Int,
    val size: String,
    val status: String,
    val moderatorEmail: String,
    val imageUri: String?
)