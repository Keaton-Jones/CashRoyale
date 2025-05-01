package com.example.cashroyale.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cashroyale.Models.User

@Dao
interface UserDAO {
    @Insert
    fun insertUser(user: User): Long

    @Query("SELECT * FROM User WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
}