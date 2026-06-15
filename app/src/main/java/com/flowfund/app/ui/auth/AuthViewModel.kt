package com.flowfund.app.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfund.app.data.entities.User
import com.flowfund.app.data.repository.FlowFundRepository
import com.flowfund.app.utils.SessionManager
import kotlinx.coroutines.launch

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthViewModel(private val repo: FlowFundRepository) : ViewModel() {
    val authResult = MutableLiveData<AuthResult>()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            authResult.value = AuthResult.Error("Please fill in all fields")
            return
        }
        authResult.value = AuthResult.Loading
        viewModelScope.launch {
            val hash = SessionManager.hashPassword(password)
            val user = repo.login(email.trim(), hash)
            if (user != null) {
                authResult.postValue(AuthResult.Success(user))
            } else {
                authResult.postValue(AuthResult.Error("Invalid email or password"))
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        when {
            username.isBlank() || email.isBlank() || password.isBlank() ->
                authResult.value = AuthResult.Error("Please fill in all fields")
            password != confirmPassword ->
                authResult.value = AuthResult.Error("Passwords do not match")
            password.length < 6 ->
                authResult.value = AuthResult.Error("Password must be at least 6 characters")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                authResult.value = AuthResult.Error("Please enter a valid email")
            else -> {
                authResult.value = AuthResult.Loading
                viewModelScope.launch {
                    val existing = repo.findUserByEmail(email.trim())
                    if (existing != null) {
                        authResult.postValue(AuthResult.Error("Email already registered"))
                        return@launch
                    }
                    val user = User(
                        username = username.trim(),
                        email = email.trim(),
                        passwordHash = SessionManager.hashPassword(password)
                    )
                    val id = repo.register(user)
                    authResult.postValue(AuthResult.Success(user.copy(id = id)))
                }
            }
        }
    }
}
