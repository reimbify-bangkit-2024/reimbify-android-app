package com.example.reimbifyapp.user.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.reimbifyapp.data.entities.Account

class BankAccountDiffCallback(
    private val oldList: List<Account>,
    private val newList: List<Account>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].accountId == newList[newItemPosition].accountId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
