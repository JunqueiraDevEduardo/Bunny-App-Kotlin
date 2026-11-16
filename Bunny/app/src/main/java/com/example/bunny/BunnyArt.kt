package com.example.bunny

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.max
import kotlin.math.sin

/** Estado visual do coelho. mood: 0 feliz, 1 neutro, 2 triste, 3 a dormir. hop em unidades de s. */
class Pose {
    var mood = 0
    var t = 0f
    var hop = 0f
    var squash = 0f
    var dirt = 0f
    var foam = 0f
    var mouth = 0f
    var look = 0f
}

object BunnyArt {
    private val dirtSpots = arrayOf(
        floatArrayOf(-0.20f, -0.40f, 0.055f), floatArrayOf(0.15f, -0.22f, 0.045f), floatArrayOf(0.22f, -0.45f, 0.05f),
        floatArrayOf(-0.30f, -0.88f, 0.045f), floatArrayOf(0.28f, -0.95f, 0.04f), floatArrayOf(-0.05f, -0.15f, 0.04f),
        floatArrayOf(0.02f, -0.98f, 0.035f), floatArrayOf(-0.12f, -0.55f, 0.04f)
    )
    private val foamSpots = arrayOf(
        floatArrayOf(-0.25f, -0.42f, 0.09f), floatArrayOf(0.2f, -0.35f, 0.1f), floatArrayOf(0.0f, -0.25f, 0.11f),
        floatArrayOf(-0.3f, -0.9f, 0.08f), floatArrayOf(0.3f, -0.92f, 0.09f), floatArrayOf(0.0f, -1.05f, 0.08f),
        floatArrayOf(-0.15f, -0.5f, 0.07f), floatArrayOf(0.12f, -0.55f, 0.08f), floatArrayOf(-0.38f, -0.7f, 0.07f),
        floatArrayOf(0.38f, -0.72f, 0.07f), floatArrayOf(0.0f, -0.62f, 0.09f), floatArrayOf(-0.2f, -1.0f, 0.06f),
        floatArrayOf(0.22f, -0.2f, 0.07f), floatArrayOf(-0.1f, -0.9f, 0.06f)
    )

