package com.example.reimbifyapp.admin.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.reimbifyapp.R
import com.example.reimbifyapp.data.entities.History
import com.example.reimbifyapp.data.entities.Reimbursement
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RequestAdapter(private val listHistory: ArrayList<History>) :
    RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: History)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_history, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val history = listHistory[position]

        holder.tvTimestamp.text = formatDateTime(history.timestamp)
        holder.tvReceiptDate.text = formatDateTime(history.receiptDate)
        holder.tvDepartment.text = history.department
        holder.tvAmount.text = formatCurrency(history.amount)
        holder.tvDescription.text = history.description

        when (history.status.lowercase(Locale.getDefault())) {
            "under review", "under-review", "under_review" -> {
                holder.statusUnderReview.visibility = View.VISIBLE
                holder.statusApproved.visibility = View.GONE
                holder.statusRejected.visibility = View.GONE
            }
            "approved" -> {
                holder.statusUnderReview.visibility = View.GONE
                holder.statusApproved.visibility = View.VISIBLE
                holder.statusRejected.visibility = View.GONE
            }
            "rejected" -> {
                holder.statusUnderReview.visibility = View.GONE
                holder.statusApproved.visibility = View.GONE
                holder.statusRejected.visibility = View.VISIBLE
            }
            else -> {
                holder.statusUnderReview.visibility = View.GONE
                holder.statusApproved.visibility = View.GONE
                holder.statusRejected.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            onItemClickCallback?.onItemClicked(history)
        }
    }

    override fun getItemCount(): Int = listHistory.size

    fun updateData(newData: List<Reimbursement>) {
        listHistory.clear()

        val newList = parsingList(newData)
        listHistory.addAll(newList)
        notifyDataSetChanged()
    }

    private fun parsingList(data: List<Reimbursement>): List<History> {
        return data.map { reimbursement ->
            History(
                id = reimbursement.receiptId,
                timestamp = reimbursement.requestDate.toString(),
                status = reimbursement.status,
                receiptDate = reimbursement.receiptDate.toString(),
                department = reimbursement.department.departmentName,
                amount = reimbursement.amount.toDouble(),
                description = reimbursement.description,
                adminName = reimbursement.approval?.admin?.userName,
                accountNumber = reimbursement.account.accountNumber,
                receiveDate = reimbursement.approval?.responseDate?.toString(),
                declineDate = if (reimbursement.status == "rejected") reimbursement.approval?.responseDate?.toString() else null,
                declineReason = reimbursement.approval?.responseDescription,
                notaImage = reimbursement.receiptImageUrl,
                transferReceiptImage = reimbursement.approval?.transferImageUrl
            )
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        format.currency = Currency.getInstance("IDR")
        return format.format(amount)
    }

    private fun formatDateTime(dateString: String): String {
        return try {
            val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val date = originalFormat.parse(dateString)

            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            "${dateFormat.format(date!!)}\n${timeFormat.format(date)}"
        } catch (e: Exception) {
            Log.d("FAILED", "Failed to format date $e")
            dateString
        }
    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTimestamp: TextView = itemView.findViewById(R.id.timestamp)
        val tvReceiptDate: TextView = itemView.findViewById(R.id.receipt_date)
        val tvDepartment: TextView = itemView.findViewById(R.id.department)
        val tvAmount: TextView = itemView.findViewById(R.id.amount)
        val tvDescription: TextView = itemView.findViewById(R.id.description)

        val statusUnderReview: View = itemView.findViewById(R.id.status_under_review)
        val statusApproved: View = itemView.findViewById(R.id.status_approved)
        val statusRejected: View = itemView.findViewById(R.id.status_rejected)
    }
}
