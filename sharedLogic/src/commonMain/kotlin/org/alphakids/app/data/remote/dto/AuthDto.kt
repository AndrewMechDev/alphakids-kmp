package org.alphakids.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Requests ──

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val name: String,
)

@Serializable
data class RefreshRequestDto(
    @SerialName("refresh_token")
    val refreshToken: String,
)

@Serializable
data class ForgotPasswordRequestDto(
    val email: String,
)

@Serializable
data class ResetPasswordRequestDto(
    val token: String,
    val password: String,
)

@Serializable
data class SetupPasswordRequestDto(
    val token: String,
    val password: String,
)

// ── Responses ──

@Serializable
data class AuthResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
)

@Serializable
data class MessageResponseDto(
    val message: String,
)

@Serializable
data class UserProfileDto(
    val id: String,
    val email: String,
    val name: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val roles: List<UserRoleDto> = emptyList(),
)

@Serializable
data class UserRoleDto(
    val role: RoleDto,
)

@Serializable
data class RoleDto(
    val id: String,
    val name: String,
    val description: String? = null,
)
