package com.flowfund.app.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.flowfund.app.data.entities.Budget
import com.flowfund.app.data.entities.Category
import com.flowfund.app.data.repository.FlowFundRepository
import com.flowfund.app.utils.AdviceProvider
import com.flowfund.app.utils.DateUtils
import kotlinx.coroutines.launch

data class DashboardData(
    val totalIncome: Double,
    val totalExpenses: Double,
    val balance: Double,
    val categoryBreakdown: List<Pair<String, Double>>
)

data class CategoryGoalStatus(
    val categoryName: String,
    val spent: Double,
    val minGoal: Double,
    val maxGoal: Double,
    val status: GoalStatus
)

enum class GoalStatus { UNDER_MIN, ON_TRACK, OVER_MAX, NO_GOAL }

data class BarChartData(
    val labels: List<String>,
    val spent: List<Float>,
    val minGoals: List<Float>,
    val maxGoals: List<Float>
)

class DashboardViewModel(
    private val repo: FlowFundRepository,
    private val userId: Long
) : ViewModel() {

    val dashboardData = MutableLiveData<DashboardData>()
    val goalStatusList = MutableLiveData<List<CategoryGoalStatus>>()
    val barChartData = MutableLiveData<BarChartData>()

    private var selectedMonth = DateUtils.currentMonth()
    private var selectedYear = DateUtils.currentYear()

    init { loadData() }

    fun setMonthYear(month: Int, year: Int) {
        selectedMonth = month
        selectedYear = year
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val from = DateUtils.startOfMonth(selectedMonth, selectedYear)
            val to = DateUtils.endOfMonth(selectedMonth, selectedYear)

            val income = repo.totalIncome(userId, from, to)
            val expenses = repo.totalExpenses(userId, from, to)
            val byCat = repo.expenseByCategory(userId, from, to)

            val breakdown = byCat.map { ct ->
                val cat = if (ct.categoryId != null) repo.getCategoryById(ct.categoryId) else null
                (cat?.name ?: "Uncategorised") to ct.total
            }

            dashboardData.postValue(DashboardData(income, expenses, income - expenses, breakdown))

            // Load budgets for the selected month
            val budgets = repo.getBudgetsSync(userId, selectedMonth, selectedYear)

            if (budgets.isEmpty()) {
                goalStatusList.postValue(emptyList())
                barChartData.postValue(BarChartData(emptyList(), emptyList(), emptyList(), emptyList()))
                return@launch
            }

            val labels = mutableListOf<String>()
            val spentValues = mutableListOf<Float>()
            val minValues = mutableListOf<Float>()
            val maxValues = mutableListOf<Float>()
            val statusItems = mutableListOf<CategoryGoalStatus>()

            budgets.forEach { budget ->
                val cat = repo.getCategoryById(budget.categoryId)
                val catName = cat?.name ?: "Unknown"
                val spent = repo.sumExpenseByCategory(userId, budget.categoryId, from, to)

                labels.add(catName)
                spentValues.add(spent.toFloat())
                minValues.add(budget.minimumGoal.toFloat())
                maxValues.add(budget.maximumGoal.toFloat())

                val status = when {
                    budget.minimumGoal <= 0 && budget.maximumGoal <= 0 -> GoalStatus.NO_GOAL
                    budget.maximumGoal > 0 && spent > budget.maximumGoal -> GoalStatus.OVER_MAX
                    budget.minimumGoal > 0 && spent < budget.minimumGoal -> GoalStatus.UNDER_MIN
                    else -> GoalStatus.ON_TRACK
                }

                statusItems.add(
                    CategoryGoalStatus(catName, spent, budget.minimumGoal, budget.maximumGoal, status)
                )
            }

            goalStatusList.postValue(statusItems)
            barChartData.postValue(BarChartData(labels, spentValues, minValues, maxValues))
        }
    }

    // NEW: Get monthly spending advice
    fun getMonthlyAdvice(): LiveData<String> = liveData {
        val totalBudget = repo.getTotalBudgetForMonth(userId, selectedMonth, selectedYear)
        val totalSpent = repo.getTotalSpentForMonth(userId, selectedMonth, selectedYear)

        val from = DateUtils.startOfMonth(selectedMonth, selectedYear)
        val to = DateUtils.endOfMonth(selectedMonth, selectedYear)
        val byCat = repo.expenseByCategory(userId, from, to)
        val topCatId = byCat.maxByOrNull { it.total }?.categoryId
        val topCatName = topCatId?.let { repo.getCategoryById(it)?.name } ?: "everything"

        val advice = AdviceProvider.getAdvice(totalSpent, totalBudget, topCatName)
        emit(advice)
    }
}