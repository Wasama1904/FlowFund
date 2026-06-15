package com.flowfund.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.flowfund.app.databinding.FragmentSettingsBinding
import com.flowfund.app.utils.CurrencyHelper

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        // ─── Dark Mode ─────────────────────────────────────────────────────
        val currentMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        binding.switchDarkMode.isChecked = currentMode == AppCompatDelegate.MODE_NIGHT_YES
        updateThemeDesc(currentMode)

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val newMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            prefs.edit().putInt("theme_mode", newMode).apply()
            AppCompatDelegate.setDefaultNightMode(newMode)
            updateThemeDesc(newMode)
        } //

        // Badges info expand/collapse with animation - MOVED OUTSIDE SWITCH LISTENER
        binding.llBadgesHeader.setOnClickListener {
            if (binding.llBadgesContent.visibility == View.VISIBLE) {
                binding.llBadgesContent.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction {
                        binding.llBadgesContent.visibility = View.GONE
                        binding.llBadgesContent.alpha = 1f
                    }
                    .start()
                binding.ivBadgesExpand.animate().rotation(0f).setDuration(150).start()
            } else {
                binding.llBadgesContent.visibility = View.VISIBLE
                binding.llBadgesContent.alpha = 0f
                binding.llBadgesContent.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
                binding.ivBadgesExpand.animate().rotation(180f).setDuration(150).start()
            }
        }

        // ─── Currency ──────────────────────────────────────────────────────
        val currencies = CurrencyHelper.getCurrencyList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter

        val savedCurrency = CurrencyHelper.getSavedCurrency(requireContext())
        binding.spinnerCurrency.setSelection(currencies.indexOf(savedCurrency))
        binding.tvCurrencyDesc.text = CurrencyHelper.getCurrencyDisplay(savedCurrency)

        binding.spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = currencies[position]
                prefs.edit().putString("currency", selected).apply()
                binding.tvCurrencyDesc.text = CurrencyHelper.getCurrencyDisplay(selected)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateThemeDesc(mode: Int) {
        binding.tvThemeDesc.text = when (mode) {
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark mode enabled"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light mode enabled"
            else -> "Follow system setting"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}