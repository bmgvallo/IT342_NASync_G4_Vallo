package com.vallo.nasync.api

import com.vallo.nasync.features.auth.AuthResponse
import com.vallo.nasync.features.auth.LoginRequest
import com.vallo.nasync.features.auth.RegisterRequest
import com.vallo.nasync.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("/api/v1/auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @POST("/api/v1/auth/logout")
    suspend fun logout(): Response<Map<String, String>>

    @PUT("/api/v1/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Map<String, String>>

    @PUT("/api/v1/auth/me/gmail")
    suspend fun updateGmail(@Body request: UpdateGmailRequest): Response<UserResponse>

    // ── User Profile ──────────────────────────────────────────────────────────
    @Multipart
    @POST("/api/v1/users/me/photo")
    suspend fun uploadSelfPhoto(@Part photo: MultipartBody.Part): Response<Map<String, String>>

    // ── Scholar Duty ──────────────────────────────────────────────────────────
    @GET("/api/v1/scholar/schedule/today")
    suspend fun getTodaySchedule(): Response<TodayScheduleResponse>

    @GET("/api/v1/scholar/duties/summary")
    suspend fun getDutySummary(): Response<DutySummaryResponse>

    @GET("/api/v1/scholar/duties")
    suspend fun getMyDuties(): Response<List<DutyResponse>>

    @POST("/api/v1/scholar/duties/clock-in")
    suspend fun clockIn(@Body request: ClockInRequest): Response<DutyResponse>

    @PUT("/api/v1/scholar/duties/clock-out")
    suspend fun clockOut(): Response<DutyResponse>

    // ── Semester ──────────────────────────────────────────────────────────────
    @GET("/api/v1/admin/semesters/active")
    suspend fun getActiveSemester(): Response<SemesterResponse>

    // ── Admin ─────────────────────────────────────────────────────────────────
    @POST("/api/v1/admin/users/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<UserResponse>

    @GET("/api/v1/admin/users/scholars")
    suspend fun getScholars(): Response<List<UserResponse>>

    @GET("/api/v1/admin/departments")
    suspend fun getDepartments(): Response<Map<String, List<DepartmentResponse>>>

    @GET("/api/v1/admin/branches")
    suspend fun getBranches(): Response<Map<String, List<BranchResponse>>>

    @GET("/api/v1/admin/dashboard/stats")
    suspend fun getDashboardStats(): Response<Map<String, Any>>

    @GET("/api/v1/admin/scholars/{schoolId}/schedule")
    suspend fun getScholarSchedule(@Path("schoolId") schoolId: String): Response<List<ScheduleEntryResponse>>

    // ── Dept Head ─────────────────────────────────────────────────────────────
    @GET("/api/v1/depthead/duties/pending")
    suspend fun getPendingDuties(): Response<List<DutyResponse>>

    @GET("/api/v1/depthead/duties")
    suspend fun getBranchDuties(): Response<List<DutyResponse>>

    @PUT("/api/v1/depthead/duties/{id}/approve")
    suspend fun approveDuty(@Path("id") id: Long, @Body request: ApprovalRequest): Response<DutyResponse>

    @PUT("/api/v1/depthead/duties/{id}/reject")
    suspend fun rejectDuty(@Path("id") id: Long, @Body request: ApprovalRequest): Response<DutyResponse>

    @GET("/api/v1/depthead/duties/scholars")
    suspend fun getBranchScholars(): Response<List<UserResponse>>

    @GET("/api/v1/depthead/duties/scholars/{schoolId}/details")
    suspend fun getDeptHeadScholarDetails(@Path("schoolId") schoolId: String): Response<ScholarDetailsResponse>
}
