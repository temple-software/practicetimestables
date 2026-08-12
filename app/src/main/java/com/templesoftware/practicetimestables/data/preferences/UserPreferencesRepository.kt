package com.templesoftware.practicetimestables.data.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setLanguage(language: AppLanguage)

    suspend fun setSelectedTables(tables: Set<Int>)
}