    fun draw(c: Canvas, cx: Float, baseY: Float, s: Float, ps: Pose, hat: String, outfit: String, acc: String) {
        val ol = C.outline
        val lw = max(1.5f, s * 0.014f)
        c.save()
        c.translate(cx, baseY - ps.hop * s)
        c.scale(1f + ps.squash * 0.08f, 1f - ps.squash * 0.08f)

        // capa (atrás)
        if (outfit == "fit_cape") {
            D.poly(c, C.pinkD, ol, lw, -0.28f * s, -0.58f * s, 0.28f * s, -0.58f * s, 0.46f * s, -0.02f * s, -0.46f * s, -0.02f * s)
        }
        // cauda
        D.circle(c, 0.30f * s, -0.2f * s, 0.1f * s, C.white, ol, lw)

        // orelhas
        val sway = sin(ps.t * 2f) * 3f
        val drop = when (ps.mood) { 2 -> 30f; 3 -> 20f; else -> 0f }
        for (sg in -1..1 step 2) {
            c.save()
            c.translate(sg * 0.17f * s, -1.0f * s)
            c.rotate(sg * (10f + drop) + sway)
            D.oval(c, 0f, -0.30f * s, 0.10f * s, 0.30f * s, C.white, ol, lw)
            D.oval(c, 0f, -0.28f * s, 0.05f * s, 0.21f * s, C.pink)
            c.restore()
        }

        // corpo
        D.oval(c, 0f, -0.30f * s, 0.34f * s, 0.30f * s, C.white, ol, lw)
        D.oval(c, 0f, -0.26f * s, 0.20f * s, 0.19f * s, Color.rgb(255, 240, 244))
        // pés
        D.oval(c, -0.17f * s, -0.04f * s, 0.13f * s, 0.07f * s, C.white, ol, lw)
        D.oval(c, 0.17f * s, -0.04f * s, 0.13f * s, 0.07f * s, C.white, ol, lw)
        D.circle(c, -0.17f * s, -0.04f * s, 0.03f * s, C.pink)
        D.circle(c, 0.17f * s, -0.04f * s, 0.03f * s, C.pink)
        // braços
        val armC = if (outfit == "fit_sweater") C.lav else C.white
        D.oval(c, -0.31f * s, -0.33f * s, 0.075f * s, 0.15f * s, armC, ol, lw)
        D.oval(c, 0.31f * s, -0.33f * s, 0.075f * s, 0.15f * s, armC, ol, lw)

        // roupa (corpo)
        when (outfit) {
            "fit_overall" -> {
                D.rrect(c, -0.29f * s, -0.50f * s, 0.29f * s, -0.08f * s, 0.1f * s, C.blue, ol, lw)
                D.rrect(c, -0.14f * s, -0.52f * s, 0.14f * s, -0.34f * s, 0.04f * s, D.mix(C.blue, C.white, 0.3f), ol, lw)
                D.line(c, -0.12f * s, -0.52f * s, -0.14f * s, -0.62f * s, lw * 2f, C.blue)
                D.line(c, 0.12f * s, -0.52f * s, 0.14f * s, -0.62f * s, lw * 2f, C.blue)
                D.circle(c, -0.13f * s, -0.45f * s, 0.022f * s, C.gold)
                D.circle(c, 0.13f * s, -0.45f * s, 0.022f * s, C.gold)
            }
            "fit_sweater" -> {
                D.rrect(c, -0.33f * s, -0.58f * s, 0.33f * s, -0.07f * s, 0.12f * s, C.lav, ol, lw)
                for (i in 0 until 3) D.line(c, -0.3f * s, (-0.45f + i * 0.13f) * s, 0.3f * s, (-0.45f + i * 0.13f) * s, lw * 2f, C.white)
            }
        }

        // sujidade no corpo
        val nd = (ps.dirt * dirtSpots.size).toInt()
        for (i in 0 until nd) {
            val d = dirtSpots[i]
            if (d[1] > -0.7f) D.circle(c, d[0] * s, d[1] * s, d[2] * s, D.alpha(C.brown, 150))
        }

        // cabeça
        D.oval(c, 0f, -0.78f * s, 0.40f * s, 0.34f * s, C.white, ol, lw)
        for (i in 0 until nd) {
            val d = dirtSpots[i]
            if (d[1] <= -0.7f) D.circle(c, d[0] * s, d[1] * s, d[2] * s, D.alpha(C.brown, 150))
        }

        // cachecol (por cima do queixo)
        if (outfit == "fit_scarf") {
            D.oval(c, 0f, -0.46f * s, 0.27f * s, 0.06f * s, C.pinkD, ol, lw)
            D.rrect(c, 0.10f * s, -0.46f * s, 0.2f * s, -0.18f * s, 0.04f * s, C.pinkD, ol, lw)
            D.line(c, 0.12f * s, -0.28f * s, 0.18f * s, -0.28f * s, lw * 1.5f, C.white)
        }
        if (outfit == "fit_cape") D.circle(c, 0f, -0.5f * s, 0.035f * s, C.gold, ol, lw)

        // cara
        D.circle(c, -0.26f * s, -0.70f * s, 0.065f * s, D.alpha(C.pinkD, 120))
        D.circle(c, 0.26f * s, -0.70f * s, 0.065f * s, D.alpha(C.pinkD, 120))
        val ey = -0.81f * s
        val ex = 0.155f * s
        val blink = (ps.t % 4.2f) < 0.12f
        for (sg in -1..1 step 2) {
            val x = sg * ex
            if (ps.mood == 3 || blink) {
                D.path.reset(); D.path.moveTo(x - 0.05f * s, ey); D.path.quadTo(x, ey + 0.05f * s, x + 0.05f * s, ey)
                D.s(C.ink, lw * 1.6f); c.drawPath(D.path, D.p)
            } else {
                D.circle(c, x + ps.look * 0.02f * s, ey, 0.058f * s, C.ink)
                D.circle(c, x + ps.look * 0.02f * s - 0.02f * s, ey - 0.022f * s, 0.02f * s, C.white)
                D.circle(c, x + ps.look * 0.02f * s + 0.018f * s, ey + 0.02f * s, 0.01f * s, C.white)
            }
            if (ps.mood == 2) {
                val outer = x + sg * 0.06f * s
                val inner = x - sg * 0.05f * s
                D.line(c, outer, ey - 0.085f * s, inner, ey - 0.125f * s, lw * 1.4f, C.ink)
            }
        }
        D.oval(c, 0f, -0.735f * s, 0.032f * s, 0.024f * s, C.pinkD)
        val my = -0.655f * s
        if (ps.mouth > 0.05f) {
            D.oval(c, 0f, my + 0.01f * s, 0.05f * s, (0.02f + 0.045f * ps.mouth) * s, C.pinkD, ol, lw)
        } else {
            if (ps.mood <= 1) {
                D.rrect(c, -0.028f * s, -0.70f * s, -0.002f * s, -0.648f * s, 0.01f * s, C.white, ol, lw * 0.8f)
                D.rrect(c, 0.002f * s, -0.70f * s, 0.028f * s, -0.648f * s, 0.01f * s, C.white, ol, lw * 0.8f)
            }
            D.path.reset()
            if (ps.mood == 2) {
                D.path.moveTo(-0.05f * s, my + 0.02f * s); D.path.quadTo(0f, my - 0.025f * s, 0.05f * s, my + 0.02f * s)
            } else {
                D.path.moveTo(-0.055f * s, my - 0.005f * s); D.path.quadTo(0f, my + 0.04f * s, 0.055f * s, my - 0.005f * s)
            }
            D.s(ol, lw * 1.2f); c.drawPath(D.path, D.p)
        }
        // bigodes
        for (sg in -1..1 step 2) {
            D.line(c, sg * 0.22f * s, -0.73f * s, sg * 0.42f * s, -0.76f * s, lw * 0.8f, D.alpha(ol, 180))
            D.line(c, sg * 0.22f * s, -0.70f * s, sg * 0.42f * s, -0.68f * s, lw * 0.8f, D.alpha(ol, 180))
        }

        // espuma
        val nf = (ps.foam * foamSpots.size).toInt()
        for (i in 0 until nf) {
            val f = foamSpots[i]
            D.circle(c, f[0] * s, f[1] * s, f[2] * s, D.alpha(C.white, 235), D.alpha(C.sky, 255), lw)
        }

        // acessórios
        when (acc) {
            "acc_glasses" -> {
                D.s(C.ink, lw * 1.3f)
                c.drawCircle(-ex, ey, 0.085f * s, D.p)
                c.drawCircle(ex, ey, 0.085f * s, D.p)
                D.line(c, -ex + 0.085f * s, ey, ex - 0.085f * s, ey, lw * 1.3f, C.ink)
            }
            "acc_bow" -> {
                D.poly(c, C.lav, ol, lw, 0f, -0.45f * s, -0.12f * s, -0.52f * s, -0.12f * s, -0.38f * s)
                D.poly(c, C.lav, ol, lw, 0f, -0.45f * s, 0.12f * s, -0.52f * s, 0.12f * s, -0.38f * s)
                D.circle(c, 0f, -0.45f * s, 0.035f * s, C.pinkD, ol, lw)
            }
            "acc_pearls" -> {
                for (i in 0 until 7) {
                    val a = -0.24f + i * 0.08f
                    D.circle(c, a * s, (-0.46f + 0.07f * (1f - (a / 0.24f) * (a / 0.24f))) * s, 0.027f * s, C.white, ol, lw * 0.7f)
                }
            }
        }

        // chapéus
        when (hat) {
            "hat_bow" -> {
                D.poly(c, C.pinkD, ol, lw, 0.24f * s, -1.1f * s, 0.10f * s, -1.2f * s, 0.10f * s, -1.0f * s)
                D.poly(c, C.pinkD, ol, lw, 0.24f * s, -1.1f * s, 0.38f * s, -1.2f * s, 0.38f * s, -1.0f * s)
                D.circle(c, 0.24f * s, -1.1f * s, 0.04f * s, C.pink, ol, lw)
            }
            "hat_flower" -> {
                for (i in 0 until 6) {
                    val k = i / 5f
                    val x = (-0.32f + 0.64f * k) * s
                    val y = (-1.04f + 0.1f * (2f * k - 1f) * (2f * k - 1f)) * s
                    D.circle(c, x, y + 0.02f * s, 0.05f * s, C.greenD)
                    D.circle(c, x, y, 0.06f * s, if (i % 2 == 0) C.pink else C.lav, ol, lw * 0.8f)
                    D.circle(c, x, y, 0.022f * s, C.butter)
                }
            }
            "hat_straw" -> {
                D.oval(c, 0f, -1.08f * s, 0.5f * s, 0.085f * s, C.butter, C.gold, lw)
                D.oval(c, 0f, -1.17f * s, 0.26f * s, 0.15f * s, C.butter, C.gold, lw)
                D.rrect(c, -0.26f * s, -1.14f * s, 0.26f * s, -1.07f * s, 0.02f * s, C.pinkD)
            }
            "hat_party" -> {
                D.poly(c, C.lav, ol, lw, -0.15f * s, -1.07f * s, 0.15f * s, -1.07f * s, 0f, -1.6f * s)
                D.circle(c, -0.04f * s, -1.2f * s, 0.025f * s, C.white)
                D.circle(c, 0.05f * s, -1.3f * s, 0.025f * s, C.pink)
                D.circle(c, -0.01f * s, -1.4f * s, 0.022f * s, C.butter)
                D.circle(c, 0f, -1.62f * s, 0.05f * s, C.pinkD, ol, lw)
            }
            "hat_crown" -> {
                D.poly(c, C.gold, C.brown, lw, -0.2f * s, -1.07f * s, 0.2f * s, -1.07f * s, 0.22f * s, -1.28f * s, 0.1f * s, -1.2f * s,
                    0f, -1.32f * s, -0.1f * s, -1.2f * s, -0.22f * s, -1.28f * s)
                D.circle(c, 0f, -1.15f * s, 0.028f * s, C.red)
            }
            "hat_beanie" -> {
                D.oval(c, 0f, -1.1f * s, 0.34f * s, 0.2f * s, C.mint, ol, lw)
                D.rrect(c, -0.35f * s, -1.07f * s, 0.35f * s, -0.97f * s, 0.05f * s, C.greenD, ol, lw)
                D.circle(c, 0f, -1.3f * s, 0.06f * s, C.white, ol, lw)
            }
        }
        c.restore()
    }
}
