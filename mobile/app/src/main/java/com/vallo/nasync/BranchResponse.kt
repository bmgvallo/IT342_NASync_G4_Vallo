package com.vallo.nasync.models

data class BranchResponse(
    val branchId: Long,
    val name: String,
    val deptId: Long,
    val departmentName: String,
    val createdAt: String
)