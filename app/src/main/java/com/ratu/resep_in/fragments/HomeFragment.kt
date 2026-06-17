package com.ratu.resep_in.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R
import com.ratu.resep_in.DetailRecipeActivity
import com.ratu.resep_in.ui.theme.SearchActivity
import com.ratu.resep_in.adapter.HomeCategory
import com.ratu.resep_in.adapter.HomeCategoryAdapter
import com.ratu.resep_in.adapter.RecommendationAdapter
import com.ratu.resep_in.adapter.RecipeAdapter
import com.ratu.resep_in.model.Recipe

class HomeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvGreeting: TextView
    private lateinit var rvRecommendation: RecyclerView
    private lateinit var rvHomeCategory: RecyclerView
    private lateinit var rvLatest: RecyclerView
    private lateinit var searchBarHome: View

    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var categoryAdapter: HomeCategoryAdapter
    private lateinit var latestAdapter: RecipeAdapter

    private val recipeList = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        rvRecommendation = view.findViewById(R.id.rvRecommendation)
        rvHomeCategory = view.findViewById(R.id.rvHomeCategory)
        rvLatest = view.findViewById(R.id.rvLatest)

        searchBarHome = view.findViewById(R.id.etSearch)

        rvRecommendation.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvHomeCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvLatest.layoutManager = LinearLayoutManager(requireContext())

        setupHomeCategories()

        loadUserProfile()

        loadDataFromCloud()

        searchBarHome.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun setupHomeCategories() {
        val categories = listOf(
            HomeCategory("Daging", R.drawable.ic_daging),
            HomeCategory("Ayam & Bebek", R.drawable.ic_ayam),
            HomeCategory("Seafood", R.drawable.ic_seafood),
            HomeCategory("Tahu & Tempe", R.drawable.ic_tahu),
            HomeCategory("Telur", R.drawable.ic_telur),
            HomeCategory("Buah", R.drawable.ic_buah),
            HomeCategory("Sayur", R.drawable.ic_sayur),
            HomeCategory("Nasi", R.drawable.ic_nasi),
            HomeCategory("Mie", R.drawable.ic_mie)
        )

        categoryAdapter = HomeCategoryAdapter(categories) { selectedCategory ->
            Toast.makeText(requireContext(), "Kategori ${selectedCategory.name} diklik", Toast.LENGTH_SHORT).show()
        }
        rvHomeCategory.adapter = categoryAdapter
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            tvGreeting.text = "Halo, Tamu!"
            return
        }

        val nameFromGoogle = currentUser.displayName
        if (!nameFromGoogle.isNullOrEmpty()) {
            tvGreeting.text = "Halo, $nameFromGoogle!"
            return
        }

        val uid = currentUser.uid
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("username")
                        ?: document.getString("name")
                        ?: "User"
                    tvGreeting.text = "Halo, $name!"
                } else {
                    tvGreeting.text = "Halo, User!"
                }
            }
            .addOnFailureListener {
                tvGreeting.text = "Halo, User!"
            }
    }

    private fun loadDataFromCloud() {
        db.collection("recipes")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Gagal memuat data Cloud", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                recipeList.clear()
                if (value != null) {
                    for (doc in value.documents) {
                        val recipe = doc.toObject(Recipe::class.java)
                        if (recipe != null) {
                            recipeList.add(recipe.copy(id = doc.id))
                        }
                    }
                }

                recommendationAdapter = RecommendationAdapter(recipeList) { clickedRecipe ->
                    navigateToDetail(clickedRecipe)
                }
                rvRecommendation.adapter = recommendationAdapter

                latestAdapter = RecipeAdapter(recipeList) { clickedRecipe ->
                    navigateToDetail(clickedRecipe)
                }
                rvLatest.adapter = latestAdapter
            }
    }

    private fun navigateToDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), DetailRecipeActivity::class.java)
        intent.putExtra("RECIPE_DATA", recipe)
        startActivity(intent)
    }
}