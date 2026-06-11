package com.ratu.resep_in.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ratu.resep_in.R
import com.ratu.resep_in.adapter.RecipeAdapter
import com.ratu.resep_in.model.DatabaseHelper

class HomeFragment : Fragment() {

    private lateinit var rvRecipes: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        rvRecipes = view.findViewById(R.id.rvRecipes)
        rvRecipes.layoutManager = LinearLayoutManager(requireContext())

        val dbHelper = DatabaseHelper(requireContext())
        val listResepDariDatabase = dbHelper.getAllRecipes()

        recipeAdapter = RecipeAdapter(listResepDariDatabase)
        rvRecipes.adapter = recipeAdapter

        return view
    }
}