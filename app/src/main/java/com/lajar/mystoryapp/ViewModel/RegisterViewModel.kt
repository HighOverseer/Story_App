package com.lajar.mystoryapp.ViewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajar.mystoryapp.data.Event
import com.lajar.mystoryapp.data.UserRepository
import kotlinx.coroutines.launch
import com.lajar.mystoryapp.data.Result

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _registerResponse = MutableLiveData<Result<Event<String>>>()
    val registerResponse: LiveData<Result<Event<String>>> = _registerResponse

    fun register(name: String, email: String, password: String) = viewModelScope.launch {
        _registerResponse.postValue(Result.Loading)
        when (val result = userRepository.register(name, email, password)) {
            is Result.Success -> {
                val response = result.data
                if (response.isSuccessful && response.body() != null) {
                    val resultSuccess = Result.Success(Event(response.body()?.message ?: ""))
                    _registerResponse.postValue(resultSuccess)
                } else {
                    val resultUnsuccessful = Result.Error(Event(response.message()))
                    _registerResponse.postValue(resultUnsuccessful)
                }
            }
            is Result.Error -> {
                _registerResponse.postValue(result)
            }
            else -> {

            }

        }
    }

}


