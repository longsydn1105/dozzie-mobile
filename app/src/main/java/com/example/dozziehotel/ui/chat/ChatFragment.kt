package com.example.dozziehotel.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.launch
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.dozziehotel.R
import com.example.dozziehotel.data.local.PreferenceManager
import com.example.dozziehotel.databinding.FragmentChatBinding
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private lateinit var binding: FragmentChatBinding
    private val viewModel: ChatViewModel by viewModel()
    private val pref: PreferenceManager by inject()
    private lateinit var chatAdapter: ChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)

        val bookingId = arguments?.getString("bookingId") ?: ""
        val roomId = arguments?.getString("roomId") ?: "Phòng"

        binding.tvToolbarTitle.text = "Hỗ trợ - $roomId"
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        // Setup Adapter
        val userName = pref.getUser()?.fullName ?: "Khách hàng"
        chatAdapter = ChatAdapter(userName)
        binding.rvChat.adapter = chatAdapter

        setupObservers()

        // Gọi API lấy lịch sử tin nhắn
        viewModel.fetchChatHistory(bookingId)

        // Sự kiện nút gửi
        binding.btnSend.setOnClickListener {
            val text = binding.edtMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                binding.edtMessage.setText("") // Xóa text sau khi gửi
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collectLatest { list ->
                    Log.d("CHAT_UI", "Nhận được ${list.size} tin nhắn")
                    binding.tvEmpty.isVisible = list.isEmpty()
                    chatAdapter.submitList(list)
                    if (list.isNotEmpty()) {
                        // Cuộn xuống cuối cùng khi có tin nhắn mới
                        binding.rvChat.scrollToPosition(list.size - 1)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyStatus.collectLatest { resource ->
                    binding.progressBar.isVisible = resource is Resource.Loading

                    when (resource) {
                        is Resource.Success -> {
                            Log.d("CHAT_UI", "Tải lịch sử thành công")
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}