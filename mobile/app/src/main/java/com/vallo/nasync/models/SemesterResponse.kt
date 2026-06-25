package com.vallo.nasync.models

data class SemesterResponse(
    val semesterId: Long,
    val name: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean
)
