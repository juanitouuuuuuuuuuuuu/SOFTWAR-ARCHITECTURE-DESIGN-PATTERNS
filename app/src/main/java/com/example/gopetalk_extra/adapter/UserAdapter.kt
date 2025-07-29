package com.example.gopetalk_extra.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gopetalk_extra.R

class UserAdapter(private var users: List<String>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconUser: ImageView = view.findViewById(R.id.icon_user)
        val nameUser: TextView = view.findViewById(R.id.name_user)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val email = users[position]
        holder.nameUser.text = email
        holder.iconUser.setImageResource(R.drawable.user_icon) // imagen local
    }

    fun updateUsers(newUsers: List<String>) {
        Log.d("UserAdapter", "Users: $users")
        users = newUsers
        notifyDataSetChanged()
        Log.d("UserAdapter", "Users: $users")
    }
}


