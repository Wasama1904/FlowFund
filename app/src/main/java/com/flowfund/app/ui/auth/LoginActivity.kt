package com.flowfund.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.flowfund.app.databinding.ActivityLoginBinding
import com.flowfund.app.ui.MainActivity
import com.flowfund.app.utils.SessionManager
import com.flowfund.app.utils.ViewModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login if session exists
        if (SessionManager.isLoggedIn(this)) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, ViewModelFactory(this))[AuthViewModel::class.java]

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> showLoading(true)
                is AuthResult.Success -> {
                    showLoading(false)
                    SessionManager.saveSession(this, result.user.id, result.user.email)
                    goToMain()
                }
                is AuthResult.Error -> {
                    showLoading(false)
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAction.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (isLoginMode) {
                viewModel.login(email, password)
            } else {
                val username = binding.etUsername.text.toString()
                val confirm = binding.etConfirmPassword.text.toString()
                viewModel.register(username, email, password, confirm)
            }
        }

        binding.tvToggleMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI()
        }
    }

    private fun updateUI() {
        if (isLoginMode) {
            binding.tvTitle.text = "Welcome Back"
            binding.tvSubtitle.text = "Sign in to FlowFund"
            binding.tilUsername.visibility = View.GONE
            binding.tilConfirmPassword.visibility = View.GONE
            binding.btnAction.text = "Login"
            binding.tvToggleMode.text = "Don't have an account? Register"
        } else {
            binding.tvTitle.text = "Create Account"
            binding.tvSubtitle.text = "Join FlowFund today"
            binding.tilUsername.visibility = View.VISIBLE
            binding.tilConfirmPassword.visibility = View.VISIBLE
            binding.btnAction.text = "Register"
            binding.tvToggleMode.text = "Already have an account? Login"
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnAction.isEnabled = !loading
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
