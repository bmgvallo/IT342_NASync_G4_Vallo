package com.vallo.nasync

import BaseDrawerActivity
import android.os.Bundle
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ScholarDashboardActivity : BaseDrawerActivity() {

    override fun getContentLayoutId(): Int = R.layout.content_scholar_dashboard
    override fun getCurrentNavItemId(): Int = R.id.nav_dashboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Dashboard")

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("first_name", "Scholar") ?: "Scholar"

        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $firstName!"
    }
}