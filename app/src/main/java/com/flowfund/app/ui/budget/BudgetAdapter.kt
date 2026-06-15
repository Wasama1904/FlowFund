package com.flowfund.app.ui.budget

import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flowfund.app.R
import java.text.NumberFormat
import java.util.Locale

class BudgetAdapter(
    private val onEdit: (BudgetStatus) -> Unit,
    private val onDelete: (BudgetStatus) -> Unit
) : ListAdapter<BudgetStatus, BudgetAdapter.VH>(DIFF) {

    private val currency = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView    = view.findViewById(R.id.tvBudgetCategory)
        val tvLimit: TextView       = view.findViewById(R.id.tvBudgetLimit)
        val tvSpent: TextView       = view.findViewById(R.id.tvBudgetSpent)
        val tvRemaining: TextView   = view.findViewById(R.id.tvBudgetRemaining)
        val progressBar: ProgressBar= view.findViewById(R.id.budgetProgress)
        val tvAlert: TextView       = view.findViewById(R.id.tvBudgetAlert)
        val btnEdit: View           = view.findViewById(R.id.btnEditBudget)
        val btnDelete: View         = view.findViewById(R.id.btnDeleteBudget)
        val tvMinMax: TextView      = view.findViewById(R.id.tvMinMax)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val status = getItem(position)
        holder.tvCategory.text  = status.category?.name ?: "Unknown"
        holder.tvLimit.text     = "Limit: ${currency.format(status.budget.monthlyLimit)}"
        holder.tvSpent.text     = "Spent: ${currency.format(status.spent)}"
        holder.tvRemaining.text = if (status.isOverBudget)
            "OVER by ${currency.format(-status.remaining)}"
        else "Remaining: ${currency.format(status.remaining)}"
        holder.tvRemaining.setTextColor(if (status.isOverBudget) Color.RED else Color.parseColor("#2E7D32"))
        holder.progressBar.progress = status.percentUsed.toInt().coerceIn(0, 100)
        holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(when {
            status.isOverBudget  -> Color.RED
            status.isNearLimit   -> Color.parseColor("#FF9800")
            else                 -> Color.parseColor("#4CAF50")
        })

        holder.tvAlert.visibility = if (status.isOverBudget || status.isNearLimit) View.VISIBLE else View.GONE
        holder.tvAlert.text = if (status.isOverBudget) "⚠ Over budget!" else "⚠ Near limit (${status.percentUsed.toInt()}%)"
        holder.tvAlert.setTextColor(if (status.isOverBudget) Color.RED else Color.parseColor("#E65100"))

        val min = status.budget.minimumGoal; val max = status.budget.maximumGoal
        holder.tvMinMax.text = "Min goal: ${currency.format(min)}  |  Max goal: ${currency.format(max)}"

        holder.btnEdit.setOnClickListener { onEdit(status) }
        holder.btnDelete.setOnClickListener { onDelete(status) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<BudgetStatus>() {
            override fun areItemsTheSame(a: BudgetStatus, b: BudgetStatus) = a.budget.id == b.budget.id
            override fun areContentsTheSame(a: BudgetStatus, b: BudgetStatus) = a == b
        }
    }
}
