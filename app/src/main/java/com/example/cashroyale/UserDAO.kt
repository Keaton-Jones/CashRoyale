package com.example.cashroyale

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDAO {
    @Insert
    fun insertUser(user: User): Long // Returns the row ID

    @Query("SELECT * FROM User WHERE email = :email")
    fun getUserByEmail(email: String): User?
}