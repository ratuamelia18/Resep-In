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
        val imgCategory: ImageView = itemView.findViewById(R.id.imgCategory)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val cbBookmark: CheckBox = itemView.findViewById(R.id.cbBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_category, parent, false)
        return SavedViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {
        val recipe = recipeList[position]
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        holder.tvCategoryName.text = recipe.title
        Glide.with(holder.itemView.context).load(recipe.imgurl).into(holder.imgCategory)
        holder.itemView.setOnClickListener { onItemClick(recipe) }

        if (currentUid != null) {
            val favId = "${currentUid}_${recipe.id}"

            holder.cbBookmark.setOnClickListener(null)

            db.collection("favorites").document(favId).get()
                .addOnSuccessListener { doc ->
                    holder.cbBookmark.isChecked = doc.exists()
                }

            holder.cbBookmark.setOnClickListener {
                val isChecked = holder.cbBookmark.isChecked

                if (isChecked) {
                    val data = mapOf("userId" to currentUid, "recipeId" to recipe.id)
                    db.collection("favorites").document(favId).set(data)
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Disimpan ke bookmark", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    db.collection("favorites").document(favId).delete()
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Dihapus dari bookmark", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int = recipeList.size

    fun updateData(newList: List<Recipe>) {
        this.recipeList.clear()
        this.recipeList.addAll(newList)
        notifyDataSetChanged()
    }
}