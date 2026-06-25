package com.vallo.nasync.features.scholar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vallo.nasync.R
import com.vallo.nasync.models.DutyResponse

class DutyAdapter(private val duties: List<DutyResponse>) :
    RecyclerView.Adapter<DutyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDutyDate)
        val tvType: TextView = view.findViewById(R.id.tvDutyType)
        val tvTime: TextView = view.findViewById(R.id.tvDutyTime)
        val tvAttendance: TextView = view.findViewById(R.id.tvAttendanceStatus)
        val tvApproval: TextView = view.findViewById(R.id.tvApprovalStatus)
        val tvRemarks: TextView = view.findViewById(R.id.tvRemarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_duty, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val duty = duties[position]
        val ctx = holder.itemView.context

        holder.tvDate.text = formatDate(duty.dutyDate)
        holder.tvType.text = duty.dutyType

        val timeIn = ScholarDashboardActivity.formatTime(duty.timeIn)
        val timeOut = ScholarDashboardActivity.formatTime(duty.timeOut)
        holder.tvTime.text = "$timeIn  →  $timeOut"

        val attendance = duty.attendanceStatus ?: "PENDING"
        holder.tvAttendance.text = attendance
        holder.tvAttendance.setTextColor(
            when (attendance) {
                "PRESENT" -> ctx.getColor(R.color.status_present)
                "LATE"    -> ctx.getColor(R.color.status_late)
                "ABSENT"  -> ctx.getColor(R.color.status_absent)
                else      -> ctx.getColor(R.color.grey)
            }
        )

        val approval = duty.approvalStatus
        holder.tvApproval.text = approval
        holder.tvApproval.setTextColor(
            when (approval) {
                "APPROVED" -> ctx.getColor(R.color.status_approved)
                "REJECTED" -> ctx.getColor(R.color.status_rejected)
                else       -> ctx.getColor(R.color.status_pending)
            }
        )

        if (!duty.remarks.isNullOrBlank()) {
            holder.tvRemarks.text = "Note: ${duty.remarks}"
            holder.tvRemarks.visibility = View.VISIBLE
        } else {
            holder.tvRemarks.visibility = View.GONE
        }
    }

    override fun getItemCount() = duties.size

    private fun formatDate(dateStr: String): String {
        return try {
            val parts = dateStr.split("-")
            val year = parts[0]
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val months = arrayOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            "${months[month - 1]} $day, $year"
        } catch (e: Exception) {
            dateStr
        }
    }
}
