package com.ratu.resep_in.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ratu.resep_in.profile.ArsipActivity
import com.ratu.resep_in.R
import com.ratu.resep_in.auth.LoginActivity


class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val menuPreferensi = findViewById<LinearLayout>(R.id.menuPreferensi)
        val menuArsip = findViewById<LinearLayout>(R.id.menuArsip)
        val menuLogout = findViewById<LinearLayout>(R.id.menuLogout)


        menuPreferensi.setOnClickListener {
            startActivity(Intent(this, PreferenceActivity::class.java))
        }

        menuArsip.setOnClickListener {
            startActivity(Intent(this, ArsipActivity::class.java))
        }

        menuLogout.setOnClickListener {
            showLogoutDialog()
        }

        btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ -> logoutUser() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}