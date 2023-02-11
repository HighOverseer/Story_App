package com.lajar.mystoryapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.lajar.mystoryapp.data.local.preference.UserPreference
import com.lajar.mystoryapp.data.remote.ApiClient
import com.lajar.mystoryapp.data.remote.response.LoginResponse
import com.lajar.mystoryapp.data.remote.response.Responses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.awaitResponse

class UserRepository private constructor(dataStore: DataStore<Preferences>) {
    private val userPreference = UserPreference.getInstance(dataStore)
    private val apiService = ApiClient.getApiService(dataStore)

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Response<Responses>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(name, email, password).awaitResponse()
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(Event(e.message.toString()))
            }
        }

    suspend fun login(email: String, password: String): Result<Response<LoginResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val loginResponse = apiService.login(email, password).awaitResponse()
                Result.Success(loginResponse)
            } catch (e: Exception) {
                Result.Error(Event(e.message.toString()))
            }
        }

    suspend fun getToken(): String {
        return userPreference.getToken()
    }

    suspend fun saveToken(token: String) {
        userPreference.saveToken(token)
    }

    suspend fun deleteToken() {
        userPreference.deleteToken()
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(dataStore: DataStore<Preferences>): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(dataStore)
            }.also {
                INSTANCE = it
            }

        }
    }
}