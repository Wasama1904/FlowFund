package com.flowfund.app.ui.transactions

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.flowfund.app.R
import com.flowfund.app.databinding.FragmentTransactionsBinding
import com.flowfund.app.data.entities.Transaction
import com.flowfund.app.utils.DateUtils
import com.flowfund.app.utils.ViewModelFactory
import java.util.*

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory(requireContext()))[TransactionViewModel::class.java]

        adapter = TransactionAdapter(
            onEdit   = { showAddEditDialog(it) },
            onDelete = { confirmDelete(it) },
            onPhotoClick = { path -> showFullPhoto(path) }
        )
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        viewModel.transactions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.categories.observe(viewLifecycleOwner) { cats ->
            adapter.categories = cats
            adapter.notifyDataSetChanged()
        }

        binding.fabAdd.setOnClickListener { showAddEditDialog(null) }
        binding.btnFromDate.setOnClickListener { pickDate(isFrom = true) }
        binding.btnToDate.setOnClickListener { pickDate(isFrom = false) }

        updateDateLabels()
    }

    private fun updateDateLabels() {
        viewModel.fromDate.observe(viewLifecycleOwner) { binding.btnFromDate.text = "From: ${DateUtils.format(it)}" }
        viewModel.toDate.observe(viewLifecycleOwner)   { binding.btnToDate.text   = "To: ${DateUtils.format(it)}" }
    }

    private fun pickDate(isFrom: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d)
            if (isFrom) viewModel.setPeriod(DateUtils.startOfDay(cal.timeInMillis), viewModel.toDate.value!!)
            else viewModel.setPeriod(viewModel.fromDate.value!!, DateUtils.endOfDay(cal.timeInMillis))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showAddEditDialog(existing: Transaction?) {
        val fragment = AddEditTransactionFragment.newInstance(existing?.id)
        fragment.show(childFragmentManager, "add_edit_tx")
    }

    private fun confirmDelete(tx: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Delete this transaction?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteTransaction(tx) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFullPhoto(path: String) {
        val fragment = PhotoViewFragment.newInstance(path)
        fragment.show(childFragmentManager, "photo_view")
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
