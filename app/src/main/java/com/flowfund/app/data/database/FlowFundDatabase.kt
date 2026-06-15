package com.flowfund.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.flowfund.app.data.dao.*
import com.flowfund.app.data.entities.*

@Database(
    entities = [
        User::class,
        Category::class,
        Transaction::class,
        Budget::class,
        Goal::class,
        Badge::class // 1. Added Badge entity
    ],
    version = 2, // 2. Bumped version from 1 → 2
    exportSchema = false
)
abstract class FlowFundDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun badgeDao(): BadgeDao // 3. Added BadgeDao

    companion object {
        @Volatile private var INSTANCE: FlowFundDatabase? = null

        fun getInstance(context: Context): FlowFundDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FlowFundDatabase::class.java,
                    "flowfund_database"
                )
                    // 4. For dev: wipe DB on version change.
                    // For production you'd write a migration instead.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
