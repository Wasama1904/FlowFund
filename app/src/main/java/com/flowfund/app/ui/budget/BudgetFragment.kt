package com.flowfund.app.ui.budget

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.flowfund.app.R
import com.flowfund.app.databinding.FragmentBudgetBinding
import com.flowfund.app.utils.DateUtils
import com.flowfund.app.utils.ViewModelFactory
import java.util.*

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BudgetViewModel
    private lateinit var adapter: BudgetAdapter
    // Cache categories so they're available when the FAB is tapped
    private var cachedCategories: List<com.flowfund.app.data.entities.Category> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory(requireContext()))[BudgetViewModel::class.java]

        adapter = BudgetAdapter(onEdit = { showAddEditDialog(it) }, onDelete = { confirmDelete(it) })
        binding.rvBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBudgets.adapter = adapter

        viewModel.budgetStatuses.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // Keep a local copy of categories so they're ready when the FAB is tapped
        viewModel.categories.observe(viewLifecycleOwner) { cats ->
            cachedCategories = cats ?: emptyList()
        }

        // Observe saveResult once here, not inside the dialog (avoids stacking observers)
        viewModel.saveResult.observe(viewLifecycleOwner) { res ->
            if (res == null) return@observe
            viewModel.saveResult.value = null
            if (res.isFailure) {
                Toast.makeText(requireContext(), res.exceptionOrNull()?.message ?: "Error saving budget", Toast.LENGTH_SHORT).show()
            }
        }

        binding.fabAddBudget.setOnClickListener { showAddEditDialog(null) }
        binding.btnPickMonth.setOnClickListener { pickMonth() }
        updateMonthLabel()
    }

    private fun updateMonthLabel() {
        // Observe month and year independently to avoid nested observer leak
        viewModel.month.observe(viewLifecycleOwner) { m ->
            val y = viewModel.year.value ?: return@observe
            binding.tvBudgetMonth.text = String.format("%02d / %d", m, y)
        }
        viewModel.year.observe(viewLifecycleOwner) { y ->
            val m = viewModel.month.value ?: return@observe
            binding.tvBudgetMonth.text = String.format("%02d / %d", m, y)
        }
    }

    private fun pickMonth() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, _ ->
            viewModel.setMonthYear(m + 1, y)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1).show()
    }

    private fun showAddEditDialog(existing: BudgetStatus?) {
        val cats = cachedCategories
        if (cats.isEmpty()) { Toast.makeText(requireContext(), "Add a category first", Toast.LENGTH_SHORT).show(); return }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_budget, null)
        val spinner    = dialogView.findViewById<Spinner>(R.id.spinnerBudgetCategory)
        val etLimit    = dialogView.findViewById<EditText>(R.id.etBudgetLimit)
        val etMin      = dialogView.findViewById<EditText>(R.id.etMinGoal)
        val etMax      = dialogView.findViewById<EditText>(R.id.etMaxGoal)

        val names = cats.map { it.name }
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)

        existing?.let { bs ->
            val idx = cats.indexOfFirst { it.id == bs.budget.categoryId }
            if (idx >= 0) spinner.setSelection(idx)
            etLimit.setText(bs.budget.monthlyLimit.toString())
            etMin.setText(bs.budget.minimumGoal.toString())
            etMax.setText(bs.budget.maximumGoal.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing != null) "Edit Budget" else "Add Budget")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val cat   = cats[spinner.selectedItemPosition]
                val limit = etLimit.text.toString().toDoubleOrNull() ?: 0.0
                val min   = etMin.text.toString().toDoubleOrNull()   ?: 0.0
                val max   = etMax.text.toString().toDoubleOrNull()   ?: 0.0
                viewModel.saveBudget(cat.id, limit, min, max)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(bs: BudgetStatus) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Budget")
            .setMessage("Delete budget for ${bs.category?.name}?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteBudget(bs.budget) }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
