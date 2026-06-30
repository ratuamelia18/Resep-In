package com.ratu.resep_in

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.fragment.SavedFragment
import com.ratu.resep_in.fragments.*
import com.ratu.resep_in.ui.theme.PreferenceActivity
import com.ratu.resep_in.ui.theme.SearchActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        checkUserPreference()
        setupNavbar()

        if (savedInstanceState == null) {
            updateNavbarState("home")
            replaceFragment(HomeFragment())
        }
    }

    private fun setupNavbar() {
        val navbar = findViewById<View>(R.id.customNavbar)

        navbar.findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            replaceFragment(HomeFragment())
            updateNavbarState("home")
        }

        navbar.findViewById<LinearLayout>(R.id.navSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        navbar.findViewById<ImageView>(R.id.navAdd).setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }

        navbar.findViewById<LinearLayout>(R.id.navSave).setOnClickListener {
            replaceFragment(SavedFragment())
            updateNavbarState("save")
        }

        navbar.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            replaceFragment(ProfileFragment())
            updateNavbarState("profile")
        }
    }

    private fun updateNavbarState(activeTab: String) {
        val navbar = findViewById<View>(R.id.customNavbar)

        val iconHome = navbar.findViewById<ImageView>(R.id.iconHome)
        val textHome = navbar.findViewById<TextView>(R.id.textHome)

        val iconSave = navbar.findViewById<ImageView>(R.id.iconSave)
        val textSave = navbar.findViewById<TextView>(R.id.textSave)

        val iconProfile = navbar.findViewById<ImageView>(R.id.iconProfile)
        val textProfile = navbar.findViewById<TextView>(R.id.textProfile)

        textHome.visibility = View.GONE
        textSave.visibility = View.GONE
        textProfile.visibility = View.GONE

        iconHome.setImageResource(R.drawable.ic_home_solid)
        iconSave.setImageResource(R.drawable.ic_bookmark)
        iconProfile.setImageResource(R.drawable.ic_person)

        when (activeTab) {
            "home" -> {
                textHome.visibility = View.VISIBLE
                iconHome.setImageResource(R.drawable.ic_home)
            }
            "save" -> {
                textSave.visibility = View.VISIBLE
                iconSave.setImageResource(R.drawable.ic_bookmark_outline)
            }
            "profile" -> {
                textProfile.visibility = View.VISIBLE
                iconProfile.setImageResource(R.drawable.ic_person_outline)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    private fun checkUserPreference() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document.getBoolean("pref_set") != true) {
                startActivity(Intent(this, PreferenceActivity::class.java))
                finish()
            }
        }
    }
}