package com.example.cashroyale.Models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a category for expenses or income.
 * Implements [Parcelable] to allow passing instances between components.
 */
@Parcelize
@Entity(tableName = "Category") // Specifies the table name in the database
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Unique identifier for the category, auto-generated
    val name: String, // The name of the category (e.g., "Food", "Salary")
    val color: String, // The color associated with the category (e.g., "#FF0000" for Red)
    val type: String // The type of the category ("income" or "expense")
): Parcelable