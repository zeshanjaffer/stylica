package com.example.stylica.data.db

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String,
    val profileImage: String? = null,
    val gender: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val domain: String? = null,
    val registeredAt: String? = null
)
