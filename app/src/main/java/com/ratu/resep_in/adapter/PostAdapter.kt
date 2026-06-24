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

class PostAdapter(
    private val onItemClick: (Recipe) -> Unit,
    private val onMoreClick: (Recipe, View) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var recipeList = listOf<Recipe>()

    fun submitList(newList: List<Recipe>) {
        recipeList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.bind(recipe, onItemClick, onMoreClick)
    }

    override fun getItemCount() = recipeList.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val category: TextView = itemView.findViewById(R.id.tvCategory)
        private val image: ImageView = itemView.findViewById(R.id.imgPost)
        private val btnMore: ImageView = itemView.findViewById(R.id.btnMore)

        fun bind(
            recipe: Recipe,
            onItemClick: (Recipe) -> Unit,
            onMoreClick: (Recipe, View) -> Unit
        ) {
            title.text = recipe.title
            category.text = recipe.category
            Glide.with(itemView.context).load(recipe.imgurl).into(image)

            itemView.setOnClickListener { onItemClick(recipe) }

            btnMore.setOnClickListener { view ->
                onMoreClick(recipe, view)
            }
        }
    }
}