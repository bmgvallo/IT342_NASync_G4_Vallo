package com.vallo.nasync.features.scholar

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.vallo.nasync.BaseDrawerActivity
import com.vallo.nasync.R
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.features.auth.ChangePasswordActivity
import com.vallo.nasync.models.ClockInRequest
import com.vallo.nasync.models.DutyResponse
import com.vallo.nasync.models.DutySummaryResponse
import com.vallo.nasync.models.TodayScheduleResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScholarDashboardActivity : BaseDrawerActivity() {

    override fun getContentLayoutId() = R.layout.content_scholar_dashboard
    override fun getCurrentNavItemId() = R.id.nav_dashboard

    private lateinit var bannerNoSemester: TextView
    private lateinit var bannerAbsent: TextView
    private lateinit var tvWelcome: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvPresent: TextView
    private lateinit var tvLate: TextView
    private lateinit var tvAbsent: TextView
    private lateinit var tvMakeupRemaining: TextView
    private lateinit var tvMakeupSub: TextView
    private lateinit var tvShiftInfo: TextView
    private lateinit var layoutOpenDuty: View
    private lateinit var tvOpenDutyInfo: TextView
    private lateinit var layoutDutyTypeSelector: View
    private lateinit var tvMinHourHint: TextView
    private lateinit var tvNoDutyHint: TextView
    private lateinit var tvEarlyHint: TextView
    private lateinit var tvClockOutHint: TextView
    private lateinit var tvDeptBranch: TextView
    private lateinit var ivScholarAvatarPhoto: ImageView
    private lateinit var btnClockIn: MaterialButton
    private lateinit var btnClockOut: MaterialButton
    private lateinit var btnTypeRegular: Button
    private lateinit var btnTypeMakeup: Button
    private lateinit var btnTypeOvertime: Button

    private var selectedDutyType = "REGULAR"
    private var activeSemesterAvailable = true
    private var openDuty: DutyResponse? = null
    private var actionLoading = false
    private var refreshJob: Job? = null
    private var noScheduledDutyToday = false
    private var noScheduledDutyDate = ""
    private var todayScheduleData: TodayScheduleResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Dashboard")
        tvAvatar.visibility = View.GONE
        ivTopbarAvatarPhoto.visibility = View.GONE
        bindViews()
        setupDutyTypeButtons()
        setupQuickActions()
        setupWelcome()
    }

    override fun onResume() {
        super.onResume()
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        refreshJob?.cancel()
    }

    private fun bindViews() {
        bannerNoSemester = findViewById(R.id.bannerNoSemester)
        bannerAbsent = findViewById(R.id.bannerAbsent)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvDate = findViewById(R.id.tvDate)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        tvPresent = findViewById(R.id.tvPresent)
        tvLate = findViewById(R.id.tvLate)
        tvAbsent = findViewById(R.id.tvAbsent)
        tvMakeupRemaining = findViewById(R.id.tvMakeupRemaining)
        tvMakeupSub = findViewById(R.id.tvMakeupSub)
        tvShiftInfo = findViewById(R.id.tvShiftInfo)
        layoutOpenDuty = findViewById(R.id.layoutOpenDuty)
        tvOpenDutyInfo = findViewById(R.id.tvOpenDutyInfo)
        layoutDutyTypeSelector = findViewById(R.id.layoutDutyTypeSelector)
        tvMinHourHint = findViewById(R.id.tvMinHourHint)
        tvNoDutyHint = findViewById(R.id.tvNoDutyHint)
        tvEarlyHint = findViewById(R.id.tvEarlyHint)
        tvClockOutHint = findViewById(R.id.tvClockOutHint)
        tvDeptBranch = findViewById(R.id.tvDeptBranch)
        ivScholarAvatarPhoto = findViewById(R.id.ivScholarAvatarPhoto)
        btnClockIn = findViewById(R.id.btnClockIn)
        btnClockOut = findViewById(R.id.btnClockOut)
        btnTypeRegular = findViewById(R.id.btnTypeRegular)
        btnTypeMakeup = findViewById(R.id.btnTypeMakeup)
        btnTypeOvertime = findViewById(R.id.btnTypeOvertime)
    }

    private fun setupWelcome() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("first_name", "Scholar") ?: "Scholar"
        val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
        tvWelcome.text = "$greeting, $firstName!"
        val lastName = prefs.getString("last_name", "") ?: ""
        val firstInitial = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        val lastInitial = lastName.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        tvAvatarInitial.text = (firstInitial + lastInitial).ifEmpty { "S" }
        tvDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())

        val shift = prefs.getString("shift", null)
        val timeIn = prefs.getString("expected_time_in", null)
        val timeOut = prefs.getString("expected_time_out", null)
        val shiftLabel = if (timeIn != null && timeOut != null) {
            "${formatTime(timeIn)} – ${formatTime(timeOut)}"
        } else "—"
        tvShiftInfo.text = shiftLabel

        val photoUrl = prefs.getString("profile_photo_url", null)
        if (!photoUrl.isNullOrBlank()) {
            Glide.with(this).load(photoUrl).circleCrop().into(ivScholarAvatarPhoto)
            ivScholarAvatarPhoto.visibility = View.VISIBLE
            tvAvatarInitial.visibility = View.INVISIBLE
        } else {
            ivScholarAvatarPhoto.visibility = View.GONE
            tvAvatarInitial.visibility = View.VISIBLE
        }

        val deptName = prefs.getString("department_name", null)
        val branchName = prefs.getString("branch_name", null)
        val deptBranchText = listOfNotNull(deptName, branchName).joinToString(" · ")
        if (deptBranchText.isNotEmpty()) {
            tvDeptBranch.text = deptBranchText
            tvDeptBranch.visibility = View.VISIBLE
        } else {
            tvDeptBranch.visibility = View.GONE
        }
    }

    private fun setupDutyTypeButtons() {
        btnTypeRegular.setOnClickListener { if (!btnTypeRegular.isEnabled) return@setOnClickListener; selectDutyType("REGULAR"); refreshClockInButton() }
        btnTypeMakeup.setOnClickListener { selectDutyType("MAKEUP"); refreshClockInButton() }
        btnTypeOvertime.setOnClickListener { selectDutyType("OVERTIME"); refreshClockInButton() }

        btnClockIn.setOnClickListener { if (!actionLoading) performClockIn() }
        btnClockOut.setOnClickListener { if (!actionLoading) performClockOut() }
    }

    private fun selectDutyType(type: String) {
        selectedDutyType = type
        val active = getColor(R.color.maroon)
        val inactive = getColor(R.color.border)
        val activeText = getColor(R.color.white)
        val inactiveText = getColor(R.color.text_soft)

        btnTypeRegular.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (type == "REGULAR") active else inactive)
        btnTypeRegular.setTextColor(if (type == "REGULAR") activeText else inactiveText)

        btnTypeMakeup.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (type == "MAKEUP") active else inactive)
        btnTypeMakeup.setTextColor(if (type == "MAKEUP") activeText else inactiveText)

        btnTypeOvertime.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (type == "OVERTIME") active else inactive)
        btnTypeOvertime.setTextColor(if (type == "OVERTIME") activeText else inactiveText)

        tvMinHourHint.visibility = if (type == "MAKEUP" || type == "OVERTIME") View.VISIBLE else View.GONE
    }

    private fun setupQuickActions() {
        findViewById<Button>(R.id.btnMyDuties).setOnClickListener {
            startActivity(Intent(this, DutyHistoryActivity::class.java))
        }
        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = lifecycleScope.launch {
            while (isActive) {
                loadData()
                delay(5_000)
            }
        }
    }

    private suspend fun loadData() {
        try {
            val today = todayString()
            if (today != noScheduledDutyDate) {
                noScheduledDutyToday = false
                noScheduledDutyDate = today
            }

            val summaryJob = lifecycleScope.launch { /* summary loaded in parallel below */ }

            // Parallel: summary + duties + semester + me
            var summary: DutySummaryResponse? = null
            var duties: List<DutyResponse> = emptyList()
            var semesterActive = true

            val j1 = lifecycleScope.launch {
                try { summary = RetrofitClient.api.getDutySummary().body() } catch (_: Exception) {}
            }
            val j2 = lifecycleScope.launch {
                try { duties = RetrofitClient.api.getMyDuties().body() ?: emptyList() } catch (_: Exception) {}
            }
            val j3 = lifecycleScope.launch {
                try {
                    val semRes = RetrofitClient.api.getActiveSemester()
                    semesterActive = semRes.isSuccessful && semRes.body() != null
                } catch (_: Exception) { semesterActive = false }
            }
            val j4 = lifecycleScope.launch {
                try {
                    val meRes = RetrofitClient.api.getCurrentUser()
                    if (meRes.isSuccessful) {
                        val user = meRes.body()!!
                        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putString("expected_time_in", user.expectedTimeIn)
                            .putString("expected_time_out", user.expectedTimeOut)
                            .putString("shift", user.shift)
                            .putString("profile_photo_url", user.profilePhotoUrl)
                            .putString("department_name", user.departmentName)
                            .putString("branch_name", user.branchName)
                            .apply()
                    }
                } catch (_: Exception) {}
            }
            val j5 = lifecycleScope.launch {
                try {
                    val schRes = RetrofitClient.api.getTodaySchedule()
                    if (schRes.isSuccessful) todayScheduleData = schRes.body()
                } catch (_: Exception) {}
            }
            j1.join(); j2.join(); j3.join(); j4.join(); j5.join()
            summaryJob.cancel()

            activeSemesterAvailable = semesterActive

            val todayDuties = duties.filter { it.dutyDate == today }
            val absentToday = todayDuties.any { it.attendanceStatus == "ABSENT" }
            val open = todayDuties.firstOrNull { it.timeIn != null && it.timeOut == null }
            openDuty = open

            val hasOpenRegular = todayDuties.any {
                it.dutyType == "REGULAR" && it.approvalStatus != "REJECTED" && it.timeIn != null && it.timeOut == null
            }
            val completedRegular = todayDuties.count {
                it.dutyType == "REGULAR" && it.approvalStatus != "REJECTED" && it.timeOut != null
            }
            val regularDone = absentToday || hasOpenRegular || completedRegular >= 2

            runOnUiThread {
                // Banners
                bannerNoSemester.visibility = if (!semesterActive) View.VISIBLE else View.GONE
                bannerAbsent.visibility = if (absentToday) View.VISIBLE else View.GONE

                // KPIs
                summary?.let { s ->
                    tvPresent.text = s.presentCount.toString()
                    tvLate.text = s.lateCount.toString()
                    tvAbsent.text = s.absentCount.toString()
                    tvMakeupRemaining.text = s.makeupHoursRemaining.toString()
                    tvMakeupSub.text = "Owed: ${s.makeupHoursOwed}h · Done: ${s.makeupHoursRendered}h"
                }

                // Refresh shift info after getMe
                setupWelcome()

                // Override shift display with today's per-day schedule if available
                val sched = todayScheduleData
                val prefs2 = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                val legacyIn  = prefs2.getString("expected_time_in",  null)
                val legacyOut = prefs2.getString("expected_time_out", null)
                tvShiftInfo.text = when {

                    sched?.primaryTimeIn != null && sched.primaryTimeOut != null -> {

                        buildString {

                            append(
                                "Shift 1: ${
                                    formatTime(sched.primaryTimeIn)
                                } – ${
                                    formatTime(sched.primaryTimeOut)
                                }"
                            )

                            if (
                                sched.secondaryTimeIn != null &&
                                sched.secondaryTimeOut != null
                            ) {

                                append("\n")

                                append(
                                    "Shift 2: ${
                                        formatTime(sched.secondaryTimeIn)
                                    } – ${
                                        formatTime(sched.secondaryTimeOut)
                                    }"
                                )
                            }
                        }
                    }

                    legacyIn != null && legacyOut != null -> {
                        "Shift 1: ${formatTime(legacyIn)} – ${formatTime(legacyOut)}"
                    }

                    else -> "—"
                }

                // Regular chip disabled state
                btnTypeRegular.isEnabled = !regularDone
                if (regularDone) {
                    btnTypeRegular.paintFlags = btnTypeRegular.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    btnTypeRegular.paintFlags = btnTypeRegular.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                if (regularDone && selectedDutyType == "REGULAR") selectDutyType("MAKEUP")

                // Clock state
                if (open != null) {
                    // Clocked in — show clock-out UI
                    layoutDutyTypeSelector.visibility = View.GONE
                    layoutOpenDuty.visibility = View.VISIBLE
                    tvOpenDutyInfo.text = "${open.dutyType} · In at ${formatTime(open.timeIn)}"
                    btnClockIn.visibility = View.GONE
                    btnClockOut.visibility = View.VISIBLE

                    val expectedOut = open.expectedTimeOut
                        ?: getSharedPreferences("auth_prefs", MODE_PRIVATE).getString("expected_time_out", null)
                    val canClockOut = canClockOutNow(expectedOut)
                    btnClockOut.isEnabled = canClockOut && semesterActive
                    btnClockOut.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        if (canClockOut && semesterActive) Color.parseColor("#C62828")
                        else getColor(R.color.maroon_disabled)
                    )
                    if (!canClockOut && expectedOut != null) {
                        tvClockOutHint.text = "Clock-out available after ${formatTime(expectedOut)}"
                        tvClockOutHint.visibility = View.VISIBLE
                    } else {
                        tvClockOutHint.visibility = View.GONE
                    }
                } else {
                    // Not clocked in — show clock-in UI
                    layoutOpenDuty.visibility = View.GONE
                    layoutDutyTypeSelector.visibility = View.VISIBLE
                    btnClockOut.visibility = View.GONE
                    btnClockIn.visibility = View.VISIBLE
                    tvClockOutHint.visibility = View.GONE

                    val earlyRestricted = isTooEarlyToClockIn()
                    when {
                        noScheduledDutyToday && selectedDutyType == "REGULAR" -> {
                            btnClockIn.isEnabled = false
                            btnClockIn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                                getColor(R.color.maroon_disabled)
                            )
                            tvNoDutyHint.visibility = View.VISIBLE
                            tvEarlyHint.visibility = View.GONE
                        }
                        earlyRestricted -> {
                            btnClockIn.isEnabled = false
                            btnClockIn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                                getColor(R.color.maroon_disabled)
                            )
                            tvNoDutyHint.visibility = View.GONE
                            tvEarlyHint.visibility = View.VISIBLE
                        }
                        else -> {
                            btnClockIn.isEnabled = semesterActive
                            btnClockIn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                                getColor(if (semesterActive) R.color.maroon else R.color.maroon_disabled)
                            )
                            tvNoDutyHint.visibility = View.GONE
                            tvEarlyHint.visibility = View.GONE
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // silent; data stays as last loaded
        }
    }

    private fun performClockIn() {
        actionLoading = true
        btnClockIn.isEnabled = false
        btnClockIn.text = "Processing…"
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.clockIn(ClockInRequest(selectedDutyType))
                if (res.isSuccessful) {
                    val duty = res.body()!!
                    val status = duty.attendanceStatus ?: ""
                    val msg = "Clocked in at ${formatTime(duty.timeIn)} — $status (${duty.dutyType})"
                    Toast.makeText(this@ScholarDashboardActivity, msg, Toast.LENGTH_LONG).show()
                    loadData()
                } else {
                    val err = parseApiError(res.errorBody()?.string())
                    Toast.makeText(this@ScholarDashboardActivity, err, Toast.LENGTH_LONG).show()
                    if (err.contains("No duty scheduled for")) {
                        noScheduledDutyToday = true
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ScholarDashboardActivity, "Network error.", Toast.LENGTH_SHORT).show()
            } finally {
                actionLoading = false
                if (noScheduledDutyToday) {
                    btnClockIn.isEnabled = false
                    btnClockIn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        getColor(R.color.maroon_disabled)
                    )
                    tvNoDutyHint.visibility = View.VISIBLE
                    tvEarlyHint.visibility = View.GONE
                } else {
                    btnClockIn.isEnabled = activeSemesterAvailable
                    btnClockIn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        getColor(if (activeSemesterAvailable) R.color.maroon else R.color.maroon_disabled)
                    )
                }
                btnClockIn.text = "▶  Clock In"
            }
        }
    }

    private fun performClockOut() {
        actionLoading = true
        btnClockOut.isEnabled = false
        btnClockOut.backgroundTintList = android.content.res.ColorStateList.valueOf(
            getColor(R.color.maroon_disabled)
        )
        btnClockOut.text = "Processing…"
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.clockOut()
                if (res.isSuccessful) {
                    val duty = res.body()!!
                    Toast.makeText(
                        this@ScholarDashboardActivity,
                        "Clocked out at ${formatTime(duty.timeOut)}. Pending review.",
                        Toast.LENGTH_LONG
                    ).show()
                    loadData()
                } else {
                    val err = parseApiError(res.errorBody()?.string())
                    Toast.makeText(this@ScholarDashboardActivity, err, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ScholarDashboardActivity, "Network error.", Toast.LENGTH_SHORT).show()
            } finally {
                actionLoading = false
                btnClockOut.text = "Clock Out"
                btnClockOut.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    getColor(R.color.maroon_disabled)
                )
            }
        }
    }

    private fun refreshClockInButton() {
        if (openDuty != null) return
        val blocked = noScheduledDutyToday && selectedDutyType == "REGULAR"
        val early = isTooEarlyToClockIn() && selectedDutyType == "REGULAR"
        btnClockIn.isEnabled = !blocked && !early && activeSemesterAvailable
        btnClockIn.backgroundTintList = android.content.res.ColorStateList.valueOf(
            getColor(if (btnClockIn.isEnabled) R.color.maroon else R.color.maroon_disabled)
        )
        tvNoDutyHint.visibility = if (blocked) View.VISIBLE else View.GONE
        tvEarlyHint.visibility = if (early) View.VISIBLE else View.GONE
    }

    private fun isTooEarlyToClockIn(): Boolean {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val expectedIn = prefs.getString("expected_time_in", null) ?: return false
        return try {
            val parts = expectedIn.split(":")
            val expHour = parts[0].toInt()
            val expMin = parts[1].toInt()
            val cal = Calendar.getInstance()
            val nowMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            val expMins = expHour * 60 + expMin
            (expMins - nowMins) > 15
        } catch (e: Exception) { false }
    }

    private fun canClockOutNow(expectedTimeOut: String?): Boolean {
        if (expectedTimeOut == null) return false
        return try {
            val parts = expectedTimeOut.split(":")
            val expHour = parts[0].toInt()
            val expMin = parts[1].toInt()
            val cal = Calendar.getInstance()
            val nowMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            val expMins = expHour * 60 + expMin
            nowMins >= expMins
        } catch (e: Exception) { false }
    }

    private fun todayString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${
            (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        }-${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
    }

    private fun parseApiError(body: String?): String {
        if (body.isNullOrBlank()) return "Action failed."
        return try {
            val json = org.json.JSONObject(body)
            json.optString("error", json.optString("message", "Action failed."))
        } catch (e: Exception) { "Action failed." }
    }

    companion object {
        fun formatTime(timeStr: String?): String {
            if (timeStr == null) return "—"
            return try {
                val parts = timeStr.split(":")
                val hour = parts[0].toInt()
                val min = parts[1].toInt()
                val ampm = if (hour >= 12) "PM" else "AM"
                val h12 = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                "$h12:${min.toString().padStart(2, '0')} $ampm"
            } catch (e: Exception) { timeStr }
        }
    }
}
