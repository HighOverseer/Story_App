package com.lajar.mystoryapp.data

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.lajar.mystoryapp.Helper.Helper
import com.lajar.mystoryapp.data.local.database.StoryDatabase
import com.lajar.mystoryapp.data.local.entity.Story
import com.lajar.mystoryapp.data.remote.ApiService
import com.lajar.mystoryapp.data.remote.response.Responses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.File


class StoryRepository private constructor(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService
) {
    private val remoteMediator = StoryRemoteMediator(storyDatabase, apiService)
    fun getStoriesWithPagination(): LiveData<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 15
            ),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStoriesWithPaging()
            },
            remoteMediator = remoteMediator

        ).liveData
    }

    fun getStories(): LiveData<List<Story>> {
        return storyDatabase.storyDao().getAllStories()
    }

    suspend fun addStory(
        desc: String,
        file: File,
        lat: Float?,
        lon: Float?
    ): Result<Response<Responses>> =
        withContext(Dispatchers.IO) {
            val compressedFile = Helper.reduceFileImage(file)
            val description = desc.toRequestBody("text/plain".toMediaType())
            val requestImageFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                compressedFile.name,
                requestImageFile
            )
            val response: Response<Responses>
            try {
                response = if (lat != null && lon != null) {
                    apiService.addStoryWithLoc(desc, imageMultipart, lat, lon).awaitResponse()
                } else {
                    apiService.addStory(description, imageMultipart).awaitResponse()
                }
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(Event(e.message.toString()))
            }

        }

    companion object {
        @Volatile
        private var INSTANCE: StoryRepository? = null

        fun getInstance(storyDatabase: StoryDatabase, apiService: ApiService): StoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryRepository(storyDatabase, apiService)
            }.also {
                INSTANCE = it
            }
        }
    }
}

