package com.example.dozziehotel.data.model

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("bookingId")
    val bookingId: String? = null,
    @SerializedName("rating")
    val rating: Int? = null,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("isShow")
    val isShow: Boolean? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
