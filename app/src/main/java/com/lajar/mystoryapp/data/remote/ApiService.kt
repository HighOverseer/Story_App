package com.lajar.mystoryapp.data.remote

import com.lajar.mystoryapp.data.remote.response.GetStoriesResponse
import com.lajar.mystoryapp.data.remote.response.LoginResponse
import com.lajar.mystoryapp.data.remote.response.Responses
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("v1/register")
    fun register(
        @Field("name")
        name: String,
        @Field("email")
        email: String,
        @Field("password")
        password: String
    ): Call<Responses>

    @FormUrlEncoded
    @POST("v1/login")
    fun login(
        @Field("email")
        email: String,
        @Field("password")
        password: String
    ): Call<LoginResponse>

    @Multipart
    @POST("v1/stories")
    fun addStory(
        @Part("description")
        description: RequestBody,
        @Part
        file: MultipartBody.Part
    ): Call<Responses>

    @Multipart
    @POST("v1/stories")
    fun addStoryWithLoc(
        @Part("description")
        description: String,
        @Part
        file: MultipartBody.Part,
        @Part("lat")
        lat: Float,
        @Part("lon")
        lon: Float
    ): Call<Responses>

    @GET("v1/stories")
    fun getAllStories(@Query("page") page: Int, @Query("size") size: Int): Call<GetStoriesResponse>
}