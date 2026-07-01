package com.ratu.resep_in.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.detail.DetailRecipeActivity
import com.ratu.resep_in.R
import com.ratu.resep_in.adapter.SavedRecipeAdapter
import com.ratu.resep_in.model.Recipe

class SavedFragment : Fragment(R.layout.fragment_saved) {

    private lateinit var rvSaved: RecyclerView
    private lateinit var savedRecipeAdapter: SavedRecipeAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val recipeList = mutableListOf<Recipe>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            rvSaved = view.findViewById(R.id.rvSavedCategories)
            rvSaved.layoutManager = GridLayoutManager(requireContext(), 2)

            savedRecipeAdapter = SavedRecipeAdapter(recipeList) { selectedRecipe ->
                val intent = Intent(requireContext(), DetailRecipeActivity::class.java)
                intent.putExtra("RECIPE_ID", selectedRecipe.id)
                startActivity(intent)
            }
            rvSaved.adapter = savedRecipeAdapter

            loadSavedRecipes()

        } catch (e: Exception) {
            Log.e("SAVED_FRAGMENT", "Error saat inisialisasi: ${e.message}")
        }
    }

    private fun loadSavedRecipes() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("favorites")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { favSnapshots ->
                if (!isAdded) return@addOnSuccessListener

                if (favSnapshots.isEmpty) {
                    savedRecipeAdapter.updateData(emptyList())
                    return@addOnSuccessListener
                }
                val recipeIds = favSnapshots.documents
                    .mapNotNull { it.getString("recipeId") }
                    .filter { it.isNotEmpty() }

                if (recipeIds.isEmpty()) return@addOnSuccessListener

                val chunks = recipeIds.chunked(10)
                var chunksProcessed = 0
                val tempList = mutableListOf<Recipe>()

                chunks.forEach { chunk ->
                    db.collection("resep")
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                        .get()
                        .addOnSuccessListener { recipeSnapshots ->
                            if (!isAdded) return@addOnSuccessListener

                            val fetched = recipeSnapshots.documents.mapNotNull { doc ->
                                doc.toObject(Recipe::class.java)?.apply { id = doc.id }
                            }
                            tempList.addAll(fetched)
                            chunksProcessed++

                            if (chunksProcessed == chunks.size) {
                                savedRecipeAdapter.updateData(tempList)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("SAVED_FRAGMENT", "Gagal load chunk: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SAVED_FRAGMENT", "Gagal load favorites: ${e.message}")
            }
    }


}