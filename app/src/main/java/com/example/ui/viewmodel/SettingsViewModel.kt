package com.example.ui.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferences(null, null, null, null, isDarkTheme = true, isTutorialCompleted = false)
        )

    val deviceDiagnosticInfo: Map<String, String> = mapOf(
        "Manufacturer" to Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
        "Device Model" to Build.MODEL,
        "Android Version" to "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        "LE Audio Support" to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) "Android 13+ Stack Present (Hardware Dependent)" else "Requires Android 13+",
        "OEM Dual Audio" to when {
            Build.MANUFACTURER.contains("samsung", ignoreCase = true) -> "Samsung Dual Audio (Supported via Quick Settings)"
            Build.MANUFACTURER.contains("xiaomi", ignoreCase = true) -> "Xiaomi Sound Assistant (Multi-Speaker supported)"
            Build.MANUFACTURER.contains("oneplus", ignoreCase = true) || Build.MANUFACTURER.contains("oppo", ignoreCase = true) -> "ColorOS/OxygenOS Dual Mode Audio"
            else -> "Stock Android (Standard single A2DP sink)"
        }
    )

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkTheme(isDark)
        }
    }

    fun setTutorialCompleted(completed: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setTutorialCompleted(completed)
        }
    }
}
