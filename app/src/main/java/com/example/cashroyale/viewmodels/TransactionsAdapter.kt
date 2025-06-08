package com.example.cashroyale.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.Transactions
import com.example.cashroyale.R

class TransactionsAdapter(private val transactionsList: List<Transactions>) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        val amountTextView: TextView = itemView.findViewById(R.id.tvAmount)
        val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        val typeTextView: TextView = itemView.findViewById(R.id.tvType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionsList[position]
        holder.descriptionTextView.text = transaction.description
        holder.amountTextView.text = "R${transaction.amount}"
        holder.dateTextView.text = transaction.date
        holder.typeTextView.text = transaction.type // Income or Expense

        // Sets the colour depending on the type of transaction
        val colorRes = if (transaction.type.lowercase() == "income") {
            R.color.green
        } else {
            R.color.red
        }
        holder.amountTextView.setTextColor(holder.itemView.context.getColor(colorRes))
    }

    override fun getItemCount(): Int = transactionsList.size
}