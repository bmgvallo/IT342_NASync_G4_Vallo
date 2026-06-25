package com.vallo.nasync

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vallo.nasync.api.RetrofitClient
import com.vallo.nasync.features.admin.AdminDashboardActivity
import com.vallo.nasync.features.auth.RegisterActivity
import com.vallo.nasync.features.depthead.DeptHeadDashboardActivity
import com.vallo.nasync.features.depthead.PendingDutiesActivity
import com.vallo.nasync.features.depthead.ScholarsActivity
import com.vallo.nasync.features.profile.ProfileActivity
import com.vallo.nasync.features.scholar.DutyHistoryActivity
import com.vallo.nasync.features.scholar.ScholarDashboardActivity

abstract class BaseDrawerActivity : AppCompatActivity() {

    protected lateinit var tvTitle: TextView
    protected lateinit var tvAvatar: TextView
    protected lateinit var ivTopbarAvatarPhoto: ImageView
    protected lateinit var bottomNav: BottomNavigationView

    abstract fun getContentLayoutId(): Int
    abstract fun getCurrentNavItemId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_base)
        layoutInflater.inflate(getContentLayoutId(), findViewById(R.id.content_frame), true)

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        if (RetrofitClient.token.isEmpty()) {
            RetrofitClient.token = prefs.getString("access_token", "") ?: ""
        }

        tvTitle = findViewById(R.id.tvTitle)
        tvAvatar = findViewById(R.id.tvAvatar)
        ivTopbarAvatarPhoto = findViewById(R.id.ivTopbarAvatarPhoto)
        bottomNav = findViewById(R.id.bottom_nav)

        setupTopbarAvatar()
        setupBottomNavigation()
    }

    private fun setupTopbarAvatar() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("first_name", "") ?: ""
        val lastName = prefs.getString("last_name", "") ?: ""
        val firstInitial = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        val lastInitial = lastName.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        tvAvatar.text = (firstInitial + lastInitial).ifEmpty { "U" }

        val photoUrl = prefs.getString("profile_photo_url", null)
        if (!photoUrl.isNullOrBlank()) {
            Glide.with(this).load(photoUrl).circleCrop().into(ivTopbarAvatarPhoto)
            ivTopbarAvatarPhoto.visibility = View.VISIBLE
            tvAvatar.visibility = View.INVISIBLE
        } else {
            ivTopbarAvatarPhoto.visibility = View.GONE
            tvAvatar.visibility = View.VISIBLE
        }
    }

    private fun setupBottomNavigation() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val role = prefs.getString("role", "SCHOLAR") ?: "SCHOLAR"

        val menuRes = when (role) {
            "DEPARTMENT_HEAD" -> R.menu.bottom_nav_depthead
            "ADMIN" -> R.menu.bottom_nav_admin
            else -> R.menu.bottom_nav_scholar
        }
        bottomNav.inflateMenu(menuRes)

        val currentId = getCurrentNavItemId()
        if (bottomNav.menu.findItem(currentId) != null) {
            bottomNav.selectedItemId = currentId
        }

        bottomNav.setOnItemSelectedListener { item ->
            val target: Class<*>? = when (item.itemId) {
                R.id.nav_dashboard -> ScholarDashboardActivity::class.java
                R.id.nav_my_duties -> DutyHistoryActivity::class.java
                R.id.nav_depthead_dashboard -> DeptHeadDashboardActivity::class.java
                R.id.nav_depthead_pending -> PendingDutiesActivity::class.java
                R.id.nav_depthead_scholars -> ScholarsActivity::class.java
                R.id.nav_admin_dashboard -> AdminDashboardActivity::class.java
                R.id.nav_register_user -> RegisterActivity::class.java
                R.id.nav_profile -> ProfileActivity::class.java
                else -> null
            }
            if (target != null && this.javaClass != target) {
                startActivity(
                    Intent(this, target)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            }
            true
        }
    }

    protected fun setTitle(title: String) {
        tvTitle.text = title
    }
}
