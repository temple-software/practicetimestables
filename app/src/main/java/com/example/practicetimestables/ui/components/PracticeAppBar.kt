package com.example.practicetimestables.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.practicetimestables.R
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.ui.navigation.NavigationAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeAppBar(
    title: String,
    navigationAction: NavigationAction,
    language: AppLanguage,
    onNavigationClick: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            when (navigationAction) {
                NavigationAction.NONE -> Unit
                NavigationAction.BACK -> IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back),
                    )
                }
                NavigationAction.HOME -> IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(R.string.navigate_home),
                    )
                }
            }
        },
        actions = {
            LanguageSelector(language = language, onLanguageSelected = onLanguageSelected)
        },
    )
}

@Composable
private fun LanguageSelector(
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val english = stringResource(R.string.language_english)
    val french = stringResource(R.string.language_french)
    val currentLabel = if (language == AppLanguage.FRENCH) french else english

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(text = "${stringResource(R.string.language)}: $currentLabel")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppLanguage.entries.forEach { option ->
                val label = if (option == AppLanguage.FRENCH) french else english
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onLanguageSelected(option)
                    },
                )
            }
        }
    }
}
