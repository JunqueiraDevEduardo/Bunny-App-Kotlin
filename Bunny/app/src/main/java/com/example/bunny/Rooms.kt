package com.example.bunny

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.sin

object Rooms {
    val names = listOf("Sala", "Quarto", "Cozinha")
    private var nightNow = 0f
    private val rf = RectF()

    fun floorY(r: RectF) = r.top + r.height() * 0.60f

    fun drawWall(c: Canvas, r: RectF, id: String) {
        val base = when (id) {
            "wall_mint" -> Color.rgb(212, 240, 226)
            "wall_sky" -> Color.rgb(211, 233, 250)
            "wall_lav" -> Color.rgb(227, 217, 246)
            "wall_stripes" -> C.cream
            "wall_stars" -> Color.rgb(214, 200, 240)
            else -> Color.rgb(255, 217, 228)
        }
        D.f(base); c.drawRect(r, D.p)
        if (id == "wall_stripes") {
            val n = 10
            val w = r.width() / n
            for (i in 0 until n step 2) {
                D.f(C.pink); c.drawRect(r.left + i * w, r.top, r.left + (i + 1) * w, r.bottom, D.p)
            }
        }
        if (id == "wall_stars") {
            val cols = 5
            val rows = max(2, (r.height() / (r.width() / cols)).toInt())
            for (i in 0 until cols) for (j in 0 until rows) {
                val x = r.left + (i + 0.5f + if (j % 2 == 0) 0f else 0.5f) * r.width() / cols
                val y = r.top + (j + 0.5f) * r.height() / rows
                if (x < r.right) Icons.drawSpark(c, x, y, r.width() * 0.018f)
            }
        }
        if (id == "wall_pink" || id == "wall_mint" || id == "wall_sky" || id == "wall_lav") {
            // pontinhos suaves
            val dots = 6
            for (i in 0 until dots) for (j in 0 until dots) {
                D.f(D.alpha(C.white, 90))
                c.drawCircle(r.left + (i + 0.5f + if (j % 2 == 0) 0f else 0.5f) * r.width() / dots,
                    r.top + (j + 0.5f) * r.height() / dots, r.width() * 0.008f, D.p)
            }
        }
    }

    fun drawFloor(c: Canvas, r: RectF, id: String) {
        when (id) {
            "floor_check" -> {
                val n = 8
                val w = r.width() / n
                val rows = max(1, (r.height() / w).toInt() + 1)
                for (i in 0 until n) for (j in 0 until rows) {
                    D.f(if ((i + j) % 2 == 0) C.pink else C.cream)
                    c.drawRect(r.left + i * w, r.top + j * w, r.left + (i + 1) * w, minOf(r.bottom, r.top + (j + 1) * w), D.p)
                }
            }
            "floor_grass" -> {
                D.f(Color.rgb(191, 229, 176)); c.drawRect(r, D.p)
                val n = 9
                for (i in 0 until n) for (j in 0 until 4) {
                    val x = r.left + (i + 0.5f + if (j % 2 == 0) 0f else 0.5f) * r.width() / n
                    val y = r.top + (j + 0.6f) * r.height() / 4f
                    D.line(c, x, y, x - r.width() * 0.01f, y - r.width() * 0.025f, 3f, C.greenD)
                    D.line(c, x, y, x + r.width() * 0.01f, y - r.width() * 0.03f, 3f, C.greenD)
                }
            }
            "floor_cloud" -> {
                D.f(Color.rgb(228, 240, 255)); c.drawRect(r, D.p)
                for (i in 0 until 4) for (j in 0 until 3) {
                    val x = r.left + (i + 0.3f + if (j % 2 == 0) 0f else 0.5f) * r.width() / 4f
                    val y = r.top + (j + 0.5f) * r.height() / 3f
                    Icons.draw(c, Icons.CLOUD, x, y, r.width() * 0.06f)
                }
            }
            else -> {
                D.f(Color.rgb(235, 203, 159)); c.drawRect(r, D.p)
                val rows = 5
                for (j in 1 until rows) D.line(c, r.left, r.top + j * r.height() / rows, r.right, r.top + j * r.height() / rows, 3f, Color.rgb(214, 176, 130))
                for (j in 0 until rows) {
                    val off = if (j % 2 == 0) 0.25f else 0.7f
                    for (k in 0 until 3) {
                        val x = r.left + (off + k * 0.33f) % 1f * r.width()
                        D.line(c, x, r.top + j * r.height() / rows, x, r.top + (j + 1) * r.height() / rows, 3f, Color.rgb(214, 176, 130))
                    }
                }
            }
        }
    }

