package com.example.dozziehotel.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dozziehotel.R
import com.example.dozziehotel.databinding.FragmentRegisterBinding
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        setupListeners()
        observeRegisterState()
    }

    /**
     * Thiết lập các sự kiện click: kiểm tra tính hợp lệ của thông tin và gửi yêu cầu đăng ký.
     */
    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val fullName = binding.edtFullNameRegister.text.toString().trim()
            val email = binding.edtEmailRegister.text.toString().trim()
            val password = binding.edtPasswordRegister.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.edtFullNameRegister.error = "Vui lòng nhập họ tên"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtEmailRegister.error = "Email không hợp lệ"
                return@setOnClickListener
            }
            if (password.length <= 5) {
                binding.edtPasswordRegister.error = "Mật khẩu tối thiểu 6 ký tự"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                viewModel.register(fullName, email, password)
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Theo dõi trạng thái đăng ký để cập nhật UI và chuyển hướng khi thành công.
     */
    private fun observeRegisterState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registerState.collect { resource ->
                binding.progressBarRegister.isVisible = resource is Resource.Loading
                binding.btnRegister.isEnabled = resource !is Resource.Loading

                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Đăng ký thành công! Hãy đăng nhập lại.", Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
