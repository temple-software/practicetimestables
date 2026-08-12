package com.templesoftware.practicetimestables.ui.results

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

interface ResultsFanfareController {
    fun play()
    fun release()
}

class SynthesizedResultsFanfareController : ResultsFanfareController {
    private val audioThread = HandlerThread("results-fanfare-audio").apply { start() }
    private val handler = Handler(audioThread.looper)
    private var track: AudioTrack? = null
    @Volatile private var released = false

    init {
        handler.post { track = createTrack(synthesizeFanfare()) }
    }

    override fun play() {
        if (released) return
        handler.post {
            if (released) return@post
            runCatching {
                track?.apply {
                    pause()
                    setPlaybackHeadPosition(0)
                    play()
                }
            }.onFailure { Log.w(TAG, "Unable to play Results fanfare", it) }
        }
    }

    override fun release() {
        if (released) return
        released = true
        handler.removeCallbacksAndMessages(null)
        handler.post {
            runCatching { track?.pause() }
            runCatching { track?.release() }
                .onFailure { Log.w(TAG, "Unable to release Results fanfare", it) }
            track = null
            audioThread.quitSafely()
        }
    }

    private fun createTrack(samples: ShortArray): AudioTrack? = try {
        AudioTrack.Builder()
            .setAudioAttributes(AUDIO_ATTRIBUTES)
            .setAudioFormat(AUDIO_FORMAT)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(samples.size * Short.SIZE_BYTES)
            .build()
            .also { audioTrack ->
                check(audioTrack.write(samples, 0, samples.size) == samples.size)
            }
    } catch (failure: Throwable) {
        Log.w(TAG, "Unable to prepare Results fanfare", failure)
        null
    }

    private fun synthesizeFanfare(): ShortArray {
        val mixed = DoubleArray(DURATION_MILLIS * SAMPLE_RATE / 1_000)
        addNote(mixed, 0, 330, 523.25, 0.72)
        addNote(mixed, 245, 360, 659.25, 0.78)
        addNote(mixed, 510, 390, 783.99, 0.82)
        addNote(mixed, 775, 560, 1_046.50, 1.0)
        addNote(mixed, 790, 530, 783.99, 0.34)
        val peak = mixed.maxOf { kotlin.math.abs(it) }.coerceAtLeast(1.0)
        return ShortArray(mixed.size) { index ->
            ((mixed[index] / peak * AMPLITUDE).coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun addNote(output: DoubleArray, startMillis: Int, durationMillis: Int, frequency: Double, gain: Double) {
        val start = startMillis * SAMPLE_RATE / 1_000
        val count = durationMillis * SAMPLE_RATE / 1_000
        repeat(count) { noteIndex ->
            val outputIndex = start + noteIndex
            if (outputIndex >= output.size) return@repeat
            val time = noteIndex.toDouble() / SAMPLE_RATE
            val progress = noteIndex.toDouble() / count
            val attack = smoothStep((time / ATTACK_SECONDS).coerceIn(0.0, 1.0))
            val release = smoothStep(((1.0 - progress) / RELEASE_FRACTION).coerceIn(0.0, 1.0))
            val resonance = exp(-DECAY_RATE * time)
            val phase = TWO_PI * frequency * time
            val tone = sin(phase) + 0.3 * sin(phase * 2.01) + 0.12 * sin(phase * 3.98)
            output[outputIndex] += tone * attack * release * resonance * gain
        }
    }

    private fun smoothStep(value: Double) = value * value * (3.0 - 2.0 * value)

    private companion object {
        const val TAG = "ResultsFanfare"
        const val SAMPLE_RATE = 44_100
        const val DURATION_MILLIS = 1_350
        const val AMPLITUDE = 0.34
        const val ATTACK_SECONDS = 0.008
        const val RELEASE_FRACTION = 0.42
        const val DECAY_RATE = 1.15
        const val TWO_PI = 2.0 * PI
        val AUDIO_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val AUDIO_FORMAT: AudioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()
    }
}
