package com.example.reimbifyapp.user.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.example.reimbifyapp.data.entities.Account
import com.example.reimbifyapp.databinding.ItemBankAccountBinding

class BankAccountAdapter(
    private var accounts: MutableList<Account>,
    private val onEditClicked: (Account) -> Unit
) : RecyclerView.Adapter<BankAccountAdapter.BankAccountViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankAccountViewHolder {
        val binding = ItemBankAccountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BankAccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BankAccountViewHolder, position: Int) {
        val account = accounts[position]
        holder.bind(account)
    }

    override fun getItemCount(): Int = accounts.size

    fun updateAccounts(newAccounts: List<Account>) {
        val diffResult = DiffUtil.calculateDiff(BankAccountDiffCallback(accounts, newAccounts))
        accounts.clear()
        accounts.addAll(newAccounts)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class BankAccountViewHolder(private val binding: ItemBankAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(account: Account) {
            binding.tvAccountTitle.text = account.accountTitle
            binding.tvAccountName.text = account.accountHolderName
            binding.tvAccountNumber.text = maskAccountNumber(account.accountNumber)
            binding.tvBankName.text = account.bank.bankName

            binding.ivEditIcon.setOnClickListener {
                onEditClicked(account)
            }
        }

        private fun maskAccountNumber(accountNumber: String): String {
            return if (accountNumber.length > 4) {
                val initialPart = accountNumber.dropLast(4)
                val maskedPart = "****"
                initialPart + maskedPart
            } else {
                accountNumber
            }
        }
    }
}

