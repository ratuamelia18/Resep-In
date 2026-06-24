package com.ratu.resep_in.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ratu.resep_in.R
import com.ratu.resep_in.model.Comment
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter(private var commentList: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivUserPhoto: CircleImageView = view.findViewById(R.id.ivUserPhoto)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvComment: TextView = view.findViewById(R.id.tvComment)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        val rbCommentRating: RatingBar = view.findViewById(R.id.rbCommentRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentList[position]

        holder.tvUsername.text = comment.username
        holder.tvComment.text = comment.comment
        holder.rbCommentRating.rating = comment.rating

        Glide.with(holder.itemView.context)
            .load(comment.userPhoto)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(holder.ivUserPhoto)

        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        holder.tvTimestamp.text = comment.timestamp?.toDate()?.let { sdf.format(it) } ?: ""
    }

    override fun getItemCount() = commentList.size

    fun updateData(newComments: List<Comment>) {
        commentList = newComments
        notifyDataSetChanged()
    }
}