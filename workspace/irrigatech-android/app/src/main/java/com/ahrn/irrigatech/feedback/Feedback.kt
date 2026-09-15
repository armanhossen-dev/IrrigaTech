package com.ahrn.irrigatech.feedback

import kotlinx.serialization.Serializable

@Serializable
data class Feedback(
    val userName: String,
    val email: String,
    val feedbackType: String,
    val rating: Int,
    val message: String,
    val appVersion: String
)

@Serializable
data class FeedbackResponse(
    val status: String? = null,
    val message: String? = null
)
