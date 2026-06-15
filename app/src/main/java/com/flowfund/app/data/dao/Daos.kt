package com.flowfund.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.flowfund.app.data.entities.Transaction
import com.flowfund.app.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :hash LIMIT 1")
    suspend fun login(email: String, hash: String): User?

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getByUser(userId: Long): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getByUserSync(userId: Long): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Category?
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId AND date BETWEEN :from AND :to
        ORDER BY date DESC
    """)
    fun getByPeriod(userId: Long, from: Long, to: Long): LiveData<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId AND date BETWEEN :from AND :to
        ORDER BY date DESC
    """)
    suspend fun getByPeriodSync(userId: Long, from: Long, to: Long): List<Transaction>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
        ORDER BY date DESC
    """)
    fun getAll(userId: Long): LiveData<List<Transaction>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE userId = :userId AND categoryId = :categoryId
        AND type = 'EXPENSE' AND date BETWEEN :from AND :to
    """)
    suspend fun sumExpenseByCategory(userId: Long, categoryId: Long, from: Long, to: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE userId = :userId AND type = 'EXPENSE' AND date BETWEEN :from AND :to
    """)
    suspend fun totalExpenses(userId: Long, from: Long, to: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE userId = :userId AND type = 'INCOME' AND date BETWEEN :from AND :to
    """)
    suspend fun totalIncome(userId: Long, from: Long, to: Long): Double

    @Query("""
        SELECT categoryId, COALESCE(SUM(amount), 0) as total FROM transactions
        WHERE userId = :userId AND type = 'EXPENSE' AND date BETWEEN :from AND :to
        GROUP BY categoryId
    """)
    suspend fun expenseByCategory(userId: Long, from: Long, to: Long): List<CategoryTotal>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Transaction?

    @Query("""
    SELECT SUM(amount) FROM transactions 
    WHERE userId = :userId AND type = 'EXPENSE' 
    AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month 
    AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year""")
    suspend fun getTotalSpentForMonth(userId: Long, month: Int, year: Int): Double?
}

data class CategoryTotal(val categoryId: Long?, val total: Double)

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    fun getByMonthYear(userId: Long, month: Int, year: Int): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getByMonthYearSync(userId: Long, month: Int, year: Int): List<Budget>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId = :catId AND month = :month AND year = :year LIMIT 1")
    suspend fun findByCategoryMonth(userId: Long, catId: Long, month: Int, year: Int): Budget?

    @Query("""
    SELECT SUM(monthlyLimit) FROM budgets WHERE userId = :userId AND month = :month AND year = :year """)
    suspend fun getTotalBudgetForMonth(userId: Long, month: Int, year: Int): Double?
}

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getByUser(userId: Long): LiveData<List<Goal>> // 1. Completed this line for you
}

// 2. NEW: BadgeDao for the badge feature
@Dao
interface BadgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badge)

    @Query("SELECT * FROM badges WHERE userId = :userId ORDER BY earnedDate DESC")
    fun getAllBadges(userId: Long): Flow<List<Badge>>

    @Query("SELECT * FROM badges WHERE userId = :userId AND name = :name LIMIT 1")
    suspend fun findBadgeByName(userId: Long, name: String): Badge?
}