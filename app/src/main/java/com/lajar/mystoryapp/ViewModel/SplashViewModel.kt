package com.lajar.mystoryapp.ViewModel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajar.mystoryapp.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashViewModel(dataStore: DataStore<Preferences>) : ViewModel() {
    private val userRepository = UserRepository.getInstance(dataStore)

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