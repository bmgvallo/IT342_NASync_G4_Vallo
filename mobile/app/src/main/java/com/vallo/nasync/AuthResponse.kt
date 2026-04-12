package com.vallo.nasync.models

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)