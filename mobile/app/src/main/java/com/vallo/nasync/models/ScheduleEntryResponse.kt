package com.vallo.nasync.models

data class ScheduleEntryResponse(
    val dayOfWeek: String,
    val primaryTimeIn: String?,
    val primaryTimeOut: String?,
    val secondaryTimeIn: String?,
    val secondaryTimeOut: String?
)
