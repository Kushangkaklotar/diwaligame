package com.example.diwali.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.diwali.R
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

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Sound IDs loaded from authentic video raw recordings
    private val idLighter: Int = try { soundPool.load(context, R.raw.snd_lighter, 1) } catch (_: Exception) { 0 }
    private val idFuse: Int = try { soundPool.load(context, R.raw.snd_fuse, 1) } catch (_: Exception) { 0 }
    private val idExplosion: Int = try { soundPool.load(context, R.raw.snd_explosion, 1) } catch (_: Exception) { 0 }
    private val idFountain: Int = try { soundPool.load(context, R.raw.snd_fountain, 1) } catch (_: Exception) { 0 }
    private val idRocketBlast: Int = try { soundPool.load(context, R.raw.snd_rocket_blast, 1) } catch (_: Exception) { 0 }
    private val idCheer: Int = try { soundPool.load(context, R.raw.snd_cheer, 1) } catch (_: Exception) { 0 }

    private var activeLoopStreamId: Int = 0

    fun playClickSound(enabled: Boolean = true) {
        if (!enabled) return
        if (idLighter != 0) {
            soundPool.play(idLighter, 0.4f, 0.4f, 1, 0, 1.3f)
        } else {
            scope.launch { playTone(frequency = 600.0, durationMs = 35) }
        }
        vibrate(20)
    }

    fun playCoinSound(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            playTone(frequency = 987.77, durationMs = 70) // B5
            playTone(frequency = 1318.51, durationMs = 120) // E6
        }
        vibrate(30)
    }

    fun playUnlockSuccessSound(enabled: Boolean = true) {
        if (!enabled) return
        if (idCheer != 0) {
            soundPool.play(idCheer, 0.7f, 0.7f, 2, 0, 1.2f)
        } else {
            scope.launch {
                playTone(frequency = 523.25, durationMs = 60)
                playTone(frequency = 659.25, durationMs = 60)
                playTone(frequency = 783.99, durationMs = 60)
                playTone(frequency = 1046.50, durationMs = 140)
            }
        }
        vibrate(60)
    }

    fun playLighterClick(enabled: Boolean = true) {
        if (!enabled) return
        if (idLighter != 0) {
            soundPool.play(idLighter, 1.0f, 1.0f, 1, 0, 1.0f)
        } else {
            scope.launch {
                playTone(frequency = 1800.0, durationMs = 15)
                playTone(frequency = 2400.0, durationMs = 30)
            }
        }
        vibrate(30)
    }

    fun playFuseBurning(enabled: Boolean = true) {
        if (!enabled) return
        if (idFuse != 0) {
            soundPool.play(idFuse, 1.0f, 1.0f, 1, 0, 1.0f)
        } else {
            scope.launch {
                for (i in 0..12) {
                    val freq = 2000.0 + (i % 5) * 400.0 + Math.random() * 600.0
                    playTone(frequency = freq, durationMs = 20)
                }
            }
        }
        vibrate(35)
    }

    fun playWellDoneFanfare(enabled: Boolean = true) {
        if (!enabled) return
        if (idCheer != 0) {
            soundPool.play(idCheer, 1.0f, 1.0f, 2, 0, 1.0f)
        } else {
            scope.launch {
                playTone(frequency = 523.25, durationMs = 80)
                playTone(frequency = 659.25, durationMs = 80)
                playTone(frequency = 783.99, durationMs = 90)
                playTone(frequency = 1046.50, durationMs = 220)
            }
        }
        vibrate(80)
    }

    fun playBurstSound(id: String, enabled: Boolean = true) {
        if (!enabled) return
        when (id) {
            "sutli_bomb" -> {
                if (idExplosion != 0) {
                    soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.95f)
                } else {
                    scope.launch {
                        playTone(frequency = 200.0, durationMs = 70)
                        playTone(frequency = 110.0, durationMs = 140)
                        playTone(frequency = 65.0, durationMs = 220)
                    }
                }
                vibrate(120)
            }
            "rocket" -> {
                if (idRocketBlast != 0) {
                    soundPool.play(idRocketBlast, 1.0f, 1.0f, 3, 0, 1.0f)
                } else {
                    scope.launch {
                        for (f in 250..1400 step 120) playTone(frequency = f.toDouble(), durationMs = 25)
                        playTone(frequency = 160.0, durationMs = 180)
                    }
                }
                vibrate(90)
            }
            "anar" -> {
                if (idFountain != 0) {
                    soundPool.play(idFountain, 1.0f, 1.0f, 2, 0, 1.0f)
                } else {
                    scope.launch {
                        for (f in 500..1600 step 150) playTone(frequency = f.toDouble(), durationMs = 45)
                    }
                }
                vibrate(80)
            }
            "chakri" -> {
                if (idFountain != 0) {
                    soundPool.play(idFountain, 1.0f, 1.0f, 2, 0, 1.35f)
                } else {
                    scope.launch {
                        for (repeat in 0..2) {
                            for (f in 350..1100 step 120) playTone(frequency = f.toDouble(), durationMs = 25)
                        }
                    }
                }
                vibrate(75)
            }
            "phooljhadi" -> {
                if (idFuse != 0) {
                    soundPool.play(idFuse, 0.8f, 0.8f, 1, 0, 1.25f)
                } else {
                    scope.launch {
                        for (i in 0..10) playTone(frequency = (1100 + (i * 140)).toDouble(), durationMs = 30)
                    }
                }
                vibrate(50)
            }
            else -> {
                if (idExplosion != 0) {
                    soundPool.play(idExplosion, 0.8f, 0.8f, 1, 0, 1.0f)
                }
                vibrate(60)
            }
        }
    }

    fun stopContinuousSound() {
        if (activeLoopStreamId != 0) {
            soundPool.stop(activeLoopStreamId)
            activeLoopStreamId = 0
        }
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
