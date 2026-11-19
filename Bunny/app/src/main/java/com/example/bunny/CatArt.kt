package com.example.bunny

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.max
import kotlin.math.sin

object CatArt {
    /**
     * Desenha a gatinha fofinha lindinha companheira do Bunny.
     * @param cx centro X
     * @param baseY chão/base Y
     * @param s escala da gatinha
     * @param t tempo para animações (abana cauda, piscar, respiração)
     * @param mood 0 feliz/ronronar, 1 curiosa, 2 soninho
     * @param pounce salto ao brincar (0f repouso, >0f no ar)
     */
    fun draw(
        c: Canvas, cx: Float, baseY: Float, s: Float, t: Float,
        mood: Int = 0, pounce: Float = 0f, lookX: Float = 0f
    ) {
        val ol = C.outline
        val lw = max(1.2f, s * 0.015f)
        val breathe = sin(t * 3f) * 0.03f

        c.save()
        c.translate(cx, baseY - pounce * s)
        c.scale(1f + breathe, 1f - breathe)

        // Cauda fofa e curva a abanar suavemente
        val tailAngle = sin(t * 3.5f) * 20f
        c.save()
        c.translate(-0.24f * s, -0.15f * s)
        c.rotate(tailAngle)
        D.path.reset()
        D.path.moveTo(0f, 0f)
        D.path.quadTo(-0.25f * s, -0.15f * s, -0.18f * s, -0.42f * s)
        D.path.quadTo(-0.1f * s, -0.55f * s, 0f, -0.45f * s)
        D.path.quadTo(-0.06f * s, -0.2f * s, 0.06f * s, -0.05f * s)
        D.path.close()
        D.f(C.catFur); c.drawPath(D.path, D.p)
        D.s(ol, lw); c.drawPath(D.path, D.p)
        // Pontinha da cauda
        D.circle(c, -0.1f * s, -0.46f * s, 0.06f * s, C.catSpot)
        c.restore()

        // Patinhas traseiras
        D.oval(c, -0.20f * s, -0.04f * s, 0.12f * s, 0.07f * s, C.catFur, ol, lw)
        D.oval(c, 0.20f * s, -0.04f * s, 0.12f * s, 0.07f * s, C.catFur, ol, lw)

        // Corpo redondinho e fofo
        D.oval(c, 0f, -0.26f * s, 0.28f * s, 0.25f * s, C.catFur, ol, lw)
        // Barriguinha branca
        D.oval(c, 0f, -0.22f * s, 0.18f * s, 0.17f * s, C.white)

        // Patinhas dianteiras
        D.oval(c, -0.09f * s, -0.03f * s, 0.065f * s, 0.08f * s, C.white, ol, lw)
        D.oval(c, 0.09f * s, -0.03f * s, 0.065f * s, 0.08f * s, C.white, ol, lw)

        // Coleira fofa com guizo dourado
        D.oval(c, 0f, -0.44f * s, 0.22f * s, 0.05f * s, C.peruRed, ol, lw * 0.8f)
        // Guizo dourado com ranhura
        D.circle(c, 0f, -0.41f * s, 0.045f * s, C.incaGold, ol, lw * 0.8f)
        D.circle(c, 0f, -0.41f * s, 0.015f * s, C.ink)

        // Orelhas pontiagudas de gatinha
        val earTwitch = sin(t * 1.5f) * 2f
        for (sg in -1..1 step 2) {
            c.save()
            c.translate(sg * 0.18f * s, -0.72f * s)
            c.rotate(sg * (12f + earTwitch))
            D.poly(c, C.catFur, ol, lw, -0.14f * s, 0.05f * s, 0.14f * s, 0.05f * s, 0f, -0.28f * s)
            D.poly(c, C.pink, 0, 0f, -0.08f * s, 0.03f * s, 0.08f * s, 0.03f * s, 0f, -0.20f * s)
            c.restore()
        }

        // Cabeça redondinha e fofa
        D.circle(c, 0f, -0.62f * s, 0.32f * s, C.catFur, ol, lw)
        // Mancha fofa de gatinha tricolor
        D.oval(c, -0.15f * s, -0.75f * s, 0.14f * s, 0.10f * s, C.catSpot)

        // Bochechinhas coradas
        D.circle(c, -0.20f * s, -0.56f * s, 0.055f * s, D.alpha(C.pinkD, 115))
        D.circle(c, 0.20f * s, -0.56f * s, 0.055f * s, D.alpha(C.pinkD, 115))

        // Olhos da gatinha
        val ey = -0.64f * s
        val blink = (t % 3.6f) < 0.12f
        for (sg in -1..1 step 2) {
            val ex = sg * 0.13f * s
            if (mood == 2 || blink) {
                // Olhos fechados felizes / a ronronar (^.^)
                D.path.reset()
                D.path.moveTo(ex - 0.05f * s, ey)
                D.path.quadTo(ex, ey - 0.045f * s, ex + 0.05f * s, ey)
                D.s(C.ink, lw * 1.6f); c.drawPath(D.path, D.p)
            } else {
                // Olhos grandes anime / chibi super brilhantes
                val lx = lookX * 0.015f * s
                D.oval(c, ex + lx, ey, 0.062f * s, 0.085f * s, C.ink)
                // Brilhos nos olhos
                D.circle(c, ex + lx - 0.02f * s, ey - 0.032f * s, 0.026f * s, C.white)
                D.circle(c, ex + lx + 0.022f * s, ey + 0.03f * s, 0.014f * s, C.white)
            }
        }

        // Narizinho rosa
        D.poly(c, C.pinkD, 0, 0f, -0.024f * s, -0.545f * s, 0.024f * s, -0.545f * s, 0f, -0.52f * s)

        // Boquinha de gatinha :3
        val my = -0.505f * s
        D.path.reset()
        D.path.moveTo(-0.065f * s, my - 0.01f * s)
        D.path.quadTo(-0.03f * s, my + 0.025f * s, 0f, my)
        D.path.quadTo(0.03f * s, my + 0.025f * s, 0.065f * s, my - 0.01f * s)
        D.s(ol, lw * 1.2f); c.drawPath(D.path, D.p)

        // Bigodes fofos
        for (sg in -1..1 step 2) {
            D.line(c, sg * 0.16f * s, -0.53f * s, sg * 0.36f * s, -0.56f * s, lw * 0.8f, D.alpha(ol, 190))
            D.line(c, sg * 0.16f * s, -0.50f * s, sg * 0.36f * s, -0.48f * s, lw * 0.8f, D.alpha(ol, 190))
        }

        c.restore()
    }
}
