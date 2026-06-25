package com.vallo.nasync.features.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vallo.nasync.R
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.features.scholar.ScholarDashboardActivity
import kotlinx.coroutines.launch

class ScholarDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_SCHOOL_ID      = "schoolId"
        private const val ARG_FIRST_NAME     = "firstName"
        private const val ARG_LAST_NAME      = "lastName"
        private const val ARG_EMAIL          = "email"
        private const val ARG_DEPARTMENT     = "departmentName"
        private const val ARG_BRANCH         = "branchName"
        private const val ARG_ACTIVE         = "active"
        private const val ARG_USE_DEPTHEAD   = "useDeptHead"

        fun newInstance(
            schoolId: String,
            firstName: String,
            lastName: String,
            email: String,
            departmentName: String?,
            branchName: String?,
            active: Boolean
        ) = ScholarDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_SCHOOL_ID,  schoolId)
                putString(ARG_FIRST_NAME, firstName)
                putString(ARG_LAST_NAME,  lastName)
                putString(ARG_EMAIL,      email)
                putString(ARG_DEPARTMENT, departmentName)
                putString(ARG_BRANCH,     branchName)
                putBoolean(ARG_ACTIVE,    active)
                putBoolean(ARG_USE_DEPTHEAD, false)
            }
        }

        fun newInstanceForDeptHead(
            schoolId: String,
            firstName: String,
            lastName: String,
            email: String,
            departmentName: String?,
            branchName: String?,
            active: Boolean
        ) = ScholarDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_SCHOOL_ID,  schoolId)
                putString(ARG_FIRST_NAME, firstName)
                putString(ARG_LAST_NAME,  lastName)
                putString(ARG_EMAIL,      email)
                putString(ARG_DEPARTMENT, departmentName)
                putString(ARG_BRANCH,     branchName)
                putBoolean(ARG_ACTIVE,    active)
                putBoolean(ARG_USE_DEPTHEAD, true)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_scholar_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val schoolId       = args.getString(ARG_SCHOOL_ID,   "")!!
        val firstName      = args.getString(ARG_FIRST_NAME,  "")!!
        val lastName       = args.getString(ARG_LAST_NAME,   "")!!
        val email          = args.getString(ARG_EMAIL,       "")!!
        val departmentName = args.getString(ARG_DEPARTMENT)
        val branchName     = args.getString(ARG_BRANCH)
        val active         = args.getBoolean(ARG_ACTIVE, true)
        val useDeptHead    = args.getBoolean(ARG_USE_DEPTHEAD, false)

        view.findViewById<TextView>(R.id.tvScholarName).text = "$firstName $lastName"
        view.findViewById<TextView>(R.id.tvSchoolId).text    = schoolId
        view.findViewById<TextView>(R.id.tvEmail).text       = email
        view.findViewById<TextView>(R.id.tvDepartment).text  = departmentName ?: "—"
        view.findViewById<TextView>(R.id.tvBranch).text      = branchName ?: "—"

        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = if (active) "Active" else "Inactive"
        tvStatus.setTextColor(
            requireContext().getColor(if (active) R.color.status_present else R.color.status_absent)
        )

        view.findViewById<View>(R.id.btnClose).setOnClickListener { dismiss() }

        loadSchedule(view, schoolId, useDeptHead)
    }

    private fun loadSchedule(view: View, schoolId: String, useDeptHead: Boolean) {
        val pbSchedule     = view.findViewById<ProgressBar>(R.id.pbSchedule)
        val tvNoSchedule   = view.findViewById<TextView>(R.id.tvNoSchedule)
        val layoutSchedule = view.findViewById<LinearLayout>(R.id.layoutSchedule)

        pbSchedule.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val entries = if (useDeptHead) {
                    val res = RetrofitClient.api.getDeptHeadScholarDetails(schoolId)
                    if (res.isSuccessful) res.body()?.schedule ?: emptyList() else null
                } else {
                    val res = RetrofitClient.api.getScholarSchedule(schoolId)
                    if (res.isSuccessful) res.body() ?: emptyList() else null
                }

                pbSchedule.visibility = View.GONE

                if (entries == null) {
                    tvNoSchedule.text = "Could not load schedule."
                    tvNoSchedule.visibility = View.VISIBLE
                } else if (entries.isEmpty()) {
                    tvNoSchedule.visibility = View.VISIBLE
                } else {
                    entries.forEach { entry ->
                        layoutSchedule.addView(
                            buildScheduleRow(entry.dayOfWeek, entry.primaryTimeIn, entry.primaryTimeOut)
                        )
                        if (entry.secondaryTimeIn != null && entry.secondaryTimeOut != null) {
                            layoutSchedule.addView(
                                buildScheduleRow(entry.dayOfWeek, entry.secondaryTimeIn, entry.secondaryTimeOut, secondary = true)
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                pbSchedule.visibility = View.GONE
                tvNoSchedule.text = "Could not load schedule."
                tvNoSchedule.visibility = View.VISIBLE
            }
        }
    }

    private fun buildScheduleRow(
        dayOfWeek: String,
        timeIn: String?,
        timeOut: String?,
        secondary: Boolean = false
    ): View {
        val ctx = requireContext()
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 4, 0, 4)
        }

        val dayLabel = dayOfWeek.lowercase().replaceFirstChar { it.uppercaseChar() }
        val suffix   = if (secondary) " (Shift 2)" else ""

        val tvDay = TextView(ctx).apply {
            text = "$dayLabel$suffix"
            textSize = 13f
            setTextColor(ctx.getColor(R.color.text_soft))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvTime = TextView(ctx).apply {
            val inStr  = ScholarDashboardActivity.formatTime(timeIn)
            val outStr = ScholarDashboardActivity.formatTime(timeOut)
            text = "$inStr – $outStr"
            textSize = 13f
            setTextColor(ctx.getColor(R.color.maroon))
        }

        row.addView(tvDay)
        row.addView(tvTime)
        return row
    }
}
