package com.example.cashroyale.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cashroyale.Models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category): Int

    @Delete
    suspend fun delete(category: Category): Int

    @Query("SELECT * FROM category ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE id = :id")
    fun getCategoryById(id: Int): Flow<Category?>

    @Query("SELECT * FROM category WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: String): Flow<List<Category>>


    @Query("SELECT EXISTS(SELECT 1 FROM category WHERE name = :name)")
    suspend fun exists(name: String): Boolean
}