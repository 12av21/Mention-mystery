package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.GameSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class SoundEffect {
    FOOTSTEP,
    DOOR_UNLOCK,
    PUZZLE_SUCCESS,
    PUZZLE_FAIL,
    CLUE_FOUND,
    GUARD_ALERT,
    SECRET_DISCOVERY,
    VICTORY,
    UI_CLICK,
}

class SoundEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ambienceJob: Job? = null
    private var ambienceTrack: AudioTrack? = null

    @Volatile
    private var settings: GameSettings = GameSettings()

    @Volatile
    private var ambienceActive: Boolean = false

    fun updateSettings(newSettings: GameSettings) {
        settings = newSettings
        if (newSettings.muted || newSettings.musicVolume <= 0.01f || newSettings.masterVolume <= 0.01f) {
            stopAmbience()
        } else if (ambienceActive && ambienceJob == null) {
            launchAmbienceLoop()
        }
    }

    fun startGameplayAmbience() {
        ambienceActive = true
        if (!settings.muted && settings.musicVolume > 0.01f && settings.masterVolume > 0.01f) {
            launchAmbienceLoop()
        }
    }

    fun stopGameplayAmbience() {
        ambienceActive = false
        stopAmbience()
    }

    private fun stopAmbience() {
        ambienceJob?.cancel()
        ambienceJob = null
        runCatching {
            ambienceTrack?.pause()
            ambienceTrack?.flush()
            ambienceTrack?.release()
        }
        ambienceTrack = null
    }

    private fun launchAmbienceLoop() {
        if (ambienceJob != null) return
        ambienceJob = scope.launch {
            val sampleRate = 22050
            val chunkSamples = sampleRate / 2
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            ).coerceAtLeast(chunkSamples * 2)

            val track = runCatching {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }.getOrNull() ?: return@launch

            ambienceTrack = track
            runCatching { track.play() }

            var phase1 = 0.0
            var phase2 = 0.0
            var windLfo = 0.0
            var noiseState = 0.0
            val buffer = ShortArray(chunkSamples)

            while (isActive && ambienceActive) {
                val vol = if (settings.muted) 0f else (settings.masterVolume * settings.musicVolume)
                if (vol <= 0.005f) {
                    delay(120)
                    continue
                }
                for (i in 0 until chunkSamples) {
                    phase1 += 2.0 * PI * 73.42 / sampleRate // Low D2 manor drone
                    phase2 += 2.0 * PI * 110.0 / sampleRate // A2 fifth harmonic
                    windLfo += 2.0 * PI * 0.14 / sampleRate
                    val rawNoise = (Random.nextDouble() * 2.0 - 1.0)
                    noiseState = noiseState * 0.96 + rawNoise * 0.04
                    val windEnv = 0.5 + 0.5 * sin(windLfo)
                    val sample =
                        (sin(phase1) * 0.18 + sin(phase2) * 0.08 + noiseState * 0.28 * windEnv) *
                            vol * 0.35
                    buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                runCatching { track.write(buffer, 0, chunkSamples) }
            }
        }
    }

    fun playSfx(effect: SoundEffect) {
        val current = settings
        if (current.muted) return
        val gain = (current.masterVolume * current.sfxVolume).coerceIn(0f, 1f)
        if (gain <= 0.01f) return

        scope.launch {
            val sampleRate = 22050
            val samples = synthesizeEffect(effect, sampleRate, gain)
            if (samples.isEmpty()) return@launch
            val byteSize = samples.size * 2
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            ).coerceAtLeast(byteSize)

            runCatching {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.play()
                val durationMs = (samples.size * 1000L) / sampleRate + 60L
                delay(durationMs)
                track.stop()
                track.release()
            }
        }
    }

    private fun synthesizeEffect(effect: SoundEffect, sampleRate: Int, gain: Float): ShortArray {
        return when (effect) {
            SoundEffect.FOOTSTEP -> {
                val count = (sampleRate * 0.065f).toInt()
                ShortArray(count) { i ->
                    val t = i.toFloat() / sampleRate
                    val env = exp(-t * 48f)
                    val tone = sin(2.0 * PI * 125.0 * t) * 0.55 + (Random.nextDouble() - 0.5) * 0.45
                    (tone * env * gain * 0.38 * Short.MAX_VALUE).toInt().toShort()
                }
            }
            SoundEffect.UI_CLICK -> toneSequence(sampleRate, gain * 0.35f, listOf(620.0 to 0.045f))
            SoundEffect.DOOR_UNLOCK -> toneSequence(
                sampleRate,
                gain * 0.45f,
                listOf(180.0 to 0.12f, 240.0 to 0.12f, 360.0 to 0.18f),
            )
            SoundEffect.PUZZLE_SUCCESS -> toneSequence(
                sampleRate,
                gain * 0.5f,
                listOf(440.0 to 0.10f, 554.37 to 0.10f, 659.25 to 0.22f),
            )
            SoundEffect.PUZZLE_FAIL -> toneSequence(
                sampleRate,
                gain * 0.45f,
                listOf(210.0 to 0.14f, 155.0 to 0.22f),
            )
            SoundEffect.CLUE_FOUND -> toneSequence(
                sampleRate,
                gain * 0.5f,
                listOf(587.33 to 0.09f, 739.99 to 0.09f, 880.0 to 0.22f),
            )
            SoundEffect.GUARD_ALERT -> toneSequence(
                sampleRate,
                gain * 0.58f,
                listOf(720.0 to 0.10f, 540.0 to 0.10f, 760.0 to 0.16f),
            )
            SoundEffect.SECRET_DISCOVERY -> toneSequence(
                sampleRate,
                gain * 0.55f,
                listOf(293.66 to 0.14f, 440.0 to 0.14f, 587.33 to 0.16f, 880.0 to 0.32f),
            )
            SoundEffect.VICTORY -> toneSequence(
                sampleRate,
                gain * 0.55f,
                listOf(392.0 to 0.14f, 493.88 to 0.14f, 587.33 to 0.14f, 783.99 to 0.42f),
            )
        }
    }

    private fun toneSequence(
        sampleRate: Int,
        gain: Float,
        notes: List<Pair<Double, Float>>,
    ): ShortArray {
        val total = notes.sumOf { (it.second * sampleRate).toInt() }
        val out = ShortArray(total)
        var offset = 0
        for ((freq, dur) in notes) {
            val len = (dur * sampleRate).toInt()
            for (i in 0 until len) {
                val t = i.toDouble() / sampleRate
                val env = (1.0 - i.toDouble() / len).coerceIn(0.0, 1.0)
                val v = (sin(2.0 * PI * freq * t) * 0.75 + sin(4.0 * PI * freq * t) * 0.25) * env * gain
                out[offset + i] = (v.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            }
            offset += len
        }
        return out
    }

    fun release() {
        stopGameplayAmbience()
        scope.cancel()
    }
}
