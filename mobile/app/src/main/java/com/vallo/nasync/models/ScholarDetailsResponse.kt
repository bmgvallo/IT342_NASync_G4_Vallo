package com.vallo.nasync.models

data class ScholarDetailsResponse(
    val schoolId: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val schedule: List<ScheduleEntryResponse>?
)
