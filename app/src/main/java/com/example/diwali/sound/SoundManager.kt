package com.example.diwali.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playClickSound(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            playTone(frequency = 600.0, durationMs = 40, sampleRate = 22050)
        }
        vibrate(20)
    }

    fun playCoinSound(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            // Shiny pleasant dual chime
            playTone(frequency = 987.77, durationMs = 80, sampleRate = 22050) // B5
            playTone(frequency = 1318.51, durationMs = 120, sampleRate = 22050) // E6
        }
        vibrate(30)
    }

    fun playUnlockSuccessSound(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            playTone(frequency = 523.25, durationMs = 60, sampleRate = 22050) // C5
            playTone(frequency = 659.25, durationMs = 60, sampleRate = 22050) // E5
            playTone(frequency = 783.99, durationMs = 60, sampleRate = 22050) // G5
            playTone(frequency = 1046.50, durationMs = 150, sampleRate = 22050) // C6
        }
        vibrate(60)
    }

    fun playLighterClick(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            // Metallic flick sound
            playTone(frequency = 1800.0, durationMs = 15, sampleRate = 22050)
            playTone(frequency = 2400.0, durationMs = 30, sampleRate = 22050)
        }
        vibrate(25)
    }

    fun playFuseBurning(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            // Crackling hissing sparks sound
            for (i in 0..12) {
                val freq = 2000.0 + (i % 5) * 400.0 + Math.random() * 600.0
                playTone(frequency = freq, durationMs = 20, sampleRate = 22050)
            }
        }
        vibrate(35)
    }

    fun playWellDoneFanfare(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            // Cheerful victory melody (C - E - G - high C)
            playTone(frequency = 523.25, durationMs = 80, sampleRate = 22050) // C5
            playTone(frequency = 659.25, durationMs = 80, sampleRate = 22050) // E5
            playTone(frequency = 783.99, durationMs = 90, sampleRate = 22050) // G5
            playTone(frequency = 1046.50, durationMs = 220, sampleRate = 22050) // C6
        }
        vibrate(80)
    }

    fun playBurstSound(id: String, enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            when (id) {
                "chakri" -> {
                    // Spinning whir sound
                    for (repeat in 0..2) {
                        for (f in 350..1100 step 120) {
                            playTone(frequency = f.toDouble(), durationMs = 25, sampleRate = 22050)
                        }
                    }
                }
                "anar" -> {
                    // Ascending sparkles fountain
                    for (f in 500..1600 step 150) {
                        playTone(frequency = f.toDouble(), durationMs = 45, sampleRate = 22050)
                    }
                }
                "phooljhadi" -> {
                    // Crackling fizz
                    for (i in 0..10) {
                        playTone(frequency = (1100 + (i * 140)).toDouble(), durationMs = 30, sampleRate = 22050)
                    }
                }
                "sutli_bomb" -> {
                    // Massive boom explosion
                    playTone(frequency = 220.0, durationMs = 60, sampleRate = 22050)
                    playTone(frequency = 140.0, durationMs = 120, sampleRate = 22050)
                    playTone(frequency = 80.0, durationMs = 240, sampleRate = 22050)
                }
                "rocket" -> {
                    // Whoosh up + high sky boom
                    for (f in 250..1400 step 120) {
                        playTone(frequency = f.toDouble(), durationMs = 25, sampleRate = 22050)
                    }
                    playTone(frequency = 180.0, durationMs = 160, sampleRate = 22050)
                }
                else -> {
                    playTone(frequency = 500.0, durationMs = 80, sampleRate = 22050)
                }
            }
        }
        vibrate(100)
    }

    private fun vibrate(durationMs: Long) {
        try {
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    private fun playTone(frequency: Double, durationMs: Int, sampleRate: Int = 22050) {
        try {
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                // Sine wave with gentle linear fade-out to prevent pops
                val envelope = 1.0 - (i.toDouble() / numSamples.toDouble())
                val angle = 2.0 * Math.PI * i * frequency / sampleRate
                buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()

            Thread.sleep(durationMs.toLong() + 20)
            track.release()
        } catch (_: Exception) {}
    }
}
