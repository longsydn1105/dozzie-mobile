package com.example.dozziehotel.ui.invoice

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.dozziehotel.R
import com.example.dozziehotel.databinding.FragmentInvoiceBinding
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class InvoiceFragment : Fragment(R.layout.fragment_invoice) {

    private lateinit var binding: FragmentInvoiceBinding
    private val viewModel: InvoiceViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInvoiceBinding.bind(view)

        setupObservers()
        viewModel.loadLatestInvoice()
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun setupObservers() {
        // Sử dụng repeatOnLifecycle thay cho launchWhenStarted
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe trạng thái load hóa đơn
                launch {
                    viewModel.invoiceState.collect { resource ->
                        binding.progressBar.isVisible = resource is Resource.Loading
                        if (resource is Resource.Success) {
                            Log.d("INVOICE_DATA", "Dữ liệu nhận được: ${resource.data}")
                        }

                        when (resource) {
                            is Resource.Success -> {
                                val invoice = resource.data
                                if (invoice != null) {
                                    binding.cvInvoice.isVisible = true
                                    binding.layoutEmpty.isVisible = false

                                    // Bind data cũ
                                    binding.tvInvoiceCode.text = invoice.invoiceCode
                                    binding.tvAmount.text = String.format("%,.0f", invoice.totalAmount ?: 0.0)
                                    binding.tvRoomName.text = "Phòng: ${invoice.booking?.room?.id ?: "N/A"}"
                                    binding.tvPackageName.text = "${invoice.booking?.servicePackage?.name} (${invoice.booking?.servicePackage?.hours}h)"

                                    // --- HIỂN THỊ THỜI GIAN ---
                                    val startTime = invoice.booking?.startTime
                                    val endTime = invoice.booking?.endTime

                                    binding.tvCheckInTime.text = formatIsoDate(startTime)
                                    binding.tvCheckOutTime.text = formatIsoDate(endTime)

                                    binding.btnPay.setOnClickListener {
                                        invoice.id?.let { id -> viewModel.executePayment(id) }
                                    }
                                } else {
                                    binding.cvInvoice.isVisible = false
                                    binding.layoutEmpty.isVisible = true // Hiện layout trống

                                }
                            }
                            is Resource.Error -> {
                                binding.progressBar.isVisible = false
                                binding.layoutEmpty.isVisible = true
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                // Observe trạng thái thanh toán
                launch {
                    viewModel.payState.collect { resource ->
                        binding.progressBar.isVisible = resource is Resource.Loading
                        when (resource) {
                            is Resource.Success -> {
                                Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_LONG).show()
                                viewModel.loadLatestInvoice()
                            }
                            is Resource.Error -> {
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun formatIsoDate(isoString: String?): String {
        if (isoString == null || isoString == "N/A") return "N/A"
        return try {
            // SimpleDateFormat với 'X' sẽ tự động xử lý các định dạng múi giờ như Z, +07:00, +0000
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
            val date = inputFormat.parse(isoString)

            // Xuất ra định dạng giờ Việt Nam cho dễ nhìn
            val outputFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            // Nếu chuỗi không có milliseconds (.SSS), thử format ngắn hơn
            try {
                val altFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
                val date = altFormat.parse(isoString)
                val outputFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                outputFormat.format(date!!)
            } catch (e2: Exception) {
                isoString // Trả về chuỗi gốc nếu cả 2 cách đều lỗi
            }
        }
    }
}