package com.ratu.resep_in.ui.theme

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R
import com.ratu.resep_in.DetailRecipeActivity
import com.ratu.resep_in.adapter.SuggestionAdapter
import com.ratu.resep_in.adapter.RecommendationAdapter
import com.ratu.resep_in.adapter.RecipeAdapter
import com.ratu.resep_in.model.Recipe

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearchInput: EditText
    private lateinit var btnBackSearch: ImageView
    private lateinit var layoutSuggestions: LinearLayout
    private lateinit var layoutSearchResults: LinearLayout
    private lateinit var tvNoResult: TextView

    private lateinit var btnSort: LinearLayout
    private lateinit var btnFilter: LinearLayout

    private lateinit var rvSuggestions: RecyclerView
    private lateinit var suggestionAdapter: SuggestionAdapter
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var searchResultAdapter: RecipeAdapter

    private val db = FirebaseFirestore.getInstance()
    private val allRecipesMaster = mutableListOf<Recipe>()
    private val currentSuggestionsList = mutableListOf<String>()
    private val currentFilteredResult = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        etSearchInput = findViewById(R.id.etSearchInput)
        btnBackSearch = findViewById(R.id.btnBackSearch)
        layoutSuggestions = findViewById(R.id.layoutSuggestions)
        layoutSearchResults = findViewById(R.id.layoutSearchResults)
        rvSuggestions = findViewById(R.id.rvSuggestions)
        rvRecommendations = findViewById(R.id.rvRecommendations)
        rvSearchResults = findViewById(R.id.rvSearchResults)
        tvNoResult = findViewById(R.id.tvNoResult)

        btnSort = findViewById(R.id.btnSort)
        btnFilter = findViewById(R.id.btnFilter)

        rvSuggestions.layoutManager = LinearLayoutManager(this)
        suggestionAdapter = SuggestionAdapter(currentSuggestionsList) { clickedText ->
            etSearchInput.setText(clickedText)
            etSearchInput.setSelection(clickedText.length)
            performSearch(clickedText)
        }
        rvSuggestions.adapter = suggestionAdapter

        rvRecommendations.layoutManager = GridLayoutManager(this, 2)
        rvSearchResults.layoutManager = LinearLayoutManager(this)

        fetchAllRecipesFromCloud()

        btnBackSearch.setOnClickListener { finish() }

        btnSort.setOnClickListener { view -> showSortingMenu(view) }
        btnFilter.setOnClickListener { view -> showFilterMenu(view) }

        etSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    currentSuggestionsList.clear()
                    suggestionAdapter.updateData(currentSuggestionsList, "")
                    layoutSuggestions.visibility = View.VISIBLE
                    layoutSearchResults.visibility = View.GONE
                } else {
                    val filteredSuggestions = allRecipesMaster
                        .filter { it.title.lowercase().contains(query.lowercase()) }
                        .map { it.title }.distinct().take(5)
                    layoutSuggestions.visibility = View.VISIBLE
                    layoutSearchResults.visibility = View.GONE
                    suggestionAdapter.updateData(filteredSuggestions, query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearchInput.text.toString().trim()
                if (query.isNotEmpty()) performSearch(query)
                true
            } else false
        }
    }

    private fun fetchAllRecipesFromCloud() {
        db.collection("recipes").get().addOnSuccessListener { snapshots ->
            allRecipesMaster.clear()
            for (doc in snapshots.documents) {
                val recipe = doc.toObject(Recipe::class.java)
                if (recipe != null) allRecipesMaster.add(recipe.copy(id = doc.id))
            }
            setupRecommendationGrid()
        }
    }

    private fun setupRecommendationGrid() {
        recommendationAdapter = RecommendationAdapter(allRecipesMaster) { clickedRecipe ->
            val intent = Intent(this, DetailRecipeActivity::class.java).apply {
                putExtra("RECIPE_DATA", clickedRecipe)
            }
            startActivity(intent)
        }
        rvRecommendations.adapter = recommendationAdapter
    }

    private fun performSearch(query: String) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etSearchInput.windowToken, 0)

        layoutSuggestions.visibility = View.GONE
        layoutSearchResults.visibility = View.VISIBLE

        currentFilteredResult.clear()
        currentFilteredResult.addAll(allRecipesMaster.filter {
            it.title.lowercase().contains(query.lowercase())
        })

        displaySearchResults()
    }

    private fun displaySearchResults() {
        if (currentFilteredResult.isEmpty()) {
            rvSearchResults.visibility = View.GONE
            tvNoResult.visibility = View.VISIBLE
            tvNoResult.text = "Resep tidak ditemukan"
        } else {
            tvNoResult.visibility = View.GONE
            rvSearchResults.visibility = View.VISIBLE

            searchResultAdapter = RecipeAdapter(currentFilteredResult) { clickedRecipe ->
                val intent = Intent(this, DetailRecipeActivity::class.java).apply {
                    putExtra("RECIPE_DATA", clickedRecipe)
                }
                startActivity(intent)
            }
            rvSearchResults.adapter = searchResultAdapter
        }
    }

    private fun showSortingMenu(anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menu.add(0, 1, 0, "Terpopuler")
        popup.menu.add(0, 2, 1, "Terbaru")
        popup.menu.add(0, 3, 2, "Rating Tertinggi")
        popup.menu.add(0, 4, 3, "Rating Terendah")
        popup.menu.add(0, 5, 4, "Waktu Masak Tercepat")
        popup.menu.add(0, 6, 5, "Waktu Masak Terlama")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> currentFilteredResult.sortByDescending { it.rating }
                2 -> currentFilteredResult.sortByDescending { it.timestamp }
                3 -> currentFilteredResult.sortByDescending { it.rating }
                4 -> currentFilteredResult.sortBy { it.rating }
                5 -> currentFilteredResult.sortBy { it.duration }
                6 -> currentFilteredResult.sortByDescending { it.duration }
            }
            displaySearchResults()
            true
        }
        popup.show()
    }

    private fun showFilterMenu(anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menu.add(0, 1, 0, "Waktu Kilat (< 15 menit)")
        popup.menu.add(0, 2, 1, "Rating 4 ke atas")

        popup.setOnMenuItemClickListener { item ->
            val filtered = when (item.itemId) {
                1 -> allRecipesMaster.filter { it.duration < 15 }
                2 -> allRecipesMaster.filter { it.rating >= 4.0 }
                else -> allRecipesMaster
            }
            currentFilteredResult.clear()
            currentFilteredResult.addAll(filtered)
            displaySearchResults()
            true
        }
        popup.show()
    }
}