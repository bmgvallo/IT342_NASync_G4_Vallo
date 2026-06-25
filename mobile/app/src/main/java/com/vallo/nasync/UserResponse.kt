package com.vallo.nasync.models

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val userId: Long,
    val schoolId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val personalGmail: String?,
    val role: String,
    val departmentName: String?,
    val branchName: String?,
    val shift: String?,
    val expectedTimeIn: String?,
    val expectedTimeOut: String?,
    @SerializedName("isActive") val active: Boolean = false,
    val firstTimeLogin: Boolean = false,
    val profilePhotoUrl: String? = null
)