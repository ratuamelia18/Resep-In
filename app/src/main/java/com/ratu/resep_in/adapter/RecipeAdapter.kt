package com.ratu.resep_in.adapter



import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup

import android.widget.ImageView

import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.ratu.resep_in.R

import com.ratu.resep_in.data.Recipe



class RecipeAdapter(

    private var recipeList: List<Recipe>

) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {



    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imgRecipe: ImageView = itemView.findViewById(R.id.imgRecipe)

        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {

        val view = LayoutInflater.from(parent.context)

            .inflate(R.layout.item_recipe, parent, false)



        return RecipeViewHolder(view)

    }



    override fun getItemCount(): Int {

        return recipeList.size

    }



    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {

        val recipe = recipeList[position]



        holder.tvTitle.text = recipe.title

        holder.tvDescription.text = recipe.description

        holder.imgRecipe.setImageResource(recipe.imageResId)

    }



    fun updateData(newList: List<Recipe>) {

        recipeList = newList

        notifyDataSetChanged()

    }

}

