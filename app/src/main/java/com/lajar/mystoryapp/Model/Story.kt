package com.lajar.mystoryapp.Model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Story(
    @field:SerializedName("id")
    val id: String? = null,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("description")
    val description: String,
    @field:SerializedName("photoUrl")
    val photoUrl: String,
    @field:SerializedName("lat")
    val lat: Float? = null,
    @field:SerializedName("lon")
    val lon: Float? = null
) : Parcelable