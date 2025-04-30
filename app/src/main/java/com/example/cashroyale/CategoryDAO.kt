package com.example.cashroyale

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(category: Category): Long // Returns the row ID

    @Update
    suspend fun update(category: Category): Int // Update based on id

    @Delete
    suspend fun delete(category: Category): Int // Delete based on id

    @Query("SELECT * FROM Category WHERE id = :categoryId")
    fun getCategoryById(categoryId: Int): Flow<Category?>

    @Query("SELECT * FROM Category WHERE name = :categoryName")
    fun getCategoryByName(categoryName: String): Flow<Category?>

    @Query("SELECT * FROM Category ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT EXISTS(SELECT 1 FROM Category WHERE name = :categoryName)")
    suspend fun exists(categoryName: String): Boolean
}