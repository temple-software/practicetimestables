package com.templesoftware.practicetimestables.ui.quiz

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

interface QuizAudioController {
    fun playSuccess()
    fun playFailure()
    fun stopCurrent()
    fun release()
}

class SynthesizedQuizAudioController : QuizAudioController {
    private val audioThread = HandlerThread("quiz-feedback-audio").apply { start() }
    private val handler = Handler(audioThread.looper)
    private var successTrack: AudioTrack? = null
    private var failureTrack: AudioTrack? = null
    private var activeTrack: AudioTrack? = null
    @Volatile private var released = false

    init {
        handler.post {
            successTrack = createStaticTrack(synthesizeBell())
            failureTrack = createStaticTrack(synthesizeFailureSting())
        }
    }

    override fun playSuccess() = play { successTrack }
    override fun playFailure() = play { failureTrack }

    override fun stopCurrent() {
        if (released) return
        handler.post(::stopActiveOnAudioThread)
    }

    private fun play(trackProvider: () -> AudioTrack?) {
        if (released) return
        handler.post {
            if (released) return@post
            stopActiveOnAudioThread()
            val track = trackProvider() ?: return@post
            try {
                track.setPlaybackHeadPosition(0)
                activeTrack = track
                track.play()
            } catch (failure: Throwable) {
                activeTrack = null
                Log.w(TAG, "Unable to play synthesized Quiz feedback", failure)
            }
        }
    }

    override fun release() {
        if (released) return
        released = true
        handler.removeCallbacksAndMessages(null)
        handler.post {
            stopActiveOnAudioThread()
            releaseTrack(successTrack)
            releaseTrack(failureTrack)
            successTrack = null
            failureTrack = null
            audioThread.quitSafely()
        }
    }

    private fun stopActiveOnAudioThread() {
        val track = activeTrack ?: return
        activeTrack = null
        runCatching { track.pause() }
            .onFailure { Log.w(TAG, "Unable to stop Quiz feedback", it) }
        runCatching { track.setPlaybackHeadPosition(0) }
    }

    private fun createStaticTrack(samples: ShortArray): AudioTrack? = try {
        AudioTrack.Builder()
            .setAudioAttributes(AUDIO_ATTRIBUTES)
            .setAudioFormat(AUDIO_FORMAT)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(samples.size * Short.SIZE_BYTES)
            .build()
            .also { track ->
                check(track.write(samples, 0, samples.size) == samples.size) {
                    "Synthesized Quiz audio buffer was not fully written"
                }
            }
    } catch (failure: Throwable) {
        Log.w(TAG, "Unable to prepare synthesized Quiz feedback", failure)
        null
    }

    private fun releaseTrack(track: AudioTrack?) {
        if (track == null) return
        runCatching { track.release() }
            .onFailure { Log.w(TAG, "Unable to release Quiz audio track", it) }
    }

    private fun synthesizeBell(): ShortArray {
        val sampleCount = SUCCESS_DURATION_MILLIS * SAMPLE_RATE / 1_000
        return ShortArray(sampleCount) { index ->
            val time = index.toDouble() / SAMPLE_RATE
            val attack = (time / BELL_ATTACK_SECONDS).coerceIn(0.0, 1.0)
            val decay = exp(-time * BELL_DECAY_RATE)
            val fundamental = sin(TWO_PI * BELL_FUNDAMENTAL_HZ * time)
            val overtoneOne = 0.42 * sin(TWO_PI * BELL_FUNDAMENTAL_HZ * 2.01 * time)
            val overtoneTwo = 0.2 * sin(TWO_PI * BELL_FUNDAMENTAL_HZ * 3.97 * time)
            pcmSample((fundamental + overtoneOne + overtoneTwo) * attack * decay * SUCCESS_AMPLITUDE)
        }
    }

    private fun synthesizeFailureSting(): ShortArray {
        val sampleCount = FAILURE_DURATION_MILLIS * SAMPLE_RATE / 1_000
        val mixed = DoubleArray(sampleCount)
        addFailureVoice(
            output = mixed,
            startMillis = 0,
            durationMillis = FAILURE_FIRST_DURATION_MILLIS,
            startFrequency = FAILURE_FIRST_START_HZ,
            endFrequency = FAILURE_FIRST_END_HZ,
            gain = 1.0,
        )
        addFailureVoice(
            output = mixed,
            startMillis = FAILURE_SECOND_START_MILLIS,
            durationMillis = FAILURE_SECOND_DURATION_MILLIS,
            startFrequency = FAILURE_SECOND_START_HZ,
            endFrequency = FAILURE_SECOND_END_HZ,
            gain = 0.92,
        )

        val peak = mixed.maxOf { kotlin.math.abs(it) }.coerceAtLeast(1.0)
        val edgeSamples = FAILURE_GLOBAL_TAPER_MILLIS * SAMPLE_RATE / 1_000
        return ShortArray(sampleCount) { index ->
            val edgeTaper = when {
                index < edgeSamples -> smoothStep(index.toDouble() / edgeSamples)
                index >= sampleCount - edgeSamples ->
                    smoothStep((sampleCount - index - 1).coerceAtLeast(0).toDouble() / edgeSamples)
                else -> 1.0
            }
            pcmSample(mixed[index] / peak * FAILURE_AMPLITUDE * edgeTaper)
        }
    }

