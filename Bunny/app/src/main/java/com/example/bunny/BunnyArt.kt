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

    fun furPalette(colorId: String): Triple<Int, Int, Int> = when (colorId) {
        "col_caramel" -> Triple(Color.rgb(215, 150, 95), Color.rgb(248, 222, 192), Color.rgb(235, 175, 145))
        "col_grey" -> Triple(Color.rgb(192, 198, 208), Color.rgb(238, 240, 246), Color.rgb(220, 185, 200))
        "col_pink" -> Triple(Color.rgb(255, 195, 215), Color.rgb(255, 232, 242), Color.rgb(245, 155, 185))
        "col_gold" -> Triple(Color.rgb(250, 200, 70), Color.rgb(255, 235, 160), Color.rgb(240, 165, 80))
        "col_cocoa" -> Triple(Color.rgb(130, 85, 62), Color.rgb(180, 135, 108), Color.rgb(160, 105, 95))
        "col_sky" -> Triple(Color.rgb(185, 218, 245), Color.rgb(230, 244, 255), Color.rgb(215, 185, 225))
        else -> Triple(C.white, Color.rgb(255, 240, 244), C.pink) // col_white default
    }

    fun draw(
        c: Canvas, cx: Float, baseY: Float, s: Float, ps: Pose,
        hat: String, outfit: String, acc: String,
        shoes: String = Save.shoes, bunnyColor: String = Save.bunnyColor
    ) {
        val ol = C.outline
        val lw = max(1.5f, s * 0.014f)
        val (furC, bellyC, earInnerC) = furPalette(bunnyColor)

        c.save()
        c.translate(cx, baseY - ps.hop * s)
        c.scale(1f + ps.squash * 0.08f, 1f - ps.squash * 0.08f)

        // Capa (atrás do coelho)
        if (outfit == "fit_cape") {
            D.poly(c, C.pinkD, ol, lw, -0.28f * s, -0.58f * s, 0.28f * s, -0.58f * s, 0.46f * s, -0.02f * s, -0.46f * s, -0.02f * s)
        }
        // Cauda
        D.circle(c, 0.30f * s, -0.2f * s, 0.1f * s, furC, ol, lw)

        // Orelhas
        val sway = sin(ps.t * 2f) * 3f
        val drop = when (ps.mood) { 2 -> 30f; 3 -> 20f; else -> 0f }
        for (sg in -1..1 step 2) {
            c.save()
            c.translate(sg * 0.17f * s, -1.0f * s)
            c.rotate(sg * (10f + drop) + sway)
            D.oval(c, 0f, -0.30f * s, 0.10f * s, 0.30f * s, furC, ol, lw)
            D.oval(c, 0f, -0.28f * s, 0.05f * s, 0.21f * s, earInnerC)
            c.restore()
        }

        // Corpo
        D.oval(c, 0f, -0.30f * s, 0.34f * s, 0.30f * s, furC, ol, lw)
        D.oval(c, 0f, -0.26f * s, 0.20f * s, 0.19f * s, bellyC)

        // Pés & Sapatilhas
        for (sg in -1..1 step 2) {
            val fx = sg * 0.17f * s
            val fy = -0.04f * s
            D.oval(c, fx, fy, 0.13f * s, 0.07f * s, furC, ol, lw)
            if (shoes.isEmpty()) {
                D.circle(c, fx, fy, 0.03f * s, earInnerC)
            } else {
                drawSneaker(c, fx, fy, s, shoes, ol, lw)
            }
        }

        // Braços
        val armC = if (outfit == "fit_sweater") C.lav else if (outfit == "fit_andina") C.turquoise else furC
        D.oval(c, -0.31f * s, -0.33f * s, 0.075f * s, 0.15f * s, armC, ol, lw)
        D.oval(c, 0.31f * s, -0.33f * s, 0.075f * s, 0.15f * s, armC, ol, lw)

        // Roupa (corpo)
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
            "fit_poncho" -> {
                // Poncho tradicional peruano colorido com padrões incaicos e franjas
                D.poly(c, C.peruRed, ol, lw, 0f, -0.58f * s, 0.36f * s, -0.32f * s, 0f, -0.04f * s, -0.36f * s, -0.32f * s)
                // Risca dourada inca e turquesa
                D.poly(c, C.incaGold, 0, 0f, 0f, -0.48f * s, 0.24f * s, -0.32f * s, 0f, -0.15f * s, -0.24f * s, -0.32f * s)
                D.poly(c, C.turquoise, 0, 0f, 0f, -0.42f * s, 0.16f * s, -0.32f * s, 0f, -0.21f * s, -0.16f * s, -0.32f * s)
                // Franjas andinas
                for (i in -4..4) {
                    val k = i / 4f
                    val fx = k * 0.28f * s
                    val fy = -0.04f * s - kotlin.math.abs(k) * 0.16f * s
                    D.line(c, fx, fy, fx, fy + 0.045f * s, lw * 1.2f, C.incaGold)
                }
            }
            "fit_andina" -> {
                // Camisola andina de lã com motivos geométricos
                D.rrect(c, -0.33f * s, -0.58f * s, 0.33f * s, -0.07f * s, 0.12f * s, C.turquoise, ol, lw)
                D.rrect(c, -0.33f * s, -0.40f * s, 0.33f * s, -0.26f * s, 0.02f * s, C.peruRed)
                for (i in -3..3) {
                    D.poly(c, C.incaGold, 0, 0f, i * 0.08f * s, -0.40f * s, (i + 0.04f) * s, -0.26f * s, (i - 0.04f) * s, -0.26f * s)
                }
            }
        }

        // Sujidade no corpo
        val nd = (ps.dirt * dirtSpots.size).toInt()
        for (i in 0 until nd) {
            val d = dirtSpots[i]
            if (d[1] > -0.7f) D.circle(c, d[0] * s, d[1] * s, d[2] * s, D.alpha(C.brown, 150))
        }

        // Cabeça
        D.oval(c, 0f, -0.78f * s, 0.40f * s, 0.34f * s, furC, ol, lw)
        for (i in 0 until nd) {
            val d = dirtSpots[i]
            if (d[1] <= -0.7f) D.circle(c, d[0] * s, d[1] * s, d[2] * s, D.alpha(C.brown, 150))
        }

        // Cachecol
        if (outfit == "fit_scarf") {
            D.oval(c, 0f, -0.46f * s, 0.27f * s, 0.06f * s, C.pinkD, ol, lw)
            D.rrect(c, 0.10f * s, -0.46f * s, 0.2f * s, -0.18f * s, 0.04f * s, C.pinkD, ol, lw)
            D.line(c, 0.12f * s, -0.28f * s, 0.18f * s, -0.28f * s, lw * 1.5f, C.white)
        }
        if (outfit == "fit_cape") D.circle(c, 0f, -0.5f * s, 0.035f * s, C.gold, ol, lw)

        // Bochechas rosadas
        D.circle(c, -0.26f * s, -0.70f * s, 0.065f * s, D.alpha(C.pinkD, 120))
        D.circle(c, 0.26f * s, -0.70f * s, 0.065f * s, D.alpha(C.pinkD, 120))

        // Olhos
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
                D.circle(c, x + ps.look * 0.02f * s - 0.02f * s, ey - 0.022f * s, 0.022f * s, C.white)
                D.circle(c, x + ps.look * 0.02f * s + 0.018f * s, ey + 0.02f * s, 0.012f * s, C.white)
            }
            if (ps.mood == 2) {
                val outer = x + sg * 0.06f * s
                val inner = x - sg * 0.05f * s
                D.line(c, outer, ey - 0.085f * s, inner, ey - 0.125f * s, lw * 1.4f, C.ink)
            }
        }

        // Narizinho
        D.oval(c, 0f, -0.735f * s, 0.032f * s, 0.024f * s, C.pinkD)

        // =================================================================
        // BOCA E DENTES: DENTES PARA BAIXO!
        // Os dentes de coelho penduram-se do lábio superior e descem para baixo!
        // =================================================================
        val my = -0.665f * s
        // Sulco do nariz ao lábio (filtro)
        D.line(c, 0f, -0.71f * s, 0f, my + 0.005f * s, lw * 1.1f, ol)

        if (ps.mouth > 0.05f) {
            // Boca aberta a comer (com dentes pendurados no bordo superior para baixo)
            val openH = (0.025f + 0.05f * ps.mouth) * s
            D.oval(c, 0f, my + openH * 0.4f, 0.055f * s, openH, C.pinkD, ol, lw)
            // Dentes para baixo no topo da boca aberta
            D.rrect(c, -0.026f * s, my - 0.01f * s, -0.002f * s, my + 0.035f * s, 0.008f * s, C.white, ol, lw * 0.7f)
            D.rrect(c, 0.002f * s, my - 0.01f * s, 0.026f * s, my + 0.035f * s, 0.008f * s, C.white, ol, lw * 0.7f)
        } else {
            // Dentes de coelho pendurados para baixo a partir do lábio superior
            if (ps.mood <= 1) {
                val toothTop = my - 0.004f * s
                val toothBottom = my + 0.052f * s // Estão nitidamente PARA BAIXO!
                D.rrect(c, -0.026f * s, toothTop, -0.002f * s, toothBottom, 0.008f * s, C.white, ol, lw * 0.8f)
                D.rrect(c, 0.002f * s, toothTop, 0.026f * s, toothBottom, 0.008f * s, C.white, ol, lw * 0.8f)
            }
            // Linha do sorriso / lábio superior
            D.path.reset()
            if (ps.mood == 2) {
                // Triste
                D.path.moveTo(-0.055f * s, my + 0.03f * s)
                D.path.quadTo(0f, my - 0.015f * s, 0.055f * s, my + 0.03f * s)
            } else {
                // Sorriso fofo de coelhinho
                D.path.moveTo(-0.06f * s, my - 0.005f * s)
                D.path.quadTo(-0.03f * s, my + 0.012f * s, 0f, my + 0.002f * s)
                D.path.quadTo(0.03f * s, my + 0.012f * s, 0.06f * s, my - 0.005f * s)
            }
            D.s(ol, lw * 1.3f); c.drawPath(D.path, D.p)
        }

        // Bigodes
        for (sg in -1..1 step 2) {
            D.line(c, sg * 0.22f * s, -0.73f * s, sg * 0.42f * s, -0.76f * s, lw * 0.8f, D.alpha(ol, 180))
            D.line(c, sg * 0.22f * s, -0.70f * s, sg * 0.42f * s, -0.68f * s, lw * 0.8f, D.alpha(ol, 180))
        }

        // Espuma de banho
        val nf = (ps.foam * foamSpots.size).toInt()
        for (i in 0 until nf) {
            val f = foamSpots[i]
            D.circle(c, f[0] * s, f[1] * s, f[2] * s, D.alpha(C.white, 235), D.alpha(C.sky, 255), lw)
        }

        // Acessórios
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

        // Chapéus (inclui cultura peruana)
        when (hat) {
            "hat_chullo" -> {
                // Chullo peruano autêntico com abas, borlas e padrões andinos
                D.poly(c, C.peruRed, ol, lw, -0.36f * s, -1.02f * s, 0.36f * s, -1.02f * s, 0f, -1.45f * s)
                // Padrão geométrico inca
                D.rrect(c, -0.34f * s, -1.16f * s, 0.34f * s, -1.08f * s, 0.02f * s, C.incaGold)
                for (i in -2..2) {
                    D.poly(c, C.turquoise, 0, 0f, i * 0.12f * s, -1.16f * s, (i + 0.05f) * s, -1.08f * s, (i - 0.05f) * s, -1.08f * s)
                }
                // Abas para as orelhas
                D.poly(c, C.peruRed, ol, lw, -0.34f * s, -1.02f * s, -0.18f * s, -1.02f * s, -0.26f * s, -0.84f * s)
                D.poly(c, C.peruRed, ol, lw, 0.34f * s, -1.02f * s, 0.18f * s, -1.02f * s, 0.26f * s, -0.84f * s)
                // Pompons / borlas
                D.circle(c, 0f, -1.48f * s, 0.09f * s, C.incaGold, ol, lw * 0.8f)
                D.circle(c, -0.26f * s, -0.82f * s, 0.045f * s, C.turquoise)
                D.circle(c, 0.26f * s, -0.82f * s, 0.045f * s, C.turquoise)
            }
            "hat_montera" -> {
                // Montera peruana tradicional
                D.oval(c, 0f, -1.12f * s, 0.44f * s, 0.11f * s, C.peruRed, C.ink, lw * 1.2f)
                D.oval(c, 0f, -1.16f * s, 0.25f * s, 0.08f * s, C.peruRedD, ol, lw)
                // Fitas coloridas andinas a cair pelas costas
                D.line(c, -0.22f * s, -1.08f * s, -0.28f * s, -0.82f * s, lw * 2f, C.incaGold)
                D.line(c, 0.22f * s, -1.08f * s, 0.28f * s, -0.82f * s, lw * 2f, C.turquoise)
            }
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

    /** Desenha sapatilhas / calçado nos pés do coelhinho. */
    private fun drawSneaker(c: Canvas, fx: Float, fy: Float, s: Float, shoeId: String, ol: Int, lw: Float) {
        val w = 0.14f * s
        val h = 0.075f * s
        when (shoeId) {
            "shoe_andean" -> {
                // Ténis andinos artesanais com riscas coloridas
                D.oval(c, fx, fy, w, h, C.peruRed, ol, lw)
                D.rrect(c, fx - w * 0.6f, fy - h * 0.3f, fx + w * 0.6f, fy + h * 0.3f, 0.02f * s, C.incaGold)
                D.line(c, fx - w * 0.5f, fy, fx + w * 0.5f, fy, lw * 1.5f, C.turquoise)
                // Sola de borracha branca
                D.rrect(c, fx - w, fy + h * 0.25f, fx + w, fy + h * 0.8f, 0.02f * s, C.white, ol, lw * 0.7f)
                D.circle(c, fx, fy - h * 0.2f, 0.018f * s, C.white)
            }
            "shoe_red" -> {
                // Sapatilhas vermelhas clássicas
                D.oval(c, fx, fy, w, h, C.peruRed, ol, lw)
                D.rrect(c, fx - w, fy + h * 0.25f, fx + w, fy + h * 0.8f, 0.02f * s, C.white, ol, lw * 0.7f)
                D.oval(c, fx, fy - h * 0.1f, w * 0.5f, h * 0.35f, C.white, ol, lw * 0.6f)
                D.line(c, fx - 0.03f * s, fy, fx + 0.03f * s, fy, lw * 1.4f, C.peruRed)
            }
            "shoe_blue" -> {
                // Sapatilhas azuis desportivas
                D.oval(c, fx, fy, w, h, C.blue, ol, lw)
                D.rrect(c, fx - w, fy + h * 0.25f, fx + w, fy + h * 0.8f, 0.02f * s, C.white, ol, lw * 0.7f)
                D.line(c, fx - 0.04f * s, fy - h * 0.2f, fx + 0.04f * s, fy - h * 0.2f, lw * 1.4f, C.white)
            }
            "shoe_boots" -> {
                // Botas quentes de lã andina
                D.oval(c, fx, fy + 0.01f * s, w, h * 1.1f, C.brown, ol, lw)
                // Rebordo fofo de lã de ovelha / alpaca
                D.oval(c, fx, fy - h * 0.55f, w * 0.85f, h * 0.45f, C.white, ol, lw * 0.7f)
            }
            "shoe_gold" -> {
                // Ténis dourados de campeão
                D.oval(c, fx, fy, w, h, C.incaGold, ol, lw)
                D.rrect(c, fx - w, fy + h * 0.25f, fx + w, fy + h * 0.8f, 0.02f * s, C.white, ol, lw * 0.7f)
                Icons.drawSpark(c, fx, fy - h * 0.15f, 0.025f * s)
            }
        }
    }
}
