package com.example.cashroyale.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.R

class GoalAdapter(private val goals: List<GoalStatus>) :
    RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    // ViewHolder holds references to views in each item layout
    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvGoalTitle)
        val tvTarget: TextView = itemView.findViewById(R.id.tvGoalTarget)
        val ivStatus: ImageView = itemView.findViewById(R.id.ivGoalStatus)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarGoal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        // Inflate the item layout for the RecyclerView
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.tvTitle.text = goal.title
        holder.tvTarget.text = "Target: R%.2f".format(goal.target)

        // Calculate progress % safely between 0 and 100
        val progressPercent = ((goal.currentSavings / goal.target) * 100).toInt().coerceIn(0, 100)
        holder.progressBar.progress = progressPercent

        // Show check icon if goal reached, hide otherwise
        if (goal.isReached) {
            holder.ivStatus.visibility = View.VISIBLE
            holder.ivStatus.setImageResource(R.drawable.ic_check_circle)
        } else {
            holder.ivStatus.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = goals.size
}

// Simple data class to hold goal info
data class GoalStatus(
    val title: String,
    val target: Double,
    val currentSavings: Double,
    val isReached: Boolean
)
