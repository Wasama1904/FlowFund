package com.flowfund.app.ui.badges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flowfund.app.R
import com.flowfund.app.data.entities.Badge
import com.flowfund.app.databinding.ItemBadgeBinding
import java.text.SimpleDateFormat
import java.util.*

class BadgeAdapter : ListAdapter<Badge, BadgeAdapter.BadgeViewHolder>(BadgeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemBadgeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BadgeViewHolder(private val binding: ItemBadgeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(badge: Badge) {
            binding.tvBadgeName.text = badge.name
            binding.tvBadgeDate.text = formatDate(badge.earnedDate)

            // Load icon by name from drawables
            val context = binding.root.context
            val resId = context.resources.getIdentifier(
                badge.iconRes, "drawable", context.packageName
            )
            if (resId != 0) {
                binding.ivBadgeIcon.setImageResource(resId)
            } else {
                binding.ivBadgeIcon.setImageResource(R.drawable.ic_badge_budget) // fallback
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class BadgeDiffCallback : DiffUtil.ItemCallback<Badge>() {
        override fun areItemsTheSame(oldItem: Badge, newItem: Badge): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Badge, newItem: Badge): Boolean {
            return oldItem == newItem
        }
    }
}