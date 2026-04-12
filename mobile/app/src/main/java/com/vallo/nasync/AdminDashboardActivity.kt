package com.vallo.nasync

import BaseDrawerActivity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.vallo.nasync.api.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : BaseDrawerActivity() {

    override fun getContentLayoutId(): Int = R.layout.content_admin_dashboard
    override fun getCurrentNavItemId(): Int = R.id.nav_admin_dashboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Admin Dashboard")

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("first_name", "Admin") ?: "Admin"
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome back, $firstName!"

        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        findViewById<TextView>(R.id.tvDate).text = dateFormat.format(Date())

        findViewById<CardView>(R.id.card_register).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loadStats()
    }

    private fun loadStats() {
        val token = "Bearer ${getSharedPreferences("auth_prefs", MODE_PRIVATE).getString("access_token", "")}"
        lifecycleScope.launch {
            try {
                val scholarsResponse = RetrofitClient.api.getScholars(token)
                if (scholarsResponse.isSuccessful) {
                    val scholars = scholarsResponse.body() ?: emptyList()
                    updateStatCard(R.id.stat_scholars, "Scholars", scholars.size.toString())
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminDashboardActivity, "Failed to load stats", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatCard(cardId: Int, label: String, value: String) {
        val card = findViewById<androidx.cardview.widget.CardView>(cardId)
        card.findViewById<TextView>(R.id.stat_label)?.text = label
        card.findViewById<TextView>(R.id.stat_value)?.text = value
    }
}