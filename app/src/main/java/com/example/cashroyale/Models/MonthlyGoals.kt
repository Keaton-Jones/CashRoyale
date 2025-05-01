package com.example.cashroyale.Models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "monthly_goals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["email"], // Or ["id"] if User's primary key is id
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class MonthlyGoals(
    @PrimaryKey(autoGenerate = true) val goalId: Int = 0,
    val userId: String, // Should match the type of User's primary key
    val maxGoalAmount: Double,
    val minGoalAmount: Double,
    val goalSet: Boolean
): Parcelable
