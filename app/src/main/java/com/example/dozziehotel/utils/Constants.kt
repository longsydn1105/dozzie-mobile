package com.example.dozziehotel.utils

import com.example.dozziehotel.BuildConfig

object Constants {
    val BASE_URL: String
        get() = BuildConfig.BASE_URL

    // DataStore / preference keys
    const val DATASTORE_NAME = "dozzie_preferences"
    const val KEY_AUTH_TOKEN = "key_auth_token"
    const val KEY_USER_DATA = "key_user_data"
    const val KEY_USER_ID = "key_user_id"
    const val KEY_USER_EMAIL = "key_user_email"
    const val KEY_USER_FULL_NAME = "key_user_full_name"

    // Network headers
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_BEARER_PREFIX = "Bearer "

    // API public routes
    const val ROUTE_LOGIN = "/login"
    const val ROUTE_REGISTER = "/register"
}
