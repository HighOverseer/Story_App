package com.lajar.mystoryapp.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.lajar.mystoryapp.data.StoryRepository
import com.lajar.mystoryapp.data.UserRepository
import com.lajar.mystoryapp.data.local.database.StoryDatabase
import com.lajar.mystoryapp.data.local.preference.UserPreference
import com.lajar.mystoryapp.data.remote.ApiClient


object Injection {
    fun provideStoryRepository(
        application: Application,
        dataStore: DataStore<Preferences>
    ): StoryRepository {
        val storyDatabase = StoryDatabase.getInstance(application)
        val apiService = ApiClient.getApiService(dataStore)
        return StoryRepository.getInstance(storyDatabase, apiService)
    }

    fun provideUserRepository(dataStore: DataStore<Preferences>): UserRepository {
        val userPreferences = UserPreference.getInstance(dataStore)
        val apiService = ApiClient.getApiService(dataStore)
        return UserRepository.getInstance(userPreferences, apiService)
    }
}