package com.ratu.resep_in

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {

    private lateinit var rvRecipes: RecyclerView
    private lateinit var rvCategories: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        rvRecipes = findViewById(R.id.rvRecipes)
        rvCategories = findViewById(R.id.rvCategories)

        rvRecipes.layoutManager = LinearLayoutManager(this)

        rvCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }
}