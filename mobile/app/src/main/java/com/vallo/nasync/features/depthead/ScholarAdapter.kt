package com.vallo.nasync.features.depthead

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vallo.nasync.R
import com.vallo.nasync.models.UserResponse

class ScholarAdapter(
    private val scholars: List<UserResponse>,
    private val onClick: (UserResponse) -> Unit
) : RecyclerView.Adapter<ScholarAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvScholarAvatar)
        val tvName: TextView = view.findViewById(R.id.tvScholarName)
        val tvIdShift: TextView = view.findViewById(R.id.tvScholarIdShift)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scholar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scholar = scholars[position]
        holder.tvAvatar.text = scholar.firstName.firstOrNull()?.uppercase() ?: "S"
        holder.tvName.text = "${scholar.firstName} ${scholar.lastName}"
        val shift = scholar.shift?.lowercase()?.replaceFirstChar { it.uppercaseChar() } ?: "No shift"
        holder.tvIdShift.text = "${scholar.schoolId} · $shift"
        holder.itemView.setOnClickListener { onClick(scholar) }
    }

    override fun getItemCount() = scholars.size
}
