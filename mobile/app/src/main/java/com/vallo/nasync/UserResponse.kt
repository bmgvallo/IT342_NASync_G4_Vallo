package com.vallo.nasync.models

data class UserResponse(
    val userId: Long,
    val schoolId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String, // "ADMIN", "SCHOLAR", "DEPARTMENT_HEAD"
    val departmentName: String?,
    val branchName: String?,
    val shift: String?,
    val expectedTimeIn: String?,
    val expectedTimeOut: String?,
    val active: Boolean
)