package com.flowfund.app.ui.budget

import androidx.lifecycle.*
import com.flowfund.app.data.entities.Badge
import com.flowfund.app.data.entities.Budget
import com.flowfund.app.data.entities.Category
import com.flowfund.app.data.repository.FlowFundRepository
import com.flowfund.app.utils.DateUtils
import kotlinx.coroutines.launch

data class BudgetStatus(
    val budget: Budget,
    val category: Category?,
    val spent: Double,
    val remaining: Double,
    val percentUsed: Float,
    val isOverBudget: Boolean,
    val isNearLimit: Boolean  // within 20%
)

class BudgetViewModel(
    private val repo: FlowFundRepository,
    val userId: Long
) : ViewModel() {

    private val _month = MutableLiveData(DateUtils.currentMonth())
    private val _year  = MutableLiveData(DateUtils.currentYear())
    val month: LiveData<Int> = _month
    val year:  LiveData<Int> = _year

    val categories: LiveData<List<Category>> = repo.getCategories(userId)
    val budgetStatuses = MutableLiveData<List<BudgetStatus>>()
    val saveResult = MutableLiveData<Result<Unit>>()

    init { loadBudgets() }

    fun setMonthYear(m: Int, y: Int) { _month.value = m; _year.value = y; loadBudgets() }

    fun loadBudgets() = viewModelScope.launch {
        val m = _month.value ?: DateUtils.currentMonth()
        val y = _year.value ?: DateUtils.currentYear()
        val from = DateUtils.startOfMonth(m, y)
        val to   = DateUtils.endOfMonth(m, y)
        val budgets = repo.getBudgetsSync(userId, m, y)
        val statuses = budgets.map { b ->
            val cat   = b.categoryId.let { repo.getCategoryById(it) }
            val spent = repo.sumExpenseByCategory(userId, b.categoryId, from, to)
            val rem   = b.monthlyLimit - spent
            val pct   = if (b.monthlyLimit > 0) (spent / b.monthlyLimit * 100).toFloat() else 0f
            BudgetStatus(b, cat, spent, rem, pct, rem < 0, pct >= 80f)
        }
        budgetStatuses.postValue(statuses)
        checkAndAwardBadge()
    }

    fun saveBudget(categoryId: Long, limit: Double, minGoal: Double, maxGoal: Double) {
        if (limit <= 0) { saveResult.value = Result.failure(Exception("Limit must be > 0")); return }
        val m = _month.value ?: DateUtils.currentMonth()
        val y = _year.value ?: DateUtils.currentYear()
        viewModelScope.launch {
            try {
                val existing = repo.findBudgetByCategoryMonth(userId, categoryId, m, y)
                val budget = Budget(
                    id = existing?.id ?: 0,
                    userId = userId, categoryId = categoryId,
                    monthlyLimit = limit, minimumGoal = minGoal, maximumGoal = maxGoal,
                    month = m, year = y
                )
                if (existing != null) repo.updateBudget(budget) else repo.insertBudget(budget)
                saveResult.postValue(Result.success(Unit))
                loadBudgets()
            } catch (e: Exception) { saveResult.postValue(Result.failure(e)) }
        }
    }

    fun deleteBudget(b: Budget) = viewModelScope.launch { repo.deleteBudget(b); loadBudgets() }

    // --- NEW: Badge awarding logic ---
    fun checkAndAwardBadge() = viewModelScope.launch {
        val m = _month.value ?: DateUtils.currentMonth()
        val y = _year.value ?: DateUtils.currentYear()
        val from = DateUtils.startOfMonth(m, y)
        val to = DateUtils.endOfMonth(m, y)

        val budgets = repo.getBudgetsSync(userId, m, y)
        val totalBudget = budgets.sumOf { it.monthlyLimit }
        val totalSpent = repo.totalExpenses(userId, from, to)

        // Only award if user has budgets set and stayed under total
        if (totalBudget > 0 && totalSpent <= totalBudget) {
            val badgeName = "Budget Master ${DateUtils.monthName(m)} $y"

            // Check if badge already exists so we don't award duplicates
            val existing = repo.findBadgeByName(userId, badgeName)
            if (existing == null) {
                val badge = Badge(
                    userId = userId,
                    name = badgeName,
                    description = "Stayed under your total budget for ${DateUtils.monthName(m)}",
                    earnedDate = System.currentTimeMillis(),
                    iconRes = "ic_badge_budget",
                    type = "BUDGET"
                )
                repo.insertBadge(badge)
            }
        }
    }
}