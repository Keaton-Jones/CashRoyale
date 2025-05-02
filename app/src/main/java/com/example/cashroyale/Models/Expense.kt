package com.example.cashroyale.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an expense record in the application.
 * The [Entity] annotation specifies that this class maps to a database table named "expenses".
 */
@Entity(tableName = "expenses")
data class Expense(
    /**
     * Unique identifier for the expense.
     * The [PrimaryKey] annotation marks this field as the primary key of the table.
     * `autoGenerate = true` indicates that the database will automatically generate the ID.
     */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /** A brief description of the expense (e.g., "Grocery shopping", "Dinner with friends"). */
    val description: String,

    /** The monetary amount of the expense. */
    val amount: Double,

    /** The date when the expense occurred (stored as a String for simplicity). */
    val date: String,

    /** The method used for payment (e.g., "Cash", "Credit Card"). */
    val paymentMethod: String,

    /** The category to which the expense belongs (e.g., "Food", "Entertainment"). */
    val category: String,

    /**
     * The URI of an image associated with the expense (e.g., a receipt).
     * Can be null if no image is attached.
     */
    val imageUri: String? = null
)