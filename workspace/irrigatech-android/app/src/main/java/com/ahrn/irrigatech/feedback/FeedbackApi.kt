package com.ahrn.irrigatech.feedback

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface FeedbackApi {
    @POST
    suspend fun submitFeedback(
        @Url url: String,
        @Body feedback: Feedback
    ): Response<FeedbackResponse>
}
