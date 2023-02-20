package com.lajar.mystoryapp.ViewModel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.Marker
import com.lajar.mystoryapp.Model.Story
import com.lajar.mystoryapp.data.Event
import com.lajar.mystoryapp.data.StoryRepository
import com.lajar.mystoryapp.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.lajar.mystoryapp.data.Result
import kotlinx.coroutines.withContext

class ListViewModel(dataStore: DataStore<Preferences>) : ViewModel() {
    private val userRepository = UserRepository.getInstance(dataStore)
    private val storyRepository = StoryRepository.getInstance(dataStore)

    private val _listStories = MutableLiveData<Result<List<Story>>>()
    val listStories: LiveData<Result<List<Story>>> = _listStories

    private val _selectedUserStories = MutableLiveData<List<Story>?>()
    val selectedUserStories:LiveData<List<Story>?> =  _selectedUserStories

    private val _isAllMarkerReady = MutableLiveData<Boolean>()
    val isAllMarkerReady:LiveData<Boolean> = _isAllMarkerReady

    fun getStories() =
        viewModelScope.launch {
            _listStories.postValue(Result.Loading)
            when (val result = storyRepository.getStories()) {
                is Result.Success -> {
                    val response = result.data
                    if (response.isSuccessful && response.body() != null && response.body()?.error == false) {
                        val resultSuccess = Result.Success(response.body()?.listStory ?: listOf())
                        _listStories.postValue(resultSuccess)
                        _selectedUserStories.postValue(null)
                    } else {
                        val resultUnsuccessful = Result.Error(Event(response.message()))
                        _listStories.postValue(resultUnsuccessful)
                    }
                }
                is Result.Error -> _listStories.postValue(result)
                else -> {}
            }
        }

    fun updateSelectedUserStories(stories:List<Story>?, marker: Marker?=null) = viewModelScope.launch{
        if (stories != null && marker!=null){
            val userStories = mutableListOf<Story>()
            val markerTitle = marker.title
            val markerLat = marker.position.latitude.toFloat()
            val markerLon = marker.position.longitude.toFloat()
            withContext(Dispatchers.IO){
                userStories.apply {
                    addAll(stories.filter {
                        it.name == markerTitle && it.lat == markerLat && it.lon == markerLon
                    })
                    addAll(stories.filter {
                        it.name == markerTitle && it.lat != markerLat && it.lon != markerLon
                    })
                }
            }
            _selectedUserStories.postValue(userStories)
        }else{
            _selectedUserStories.postValue(null)
        }
        if (isAllMarkerReady.value != null && isAllMarkerReady.value == true){
            _isAllMarkerReady.postValue(true)
        }
    }

    fun checkIfStoryShouldShownInfoWindow(story: Story):Boolean{
        return if (selectedUserStories.value != null){
            val firstSelectedUserStory = selectedUserStories.value?.get(0)
            if (firstSelectedUserStory!=null){
                (story.name == firstSelectedUserStory.name
                        && story.lat == firstSelectedUserStory.lat
                        && story.lon == firstSelectedUserStory.lon)
            }else false

        }else false
    }

    fun setIsAllMarkerReady(isAllMarkerReady:Boolean){
        _isAllMarkerReady.value = isAllMarkerReady
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