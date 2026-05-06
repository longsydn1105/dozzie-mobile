// File: app/src/main/java/com/example/dozziehotel/data/remote/AuthInterceptor.kt

package com.example.dozziehotel.data.remote

import com.example.dozziehotel.data.local.PreferenceManager
import com.example.dozziehotel.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val pref: PreferenceManager // Dùng chung PreferenceManager cho đồng bộ
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Bỏ qua các endpoint public
        val isPublicAuthEndpoint = path.contains("/auth/login") || path.contains("/auth/register")
        if (isPublicAuthEndpoint) {
            return chain.proceed(originalRequest)
        }

        val token = pref.getToken()

        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        // Tạo request mới có gắn thêm Token
        val authorizedRequest = originalRequest.newBuilder()
            .addHeader(Constants.HEADER_AUTHORIZATION, "${Constants.HEADER_BEARER_PREFIX}$token")
            .build()

        return chain.proceed(authorizedRequest)
    }
}