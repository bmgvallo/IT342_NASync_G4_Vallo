package com.vallo.nasync.models

data class DutyResponse(
    val dutyId: Long,
    val scholarId: Long,
    val scholarName: String?,
    val scholarSchoolId: String?,
    val branchName: String?,
    val semesterId: Long,
    val dutyDate: String,
    val dutyType: String,
    val timeIn: String?,
    val timeOut: String?,
    val expectedTimeOut: String?,
    val approvalStatus: String,
    val attendanceStatus: String?,
    val remarks: String?,
    val approvedByName: String?
)
