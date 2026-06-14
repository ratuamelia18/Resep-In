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
import com.ratu.resep_in.R
import com.ratu.resep_in.LoginActivity
import com.ratu.resep_in.ui.theme.RegisterActivity


class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<Button>(R.id.btnMulai).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val tvMasuk = findViewById<TextView>(R.id.tvMasukLink)
        val text = "Sudah memiliki akun? Masuk"
        val spannable = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#A94442") // Warna merah
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(clickableSpan, 21, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvMasuk.text = spannable
        tvMasuk.movementMethod = LinkMovementMethod.getInstance()
    }
}