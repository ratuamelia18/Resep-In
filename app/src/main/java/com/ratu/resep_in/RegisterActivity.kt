package com.ratu.resep_in.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etNama = findViewById<TextInputEditText>(R.id.etNama)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnBuatAkun)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)

        btnRegister.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lengkapi semua kolom!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: ""

                    val userMap = hashMapOf(
                        "uid" to userId,
                        "username" to nama,
                        "email" to email,
                        "pref_set" to false
                    )

                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Berhasil Daftar!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, PreferenceActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(this, gso)
            startActivityForResult(client.signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { taskResult ->
                    if (taskResult.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: ""
                        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                val userMap = hashMapOf(
                                    "uid" to userId,
                                    "email" to account.email,
                                    "username" to (account.displayName ?: "User Google"),
                                    "pref_set" to false
                                )
                                db.collection("users").document(userId).set(userMap)
                            }
                            Toast.makeText(this, "Login Google Berhasil!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, PreferenceActivity::class.java))
                            finish()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Login Google Gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}