package com.example.reimbifyapp.user.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reimbifyapp.R
import com.example.reimbifyapp.data.entities.History

class UnderReviewAdapter(private val listHistory: ArrayList<History>) :
    RecyclerView.Adapter<UnderReviewAdapter.UnderReviewViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: History)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnderReviewViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_history, parent, false)
        return UnderReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnderReviewViewHolder, position: Int) {
        val history = listHistory[position]
        holder.tvTimestamp.text = history.timestamp
        holder.tvReceiptDate.text = history.receiptDate
        holder.tvDepartment.text = history.department
        holder.tvAmount.text = history.amount.toString()
        holder.tvDescription.text = history.description

        // Set visibility for status views
        holder.statusUnderReview.visibility = View.VISIBLE
        holder.statusApproved.visibility = View.GONE
        holder.statusRejected.visibility = View.GONE

        // Set item click listener
        holder.itemView.setOnClickListener {
            onItemClickCallback?.onItemClicked(history)
        }
    }

    override fun getItemCount(): Int = listHistory.size

    class UnderReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
