package com.example.cashroyale.Models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cashroyale.DAO.CategoryDAO
import com.example.cashroyale.DAO.MonthlyGoalDAO
import com.example.cashroyale.DAO.UserDAO

@Database(entities = [User::class, Category::class, MonthlyGoals::class], version = 5)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDAO(): UserDAO
    abstract fun categoryDAO(): CategoryDAO
    abstract fun monthlyGoalDAO(): MonthlyGoalDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the 'type' column to the 'categories' table.
                database.execSQL("ALTER TABLE `category` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'expense'")
                // The default value is set to 'expense'.  You can choose a different default if appropriate.
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashroyale_database"
                ).addMigrations(MIGRATION_4_5).build()
                INSTANCE = instance
                instance
            }
        }
    }
}