package com.ratu.resep_in.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ratu.resep_in.R
import com.ratu.resep_in.model.Recipe

class RecipeAdapter(
    private var recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgRecipe: ImageView = itemView.findViewById(R.id.imgRecipeLatest)
        val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitleLatest)
        val tvCategory: TextView = itemView.findViewById(R.id.tvRecipeCategoryLatest)
        val tvRating: TextView = itemView.findViewById(R.id.tvRecipeRatingLatest)
        val tvDuration: TextView = itemView.findViewById(R.id.tvRecipeDurationLatest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = recipeList[position]
        holder.tvTitle.text = item.title
        holder.tvCategory.text = item.category
        holder.tvRating.text = item.rating.toString()
        holder.tvDuration.text = "${item.duration} menit"

        Glide.with(holder.imgRecipe.context)
            .load(item.imgurl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.imgRecipe)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = recipeList.size

    fun updateData(newList: List<Recipe>) {
        recipeList = newList
        notifyDataSetChanged()
    }
}