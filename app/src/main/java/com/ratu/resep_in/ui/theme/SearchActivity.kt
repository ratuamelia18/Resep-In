package com.ratu.resep_in.ui.theme

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R
import com.ratu.resep_in.DetailRecipeActivity
import com.ratu.resep_in.adapter.*
import com.ratu.resep_in.model.Recipe
import com.ratu.resep_in.model.User
import com.google.firebase.auth.FirebaseAuth

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearchInput: EditText
    private lateinit var btnBackSearch: ImageView
    private lateinit var layoutSuggestions: LinearLayout
    private lateinit var layoutSearchResults: LinearLayout
    private lateinit var tvNoResult: TextView
    private lateinit var btnSort: LinearLayout
    private lateinit var btnFilter: LinearLayout
    private lateinit var tabSearch: TabLayout

    private lateinit var filterGroup: LinearLayout

    private lateinit var rvSuggestions: RecyclerView
    private lateinit var suggestionAdapter: SuggestionAdapter
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var searchResultAdapter: RecipeAdapter
    private lateinit var userAdapter: UserAdapter

    private val db = FirebaseFirestore.getInstance()
    private val allRecipesMaster = mutableListOf<Recipe>()
    private val currentSuggestionsList = mutableListOf<String>()
    private val currentFilteredResult = mutableListOf<Recipe>()

    private var isSearchingUser = false

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
        tabSearch = findViewById(R.id.tabSearch)

        searchResultAdapter = RecipeAdapter(
            recipeList = currentFilteredResult,
            onItemClick = { clickedRecipe ->
                val intent = Intent(this, DetailRecipeActivity::class.java).apply {
                    putExtra("RECIPE_DATA", clickedRecipe)
                }
                startActivity(intent)
            },
            onEditClick = null,
            onDeleteClick = null
        )

        userAdapter = UserAdapter(
            userList = emptyList(),
            onItemClick = { user ->
                val intent = Intent(this, OtherProfileActivity::class.java)
                intent.putExtra("USER_ID", user.id)
                startActivity(intent)
            },
            onFollowClick = { user ->
                toggleFollow(user.id)
            }
        )

        rvSearchResults.layoutManager = LinearLayoutManager(this)
        rvSearchResults.adapter = searchResultAdapter

        rvSuggestions.layoutManager = LinearLayoutManager(this)
        suggestionAdapter = SuggestionAdapter(currentSuggestionsList) { clickedText ->
            etSearchInput.setText(clickedText)
            etSearchInput.setSelection(clickedText.length)
            performSearch(clickedText)
        }
        rvSuggestions.adapter = suggestionAdapter

        rvRecommendations.layoutManager = GridLayoutManager(this, 2)
        fetchAllRecipesFromCloud()

        tabSearch.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isSearchingUser = (tab?.position == 1)
                rvSearchResults.adapter = if (isSearchingUser) userAdapter else searchResultAdapter
                val query = etSearchInput.text.toString().trim()
                if (query.isNotEmpty()) performSearch(query)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnBackSearch.setOnClickListener { finish() }
        btnSort.setOnClickListener { showSortingMenu(it) }
        btnFilter.setOnClickListener { showFilterMenu(it) }

        etSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    layoutSuggestions.visibility = View.VISIBLE
                    layoutSearchResults.visibility = View.GONE
                } else {
                    layoutSuggestions.visibility = View.VISIBLE
                    layoutSearchResults.visibility = View.GONE
                    if (!isSearchingUser) {
                        val filteredSuggestions = allRecipesMaster
                            .filter { it.title.contains(query, ignoreCase = true) }
                            .map { it.title }.distinct().take(5)
                        suggestionAdapter.updateData(filteredSuggestions, query)
                    }
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


    private fun performSearch(query: String) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etSearchInput.windowToken, 0)

        layoutSuggestions.visibility = View.GONE
        layoutSearchResults.visibility = View.VISIBLE

        if (isSearchingUser) {
            db.collection("users")
                .orderBy("username")
                .startAt(query.lowercase())
                .endAt(query.lowercase() + "\uf8ff")
                .get()
                .addOnSuccessListener { snapshots ->
                    val userList = snapshots.toObjects(User::class.java)
                    userAdapter.updateData(userList)
                    rvSearchResults.visibility = if (userList.isEmpty()) View.GONE else View.VISIBLE
                    tvNoResult.visibility = if (userList.isEmpty()) View.VISIBLE else View.GONE
                    tvNoResult.text = "Pengguna tidak ditemukan"
                }
        } else {
            currentFilteredResult.clear()
            currentFilteredResult.addAll(allRecipesMaster.filter {
                it.title.contains(query, ignoreCase = true)
            })
            displaySearchResults()
        }
    }

    private fun fetchAllRecipesFromCloud() {
        db.collection("resep").get().addOnSuccessListener { snapshots ->
            allRecipesMaster.clear()
            for (doc in snapshots.documents) {
                val recipe = doc.toObject(Recipe::class.java)
                if (recipe != null) {
                    val ts = doc.getTimestamp("timestamp")?.seconds ?: 0L
                    allRecipesMaster.add(recipe.copy(id = doc.id, timestamp = ts))
                }
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

    private fun displaySearchResults() {
        if (currentFilteredResult.isEmpty()) {
            rvSearchResults.visibility = View.GONE
            tvNoResult.visibility = View.VISIBLE
            tvNoResult.text = "Resep tidak ditemukan"
        } else {
            tvNoResult.visibility = View.GONE
            rvSearchResults.visibility = View.VISIBLE
            searchResultAdapter.updateData(currentFilteredResult)
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
                1 -> currentFilteredResult.sortByDescending { it.averageRating }
                2 -> currentFilteredResult.sortByDescending { it.timestamp }
                3 -> currentFilteredResult.sortByDescending { it.averageRating }
                4 -> currentFilteredResult.sortBy { it.averageRating }
                5 -> currentFilteredResult.sortBy { it.duration }
                6 -> currentFilteredResult.sortByDescending { it.duration }
            }
            searchResultAdapter.updateData(currentFilteredResult)
            true
        }
        popup.show()
    }

    private fun showFilterMenu(anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menu.add(0, 1, 0, "Waktu Kilat (< 15 menit)")
        popup.menu.add(0, 2, 1, "averageRating 4 ke atas")

        popup.setOnMenuItemClickListener { item ->
            val filtered = when (item.itemId) {
                1 -> allRecipesMaster.filter { it.duration < 15 }
                2 -> allRecipesMaster.filter { it.averageRating >= 4.0 }
                else -> allRecipesMaster
            }
            currentFilteredResult.clear()
            currentFilteredResult.addAll(filtered)
            displaySearchResults()
            true
        }
        popup.show()
    }

    private fun toggleFollow(targetUserId: String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val followId = "${currentUid}_${targetUserId}"
        val followRef = db.collection("follows").document(followId)

        val meRef = db.collection("users").document(currentUid)
        val targetRef = db.collection("users").document(targetUserId)

        followRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                followRef.delete()
                meRef.update("followingCount", com.google.firebase.firestore.FieldValue.increment(-1))
                targetRef.update("followerCount", com.google.firebase.firestore.FieldValue.increment(-1))
                Toast.makeText(this, "Berhenti mengikuti", Toast.LENGTH_SHORT).show()
            } else {
                val followData = mapOf("followerId" to currentUid, "followedId" to targetUserId)
                followRef.set(followData)
                meRef.update("followingCount", com.google.firebase.firestore.FieldValue.increment(1))
                targetRef.update("followerCount", com.google.firebase.firestore.FieldValue.increment(1))
                Toast.makeText(this, "Mengikuti!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}