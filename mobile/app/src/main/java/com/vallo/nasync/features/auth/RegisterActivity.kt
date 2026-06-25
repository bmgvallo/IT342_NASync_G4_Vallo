package com.vallo.nasync.features.auth

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.databinding.ActivityRegisterBinding
import com.vallo.nasync.models.DepartmentResponse
import com.vallo.nasync.models.BranchResponse
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var departments = listOf<DepartmentResponse>()
    private var branches = listOf<BranchResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupShiftSpinner()
        loadDepartments()

        binding.btnRegister.setOnClickListener { registerScholar() }
        binding.btnCancel.setOnClickListener { finish() }

        binding.spinnerDept.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position > 0) {
                    val dept = departments[position - 1]
                    loadBranches(dept.departmentId)
                } else {
                    binding.spinnerBranch.adapter = null
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupShiftSpinner() {
        val shifts = arrayOf("Select Shift", "Morning", "Afternoon")
        binding.spinnerShift.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, shifts).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun loadDepartments() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getDepartments()
                if (response.isSuccessful) {
                    departments = response.body()?.get("data") ?: emptyList()
                    val deptNames = listOf("Select Department") + departments.map { it.name }
                    binding.spinnerDept.adapter = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, deptNames).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Failed to load departments", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBranches(deptId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getBranches()
                if (response.isSuccessful) {
                    val allBranches = response.body()?.get("data") ?: emptyList()
                    branches = allBranches.filter { it.deptId == deptId }
                    val branchNames = listOf("Select Branch (Optional)") + branches.map { it.name }
                    binding.spinnerBranch.adapter = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, branchNames).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error loading branches", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerScholar() {
        val schoolId = binding.etSchoolId.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val deptPos = binding.spinnerDept.selectedItemPosition
        val branchPos = binding.spinnerBranch.selectedItemPosition
        val shiftPos = binding.spinnerShift.selectedItemPosition

        if (schoolId.isEmpty() || fullName.isEmpty() || email.isEmpty() || deptPos == 0) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val nameParts = fullName.split(" ", limit = 2)
        val firstName = nameParts.getOrElse(0) { "" }
        val lastName = nameParts.getOrElse(1) { "" }

        val deptId = departments[deptPos - 1].departmentId
        val branchId = if (branchPos > 0) branches[branchPos - 1].branchId else null
        val shift = when (shiftPos) {
            1 -> "MORNING"
            2 -> "AFTERNOON"
            else -> null
        }

        val request = RegisterRequest(
            schoolId = schoolId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = schoolId,
            role = "SCHOLAR",
            deptId = deptId,
            branchId = branchId,
            shift = shift,
            expectedTimeIn = if (shift == "MORNING") "08:00:00" else "13:00:00",
            expectedTimeOut = if (shift == "MORNING") "12:00:00" else "17:00:00"
        )

        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registering…"
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.registerUser(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Scholar registered successfully!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Registration failed"
                    Toast.makeText(this@RegisterActivity, "Error: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Register"
            }
        }
    }

}