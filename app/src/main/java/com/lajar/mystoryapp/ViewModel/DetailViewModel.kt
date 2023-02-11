package com.lajar.mystoryapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lajar.mystoryapp.Model.Story

class DetailViewModel : ViewModel() {
    private val _storyDetail = MutableLiveData<Story>()
    val storyDetail: LiveData<Story> = _storyDetail

    fun setStoryDetail(story: Story) {
        _storyDetail.value = story
    }
}