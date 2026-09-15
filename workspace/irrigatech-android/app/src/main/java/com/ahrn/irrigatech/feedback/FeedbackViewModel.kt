package com.ahrn.irrigatech.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahrn.irrigatech.BuildConfig
import com.ahrn.irrigatech.data.local.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class FeedbackUiState {
    data object Idle : FeedbackUiState()
    data object Loading : FeedbackUiState()
    data class Success(val message: String) : FeedbackUiState()
    data class Error(val error: String) : FeedbackUiState()
}

class FeedbackViewModel(
    private val repository: FeedbackRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    init {
        viewModelScope.launch {
            val user = sessionStore.user.first()
            user?.let {
                _userName.value = it.displayName.orEmpty()
                _email.value = it.email.orEmpty()
            }
        }
    }

    fun updateName(name: String) { _userName.value = name }
    fun updateEmail(email: String) { _email.value = email }

    fun submitFeedback(type: String, rating: Int, message: String) {
        if (message.isBlank() || rating < 1 || type.isBlank()) {
            _uiState.value = FeedbackUiState.Error("Please fill in all required fields.")
            return
        }

        viewModelScope.launch {
            _uiState.value = FeedbackUiState.Loading
            val feedback = Feedback(
                userName = _userName.value.trim(),
                email = _email.value.trim(),
                feedbackType = type,
                rating = rating,
                message = message.trim(),
                appVersion = BuildConfig.VERSION_NAME
            )
            
            repository.submitFeedback(feedback)
                .onSuccess {
                    _uiState.value = FeedbackUiState.Success(it.message ?: "Thank you! Your feedback has been submitted.")
                }
                .onFailure {
                    _uiState.value = FeedbackUiState.Error(it.message ?: "Failed to submit feedback. Please try again.")
                }
        }
    }

    fun resetState() {
        _uiState.value = FeedbackUiState.Idle
    }
}
