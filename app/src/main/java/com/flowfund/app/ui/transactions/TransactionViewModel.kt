package com.flowfund.app.ui.transactions

import androidx.lifecycle.*
import com.flowfund.app.data.entities.Category
import com.flowfund.app.data.entities.Transaction
import com.flowfund.app.data.repository.FlowFundRepository
import com.flowfund.app.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(
    private val repo: FlowFundRepository,
    val userId: Long
) : ViewModel() {

    private val _fromDate = MutableLiveData(DateUtils.startOfMonth(DateUtils.currentMonth(), DateUtils.currentYear()))
    private val _toDate   = MutableLiveData(DateUtils.endOfMonth(DateUtils.currentMonth(), DateUtils.currentYear()))

    val fromDate: LiveData<Long> = _fromDate
    val toDate:   LiveData<Long> = _toDate

    val transactions: LiveData<List<Transaction>> = _fromDate.switchMap { from ->
        _toDate.switchMap { to -> repo.getTransactionsByPeriod(userId, from, to) }
    }

    val categories: LiveData<List<Category>> = repo.getCategories(userId)

    val saveResult = MutableLiveData<Result<Unit>>()

    fun setPeriod(from: Long, to: Long) {
        _fromDate.value = from
        _toDate.value   = to
    }

    fun saveTransaction(
        id: Long?,
        categoryId: Long?,
        amount: Double,
        type: String,
        description: String,
        date: Long,
        startTime: Long?,
        endTime: Long?,
        photoPath: String?
    ) {
        if (amount <= 0) { saveResult.value = Result.failure(Exception("Amount must be greater than 0")); return }
        viewModelScope.launch {
            try {
                val tx = Transaction(
                    id         = id ?: 0,
                    userId     = userId,
                    categoryId = categoryId,
                    amount     = amount,
                    type       = type,
                    description= description,
                    date       = date,
                    startTime  = startTime,
                    endTime    = endTime,
                    photoPath  = photoPath
                )
                if (id != null && id > 0) repo.updateTransaction(tx) else repo.insertTransaction(tx)
                saveResult.postValue(Result.success(Unit))
            } catch (e: Exception) {
                saveResult.postValue(Result.failure(e))
            }
        }
    }

    fun deleteTransaction(tx: Transaction) = viewModelScope.launch { repo.deleteTransaction(tx) }

    suspend fun getTransactionById(id: Long): Transaction? = repo.getTransactionById(id)
}
