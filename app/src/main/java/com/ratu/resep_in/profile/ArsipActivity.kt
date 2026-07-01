package com.ratu.resep_in.profile

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R
import com.ratu.resep_in.adapter.PostAdapter
import com.ratu.resep_in.model.Recipe

class ArsipActivity : AppCompatActivity() {

    private lateinit var rvArchived: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arsip)

        rvArchived = findViewById(R.id.rvArchivedPosts)
        setupRecyclerView()
        loadArchivedPosts()

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {

        postAdapter = PostAdapter(
            onItemClick = {},
            onMoreClick = { recipe, view -> showArchiveMenu(recipe, view) }
        )
        rvArchived.apply {
            layoutManager = GridLayoutManager(this@ArsipActivity, 2)
            adapter = postAdapter
        }
    }

    private fun loadArchivedPosts() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("resep")
            .whereEqualTo("authorId", uid)
            .whereEqualTo("isArchived", true)
            .get()
            .addOnSuccessListener { snapshots ->
                val archivedPosts = snapshots.map { doc ->
                    doc.toObject(Recipe::class.java).apply { id = doc.id }
                }
                postAdapter.submitList(archivedPosts)
            }
    }

    private fun showArchiveMenu(recipe: Recipe, view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add("Kembalikan ke Profil")
        popup.setOnMenuItemClickListener {
            unarchiveRecipe(recipe.id)
            true
        }
        popup.show()
    }

    private fun unarchiveRecipe(recipeId: String) {
        db.collection("resep").document(recipeId)
            .update("isArchived", false)
            .addOnSuccessListener {
                Toast.makeText(this, "Resep dikembalikan ke profil", Toast.LENGTH_SHORT).show()
                loadArchivedPosts()
            }
    }
}