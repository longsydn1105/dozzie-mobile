package com.example.dozziehotel.data.model

import com.google.gson.annotations.SerializedName

data class Room(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("floor")
    val floor: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("iotConfig")
    val iotConfig: IotConfig? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

data class IotConfig(
    @SerializedName("deviceId")
    val deviceId: String? = null,
    @SerializedName("topicDoor")
    val topicDoor: String? = null,
    @SerializedName("topicPower")
    val topicPower: String? = null,
    @SerializedName("isOnline")
    val isOnline: Boolean? = null,
    @SerializedName("lastPing")
    val lastPing: String? = null
)
