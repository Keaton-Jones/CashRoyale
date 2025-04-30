package com.example.cashroyale

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [User::class, Category::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDAO():UserDAO
    abstract fun categoryDAO():CategoryDAO

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new table with the new schema (including the 'id' primary key)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `Category_New` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`color` TEXT NOT NULL)"
                )

                // Copy the data from the old table to the new table
                database.execSQL(
                    "INSERT INTO `Category_New` (`name`, `color`) SELECT `name`, `color` FROM `Category`"
                )

                // Drop the old table
                database.execSQL("DROP TABLE `Category`")

                // Rename the new table to the original table name
                database.execSQL("ALTER TABLE `Category_New` RENAME TO `Category`")
            }
        }
        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashroyale_database"
                ).addMigrations(MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }
    }
}