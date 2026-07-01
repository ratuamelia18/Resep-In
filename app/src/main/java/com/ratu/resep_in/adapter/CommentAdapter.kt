package com.ratu.resep_in.adapter

import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ratu.resep_in.R
import com.ratu.resep_in.model.Comment
import de.hdodenhof.circleimageview.CircleImageView


class CommentAdapter(
    private var commentList: List<Comment>,
    private val currentUserUid: String?,
    private val onDeleteClick: (Comment) -> Unit,
    private val onUserClick: (String) -> Unit
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivUserPhoto: CircleImageView = view.findViewById(R.id.ivUserPhoto)
        val tvInitial: TextView = view.findViewById(R.id.tvInitial)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvComment: TextView = view.findViewById(R.id.tvComment)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        val rbCommentRating: RatingBar = view.findViewById(R.id.rbCommentRating)
        val btnDeleteComment: ImageView = view.findViewById(R.id.btnDeleteComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentList[position]

        android.util.Log.d("DEBUG_ADAPTER", "Posisi: $position, User: ${comment.username}, PhotoURL: '${comment.userPhoto}'")

        holder.tvUsername.text = comment.username
        holder.tvComment.text = comment.comment
        holder.rbCommentRating.rating = comment.rating

        holder.tvUsername.setOnClickListener {
            onUserClick(comment.userId)
        }

        if (comment.userPhoto.isNullOrBlank()) {
            holder.tvInitial.visibility = View.VISIBLE
            holder.tvInitial.text = if (comment.username.isNotEmpty()) comment.username[0].toString().uppercase() else "U"
            holder.ivUserPhoto.setImageResource(R.drawable.bg_circle_initial)
        } else {
            holder.tvInitial.visibility = View.GONE
            Glide.with(holder.itemView.context)
                .load(comment.userPhoto)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.ivUserPhoto)
        }

        if (comment.userId == currentUserUid) {
            holder.btnDeleteComment.visibility = View.VISIBLE
            holder.btnDeleteComment.setOnClickListener { onDeleteClick(comment) }
        } else {
            holder.btnDeleteComment.visibility = View.GONE
        }

        comment.timestamp?.toDate()?.time?.let { timestamp ->
            val now = System.currentTimeMillis()
            val timeAgo = DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.MINUTE_IN_MILLIS)
            holder.tvTimestamp.text = timeAgo
        } ?: run {
            holder.tvTimestamp.text = ""
        }
    }

    override fun getItemCount() = commentList.size

    fun updateData(newComments: List<Comment>) {
        commentList = newComments
        notifyDataSetChanged()
    }
}