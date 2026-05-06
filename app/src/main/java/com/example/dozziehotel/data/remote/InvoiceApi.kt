package com.example.dozziehotel.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface InvoiceApi {

    /**
     * Lấy danh sách hóa đơn cá nhân của người dùng.
     */
    @GET("invoices/my-invoices")
    suspend fun getMyInvoices(): Response<MyInvoicesResponse>

    /**
     * Thực hiện thanh toán cho một hóa đơn cụ thể.
     * Input: [invoiceId] ID của hóa đơn cần thanh toán.
     */
    @PATCH("invoices/{id}/pay")
    suspend fun payInvoice(
        @Path("id") invoiceId: String
    ): Response<PayInvoiceResponse>
}
