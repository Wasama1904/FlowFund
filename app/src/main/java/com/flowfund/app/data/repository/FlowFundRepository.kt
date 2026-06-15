package com.flowfund.app.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.flowfund.app.data.dao.CategoryTotal
import com.flowfund.app.data.database.FlowFundDatabase
import com.flowfund.app.data.entities.*
import kotlinx.coroutines.flow.Flow

class FlowFundRepository(context: Context) {
    private val db = FlowFundDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()
    private val budgetDao = db.budgetDao()
    private val goalDao = db.goalDao()
    private val badgeDao = db.badgeDao()

    // --- Users ---
    suspend fun register(user: User): Long = userDao.insert(user)
    suspend fun login(email: String, hash: String): User? = userDao.login(email, hash)
    suspend fun findUserByEmail(email: String): User? = userDao.findByEmail(email)
    suspend fun getUserById(id: Long): User? = userDao.findById(id)
    suspend fun updateUser(user: User) = userDao.update(user)

    // --- Categories ---
    fun getCategories(userId: Long): LiveData<List<Category>> = categoryDao.getByUser(userId)
    suspend fun getCategoriesSync(userId: Long): List<Category> = categoryDao.getByUserSync(userId)
    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)
    suspend fun getCategoryById(id: Long): Category? = categoryDao.findById(id)

    // --- Transactions ---
    fun getAllTransactions(userId: Long): LiveData<List<Transaction>> = transactionDao.getAll(userId)
    fun getTransactionsByPeriod(userId: Long, from: Long, to: Long): LiveData<List<Transaction>> =
        transactionDao.getByPeriod(userId, from, to)
    suspend fun getTransactionsByPeriodSync(userId: Long, from: Long, to: Long): List<Transaction> =
        transactionDao.getByPeriodSync(userId, from, to)
    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insert(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.delete(transaction)
    suspend fun totalExpenses(userId: Long, from: Long, to: Long): Double =
        transactionDao.totalExpenses(userId, from, to)
    suspend fun totalIncome(userId: Long, from: Long, to: Long): Double =
        transactionDao.totalIncome(userId, from, to)
    suspend fun expenseByCategory(userId: Long, from: Long, to: Long): List<CategoryTotal> =
        transactionDao.expenseByCategory(userId, from, to)
    suspend fun sumExpenseByCategory(userId: Long, categoryId: Long, from: Long, to: Long): Double =
        transactionDao.sumExpenseByCategory(userId, categoryId, from, to)
    suspend fun getTransactionById(id: Long): Transaction? = transactionDao.findById(id)

    // --- Budgets ---
    fun getBudgets(userId: Long, month: Int, year: Int): LiveData<List<Budget>> =
        budgetDao.getByMonthYear(userId, month, year)
    suspend fun getBudgetsSync(userId: Long, month: Int, year: Int): List<Budget> =
        budgetDao.getByMonthYearSync(userId, month, year)
    suspend fun insertBudget(budget: Budget): Long = budgetDao.insert(budget)
    suspend fun updateBudget(budget: Budget) = budgetDao.update(budget)
    suspend fun deleteBudget(budget: Budget) = budgetDao.delete(budget)
    suspend fun findBudgetByCategoryMonth(userId: Long, catId: Long, month: Int, year: Int): Budget? =
        budgetDao.findByCategoryMonth(userId, catId, month, year)

    // --- Goals ---
    fun getGoals(userId: Long): LiveData<List<Goal>> = goalDao.getByUser(userId)
    suspend fun insertGoal(goal: Goal): Long = goalDao.insert(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.update(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.delete(goal)

    // --- NEW: For AdviceProvider ---
    suspend fun getTotalBudgetForMonth(userId: Long, month: Int, year: Int): Double {
        return budgetDao.getTotalBudgetForMonth(userId, month, year) ?: 0.0
    }

    suspend fun getTotalSpentForMonth(userId: Long, month: Int, year: Int): Double {
        return transactionDao.getTotalSpentForMonth(userId, month, year) ?: 0.0
    }

    // --- Badges ---
    fun getAllBadges(userId: Long): Flow<List<Badge>> = badgeDao.getAllBadges(userId)
    suspend fun insertBadge(badge: Badge) = badgeDao.insertBadge(badge)
    suspend fun findBadgeByName(userId: Long, name: String): Badge? = badgeDao.findBadgeByName(userId, name)
}