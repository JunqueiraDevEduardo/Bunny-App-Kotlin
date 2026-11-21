package com.example.bunny

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * SoundFx.kt
 * Motor de áudio acústico e procedural de alta fidelidade para TOGETHER:
 * - GATINHA (Cat):
 *     * Miado melodioso autêntico com formantes vocálicos ("M-i-a-u! :3")
 *     * Ronronar profundo ("run-run-run-run") para dormir com flutter de 26Hz
 * - COELHINHO (Bunny):
 *     * Mastigar real de coelho: "crunch-crunch-crunch" estaladiço de cenoura/vegetais
 *     * Som de sono/contentamento real: "tooth purring" suave com respiração tranquila
 * - Kart e Efeitos:
 *     * Turbo whoosh, motor acelerado, minar, construir, moedas e beijinhos doces
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
                // Modo silencioso gracioso
            }
        }.apply {
            isDaemon = true
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    private fun postPcm(samples: ShortArray) {
        if (!Save.soundEnabled) return
        if (audioQueue.size < 8) {
            audioQueue.offer(samples)
        }
    }

    // ================================================================= SOM AO COMER (REALISTA)
    fun playEat(isCat: Boolean) {
        if (isCat) {
            // Cat: Lambidela macia e ronronar satisfeito ("slurp-purr")
            val duration = 0.32f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                // Duas lambidelas suaves seguidas de ronronar
                val lickPhase = (t % 0.14f) / 0.14f
                val env = sin(PI * lickPhase).toFloat()
                val freq = 480f - lickPhase * 160f
                val purrMod = 0.5f + 0.5f * sin(2.0 * PI * 28.0 * t).toFloat()
                val lick = sin(2.0 * PI * freq * t).toFloat() * 0.6f +
                        0.25f * sin(4.0 * PI * freq * t).toFloat()
                buf[i] = (lick * env * purrMod * 19000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        } else {
            // Bunny: Mastigação REAL de coelho a comer cenourinha fresca!
            // Rápido "crunch-crunch-crunch-crunch" com estalidos crocantes característicos
            val duration = 0.38f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            val rnd = Random(42)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                val biteIdx = (t / 0.085f).toInt()
                val biteT = (t % 0.085f)
                val env = exp(-biteT * 48.0).toFloat()

                // Ruído crocante filtrado (cenoura estaladiça)
                val noise = (rnd.nextFloat() * 2f - 1f) * env
                // Impacto dos dentes incisivos do coelho
                val toothClick = sin(2.0 * PI * (1950f + biteIdx * 180f) * biteT).toFloat() * exp(-biteT * 95.0).toFloat()
                val crunch = noise * 0.65f + toothClick * 0.35f
                buf[i] = (crunch * 23000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        }
    }

    // ================================================================= SOM AO DORMIR (RON-RON & PURR)
    fun playSleep(isCat: Boolean) {
        if (isCat) {
            // Gatinha a dormir: Ronronar rítmico profundo e reconfortante ("run... run... run... run...")
            // Modulação de 25 Hz típica do músculo laríngeo felino sobre frequência fundamental de 70 Hz
            val duration = 0.75f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                val breatheEnv = (0.35f + 0.65f * sin(2.0 * PI * 1.35 * t).toFloat().coerceAtLeast(0f))
                val flutter = (0.5f + 0.5f * sin(2.0 * PI * 26.0 * t).toFloat())
                val fundamental = sin(2.0 * PI * 72.0 * t).toFloat()
                val harm2 = 0.45f * sin(2.0 * PI * 144.0 * t).toFloat()
                val harm3 = 0.20f * sin(2.0 * PI * 216.0 * t).toFloat()
                val purr = (fundamental + harm2 + harm3) * flutter * breatheEnv
                buf[i] = (purr * 22000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        } else {
            // Bunny a dormir: Ronromar rítmico suave, profundo e reconfortante ("rum... rum... rum...")
            // Sem estalidos de dentes nem ruídos: purr harmónico doce e relaxante com modulação suave
            val duration = 0.78f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                // Ciclo respiratório suave e calmante
                val breatheEnv = (0.30f + 0.70f * sin(2.0 * PI * 1.25 * t).toFloat().coerceAtLeast(0f))
                // Vibração de ronronar quente (22 Hz)
                val flutter = (0.5f + 0.5f * sin(2.0 * PI * 22.0 * t).toFloat())
                // Frequências fundamentais graves e quentes (65 Hz + harmónicos ricos)
                val fundamental = sin(2.0 * PI * 65.0 * t).toFloat()
                val harm2 = 0.42f * sin(2.0 * PI * 130.0 * t).toFloat()
                val harm3 = 0.16f * sin(2.0 * PI * 195.0 * t).toFloat()
                val purr = (fundamental + harm2 + harm3) * flutter * breatheEnv
                buf[i] = (purr * 21000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        }
    }

    // ================================================================= MIADO REAL DA GATINHA ("M-I-A-U! :3")
    fun playCatMeow() {
        val duration = 0.42f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            val p = t / duration

            // Curva melódica natural: inicia em 350Hz (m-), sobe para 860Hz (-ia-), finaliza em 420Hz (-u)
            val freq = when {
                p < 0.25f -> 360f + (p / 0.25f) * 500f
                p < 0.65f -> 860f + sin((p - 0.25f) / 0.40f * PI).toFloat() * 80f
                else -> 860f - ((p - 0.65f) / 0.35f) * 440f
            }

            // Envelope de volume suave sem estalidos
            val env = when {
                p < 0.12f -> p / 0.12f
                p > 0.70f -> (1f - p) / 0.30f
                else -> 1.0f
            }

            // Formantes e harmónicos felinos quentes
            val f1 = sin(2.0 * PI * freq * t).toFloat()
            val f2 = 0.38f * sin(4.0 * PI * freq * t).toFloat()
            val f3 = 0.15f * sin(6.0 * PI * freq * t).toFloat()
            val meow = (f1 + f2 + f3) * env
            buf[i] = (meow * 20000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        postPcm(buf)
    }

    // ================================================================= SOM NO BANHO (SILENCIOSO)
    fun playBath(isCat: Boolean) {
        // Silêncio completo no banho/duche a pedido do utilizador
    }

    // ================================================================= SOM AO BRINCAR
    fun playPlay(isCat: Boolean) {
        if (isCat) {
            playCatMeow()
        } else {
            // Bunny: Salto elástico e animado ("boing!")
            val duration = 0.26f
            val count = (SAMPLE_RATE * duration).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toFloat()
                val frac = t / duration
                val freq = 460f + frac * 850f
                val env = (1f - frac) * sin(PI * frac).toFloat()
                val s = sin(2.0 * PI * freq * t).toFloat() * env
                buf[i] = (s * 21000f).toInt().coerceIn(-32767, 32767).toShort()
            }
            postPcm(buf)
        }
    }

    // ================================================================= BEIJOCAS DOCES ("Mwah! ♥")
    fun playKiss() {
        val duration = 0.28f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            if (t < 0.05f) {
                val noise = (Random.nextFloat() * 2f - 1f) * (t / 0.05f)
                val pop = sin(2.0 * PI * 440.0 * t).toFloat()
                buf[i] = ((pop * 0.6f + noise * 0.4f) * 20000f).toInt().coerceIn(-32767, 32767).toShort()
            } else {
                val remT = t - 0.05f
                val env = exp(-remT * 18.0).toFloat()
                val s1 = sin(2.0 * PI * 960.0 * remT).toFloat()
                val s2 = sin(2.0 * PI * 1440.0 * remT).toFloat() * 0.5f
                buf[i] = ((s1 + s2) * env * 18000f).toInt().coerceIn(-32767, 32767).toShort()
            }
        }
        postPcm(buf)
    }

    // ================================================================= MOEDAS
    fun playCoin() {
        val duration = 0.24f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            val env = exp(-t * 14.0).toFloat()
            val s1 = sin(2.0 * PI * 1320.0 * t).toFloat()
            val s2 = sin(2.0 * PI * 1760.0 * t).toFloat() * 0.6f
            buf[i] = ((s1 + s2) * env * 17000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        postPcm(buf)
    }

    // ================================================================= CATBUNNYCRAFT (CONSTRUIR & MINAR)
    fun playBuild() {
        val duration = 0.10f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            val env = exp(-t * 40.0).toFloat()
            val s = sin(2.0 * PI * 580.0 * t).toFloat() * env
            buf[i] = (s * 22000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        postPcm(buf)
    }

    fun playMine() {
        val duration = 0.14f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        val rnd = Random(55)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            val env = exp(-t * 26.0).toFloat()
            val noise = (rnd.nextFloat() * 2f - 1f) * env * 0.65f
            val thud = sin(2.0 * PI * 190.0 * t).toFloat() * env * 0.35f
            buf[i] = ((noise + thud) * 22000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        postPcm(buf)
    }

    // ================================================================= KART RACER (TURBO & MOTOR)
    fun playTurbo() {
        val duration = 0.35f
        val count = (SAMPLE_RATE * duration).toInt()
        val buf = ShortArray(count)
        val rnd = Random(88)
        for (i in 0 until count) {
            val t = i / SAMPLE_RATE.toFloat()
            val frac = t / duration
            val freq = 350f + frac * 900f
            val noise = (rnd.nextFloat() * 2f - 1f) * (0.3f + 0.7f * frac)
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val env = sin(PI * frac).toFloat()
            buf[i] = ((tone * 0.6f + noise * 0.4f) * env * 22000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        postPcm(buf)
    }
}
