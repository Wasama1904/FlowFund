package com.flowfund.app.ui.badges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.flowfund.app.databinding.FragmentBadgeBinding
import com.flowfund.app.utils.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class BadgeFragment : Fragment() {
    private var _binding: FragmentBadgeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BadgeViewModel by viewModels(
        factoryProducer = { ViewModelFactory(requireContext()) }
    )

    private lateinit var adapter: BadgeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBadgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BadgeAdapter()
        binding.rvBadges.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvBadges.adapter = adapter

        // Collect Flow from Room
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.badges.collectLatest { badgeList ->
                adapter.submitList(badgeList)
                binding.tvEmpty.visibility = if (badgeList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}