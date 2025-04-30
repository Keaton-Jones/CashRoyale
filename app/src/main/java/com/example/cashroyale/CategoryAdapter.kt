package com.example.cashroyale.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.R
import com.example.cashroyale.Category

class CategoryAdapter(
    private var categories: List<Category>,
    private val onEditClicked: (Category) -> Unit,
    private val onDeleteClicked: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val colorView: View = itemView.findViewById(R.id.categoryColorView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentCategory = categories[position]
        holder.nameTextView.text = currentCategory.name
        try {
            holder.colorView.setBackgroundColor(Color.parseColor(currentCategory.color))
        } catch (e: IllegalArgumentException) {
            holder.colorView.setBackgroundColor(Color.GRAY)
        }

        holder.editButton.setOnClickListener {
            onEditClicked(currentCategory)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClicked(currentCategory)
        }
    }

    override fun getItemCount() = categories.size

    fun updateList(newList: List<Category>) {
        categories = newList
        notifyDataSetChanged()
    }
}