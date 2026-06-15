package com.flowfund.app.ui.goals

import android.app.AlertDialog
import android.app.DatePickerDialog
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
import com.flowfund.app.data.entities.Goal
import com.flowfund.app.data.repository.FlowFundRepository
import com.flowfund.app.databinding.FragmentGoalsBinding
import com.flowfund.app.utils.DateUtils
import com.flowfund.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

// ─── ViewModel ───────────────────────────────────────────────────────────────
class GoalViewModel(private val repo: FlowFundRepository, val userId: Long) : ViewModel() {
    val goals: LiveData<List<Goal>> = repo.getGoals(userId)
    val saveResult = MutableLiveData<Result<Unit>>()

    fun save(id: Long?, title: String, target: Double, current: Double, deadline: Long?) {
        if (title.isBlank() || target <= 0) { saveResult.value = Result.failure(Exception("Fill all fields")); return }
        viewModelScope.launch {
            try {
                val g = Goal(id = id ?: 0, userId = userId, title = title.trim(),
                    targetAmount = target, currentAmount = current, deadline = deadline,
                    isCompleted = current >= target)
                if (id != null && id > 0) repo.updateGoal(g) else repo.insertGoal(g)
                saveResult.postValue(Result.success(Unit))
            } catch (e: Exception) { saveResult.postValue(Result.failure(e)) }
        }
    }

    fun delete(g: Goal) = viewModelScope.launch { repo.deleteGoal(g) }
}

// ─── Adapter ─────────────────────────────────────────────────────────────────
class GoalAdapter(
    private val onEdit: (Goal) -> Unit,
    private val onDelete: (Goal) -> Unit
) : ListAdapter<Goal, GoalAdapter.VH>(DIFF) {
    private val currency = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView     = v.findViewById(R.id.tvGoalTitle)
        val tvProgress: TextView  = v.findViewById(R.id.tvGoalProgress)
        val tvDeadline: TextView  = v.findViewById(R.id.tvGoalDeadline)
        val progressBar: ProgressBar = v.findViewById(R.id.goalProgress)
        val tvBadge: TextView     = v.findViewById(R.id.tvGoalBadge)
        val btnEdit: View         = v.findViewById(R.id.btnEditGoal)
        val btnDelete: View       = v.findViewById(R.id.btnDeleteGoal)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_goal, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val g = getItem(pos)
        val pct = if (g.targetAmount > 0) (g.currentAmount / g.targetAmount * 100).toInt().coerceIn(0, 100) else 0
        h.tvTitle.text    = g.title
        h.tvProgress.text = "${currency.format(g.currentAmount)} / ${currency.format(g.targetAmount)} ($pct%)"
        h.progressBar.progress = pct
        h.tvDeadline.text = if (g.deadline != null) "Deadline: ${DateUtils.format(g.deadline)}" else "No deadline"
        h.tvBadge.visibility = if (g.isCompleted) View.VISIBLE else View.GONE
        h.tvBadge.text = "🏆 Goal Achieved!"
        h.btnEdit.setOnClickListener { onEdit(g) }
        h.btnDelete.setOnClickListener { onDelete(g) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Goal>() {
            override fun areItemsTheSame(a: Goal, b: Goal) = a.id == b.id
            override fun areContentsTheSame(a: Goal, b: Goal) = a == b
        }
    }
}

// ─── Fragment ─────────────────────────────────────────────────────────────────
class GoalsFragment : Fragment() {
    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: GoalViewModel
    private lateinit var adapter: GoalAdapter
    private var pickedDeadline: Long? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = SessionManager.getUserId(requireContext())
        val repo = FlowFundRepository(requireContext())
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(c: Class<T>) = GoalViewModel(repo, userId) as T
        })[GoalViewModel::class.java]

        adapter = GoalAdapter(onEdit = { showDialog(it) }, onDelete = { confirmDelete(it) })
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGoals.adapter = adapter

        viewModel.goals.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddGoal.setOnClickListener { showDialog(null) }
    }

    private fun showDialog(existing: Goal?) {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_goal, null)
        val etTitle   = v.findViewById<EditText>(R.id.etGoalTitle)
        val etTarget  = v.findViewById<EditText>(R.id.etGoalTarget)
        val etCurrent = v.findViewById<EditText>(R.id.etGoalCurrent)
        val btnDeadline = v.findViewById<Button>(R.id.btnGoalDeadline)

        pickedDeadline = existing?.deadline
        existing?.let {
            etTitle.setText(it.title)
            etTarget.setText(it.targetAmount.toString())
            etCurrent.setText(it.currentAmount.toString())
            btnDeadline.text = if (it.deadline != null) DateUtils.format(it.deadline) else "Set Deadline (optional)"
        }

        btnDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                pickedDeadline = cal.timeInMillis
                btnDeadline.text = DateUtils.format(cal.timeInMillis)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing != null) "Edit Goal" else "Add Goal")
            .setView(v)
            .setPositiveButton("Save") { _, _ ->
                val target  = etTarget.text.toString().toDoubleOrNull() ?: 0.0
                val current = etCurrent.text.toString().toDoubleOrNull() ?: 0.0
                viewModel.save(existing?.id, etTitle.text.toString(), target, current, pickedDeadline)
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun confirmDelete(g: Goal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Goal").setMessage("Delete '${g.title}'?")
            .setPositiveButton("Delete") { _, _ -> viewModel.delete(g) }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
