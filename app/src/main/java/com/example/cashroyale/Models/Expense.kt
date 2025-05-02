package com.example.cashroyale.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val date: String,
    val paymentMethod: String,
    val category: String,
    val imageUri: String? = null
)
