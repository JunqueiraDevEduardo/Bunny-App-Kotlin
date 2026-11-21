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
        val baseRaw = when (id) {
            "wall_mint" -> Color.rgb(212, 240, 226)
            "wall_sky" -> if (Theme.isBunny) Theme.bNavyLight else Color.rgb(211, 233, 250)
            "wall_lav", "wall_purple" -> if (Theme.isBunny) Theme.bPurpleDark else Color.rgb(227, 217, 246)
            "wall_navy" -> Theme.bNavyDark
            "wall_stripes" -> if (Theme.isBunny) Theme.bNavyDark else C.cream
            "wall_stars" -> if (Theme.isBunny) Theme.bNavyDark else Color.rgb(214, 200, 240)
            "wall_inca" -> if (Theme.isBunny) Color.rgb(32, 42, 75) else Color.rgb(228, 218, 202)
            "wall_aguayo" -> if (Theme.isBunny) Theme.bNavyDark else C.cream
            "wall_azulejos_pt" -> Color.rgb(238, 244, 255)
            "cat_wall_azulejos_rosa" -> Color.rgb(255, 242, 248)
            "wall_marmore_wc_azul" -> Color.rgb(225, 238, 252)
            "cat_wall_marmore_wc_rosa" -> Color.rgb(255, 230, 242)
            "cat_wall_azulejo_wc_branco" -> Color.rgb(248, 250, 255)
            "wall_granito_porto" -> Color.rgb(180, 185, 195)
            "wall_madeira_alentejo" -> Color.rgb(145, 105, 75)
            "wall_pedra_machupicchu" -> Color.rgb(175, 170, 160)
            "cat_wall_pink" -> Color.rgb(255, 222, 235)
            "cat_wall_cloud" -> Color.rgb(255, 230, 245)
            "cat_wall_hearts" -> Color.rgb(255, 218, 232)
            "cat_wall_stars" -> Color.rgb(255, 225, 240)
            "cat_wall_stripes" -> Color.rgb(255, 228, 238)
            "cat_wall_renda_sintra" -> Color.rgb(255, 235, 245)
            "cat_wall_nuvens_douradas" -> Color.rgb(255, 225, 240)
            else -> if (Theme.isBunny) Theme.bNavyDark else Color.rgb(255, 217, 228)
        }
        val base = Theme.sanitize(baseRaw)
        D.f(base); c.drawRect(r, D.p)

        when (id) {
            "wall_stripes", "cat_wall_stripes" -> {
                val n = 10; val w = r.width() / n
                val stripeCol = if (Theme.isBunny) Theme.bWine else Color.rgb(255, 185, 212)
                for (i in 0 until n step 2) {
                    D.f(stripeCol); c.drawRect(r.left + i * w, r.top, r.left + (i + 1) * w, r.bottom, D.p)
                }
            }
            "cat_wall_hearts" -> {
                // Padrão delicado de corações rosa e brancos
                val cols = 6; val rows = 5
                val cw = r.width() / cols; val rh = r.height() / rows
                for (i in 0 until cols) for (j in 0 until rows) {
                    val hx = r.left + (i + 0.5f + if (j % 2 == 0) 0f else 0.5f) * cw
                    val hy = r.top + (j + 0.5f) * rh
                    if (hx < r.right) {
                        D.heart(c, hx, hy, cw * 0.22f, if ((i + j) % 2 == 0) C.pinkD else C.white)
                    }
                }
            }
            "cat_wall_cloud" -> {
                // Nuvens fofas brancas no fundo rosa suave
                val cols = 4; val rows = 3
                val cw = r.width() / cols; val rh = r.height() / rows
                for (i in 0 until cols) for (j in 0 until rows) {
                    val cx = r.left + (i + 0.5f + if (j % 2 == 0) 0f else 0.5f) * cw
                    val cy = r.top + (j + 0.5f) * rh
                    if (cx < r.right) Icons.draw(c, Icons.CLOUD, cx, cy, cw * 0.35f)
                }
            }
            "wall_aguayo" -> {
                val cols = if (Theme.isBunny) {
                    intArrayOf(Theme.bWine, Theme.bBlueRoyal, Theme.bPurple, Theme.bBlueSky, Theme.bWineLight)
                } else {
                    intArrayOf(C.pinkD, Theme.cPeach, C.gold, C.pink, Color.rgb(255, 140, 180))
                }
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
                val rows = 6
                val rh = r.height() / rows
                val lineCol = if (Theme.isBunny) Theme.bPurple else Color.rgb(180, 168, 150)
                for (j in 1 until rows) {
                    D.line(c, r.left, r.top + j * rh, r.right, r.top + j * rh, 3f, lineCol)
                    val cols = 5
                    for (i in 0 until cols) {
                        val off = if (j % 2 == 0) 0.5f else 0f
                        val x = r.left + (i + off) * (r.width() / cols)
                        D.line(c, x, r.top + j * rh, x, r.top + (j + 1) * rh, 3f, lineCol)
                    }
                }
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
            "wall_azulejos_pt", "cat_wall_azulejos_rosa" -> {
                // Azulejos Portugueses Tradicionais de Lisboa
                val tileCols = 6
                val tw = r.width() / tileCols
                val tileRows = (r.height() / tw).toInt().coerceAtLeast(2)
                val lineCol = if (id == "wall_azulejos_pt") Color.rgb(38, 92, 175) else Color.rgb(235, 115, 165)
                val accCol = if (id == "wall_azulejos_pt") Color.rgb(26, 52, 115) else Color.rgb(255, 185, 215)
                for (j in 0 until tileRows) {
                    for (i in 0 until tileCols) {
                        val tx = r.left + i * tw; val ty = r.top + j * tw
                        val tr = RectF(tx + 2f, ty + 2f, tx + tw - 2f, ty + tw - 2f)
                        D.rrect(c, tr, 4f, Color.WHITE, lineCol, 1.5f)
                        // Motivo floral de azulejo no centro
                        val tcx = tr.centerX(); val tcy = tr.centerY()
                        D.circle(c, tcx, tcy, tw * 0.16f, accCol)
                        D.circle(c, tcx, tcy, tw * 0.08f, lineCol)
                        for (k in 0..3) {
                            val a = (k * 90) * Math.PI / 180.0
                            val px = tcx + (Math.cos(a) * tw * 0.26f).toFloat()
                            val py = tcy + (Math.sin(a) * tw * 0.26f).toFloat()
                            D.circle(c, px, py, tw * 0.05f, lineCol)
                        }
                    }
                }
            }
            "wall_marmore_wc_azul", "cat_wall_marmore_wc_rosa", "cat_wall_azulejo_wc_branco" -> {
                // Azulejos vidrados de Casa de Banho (Subway tiles)
                val subH = r.height() * 0.10f
                val subW = subH * 2.0f
                val groutCol = Color.rgb(215, 222, 235)
                val rows = (r.height() / subH).toInt() + 1
                for (j in 0 until rows) {
                    val y = r.top + j * subH
                    D.line(c, r.left, y, r.right, y, 2.5f, groutCol)
                    val off = if (j % 2 == 0) 0f else subW * 0.5f
                    var x = r.left - subW + off
                    while (x <= r.right + subW) {
                        D.line(c, x, y, x, y + subH, 2.5f, groutCol)
                        x += subW
                    }
                }
            }
            "wall_stars", "cat_wall_stars" -> {
                val cols = 5
                val rows = max(2, (r.height() / (r.width() / cols)).toInt())
                for (i in 0 until cols) for (j in 0 until rows) {
                    val x = r.left + (i + 0.5f + if (j % 2 == 0) 0f else 0.5f) * r.width() / cols
                    val y = r.top + (j + 0.5f) * r.height() / rows
                    if (x < r.right) Icons.drawSpark(c, x, y, r.width() * 0.018f)
                }
            }
            "wall_pink", "cat_wall_pink", "wall_mint", "wall_sky", "wall_lav" -> {
                val dots = 6
                val dotCol = if (Theme.isBunny) D.alpha(Theme.bBlueIce, 90) else D.alpha(C.white, 120)
                for (i in 0 until dots) for (j in 0 until dots) {
                    D.f(dotCol)
                    c.drawCircle(r.left + (i + 0.5f + if (j % 2 == 0) 0f else 0.5f) * r.width() / dots,
                        r.top + (j + 0.5f) * r.height() / dots, r.width() * 0.008f, D.p)
                }
            }
        }
    }

    fun drawFloor(c: Canvas, r: RectF, id: String) {
        when (id) {
            "floor_inca" -> {
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
                D.f(Color.rgb(215, 175, 130)); c.drawRect(r, D.p)
                val rows = 5
                for (j in 1 until rows) D.line(c, r.left, r.top + j * r.height() / rows, r.right, r.top + j * r.height() / rows, 3f, Color.rgb(180, 140, 100))
                val bw = r.width() * 0.04f
                for (k in 0 until (r.width() / bw).toInt() + 1) {
                    D.f(if (k % 2 == 0) C.peruRed else C.incaGold)
                    c.drawRect(r.left + k * bw, r.top, r.left + (k + 1) * bw, r.top + r.height() * 0.06f, D.p)
                }
            }
            "floor_check", "cat_floor_check" -> {
                val n = 8
                val w = r.width() / n
                val rows = max(1, (r.height() / w).toInt() + 1)
                for (i in 0 until n) for (j in 0 until rows) {
                    D.f(if ((i + j) % 2 == 0) Color.rgb(255, 205, 225) else C.white)
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
            "floor_cloud", "cat_floor_cloud" -> {
                D.f(Color.rgb(255, 240, 248)); c.drawRect(r, D.p)
                for (i in 0 until 4) for (j in 0 until 3) {
                    val x = r.left + (i + 0.3f + if (j % 2 == 0) 0f else 0.5f) * r.width() / 4f
                    val y = r.top + (j + 0.5f) * r.height() / 3f
                    Icons.draw(c, Icons.CLOUD, x, y, r.width() * 0.06f)
                }
            }
            "floor_calcada_portuguesa", "cat_floor_calcada_rosa" -> {
                // Calçada Portuguesa em Mosaico
                val darkStone = if (id == "floor_calcada_portuguesa") Color.rgb(45, 45, 52) else Color.rgb(235, 115, 160)
                val lightStone = Color.rgb(245, 245, 248)
                D.f(lightStone); c.drawRect(r, D.p)
                val cols = 12
                val cw = r.width() / cols
                val rows = (r.height() / (cw * 0.7f)).toInt() + 1
                for (j in 0 until rows) {
                    val y = r.top + j * (cw * 0.7f)
                    for (i in 0 until cols) {
                        val isWave = (i + (j * 2)) % 4 < 2
                        if (isWave) {
                            val stoneR = RectF(r.left + i * cw + 1.5f, y + 1.5f, r.left + (i + 1) * cw - 1.5f, y + cw * 0.7f - 1.5f)
                            D.rrect(c, stoneR, 3f, darkStone)
                        }
                    }
                }
            }
            "floor_marmore_wc_azul", "cat_floor_marmore_wc_rosa" -> {
                // Piso de Mármore Polido para Casa de Banho
                val marbleBase = if (id == "floor_marmore_wc_azul") Color.rgb(240, 245, 255) else Color.rgb(255, 244, 250)
                val veinCol = if (id == "floor_marmore_wc_azul") Color.rgb(205, 220, 245) else Color.rgb(250, 215, 235)
                D.f(marbleBase); c.drawRect(r, D.p)
                val size = r.width() * 0.18f
                val cols = (r.width() / size).toInt() + 1
                val rows = (r.height() / size).toInt() + 1
                for (i in 0 until cols) for (j in 0 until rows) {
                    val mr = RectF(r.left + i * size + 2f, r.top + j * size + 2f, r.left + (i + 1) * size - 2f, r.top + (j + 1) * size - 2f)
                    D.rrect(c, mr, 4f, marbleBase, veinCol, 1.8f)
                    D.line(c, mr.left + size * 0.2f, mr.top + size * 0.15f, mr.right - size * 0.15f, mr.bottom - size * 0.2f, 1.2f, veinCol)
                }
            }
            "cat_floor_pink" -> {
                D.f(Color.rgb(255, 218, 230)); c.drawRect(r, D.p)
                val rows = 5
                for (j in 1 until rows) D.line(c, r.left, r.top + j * r.height() / rows, r.right, r.top + j * r.height() / rows, 3f, Color.rgb(240, 185, 205))
            }
            "cat_floor_white" -> {
                D.f(Color.rgb(252, 250, 252)); c.drawRect(r, D.p)
                val rows = 5
                for (j in 1 until rows) D.line(c, r.left, r.top + j * r.height() / rows, r.right, r.top + j * r.height() / rows, 3f, Color.rgb(230, 220, 230))
            }
            else -> {
                val woodBase = if (Theme.isBunny) Color.rgb(90, 65, 55) else Color.rgb(235, 203, 159)
                val woodLine = if (Theme.isBunny) Color.rgb(65, 45, 38) else Color.rgb(214, 176, 130)
                D.f(woodBase); c.drawRect(r, D.p)
                val rows = 5
                for (j in 1 until rows) D.line(c, r.left, r.top + j * r.height() / rows, r.right, r.top + j * r.height() / rows, 3f, woodLine)
                for (j in 0 until rows) {
                    val off = if (j % 2 == 0) 0.25f else 0.7f
                    for (k in 0 until 3) {
                        val x = r.left + (off + k * 0.33f) % 1f * r.width()
                        D.line(c, x, r.top + j * r.height() / rows, x, r.top + (j + 1) * r.height() / rows, 3f, woodLine)
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
                D.rrect(c, cx - u * 0.55f, cy - u * 0.45f, cx + u * 0.55f, cy + u * 0.45f, u * 0.06f, C.incaGold, C.brown, lw * 1.3f)
                D.rrect(c, cx - u * 0.48f, cy - u * 0.38f, cx + u * 0.48f, cy + u * 0.38f, u * 0.03f, C.sky)
                D.circle(c, cx + u * 0.28f, cy - u * 0.2f, u * 0.11f, C.incaGold)
                D.poly(c, C.greenD, 0, 0f, cx - u * 0.48f, cy + u * 0.38f, cx - u * 0.1f, cy - u * 0.28f, cx + u * 0.25f, cy + u * 0.38f)
                D.poly(c, C.green, 0, 0f, cx - u * 0.1f, cy + u * 0.38f, cx + u * 0.18f, cy - u * 0.15f, cx + u * 0.48f, cy + u * 0.38f)
                D.rrect(c, cx - u * 0.35f, cy + u * 0.18f, cx - u * 0.05f, cy + u * 0.28f, 0.02f * u, Color.rgb(210, 200, 185))
                D.rrect(c, cx + u * 0.05f, cy + u * 0.12f, cx + u * 0.38f, cy + u * 0.24f, 0.02f * u, Color.rgb(210, 200, 185))
            }
            "dec_tapete_peruano" -> {
                D.oval(c, cx, cy, u * 1.1f, u * 0.32f, C.peruRed, ol, lw)
                D.oval(c, cx, cy, u * 0.85f, u * 0.24f, C.incaGold)
                D.oval(c, cx, cy, u * 0.6f, u * 0.16f, C.turquoise)
                D.oval(c, cx, cy, u * 0.35f, u * 0.09f, C.purpleAndes)
                for (i in -3..3) {
                    val fx = cx - u * 0.95f + (i + 3) * 0.03f * u
                    D.line(c, fx, cy - u * 0.08f, fx - u * 0.06f, cy - u * 0.08f, lw, C.incaGold)
                    val rx = cx + u * 0.95f - (i + 3) * 0.03f * u
                    D.line(c, rx, cy + u * 0.08f, rx + u * 0.06f, cy + u * 0.08f, lw, C.incaGold)
                }
            }
            "dec_lhama" -> {
                Icons.draw(c, Icons.LLAMA, cx, cy - u * 0.15f, u * 0.45f)
            }
            "dec_zampogna" -> {
                D.rrect(c, cx - u * 0.35f, cy - u * 0.4f, cx + u * 0.35f, cy + u * 0.35f, 0.06f * u, D.alpha(C.white, 200), ol, lw)
                val tubes = 7
                for (i in 0 until tubes) {
                    val tx = cx - u * 0.26f + i * (u * 0.52f / (tubes - 1))
                    val th = u * (0.25f + i * 0.06f)
                    D.rrect(c, tx - u * 0.025f, cy + u * 0.28f - th, tx + u * 0.025f, cy + u * 0.28f, 0.015f * u, Color.rgb(225, 195, 145), C.brown, lw * 0.7f)
                }
                D.line(c, cx - u * 0.28f, cy + u * 0.05f, cx + u * 0.28f, cy + u * 0.05f, lw * 2.5f, C.peruRed)
                D.line(c, cx - u * 0.28f, cy + u * 0.12f, cx + u * 0.28f, cy + u * 0.12f, lw * 2.5f, C.incaGold)
            }
            "cat_dec_plush" -> {
                // Peluche Ursinho Rosa com lacinho branco
                val pCol = Color.rgb(255, 185, 215)
                D.oval(c, cx, cy - u * 0.2f, u * 0.28f, u * 0.25f, pCol, ol, lw)
                D.circle(c, cx, cy - u * 0.5f, u * 0.19f, pCol, ol, lw)
                // Orelhas de urso
                D.circle(c, cx - u * 0.15f, cy - u * 0.65f, u * 0.07f, pCol, ol, lw)
                D.circle(c, cx + u * 0.15f, cy - u * 0.65f, u * 0.07f, pCol, ol, lw)
                D.circle(c, cx - u * 0.15f, cy - u * 0.65f, u * 0.035f, C.white)
                D.circle(c, cx + u * 0.15f, cy - u * 0.65f, u * 0.035f, C.white)
                // Focinho branco e olhos
                D.oval(c, cx, cy - u * 0.46f, u * 0.08f, u * 0.06f, C.white)
                D.circle(c, cx, cy - u * 0.48f, u * 0.025f, C.pinkD)
                D.circle(c, cx - u * 0.07f, cy - u * 0.53f, u * 0.022f, C.ink)
                D.circle(c, cx + u * 0.07f, cy - u * 0.53f, u * 0.022f, C.ink)
                // Lacinho branco
                D.circle(c, cx, cy - u * 0.33f, u * 0.03f, C.white, ol, lw * 0.7f)
            }
            "cat_dec_rug" -> {
                // Tapete fofo em forma de coração rosa
                D.heart(c, cx, cy, u * 0.55f, Color.rgb(255, 175, 210), ol, lw)
                D.heart(c, cx, cy - u * 0.03f, u * 0.42f, Color.rgb(255, 220, 235))
                D.heart(c, cx, cy - u * 0.05f, u * 0.26f, C.white)
            }
            "cat_dec_lamp" -> {
                // Candeeiro nuvem fofa com luz quente
                if (nightNow > 0.3f) D.circle(c, cx, cy - u * 0.5f, u * 0.7f, D.alpha(Color.rgb(255, 235, 210), (nightNow * 120).toInt()))
                Icons.draw(c, Icons.CLOUD, cx, cy - u * 0.55f, u * 0.35f)
                D.line(c, cx, cy - u * 0.35f, cx, cy + u * 0.3f, lw * 2.5f, Color.rgb(255, 175, 205))
                D.oval(c, cx, cy + u * 0.33f, u * 0.25f, u * 0.07f, Color.rgb(255, 175, 205), ol, lw)
            }
            "cat_dec_mirror" -> {
                // Espelho princesa oval com moldura de pérolas
                D.oval(c, cx, cy - u * 0.4f, u * 0.32f, u * 0.45f, Color.rgb(255, 205, 225), ol, lw * 1.3f)
                D.oval(c, cx, cy - u * 0.4f, u * 0.26f, u * 0.39f, Color.rgb(240, 245, 255), ol, lw * 0.8f)
                // Reflexo de luz
                D.line(c, cx - u * 0.12f, cy - u * 0.6f, cx + u * 0.05f, cy - u * 0.3f, lw * 1.8f, D.alpha(C.white, 200))
                // Laço rosa no topo do espelho
                D.oval(c, cx - u * 0.06f, cy - u * 0.82f, u * 0.06f, u * 0.035f, C.pinkD, ol, lw * 0.7f)
                D.oval(c, cx + u * 0.06f, cy - u * 0.82f, u * 0.06f, u * 0.035f, C.pinkD, ol, lw * 0.7f)
                D.circle(c, cx, cy - u * 0.82f, u * 0.025f, C.white)
            }
            // ========================================== CASA DE BANHO (WC)
            "dec_banheira", "cat_dec_banheira_rosa" -> {
                // Banheira de porcelana com pés dourados, espuma e patinho de borracha
                val isPink = id == "cat_dec_banheira_rosa"
                val tubBase = if (isPink) Color.rgb(255, 215, 235) else Color.WHITE
                val rimCol = if (isPink) Color.rgb(255, 175, 205) else Color.rgb(225, 235, 250)
                // Pés dourados clássicos
                D.circle(c, cx - u * 0.38f, cy + u * 0.22f, u * 0.06f, C.gold, ol, lw)
                D.circle(c, cx + u * 0.38f, cy + u * 0.22f, u * 0.06f, C.gold, ol, lw)
                // Corpo da banheira
                D.oval(c, cx, cy + u * 0.05f, u * 0.52f, u * 0.25f, tubBase, ol, lw * 1.3f)
                D.oval(c, cx, cy - u * 0.05f, u * 0.48f, u * 0.16f, rimCol, ol, lw * 0.8f)
                // Água com espuma
                D.oval(c, cx, cy - u * 0.04f, u * 0.44f, u * 0.13f, if (isPink) Color.rgb(255, 235, 245) else Color.rgb(195, 225, 255))
                for (b in 0..4) {
                    val bx = cx - u * 0.28f + b * u * 0.14f
                    val by = cy - u * 0.05f + (b % 2) * u * 0.02f
                    D.circle(c, bx, by, u * 0.035f, Color.WHITE)
                }
                // Torneira dourada curva
                D.line(c, cx - u * 0.42f, cy - u * 0.08f, cx - u * 0.42f, cy - u * 0.22f, lw * 2f, C.gold)
                D.line(c, cx - u * 0.42f, cy - u * 0.22f, cx - u * 0.34f, cy - u * 0.20f, lw * 2f, C.gold)
                // Patinho de borracha amarelo
                D.oval(c, cx + u * 0.15f, cy - u * 0.06f, u * 0.06f, u * 0.045f, C.gold, ol, lw * 0.6f)
                D.circle(c, cx + u * 0.18f, cy - u * 0.09f, u * 0.035f, C.gold, ol, lw * 0.6f)
                D.poly(c, Color.rgb(255, 120, 30), 0, 0f, cx + u * 0.21f, cy - u * 0.09f, cx + u * 0.25f, cy - u * 0.08f, cx + u * 0.21f, cy - u * 0.07f)
            }
            "dec_sanita" -> {
                // Sanita moderna de porcelana
                D.rrect(c, cx - u * 0.18f, cy - u * 0.45f, cx + u * 0.18f, cy - u * 0.05f, u * 0.04f, Color.WHITE, ol, lw)
                D.circle(c, cx, cy - u * 0.28f, u * 0.04f, Color.rgb(215, 225, 235), ol, lw * 0.6f) // Botão autoclismo
                D.oval(c, cx, cy + u * 0.08f, u * 0.24f, u * 0.18f, Color.WHITE, ol, lw)
                D.oval(c, cx, cy + u * 0.06f, u * 0.19f, u * 0.14f, Color.rgb(235, 242, 252), ol, lw * 0.7f)
                D.rrect(c, cx - u * 0.12f, cy + u * 0.18f, cx + u * 0.12f, cy + u * 0.35f, u * 0.02f, Color.WHITE, ol, lw * 0.8f)
            }
            "dec_lavatorio", "cat_dec_lavatorio_rosa" -> {
                // Móvel de lavatório com torneira e toalha
                val isP = id == "cat_dec_lavatorio_rosa"
                val cabCol = if (isP) Color.rgb(255, 225, 240) else Color.rgb(235, 240, 250)
                D.rrect(c, cx - u * 0.30f, cy - u * 0.05f, cx + u * 0.30f, cy + u * 0.32f, u * 0.04f, cabCol, ol, lw)
                D.oval(c, cx, cy - u * 0.08f, u * 0.28f, u * 0.12f, Color.WHITE, ol, lw)
                D.oval(c, cx, cy - u * 0.08f, u * 0.22f, u * 0.08f, Color.rgb(205, 230, 255), ol, lw * 0.7f)
                D.line(c, cx, cy - u * 0.14f, cx, cy - u * 0.28f, lw * 2.2f, C.gold)
                D.line(c, cx, cy - u * 0.28f, cx + u * 0.06f, cy - u * 0.26f, lw * 2.2f, C.gold)
            }
            "dec_toalheiro", "cat_dec_toalheiro_rosa" -> {
                // Toalheiro aquecido com toalhas dobradas
                val railCol = Color.rgb(205, 215, 225)
                D.line(c, cx - u * 0.25f, cy - u * 0.35f, cx - u * 0.25f, cy + u * 0.35f, lw * 2.5f, railCol)
                D.line(c, cx + u * 0.25f, cy - u * 0.35f, cx + u * 0.25f, cy + u * 0.35f, lw * 2.5f, railCol)
                for (k in 0..3) {
                    val ry = cy - u * 0.25f + k * u * 0.18f
                    D.line(c, cx - u * 0.25f, ry, cx + u * 0.25f, ry, lw * 2.0f, railCol)
                }
                val towCol = if (id == "cat_dec_toalheiro_rosa") Color.rgb(255, 185, 215) else Color.rgb(90, 160, 240)
                D.rrect(c, cx - u * 0.18f, cy - u * 0.10f, cx + u * 0.18f, cy + u * 0.28f, u * 0.03f, towCol, ol, lw)
            }
            "dec_espelho_wc" -> {
                // Espelho redondo com luz LED suave
                D.circle(c, cx, cy - u * 0.15f, u * 0.36f, D.alpha(Color.rgb(255, 245, 210), 160))
                D.circle(c, cx, cy - u * 0.15f, u * 0.30f, Color.rgb(235, 245, 255), ol, lw * 1.2f)
                D.line(c, cx - u * 0.10f, cy - u * 0.30f, cx + u * 0.08f, cy - u * 0.05f, lw * 1.8f, D.alpha(Color.WHITE, 210))
            }

            // ========================================== CULTURA PORTUGUESA & PERU
            "dec_galo_barcelos" -> {
                // Galo de Barcelos português
                val gcx = cx; val gcy = cy - u * 0.10f
                D.oval(c, gcx, gcy + u * 0.15f, u * 0.22f, u * 0.18f, Color.BLACK, ol, lw)
                D.circle(c, gcx + u * 0.08f, gcy - u * 0.12f, u * 0.11f, Color.BLACK, ol, lw)
                // Crista vermelha
                D.circle(c, gcx + u * 0.06f, gcy - u * 0.25f, u * 0.07f, Color.RED)
                D.circle(c, gcx + u * 0.12f, gcy - u * 0.24f, u * 0.06f, Color.RED)
                // Bico amarelo
                D.poly(c, C.gold, 0, 0f, gcx + u * 0.16f, gcy - u * 0.12f, gcx + u * 0.26f, gcy - u * 0.09f, gcx + u * 0.16f, gcy - u * 0.06f)
                // Corações vermelhos nas asas
                D.heart(c, gcx - u * 0.02f, gcy + u * 0.12f, u * 0.07f, Color.RED)
                D.circle(c, gcx - u * 0.02f, gcy + u * 0.12f, u * 0.02f, C.gold)
            }
            "dec_guitarra_fado" -> {
                // Guitarra Portuguesa de Fado
                D.oval(c, cx, cy + u * 0.08f, u * 0.25f, u * 0.28f, Color.rgb(165, 85, 45), ol, lw)
                D.circle(c, cx, cy + u * 0.08f, u * 0.07f, Color.BLACK)
                D.rrect(c, cx - u * 0.035f, cy - u * 0.35f, cx + u * 0.035f, cy - u * 0.08f, 2f, Color.rgb(90, 45, 25), ol, lw * 0.7f)
                // Leque de afinação no topo
                D.poly(c, Color.rgb(215, 175, 55), ol, lw * 0.7f, cx - u * 0.08f, cy - u * 0.45f, cx + u * 0.08f, cy - u * 0.45f, cx, cy - u * 0.35f)
            }
            "dec_azulejo_lisboa" -> {
                // Painel de Azulejo de Lisboa com Caravela
                D.rrect(c, cx - u * 0.45f, cy - u * 0.42f, cx + u * 0.45f, cy + u * 0.38f, u * 0.04f, Color.rgb(238, 244, 255), Color.rgb(32, 75, 155), lw * 1.5f)
                // Caravela azul
                D.poly(c, Color.rgb(28, 62, 135), 0, 0f, cx - u * 0.26f, cy + u * 0.15f, cx + u * 0.26f, cy + u * 0.15f, cx + u * 0.18f, cy + u * 0.25f, cx - u * 0.18f, cy + u * 0.25f)
                // Velas com cruz
                D.poly(c, Color.WHITE, Color.rgb(28, 62, 135), lw * 0.8f, cx - u * 0.18f, cy - u * 0.18f, cx, cy - u * 0.18f, cx, cy + u * 0.08f, cx - u * 0.18f, cy + u * 0.08f)
                D.poly(c, Color.WHITE, Color.rgb(28, 62, 135), lw * 0.8f, cx + u * 0.02f, cy - u * 0.18f, cx + u * 0.20f, cy - u * 0.18f, cx + u * 0.20f, cy + u * 0.08f, cx + u * 0.02f, cy + u * 0.08f)
                D.line(c, cx - u * 0.10f, cy - u * 0.10f, cx - u * 0.10f, cy + u * 0.02f, lw, Color.RED)
                D.line(c, cx - u * 0.14f, cy - u * 0.04f, cx - u * 0.06f, cy - u * 0.04f, lw, Color.RED)
            }
            "dec_andorinhas", "cat_dec_andorinhas_rosa" -> {
                // Andorinhas de Cerâmica (estilo Bordalo Pinheiro)
                val aCol = if (id == "cat_dec_andorinhas_rosa") Color.rgb(255, 115, 165) else Color.rgb(35, 38, 48)
                val positions = listOf(Pair(-0.15f, -0.25f), Pair(0.08f, -0.10f), Pair(-0.05f, 0.12f))
                for (p in positions) {
                    val ax = cx + p.first * u; val ay = cy + p.second * u
                    D.oval(c, ax, ay, u * 0.08f, u * 0.04f, aCol)
                    D.poly(c, aCol, 0, 0f, ax - u * 0.06f, ay - u * 0.03f, ax + u * 0.02f, ay - u * 0.10f, ax, ay)
                    D.poly(c, aCol, 0, 0f, ax - u * 0.06f, ay + u * 0.03f, ax + u * 0.02f, ay + u * 0.10f, ax, ay)
                }
            }
            "dec_sol_inti" -> {
                // Sol Inca Sagrado em Ouro
                val scx = cx; val scy = cy - u * 0.15f
                D.circle(c, scx, scy, u * 0.20f, C.incaGold, Color.rgb(190, 140, 40), lw * 1.3f)
                D.circle(c, scx, scy, u * 0.12f, Color.rgb(255, 230, 95))
                for (k in 0..11) {
                    val a = (k * 30) * Math.PI / 180.0
                    val r1 = u * 0.22f; val r2 = u * 0.32f
                    val x1 = scx + (Math.cos(a) * r1).toFloat(); val y1 = scy + (Math.sin(a) * r1).toFloat()
                    val x2 = scx + (Math.cos(a) * r2).toFloat(); val y2 = scy + (Math.sin(a) * r2).toFloat()
                    D.line(c, x1, y1, x2, y2, lw * 1.8f, C.incaGold)
                }
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
            }
            "dec_rug" -> {
                D.oval(c, cx, cy, u * 1.0f, u * 0.2f, C.pink, ol, lw)
                D.oval(c, cx, cy, u * 0.72f, u * 0.14f, C.cream)
                D.oval(c, cx, cy, u * 0.42f, u * 0.08f, C.lav)
            }
            "dec_plant" -> {
                D.oval(c, cx - u * 0.18f, cy - u * 0.5f, u * 0.14f, u * 0.3f, C.green, ol, lw)
                D.oval(c, cx + u * 0.18f, cy - u * 0.45f, u * 0.14f, u * 0.28f, C.greenD, ol, lw)
                D.oval(c, cx, cy - u * 0.62f, u * 0.14f, u * 0.34f, C.green, ol, lw)
                D.poly(c, C.peach, ol, lw, cx - u * 0.3f, cy - u * 0.2f, cx + u * 0.3f, cy - u * 0.2f, cx + u * 0.22f, cy + u * 0.35f, cx - u * 0.22f, cy + u * 0.35f)
            }
            "dec_lamp" -> {
                if (nightNow > 0.3f) D.circle(c, cx, cy - u * 0.5f, u * 0.7f, D.alpha(C.butter, (nightNow * 110).toInt()))
                D.poly(c, C.butter, ol, lw, cx - u * 0.2f, cy - u * 0.95f, cx + u * 0.2f, cy - u * 0.95f, cx + u * 0.38f, cy - u * 0.5f, cx - u * 0.38f, cy - u * 0.5f)
                D.line(c, cx, cy - u * 0.5f, cx, cy + u * 0.3f, lw * 2.5f, C.brown)
                D.oval(c, cx, cy + u * 0.33f, u * 0.25f, u * 0.07f, C.brown, ol, lw)
            }
            "dec_plush" -> {
                D.oval(c, cx, cy - u * 0.2f, u * 0.28f, u * 0.25f, C.butter, ol, lw)
                D.circle(c, cx, cy - u * 0.5f, u * 0.19f, C.butter, ol, lw)
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
            // Itens Bunny & Portugal & Peru
            if (has("dec_machupicchu")) drawDecor(c, "dec_machupicchu", r.left + w * 0.8f, r.top + h * 0.2f, w * 0.32f)
            if (has("dec_tapete_peruano")) drawDecor(c, "dec_tapete_peruano", r.left + w * 0.5f, fy + h * 0.27f, w * 0.42f)
            if (has("dec_lhama")) drawDecor(c, "dec_lhama", r.left + w * 0.16f, fy + h * 0.32f, w * 0.26f)
            if (has("dec_zampogna")) drawDecor(c, "dec_zampogna", r.left + w * 0.16f, r.top + h * 0.24f, w * 0.24f)
            if (has("dec_galo_barcelos")) drawDecor(c, "dec_galo_barcelos", r.left + w * 0.84f, fy + h * 0.28f, w * 0.26f)
            if (has("dec_guitarra_fado")) drawDecor(c, "dec_guitarra_fado", r.left + w * 0.86f, fy + h * 0.18f, w * 0.28f)
            if (has("dec_azulejo_lisboa")) drawDecor(c, "dec_azulejo_lisboa", r.left + w * 0.5f, r.top + h * 0.20f, w * 0.34f)
            if (has("dec_andorinhas")) drawDecor(c, "dec_andorinhas", r.left + w * 0.26f, r.top + h * 0.16f, w * 0.26f)
            if (has("dec_sol_inti")) drawDecor(c, "dec_sol_inti", r.left + w * 0.5f, r.top + h * 0.18f, w * 0.32f)

            // Casa de Banho (WC)
            if (has("dec_banheira")) drawDecor(c, "dec_banheira", r.left + w * 0.72f, fy + h * 0.22f, w * 0.44f)
            if (has("cat_dec_banheira_rosa")) drawDecor(c, "cat_dec_banheira_rosa", r.left + w * 0.72f, fy + h * 0.22f, w * 0.44f)
            if (has("dec_sanita")) drawDecor(c, "dec_sanita", r.left + w * 0.90f, fy + h * 0.16f, w * 0.22f)
            if (has("dec_lavatorio")) drawDecor(c, "dec_lavatorio", r.left + w * 0.20f, fy + h * 0.16f, w * 0.28f)
            if (has("cat_dec_lavatorio_rosa")) drawDecor(c, "cat_dec_lavatorio_rosa", r.left + w * 0.20f, fy + h * 0.16f, w * 0.28f)
            if (has("dec_toalheiro")) drawDecor(c, "dec_toalheiro", r.left + w * 0.38f, r.top + h * 0.32f, w * 0.22f)
            if (has("cat_dec_toalheiro_rosa")) drawDecor(c, "cat_dec_toalheiro_rosa", r.left + w * 0.38f, r.top + h * 0.32f, w * 0.22f)
            if (has("dec_espelho_wc")) drawDecor(c, "dec_espelho_wc", r.left + w * 0.20f, r.top + h * 0.22f, w * 0.24f)

            // Itens Cat
            if (has("cat_dec_plush")) drawDecor(c, "cat_dec_plush", r.left + w * 0.18f, fy + h * 0.32f, w * 0.24f)
            if (has("cat_dec_rug")) drawDecor(c, "cat_dec_rug", r.left + w * 0.5f, fy + h * 0.27f, w * 0.40f)
            if (has("cat_dec_lamp")) drawDecor(c, "cat_dec_lamp", r.left + w * 0.88f, fy + h * 0.13f, w * 0.22f)
            if (has("cat_dec_mirror")) drawDecor(c, "cat_dec_mirror", r.left + w * 0.8f, r.top + h * 0.22f, w * 0.26f)
            if (has("cat_dec_andorinhas_rosa")) drawDecor(c, "cat_dec_andorinhas_rosa", r.left + w * 0.26f, r.top + h * 0.16f, w * 0.26f)
            if (has("cat_dec_jarra_flores")) drawDecor(c, "cat_dec_plush", r.left + w * 0.82f, fy + h * 0.26f, w * 0.22f)

            // Itens Universais
            if (has("dec_window")) drawDecor(c, "dec_window", r.left + w * 0.2f, r.top + h * 0.22f, w * 0.26f)
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
