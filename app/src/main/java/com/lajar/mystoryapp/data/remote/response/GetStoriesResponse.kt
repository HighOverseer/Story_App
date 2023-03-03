package com.lajar.mystoryapp.data.remote.response

import com.google.gson.annotations.SerializedName
import com.lajar.mystoryapp.data.local.entity.Story

data class GetStoriesResponse(
    @field:SerializedName("error")
    val error: Boolean,
    @field:SerializedName("message")
    val message: String,
    @field:SerializedName("listStory")
    val listStory: List<Story>
)