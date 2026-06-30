package com.ratu.resep_in.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R
import com.ratu.resep_in.model.Recipe

class SavedRecipeAdapter(
    private var recipeList: MutableList<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<SavedRecipeAdapter.SavedViewHolder>() {

    class SavedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPost: ImageView = itemView.findViewById(R.id.imgPost)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_category, parent, false)
        return SavedViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {
        val recipe = recipeList[position]
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        holder.tvTitle.text = recipe.title
        holder.tvCategoryName.text = recipe.category


        Glide.with(holder.itemView.context).load(recipe.imgurl).into(holder.imgPost)
        holder.itemView.setOnClickListener { onItemClick(recipe) }

    }

    override fun getItemCount(): Int = recipeList.size

    fun updateData(newList: List<Recipe>) {
        this.recipeList.clear()
        this.recipeList.addAll(newList)
        notifyDataSetChanged()
    }
}