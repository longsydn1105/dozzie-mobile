package com.example.dozziehotel.data.model

import com.google.gson.annotations.SerializedName

data class ServicePackage(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("hours")
    val hours: Int? = null,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("isActive")
    val isActive: Boolean? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    fun getDisplayText(): String {
        val displayPrice = price?.toInt() ?: 0
        return "$name - ${hours}h - $displayPrice VND"
    }
}
