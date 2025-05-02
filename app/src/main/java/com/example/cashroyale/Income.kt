package com.example.cashroyale

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.ForeignKey
import androidx.room.Index
import com.example.cashroyale.Models.User


@Entity(
    tableName = "income",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["email"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class Income(
    @PrimaryKey(autoGenerate = true) val incomeId: Int = 0,
    val description: String,
    val amount: Double,
    val date: String,
    val paymentMethod: String,
    val category: String,
    val imageUri: String?
    val userId: String
)
