package com.vallo.nasync.features.depthead

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.vallo.nasync.BaseDrawerActivity
import com.vallo.nasync.R
import com.vallo.nasync.api.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DeptHeadDashboardActivity : BaseDrawerActivity() {

    override fun getContentLayoutId() = R.layout.content_depthead_dashboard
    override fun getCurrentNavItemId() = R.id.nav_depthead_dashboard

    private lateinit var tvWelcome: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvBranchName: TextView
    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvScholarsCount: TextView
    private lateinit var ivDeptHeadAvatarPhoto: ImageView
    private var refreshJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Dashboard")

        tvWelcome = findViewById(R.id.tvWelcome)
        tvDate = findViewById(R.id.tvDate)
        tvBranchName = findViewById(R.id.tvBranchName)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvScholarsCount = findViewById(R.id.tvScholarsCount)
        ivDeptHeadAvatarPhoto = findViewById(R.id.ivDeptHeadAvatarPhoto)

        setupWelcome()

        findViewById<MaterialButton>(R.id.btnViewPending).setOnClickListener {
            startActivity(Intent(this, PendingDutiesActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnViewScholars).setOnClickListener {
            startActivity(Intent(this, ScholarsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        refreshJob?.cancel()
    }

    private fun setupWelcome() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("first_name", "Dept Head") ?: "Dept Head"
        val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
        tvWelcome.text = "$greeting, $firstName!"
        val lastName = prefs.getString("last_name", "") ?: ""
        val firstInitial = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        val lastInitial = lastName.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        tvAvatarInitial.text = (firstInitial + lastInitial).ifEmpty { "D" }
        tvDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
        val branch = prefs.getString("branch_name", null)
        tvBranchName.text = if (branch != null) "Branch: $branch" else ""

        val photoUrl = prefs.getString("profile_photo_url", null)
        if (!photoUrl.isNullOrBlank()) {
            Glide.with(this).load(photoUrl).circleCrop().into(ivDeptHeadAvatarPhoto)
            ivDeptHeadAvatarPhoto.visibility = View.VISIBLE
            tvAvatarInitial.visibility = View.INVISIBLE
        } else {
            ivDeptHeadAvatarPhoto.visibility = View.GONE
            tvAvatarInitial.visibility = View.VISIBLE
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = lifecycleScope.launch {
            while (isActive) {
                loadStats()
                delay(5_000)
            }
        }
    }

    private suspend fun loadStats() {
        try {
            val pendingRes = RetrofitClient.api.getPendingDuties()
            if (pendingRes.isSuccessful) {
                runOnUiThread { tvPendingCount.text = (pendingRes.body()?.size ?: 0).toString() }
            }
        } catch (_: Exception) {}

        try {
            val scholarsRes = RetrofitClient.api.getBranchScholars()
            if (scholarsRes.isSuccessful) {
                runOnUiThread { tvScholarsCount.text = (scholarsRes.body()?.size ?: 0).toString() }
            }
        } catch (_: Exception) {}
    }
}
