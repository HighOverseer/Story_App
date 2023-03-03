package com.lajar.mystoryapp.ViewModel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lajar.mystoryapp.Helper.NetworkConnectivityObserver
import com.lajar.mystoryapp.di.Injection

class ViewModelFactory private constructor(private val application: Application, private val dataStore: DataStore<Preferences>) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel(Injection.provideUserRepository(dataStore)) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(Injection.provideUserRepository(dataStore)) as T
        } else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(Injection.provideUserRepository(dataStore)) as T
        } else if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            return ListViewModel(
                Injection.provideStoryRepository(application, dataStore),
                Injection.provideUserRepository(dataStore),
                NetworkConnectivityObserver.getInstance(application)
            ) as T
        } else if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel() as T
        } else if (modelClass.isAssignableFrom(AddViewModel::class.java)) {
            return AddViewModel(Injection.provideStoryRepository(application, dataStore)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class : ${modelClass.name}")
    }

    companion object{
        @Volatile
        private var INSTANCE:ViewModelFactory?=null

        fun getInstance(application: Application, dataStore: DataStore<Preferences>):ViewModelFactory{
            return INSTANCE?: synchronized(ViewModelFactory::class.java){
                val instance = ViewModelFactory(application, dataStore)
                INSTANCE = instance
                instance
            }.also { INSTANCE = it }
        }
    }

}