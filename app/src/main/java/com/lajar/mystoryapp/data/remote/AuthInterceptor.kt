package com.lajar.mystoryapp.data.remote


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.lajar.mystoryapp.data.local.preference.UserPreference
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(dataStore: DataStore<Preferences>) : Interceptor {

    private val userPreference = UserPreference.getInstance(dataStore)

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        var token: String
        return runBlocking {
            token = userPreference.getToken()
            if (token.isNotEmpty()) {
                val authorized = original.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(authorized)
            } else {
                chain.proceed(original)
            }
        }
    }
}