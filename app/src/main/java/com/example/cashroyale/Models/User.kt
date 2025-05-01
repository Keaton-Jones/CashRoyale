package com.example.cashroyale.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "User")
data class User(
    @PrimaryKey val email:String,
    val password:String
)
