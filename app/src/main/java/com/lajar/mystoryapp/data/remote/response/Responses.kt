package com.lajar.mystoryapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class Responses(
    @field:SerializedName("error")
    val error: Boolean,
    @field:SerializedName("message")
    val message: String
)