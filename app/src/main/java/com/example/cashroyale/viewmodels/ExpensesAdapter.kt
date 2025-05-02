package com.example.cashroyale.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.Expense
import com.example.cashroyale.R
import com.squareup.picasso.Picasso

class ExpensesAdapter(private var expenses: List<Expense>) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    // Holds the views for each expense item
    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val amount: TextView = itemView.findViewById(R.id.tvAmount)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val category: TextView = itemView.findViewById(R.id.tvCategory)
        val imageView: ImageView = itemView.findViewById(R.id.ivExpenseImage)
    }

    // Creates a new view holder for each expense item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(itemView)
    }

    // Binds the data to the views for each item in the list
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.description.text = expense.description
        holder.amount.text = "Amount: $${expense.amount}"
        holder.date.text = "Date: ${expense.date}"
        holder.category.text = "Category: ${expense.category}"
        // If there is an image, display it; otherwise, hide the image view
        if (!expense.imageUri.isNullOrEmpty()) {
            holder.imageView.visibility = View.VISIBLE
            Picasso.get().load(expense.imageUri).into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
        }
    }

    // Tells the adapter how many items there are in the list
    override fun getItemCount(): Int = expenses.size

    // Updates the list of expenses and refreshes the view
    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}

