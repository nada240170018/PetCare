package com.example.petcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)

        val currentUser = FirebaseAuth.getInstance().currentUser
        tvEmail.text = currentUser?.email
            ?: getString(R.string.profile_email_unavailable)

        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.profile_logout_title))
                .setMessage(getString(R.string.profile_logout_message))
                .setPositiveButton(getString(R.string.profile_logout_confirm)) { _, _ ->
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        val bottomNavigation =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation.selectedItemId = R.id.nav_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home, R.id.nav_pets -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("OPEN_TAB", item.itemId)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.nav_schedule -> {
                    startActivity(
                        Intent(this, CareScheduleActivity::class.java)
                    )
                    finish()
                    true
                }

                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}
