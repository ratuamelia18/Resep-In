package com.ratu.resep_in.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

        rvSaved = view.findViewById(R.id.rvSavedCategories)
        rvSaved.layoutManager = GridLayoutManager(requireContext(), 2)

        savedRecipeAdapter = SavedRecipeAdapter(recipeList) { selectedRecipe ->
        }
        rvSaved.adapter = savedRecipeAdapter

        loadSavedRecipes()
    }

    private fun loadSavedRecipes() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("favorites")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { favSnapshots ->
                if (favSnapshots.isEmpty) {
                    android.util.Log.d("SAVED", "Belum ada resep yang disimpan")
                    return@addOnSuccessListener
                }


                val recipeIds = favSnapshots.documents.mapNotNull { it.getString("recipeId") }

                if (recipeIds.isEmpty()) return@addOnSuccessListener

                recipeIds.chunked(10).forEach { chunk ->
                    db.collection("resep")
                        .whereIn("__name__", chunk)
                        .get()
                        .addOnSuccessListener { recipeSnapshots ->
                            val fetched = recipeSnapshots.documents.mapNotNull { doc ->
                                doc.toObject(Recipe::class.java)?.copy(id = doc.id)
                            }
                            recipeList.addAll(fetched)
                            savedRecipeAdapter.updateData(recipeList.toList())
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("SAVED", "Gagal fetch resep: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("SAVED", "Gagal fetch favorites: ${e.message}")
            }
    }
}