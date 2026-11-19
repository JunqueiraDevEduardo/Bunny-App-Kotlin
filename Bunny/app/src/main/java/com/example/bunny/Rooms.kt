package com.example.bunny

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import kotlin.math.max

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
            "wall_inca" -> Color.rgb(228, 218, 202) // Pedra Inca de Cusco
            "wall_aguayo" -> C.cream // Tecido Aguayo peruano às riscas vibrantes
            else -> Color.rgb(255, 217, 228)
        }
        D.f(base); c.drawRect(r, D.p)

        when (id) {
            "wall_stripes" -> {
                val n = 10; val w = r.width() / n
                for (i in 0 until n step 2) {
                    D.f(C.pink); c.drawRect(r.left + i * w, r.top, r.left + (i + 1) * w, r.bottom, D.p)
                }
            }
            "wall_aguayo" -> {
                // Tecido Aguayo tradicional peruano: riscas coloridas andinas
                val cols = intArrayOf(C.peruRed, C.incaGold, C.turquoise, C.purpleAndes, C.peruRedD, C.mint)
                val stripeW = r.width() * 0.05f
                var x = r.left
                var idx = 0
                while (x < r.right) {
                    D.f(cols[idx % cols.size])
                    c.drawRect(x, r.top, minOf(r.right, x + stripeW), r.bottom, D.p)
                    x += stripeW * 1.5f
                    idx++
                }
            }
            "wall_inca" -> {
                // Parede de pedras talhadas incas com sol dourado Inti
                val rows = 6
                val rh = r.height() / rows
                for (j in 1 until rows) {
                    D.line(c, r.left, r.top + j * rh, r.right, r.top + j * rh, 3f, Color.rgb(180, 168, 150))
                    val cols = 5
                    for (i in 0 until cols) {
                        val off = if (j % 2 == 0) 0.5f else 0f
                        val x = r.left + (i + off) * (r.width() / cols)
                        D.line(c, x, r.top + j * rh, x, r.top + (j + 1) * rh, 3f, Color.rgb(180, 168, 150))
                    }
                }
                // Sol Inti dourado esculpido no centro
                val scx = r.centerX(); val scy = r.top + r.height() * 0.35f
                D.circle(c, scx, scy, r.width() * 0.08f, C.incaGold, Color.rgb(190, 140, 40), 2f)
                for (k in 0 until 8) {
                    val a = (k * 45) * Math.PI / 180.0
                    val rx1 = scx + (Math.cos(a) * r.width() * 0.09f).toFloat()
                    val ry1 = scy + (Math.sin(a) * r.width() * 0.09f).toFloat()
                    val rx2 = scx + (Math.cos(a) * r.width() * 0.13f).toFloat()
                    val ry2 = scy + (Math.sin(a) * r.width() * 0.13f).toFloat()
                    D.line(c, rx1, ry1, rx2, ry2, 3f, C.incaGold)
                }
            }
            "wall_stars" -> {
                val cols = 5
                val rows = max(2, (r.height() / (r.width() / cols)).toInt())
                for (i in 0 until cols) for (j in 0 until rows) {
                    val x = r.left + (i + 0.5f + if (j % 2 == 0) 0f else 0.5f) * r.width() / cols
                    val y = r.top + (j + 0.5f) * r.height() / rows
                    if (x < r.right) Icons.drawSpark(c, x, y, r.width() * 0.018f)
                }
            }
            "wall_pink", "wall_mint", "wall_sky", "wall_lav" -> {
                val dots = 6
                for (i in 0 until dots) for (j in 0 until dots) {
                    D.f(D.alpha(C.white, 90))
                    c.drawCircle(r.left + (i + 0.5f + if (j % 2 == 0) 0f else 0.5f) * r.width() / dots,
                        r.top + (j + 0.5f) * r.height() / dots, r.width() * 0.008f, D.p)
                }
            }
        }
    }

    fun drawFloor(c: Canvas, r: RectF, id: String) {
        when (id) {
            "floor_inca" -> {
                // Calçada inca de pedra com mosaico andino
                D.f(Color.rgb(205, 195, 180)); c.drawRect(r, D.p)
                val rows = 5
                val rh = r.height() / rows
                for (j in 0 until rows) {
                    D.line(c, r.left, r.top + j * rh, r.right, r.top + j * rh, 3f, Color.rgb(165, 155, 140))
                    val cols = 6
                    for (i in 0 until cols) {
                        val shift = (j * 0.35f) % 1f
                        val x = r.left + (i + shift) * (r.width() / cols)
                        D.line(c, x, r.top + j * rh, x, r.top + (j + 1) * rh, 3f, Color.rgb(165, 155, 140))
                    }
                }
            }
            "floor_peru" -> {
                // Soalho de madeira andina com friso artesanal
                D.f(Color.rgb(215, 175, 130)); c.drawRect(r, D.p)
                val rows = 5
                for (j in 1 until rows) D.line(c, r.left, r.top + j * r.height() / rows, r.right, r.top + j * r.height() / rows, 3f, Color.rgb(180, 140, 100))
                // Friso decorativo peruano no topo do chão
                val bw = r.width() * 0.04f
                for (k in 0 until (r.width() / bw).toInt() + 1) {
                    D.f(if (k % 2 == 0) C.peruRed else C.incaGold)
                    c.drawRect(r.left + k * bw, r.top, r.left + (k + 1) * bw, r.top + r.height() * 0.06f, D.p)
                }
            }
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
            "dec_machupicchu" -> {
                // Quadro ilustrado de Machu Picchu com sol dourado
                D.rrect(c, cx - u * 0.55f, cy - u * 0.45f, cx + u * 0.55f, cy + u * 0.45f, u * 0.06f, C.incaGold, C.brown, lw * 1.3f)
                D.rrect(c, cx - u * 0.48f, cy - u * 0.38f, cx + u * 0.48f, cy + u * 0.38f, u * 0.03f, C.sky)
                // Sol Inti dourado
                D.circle(c, cx + u * 0.28f, cy - u * 0.2f, u * 0.11f, C.incaGold)
                // Montanha Huayna Picchu verde icónica
                D.poly(c, C.greenD, 0, 0f, cx - u * 0.48f, cy + u * 0.38f, cx - u * 0.1f, cy - u * 0.28f, cx + u * 0.25f, cy + u * 0.38f)
                D.poly(c, C.green, 0, 0f, cx - u * 0.1f, cy + u * 0.38f, cx + u * 0.18f, cy - u * 0.15f, cx + u * 0.48f, cy + u * 0.38f)
                // Terrazas incas de pedra
                D.rrect(c, cx - u * 0.35f, cy + u * 0.18f, cx - u * 0.05f, cy + u * 0.28f, 0.02f * u, Color.rgb(210, 200, 185))
                D.rrect(c, cx + u * 0.05f, cy + u * 0.12f, cx + u * 0.38f, cy + u * 0.24f, 0.02f * u, Color.rgb(210, 200, 185))
            }
            "dec_tapete_peruano" -> {
                // Tapete andino com motivos geométricos e franjas
                D.oval(c, cx, cy, u * 1.1f, u * 0.32f, C.peruRed, ol, lw)
                D.oval(c, cx, cy, u * 0.85f, u * 0.24f, C.incaGold)
                D.oval(c, cx, cy, u * 0.6f, u * 0.16f, C.turquoise)
                D.oval(c, cx, cy, u * 0.35f, u * 0.09f, C.purpleAndes)
                // Franjas nas pontas
                for (i in -3..3) {
                    val fx = cx - u * 0.95f + (i + 3) * 0.03f * u
                    D.line(c, fx, cy - u * 0.08f, fx - u * 0.06f, cy - u * 0.08f, lw, C.incaGold)
                    val rx = cx + u * 0.95f - (i + 3) * 0.03f * u
                    D.line(c, rx, cy + u * 0.08f, rx + u * 0.06f, cy + u * 0.08f, lw, C.incaGold)
                }
            }
            "dec_lhama" -> {
                // Peluche / Estátua fofa de Lhama andina
                Icons.draw(c, Icons.LLAMA, cx, cy - u * 0.15f, u * 0.45f)
            }
            "dec_zampogna" -> {
                // Flauta de Pã tradicional peruana (tubos de bambu)
                D.rrect(c, cx - u * 0.35f, cy - u * 0.4f, cx + u * 0.35f, cy + u * 0.35f, 0.06f * u, D.alpha(C.white, 200), ol, lw)
                val tubes = 7
                for (i in 0 until tubes) {
                    val tx = cx - u * 0.26f + i * (u * 0.52f / (tubes - 1))
                    val th = u * (0.25f + i * 0.06f)
                    D.rrect(c, tx - u * 0.025f, cy + u * 0.28f - th, tx + u * 0.025f, cy + u * 0.28f, 0.015f * u, Color.rgb(225, 195, 145), C.brown, lw * 0.7f)
                }
                // Fita colorida que amarra a flauta
                D.line(c, cx - u * 0.28f, cy + u * 0.05f, cx + u * 0.28f, cy + u * 0.05f, lw * 2.5f, C.peruRed)
                D.line(c, cx - u * 0.28f, cy + u * 0.12f, cx + u * 0.28f, cy + u * 0.12f, lw * 2.5f, C.incaGold)
            }
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
            if (has("dec_machupicchu")) drawDecor(c, "dec_machupicchu", r.left + w * 0.8f, r.top + h * 0.2f, w * 0.32f)
            if (has("dec_tapete_peruano")) drawDecor(c, "dec_tapete_peruano", r.left + w * 0.5f, fy + h * 0.27f, w * 0.42f)
            if (has("dec_lhama")) drawDecor(c, "dec_lhama", r.left + w * 0.16f, fy + h * 0.32f, w * 0.26f)
            if (has("dec_zampogna")) drawDecor(c, "dec_zampogna", r.left + w * 0.16f, r.top + h * 0.24f, w * 0.24f)

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
