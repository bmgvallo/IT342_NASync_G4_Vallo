package com.vallo.nasync.features.depthead

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vallo.nasync.R
import com.vallo.nasync.features.scholar.ScholarDashboardActivity
import com.vallo.nasync.models.DutyResponse

class DutyItemAdapter(
    private val duties: List<DutyResponse>
) : RecyclerView.Adapter<DutyItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvScholarName: TextView = view.findViewById(R.id.tvScholarName)
        val tvScholarId: TextView = view.findViewById(R.id.tvScholarId)
        val tvBadge: TextView = view.findViewById(R.id.tvBadge)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTimeIn: TextView = view.findViewById(R.id.tvTimeIn)
        val tvTimeOut: TextView = view.findViewById(R.id.tvTimeOut)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_duty_branch, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val duty = duties[position]
        holder.tvScholarName.text = duty.scholarName ?: "—"
        holder.tvScholarId.text = duty.scholarSchoolId ?: "—"
        holder.tvBadge.text = duty.dutyType
        holder.tvDate.text = formatDate(duty.dutyDate)
        holder.tvTimeIn.text = "In: ${ScholarDashboardActivity.formatTime(duty.timeIn)}"
        holder.tvTimeOut.text = "Out: ${if (duty.timeOut != null) ScholarDashboardActivity.formatTime(duty.timeOut) else "—"}"

        val (statusText, colorRes) = when {
            duty.approvalStatus == "APPROVED" -> "APPROVED" to R.color.status_approved
            duty.approvalStatus == "REJECTED" ->
                "REJECTED${if (!duty.remarks.isNullOrBlank()) ": ${duty.remarks}" else ""}" to R.color.status_rejected
            duty.attendanceStatus == "ABSENT" -> "ABSENT" to R.color.status_rejected
            else -> duty.approvalStatus to R.color.text_soft
        }
        holder.tvStatus.text = statusText
        holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))
    }

    override fun getItemCount() = duties.size

    private fun formatDate(dateStr: String): String {
        return try {
            val parts = dateStr.split("-")
            val year = parts[0]; val month = parts[1].toInt(); val day = parts[2].toInt()
            val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            "${months[month - 1]} $day, $year"
        } catch (e: Exception) { dateStr }
    }
}
