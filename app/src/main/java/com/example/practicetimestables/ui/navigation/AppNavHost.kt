package com.example.practicetimestables.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.practicetimestables.R
import com.example.practicetimestables.data.preferences.AppLanguage
import com.example.practicetimestables.ui.components.PracticeAppBar
import com.example.practicetimestables.ui.home.HomeScreen
import com.example.practicetimestables.ui.refresh.RefreshScreen
import com.example.practicetimestables.ui.refresh.RefreshViewModel
import com.example.practicetimestables.ui.quiz.QuizScreen
import com.example.practicetimestables.ui.quiz.QuizViewModel
import com.example.practicetimestables.ui.repeat.RepeatScreen
import com.example.practicetimestables.ui.repeat.RepeatViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    language: AppLanguage,
    selectedTables: Set<Int>,
    onLanguageSelected: (AppLanguage) -> Unit,
    onTableToggled: (Int) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.startDestination.route,
    ) {
        composable(AppDestination.HOME.route) {
            HomeScreen(
                language = language,
                selectedTables = selectedTables,
                onLanguageSelected = onLanguageSelected,
                onTableToggled = onTableToggled,
                onRefreshClick = { navController.navigate(AppDestination.REFRESH.route) },
                onRepeatClick = { navController.navigate(AppDestination.REPEAT.route) },
                onQuizClick = { navController.navigate(AppDestination.QUIZ.route) },
            )
        }
        composable(AppDestination.REFRESH.route) {
            val refreshViewModel: RefreshViewModel = viewModel(
                factory = RefreshViewModel.factory(selectedTables),
            )
            val refreshUiState by refreshViewModel.uiState.collectAsStateWithLifecycle()
            RefreshScreen(
                language = language,
                uiState = refreshUiState,
                onLanguageSelected = onLanguageSelected,
                onBackClick = {
                    navController.popBackStack(AppDestination.HOME.route, inclusive = false)
                },
                onNextClick = refreshViewModel::advance,
                onRepeatClick = { navController.navigate(AppDestination.REPEAT.route) },
            )
        }
        composable(AppDestination.REPEAT.route) {
            val repeatViewModel: RepeatViewModel = viewModel(
                factory = RepeatViewModel.factory(selectedTables),
            )
            val repeatUiState by repeatViewModel.uiState.collectAsStateWithLifecycle()
            RepeatScreen(
                language = language,
                uiState = repeatUiState,
                onLanguageSelected = onLanguageSelected,
                onBackClick = {
                    navController.popBackStack(AppDestination.HOME.route, inclusive = false)
                },
                onNextTableClick = repeatViewModel::advanceTable,
                onQuizClick = { navController.navigate(AppDestination.QUIZ.route) },
            )
        }
        composable(AppDestination.QUIZ.route) {
            val quizViewModel: QuizViewModel = viewModel(factory = QuizViewModel.factory(selectedTables))
            val quizUiState by quizViewModel.uiState.collectAsStateWithLifecycle()
            val quizFeedback by quizViewModel.feedback.collectAsStateWithLifecycle()
            val quizInputPresentation by quizViewModel.inputPresentation.collectAsStateWithLifecycle()
            QuizScreen(
                language = language,
                uiState = quizUiState,
                feedback = quizFeedback,
                inputPresentation = quizInputPresentation,
                onLanguageSelected = onLanguageSelected,
                onQuestionReady = quizViewModel::questionBecameInteractive,
                onDigitPressed = quizViewModel::pressDigit,
                onClearPressed = quizViewModel::clear,
                onAbandonConfirmed = {
                    navController.popBackStack(AppDestination.HOME.route, inclusive = false)
                },
                onReadyForResults = {
                    navController.navigate(AppDestination.RESULTS.route) {
                        popUpTo(AppDestination.QUIZ.route) { inclusive = true }
                    }
                },
            )
        }
        AppDestination.all
            .filterNot {
                it == AppDestination.HOME ||
                    it == AppDestination.REFRESH ||
                    it == AppDestination.REPEAT ||
                    it == AppDestination.QUIZ
            }
            .forEach { destination ->
            composable(destination.route) {
                DestinationSkeleton(
                    destination = destination,
                    language = language,
                    onLanguageSelected = onLanguageSelected,
                    onNavigationClick = {
                        when (destination.navigationAction) {
                            NavigationAction.NONE -> Unit
                            NavigationAction.BACK -> navController.navigateUp()
                            NavigationAction.HOME -> navController.navigate(AppDestination.startDestination.route) {
                                popUpTo(AppDestination.startDestination.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DestinationSkeleton(
    destination: AppDestination,
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onNavigationClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            PracticeAppBar(
                title = stringResource(destination.title),
                navigationAction = destination.navigationAction,
                language = language,
                onNavigationClick = onNavigationClick,
                onLanguageSelected = onLanguageSelected,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.stage_one_placeholder))
        }
    }
}
