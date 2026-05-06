package com.example.dozziehotel.ui.auth

import androidx.lifecycle.ViewModel
import com.example.dozziehotel.data.remote.RegisterRequest
import com.example.dozziehotel.data.remote.RegisterResponse
import com.example.dozziehotel.data.repository.AuthRepository
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<Resource<RegisterResponse>>(Resource.Idle())
    val registerState: StateFlow<Resource<RegisterResponse>> = _registerState.asStateFlow()

    /**
     * Thực hiện đăng ký tài khoản mới.
     * Input: [fullName], [email], [password].
     */
    suspend fun register(fullName: String, email: String, password: String) {
        _registerState.value = Resource.Loading()
        _registerState.value = authRepository.register(
            RegisterRequest(
                fullName = fullName,
                email = email,
                password = password
            )
        )
    }
}
