package com.vallo.nasync.features.depthead

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vallo.nasync.BaseDrawerActivity
import com.vallo.nasync.R
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.features.admin.ScholarDetailBottomSheet
import kotlinx.coroutines.launch

class ScholarsActivity : BaseDrawerActivity() {

    override fun getContentLayoutId() = R.layout.content_scholars
    override fun getCurrentNavItemId() = R.id.nav_depthead_scholars

    private lateinit var rvScholars: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("My Scholars")

        rvScholars = findViewById(R.id.rvScholars)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvScholars.layoutManager = LinearLayoutManager(this)

        loadScholars()
    }

    private fun loadScholars() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getBranchScholars()
                if (res.isSuccessful) {
                    val scholars = res.body() ?: emptyList()
                    if (scholars.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        rvScholars.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvScholars.visibility = View.VISIBLE
                        rvScholars.adapter = ScholarAdapter(scholars) { scholar ->
                            ScholarDetailBottomSheet.newInstanceForDeptHead(
                                schoolId       = scholar.schoolId,
                                firstName      = scholar.firstName,
                                lastName       = scholar.lastName,
                                email          = scholar.email,
                                departmentName = scholar.departmentName,
                                branchName     = scholar.branchName,
                                active         = scholar.active
                            ).show(supportFragmentManager, "scholar_detail")
                        }
                    }
                } else {
                    tvEmpty.text = "Failed to load scholars."
                    tvEmpty.visibility = View.VISIBLE
                    rvScholars.visibility = View.GONE
                }
            } catch (e: Exception) {
                tvEmpty.text = "Network error. Check your connection."
                tvEmpty.visibility = View.VISIBLE
                rvScholars.visibility = View.GONE
            }
        }
    }
}
