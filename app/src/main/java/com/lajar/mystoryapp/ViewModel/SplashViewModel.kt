package com.lajar.mystoryapp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajar.mystoryapp.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashViewModel(private val userRepository: UserRepository) : ViewModel() {

    private var token: String = ""
    fun getToken() = token
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                token = userRepository.getToken()
            }
        }
    }
}