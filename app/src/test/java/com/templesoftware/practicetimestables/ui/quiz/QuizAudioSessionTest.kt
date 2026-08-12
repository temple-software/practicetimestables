package com.templesoftware.practicetimestables.ui.quiz

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizAudioSessionTest {
    @Test
    fun incorrectAndCorrectFeedbackRequestMatchingSounds() {
        val audio = FakeAudioController()
        val session = QuizAudioSession(audio, initialFeedbackId = 0)

        session.onFeedback(QuizFeedback.Incorrect(1))
        session.onFeedback(QuizFeedback.Correct(2))

        assertEquals(listOf("stop", "failure", "stop", "success"), audio.calls)
    }

    @Test
    fun repeatedFeedbackIdsRetriggerButRecompositionOfSameIdDoesNot() {
        val audio = FakeAudioController()
        val session = QuizAudioSession(audio, initialFeedbackId = 0)

        session.onFeedback(QuizFeedback.Incorrect(1))
        session.onFeedback(QuizFeedback.Incorrect(1))
        session.onFeedback(QuizFeedback.Incorrect(2))

        assertEquals(listOf("stop", "failure", "stop", "failure"), audio.calls)
    }

    @Test
    fun currentFeedbackIsBaselineAndIsNotReplayedAfterRotation() {
        val audio = FakeAudioController()
        val session = QuizAudioSession(audio, initialFeedbackId = 7)

        session.onFeedback(QuizFeedback.Correct(7))

        assertEquals(emptyList<String>(), audio.calls)
    }

    @Test
    fun clearProducesNoFeedbackEventAndThereforeNoAudioRequest() {
        val audio = FakeAudioController()
        val session = QuizAudioSession(audio, initialFeedbackId = 0)

        session.onFeedback(QuizFeedback.None)

        assertEquals(emptyList<String>(), audio.calls)
    }

    @Test
    fun closingSessionReleasesAudioAndIgnoresStaleEvents() {
        val audio = FakeAudioController()
        val session = QuizAudioSession(audio, initialFeedbackId = 0)

        session.close()
        session.onFeedback(QuizFeedback.Correct(1))
        session.close()

        assertEquals(listOf("release"), audio.calls)
    }

    @Test
    fun correctAfterIncorrectStopsFailureBeforeStartingSuccess() {
        val audio = FakeAudioController()
        val session = QuizAudioSession(audio, initialFeedbackId = 0)

        session.onFeedback(QuizFeedback.Incorrect(1))
        session.onFeedback(QuizFeedback.Correct(2))

        assertEquals(listOf("stop", "failure", "stop", "success"), audio.calls)
    }

    @Test
    fun incorrectAfterCorrectStopsSuccessBeforeStartingFailure() {
        val audio = FakeAudioController()
        val session = QuizAudioSession(audio, initialFeedbackId = 0)

        session.onFeedback(QuizFeedback.Correct(1))
        session.onFeedback(QuizFeedback.Incorrect(2))

        assertEquals(listOf("stop", "success", "stop", "failure"), audio.calls)
    }

    private class FakeAudioController : QuizAudioController {
        val calls = mutableListOf<String>()
        override fun playSuccess() { calls += "success" }
        override fun playFailure() { calls += "failure" }
        override fun stopCurrent() { calls += "stop" }
        override fun release() { calls += "release" }
    }
}
