package com.lajar.mystoryapp.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.lajar.mystoryapp.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiClient {
    @Volatile
    private var okHttpClientInstance: OkHttpClient? = null

    fun getApiService(dataStore: DataStore<Preferences>): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(getOkHttpClient(dataStore))
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    private fun getOkHttpClient(dataStore: DataStore<Preferences>): OkHttpClient {
        return okHttpClientInstance ?: synchronized(this) {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(dataStore))
                .build()
            okHttpClientInstance = okHttpClient
            okHttpClient
        }
    }


}