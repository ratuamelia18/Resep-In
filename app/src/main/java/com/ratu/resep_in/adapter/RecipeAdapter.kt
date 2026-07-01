package com.ratu.resep_in.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ratu.resep_in.R
import com.ratu.resep_in.model.Recipe


class RecipeAdapter(
    private var recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit,
    private val onEditClick: ((Recipe) -> Unit)? = null,
    private val onDeleteClick: ((Recipe) -> Unit)? = null,
    private val onArchiveClick: ((Recipe) -> Unit)? = null,
    private val onSaveClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgRecipe: ImageView = itemView.findViewById(R.id.imgRecipeLatest)
        val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitleLatest)
        val tvCategory: TextView = itemView.findViewById(R.id.tvRecipeCategoryLatest)
        val tvRating: TextView = itemView.findViewById(R.id.tvRecipeRatingLatest)
        val tvDuration: TextView = itemView.findViewById(R.id.tvRecipeDurationLatest)
        val btnMenuOptions: ImageView = itemView.findViewById(R.id.btnMenuOptions)

        val btnSave: ImageView = itemView.findViewById(R.id.btnSave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = recipeList[position]
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

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

        if (isOwner) {
            holder.btnMenuOptions.visibility = View.VISIBLE
            holder.btnMenuOptions.setOnClickListener { view ->
                val popup = PopupMenu(holder.itemView.context, view)
                popup.menuInflater.inflate(R.menu.menu_post_options, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> { onEditClick?.invoke(item); true }
                        R.id.action_delete -> { onDeleteClick?.invoke(item); true }
                        R.id.action_archive -> { onArchiveClick?.invoke(item); true }
                        else -> false
                    }
                }
                popup.show()
            }
        } else {
            holder.btnMenuOptions.visibility = View.GONE
        }

        if (item.isSaved) {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark_outline)
        } else {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark)
        }



        holder.btnSave.setOnClickListener {
            onSaveClick(item)
            item.isSaved = true
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = recipeList.size

    fun updateData(newList: List<Recipe>) {
        this.recipeList = newList
        notifyDataSetChanged()
    }
}