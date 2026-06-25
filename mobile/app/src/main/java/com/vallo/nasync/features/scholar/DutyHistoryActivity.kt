package com.vallo.nasync.features.scholar

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vallo.nasync.BaseDrawerActivity
import com.vallo.nasync.R
import com.vallo.nasync.api.RetrofitClient
import kotlinx.coroutines.launch

class DutyHistoryActivity : BaseDrawerActivity() {

    override fun getContentLayoutId() = R.layout.content_duty_history
    override fun getCurrentNavItemId() = R.id.nav_my_duties

    private lateinit var rvDuties: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("My Duties")

        rvDuties = findViewById(R.id.rvDuties)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvDuties.layoutManager = LinearLayoutManager(this)

        loadDuties()
    }

    private fun loadDuties() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getMyDuties()
                if (res.isSuccessful) {
                    val duties = (res.body() ?: emptyList())
                        .sortedByDescending { it.dutyDate }
                    if (duties.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        rvDuties.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvDuties.visibility = View.VISIBLE
                        rvDuties.adapter = DutyAdapter(duties)
                    }
                } else {
                    tvEmpty.text = "Failed to load duties."
                    tvEmpty.visibility = View.VISIBLE
                    rvDuties.visibility = View.GONE
                }
            } catch (e: Exception) {
                tvEmpty.text = "Network error. Check your connection."
                tvEmpty.visibility = View.VISIBLE
                rvDuties.visibility = View.GONE
            }
        }
    }
}
