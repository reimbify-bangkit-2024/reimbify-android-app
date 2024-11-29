package com.example.reimbifyapp.user.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reimbifyapp.R
import com.example.reimbifyapp.data.entities.History

class HistoryAdapter(private val listHistory: ArrayList<History>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: History)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.card_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = listHistory[position]
        holder.tvTimestamp.text = history.timestamp
        holder.tvReceiptDate.text = history.receiptDate
        holder.tvDepartment.text = history.department
        holder.tvAmount.text = "${history.amount}"
        holder.tvDescription.text = history.description
        when (history.status) {
            "Under Review" -> {
                holder.statusUnderReview.visibility = View.VISIBLE
                holder.statusApproved.visibility = View.GONE
                holder.statusRejected.visibility = View.GONE
            }
            "Approved" -> {
                holder.statusUnderReview.visibility = View.GONE
                holder.statusApproved.visibility = View.VISIBLE
                holder.statusRejected.visibility = View.GONE
            }
            "Rejected" -> {
                holder.statusUnderReview.visibility = View.GONE
                holder.statusApproved.visibility = View.GONE
                holder.statusRejected.visibility = View.VISIBLE
            }
        }

        holder.itemView.setOnClickListener {
            onItemClickCallback?.onItemClicked(history)
        }
    }

    override fun getItemCount(): Int = listHistory.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
