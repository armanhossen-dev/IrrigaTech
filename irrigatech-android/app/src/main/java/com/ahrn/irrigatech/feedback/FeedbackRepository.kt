package com.ahrn.irrigatech.feedback

class FeedbackRepository(private val api: FeedbackApi) {

    suspend fun submitFeedback(feedback: Feedback): Result<FeedbackResponse> {
        return try {
            val response = api.submitFeedback(GOOGLE_SCRIPT_URL, feedback)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                val message = if (errorBody.contains("<!DOCTYPE html>") || response.code() == 401) {
                    "Google Script access denied. Ensure the script is deployed as a Web App with access set to 'Anyone'."
                } else if (errorBody.isNotEmpty()) {
                    errorBody
                } else {
                    "Server error (${response.code()}). Please try again later."
                }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val GOOGLE_SCRIPT_URL =
            "https://script.google.com/macros/s/AKfycbzkUWJ30TIOy9qCQZ-15ofQ85ktZnbA6cEV3bDYQ8zY2sQhmOS4w2Gc83XVZTHSVwvf/exec"
    }
}
