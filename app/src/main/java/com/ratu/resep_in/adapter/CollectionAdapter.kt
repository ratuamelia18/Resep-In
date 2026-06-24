package com.ratu.resep_in.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ratu.resep_in.R
import com.ratu.resep_in.model.RecipeCollection

class CollectionAdapter(
    private var collectionList: List<RecipeCollection>,
    private val onItemClick: (RecipeCollection) -> Unit
) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCategory: ImageView = itemView.findViewById(R.id.imgCategory)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_category, parent, false)
        return CollectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val collection = collectionList[position]

        holder.tvCategoryName.text = "${collection.name} (${collection.count})"

        Glide.with(holder.itemView.context)
            .load(collection.coverImageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .centerCrop()
            .into(holder.imgCategory)

        holder.itemView.setOnClickListener {
            onItemClick(collection)
        }
    }

    override fun getItemCount(): Int = collectionList.size

    fun updateData(newList: List<RecipeCollection>) {
        this.collectionList = newList
        notifyDataSetChanged()
    }
}