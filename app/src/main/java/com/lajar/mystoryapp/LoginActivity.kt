package com.lajar.mystoryapp

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.lajar.mystoryapp.ViewModel.LoginViewModel
import com.lajar.mystoryapp.ViewModel.ViewModelFactory
import com.lajar.mystoryapp.data.Event
import com.lajar.mystoryapp.data.Result
import com.lajar.mystoryapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_info")
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = obtainViewModel(this, dataStore)
        setupView()

        loginViewModel.loginResponse.observe(this) { result ->
            checkResult(result)
        }

        binding.tvLoginToRegister.setOnClickListener {
            toRegisterActivity()
        }

        binding.btnLogin.setOnClickListener {
            login()
        }

    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        binding.ivLogoLogin.transitionName = null
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun obtainViewModel(
        activity: AppCompatActivity,
        dataStore: DataStore<Preferences>
    ): LoginViewModel {
        val factory = ViewModelFactory.getInstance(dataStore)
        return ViewModelProvider(activity, factory)[LoginViewModel::class.java]
    }

    private fun toRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun login() {
        binding.apply {
            val email = edLoginEmail.text.toString()
            val password = edLoginPassword.text.toString()
            when {
                email.isEmpty() || email.isBlank() || password.isEmpty() || password.isBlank() -> {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    loginViewModel.login(email, password)
                }
            }
        }
    }

    private fun checkResult(result: Result<Event<String>>) {
        binding.apply {
            when (result) {
                is Result.Loading -> {
                    btnLogin.isEnabled = false
                    tvLoginToRegister.isEnabled = false
                    pbLogin.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    val messageEvent = result.data
                    val intent = Intent(this@LoginActivity, ListActivity::class.java)
                    pbLogin.visibility = View.GONE
                    showToast(messageEvent)
                    startActivity(intent)
                    finish()
                }
                is Result.Error -> {
                    pbLogin.visibility = View.GONE
                    btnLogin.isEnabled = true
                    tvLoginToRegister.isEnabled = true
                    val messageEvent = result.error
                    showToast(messageEvent)
                }
            }
        }

    }

    private fun showToast(messageEvent: Event<String>) {
        val message = messageEvent.getContentIfNotHandled()
        if (message != null) {
            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