    fun drawDecor(c: Canvas, id: String, cx: Float, cy: Float, u: Float) {
        val ol = C.outline
        val lw = max(2f, u * 0.03f)
        when (id) {
            "dec_window" -> {
                D.rrect(c, cx - u * 0.55f, cy - u * 0.6f, cx + u * 0.55f, cy + u * 0.6f, u * 0.08f, C.white, ol, lw)
                D.rrect(c, cx - u * 0.47f, cy - u * 0.52f, cx + u * 0.47f, cy + u * 0.52f, u * 0.05f, D.mix(C.sky, Color.rgb(30, 35, 80), nightNow))
                if (nightNow > 0.4f) {
                    Icons.draw(c, Icons.MOON, cx + u * 0.15f, cy - u * 0.2f, u * 0.2f)
                    Icons.drawSpark(c, cx - u * 0.25f, cy - u * 0.3f, u * 0.07f)
                    Icons.drawSpark(c, cx - u * 0.1f, cy + u * 0.1f, u * 0.05f)
                } else {
                    D.circle(c, cx + u * 0.18f, cy - u * 0.2f, u * 0.13f, C.butter)
                    Icons.draw(c, Icons.CLOUD, cx - u * 0.15f, cy + u * 0.1f, u * 0.17f)
                }
                D.line(c, cx, cy - u * 0.52f, cx, cy + u * 0.52f, lw * 1.5f, C.white)
                D.line(c, cx - u * 0.47f, cy, cx + u * 0.47f, cy, lw * 1.5f, C.white)
                D.poly(c, C.pinkD, ol, lw, cx - u * 0.6f, cy - u * 0.65f, cx - u * 0.25f, cy - u * 0.65f, cx - u * 0.6f, cy + u * 0.4f)
                D.poly(c, C.pinkD, ol, lw, cx + u * 0.6f, cy - u * 0.65f, cx + u * 0.25f, cy - u * 0.65f, cx + u * 0.6f, cy + u * 0.4f)
            }
            "dec_frame" -> {
                D.rrect(c, cx - u * 0.4f, cy - u * 0.45f, cx + u * 0.4f, cy + u * 0.45f, u * 0.06f, C.gold, C.brown, lw)
                D.rrect(c, cx - u * 0.32f, cy - u * 0.37f, cx + u * 0.32f, cy + u * 0.37f, u * 0.03f, C.cream)
                Icons.draw(c, Icons.CARROT, cx, cy, u * 0.22f)
            }
            "dec_shelf" -> {
                D.rrect(c, cx - u * 0.5f, cy - u * 0.9f, cx + u * 0.5f, cy + u * 0.9f, u * 0.05f, C.brown, ol, lw)
                for (i in 0 until 3) {
                    val y = cy - u * 0.9f + (i + 1) * u * 0.6f
                    D.line(c, cx - u * 0.5f, y, cx + u * 0.5f, y, lw * 2f, Color.rgb(160, 120, 96))
                    val cols = intArrayOf(C.pink, C.mint, C.butter, C.lav, C.sky)
                    for (k in 0 until 4) D.rrect(c, cx - u * 0.42f + k * u * 0.2f, y - u * 0.4f + (k % 2) * u * 0.05f, cx - u * 0.26f + k * u * 0.2f, y - u * 0.02f, u * 0.02f, cols[(i + k) % 5], ol, lw * 0.6f)
                }
            }
            "dec_plant" -> {
                D.oval(c, cx - u * 0.18f, cy - u * 0.5f, u * 0.14f, u * 0.3f, C.green, ol, lw)
                D.oval(c, cx + u * 0.18f, cy - u * 0.45f, u * 0.14f, u * 0.28f, C.greenD, ol, lw)
                D.oval(c, cx, cy - u * 0.62f, u * 0.14f, u * 0.34f, C.green, ol, lw)
                D.poly(c, C.peach, ol, lw, cx - u * 0.3f, cy - u * 0.2f, cx + u * 0.3f, cy - u * 0.2f, cx + u * 0.22f, cy + u * 0.35f, cx - u * 0.22f, cy + u * 0.35f)
                D.circle(c, cx, cy + u * 0.07f, u * 0.05f, C.pinkD)
            }
            "dec_lamp" -> {
                if (nightNow > 0.3f) D.circle(c, cx, cy - u * 0.5f, u * 0.7f, D.alpha(C.butter, (nightNow * 110).toInt()))
                D.poly(c, C.butter, ol, lw, cx - u * 0.2f, cy - u * 0.95f, cx + u * 0.2f, cy - u * 0.95f, cx + u * 0.38f, cy - u * 0.5f, cx - u * 0.38f, cy - u * 0.5f)
                D.line(c, cx, cy - u * 0.5f, cx, cy + u * 0.3f, lw * 2.5f, C.brown)
                D.oval(c, cx, cy + u * 0.33f, u * 0.25f, u * 0.07f, C.brown, ol, lw)
            }
            "dec_rug" -> {
                D.oval(c, cx, cy, u * 1.0f, u * 0.2f, C.pink, ol, lw)
                D.oval(c, cx, cy, u * 0.72f, u * 0.14f, C.cream)
                D.oval(c, cx, cy, u * 0.42f, u * 0.08f, C.lav)
            }
            "dec_plush" -> {
                D.oval(c, cx, cy - u * 0.2f, u * 0.28f, u * 0.25f, C.butter, ol, lw)
                D.circle(c, cx, cy - u * 0.5f, u * 0.19f, C.butter, ol, lw)
                D.poly(c, C.orange, ol, lw, cx - u * 0.05f, cy - u * 0.5f, cx + u * 0.05f, cy - u * 0.5f, cx, cy - u * 0.43f)
                D.circle(c, cx - u * 0.08f, cy - u * 0.55f, u * 0.025f, C.ink)
                D.circle(c, cx + u * 0.08f, cy - u * 0.55f, u * 0.025f, C.ink)
            }
        }
    }

