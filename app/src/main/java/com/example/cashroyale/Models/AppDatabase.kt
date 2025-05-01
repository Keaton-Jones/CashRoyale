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
import com.example.cashroyale.ExpenseDAO
import com.example.cashroyale.Expense

@Database(entities = [User::class, Category::class, MonthlyGoals::class, Expense::class], version = 6)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDAO(): UserDAO
    abstract fun categoryDAO(): CategoryDAO
    abstract fun monthlyGoalDAO(): MonthlyGoalDAO
    abstract fun expenseDAO(): ExpenseDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cash_royale_db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `Category_New` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color` TEXT NOT NULL
                    )
                """.trimIndent())
                database.execSQL("INSERT INTO `Category_New` (`name`, `color`) SELECT `name`, `color` FROM `Category`")
                database.execSQL("DROP TABLE `Category`")
                database.execSQL("ALTER TABLE `Category_New` RENAME TO `Category`")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `expenses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `description` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `date` TEXT NOT NULL,
                        `paymentMethod` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `imageUri` TEXT
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `Category` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'expense'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `monthly_goals`")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `monthly_goals` (
                        `goalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `goalSet` INTEGER NOT NULL,
                        `maxGoalAmount` REAL NOT NULL,
                        `minGoalAmount` REAL NOT NULL,
                        `userId` TEXT NOT NULL,
                        FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_monthly_goals_userId` 
                    ON `monthly_goals` (`userId`)
                """.trimIndent())
            }
        }
    }
}