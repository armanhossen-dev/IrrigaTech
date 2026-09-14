package com.ahrn.irrigatech.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahrn.irrigatech.data.auth.AuthRepository
import com.ahrn.irrigatech.data.auth.AuthUser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SignInState {
    data object Idle : SignInState
    data object Loading : SignInState
    data class Error(val message: String) : SignInState
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    val user: StateFlow<AuthUser?> = repository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _signInState = kotlinx.coroutines.flow.MutableStateFlow<SignInState>(SignInState.Idle)
    val signInState: StateFlow<SignInState> = _signInState

    fun signIn(activity: Activity) {
        if (_signInState.value is SignInState.Loading) return
        _signInState.value = SignInState.Loading
        viewModelScope.launch {
            val result = repository.signInWithGoogle(activity)
            _signInState.value = result.fold(
                onSuccess = { SignInState.Idle },
                onFailure = {
                    if (it is androidx.credentials.exceptions.GetCredentialCancellationException) {
                        SignInState.Idle
                    } else {
                        SignInState.Error(it.message ?: "Sign-in failed. Please try again.")
                    }
                },
            )
        }
    }

    fun clearError() {
        if (_signInState.value is SignInState.Error) _signInState.value = SignInState.Idle
    }

    fun signOut(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.signOut()
            onDone()
        }
    }
}
