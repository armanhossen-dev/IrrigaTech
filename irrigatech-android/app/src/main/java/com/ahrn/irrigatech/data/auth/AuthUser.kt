package com.ahrn.irrigatech.data.auth

data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isDemo: Boolean = false,
)
