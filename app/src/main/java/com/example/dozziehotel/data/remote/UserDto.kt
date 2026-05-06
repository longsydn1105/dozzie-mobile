package com.example.dozziehotel.data.remote

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = "Chưa cập nhật",
    @SerializedName("role") val role: String
)