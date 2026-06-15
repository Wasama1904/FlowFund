package com.flowfund.app.ui.categories

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flowfund.app.R
import com.flowfund.app.data.entities.Category
import com.flowfund.app.data.repository.FlowFundRepository
import com.flowfund.app.databinding.FragmentCategoriesBinding
import com.flowfund.app.utils.ViewModelFactory
import kotlinx.coroutines.launch

// ─── ViewModel ───────────────────────────────────────────────────────────────
class CategoriesViewModel(private val repo: FlowFundRepository, val userId: Long) : ViewModel() {
    val categories: LiveData<List<Category>> = repo.getCategories(userId)
    val saveResult = MutableLiveData<Result<Unit>>()

    fun save(id: Long?, name: String, color: String) {
        if (name.isBlank()) { saveResult.value = Result.failure(Exception("Name required")); return }
        viewModelScope.launch {
            try {
                val cat = Category(id = id ?: 0, userId = userId, name = name.trim(), colorHex = color)
                if (id != null && id > 0) repo.updateCategory(cat) else repo.insertCategory(cat)
                saveResult.postValue(Result.success(Unit))
            } catch (e: Exception) { saveResult.postValue(Result.failure(e)) }
        }
    }
    fun delete(cat: Category) = viewModelScope.launch { repo.deleteCategory(cat) }
}

// ─── Adapter ─────────────────────────────────────────────────────────────────
class CategoriesAdapter(
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoriesAdapter.VH>(DIFF) {
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView  = v.findViewById(R.id.tvCatName)
        val vColor: View      = v.findViewById(R.id.vCatColor)
        val btnEdit: View     = v.findViewById(R.id.btnEditCat)
        val btnDelete: View   = v.findViewById(R.id.btnDeleteCat)
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_category, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) {
        val cat = getItem(pos)
        h.tvName.text = cat.name
        try { h.vColor.setBackgroundColor(android.graphics.Color.parseColor(cat.colorHex)) } catch (_: Exception) {}
        h.btnEdit.setOnClickListener { onEdit(cat) }
        h.btnDelete.setOnClickListener { onDelete(cat) }
    }
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(a: Category, b: Category) = a.id == b.id
            override fun areContentsTheSame(a: Category, b: Category) = a == b
        }
    }
}

// ─── Fragment ─────────────────────────────────────────────────────────────────
class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var adapter: CategoriesAdapter

    private val colorOptions = listOf(
        "#6200EE","#03DAC5","#FF5722","#4CAF50",
        "#2196F3","#FF9800","#9C27B0","#F44336","#795548","#607D8B"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory(requireContext()))[CategoriesViewModel::class.java]

        adapter = CategoriesAdapter(onEdit = { showDialog(it) }, onDelete = { confirmDelete(it) })
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddCategory.setOnClickListener { showDialog(null) }
    }

    private fun showDialog(existing: Category?) {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)
        val etName = v.findViewById<EditText>(R.id.etCategoryName)
        val spinner = v.findViewById<Spinner>(R.id.spinnerColor)
        existing?.let { etName.setText(it.name) }
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, colorOptions)
        existing?.let { spinner.setSelection(colorOptions.indexOf(it.colorHex).coerceAtLeast(0)) }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing != null) "Edit Category" else "Add Category")
            .setView(v)
            .setPositiveButton("Save") { _, _ ->
                viewModel.save(existing?.id, etName.text.toString(), colorOptions[spinner.selectedItemPosition])
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun confirmDelete(cat: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Delete '${cat.name}'? All associated budgets will also be deleted.")
            .setPositiveButton("Delete") { _, _ -> viewModel.delete(cat) }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
