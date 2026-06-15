package com.flowfund.app.ui.dashboard

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.flowfund.app.R
import com.flowfund.app.databinding.FragmentDashboardBinding
import com.flowfund.app.ui.MainActivity
import com.flowfund.app.utils.CurrencyHelper
import com.flowfund.app.utils.ViewModelFactory
import com.flowfund.app.utils.AdviceProvider
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.NumberFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel

    // Dynamic currency format - reads from SharedPreferences
    private val currencyFormat: NumberFormat
        get() = CurrencyHelper.getFormatter(requireContext())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_dashboard, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewModel = ViewModelProvider(
            this, ViewModelFactory(requireContext())
        )[DashboardViewModel::class.java]

        setupPieChart()
        setupBarChart()
        observeData()

        binding.btnPickMonth.setOnClickListener { showMonthPicker() }
        binding.btnLogout.setOnClickListener { (activity as? MainActivity)?.logout() }
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning from Settings to apply new currency
        viewModel.loadData()
    }

    // ─── Chart Colors - Theme aware ──────────────────────────────────────────

    private fun getChartColors(): List<Int> {
        return listOf(
            ContextCompat.getColor(requireContext(), R.color.primary),
            ContextCompat.getColor(requireContext(), R.color.secondary),
            ContextCompat.getColor(requireContext(), R.color.primary_dark),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#F44336")
        )
    }

    // ─── Pie Chart ───────────────────────────────────────────────────────────

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 58f
            setUsePercentValues(true)
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
        }
    }

    // ─── Bar Chart ───────────────────────────────────────────────────────────

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setFitBars(true)
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
        }
    }

    // ─── Observers ───────────────────────────────────────────────────────────

    private fun observeData() {

        viewModel.getMonthlyAdvice().observe(viewLifecycleOwner) { advice ->
            binding.tvAdvice.text = advice
        }

        viewModel.dashboardData.observe(viewLifecycleOwner) { data ->
            binding.tvBalance.text = currencyFormat.format(data.balance)
            binding.tvIncome.text   = currencyFormat.format(data.totalIncome)
            binding.tvExpenses.text = currencyFormat.format(data.totalExpenses)

            if (data.categoryBreakdown.isEmpty()) {
                binding.pieChart.visibility      = View.GONE
                binding.tvNoCategoryData.visibility = View.VISIBLE
            } else {
                binding.pieChart.visibility         = View.VISIBLE
                binding.tvNoCategoryData.visibility = View.GONE
                val entries = data.categoryBreakdown.map { (name, amt) ->
                    PieEntry(amt.toFloat(), name)
                }
                val ds = PieDataSet(entries, "Expenses by Category").apply {
                    colors = getChartColors().take(entries.size)
                    sliceSpace = 2f
                    valueFormatter = PercentFormatter(binding.pieChart)
                    valueTextSize = 11f
                }
                binding.pieChart.data = PieData(ds)
                binding.pieChart.invalidate()
            }
        }

        viewModel.barChartData.observe(viewLifecycleOwner) { chartData ->
            if (chartData.labels.isEmpty()) {
                binding.barChart.visibility  = View.GONE
                binding.tvNoBarData.visibility = View.VISIBLE
                return@observe
            }

            binding.barChart.visibility   = View.VISIBLE
            binding.tvNoBarData.visibility = View.GONE

            val spentEntries  = chartData.spent.mapIndexed  { i, v -> BarEntry(i.toFloat(), v) }
            val spentDs = BarDataSet(spentEntries, "Spent").apply {
                colors = getChartColors().take(spentEntries.size)
                valueTextSize = 10f
            }

            binding.barChart.xAxis.valueFormatter =
                IndexAxisValueFormatter(chartData.labels.toTypedArray())
            binding.barChart.xAxis.labelCount = chartData.labels.size

            binding.barChart.data = BarData(spentDs).apply { barWidth = 0.6f }

            binding.barChart.axisLeft.removeAllLimitLines()
            chartData.labels.forEachIndexed { i, label ->
                val minVal = chartData.minGoals.getOrElse(i) { 0f }
                val maxVal = chartData.maxGoals.getOrElse(i) { 0f }

                if (minVal > 0) {
                    LimitLine(minVal, "Min $label").apply {
                        lineColor = Color.parseColor("#4CAF50")
                        lineWidth = 1.5f
                        textColor = Color.parseColor("#4CAF50")
                        textSize  = 9f
                        enableDashedLine(10f, 5f, 0f)
                    }.also { binding.barChart.axisLeft.addLimitLine(it) }
                }
                if (maxVal > 0) {
                    LimitLine(maxVal, "Max $label").apply {
                        lineColor = Color.parseColor("#F44336")
                        lineWidth = 1.5f
                        textColor = Color.parseColor("#F44336")
                        textSize  = 9f
                        enableDashedLine(10f, 5f, 0f)
                    }.also { binding.barChart.axisLeft.addLimitLine(it) }
                }
            }

            binding.barChart.invalidate()
        }

        viewModel.goalStatusList.observe(viewLifecycleOwner) { list ->
            binding.llGoalsStatus.removeAllViews()

            if (list.isEmpty()) {
                binding.tvNoGoalsStatus.visibility = View.VISIBLE
                return@observe
            }
            binding.tvNoGoalsStatus.visibility = View.GONE

            list.forEach { item -> binding.llGoalsStatus.addView(buildGoalCard(item)) }
        }
    }

    // ─── Goal status card builder ─────────────────────────────────────────────

    private fun buildGoalCard(item: CategoryGoalStatus): View {
        val ctx = requireContext()

        val (bgColor, statusText, progressColor) = when (item.status) {
            GoalStatus.ON_TRACK  -> Triple("#E8F5E9", "✅ On Track",  "#4CAF50")
            GoalStatus.OVER_MAX  -> Triple("#FFEBEE", "🔴 Over Budget", "#F44336")
            GoalStatus.UNDER_MIN -> Triple("#FFF8E1", "⚠ Under Minimum", "#FF9800")
            GoalStatus.NO_GOAL   -> Triple("#F3E5F5", "ℹ No Goal Set",  "#9C27B0")
        }

        val progress = if (item.maxGoal > 0)
            ((item.spent / item.maxGoal) * 100).toInt().coerceIn(0, 100)
        else 50

        val card = CardView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 12.dpToPx(ctx) }
            radius = 12.dpToPx(ctx).toFloat()
            cardElevation = 3.dpToPx(ctx).toFloat()
            setCardBackgroundColor(Color.parseColor(bgColor))
        }

        val inner = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dpToPx(ctx), 16.dpToPx(ctx), 16.dpToPx(ctx), 16.dpToPx(ctx))
        }

        inner.addView(LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(TextView(ctx).apply {
                text = item.categoryName
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(ctx).apply {
                text = statusText
                textSize = 13f
            })
        })

        inner.addView(TextView(ctx).apply {
            text = "Spent: ${currencyFormat.format(item.spent)}"
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(0, 6.dpToPx(ctx), 0, 2.dpToPx(ctx))
        })

        if (item.minGoal > 0 || item.maxGoal > 0) {
            inner.addView(TextView(ctx).apply {
                text = "Goal: ${currencyFormat.format(item.minGoal)} – ${currencyFormat.format(item.maxGoal)}"
                textSize = 12f
                setTextColor(Color.GRAY)
                setPadding(0, 0, 0, 8.dpToPx(ctx))
            })
        }

        inner.addView(ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 18.dpToPx(ctx)
            )
            max = 100
            this.progress = progress
            progressDrawable.setColorFilter(
                Color.parseColor(progressColor), android.graphics.PorterDuff.Mode.SRC_IN
            )
        })

        inner.addView(TextView(ctx).apply {
            text = if (item.maxGoal > 0) "${progress}% of max goal used" else ""
            textSize = 11f
            setTextColor(Color.GRAY)
            setPadding(0, 4.dpToPx(ctx), 0, 0)
        })

        card.addView(inner)
        return card
    }

    private fun Int.dpToPx(ctx: android.content.Context): Int =
        (this * ctx.resources.displayMetrics.density).toInt()

    // ─── Month picker ─────────────────────────────────────────────────────────

    private fun showMonthPicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, _ ->
            val m = month + 1
            viewModel.setMonthYear(m, year)
            binding.tvMonthYear.text = String.format("%02d / %d", m, year)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}