package com.example.practicetimestables.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import com.example.practicetimestables.R
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.ui.navigation.NavigationAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeAppBar(
    title: String?,
    navigationAction: NavigationAction,
    language: AppLanguage,
    onNavigationClick: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    centeredTitle: Boolean = true,
) {
    val titleContent: @Composable () -> Unit = {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
    val navigationContent: @Composable () -> Unit = {
        NavigationControl(navigationAction, onNavigationClick)
    }
    val actionsContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        LanguageSelector(language = language, onLanguageSelected = onLanguageSelected)
    }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
        navigationIconContentColor = MaterialTheme.colorScheme.primary,
        actionIconContentColor = MaterialTheme.colorScheme.primary,
    )

    if (centeredTitle) {
        CenterAlignedTopAppBar(
            title = titleContent,
            navigationIcon = navigationContent,
            actions = actionsContent,
            colors = colors,
        )
    } else {
        TopAppBar(
            title = titleContent,
            navigationIcon = navigationContent,
            actions = actionsContent,
            colors = colors,
        )
    }
}

@Composable
private fun NavigationControl(
    navigationAction: NavigationAction,
    onNavigationClick: () -> Unit,
) {
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
    val accessibilityLabel = stringResource(R.string.language_selector_description, currentLabel)

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.semantics { contentDescription = accessibilityLabel },
        ) {
            LanguageFlag(language)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppLanguage.entries.forEach { option ->
                val label = if (option == AppLanguage.FRENCH) french else english
                DropdownMenuItem(
                    text = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            LanguageFlag(option)
                        }
                    },
                    trailingIcon = if (option == language) {
                        { Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected)) }
                    } else null,
                    modifier = Modifier
                        .background(
                            color = if (option == language) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                            shape = MaterialTheme.shapes.small,
                        )
                        .semantics {
                            contentDescription = label
                            selected = option == language
                            role = Role.RadioButton
                        },
                    onClick = {
                        expanded = false
                        onLanguageSelected(option)
                    },
                )
            }
        }
    }
}
