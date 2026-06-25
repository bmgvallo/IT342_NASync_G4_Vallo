package com.vallo.nasync.features.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.databinding.ActivityLoginBinding
import com.vallo.nasync.features.admin.AdminDashboardActivity
import com.vallo.nasync.features.depthead.DeptHeadDashboardActivity
import com.vallo.nasync.features.scholar.ScholarDashboardActivity
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Auto-login if valid token already saved
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val savedToken = prefs.getString("access_token", "") ?: ""
        val savedRole = prefs.getString("role", "") ?: ""
        val firstTimeLogin = prefs.getBoolean("first_time_login", false)
        if (savedToken.isNotEmpty() && savedRole.isNotEmpty()) {
            RetrofitClient.token = savedToken
            if (firstTimeLogin) {
                navigateTo(ChangePasswordActivity::class.java)
            } else {
                navigateToDashboard(savedRole)
            }
            return
        }

        binding.btnLogin.setOnClickListener {
            val schoolId = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (schoolId.isEmpty() || password.isEmpty()) {
                showError("Please fill in all fields.")
                return@setOnClickListener
            }
            hideError()
            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "Signing in…"
            performLogin(schoolId, password)
        }

        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google sign-in not supported on mobile yet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin(schoolId: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(schoolId, password))
                if (response.isSuccessful) {
                    val auth = response.body()!!
                    RetrofitClient.token = auth.accessToken
                    saveUserData(auth)
                    if (auth.user.firstTimeLogin) {
                        navigateTo(ChangePasswordActivity::class.java)
                    } else {
                        navigateToDashboard(auth.user.role)
                    }
                } else {
                    val msg = parseErrorBody(response.errorBody()?.string())
                    showError(msg)
                }
            } catch (e: Exception) {
                showError("Unable to connect. Check your network connection.")
            } finally {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Sign In"
            }
        }
    }

    private fun parseErrorBody(body: String?): String {
        if (body.isNullOrBlank()) return "Invalid credentials."
        return try {
            val json = JSONObject(body)
            when {
                json.has("error") -> json.getString("error")
                json.has("message") -> json.getString("message")
                else -> "Invalid credentials."
            }
        } catch (e: Exception) {
            "Invalid credentials."
        }
    }

    private fun saveUserData(auth: AuthResponse) {
        getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().apply {
            putString("access_token", auth.accessToken)
            putString("refresh_token", auth.refreshToken)
            putString("school_id", auth.user.schoolId)
            putString("first_name", auth.user.firstName)
            putString("last_name", auth.user.lastName)
            putString("email", auth.user.email)
            putString("role", auth.user.role)
            putBoolean("first_time_login", auth.user.firstTimeLogin)
            putString("expected_time_in", auth.user.expectedTimeIn)
            putString("expected_time_out", auth.user.expectedTimeOut)
            putString("shift", auth.user.shift)
            putString("department_name", auth.user.departmentName)
            putString("branch_name", auth.user.branchName)
            putString("profile_photo_url", auth.user.profilePhotoUrl)
            apply()
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }

    private fun navigateToDashboard(role: String) {
        val target = when (role) {
            "ADMIN" -> AdminDashboardActivity::class.java
            "DEPARTMENT_HEAD" -> DeptHeadDashboardActivity::class.java
            else -> ScholarDashboardActivity::class.java
        }
        navigateTo(target)
    }

    private fun navigateTo(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
