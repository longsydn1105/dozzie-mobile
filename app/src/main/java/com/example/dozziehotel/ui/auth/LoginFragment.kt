package com.example.dozziehotel.ui.auth

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dozziehotel.R
import com.example.dozziehotel.databinding.FragmentLoginBinding
import com.example.dozziehotel.utils.Resource
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkNotificationPermission()
        _binding = FragmentLoginBinding.bind(view)

        setupListeners()
        observeLoginState()
    }

    /**
     * Thiết lập các sự kiện click cho nút đăng nhập và chuyển sang màn hình đăng ký.
     */
    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmailLogin.text.toString().trim()
            val password = binding.edtPasswordLogin.text.toString().trim()

            if (email.isEmpty()) {
                binding.edtEmailLogin.error = "Vui lòng nhập email"
                return@setOnClickListener
            }
            if (password.length <= 5) {
                binding.edtPasswordLogin.error = "Mật khẩu phải có ít nhất 6 ký tự"
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false

                val fcmToken = try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    Log.e("AUTH_DEBUG", "Lỗi lấy FCM Token: ${e.message}")
                    null
                }

                Log.d("AUTH_DEBUG", "Token lấy được trước khi login: $fcmToken")
                viewModel.login(email, password, fcmToken)
            }
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    /**
     * Theo dõi trạng thái đăng nhập để cập nhật UI (Loading, Success, Error).
     */
    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { resource ->
                binding.progressBar.isVisible = resource is Resource.Loading
                binding.btnLogin.isEnabled = resource !is Resource.Loading

                when (resource) {
                    is Resource.Success -> {
                        val user = resource.data?.user
                        Toast.makeText(
                            requireContext(),
                            "Chào mừng ${user?.fullName ?: "khách hàng"}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            resource.message ?: "Đăng nhập thất bại",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
