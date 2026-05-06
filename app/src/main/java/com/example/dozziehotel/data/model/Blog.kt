package com.example.dozziehotel.data.model

import com.google.gson.annotations.SerializedName

data class Blog(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("authorId")
    val authorId: String? = null,
    @SerializedName("publishedAt")
    val publishedAt: String? = null,
    @SerializedName("tags")
    val tags: List<String>? = null,
    @SerializedName("img_url")
    val imageUrl: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
