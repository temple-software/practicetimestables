package com.example.practicetimestables.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

class DataStoreUserPreferencesRepository(
    context: Context,
    private val systemLanguage: AppLanguage = AppLanguage.fromSystem(),
) : UserPreferencesRepository {
    private val dataStore = context.applicationContext.userPreferencesDataStore

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { values ->
            UserPreferences(
                language = AppLanguage.fromLanguageTag(values[LANGUAGE]) ?: systemLanguage,
                selectedTables = values[SELECTED_TABLES]
                    ?.split(',')
                    ?.mapNotNull(String::toIntOrNull)
                    ?.toSet()
                    ?.let(PreferenceDefaults::normalizeTables)
                    ?: PreferenceDefaults.selectedTables,
            )
        }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[LANGUAGE] = language.languageTag }
    }

    override suspend fun setSelectedTables(tables: Set<Int>) {
        val normalized = PreferenceDefaults.normalizeTables(tables)
        dataStore.edit { it[SELECTED_TABLES] = normalized.joinToString(",") }
    }

    private companion object {
        val LANGUAGE = stringPreferencesKey("language")
        val SELECTED_TABLES = stringPreferencesKey("selected_tables")
    }
}
