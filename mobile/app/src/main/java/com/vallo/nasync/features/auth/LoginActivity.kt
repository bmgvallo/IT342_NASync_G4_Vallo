package com.vallo.nasync.features.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vallo.nasync.features.admin.AdminDashboardActivity
import com.vallo.nasync.ScholarDashboardActivity
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val schoolId = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (schoolId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            performLogin(schoolId, password)
        }

        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google login not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin(schoolId: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(schoolId, password))
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    Log.d("LOGIN", "Response: $authResponse")
                    authResponse?.let {
                        saveUserData(it)
                        navigateToDashboard(it.user.role)
                    } ?: run {
                        Log.e("LOGIN", "Body is null")
                        Toast.makeText(this@LoginActivity, "Empty response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LOGIN", "Error: $errorBody")
                    Toast.makeText(this@LoginActivity, "Error: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("LOGIN", "Exception", e)
                Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveUserData(authResponse: AuthResponse) {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("access_token", authResponse.accessToken)
            putString("refresh_token", authResponse.refreshToken)
            putString("school_id", authResponse.user.schoolId)
            putString("first_name", authResponse.user.firstName)
            putString("last_name", authResponse.user.lastName)
            putString("email", authResponse.user.email)
            putString("role", authResponse.user.role)
            apply()
        }
    }

    private fun navigateToDashboard(role: String) {
        val intent = when (role) {
            "ADMIN" -> Intent(this, AdminDashboardActivity::class.java)
            "SCHOLAR" -> Intent(this, ScholarDashboardActivity::class.java)
            "DEPARTMENT_HEAD" -> Intent(this, ScholarDashboardActivity::class.java)
            else -> Intent(this, ScholarDashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}