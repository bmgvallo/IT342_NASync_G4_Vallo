package com.vallo.nasync.features.depthead

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.vallo.nasync.R
import com.vallo.nasync.features.scholar.ScholarDashboardActivity
import com.vallo.nasync.models.DutyResponse

class PendingDutyAdapter(
    private val duties: List<DutyResponse>,
    private val onApprove: (Long) -> Unit,
    private val onReject: (Long) -> Unit
) : RecyclerView.Adapter<PendingDutyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvScholarName: TextView = view.findViewById(R.id.tvScholarName)
        val tvScholarId: TextView = view.findViewById(R.id.tvScholarId)
        val tvBranchName: TextView = view.findViewById(R.id.tvBranchName)
        val tvDutyType: TextView = view.findViewById(R.id.tvDutyType)
        val tvDutyDate: TextView = view.findViewById(R.id.tvDutyDate)
        val tvTimeIn: TextView = view.findViewById(R.id.tvTimeIn)
        val tvActualTimeOut: TextView = view.findViewById(R.id.tvActualTimeOut)
        val tvExpectedOut: TextView = view.findViewById(R.id.tvExpectedOut)
        val tvWaitingHint: TextView = view.findViewById(R.id.tvWaitingHint)
        val btnApprove: MaterialButton = view.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_duty, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val duty = duties[position]
        holder.tvScholarName.text = duty.scholarName ?: "—"
        holder.tvScholarId.text = duty.scholarSchoolId ?: "—"
        holder.tvBranchName.text = duty.branchName ?: ""
        holder.tvDutyType.text = duty.dutyType
        holder.tvDutyDate.text = formatDate(duty.dutyDate)
        holder.tvTimeIn.text = "In: ${ScholarDashboardActivity.formatTime(duty.timeIn)}"
        holder.tvActualTimeOut.text = if (duty.timeOut != null)
            "Out: ${ScholarDashboardActivity.formatTime(duty.timeOut)}"
        else "Out: —"

        val expectedOut = duty.expectedTimeOut
        if (expectedOut != null) {
            holder.tvExpectedOut.text = "Expected: ${ScholarDashboardActivity.formatTime(expectedOut)}"
            holder.tvExpectedOut.visibility = View.VISIBLE
        } else {
            holder.tvExpectedOut.visibility = View.GONE
        }

        val canAct = duty.timeOut != null
        holder.btnApprove.isEnabled = canAct
        holder.btnReject.isEnabled = canAct
        holder.btnApprove.alpha = if (canAct) 1f else 0.35f
        holder.btnReject.alpha = if (canAct) 1f else 0.35f
        holder.tvWaitingHint.visibility = if (canAct) View.GONE else View.VISIBLE

        holder.btnApprove.setOnClickListener { if (canAct) onApprove(duty.dutyId) }
        holder.btnReject.setOnClickListener { if (canAct) onReject(duty.dutyId) }
    }

    override fun getItemCount() = duties.size

    private fun formatDate(dateStr: String): String {
        return try {
            val parts = dateStr.split("-")
            val year = parts[0]
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            "${months[month - 1]} $day, $year"
        } catch (e: Exception) { dateStr }
    }
}
