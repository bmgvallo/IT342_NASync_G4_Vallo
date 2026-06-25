package com.vallo.nasync.features.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.vallo.nasync.R
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.features.admin.AdminDashboardActivity
import com.vallo.nasync.features.depthead.DeptHeadDashboardActivity
import com.vallo.nasync.features.scholar.ScholarDashboardActivity
import com.vallo.nasync.models.ChangePasswordRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etCurrent: EditText
    private lateinit var etNew: EditText
    private lateinit var etConfirm: EditText
    private lateinit var tvError: TextView
    private lateinit var btnChange: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        etCurrent = findViewById(R.id.etCurrentPassword)
        etNew = findViewById(R.id.etNewPassword)
        etConfirm = findViewById(R.id.etConfirmPassword)
        tvError = findViewById(R.id.tvError)
        btnChange = findViewById(R.id.btnChange)

        // Block back — must complete password change before continuing
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* blocked */ }
        })

        btnChange.setOnClickListener { attemptChange() }
    }

    private fun attemptChange() {
        val current = etCurrent.text.toString()
        val newPass = etNew.text.toString()
        val confirm = etConfirm.text.toString()

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all fields.")
            return
        }
        if (newPass.length < 8) {
            showError("New password must be at least 8 characters.")
            return
        }
        if (newPass != confirm) {
            showError("Passwords do not match.")
            return
        }

        hideError()
        btnChange.isEnabled = false
        btnChange.text = "Changing…"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.changePassword(
                    ChangePasswordRequest(current, newPass)
                )
                if (response.isSuccessful) {
                    val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    prefs.edit().putBoolean("first_time_login", false).apply()
                    val role = prefs.getString("role", "SCHOLAR") ?: "SCHOLAR"
                    val target = when (role) {
                        "ADMIN" -> AdminDashboardActivity::class.java
                        "DEPARTMENT_HEAD" -> DeptHeadDashboardActivity::class.java
                        else -> ScholarDashboardActivity::class.java
                    }
                    val intent = Intent(this@ChangePasswordActivity, target)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    val msg = parseError(response.errorBody()?.string())
                    showError(msg)
                }
            } catch (e: Exception) {
                showError("Network error. Check your connection.")
            } finally {
                btnChange.isEnabled = true
                btnChange.text = "Change Password"
            }
        }
    }

    private fun parseError(body: String?): String {
        if (body.isNullOrBlank()) return "Current password is incorrect."
        return try {
            val json = JSONObject(body)
            json.optString("error", json.optString("message", "Current password is incorrect."))
        } catch (e: Exception) {
            "Current password is incorrect."
        }
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        tvError.visibility = View.GONE
    }
}
