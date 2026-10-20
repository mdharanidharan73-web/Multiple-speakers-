package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dualsync_user_prefs")

data class UserPreferences(
    val speaker1Address: String?,
    val speaker1Name: String?,
    val speaker2Address: String?,
    val speaker2Name: String?,
    val isDarkTheme: Boolean,
    val isTutorialCompleted: Boolean
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SPEAKER_1_ADDRESS = stringPreferencesKey("speaker_1_address")
        val SPEAKER_1_NAME = stringPreferencesKey("speaker_1_name")
        val SPEAKER_2_ADDRESS = stringPreferencesKey("speaker_2_address")
        val SPEAKER_2_NAME = stringPreferencesKey("speaker_2_name")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val IS_TUTORIAL_COMPLETED = booleanPreferencesKey("is_tutorial_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            speaker1Address = prefs[PreferencesKeys.SPEAKER_1_ADDRESS],
            speaker1Name = prefs[PreferencesKeys.SPEAKER_1_NAME],
            speaker2Address = prefs[PreferencesKeys.SPEAKER_2_ADDRESS],
            speaker2Name = prefs[PreferencesKeys.SPEAKER_2_NAME],
            isDarkTheme = prefs[PreferencesKeys.IS_DARK_THEME] ?: true,
            isTutorialCompleted = prefs[PreferencesKeys.IS_TUTORIAL_COMPLETED] ?: false
        )
    }

    suspend fun saveSpeaker1(address: String?, name: String?) {
        context.dataStore.edit { prefs ->
            if (address != null) {
                prefs[PreferencesKeys.SPEAKER_1_ADDRESS] = address
                prefs[PreferencesKeys.SPEAKER_1_NAME] = name ?: "Speaker 1"
            } else {
                prefs.remove(PreferencesKeys.SPEAKER_1_ADDRESS)
                prefs.remove(PreferencesKeys.SPEAKER_1_NAME)
            }
        }
    }

    suspend fun saveSpeaker2(address: String?, name: String?) {
        context.dataStore.edit { prefs ->
            if (address != null) {
                prefs[PreferencesKeys.SPEAKER_2_ADDRESS] = address
                prefs[PreferencesKeys.SPEAKER_2_NAME] = name ?: "Speaker 2"
            } else {
                prefs.remove(PreferencesKeys.SPEAKER_2_ADDRESS)
                prefs.remove(PreferencesKeys.SPEAKER_2_NAME)
            }
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_TUTORIAL_COMPLETED] = completed
        }
    }
}
