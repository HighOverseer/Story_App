package com.lajar.mystoryapp.ViewModel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajar.mystoryapp.data.Event
import com.lajar.mystoryapp.data.StoryRepository
import kotlinx.coroutines.launch
import com.lajar.mystoryapp.data.Result
import java.io.File

class AddViewModel(dataStore: DataStore<Preferences>) : ViewModel() {
    private val storyRepository = StoryRepository.getInstance(dataStore)

    private val _currentImage = MutableLiveData<File>()
    val currentImage: LiveData<File> = _currentImage

    private val _addStoryResponse = MutableLiveData<Result<Event<String>>>()
    val addStoryResponse: LiveData<Result<Event<String>>> = _addStoryResponse

    fun setCurrentImage(imageFile: File) {
        _currentImage.value = imageFile
    }

    fun uploadStory(description: String, lat:Float?, lon:Float?) = viewModelScope.launch {
        val imageFile = currentImage.value
        if (imageFile != null) {
            _addStoryResponse.postValue(Result.Loading)
            when (val result = storyRepository.addStory(description, imageFile, lat, lon)) {
                is Result.Success -> {
                    val response = result.data
                    if (response.isSuccessful && response.body()?.error == false) {
                        val resultSuccess = Result.Success(Event(response.body()?.message ?: ""))
                        _addStoryResponse.postValue(resultSuccess)
                    } else {
                        val resultUnSuccessful = Result.Error(Event(response.message().toString()))
                        _addStoryResponse.postValue(resultUnSuccessful)
                    }
                }
                is Result.Error -> _addStoryResponse.postValue(result)
                else -> {}
            }
        }

    }


}