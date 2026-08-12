package com.example.practicetimestables.ui.app

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.data.preferences.DataStoreUserPreferencesRepository
import com.example.practicetimestables.ui.navigation.AppNavHost
import com.example.practicetimestables.ui.theme.PracticeTimesTablesTheme
import java.util.Locale

@Composable
fun PracticeTimesTablesApp() {
    val context = LocalContext.current
    val initialLanguage = remember { AppLanguage.fromSystem() }
    val repository = remember(context) { DataStoreUserPreferencesRepository(context, initialLanguage) }
    val applicationViewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModel.Factory(repository, initialLanguage),
    )
    val uiState by applicationViewModel.uiState.collectAsStateWithLifecycle()

    LocalizedContent(language = uiState.language) {
        PracticeTimesTablesTheme {
            AppNavHost(
                navController = rememberNavController(),
                language = uiState.language,
                selectedTables = uiState.selectedTables,
                onLanguageSelected = applicationViewModel::selectLanguage,
                onTableToggled = applicationViewModel::toggleTable,
            )
        }
    }
}

@Composable
private fun LocalizedContent(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val locale = remember(language) { Locale.forLanguageTag(language.languageTag) }
    val configuration = remember(baseConfiguration, locale) {
        Configuration(baseConfiguration).apply { setLocale(locale) }
    }
    val localizedContext = remember(baseContext, configuration) {
        baseContext.createConfigurationContext(configuration)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration,
        content = content,
    )
}
