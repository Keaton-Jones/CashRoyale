package com.example.cashroyale.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an income record in the application.
 * The [Entity] annotation specifies that this class maps to a database table named "income".
 */
@Entity(tableName = "income")
data class Income(
    /**
     * Unique identifier for the income entry.
     * The [PrimaryKey] annotation marks this field as the primary key of the table.
     * `autoGenerate = true` indicates that the database will automatically generate the ID.
     */
    @PrimaryKey(autoGenerate = true) val incomeId: Int = 0,

    /** A brief description of the income source (e.g., "Salary", "Freelance work"). */
    val description: String,

    /** The monetary amount of the income. */
    val amount: Double,

    /** The date when the income was received (stored as a String for simplicity). */
    val date: String,

    /** The method through which the income was received (e.g., "Bank transfer", "Cash"). */
    val paymentMethod: String,

    /** The category to which the income belongs (e.g., "Salary", "Investments"). */
    val category: String,

    /**
     * The URI of an image associated with the income (e.g., a deposit slip).
     * Can be null if no image is attached.
     */
    val imageUri: String?
)