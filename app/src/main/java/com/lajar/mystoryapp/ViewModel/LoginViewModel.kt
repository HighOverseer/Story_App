package com.lajar.mystoryapp.ViewModel

import androidx.lifecycle.*
import com.lajar.mystoryapp.Helper.wrapEspressoIdlingResource
import com.lajar.mystoryapp.data.Event
import com.lajar.mystoryapp.data.UserRepository
import com.lajar.mystoryapp.data.Result
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginResponse = MutableLiveData<Result<Event<String>>>()
    val loginResponse: LiveData<Result<Event<String>>> = _loginResponse


    fun login(email: String, password: String) = viewModelScope.launch(IO) {
        _loginResponse.postValue(Result.Loading)
        when (val result = userRepository.login(email, password)) {
            is Result.Success -> {
                val response = result.data
                if (response.isSuccessful && response.body() != null) {
                    val loginResult = response.body()?.loginResult
                    saveToken(loginResult?.token ?: "")
                    val resultSuccess = Result.Success(Event(response.body()?.message ?: ""))
                    _loginResponse.postValue(resultSuccess)
                } else {
                    val resultUnsuccessful = Result.Error(Event(response.message()))
                    _loginResponse.postValue(resultUnsuccessful)
                }
            }
            is Result.Error -> _loginResponse.postValue(result)
            else -> {}
        }
    }

    private fun saveToken(token: String) {
        viewModelScope.launch {
            withContext(IO) {
                userRepository.saveToken(token)
            }
        }
    }

}