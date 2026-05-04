package com.vallo.nasync.features.auth

data class LoginRequest(
    val schoolId: String,
    val password: String
)