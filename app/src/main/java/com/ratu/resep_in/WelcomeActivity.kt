package com.ratu.resep_in.ui.theme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R
import com.ratu.resep_in.LoginActivity
import com.ratu.resep_in.MainActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()


        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkUserPreferenceAndNavigate(currentUser.uid)
            return
        }

        setContentView(R.layout.activity_welcome)

        findViewById<Button>(R.id.btnMulai).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        val tvMasuk = findViewById<TextView>(R.id.tvMasukLink)
        val text = "Sudah memiliki akun? Masuk"
        val spannable = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
                finish()
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#A94442")
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(clickableSpan, 21, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvMasuk.text = spannable
        tvMasuk.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun checkUserPreferenceAndNavigate(uid: String) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val isPrefSet = document.getBoolean("pref_set") ?: false
                if (isPrefSet) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, PreferenceActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, PreferenceActivity::class.java))
                finish()
            }
    }
}