    /** Desenha a divisão completa: parede, chão, rodapé, objetos e escurecimento da noite. */
    fun draw(c: Canvas, r: RectF, room: Int, night: Float, t: Float, decor: Boolean = true) {
        nightNow = night
        val fy = floorY(r)
        rf.set(r.left, r.top, r.right, fy)
        drawWall(c, rf, Save.wall[room])
        rf.set(r.left, fy, r.right, r.bottom)
        drawFloor(c, rf, Save.floor[room])
        D.line(c, r.left, fy, r.right, fy, r.height() * 0.012f, D.alpha(C.white, 230))
        if (decor) {
            val w = r.width(); val h = r.height()
            fun has(id: String) = Save.decor.contains("$id@$room")
            if (has("dec_window")) drawDecor(c, "dec_window", r.left + w * 0.2f, r.top + h * 0.22f, w * 0.26f)
            if (has("dec_frame")) drawDecor(c, "dec_frame", r.left + w * 0.8f, r.top + h * 0.2f, w * 0.2f)
            if (has("dec_shelf")) drawDecor(c, "dec_shelf", r.left + w * 0.84f, fy - w * 0.15f, w * 0.2f)
            if (has("dec_rug")) drawDecor(c, "dec_rug", r.left + w * 0.5f, fy + h * 0.27f, w * 0.34f)
            if (has("dec_plant")) drawDecor(c, "dec_plant", r.left + w * 0.1f, fy + h * 0.12f, w * 0.2f)
            if (has("dec_lamp")) drawDecor(c, "dec_lamp", r.left + w * 0.9f, fy + h * 0.13f, w * 0.2f)
            if (has("dec_plush")) drawDecor(c, "dec_plush", r.left + w * 0.22f, fy + h * 0.34f, w * 0.22f)
        }
        if (night > 0f) {
            D.f(Color.argb((night * 105).toInt(), 28, 24, 78))
            c.drawRect(r, D.p)
        }
    }
}
