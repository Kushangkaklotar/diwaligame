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

    fun playRocketLaunchSound(enabled: Boolean = true) {
        if (!enabled) return
        if (idFuse != 0) soundPool.play(idFuse, 0.85f, 0.85f, 2, 0, 1.8f)
        scope.launch {
            for (f in 280..1300 step 130) {
                playTone(frequency = f.toDouble(), durationMs = 26)
            }
        }
        vibrate(45)
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

    fun playBurstSound(id: String, enabled: Boolean = true, level: Int = 1) {
        if (!enabled) return
        val clampedLevel = level.coerceIn(1, 25)
        when (id) {
            "sutli_bomb" -> {
                val detail = com.example.diwali.model.SutliBombLevelConfig.getDetail(clampedLevel)
                when (detail.soundProfile) {
                    com.example.diwali.model.SutliSoundProfile.LIGHT_POP -> {
                        // Level 1: soft pop
                        if (idExplosion != 0) {
                            soundPool.play(idExplosion, 0.45f, 0.45f, 3, 0, 1.45f)
                        } else {
                            scope.launch {
                                playTone(frequency = 550.0, durationMs = 50)
                            }
                        }
                        vibrate(40)
                    }
                    com.example.diwali.model.SutliSoundProfile.SOFT_BANG -> {
                        // Level 2: gentle bang
                        if (idExplosion != 0) {
                            soundPool.play(idExplosion, 0.6f, 0.6f, 3, 0, 1.35f)
                        } else {
                            scope.launch {
                                playTone(frequency = 380.0, durationMs = 80)
                            }
                        }
                        vibrate(55)
                    }
                    com.example.diwali.model.SutliSoundProfile.FIZZ_THEN_POP -> {
                        // Level 3: hiss/fizz then pop
                        if (idFuse != 0) soundPool.play(idFuse, 0.7f, 0.7f, 2, 0, 1.4f)
                        if (idExplosion != 0) soundPool.play(idExplosion, 0.7f, 0.7f, 3, 0, 1.25f)
                        vibrate(70)
                    }
                    com.example.diwali.model.SutliSoundProfile.SHARP_TADAK -> {
                        // Level 4: snappy sharp crack "Tadak"
                        if (idExplosion != 0) {
                            soundPool.play(idExplosion, 0.85f, 0.85f, 3, 0, 1.3f)
                        }
                        scope.launch {
                            playTone(frequency = 1200.0, durationMs = 30)
                            playTone(frequency = 400.0, durationMs = 90)
                        }
                        vibrate(90)
                    }
                    com.example.diwali.model.SutliSoundProfile.NORMAL_BOOM -> {
                        // Level 5: milestone blast
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 1.0f)
                        vibrate(120)
                    }
                    com.example.diwali.model.SutliSoundProfile.INTENSE_BANG -> {
                        // Level 6: loud sharp blast
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 1.15f)
                        vibrate(130)
                    }
                    com.example.diwali.model.SutliSoundProfile.HEAVY_BASS -> {
                        // Level 7: low-frequency heavy bass
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.85f)
                        vibrate(150)
                    }
                    com.example.diwali.model.SutliSoundProfile.DEEP_ECHO -> {
                        // Level 8: hollow echoing boom
                        if (idExplosion != 0) {
                            soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.82f)
                            scope.launch {
                                kotlinx.coroutines.delay(120)
                                soundPool.play(idExplosion, 0.4f, 0.4f, 2, 0, 0.75f)
                            }
                        }
                        vibrate(160)
                    }
                    com.example.diwali.model.SutliSoundProfile.HEAVY_DHAM -> {
                        // Level 9: deep thud "Dham"
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.78f)
                        vibrate(180)
                    }
                    com.example.diwali.model.SutliSoundProfile.MID_SHOCKWAVE -> {
                        // Level 10: classic shockwave
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.90f)
                        vibrate(200)
                    }
                    com.example.diwali.model.SutliSoundProfile.CHEMICAL_CRACKLE -> {
                        // Level 11: crackling sparks
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 1.05f)
                        if (idFuse != 0) soundPool.play(idFuse, 0.9f, 0.9f, 2, 0, 1.5f)
                        vibrate(210)
                    }
                    com.example.diwali.model.SutliSoundProfile.HISS_THEN_BLAST -> {
                        // Level 12: hiss then blast
                        if (idFuse != 0) soundPool.play(idFuse, 1.0f, 1.0f, 2, 0, 1.6f)
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.88f)
                        vibrate(220)
                    }
                    com.example.diwali.model.SutliSoundProfile.EARTH_RUMBLE -> {
                        // Level 13: earth rumble
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.72f)
                        vibrate(250)
                    }
                    com.example.diwali.model.SutliSoundProfile.DEEP_BLACK_BOOM -> {
                        // Level 14: dark muffled heavy boom
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.68f)
                        vibrate(260)
                    }
                    com.example.diwali.model.SutliSoundProfile.ROCKET_HYBRID -> {
                        // Level 15: whistle into boom
                        if (idRocketBlast != 0) soundPool.play(idRocketBlast, 0.9f, 0.9f, 2, 0, 1.2f)
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.85f)
                        vibrate(270)
                    }
                    com.example.diwali.model.SutliSoundProfile.EAR_PIERCING_BANG -> {
                        // Level 16: deafening loud bang
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 1.1f)
                        scope.launch {
                            playTone(frequency = 1500.0, durationMs = 50)
                        }
                        vibrate(290)
                    }
                    com.example.diwali.model.SutliSoundProfile.WIDE_BARREL_BLAST -> {
                        // Level 17: wide barrel blast
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.74f)
                        vibrate(310)
                    }
                    com.example.diwali.model.SutliSoundProfile.ROX_DEMOLITION -> {
                        // Level 18: industrial demolition
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.70f)
                        vibrate(330)
                    }
                    com.example.diwali.model.SutliSoundProfile.GIANT_THUNDER -> {
                        // Level 19: giant thunderclap
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.65f)
                        vibrate(350)
                    }
                    com.example.diwali.model.SutliSoundProfile.GOLDEN_MAJESTIC -> {
                        // Level 20: golden majestic boom
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.85f)
                        if (idCheer != 0) soundPool.play(idCheer, 0.5f, 0.5f, 2, 0, 1.3f)
                        vibrate(370)
                    }
                    com.example.diwali.model.SutliSoundProfile.TRIPLE_POP_BANG -> {
                        // Level 21: 3 pre-bursts then main blast
                        scope.launch {
                            repeat(3) { i ->
                                if (idExplosion != 0) soundPool.play(idExplosion, 0.55f, 0.55f, 2, 0, 1.4f + i * 0.15f)
                                vibrate(45)
                                kotlinx.coroutines.delay(130)
                            }
                            if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.68f)
                            vibrate(380)
                        }
                    }
                    com.example.diwali.model.SutliSoundProfile.NUCLEAR_APOCALYPSE -> {
                        // Level 22: nuclear atmospheric blast
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.60f)
                        scope.launch {
                            playTone(frequency = 55.0, durationMs = 600)
                        }
                        vibrate(420)
                    }
                    com.example.diwali.model.SutliSoundProfile.SUB_BASS_QUAKE -> {
                        // Level 23: violent sub-bass quake
                        if (idExplosion != 0) soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.58f)
                        vibrate(450)
                    }
                    com.example.diwali.model.SutliSoundProfile.PRE_BOSS_DUAL -> {
                        // Level 24: double-gunpowder acoustic
                        if (idExplosion != 0) {
                            soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.65f)
                            scope.launch {
                                kotlinx.coroutines.delay(90)
                                soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.55f)
                            }
                        }
                        vibrate(480)
                    }
                    com.example.diwali.model.SutliSoundProfile.MEGA_BOOM_BOSS -> {
                        // Level 25: Brahmand Mega-Boom!
                        if (idExplosion != 0) {
                            soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.52f)
                            soundPool.play(idExplosion, 0.9f, 0.9f, 2, 0, 0.65f)
                        }
                        if (idRocketBlast != 0) {
                            soundPool.play(idRocketBlast, 0.8f, 0.8f, 2, 0, 0.9f)
                        }
                        scope.launch {
                            playTone(frequency = 42.0, durationMs = 800)
                        }
                        vibrate(600)
                    }
                }
            }
            "rocket" -> {
                val pitch = (1.05f - (clampedLevel - 1) * (0.25f / 24f)).coerceIn(0.75f, 1.10f)
                if (idExplosion != 0) {
                    soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, pitch)
                } else if (idRocketBlast != 0) {
                    soundPool.play(idRocketBlast, 1.0f, 1.0f, 3, 0, pitch)
                } else {
                    scope.launch {
                        playTone(frequency = 150.0 * pitch, durationMs = 240)
                    }
                }
                vibrate(100L + (clampedLevel * 4L))
            }
            "sky_shots" -> {
                val pitch = (1.10f - (clampedLevel - 1) * (0.25f / 24f)).coerceIn(0.80f, 1.20f)
                if (idRocketBlast != 0) {
                    soundPool.play(idRocketBlast, 0.95f, 0.95f, 2, 0, pitch)
                }
                if (idExplosion != 0) {
                    soundPool.play(idExplosion, 0.9f, 0.9f, 2, 0, pitch)
                }
                vibrate(70L + (clampedLevel * 4L))
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
            "pencil_whip" -> {
                if (idFuse != 0) {
                    soundPool.play(idFuse, 1.0f, 1.0f, 2, 0, 1.6f)
                }
                scope.launch {
                    repeat(12) {
                        playTone(frequency = 1400.0 + (Math.random() * 800.0), durationMs = 25)
                        kotlinx.coroutines.delay(120)
                    }
                }
                vibrate(40)
            }
            "laxmi_bomb", "paper_bomb" -> {
                val pitch = (1.30f - (clampedLevel - 1) * (0.30f / 24f)).coerceIn(0.90f, 1.35f)
                if (idExplosion != 0) {
                    soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, pitch)
                } else {
                    scope.launch {
                        playTone(frequency = 320.0 * pitch, durationMs = 50)
                        playTone(frequency = 150.0 * pitch, durationMs = 90)
                    }
                }
                vibrate(90L + (clampedLevel * 4L))
            }
            "ladi" -> {
                scope.launch {
                    repeat(18) {
                        if (idExplosion != 0) {
                            soundPool.play(idExplosion, 0.7f, 0.7f, 1, 0, 1.4f + (Math.random() * 0.4f).toFloat())
                        } else {
                            playTone(frequency = 280.0 + (Math.random() * 200.0), durationMs = 30)
                        }
                        vibrate(25)
                        kotlinx.coroutines.delay(110)
                    }
                }
            }
            "snake_tablet" -> {
                if (idFuse != 0) {
                    soundPool.play(idFuse, 0.5f, 0.5f, 1, 0, 0.75f)
                }
                scope.launch {
                    for (f in 400..900 step 50) {
                        playTone(frequency = f.toDouble(), durationMs = 40)
                    }
                }
                vibrate(45)
            }
            "color_sparkler" -> {
                if (idFuse != 0) {
                    soundPool.play(idFuse, 0.9f, 0.9f, 2, 0, 1.35f)
                } else {
                    scope.launch {
                        for (i in 0..12) playTone(frequency = (1200 + (i * 120)).toDouble(), durationMs = 28)
                    }
                }
                vibrate(45)
            }
            "aerial_shell" -> {
                // Initial mortar thump
                if (idRocketBlast != 0) {
                    soundPool.play(idRocketBlast, 0.9f, 0.9f, 2, 0, 0.85f)
                }
                scope.launch {
                    playTone(frequency = 180.0, durationMs = 70)
                    kotlinx.coroutines.delay(1200) // Delay until aerial detonation
                    if (idExplosion != 0) {
                        soundPool.play(idExplosion, 1.0f, 1.0f, 3, 0, 0.8f)
                    } else {
                        playTone(frequency = 120.0, durationMs = 250)
                    }
                    vibrate(140)
                }
                vibrate(60)
            }
            "whiz_wheel" -> {
                if (idFountain != 0) {
                    soundPool.play(idFountain, 0.9f, 0.9f, 2, 0, 1.6f)
                }
                scope.launch {
                    // Rising whistling pitch "Vizzzz"
                    for (f in 800..2400 step 160) {
                        playTone(frequency = f.toDouble(), durationMs = 35)
                    }
                }
                vibrate(70)
            }
            "electric_fountain" -> {
                if (idFountain != 0) {
                    soundPool.play(idFountain, 1.0f, 1.0f, 2, 0, 1.45f)
                } else {
                    scope.launch {
                        for (f in 700..2000 step 140) playTone(frequency = f.toDouble(), durationMs = 40)
                    }
                }
                vibrate(85)
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

    fun release() {
        try {
            soundPool.release()
        } catch (_: Exception) {}
    }
}
