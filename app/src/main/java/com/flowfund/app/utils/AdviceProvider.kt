package com.flowfund.app.utils

object AdviceProvider {
    fun getAdvice(totalSpent: Double, totalBudget: Double, topCategory: String?): String {
        if (totalBudget <= 0) return "Set a monthly budget to start getting personalized tips!"

        val percentUsed = (totalSpent / totalBudget * 100)

        return when {
            percentUsed > 100 -> "You're over budget this month. Try reducing spending on $topCategory."
            percentUsed > 90 -> "Heads up! You've used ${percentUsed.toInt()}% of your budget. Slow down on $topCategory."
            percentUsed > 75 -> "You're at ${percentUsed.toInt()}% of your budget. Keep an eye on $topCategory."
            percentUsed > 50 -> "Good pace — you're halfway through your budget. Top spending: $topCategory."
            totalSpent == 0.0 -> "No spending yet this month. Your budget is $${totalBudget.toInt()}."
            else -> "You're doing great! Only ${percentUsed.toInt()}% of budget used. Keep it up."
        }
    }
}