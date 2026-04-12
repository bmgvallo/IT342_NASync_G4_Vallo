import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.vallo.nasync.LoginActivity
import com.vallo.nasync.R
import com.vallo.nasync.RegisterActivity

abstract class BaseDrawerActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navView: NavigationView
    protected lateinit var tvTitle: TextView
    protected lateinit var ivMenu: ImageView
    protected lateinit var tvAvatar: TextView

    abstract fun getContentLayoutId(): Int
    abstract fun getCurrentNavItemId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_base)

        layoutInflater.inflate(getContentLayoutId(), findViewById(R.id.content_frame), true)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        tvTitle = findViewById(R.id.tvTitle)
        ivMenu = findViewById(R.id.ivMenu)
        tvAvatar = findViewById(R.id.tvAvatar)

        setupToolbar()
        setupDrawerHeader()
        setupNavigationMenu()
    }

    private fun setupToolbar() {
        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupDrawerHeader() {
        val headerView = navView.getHeaderView(0)
        val drawerAvatar = headerView.findViewById<TextView>(R.id.drawer_avatar)
        val drawerName = headerView.findViewById<TextView>(R.id.drawer_name)
        val drawerRole = headerView.findViewById<TextView>(R.id.drawer_role)
        val drawerId = headerView.findViewById<TextView>(R.id.drawer_id)

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("first_name", "") ?: ""
        val lastName = prefs.getString("last_name", "") ?: ""
        val role = prefs.getString("role", "SCHOLAR") ?: "SCHOLAR"
        val schoolId = prefs.getString("school_id", "") ?: ""

        drawerName.text = "$firstName $lastName"
        drawerRole.text = role.replace("_", " ")
        drawerId.text = schoolId
        drawerAvatar.text = if (firstName.isNotEmpty()) firstName[0].toString() else "U"
        tvAvatar.text = drawerAvatar.text
    }

    private fun setupNavigationMenu() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val role = prefs.getString("role", "SCHOLAR") ?: "SCHOLAR"

        navView.menu.findItem(R.id.group_scholar)?.isVisible = role == "SCHOLAR"
        navView.menu.findItem(R.id.group_admin)?.isVisible = role == "ADMIN"

        navView.setCheckedItem(getCurrentNavItemId())

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard, R.id.nav_admin_dashboard -> {
                    true
                }
                R.id.nav_my_duties -> {
                    Toast.makeText(this, "My Duties coming soon", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_register_user -> {
                    startActivity(Intent(this, RegisterActivity::class.java))
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_manage_depts -> {
                    Toast.makeText(this, "Manage Departments coming soon", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_semesters -> {
                    Toast.makeText(this, "Semesters coming soon", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_sign_out -> {
                    prefs.edit().clear().apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                    drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    drawerLayout.closeDrawers()
                    false
                }
            }
        }
    }

    protected fun setTitle(title: String) {
        tvTitle.text = title
    }
}