package com.ratu.resep_in

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
import com.ratu.resep_in.ui.theme.PreferenceActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkUserPreference(currentUser.uid)
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnMasuk = findViewById<Button>(R.id.btnMasuk)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)

        btnMasuk.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi email dan password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    checkUserPreference(auth.currentUser?.uid)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
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
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign In Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                saveGoogleUserToFirestore()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firebase Auth Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveGoogleUserToFirestore() {
        val currentUser = auth.currentUser ?: return
        val userRef = db.collection("users").document(currentUser.uid)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val userData = hashMapOf(
                    "uid" to currentUser.uid,
                    "email" to currentUser.email,
                    "username" to (currentUser.displayName ?: "User Google"),
                    "pref_set" to false
                )
                userRef.set(userData).addOnSuccessListener {
                    checkUserPreference(currentUser.uid)
                }
            } else {
                checkUserPreference(currentUser.uid)
            }
        }.addOnFailureListener {
            checkUserPreference(currentUser.uid)
        }
    }

    private fun checkUserPreference(uid: String?) {
        if (uid == null) return

        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            val isPrefSet = document.getBoolean("pref_set") ?: false

            if (isPrefSet) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, PreferenceActivity::class.java))
            }
            finish()
        }.addOnFailureListener {
            startActivity(Intent(this, PreferenceActivity::class.java))
            finish()
        }
    }
}