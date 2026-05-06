package com.example.dozziehotel.data.repository

import android.util.Log
import com.example.dozziehotel.data.remote.InvoiceApi
import com.example.dozziehotel.data.remote.InvoiceDto
import com.example.dozziehotel.utils.Resource

class InvoiceRepository(
    private val invoiceApi: InvoiceApi
) {
    /**
     * Lấy danh sách tất cả hóa đơn của người dùng.
     */
    suspend fun getMyInvoices() = invoiceApi.getMyInvoices()

    /**
     * Thực hiện thanh toán cho một hóa đơn cụ thể.
     * Input: [invoiceId] mã hóa đơn cần thanh toán.
     */
    suspend fun payInvoice(invoiceId: String) = invoiceApi.payInvoice(invoiceId)

    /**
     * Lấy hóa đơn chưa thanh toán (pending) mới nhất.
     * Output: Đối tượng hóa đơn hoặc null nếu không có hóa đơn treo.
     */
    suspend fun getLatestPendingInvoice(): Resource<InvoiceDto?> {
        return try {
            val response = invoiceApi.getMyInvoices()
            if (response.isSuccessful) {
                val latest = response.body()?.data
                    ?.filter { it.paymentStatus == "pending" }
                    ?.maxByOrNull { it.createdAt ?: "" }
                Resource.Success(latest)
            } else {
                Resource.Error("Lỗi lấy danh sách hóa đơn")
            }
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Invoice Error: ${e.localizedMessage}")
            Resource.Error("Lỗi hệ thống: ${e.javaClass.simpleName}")
        }
    }
}
