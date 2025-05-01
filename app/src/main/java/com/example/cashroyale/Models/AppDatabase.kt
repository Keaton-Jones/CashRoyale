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

@Database(entities = [User::class, Category::class, MonthlyGoals::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDAO(): UserDAO
    abstract fun categoryDAO(): CategoryDAO
    abstract fun monthlyGoalDAO(): MonthlyGoalDAO

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the monthly_goals table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `monthly_goals` (" +
                            "`goalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`userId` TEXT NOT NULL, " +
                            "`maxGoalAmount` REAL NOT NULL, " +
                            "`minGoalAmount` REAL NOT NULL, " +
                            "`goalSet` INTEGER NOT NULL, " + // Room stores booleans as 0 (false) or 1 (true)
                            "FOREIGN KEY(`userId`) REFERENCES `User`(`email`) ON DELETE CASCADE )" // Assuming 'email' is your User identifier
                )

                // Create an index for the userId for faster querying
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_monthly_goals_userId` ON `monthly_goals` (`userId`)")
            }
        }
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashroyale_database"
                ).addMigrations(MIGRATION_3_4).build()
                INSTANCE = instance
                instance
            }
        }
    }
}