package com.ratu.resep_in.ui.theme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ratu.resep_in.MainActivity
import com.ratu.resep_in.R
import com.ratu.resep_in.adapter.CategoryAdapter
import com.ratu.resep_in.model.Category

class PreferenceActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var categoryList = mutableListOf<Category>()

    private var selectedSkill: String = ""
    private var countSelectedCategories = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        val rv = findViewById<RecyclerView>(R.id.rvKategori)
        val btnLanjut = findViewById<Button>(R.id.btnLanjut)

        // 1. Setup Logic Skill Card (Menghindari error getChildAt)
        val cardBaru = findViewById<MaterialCardView>(R.id.cardBaru)
        val cardBisa = findViewById<MaterialCardView>(R.id.cardBisa)
        val cardJago = findViewById<MaterialCardView>(R.id.cardJago)
        val cards = listOf(cardBaru, cardBisa, cardJago)

        cards.forEach { card ->
            card.setOnClickListener {
                cards.forEach { it.setCardBackgroundColor(Color.WHITE) }
                card.setCardBackgroundColor(Color.parseColor("#B74B4B"))

                // Ambil teks dari TextView dengan ID tvTitle di dalam masing-masing card
                val titleView = card.findViewById<TextView>(R.id.tvTitle)
                selectedSkill = titleView.text.toString()

                checkValidation(btnLanjut)
            }
        }

        // 2. Setup RecyclerView
        rv.layoutManager = GridLayoutManager(this, 2)
        db.collection("categories").get().addOnSuccessListener { snapshot ->
            categoryList = snapshot.toObjects(Category::class.java).toMutableList()
            val adapter = CategoryAdapter(categoryList) { count ->
                countSelectedCategories = count
                checkValidation(btnLanjut)
            }
            rv.adapter = adapter
        }

        // 3. Tombol Lanjut
        btnLanjut.setOnClickListener {
            val selected = categoryList.filter { it.isSelected }.map { it.name }
            saveToFirebase(selected, selectedSkill)
        }
    }

    private fun checkValidation(btn: Button) {
        val isReady = (countSelectedCategories >= 1 && selectedSkill.isNotEmpty())
        btn.isEnabled = isReady
        btn.setBackgroundColor(if (isReady) Color.parseColor("#B74B4B") else Color.LTGRAY)
    }

    private fun saveToFirebase(selected: List<String>, skill: String) {
        val uid = auth.currentUser?.uid ?: return
        val data = mapOf(
            "kategori_disukai" to selected,
            "skill" to skill,
            "pref_set" to true
        )

        db.collection("users").document(uid).set(data, SetOptions.merge())
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal simpan data", Toast.LENGTH_SHORT).show()
            }
    }
}