package com.vallo.nasync.features.profile

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.vallo.nasync.R
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.features.auth.LoginActivity
import com.vallo.nasync.models.ChangePasswordRequest
import com.vallo.nasync.models.UpdateGmailRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvProfileInitials: TextView
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var tvChangePhoto: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvSchoolId: TextView
    private lateinit var tvDepartment: TextView
    private lateinit var etGmail: EditText
    private lateinit var tvGmailMsg: TextView
    private lateinit var btnSaveGmail: MaterialButton
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var tvPasswordMsg: TextView
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnLogOut: MaterialButton

    private val photoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadPhoto(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvProfileInitials = findViewById(R.id.tvProfileInitials)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        tvChangePhoto = findViewById(R.id.tvChangePhoto)
        tvFullName = findViewById(R.id.tvFullName)
        tvSchoolId = findViewById(R.id.tvSchoolId)
        tvDepartment = findViewById(R.id.tvDepartment)
        etGmail = findViewById(R.id.etGmail)
        tvGmailMsg = findViewById(R.id.tvGmailMsg)
        btnSaveGmail = findViewById(R.id.btnSaveGmail)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        tvPasswordMsg = findViewById(R.id.tvPasswordMsg)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogOut = findViewById(R.id.btnLogOut)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        val avatarClick = View.OnClickListener { photoPicker.launch("image/*") }
        tvProfileInitials.setOnClickListener(avatarClick)
        ivProfilePhoto.setOnClickListener(avatarClick)
        tvChangePhoto.setOnClickListener(avatarClick)

        btnSaveGmail.setOnClickListener { saveGmail() }
        btnChangePassword.setOnClickListener { changePassword() }
        btnLogOut.setOnClickListener { performLogOut() }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getCurrentUser()
                if (res.isSuccessful) {
                    val user = res.body()!!
                    tvFullName.text = "${user.firstName} ${user.lastName}"
                    tvSchoolId.text = user.schoolId
                    tvDepartment.text = user.branchName ?: user.departmentName ?: "—"
                    if (!etGmail.isFocused) {
                        etGmail.setText(user.personalGmail ?: "")
                    }
                    val firstInitial = user.firstName.firstOrNull()?.uppercaseChar()?.toString() ?: ""
                    val lastInitial = user.lastName.firstOrNull()?.uppercaseChar()?.toString() ?: ""
                    tvProfileInitials.text = (firstInitial + lastInitial).ifEmpty { "?" }

                    val photoUrl = user.profilePhotoUrl
                    if (!photoUrl.isNullOrBlank()) {
                        Glide.with(this@ProfileActivity).load(photoUrl).circleCrop().into(ivProfilePhoto)
                        ivProfilePhoto.visibility = View.VISIBLE
                        tvProfileInitials.visibility = View.INVISIBLE
                    } else {
                        ivProfilePhoto.visibility = View.GONE
                        tvProfileInitials.visibility = View.VISIBLE
                    }

                    getSharedPreferences("auth_prefs", MODE_PRIVATE).edit()
                        .putString("profile_photo_url", photoUrl)
                        .putString("personal_gmail", user.personalGmail)
                        .apply()
                }
            } catch (_: Exception) {}
        }
    }

    private fun uploadPhoto(uri: Uri) {
        lifecycleScope.launch {
            try {
                val mimeType = contentResolver.getType(uri)
                    ?.takeIf { it == "image/jpeg" || it == "image/png" }
                    ?: run {
                        Toast.makeText(this@ProfileActivity, "Only JPG and PNG images are supported.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                val ext = if (mimeType == "image/png") "png" else "jpg"
                val bytes = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.readBytes()
                } ?: return@launch
                val part = MultipartBody.Part.createFormData(
                    "file", "photo.$ext",
                    bytes.toRequestBody(mimeType.toMediaType())
                )
                val res = RetrofitClient.api.uploadSelfPhoto(part)
                if (res.isSuccessful) {
                    val photoUrl = res.body()?.get("photoUrl")
                    getSharedPreferences("auth_prefs", MODE_PRIVATE).edit()
                        .putString("profile_photo_url", photoUrl)
                        .apply()
                    if (!photoUrl.isNullOrBlank()) {
                        Glide.with(this@ProfileActivity).load(photoUrl).circleCrop().into(ivProfilePhoto)
                        ivProfilePhoto.visibility = View.VISIBLE
                        tvProfileInitials.visibility = View.INVISIBLE
                    }
                    Toast.makeText(this@ProfileActivity, "Photo updated.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, parseError(res.errorBody()?.string()), Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this@ProfileActivity, "Photo upload failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveGmail() {
        val gmail = etGmail.text.toString().trim()
        if (gmail.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(gmail).matches()) {
            showGmailMsg("Invalid email format.", isError = true)
            return
        }
        tvGmailMsg.visibility = View.GONE
        btnSaveGmail.isEnabled = false
        btnSaveGmail.text = "Saving…"
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.updateGmail(UpdateGmailRequest(gmail.ifEmpty { null }))
                if (res.isSuccessful) {
                    showGmailMsg("Gmail saved.", isError = false)
                    getSharedPreferences("auth_prefs", MODE_PRIVATE).edit()
                        .putString("personal_gmail", gmail)
                        .apply()
                } else {
                    showGmailMsg(parseError(res.errorBody()?.string()), isError = true)
                }
            } catch (_: Exception) {
                showGmailMsg("Network error.", isError = true)
            } finally {
                btnSaveGmail.isEnabled = true
                btnSaveGmail.text = "Save Gmail"
            }
        }
    }

    private fun changePassword() {
        val current = etCurrentPassword.text.toString()
        val newPass = etNewPassword.text.toString()
        val confirm = etConfirmPassword.text.toString()
        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showPasswordMsg("Please fill in all fields.", isError = true)
            return
        }
        if (newPass.length < 8) {
            showPasswordMsg("New password must be at least 8 characters.", isError = true)
            return
        }
        if (newPass != confirm) {
            showPasswordMsg("Passwords do not match.", isError = true)
            return
        }
        tvPasswordMsg.visibility = View.GONE
        btnChangePassword.isEnabled = false
        btnChangePassword.text = "Changing…"
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.changePassword(ChangePasswordRequest(current, newPass))
                if (res.isSuccessful) {
                    etCurrentPassword.text.clear()
                    etNewPassword.text.clear()
                    etConfirmPassword.text.clear()
                    showPasswordMsg("Password changed successfully.", isError = false)
                } else {
                    showPasswordMsg(parseError(res.errorBody()?.string()), isError = true)
                }
            } catch (_: Exception) {
                showPasswordMsg("Network error.", isError = true)
            } finally {
                btnChangePassword.isEnabled = true
                btnChangePassword.text = "Change Password"
            }
        }
    }

    private fun showGmailMsg(msg: String, isError: Boolean) {
        tvGmailMsg.text = msg
        tvGmailMsg.setBackgroundColor(if (isError) 0xFFFFEBEE.toInt() else 0xFFE8F5E9.toInt())
        tvGmailMsg.setTextColor(if (isError) getColor(R.color.error_red) else getColor(R.color.status_present))
        tvGmailMsg.visibility = View.VISIBLE
    }

    private fun showPasswordMsg(msg: String, isError: Boolean) {
        tvPasswordMsg.text = msg
        tvPasswordMsg.setBackgroundColor(if (isError) 0xFFFFEBEE.toInt() else 0xFFE8F5E9.toInt())
        tvPasswordMsg.setTextColor(if (isError) getColor(R.color.error_red) else getColor(R.color.status_present))
        tvPasswordMsg.visibility = View.VISIBLE
    }

    private fun performLogOut() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out", null)
            .setNegativeButton("Cancel", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val btnPos = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnNeg = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            btnPos.isEnabled = false
            btnNeg.isEnabled = false
            btnPos.text = "Logging out…"
            lifecycleScope.launch {
                try {
                    RetrofitClient.api.logout()
                } catch (_: Exception) {}
                getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().clear().apply()
                RetrofitClient.token = ""
                dialog.dismiss()
                startActivity(
                    Intent(this@ProfileActivity, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        }
    }

    private fun parseError(body: String?): String {
        if (body.isNullOrBlank()) return "Action failed."
        return try {
            val json = org.json.JSONObject(body)
            json.optString("error", json.optString("message", "Action failed."))
        } catch (_: Exception) { "Action failed." }
    }
}
