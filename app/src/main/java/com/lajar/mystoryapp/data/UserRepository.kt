package com.lajar.mystoryapp.data

import com.lajar.mystoryapp.Helper.wrapEspressoIdlingResource
import com.lajar.mystoryapp.data.local.preference.UserPreference
import com.lajar.mystoryapp.data.remote.ApiService
import com.lajar.mystoryapp.data.remote.response.LoginResponse
import com.lajar.mystoryapp.data.remote.response.Responses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.awaitResponse

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

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
            wrapEspressoIdlingResource {
                try {
                    val loginResponse = apiService.login(email, password).awaitResponse()
                    Result.Success(loginResponse)
                } catch (e: Exception) {
                    Result.Error(Event(e.message.toString()))
                }
            }
        }

    suspend fun getToken(): String {
        return userPreference.getToken()
    }

    suspend fun saveToken(token: String) {
        userPreference.saveToken(token)
    }

    suspend fun deleteToken() {
        wrapEspressoIdlingResource { userPreference.deleteToken() }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(userPreference: UserPreference, apiService: ApiService): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(userPreference, apiService)
            }.also {
                INSTANCE = it
            }

        }
    }
}