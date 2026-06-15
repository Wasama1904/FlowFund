package com.flowfund.app.utils

import android.content.Context
import java.text.NumberFormat
import java.util.*

object CurrencyHelper {

    private val currencies = mapOf(
        "ZAR" to Triple("South African Rand", Locale("en", "ZA"), "R"),
        "USD" to Triple("US Dollar", Locale.US, "$"),
        "EUR" to Triple("Euro", Locale.GERMANY, "€"),
        "GBP" to Triple("British Pound", Locale.UK, "£"),
        "AUD" to Triple("Australian Dollar", Locale("en", "AU"), "A$"),
        "CAD" to Triple("Canadian Dollar", Locale.CANADA, "C$"),
        "JPY" to Triple("Japanese Yen", Locale.JAPAN, "¥"),
        "CNY" to Triple("Chinese Yuan", Locale.CHINA, "¥")
    )

    fun getCurrencyList(): List<String> = currencies.keys.toList()

    fun getCurrencyDisplay(code: String): String {
        val info = currencies[code] ?: return code
        return "$code - ${info.first}"
    }

    fun getFormatter(context: Context): NumberFormat {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val code = prefs.getString("currency", "ZAR") ?: "ZAR"
        val locale = currencies[code]?.second ?: Locale("en", "ZA")
        return NumberFormat.getCurrencyInstance(locale)
    }

    fun getSavedCurrency(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("currency", "ZAR") ?: "ZAR"
    }
}