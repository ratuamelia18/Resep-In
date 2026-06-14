package com.ratu.resep_in.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.ratu.resep_in.R
import com.ratu.resep_in.model.Category

class CategoryAdapter(
    private val list: List<Category>,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardCategory)
        val img: ImageView = view.findViewById(R.id.imgCategory)
        val tv: TextView = view.findViewById(R.id.tvTitle)
        val check: ImageView = view.findViewById(R.id.icCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tv.text = item.name


        Glide.with(holder.img.context)
            .load(item.imgurl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.img)

        // Terapkan style border dan centang
        holder.card.strokeWidth = if (item.isSelected) 8 else 0
        holder.check.visibility = if (item.isSelected) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            item.isSelected = !item.isSelected
            notifyItemChanged(position)
            onSelectionChanged(list.count { it.isSelected })
        }
    }

    override fun getItemCount() = list.size
}