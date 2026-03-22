package com.example.spendwiseai.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwiseai.data.repository.AuthRepository
import com.example.spendwiseai.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _uiState.value = LoginUiState(isLoading = true)
        runCatching { authRepository.signInWithGoogle(idToken) }
            .onSuccess { transactionRepository.syncFromFirestore()
                         _uiState.value = LoginUiState(isLoggedIn = true) }
            .onFailure { _uiState.value = LoginUiState(errorMessage = it.message) }
    }

    fun signInWithEmail(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(errorMessage = "E-posta ve şifre boş olamaz"); return@launch
        }
        _uiState.value = LoginUiState(isLoading = true)
        runCatching { authRepository.signInWithEmail(email, password) }
            .onSuccess { transactionRepository.syncFromFirestore()
                         _uiState.value = LoginUiState(isLoggedIn = true) }
            .onFailure { _uiState.value = LoginUiState(errorMessage = it.message) }
    }

    fun registerWithEmail(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank() || password.length < 6) {
            _uiState.value = LoginUiState(errorMessage = "Şifre en az 6 karakter olmalı"); return@launch
        }
        _uiState.value = LoginUiState(isLoading = true)
        runCatching { authRepository.registerWithEmail(email, password) }
            .onSuccess { _uiState.value = LoginUiState(isLoggedIn = true) }
            .onFailure { _uiState.value = LoginUiState(errorMessage = it.message) }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }

    fun setError(message: String) {
        _uiState.value = LoginUiState(errorMessage = message)
    }
}
