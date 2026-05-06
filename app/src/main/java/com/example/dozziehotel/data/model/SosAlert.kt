package com.example.dozziehotel.data.model

import com.google.gson.annotations.SerializedName

data class SosAlert(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("roomId")
    val roomId: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("resolvedAt")
    val resolvedAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
