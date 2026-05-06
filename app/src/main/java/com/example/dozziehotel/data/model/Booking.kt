package com.example.dozziehotel.data.model

import com.google.gson.annotations.SerializedName

data class Booking(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("roomId")
    val roomId: String? = null,
    @SerializedName("packageId")
    val packageId: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("startTime")
    val startTime: String? = null,
    @SerializedName("endTime")
    val endTime: String? = null,
    @SerializedName("actualCheckIn")
    val actualCheckIn: String? = null,
    @SerializedName("totalPrice")
    val totalPrice: Double? = null,
    @SerializedName("digitalKey")
    val digitalKey: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
