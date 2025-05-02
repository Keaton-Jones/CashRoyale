package com.example.cashroyale

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "income")
data class Income(
    @PrimaryKey(autoGenerate = true) val incomeId: Int = 0,
    val description: String,
    val amount: Double,
    val date: String,
    val paymentMethod: String,
    val category: String,
    val imageUri: String?
)
