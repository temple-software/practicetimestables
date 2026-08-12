package com.example.practicetimestables.domain.results

import com.example.practicetimestables.domain.quiz.CompletedResponse
import com.example.practicetimestables.domain.quiz.MultiplicationFact
import com.example.practicetimestables.domain.quiz.QuestionFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuizScoringTest {
    @Test
    fun accuracyUsesQuestionCountsWithoutIntegerDivision() {
        val score = QuizScoringEngine.accuracy(
            listOf(response(2, accurate = true), response(2, accurate = true), response(2, accurate = false)),
        )

        assertEquals(3, score.totalQuestionCount)
        assertEquals(2, score.accurateQuestionCount)
        assertEquals(66.666666, score.percentage, 0.000001)
        assertEquals(2, score.stars)
    }

    @Test
    fun accuracyStarBoundariesAreExact() {
        mapOf(
            100.0 to 5,
            99.9 to 4,
            90.0 to 4,
            89.999 to 3,
            75.0 to 3,
            74.999 to 2,
            50.0 to 2,
            49.999 to 1,
        ).forEach { (percentage, stars) ->
            assertEquals("percentage=$percentage", stars, QuizScoringEngine.accuracyStars(percentage))
        }
    }

    @Test
    fun accuracyCoversPerfectAndVeryPoorResults() {
        assertEquals(5, QuizScoringEngine.accuracy(List(5) { response(2, true) }).stars)
        assertEquals(1, QuizScoringEngine.accuracy(List(5) { response(2, false) }).stars)
    }

    @Test
    fun speedStarBoundariesAreExact() {
        mapOf(
            999.999 to 5,
            1_000.0 to 5,
            1_000.001 to 4,
            2_499.999 to 4,
            2_500.0 to 4,
            2_500.001 to 3,
            3_999.999 to 3,
            4_000.0 to 3,
            4_000.001 to 2,
            4_999.999 to 2,
            5_000.0 to 2,
            5_000.001 to 1,
        ).forEach { (millis, stars) ->
            assertEquals("millis=$millis", stars, QuizScoringEngine.speedStars(millis))
        }
    }

    @Test
    fun oneAndFewerThanTenResponsesAreNotTrimmed() {
        val one = requireNotNull(QuizScoringEngine.speed(listOf(1_250)))
        val nine = requireNotNull(QuizScoringEngine.speed((1L..9L).map { it * 1_000 }))

        assertEquals(0, one.trimmedResponseCount)
        assertEquals(1_250.0, one.averageResponseTimeMillis, 0.0)
        assertEquals(0, nine.trimmedResponseCount)
        assertEquals(5_000.0, nine.averageResponseTimeMillis, 0.0)
    }

    @Test
    fun exactlyTenResponsesTrimOneSlowestOutlier() {
        val score = requireNotNull(QuizScoringEngine.speed(List(9) { 1_000L } + 50_000L))

        assertEquals(10, score.originalResponseCount)
        assertEquals(1, score.trimmedResponseCount)
        assertEquals(1_000.0, score.averageResponseTimeMillis, 0.0)
        assertEquals(5, score.stars)
    }

    @Test
    fun elevenAndNineteenResponsesTrimOneButTwentyTrimTwo() {
        fun score(count: Int) = requireNotNull(
            QuizScoringEngine.speed(List(count - 2) { 1_000L } + listOf(10_000L, 20_000L)),
        )

        assertEquals(1, score(11).trimmedResponseCount)
        assertEquals(1, score(19).trimmedResponseCount)
        assertEquals(2, score(20).trimmedResponseCount)
        assertEquals(1_000.0, score(20).averageResponseTimeMillis, 0.0)
    }

    @Test
    fun largeIdenticalDatasetProducesItsExactAverage() {
        val score = requireNotNull(QuizScoringEngine.speed(List(1_000) { 2_345L }))

        assertEquals(100, score.trimmedResponseCount)
        assertEquals(2_345.0, score.averageResponseTimeMillis, 0.0)
        assertEquals(4, score.stars)
    }

    @Test
    fun severalSlowOutliersAreRemovedAccordingToFloorRule() {
        val score = requireNotNull(
            QuizScoringEngine.speed(List(18) { 2_000L } + listOf(20_000L, 30_000L)),
        )

        assertEquals(2, score.trimmedResponseCount)
        assertEquals(2_000.0, score.averageResponseTimeMillis, 0.0)
    }

    @Test
    fun noValidResponseTimesMakesSpeedUnavailable() {
        assertNull(QuizScoringEngine.speed(emptyList()))
        assertNull(QuizScoringEngine.speed(listOf(-1, -50)))
    }

    @Test
    fun invalidDurationsAreExcludedAndReported() {
        val score = requireNotNull(QuizScoringEngine.speed(listOf(-1, 1_000, 2_000)))

        assertEquals(3, score.originalResponseCount)
        assertEquals(2, score.validResponseCount)
        assertEquals(1_500.0, score.averageResponseTimeMillis, 0.0)
    }

    @Test
    fun overallScoresUseAllResponsesRatherThanAveragingTableStars() {
        val responses = List(9) { response(2, true, 1_000) } +
            listOf(response(12, false, 50_000))

        val results = QuizScoringEngine.score(responses)

        assertEquals(90.0, results.overallAccuracy.percentage, 0.0)
        assertEquals(4, results.overallAccuracy.stars)
        assertEquals(1_000.0, requireNotNull(results.overallSpeed).averageResponseTimeMillis, 0.0)
        assertEquals(5, results.overallSpeed?.stars)
    }

    @Test
    fun perTableScoresAreOrderedAndTrimIndependently() {
        val tableFive = List(9) { response(5, true, 2_000) } + response(5, false, 40_000)
        val tableTwo = listOf(response(2, true, 1_000), response(2, false, 3_000))
        val results = QuizScoringEngine.score(tableFive + tableTwo)

        assertEquals(listOf(2, 5), results.tableScores.map(TableScore::table))
        assertEquals(50.0, results.tableScores[0].accuracy.percentage, 0.0)
        assertEquals(2_000.0, requireNotNull(results.tableScores[0].speed).averageResponseTimeMillis, 0.0)
        assertEquals(90.0, results.tableScores[1].accuracy.percentage, 0.0)
        assertEquals(1, results.tableScores[1].speed?.trimmedResponseCount)
        assertEquals(2_000.0, results.tableScores[1].speed?.averageResponseTimeMillis ?: -1.0, 0.0)
    }

    @Test
    fun aSingleTableProducesOneTableScore() {
        val results = QuizScoringEngine.score(listOf(response(8, true, 900)))

        assertEquals(listOf(8), results.tableScores.map(TableScore::table))
        assertEquals(5, results.overallAccuracy.stars)
        assertEquals(5, results.overallSpeed?.stars)
    }

    private fun response(
        table: Int,
        accurate: Boolean,
        responseTimeMillis: Long = 1_000,
    ) = CompletedResponse(
        fact = MultiplicationFact(firstOperand = 1, table = table),
        selectedTable = table,
        format = QuestionFormat.RESULT_MISSING,
        expectedAnswer = table,
        hadMistake = !accurate,
        responseTimeMillis = responseTimeMillis,
    )
}
