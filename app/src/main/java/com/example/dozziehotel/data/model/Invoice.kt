package com.example.dozziehotel.data.model

import com.google.gson.annotations.SerializedName

data class Invoice(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("bookingId")
    val bookingId: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("invoiceCode")
    val invoiceCode: String? = null,
    @SerializedName("roomCharge")
    val roomCharge: Double? = null,
    @SerializedName("extraFee")
    val extraFee: Double? = null,
    @SerializedName("totalAmount")
    val totalAmount: Double? = null,
    @SerializedName("paymentStatus")
    val paymentStatus: String? = null,
    @SerializedName("paidAt")
    val paidAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
