package com.vallo.nasync.api

import com.vallo.nasync.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // auth
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("/api/v1/auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserResponse>

    @POST("/api/v1/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Map<String, String>>

    // admin endpoints
    @POST("/api/v1/admin/users/register")
    suspend fun registerUser(
        @Header("Authorization") token: String,
        @Body request: RegisterRequest
    ): Response<UserResponse>

    @GET("/api/v1/admin/users/scholars")
    suspend fun getScholars(@Header("Authorization") token: String): Response<List<UserResponse>>

    @GET("/api/v1/admin/departments")
    suspend fun getDepartments(@Header("Authorization") token: String): Response<Map<String, List<DepartmentResponse>>>

    @GET("/api/v1/admin/branches")
    suspend fun getBranches(@Header("Authorization") token: String): Response<Map<String, List<BranchResponse>>>
}