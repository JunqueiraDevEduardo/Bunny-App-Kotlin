package com.example.bunny

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * SoundFx.kt
 * Motor de áudio procedural para TOGETHER:
 * - Sons exclusivos para Bunny (mastigar cenourinha, bolhas de banho, saltos brincalhões)
 * - Sons exclusivos para Cat (ronronar, miau melodioso, espirros de água, carinho)
 * - Beijocas doces ("mwah! ♥") e moedas
 * - 100% sintetizado em PCM sem dependências de ficheiros externos
 */
object SoundFx {
    private const val SAMPLE_RATE = 22050
    private val audioQueue = LinkedBlockingQueue<ShortArray>()
    @Volatile private var isRunning = false
    private var audioThread: Thread? = null

    fun init() {
        if (isRunning) return
        isRunning = true
        audioThread = Thread {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufSize = maxOf(minBuf, 4096)

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val format = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
                AudioTrack(attrs, format, bufSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE)
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufSize,
                    AudioTrack.MODE_STREAM
                )
            }

            try {
                track.play()
                while (isRunning) {
                    val pcm = audioQueue.poll(400, TimeUnit.MILLISECONDS) ?: continue
                    if (Save.soundEnabled) {
                        track.write(pcm, 0, pcm.size)
                    }
                }
                track.stop()
                track.release()
            } catch (e: Exception) {
                // Modo silencioso gracioso se o dispositivo não tiver saída de áudio
            }
        }.apply {
            isDaemon = true
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    private fun postPcm(samples: ShortArray) {
        if (!Save.soundEnabled) return
        if (audioQueue.size < 6) {
            audioQueue.offer(samples)
        }
    }

    // ================================================================= SOM AO COMER
    fun playEat(isCat: Boolean) {
        if (isCat) {
            // Cat: Mastigar delicado + ronronar doce
            val duration = 0.28f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                val chewT = t % 0.07f
                val env = exp(-chewT * 35.0).toFloat()
                val freq = 520f - chewT * 2200f
                val purr = 0.5f + 0.5f * sin(2.0 * PI * 28.0 * t).toFloat()
                val s = sin(2.0 * PI * freq * chewT).toFloat() * env * purr
                buf[i] = (s * 17000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        } else {
            // Bunny: Mastigação rápida e fofa ("nom nom nom nom")
            val duration = 0.26f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                val chewT = t % 0.06f
                val env = exp(-chewT * 40.0).toFloat()
                val freq = 620f - chewT * 2800f
                val s = sin(2.0 * PI * freq * chewT).toFloat() * env
                buf[i] = (s * 19000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        }
    }

    // ================================================================= SOM NO BANHO
    fun playBath(isCat: Boolean) {
        if (isCat) {
            // Cat: Gotas de água suaves + miadinho fofo de espanto
            val duration = 0.22f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                val dropEnv = exp(-(t % 0.05f) * 55.0).toFloat()
                val dropFreq = 780f + sin(2.0 * PI * 35.0 * t).toFloat() * 120f
                val drop = sin(2.0 * PI * dropFreq * t).toFloat() * dropEnv
                val mewFreq = 850f + t * 450f
                val mewEnv = sin(PI * (t / duration)).toFloat()
                val mew = sin(2.0 * PI * mewFreq * t).toFloat() * mewEnv * 0.4f
                buf[i] = ((drop * 0.7f + mew) * 16000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        } else {
            // Bunny: Bolhinhas de sabão a estalar ("pop-pop-pop!")
            val duration = 0.24f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                val popT = t % 0.045f
                val env = exp(-popT * 50.0).toFloat()
                val freq = 450f + (t * 1200f) % 650f
                val s = sin(2.0 * PI * freq * popT).toFloat() * env
                buf[i] = (s * 18000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        }
    }

    // ================================================================= SOM AO BRINCAR
    fun playPlay(isCat: Boolean) {
        if (isCat) {
            // Cat: "Miau!" alegre e melodioso
            val duration = 0.32f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                val frac = t / duration
                // Curva melódica do miado: sobe e desce suavemente
                val freq = 620f + sin(PI * frac).toFloat() * 380f
                val env = sin(PI * frac).toFloat()
                val purr = 0.85f + 0.15f * sin(2.0 * PI * 32.0 * t).toFloat()
                val s = (sin(2.0 * PI * freq * t).toFloat() + 0.3f * sin(4.0 * PI * freq * t).toFloat()) * env * purr
                buf[i] = (s * 18000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        } else {
            // Bunny: Salto animado / apito fofo ("boing!")
            val duration = 0.25f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                val frac = t / duration
                val freq = 420f + frac * 750f
                val env = (1f - frac) * sin(PI * frac).toFloat()
                val s = sin(2.0 * PI * freq * t).toFloat() * env
                buf[i] = (s * 20000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        }
    }

    // ================================================================= BEIJOCAS ("Mwah! ♥")
    fun playKiss() {
        val duration = 0.28f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            if (t < 0.05f) {
                // Sucção rápida do beijo
                val noise = (Random.nextFloat() * 2f - 1f) * (t / 0.05f)
                val pop = sin(2.0 * PI * 420.0 * t).toFloat()
                buf[i] = ((pop * 0.6f + noise * 0.4f) * 19000f).toInt().coerceIn(-32767, 32767).toShort()
            } else {
                // Ressonância doce harmónica
                val remT = t - 0.05f
                val env = exp(-remT * 18.0).toFloat()
                val s1 = sin(2.0 * PI * 920.0 * remT).toFloat()
                val s2 = sin(2.0 * PI * 1380.0 * remT).toFloat() * 0.5f
                buf[i] = ((s1 + s2) * env * 17000f).toInt().coerceIn(-32767, 32767).toShort()
            }
        }
        postPcm(buf)
    }

    // ================================================================= MOEDAS & RECOMPENSAS
    fun playCoin() {
        val duration = 0.24f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            val env = exp(-t * 14.0).toFloat()
            val s1 = sin(2.0 * PI * 1320.0 * t).toFloat()
            val s2 = sin(2.0 * PI * 1760.0 * t).toFloat() * 0.6f
            buf[i] = ((s1 + s2) * env * 16000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        postPcm(buf)
    }

    // ================================================================= CATBUNNYCRAFT (CONSTRUIR & MINAR)
    fun playBuild() {
        val duration = 0.09f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            val env = exp(-t * 45.0).toFloat()
            val s = sin(2.0 * PI * 520.0 * t).toFloat() * env
            buf[i] = (s * 22000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        postPcm(buf)
    }

    fun playMine() {
        val duration = 0.12f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            val env = exp(-t * 30.0).toFloat()
            val noise = (Random.nextFloat() * 2f - 1f) * env * 0.65f
            val thud = sin(2.0 * PI * 180.0 * t).toFloat() * env * 0.35f
            buf[i] = ((noise + thud) * 21000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        postPcm(buf)
    }
}
