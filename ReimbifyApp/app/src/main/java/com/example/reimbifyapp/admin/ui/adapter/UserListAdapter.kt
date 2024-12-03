package com.example.reimbifyapp.admin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.reimbifyapp.data.entities.User
import com.example.reimbifyapp.databinding.ItemUserBinding

class UserListAdapter(
    private var userList: List<User>,
    private val onDeleteClick: (User, Int) -> Unit
) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.apply {
            tvUsername.text = user.userName
            tvDepartment.text = user.department.departmentName
            tvRole.text = user.role

            btnDelete.setOnClickListener {
                onDeleteClick(user, position)
            }
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateData(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}
