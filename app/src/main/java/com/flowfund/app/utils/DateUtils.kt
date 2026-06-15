package com.flowfund.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    fun format(millis: Long): String = displayFormat.format(Date(millis))
    fun formatTime(millis: Long): String = timeFormat.format(Date(millis))
    fun formatDateTime(millis: Long): String = dateTimeFormat.format(Date(millis))

    fun startOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun monthName(month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1) // Prevent rollover
        cal.set(Calendar.MONTH, month - 1)
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    }

    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}
