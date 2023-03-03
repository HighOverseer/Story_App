package com.lajar.mystoryapp

import android.content.Context
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
import com.lajar.mystoryapp.ViewModel.RegisterViewModel
import com.lajar.mystoryapp.ViewModel.ViewModelFactory
import com.lajar.mystoryapp.data.Event
import com.lajar.mystoryapp.databinding.ActivityRegisterBinding
import com.lajar.mystoryapp.data.Result

class RegisterActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_info")
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()

        registerViewModel = obtainViewModel(this, dataStore)

        registerViewModel.registerResponse.observe(this) { result ->
            checkResult(result)
        }

        binding.btnRegister.setOnClickListener {
            register()
        }

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
    ): RegisterViewModel {
        val factory = ViewModelFactory.getInstance(activity.application, dataStore)
        return ViewModelProvider(activity, factory)[RegisterViewModel::class.java]
    }

    private fun register() {
        binding.apply {
            val name = edRegisterName.text.toString()
            val email = edRegisterEmail.text.toString()
            val password = edRegisterPassword.text.toString()
            when {
                name.isEmpty() || name.isBlank() || email.isEmpty() || email.isBlank() || password.isEmpty() || password.isBlank() -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.login_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    registerViewModel.register(name, email, password)
                }
            }

        }
    }

    private fun checkResult(result: Result<Event<String>>) {
        binding.apply {
            when (result) {
                is Result.Loading -> {
                    btnRegister.isEnabled = false
                    pbRegister.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    val messageEvent = result.data
                    pbRegister.visibility = View.GONE
                    showToast(messageEvent)
                    finish()
                }
                is Result.Error -> {
                    pbRegister.visibility = View.GONE
                    btnRegister.isEnabled = true
                    val messageEvent = result.error
                    showToast(messageEvent)
                }
            }
        }

    }

    private fun showToast(messageEvent: Event<String>) {
        val message = messageEvent.getContentIfNotHandled()
        if (message != null) {
            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}