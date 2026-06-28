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

class RecommendationAdapter(
    private var list: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgCategory)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val cbBookmark: CheckBox = view.findViewById(R.id.cbBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        holder.tvTitle.text = item.title
        holder.tvRating.text = String.format(java.util.Locale.getDefault(), "%.2f", item.averageRating)

        Glide.with(holder.img.context)
            .load(item.imgurl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.img)

        holder.itemView.setOnClickListener { onItemClick(item) }

        if (currentUid != null) {
            holder.cbBookmark.setOnCheckedChangeListener(null)

            db.collection("favorites")
                .whereEqualTo("userId", currentUid)
                .whereEqualTo("recipeId", item.id)
                .get()
                .addOnSuccessListener { snapshot ->
                    holder.cbBookmark.isChecked = !snapshot.isEmpty
                }

            holder.cbBookmark.setOnClickListener {
                val isChecked = holder.cbBookmark.isChecked
                if (isChecked) {
                    val favData = mapOf("userId" to currentUid, "recipeId" to item.id)
                    db.collection("favorites").add(favData)
                        .addOnSuccessListener { Toast.makeText(holder.itemView.context, "Disimpan", Toast.LENGTH_SHORT).show() }
                } else {
                    db.collection("favorites")
                        .whereEqualTo("userId", currentUid)
                        .whereEqualTo("recipeId", item.id)
                        .get().addOnSuccessListener { snapshot ->
                            for (doc in snapshot) doc.reference.delete()
                            Toast.makeText(holder.itemView.context, "Dihapus", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Recipe>) {
        this.list = newList
        notifyDataSetChanged()
    }
}