package com.ratu.resep_in

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)

        btnLogin.setOnClickListener {

            if (email.text.toString().isNotEmpty() &&
                password.text.toString().isNotEmpty()
            ) {

                startActivity(
                    Intent(this, MainActivity::class.java)
                )

                finish()

            } else {
                Toast.makeText(
                    this,
                    "Isi email dan password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(
                this,
                "Login Google berhasil",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(this, MainActivity::class.java)
            )

            finish()
        }
    }
}