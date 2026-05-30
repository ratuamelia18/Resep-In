package com.ratu.resep_in.ui.theme

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ratu.resep_in.R
import com.ratu.resep_in.adapter.RecipeAdapter
import com.ratu.resep_in.data.Recipe

class SearchActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: RecipeAdapter

    private lateinit var recipeList: ArrayList<Recipe>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recyclerView = findViewById(R.id.recyclerViewSearch)
        etSearch = findViewById(R.id.etSearch)

        recipeList = arrayListOf(
            Recipe(
                "Nasi Goreng",
                "Nasi goreng spesial rumahan",
                R.drawable.nasigoreng
            ),
            Recipe(
                "Ayam Taliwang",
                "Dengan ayam kampung asli dan bumbu yg lezat",
                R.drawable.ayamtaliwang
            ),
            Recipe(
                "Rendang",
                "Daging dengan bumbu rempah asli",
                R.drawable.rendang
            )
        )

        adapter = RecipeAdapter(recipeList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

                val filteredList = recipeList.filter {
                    it.title.contains(s.toString(), true)
                }

                adapter.updateData(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}