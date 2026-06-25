package com.vallo.nasync.features.admin

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vallo.nasync.R
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.features.depthead.ScholarAdapter
import kotlinx.coroutines.launch

class AdminScholarsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_scholars)

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        if (RetrofitClient.token.isEmpty()) {
            RetrofitClient.token = prefs.getString("access_token", "") ?: ""
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        loadScholars()
    }

    private fun loadScholars() {
        val pb = findViewById<ProgressBar>(R.id.pbScholars)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val rv = findViewById<RecyclerView>(R.id.rvScholars)

        pb.visibility = View.VISIBLE
        rv.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getScholars()
                pb.visibility = View.GONE
                if (res.isSuccessful) {
                    val scholars = res.body() ?: emptyList()
                    if (scholars.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        rv.visibility = View.VISIBLE
                        rv.adapter = ScholarAdapter(scholars) { scholar ->
                            ScholarDetailBottomSheet.newInstance(
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
                }
            } catch (_: Exception) {
                pb.visibility = View.GONE
                tvEmpty.text = "Network error. Check your connection."
                tvEmpty.visibility = View.VISIBLE
            }
        }
    }
}
