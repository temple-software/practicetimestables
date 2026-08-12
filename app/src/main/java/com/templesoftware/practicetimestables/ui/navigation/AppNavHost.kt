package com.templesoftware.practicetimestables.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.templesoftware.practicetimestables.data.preferences.AppLanguage
import com.templesoftware.practicetimestables.ui.home.HomeScreen
import com.templesoftware.practicetimestables.ui.refresh.RefreshScreen
import com.templesoftware.practicetimestables.ui.refresh.RefreshViewModel
import com.templesoftware.practicetimestables.ui.quiz.QuizScreen
import com.templesoftware.practicetimestables.ui.quiz.QuizViewModel
import com.templesoftware.practicetimestables.ui.repeat.RepeatScreen
import com.templesoftware.practicetimestables.ui.repeat.RepeatViewModel
import com.templesoftware.practicetimestables.ui.results.ResultsScreen
import com.templesoftware.practicetimestables.ui.results.ResultsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    language: AppLanguage,
    selectedTables: Set<Int>,
    onLanguageSelected: (AppLanguage) -> Unit,
    onTableToggled: (Int) -> Unit,
) {
    val resultsViewModel: ResultsViewModel = viewModel()
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
                onRefreshClick = { navController.navigateSingleTop(AppDestination.REFRESH) },
                onRepeatClick = { navController.navigateSingleTop(AppDestination.REPEAT) },
                onQuizClick = { navController.navigateSingleTop(AppDestination.QUIZ) },
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
                onRepeatClick = { navController.navigateSingleTop(AppDestination.REPEAT) },
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
                onQuizClick = { navController.navigateSingleTop(AppDestination.QUIZ) },
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
                onAbandonConfirmed = {
                    navController.popBackStack(AppDestination.HOME.route, inclusive = false)
                },
                onReadyForResults = {
                    resultsViewModel.acceptCompletedQuiz(quizUiState.completedResponses)
                    navController.navigate(AppDestination.RESULTS.route) {
                        popUpTo(AppDestination.QUIZ.route) { inclusive = true }
                    }
                },
            )
        }
        composable(AppDestination.RESULTS.route) {
            val resultsUiState by resultsViewModel.uiState.collectAsStateWithLifecycle()
            val state = resultsUiState
            if (state != null) {
                val playFanfare = androidx.compose.runtime.remember { resultsViewModel.claimFanfare() }
                ResultsScreen(
                    language = language,
                    state = state,
                    playFanfare = playFanfare,
                    onLanguageSelected = onLanguageSelected,
                    onReturnHome = {
                        navController.navigate(AppDestination.HOME.route) {
                            popUpTo(AppDestination.HOME.route) { inclusive = false }
                            launchSingleTop = true
                        }
                        resultsViewModel.clearCompletedQuiz()
                    },
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(AppDestination.HOME.route) {
                        popUpTo(AppDestination.RESULTS.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

private fun NavHostController.navigateSingleTop(destination: AppDestination) {
    navigate(destination.route) {
        launchSingleTop = true
    }
}
