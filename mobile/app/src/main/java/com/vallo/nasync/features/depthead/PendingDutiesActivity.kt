package com.vallo.nasync.features.depthead

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vallo.nasync.BaseDrawerActivity
import com.vallo.nasync.R
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.models.ApprovalRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class PendingDutiesActivity : BaseDrawerActivity() {

    override fun getContentLayoutId() = R.layout.content_pending_duties
    override fun getCurrentNavItemId() = R.id.nav_depthead_pending

    private lateinit var tvPendingHeader: TextView
    private lateinit var rvPending: RecyclerView
    private lateinit var tvEmptyPending: TextView
    private lateinit var tvAbsentHeader: TextView
    private lateinit var rvAbsent: RecyclerView
    private lateinit var tvEmptyAbsent: TextView
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmptyHistory: TextView
    private var refreshJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Pending Duties")

        tvPendingHeader = findViewById(R.id.tvPendingHeader)
        rvPending = findViewById(R.id.rvPending)
        tvEmptyPending = findViewById(R.id.tvEmptyPending)
        tvAbsentHeader = findViewById(R.id.tvAbsentHeader)
        rvAbsent = findViewById(R.id.rvAbsent)
        tvEmptyAbsent = findViewById(R.id.tvEmptyAbsent)
        rvHistory = findViewById(R.id.rvHistory)
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory)

        rvPending.layoutManager = LinearLayoutManager(this)
        rvAbsent.layoutManager = LinearLayoutManager(this)
        rvHistory.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        refreshJob?.cancel()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = lifecycleScope.launch {
            while (isActive) {
                loadAllSections()
                delay(5_000)
            }
        }
    }

    private suspend fun loadAllSections() {
        try {
            val res = RetrofitClient.api.getBranchDuties()
            if (res.isSuccessful) {
                val all = res.body() ?: emptyList()
                val today = todayString()

                val pending = all.filter { it.approvalStatus == "PENDING" }
                val absentToday = all.filter {
                    it.attendanceStatus == "ABSENT" && it.dutyDate == today
                }
                val history = all.filter {
                    it.approvalStatus == "APPROVED" || it.approvalStatus == "REJECTED"
                }.sortedByDescending { it.dutyDate }

                runOnUiThread {
                    // Pending section
                    tvPendingHeader.text = "PENDING (${pending.size})"
                    if (pending.isEmpty()) {
                        tvEmptyPending.visibility = View.VISIBLE
                        rvPending.visibility = View.GONE
                    } else {
                        tvEmptyPending.visibility = View.GONE
                        rvPending.visibility = View.VISIBLE
                        rvPending.adapter = PendingDutyAdapter(
                            duties = pending,
                            onApprove = { id -> showApproveDialog(id) },
                            onReject = { id -> showRejectDialog(id) }
                        )
                    }

                    // Absent today section
                    tvAbsentHeader.text = "ABSENT TODAY (${absentToday.size})"
                    if (absentToday.isEmpty()) {
                        tvEmptyAbsent.visibility = View.VISIBLE
                        rvAbsent.visibility = View.GONE
                    } else {
                        tvEmptyAbsent.visibility = View.GONE
                        rvAbsent.visibility = View.VISIBLE
                        rvAbsent.adapter = DutyItemAdapter(absentToday)
                    }

                    // History section
                    if (history.isEmpty()) {
                        tvEmptyHistory.visibility = View.VISIBLE
                        rvHistory.visibility = View.GONE
                    } else {
                        tvEmptyHistory.visibility = View.GONE
                        rvHistory.visibility = View.VISIBLE
                        rvHistory.adapter = DutyItemAdapter(history)
                    }
                }
            }
        } catch (_: Exception) {
            // silent; keep existing data shown
        }
    }

    private fun showApproveDialog(dutyId: Long) {
        val input = EditText(this).apply {
            hint = "Remarks (optional)"
            setPadding(60, 30, 60, 30)
        }
        AlertDialog.Builder(this)
            .setTitle("Approve Duty")
            .setView(input)
            .setPositiveButton("Approve") { _, _ ->
                val remarks = input.text.toString().trim().ifEmpty { null }
                performApprove(dutyId, remarks)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRejectDialog(dutyId: Long) {
        val input = EditText(this).apply {
            hint = "Enter reason for rejection"
            setPadding(60, 30, 60, 30)
        }
        AlertDialog.Builder(this)
            .setTitle("Reject Duty")
            .setMessage("Remarks are required.")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                val remarks = input.text.toString().trim()
                if (remarks.isEmpty()) {
                    Toast.makeText(this, "Please enter rejection remarks.", Toast.LENGTH_SHORT).show()
                } else {
                    performReject(dutyId, remarks)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @Suppress("DEPRECATION")
    private fun showProgress(msg: String): ProgressDialog =
        ProgressDialog(this).apply {
            setMessage(msg)
            isIndeterminate = true
            setCancelable(false)
            show()
        }

    private fun performApprove(dutyId: Long, remarks: String?) {
        val pd = showProgress("Approving duty…")
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.approveDuty(dutyId, ApprovalRequest(remarks))
                pd.dismiss()
                if (res.isSuccessful) {
                    Toast.makeText(this@PendingDutiesActivity, "Duty approved.", Toast.LENGTH_SHORT).show()
                    loadAllSections()
                } else {
                    Toast.makeText(this@PendingDutiesActivity, parseError(res.errorBody()?.string()), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                pd.dismiss()
                Toast.makeText(this@PendingDutiesActivity, "Network error.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performReject(dutyId: Long, remarks: String) {
        val pd = showProgress("Rejecting duty…")
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.rejectDuty(dutyId, ApprovalRequest(remarks))
                pd.dismiss()
                if (res.isSuccessful) {
                    Toast.makeText(this@PendingDutiesActivity, "Duty rejected.", Toast.LENGTH_SHORT).show()
                    loadAllSections()
                } else {
                    Toast.makeText(this@PendingDutiesActivity, parseError(res.errorBody()?.string()), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                pd.dismiss()
                Toast.makeText(this@PendingDutiesActivity, "Network error.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseError(body: String?): String {
        if (body.isNullOrBlank()) return "Action failed."
        return try {
            val json = org.json.JSONObject(body)
            json.optString("error", json.optString("message", "Action failed."))
        } catch (e: Exception) { "Action failed." }
    }

    private fun todayString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${
            (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        }-${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
    }
}
