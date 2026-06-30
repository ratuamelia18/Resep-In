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

class RecipeAdapter(
    private var recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit,
    private val onEditClick: ((Recipe) -> Unit)? = null,
    private val onDeleteClick: ((Recipe) -> Unit)? = null
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgRecipe: ImageView = itemView.findViewById(R.id.imgRecipeLatest)
        val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitleLatest)
        val tvCategory: TextView = itemView.findViewById(R.id.tvRecipeCategoryLatest)
        val tvRating: TextView = itemView.findViewById(R.id.tvRecipeRatingLatest)
        val tvDuration: TextView = itemView.findViewById(R.id.tvRecipeDurationLatest)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEditRecipe)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteRecipe)
        val cbBookmark: CheckBox = itemView.findViewById(R.id.cbBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = recipeList[position]
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()


        holder.tvTitle.text = item.title
        holder.tvCategory.text = item.category
        holder.tvRating.text = String.format(java.util.Locale.getDefault(), "%.2f", item.averageRating)
        holder.tvDuration.text = "${item.duration} menit"

        Glide.with(holder.itemView.context)
            .load(item.imgurl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.imgRecipe)

        holder.itemView.setOnClickListener { onItemClick(item) }

        val isOwner = currentUid != null && item.authorId == currentUid
        holder.btnEdit.visibility = if (isOwner && onEditClick != null) View.VISIBLE else View.GONE
        holder.btnDelete.visibility = if (isOwner && onDeleteClick != null) View.VISIBLE else View.GONE
        holder.btnEdit.setOnClickListener { onEditClick?.invoke(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick?.invoke(item) }

        if (currentUid != null) {
            val favId = "${currentUid}_${item.id}"

            holder.cbBookmark.setOnClickListener(null)

            db.collection("favorites").document(favId).get()
                .addOnSuccessListener { doc ->
                    holder.cbBookmark.isChecked = doc.exists()
                }

            holder.cbBookmark.setOnClickListener {
                val isChecked = holder.cbBookmark.isChecked
                if (isChecked) {
                    val favData = mapOf("userId" to currentUid, "recipeId" to item.id)
                    db.collection("favorites").document(favId).set(favData)
                        .addOnSuccessListener { Toast.makeText(holder.itemView.context, "Disimpan", Toast.LENGTH_SHORT).show() }
                } else {
                    db.collection("favorites").document(favId).delete()
                        .addOnSuccessListener { Toast.makeText(holder.itemView.context, "Dihapus", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    override fun getItemCount() = recipeList.size

    fun updateData(newList: List<Recipe>) {
        this.recipeList = newList
        notifyDataSetChanged()
    }
}