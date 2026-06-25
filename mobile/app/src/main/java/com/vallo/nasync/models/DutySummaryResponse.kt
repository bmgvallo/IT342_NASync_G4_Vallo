package com.vallo.nasync.models

data class DutySummaryResponse(
    val presentCount: Long,
    val lateCount: Long,
    val absentCount: Long,
    val makeupHoursOwed: Long,
    val makeupHoursRendered: Long,
    val makeupHoursRemaining: Long
)
