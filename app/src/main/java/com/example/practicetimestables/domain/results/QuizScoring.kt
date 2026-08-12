package com.example.practicetimestables.domain.results

import com.example.practicetimestables.domain.quiz.CompletedResponse
import kotlin.math.floor

data class AccuracyScore(
    val totalQuestionCount: Int,
    val accurateQuestionCount: Int,
    val percentage: Double,
    val stars: Int,
)

data class SpeedScore(
    val originalResponseCount: Int,
    val validResponseCount: Int,
    val trimmedResponseCount: Int,
    val averageResponseTimeMillis: Double,
    val stars: Int,
)

data class TableScore(
    val table: Int,
    val accuracy: AccuracyScore,
    val speed: SpeedScore?,
)

data class QuizResults(
    val overallAccuracy: AccuracyScore,
    val overallSpeed: SpeedScore?,
    val tableScores: List<TableScore>,
)

object QuizScoringEngine {
    fun score(responses: List<CompletedResponse>): QuizResults = QuizResults(
        overallAccuracy = accuracy(responses),
        overallSpeed = speed(responses.map(CompletedResponse::responseTimeMillis)),
        tableScores = responses
            .groupBy(CompletedResponse::selectedTable)
            .toSortedMap()
            .map { (table, tableResponses) ->
                TableScore(
                    table = table,
                    accuracy = accuracy(tableResponses),
                    speed = speed(tableResponses.map(CompletedResponse::responseTimeMillis)),
                )
            },
    )

    fun accuracy(responses: List<CompletedResponse>): AccuracyScore {
        val accurateCount = responses.count(CompletedResponse::accurate)
        val percentage = if (responses.isEmpty()) 0.0
        else accurateCount.toDouble() / responses.size * 100.0
        return AccuracyScore(
            totalQuestionCount = responses.size,
            accurateQuestionCount = accurateCount,
            percentage = percentage,
            stars = accuracyStars(percentage),
        )
    }

    fun speed(responseTimesMillis: List<Long>): SpeedScore? {
        val validTimes = responseTimesMillis.filter { it >= 0 }.sorted()
        if (validTimes.isEmpty()) return null
        val trimCount = if (validTimes.size < MINIMUM_TRIMMABLE_RESPONSES) 0
        else floor(validTimes.size * TRIM_FRACTION).toInt()
        val retainedTimes = validTimes.dropLast(trimCount)
        val averageMillis = retainedTimes.sumOf(Long::toDouble) / retainedTimes.size
        return SpeedScore(
            originalResponseCount = responseTimesMillis.size,
            validResponseCount = validTimes.size,
            trimmedResponseCount = trimCount,
            averageResponseTimeMillis = averageMillis,
            stars = speedStars(averageMillis),
        )
    }

    fun accuracyStars(percentage: Double): Int {
        require(percentage in 0.0..100.0) { "Accuracy percentage must be between 0 and 100" }
        return when {
            percentage >= 95.0 -> 5
            percentage >= 85.0 -> 4
            percentage >= 70.0 -> 3
            percentage >= 50.0 -> 2
            else -> 1
        }
    }

    fun speedStars(averageResponseTimeMillis: Double): Int {
        require(averageResponseTimeMillis >= 0.0) { "Average response time cannot be negative" }
        return when {
            averageResponseTimeMillis < 2_000.0 -> 5
            averageResponseTimeMillis < 4_000.0 -> 4
            averageResponseTimeMillis < 6_000.0 -> 3
            averageResponseTimeMillis < 8_000.0 -> 2
            else -> 1
        }
    }

    private const val MINIMUM_TRIMMABLE_RESPONSES = 10
    private const val TRIM_FRACTION = 0.10
}
