package com.lajar.mystoryapp.ViewModel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.android.gms.maps.model.Marker
import com.lajar.mystoryapp.Helper.ConnectivityObserver
import com.lajar.mystoryapp.Helper.NetworkConnectivityObserver
import com.lajar.mystoryapp.data.local.entity.Story
import com.lajar.mystoryapp.data.Event
import com.lajar.mystoryapp.data.StoryRepository
import com.lajar.mystoryapp.data.UserRepository
import com.lajar.mystoryapp.fragment.MapFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class ListViewModel(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : ViewModel() {


    val listStoriesWithPagination: LiveData<PagingData<Story>> =
        storyRepository.getStoriesWithPagination().cachedIn(viewModelScope)

    private val _selectedUserStories = MutableLiveData<List<Story>>(emptyList())
    private val _isAllMarkerReady = MutableLiveData<Boolean>()
    private val _selectedUserStoriesServedWhenReady = MediatorLiveData<List<Story>?>()
    val selectedUserStoriesServedWhenReady:LiveData<List<Story>?> = _selectedUserStoriesServedWhenReady

    private val _singleEventMessage = MediatorLiveData<Event<String>>()
    val singleEventMessage: LiveData<Event<String>> = _singleEventMessage

    private val _connectivityStatus = MutableLiveData<ConnectivityObserver.Status>()
    val connectivityStatus: LiveData<ConnectivityObserver.Status> = _connectivityStatus

    private val _listStoriesForMap = MediatorLiveData<List<Story>>()
    val listStoriesForMap: LiveData<List<Story>> = _listStoriesForMap

    var isConnectionEverUnavailable: Boolean? = null

    fun updateSelectedUserStories(stories: List<Story>?=null, marker: Marker? = null) =
        viewModelScope.launch {
            if (stories != null && marker != null) {
                val userStories = mutableListOf<Story>()
                val markerTitle = marker.title
                val markerLat = marker.position.latitude.toFloat()
                val markerLon = marker.position.longitude.toFloat()
                withContext(Dispatchers.IO) {
                    userStories.apply {
                        val markerSumLatLon = markerLat+markerLon
                        val listFilterByName = stories.filter { it.name == markerTitle }
                        val listFilterByNameAndNotNull = listFilterByName.filter { it.lat!=null && it.lon!=null }
                        val listFilterByNameAndNull = listFilterByName.filterNot { it.lat!=null && it.lon!=null }
                        addAll(listFilterByNameAndNotNull.sortedBy{ (it.lat!!.plus(it.lon!!)).distanceTo(markerSumLatLon) })
                        addAll(listFilterByNameAndNull)
                    }
                }
                _selectedUserStories.postValue(userStories)
            } else {
                _selectedUserStories.postValue(emptyList())
            }
        }

    fun checkIfStoryShouldShownInfoWindow(story: Story): Boolean {
        val selectedUserStories = _selectedUserStories.value
        return if (selectedUserStories != null && selectedUserStories.isNotEmpty()) {
            val firstSelectedUserStory = selectedUserStories[0]
            (story.id == firstSelectedUserStory.id)
        } else false
    }

    fun setIsAllMarkerReady(isAllMarkerReady: Boolean) {
        _isAllMarkerReady.value = isAllMarkerReady
    }

    fun setWhetherConnectionEverUnavailable(wasItEverUnavailable: Boolean) {
        if (isConnectionEverUnavailable == null) {
            isConnectionEverUnavailable = wasItEverUnavailable
        } else if (isConnectionEverUnavailable == true && !wasItEverUnavailable) {
            isConnectionEverUnavailable = false
        }
    }

    fun deleteToken() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.deleteToken()
        }
    }

    private fun observeConnectivity() {
        networkConnectivityObserver.observe().onEach { status ->
            _connectivityStatus.value = status
        }.launchIn(viewModelScope)
    }

    private fun Float.distanceTo(sumLatLon:Float):Float{
        return abs(this-sumLatLon)
    }

    init {
        observeConnectivity()

        _singleEventMessage.addSource(listStoriesWithPagination) { pagingData ->
            if (pagingData != null) {
                _singleEventMessage.value = Event(MapFragment.EVENT_ADD_STORIES_TO_MAP)
            }
        }

        _listStoriesForMap.addSource(_connectivityStatus) { status ->
            when (status) {
                ConnectivityObserver.Status.Available -> {
                    if (storyRepository.getStories().value != null && isConnectionEverUnavailable == false)
                        _listStoriesForMap.value = storyRepository.getStories().value
                }
                else -> {}
            }
        }

        _listStoriesForMap.addSource(storyRepository.getStories()) { stories ->
            if (_connectivityStatus.value == ConnectivityObserver.Status.Available) {
                _listStoriesForMap.value = stories
            }
        }

        _selectedUserStoriesServedWhenReady.addSource(_isAllMarkerReady){isAllMarkersReady ->
            if (isAllMarkersReady){
                _selectedUserStoriesServedWhenReady.value = _selectedUserStories.value
            }else {
                _selectedUserStoriesServedWhenReady.value = null
            }
        }

        _selectedUserStoriesServedWhenReady.addSource(_singleEventMessage){event ->
            if (!event.hasBeenHandled){
                _selectedUserStories.value = emptyList()
            }
        }

        _selectedUserStoriesServedWhenReady.addSource(_selectedUserStories){stories ->
            if (_isAllMarkerReady.value == true){
                _selectedUserStoriesServedWhenReady.value = stories
            }
        }
    }
}