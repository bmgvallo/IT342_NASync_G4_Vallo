package com.vallo.nasync.features.auth

import com.vallo.nasync.models.UserResponse

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)