package com.lajar.mystoryapp.ViewModel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajar.mystoryapp.Model.Story
import com.lajar.mystoryapp.data.Event
import com.lajar.mystoryapp.data.StoryRepository
import com.lajar.mystoryapp.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.lajar.mystoryapp.data.Result

class ListViewModel(dataStore: DataStore<Preferences>) : ViewModel() {
    private val userRepository = UserRepository.getInstance(dataStore)
    private val storyRepository = StoryRepository.getInstance(dataStore)

    private val _listStories = MutableLiveData<Result<List<Story>>>()
    val listStories: LiveData<Result<List<Story>>> = _listStories

    fun getStories() =
        viewModelScope.launch {
            _listStories.postValue(Result.Loading)
            when (val result = storyRepository.getStories()) {
                is Result.Success -> {
                    val response = result.data
                    if (response.isSuccessful && response.body() != null && response.body()?.error == false) {
                        val resultSuccess = Result.Success(response.body()?.listStory ?: listOf())
                        _listStories.postValue(resultSuccess)
                    } else {
                        val resultUnsuccessful = Result.Error(Event(response.message()))
                        _listStories.postValue(resultUnsuccessful)
                    }
                }
                is Result.Error -> _listStories.postValue(result)
                else -> {}
            }
        }

    fun deleteToken() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.deleteToken()
        }
    }

    init {
        getStories()
    }
}