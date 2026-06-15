package com.flowfund.app.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.flowfund.app.data.repository.FlowFundRepository
import com.flowfund.app.ui.auth.AuthViewModel
import com.flowfund.app.ui.badges.BadgeViewModel
import com.flowfund.app.ui.budget.BudgetViewModel
import com.flowfund.app.ui.categories.CategoriesViewModel
import com.flowfund.app.ui.dashboard.DashboardViewModel
import com.flowfund.app.ui.goals.GoalViewModel
import com.flowfund.app.ui.transactions.TransactionViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = FlowFundRepository(context)
        val userId = SessionManager.getUserId(context)
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repo) as T
            modelClass.isAssignableFrom(CategoriesViewModel::class.java) ->
                CategoriesViewModel(repo, userId) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repo, userId) as T
            modelClass.isAssignableFrom(TransactionViewModel::class.java) ->
                TransactionViewModel(repo, userId) as T
            modelClass.isAssignableFrom(BudgetViewModel::class.java) ->
                BudgetViewModel(repo, userId) as T
            modelClass.isAssignableFrom(GoalViewModel::class.java) ->
                GoalViewModel(repo, userId) as T
            modelClass.isAssignableFrom(BadgeViewModel::class.java) ->
                BadgeViewModel(repo, userId) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
