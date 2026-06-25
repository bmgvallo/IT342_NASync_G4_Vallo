package com.vallo.nasync.models

data class TodayScheduleResponse(
    val primaryTimeIn: String?,
    val primaryTimeOut: String?,
    val secondaryTimeIn: String?,
    val secondaryTimeOut: String?
)
