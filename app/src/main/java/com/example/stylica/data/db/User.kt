package com.example.stylica.data.db

data class User(

    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String,
    val profileImage: String? = null
)