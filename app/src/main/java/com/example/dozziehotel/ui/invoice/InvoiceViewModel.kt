package com.example.dozziehotel.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dozziehotel.data.model.Invoice
import com.example.dozziehotel.data.remote.InvoiceDto
import com.example.dozziehotel.data.repository.InvoiceRepository
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    // Quản lý trạng thái lấy hóa đơn mới nhất
    private val _invoiceState = MutableStateFlow<Resource<InvoiceDto?>>(Resource.Idle())
    val invoiceState: StateFlow<Resource<InvoiceDto?>> = _invoiceState

    // Quản lý trạng thái khi nhấn nút thanh toán
    private val _payState = MutableStateFlow<Resource<Invoice>>(Resource.Idle())
    val payState: StateFlow<Resource<Invoice>> = _payState

    /**
     * Lấy hóa đơn pending mới nhất từ Repository
     */
    fun loadLatestInvoice() {
        viewModelScope.launch {
            _invoiceState.value = Resource.Loading()
            try {
                _invoiceState.value = invoiceRepository.getLatestPendingInvoice()
            } catch (e: Exception) {
                _invoiceState.value = Resource.Error("Lỗi xử lý dữ liệu: ${e.message}")
            }
        }
    }

    /**
     * Gọi API thanh toán hóa đơn theo ID
     */
    fun executePayment(invoiceId: String) {
        viewModelScope.launch {
            _payState.value = Resource.Loading()
            try {
                val response = invoiceRepository.payInvoice(invoiceId)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Nếu thành công, trả về data Invoice (Model gốc)
                    _payState.value = Resource.Success(response.body()!!.data!!)
                } else {
                    _payState.value = Resource.Error(response.body()?.message ?: "Thanh toán thất bại")
                }
            } catch (e: Exception) {
                _payState.value = Resource.Error("Lỗi kết nối hệ thống: ${e.message}")
            }
        }
    }
}