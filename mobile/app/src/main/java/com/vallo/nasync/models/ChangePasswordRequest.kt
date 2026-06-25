package com.vallo.nasync.models

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
