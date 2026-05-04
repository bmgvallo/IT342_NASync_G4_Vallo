package com.vallo.nasync.features.auth

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val schoolId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val personalGmail: String? = null,
    val password: String,
    val role: String, // "SCHOLAR" or "DEPARTMENT_HEAD"
    val deptId: Long?,
    val branchId: Long?,
    val shift: String?, // "MORNING" or "AFTERNOON"
    @SerializedName("expectedTimeIn") val expectedTimeIn: String?, // "08:00:00"
    @SerializedName("expectedTimeOut") val expectedTimeOut: String? // "12:00:00"
)