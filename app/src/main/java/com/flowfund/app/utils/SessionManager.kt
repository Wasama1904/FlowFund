package com.flowfund.app.utils

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

object SessionManager {
    private const val PREF_NAME = "flowfund_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"

    // Use plain SharedPreferences — EncryptedSharedPreferences initializes
    // crypto keys on the main thread, causing ANR ("System UI isn't responding").
    // Plain prefs are safe here: userId is not sensitive credential data.
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(context: Context, userId: Long, email: String) {
        prefs(context).edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getUserId(context: Context): Long = prefs(context).getLong(KEY_USER_ID, -1L)

    fun getEmail(context: Context): String? = prefs(context).getString(KEY_EMAIL, null)

    fun isLoggedIn(context: Context): Boolean = getUserId(context) != -1L

    fun clearSession(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
