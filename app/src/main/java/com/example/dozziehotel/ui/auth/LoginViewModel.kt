package com.example.dozziehotel.ui.auth

import androidx.lifecycle.ViewModel
import com.example.dozziehotel.data.remote.LoginRequest
import com.example.dozziehotel.data.remote.LoginResponse
import com.example.dozziehotel.data.repository.AuthRepository
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<LoginResponse>>(Resource.Idle())
    val loginState: StateFlow<Resource<LoginResponse>> = _loginState.asStateFlow()

    /**
     * Thực hiện đăng nhập người dùng.
     * Input: [email], [password], [fcmToken].
     */
    suspend fun login(email: String, password: String, fcmToken: String?) {
        _loginState.value = Resource.Loading()
        _loginState.value = authRepository.login(
            LoginRequest(
                email = email,
                password = password,
                fcmToken = fcmToken
            )
        )
    }
}
