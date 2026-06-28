package com.ratu.resep_in.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.DetailRecipeActivity
import com.ratu.resep_in.EditRecipeActivity
import com.ratu.resep_in.DetailCategoryActivity
import com.ratu.resep_in.R
import com.ratu.resep_in.adapter.HomeCategory
import com.ratu.resep_in.adapter.HomeCategoryAdapter
import com.ratu.resep_in.adapter.RecommendationAdapter
import com.ratu.resep_in.adapter.RecipeAdapter
import com.ratu.resep_in.model.Recipe
import com.ratu.resep_in.ui.theme.SearchActivity

class HomeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvGreeting: TextView
    private lateinit var rvRecommendation: RecyclerView
    private lateinit var rvHomeCategory: RecyclerView
    private lateinit var rvLatest: RecyclerView
    private lateinit var searchBarHome: View

    private lateinit var tvSeeAllRecomdasi: TextView


    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var categoryAdapter: HomeCategoryAdapter
    private lateinit var latestAdapter: RecipeAdapter

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
        tvSeeAllRecomdasi = view.findViewById(R.id.tvSeeAllRekomendasi)

        rvRecommendation.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvHomeCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvLatest.layoutManager = LinearLayoutManager(requireContext())

        recommendationAdapter = RecommendationAdapter(mutableListOf()) { navigateToDetail(it) }
        rvRecommendation.adapter = recommendationAdapter

        tvSeeAllRecomdasi.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }


        latestAdapter = RecipeAdapter(
            recipeList = mutableListOf(),
            onItemClick = { navigateToDetail(it) },
            onEditClick = { recipe ->
                val intent = Intent(requireContext(), EditRecipeActivity::class.java)
                intent.putExtra("RECIPE_DATA", recipe)
                startActivity(intent)
            },
            onDeleteClick = { recipe ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Resep")
                    .setMessage("Yakin mau hapus resep \"${recipe.title}\"?")
                    .setPositiveButton("Hapus") { _, _ ->
                        db.collection("resep").document(recipe.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Resep berhasil dihapus", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Gagal menghapus resep", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )
        rvLatest.adapter = latestAdapter

        setupHomeCategories()
        loadUserProfile()
        loadDataFromCloud()

        searchBarHome.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
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
            val intent = Intent(requireContext(), DetailCategoryActivity::class.java)
            intent.putExtra("EXTRA_KATEGORI", selectedCategory.name)
            startActivity(intent)
        }
        rvHomeCategory.adapter = categoryAdapter
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            tvGreeting.text = "Halo, Tamu!"
            return
        }

        db.collection("users").document(currentUser.uid)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    val emailName = currentUser.email?.substringBefore("@") ?: "User"
                    tvGreeting.text = "Halo, $emailName!"
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val name = document.getString("username")
                        ?: document.getString("nama")
                        ?: document.getString("name")
                        ?: currentUser.displayName
                        ?: currentUser.email?.substringBefore("@")
                        ?: "User"

                    tvGreeting.text = "Halo, $name!"
                } else {
                    tvGreeting.text = "Halo, User!"
                }
            }
    }

    private fun loadDataFromCloud() {
        db.collection("resep")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val newList = mutableListOf<Recipe>()
                if (value != null) {
                    for (doc in value.documents) {
                        val recipe = doc.toObject(Recipe::class.java)
                        if (recipe != null) {
                            val ts = doc.getTimestamp("timestamp")?.seconds ?: 0L
                            newList.add(recipe.copy(id = doc.id, timestamp = ts))
                        }
                    }
                }

                recommendationAdapter.updateData(newList)
                latestAdapter.updateData(newList)
            }
    }

    private fun navigateToDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), DetailRecipeActivity::class.java)
        intent.putExtra("RECIPE_DATA", recipe)
        startActivity(intent)
    }
}