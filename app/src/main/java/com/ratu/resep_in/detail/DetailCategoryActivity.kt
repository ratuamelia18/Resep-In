package com.ratu.resep_in.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.detail.DetailRecipeActivity
import com.ratu.resep_in.R
import com.ratu.resep_in.adapter.RecipeAdapter
import com.ratu.resep_in.model.Recipe

class DetailCategoryActivity : AppCompatActivity() {

    private lateinit var rvResult: RecyclerView
    private lateinit var tvEmpty: TextView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_category)

        val ivBack = findViewById<View>(R.id.btnBack)
        ivBack.setOnClickListener {
            finish()
        }

        val namaKategori = intent.getStringExtra("EXTRA_KATEGORI") ?: ""
        title = namaKategori


        rvResult = findViewById(R.id.rvResult)
        tvEmpty = findViewById(R.id.tvEmpty)

        rvResult.layoutManager = LinearLayoutManager(this)

        adapter = RecipeAdapter(
            recipeList = mutableListOf(),
            onItemClick = { recipe ->
                val intent = Intent(this, DetailRecipeActivity::class.java)
                intent.putExtra("RECIPE_DATA", recipe)
                startActivity(intent)
            },
            onEditClick = { },
            onDeleteClick = { },
            onArchiveClick = { },
            onSaveClick = { }
        )
        rvResult.adapter = adapter

        loadResepByKategori(namaKategori)
    }

    private fun loadResepByKategori(kategori: String) {
        db.collection("resep")
            .whereEqualTo("kategori", kategori)
            .get()
            .addOnSuccessListener { snapshots ->
                val listResep = snapshots.toObjects(Recipe::class.java)

                findViewById<TextView>(R.id.tvCategoryTitle).text = kategori
                findViewById<TextView>(R.id.tvCountResep).text = "${listResep.size} resep ditemukan"

                if (listResep.isEmpty()) {
                    findViewById<RecyclerView>(R.id.rvResult).visibility = View.GONE
                    findViewById<TextView>(R.id.tvEmpty).visibility = View.VISIBLE
                } else {
                    findViewById<RecyclerView>(R.id.rvResult).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.tvEmpty).visibility = View.GONE
                    adapter.updateData(listResep)
                }
            }
    }
}