package com.flowfund.app.ui.badges

import androidx.lifecycle.ViewModel
import com.flowfund.app.data.repository.FlowFundRepository

class BadgeViewModel(
    private val repo: FlowFundRepository,
    private val userId: Long
) : ViewModel() {
    val badges = repo.getAllBadges(userId)
}
