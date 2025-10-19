package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class UserCreateRequest(val username: String, val password: String, val email: String? = null)

@Serializable
data class UserResponse(val id: Int, val username: String, val email: String? = null)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class TokenResponse(val token: String)

@Serializable
data class UserUpdateRequest(val username: String? = null, val password: String? = null, val email: String? = null)