    private fun addFailureVoice(
        output: DoubleArray,
        startMillis: Int,
        durationMillis: Int,
        startFrequency: Double,
        endFrequency: Double,
        gain: Double,
    ) {
        val startSample = startMillis * SAMPLE_RATE / 1_000
        val voiceSamples = durationMillis * SAMPLE_RATE / 1_000
        val durationSeconds = durationMillis / 1_000.0
        val frequencySlope = (endFrequency - startFrequency) / durationSeconds
        repeat(voiceSamples) { voiceIndex ->
            val outputIndex = startSample + voiceIndex
            if (outputIndex >= output.size) return@repeat
            val time = voiceIndex.toDouble() / SAMPLE_RATE
            val progress = time / durationSeconds
            val phase = TWO_PI * (startFrequency * time + 0.5 * frequencySlope * time * time)
            val attack = smoothStep((time / FAILURE_ATTACK_SECONDS).coerceIn(0.0, 1.0))
            val release = smoothStep(
                ((1.0 - progress) / FAILURE_RELEASE_FRACTION).coerceIn(0.0, 1.0),
            )
            val body = exp(-FAILURE_BODY_DECAY_RATE * time)
            val modulation = 1.0 + FAILURE_MODULATION_DEPTH *
                sin(TWO_PI * FAILURE_MODULATION_HZ * time)
            val tone = sin(phase) +
                FAILURE_SECOND_HARMONIC * sin(phase * 2.006) +
                FAILURE_THIRD_HARMONIC * sin(phase * 3.012)
            output[outputIndex] += tone * attack * release * body * modulation * gain
        }
    }

    private fun smoothStep(value: Double): Double = value * value * (3.0 - 2.0 * value)

    private fun pcmSample(value: Double): Short =
        (value.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()

    private companion object {
        const val TAG = "QuizAudio"
        const val SAMPLE_RATE = 44_100
        const val TWO_PI = 2.0 * PI

        const val SUCCESS_DURATION_MILLIS = 390
        const val SUCCESS_AMPLITUDE = 0.38
        const val BELL_FUNDAMENTAL_HZ = 740.0
        const val BELL_ATTACK_SECONDS = 0.004
        const val BELL_DECAY_RATE = 8.2

        const val FAILURE_DURATION_MILLIS = 490
        const val FAILURE_FIRST_DURATION_MILLIS = 235
        const val FAILURE_FIRST_START_HZ = 430.0
        const val FAILURE_FIRST_END_HZ = 280.0
        const val FAILURE_SECOND_START_MILLIS = 180
        const val FAILURE_SECOND_DURATION_MILLIS = 310
        const val FAILURE_SECOND_START_HZ = 285.0
        const val FAILURE_SECOND_END_HZ = 185.0
        const val FAILURE_ATTACK_SECONDS = 0.011
        const val FAILURE_RELEASE_FRACTION = 0.3
        const val FAILURE_BODY_DECAY_RATE = 1.35
        const val FAILURE_SECOND_HARMONIC = 0.27
        const val FAILURE_THIRD_HARMONIC = 0.1
        const val FAILURE_MODULATION_HZ = 5.2
        const val FAILURE_MODULATION_DEPTH = 0.035
        const val FAILURE_GLOBAL_TAPER_MILLIS = 8
        const val FAILURE_AMPLITUDE = 0.29

        val AUDIO_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val AUDIO_FORMAT: AudioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()
    }
}

class QuizAudioSession(
    private val controller: QuizAudioController,
    initialFeedbackId: Long,
) {
    private var lastFeedbackId = initialFeedbackId
    private var closed = false

    fun onFeedback(feedback: QuizFeedback) {
        if (closed || feedback.id == lastFeedbackId) return
        lastFeedbackId = feedback.id
        controller.stopCurrent()
        when (feedback) {
            QuizFeedback.None -> Unit
            is QuizFeedback.Incorrect -> controller.playFailure()
            is QuizFeedback.Correct -> controller.playSuccess()
        }
    }

    fun close() {
        if (closed) return
        closed = true
        controller.release()
    }
}
