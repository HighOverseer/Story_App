package com.lajar.mystoryapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.lajar.mystoryapp.Helper.Helper
import com.lajar.mystoryapp.data.remote.ApiClient
import com.lajar.mystoryapp.data.remote.response.GetStoriesResponse
import com.lajar.mystoryapp.data.remote.response.Responses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.File


class StoryRepository private constructor(dataStore: DataStore<Preferences>) {

    private val apiService = ApiClient.getApiService(dataStore)

    suspend fun getStories(): Result<Response<GetStoriesResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val getStoriesResponse = apiService.getAllStories().awaitResponse()
                Result.Success(getStoriesResponse)
            } catch (e: Exception) {
                Result.Error(Event(e.message.toString()))
            }
        }


    suspend fun addStory(desc: String, file: File, lat:Float?, lon:Float?): Result<Response<Responses>> =
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
                response = if (lat != null && lon != null){
                    apiService.addStoryWithLoc(desc, imageMultipart, lat, lon).awaitResponse()
                }else{
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

        fun getInstance(dataStore: DataStore<Preferences>): StoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryRepository(dataStore)
            }.also {
                INSTANCE = it
            }
        }
    }
}

