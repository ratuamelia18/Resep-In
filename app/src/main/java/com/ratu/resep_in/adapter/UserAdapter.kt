package com.ratu.resep_in.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ratu.resep_in.R
import com.ratu.resep_in.model.User
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private var userList: List<User>,
    private val onItemClick: (User) -> Unit,
    private val onFollowClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivUserPhoto: CircleImageView = view.findViewById(R.id.ivUserPhoto)
        val tvBio: TextView = view.findViewById(R.id.tvBio)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val btnFollow: Button = view.findViewById(R.id.btnFollow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        holder.tvUsername.text = user.username
        holder.tvBio.text = user.bio

        Glide.with(holder.itemView.context)
            .load(user.profileImageUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(holder.ivUserPhoto)


        holder.itemView.setOnClickListener { onItemClick(user) }

        holder.btnFollow.setOnClickListener { onFollowClick(user) }
    }

    override fun getItemCount() = userList.size

    fun updateData(newList: List<User>) {
        this.userList = newList
        notifyDataSetChanged()
    }
}