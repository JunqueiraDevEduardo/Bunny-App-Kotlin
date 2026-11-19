package com.example.bunny

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Core.kt
 * Contém as definições visuais fundamentais do aplicativo Bunny:
 * - C: Paleta de cores (tons pastel e tradicionais andinos/peruanos).
 * - D: Utilitários e atalhos de desenho em Canvas (formas, linhas, textos e botões).
 * - Icons: Sistema de renderização vetorial programática de ícones e itens.
 */

/** Paleta pastel (estilo livro infantil) e cores tradicionais andinas e peruanas. */
object C {
    val cream = Color.rgb(255, 246, 236)
    val pink = Color.rgb(255, 200, 215)
    val pinkD = Color.rgb(246, 160, 185)
    val peach = Color.rgb(255, 217, 184)
    val mint = Color.rgb(200, 235, 217)
    val sky = Color.rgb(203, 230, 247)
    val lav = Color.rgb(220, 208, 243)
    val butter = Color.rgb(255, 240, 184)
    val ink = Color.rgb(122, 92, 97)
    val white = Color.WHITE
    val gold = Color.rgb(246, 200, 95)
    val green = Color.rgb(160, 214, 160)
    val greenD = Color.rgb(120, 190, 130)
    val orange = Color.rgb(255, 176, 112)
    val red = Color.rgb(240, 120, 130)
    val brown = Color.rgb(196, 150, 120)
    val outline = Color.rgb(214, 180, 188)
    val blue = Color.rgb(170, 205, 240)
    // Cores tradicionais andinas e peruanas
    val peruRed = Color.rgb(220, 48, 50)
    val peruRedD = Color.rgb(175, 30, 35)
    val incaGold = Color.rgb(255, 200, 45)
    val turquoise = Color.rgb(45, 185, 175)
    val purpleAndes = Color.rgb(105, 45, 115)
    val chichaMorada = Color.rgb(95, 25, 75)
    val chocloYellow = Color.rgb(255, 225, 115)
    val caramel = Color.rgb(215, 145, 88)
    val cocoa = Color.rgb(120, 78, 56)
    val greyFur = Color.rgb(195, 200, 208)
    val catFur = Color.rgb(255, 240, 225)
    val catSpot = Color.rgb(240, 185, 145)
}

/**
 * Utilitário de desenho (Canvas Helper).
 * Fornece métodos encapsulados e seguros para desenhar primitivas gráficas
 * (círculos, ovais, retângulos arredondados, polígonos, textos e botões) com anti-aliasing.
 */
object D {
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    val path = Path()
    private val rf = RectF()
    private val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }

    /** Configura o pincel (Paint) para modo de preenchimento (FILL). */
    fun f(color: Int) {
        p.style = Paint.Style.FILL; p.color = color; p.strokeWidth = 0f
    }

    /** Configura o pincel (Paint) para modo de contorno (STROKE) com bordas arredondadas. */
    fun s(color: Int, w: Float) {
        p.style = Paint.Style.STROKE; p.color = color; p.strokeWidth = w
        p.strokeCap = Paint.Cap.ROUND; p.strokeJoin = Paint.Join.ROUND
    }

    /** Retorna a cor com opacidade (alfa) ajustada. */
    fun alpha(color: Int, a: Int) = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color))

    /** Mistura linear entre duas cores (a e b) com fator t (0f a 1f). */
    fun mix(a: Int, b: Int, t: Float): Int {
        val k = t.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(a) + (Color.red(b) - Color.red(a)) * k).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * k).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * k).toInt()
        )
    }

    /** Desenha uma oval com preenchimento opcional e contorno. */
    fun oval(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, fill: Int, line: Int = 0, lw: Float = 0f) {
        rf.set(cx - rx, cy - ry, cx + rx, cy + ry)
        f(fill); c.drawOval(rf, p)
        if (lw > 0f) { s(line, lw); c.drawOval(rf, p) }
    }

    /** Desenha um círculo perfeito. */
    fun circle(c: Canvas, cx: Float, cy: Float, r: Float, fill: Int, line: Int = 0, lw: Float = 0f) =
        oval(c, cx, cy, r, r, fill, line, lw)

    /** Desenha um retângulo com cantos arredondados. */
    fun rrect(c: Canvas, l: Float, t: Float, r: Float, b: Float, rad: Float, fill: Int, line: Int = 0, lw: Float = 0f) {
        rf.set(l, t, r, b)
        f(fill); c.drawRoundRect(rf, rad, rad, p)
        if (lw > 0f) { s(line, lw); c.drawRoundRect(rf, rad, rad, p) }
    }

    fun rrect(c: Canvas, r: RectF, rad: Float, fill: Int, line: Int = 0, lw: Float = 0f) =
        rrect(c, r.left, r.top, r.right, r.bottom, rad, fill, line, lw)

    /** Desenha uma linha reta. */
    fun line(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, w: Float, color: Int) {
        s(color, w); c.drawLine(x1, y1, x2, y2, p)
    }

    /** Desenha um polígono personalizado a partir de uma sequência de coordenadas (pares x, y). */
    fun poly(c: Canvas, fill: Int, line: Int, lw: Float, vararg v: Float) {
        path.reset()
        path.moveTo(v[0], v[1])
        var i = 2
        while (i < v.size) { path.lineTo(v[i], v[i + 1]); i += 2 }
        path.close()
        f(fill); c.drawPath(path, p)
        if (lw > 0f) { s(line, lw); c.drawPath(path, p) }
    }

    /** Renderiza texto centralizado ou alinhado no Canvas. */
    fun text(c: Canvas, str: String, x: Float, y: Float, size: Float, color: Int, align: Paint.Align = Paint.Align.CENTER) {
        tp.textSize = size; tp.textAlign = align; tp.color = color
        c.drawText(str, x, y, tp)
    }

    fun textW(str: String, size: Float): Float {
        tp.textSize = size
        return tp.measureText(str)
    }

    /** Desenha um botão arredondado interativo com texto descritivo. */
    fun button(c: Canvas, r: RectF, label: String, fill: Int, textColor: Int = C.ink, size: Float = r.height() * 0.36f) {
        rrect(c, r, r.height() * 0.4f, fill, C.outline, max(2f, r.height() * 0.03f))
        text(c, label, r.centerX(), r.centerY() + size * 0.35f, size, textColor)
    }
}

/**
 * Catálogo e renderizador programático de ícones vetoriais.
 * Permite desenhar dezenas de ícones (alimentos, cultura andina, brinquedos, itens mágicos)
 * diretamente no Canvas de forma customizada e dimensionável.
 */
object Icons {
    const val CARROT = 0; const val APPLE = 1; const val FLOWER = 2; const val STAR = 3; const val HEART = 4
    const val MOON = 5; const val CLOUD = 6; const val BERRY = 7; const val CUPCAKE = 8; const val LETTUCE = 9
    const val BALL = 10; const val YARN = 11; const val BUBBLE = 12; const val HOUSE = 13; const val HOP = 14
    const val CARDS = 15; const val PUZZLE = 16; const val BAG = 17; const val HAT = 18; const val PAW = 19
    const val ROCK = 20; const val GOLDEN = 21; const val SPONGE = 22; const val SHOWER = 23; const val SPARK = 24
    const val LAMP = 25; const val COIN = 26; const val BED = 27; const val BOWL = 28
    // Comida Peruana
    const val CEVICHE = 29; const val LOMO_SALTADO = 30; const val CHICHA_MORADA = 31
    const val ALFAJOR = 32; const val CAUSA = 33; const val CHOCLO = 34
    // Cultura, Roupa e Sapatilhas
    const val CHULLO = 35; const val PONCHO = 36; const val LLAMA = 37
    const val SNEAKER = 38; const val POTION = 39; const val COLOR_PALETTE = 40
    // Brinquedos e Companhia (Gatinha)
    const val CAT = 41; const val WAND = 42; const val CAR_TOY = 43; const val CARROT_PLUSH = 44

    /** Seleciona e desenha o ícone correspondente ao ID fornecido na posição (cx, cy) com raio r. */
    fun draw(c: Canvas, id: Int, cx: Float, cy: Float, r: Float) {
        if (r < 1f) return
        val ol = C.outline
        val lw = max(1.2f, r * 0.07f)
        when (id) {
            CARROT, GOLDEN -> {
                val body = if (id == GOLDEN) C.gold else C.orange
                D.oval(c, cx - 0.2f * r, cy - 0.72f * r, 0.12f * r, 0.3f * r, C.green, ol, lw)
                D.oval(c, cx, cy - 0.82f * r, 0.12f * r, 0.36f * r, C.greenD, ol, lw)
                D.oval(c, cx + 0.2f * r, cy - 0.72f * r, 0.12f * r, 0.3f * r, C.green, ol, lw)
                D.poly(c, body, ol, lw, cx - 0.38f * r, cy - 0.5f * r, cx + 0.38f * r, cy - 0.5f * r, cx, cy + 0.95f * r)
                D.line(c, cx - 0.16f * r, cy - 0.2f * r, cx + 0.06f * r, cy - 0.2f * r, lw, D.mix(body, C.red, 0.4f))
                D.line(c, cx - 0.08f * r, cy + 0.2f * r, cx + 0.12f * r, cy + 0.2f * r, lw, D.mix(body, C.red, 0.4f))
                if (id == GOLDEN) drawSpark(c, cx + 0.5f * r, cy - 0.3f * r, r * 0.3f)
            }
            APPLE -> {
                D.circle(c, cx - 0.22f * r, cy + 0.1f * r, 0.55f * r, C.red, ol, lw)
                D.circle(c, cx + 0.22f * r, cy + 0.1f * r, 0.55f * r, C.red, ol, lw)
                D.line(c, cx, cy - 0.45f * r, cx + 0.08f * r, cy - 0.8f * r, lw * 1.4f, C.brown)
                D.oval(c, cx + 0.3f * r, cy - 0.62f * r, 0.22f * r, 0.12f * r, C.green, ol, lw)
                D.circle(c, cx - 0.35f * r, cy - 0.05f * r, 0.1f * r, D.alpha(C.white, 170))
            }
            FLOWER -> {
                for (i in 0 until 5) {
                    val a = (i * 72 - 90) * PI / 180
                    D.circle(c, cx + (cos(a) * 0.5f * r).toFloat(), cy + (sin(a) * 0.5f * r).toFloat(), 0.32f * r, C.pink, ol, lw)
                }
                D.circle(c, cx, cy, 0.26f * r, C.butter, ol, lw)
            }
            STAR -> starPath(c, cx, cy, r, C.gold, ol, lw)
            HEART -> {
                D.path.reset()
                D.path.moveTo(cx, cy + 0.75f * r)
                D.path.cubicTo(cx - 1.15f * r, cy - 0.05f * r, cx - 0.55f * r, cy - 0.95f * r, cx, cy - 0.3f * r)
                D.path.cubicTo(cx + 0.55f * r, cy - 0.95f * r, cx + 1.15f * r, cy - 0.05f * r, cx, cy + 0.75f * r)
                D.f(C.pinkD); c.drawPath(D.path, D.p)
                D.s(ol, lw); c.drawPath(D.path, D.p)
            }
            MOON -> {
                val a = Path().apply { addCircle(cx, cy, r * 0.8f, Path.Direction.CW) }
                val b = Path().apply { addCircle(cx + r * 0.42f, cy - r * 0.2f, r * 0.68f, Path.Direction.CW) }
                a.op(b, Path.Op.DIFFERENCE)
                D.f(C.butter); c.drawPath(a, D.p)
                D.s(ol, lw); c.drawPath(a, D.p)
            }
            CLOUD -> {
                D.circle(c, cx - 0.4f * r, cy + 0.1f * r, 0.4f * r, C.white, ol, lw)
                D.circle(c, cx + 0.4f * r, cy + 0.1f * r, 0.4f * r, C.white, ol, lw)
                D.circle(c, cx, cy - 0.15f * r, 0.5f * r, C.white, ol, lw)
                D.rrect(c, cx - 0.5f * r, cy + 0.05f * r, cx + 0.5f * r, cy + 0.5f * r, 0.2f * r, C.white)
            }
            BERRY -> {
                D.poly(c, C.red, ol, lw, cx - 0.55f * r, cy - 0.35f * r, cx + 0.55f * r, cy - 0.35f * r, cx, cy + 0.85f * r)
                D.circle(c, cx - 0.35f * r, cy - 0.25f * r, 0.3f * r, C.red)
                D.circle(c, cx + 0.35f * r, cy - 0.25f * r, 0.3f * r, C.red)
                D.oval(c, cx - 0.2f * r, cy - 0.45f * r, 0.2f * r, 0.1f * r, C.greenD)
                D.oval(c, cx + 0.2f * r, cy - 0.45f * r, 0.2f * r, 0.1f * r, C.greenD)
                for (i in 0 until 4) D.circle(c, cx + (i - 1.5f) * 0.2f * r, cy + (if (i % 2 == 0) 0.05f else 0.35f) * r, 0.04f * r, C.butter)
            }
            CUPCAKE -> {
                D.poly(c, C.peach, ol, lw, cx - 0.55f * r, cy, cx + 0.55f * r, cy, cx + 0.38f * r, cy + 0.85f * r, cx - 0.38f * r, cy + 0.85f * r)
                D.circle(c, cx, cy - 0.15f * r, 0.6f * r, C.pink, ol, lw)
                D.circle(c, cx, cy - 0.7f * r, 0.13f * r, C.red)
                D.line(c, cx - 0.2f * r, cy + 0.1f * r, cx - 0.14f * r, cy + 0.8f * r, lw, ol)
                D.line(c, cx + 0.2f * r, cy + 0.1f * r, cx + 0.14f * r, cy + 0.8f * r, lw, ol)
            }
            LETTUCE -> {
                D.oval(c, cx - 0.3f * r, cy, 0.45f * r, 0.7f * r, C.green, ol, lw)
                D.oval(c, cx + 0.3f * r, cy, 0.45f * r, 0.7f * r, C.greenD, ol, lw)
                D.oval(c, cx, cy - 0.05f * r, 0.4f * r, 0.75f * r, C.green, ol, lw)
                D.line(c, cx, cy - 0.5f * r, cx, cy + 0.5f * r, lw, C.greenD)
            }
            BALL -> {
                D.circle(c, cx, cy, 0.8f * r, C.pink, ol, lw)
                D.s(C.white, lw * 2f)
                D.path.reset(); D.path.addArc(cx - 0.8f * r, cy - 0.8f * r, cx + 0.8f * r, cy + 0.8f * r, 200f, 140f)
                c.drawPath(D.path, D.p)
                D.path.reset(); D.path.addArc(cx - 0.5f * r, cy - 0.5f * r, cx + 0.5f * r, cy + 0.5f * r, 20f, 140f)
                c.drawPath(D.path, D.p)
                D.circle(c, cx - 0.3f * r, cy - 0.35f * r, 0.12f * r, D.alpha(C.white, 200))
            }
            YARN -> {
                D.circle(c, cx, cy, 0.75f * r, C.lav, ol, lw)
                D.s(C.white, lw)
                for (i in -1..1) {
                    D.path.reset()
                    D.path.addArc(cx - 0.75f * r, cy - 0.75f * r + i * 0.25f * r, cx + 0.75f * r, cy + 0.75f * r + i * 0.25f * r, 30f, 120f)
                    c.drawPath(D.path, D.p)
                }
                D.path.reset(); D.path.moveTo(cx + 0.6f * r, cy + 0.5f * r)
                D.path.quadTo(cx + 1.0f * r, cy + 0.9f * r, cx + 0.5f * r, cy + 1.0f * r)
                D.s(C.lav, lw * 1.5f); c.drawPath(D.path, D.p)
            }
            BUBBLE -> {
                D.circle(c, cx, cy, r, D.alpha(C.sky, 150), D.alpha(C.white, 230), lw)
                D.circle(c, cx - 0.35f * r, cy - 0.35f * r, 0.2f * r, D.alpha(C.white, 230))
            }
            HOUSE -> {
                D.rrect(c, cx - 0.65f * r, cy - 0.1f * r, cx + 0.65f * r, cy + 0.8f * r, 0.1f * r, C.cream, ol, lw)
                D.poly(c, C.pinkD, ol, lw, cx - 0.9f * r, cy - 0.05f * r, cx, cy - 0.85f * r, cx + 0.9f * r, cy - 0.05f * r)
                D.rrect(c, cx - 0.18f * r, cy + 0.25f * r, cx + 0.18f * r, cy + 0.8f * r, 0.08f * r, C.peach, ol, lw)
            }
            HOP -> {
                D.oval(c, cx - 0.28f * r, cy - 0.55f * r, 0.17f * r, 0.5f * r, C.white, ol, lw)
                D.oval(c, cx + 0.28f * r, cy - 0.55f * r, 0.17f * r, 0.5f * r, C.white, ol, lw)
                D.oval(c, cx - 0.28f * r, cy - 0.5f * r, 0.08f * r, 0.32f * r, C.pink)
                D.oval(c, cx + 0.28f * r, cy - 0.5f * r, 0.08f * r, 0.32f * r, C.pink)
                D.oval(c, cx, cy + 0.2f * r, 0.7f * r, 0.6f * r, C.white, ol, lw)
                D.circle(c, cx - 0.25f * r, cy + 0.12f * r, 0.07f * r, C.ink)
                D.circle(c, cx + 0.25f * r, cy + 0.12f * r, 0.07f * r, C.ink)
                D.oval(c, cx, cy + 0.32f * r, 0.08f * r, 0.06f * r, C.pinkD)
            }
            CARDS -> {
                D.rrect(c, cx - 0.85f * r, cy - 0.7f * r, cx + 0.15f * r, cy + 0.6f * r, 0.15f * r, C.lav, ol, lw)
                D.rrect(c, cx - 0.15f * r, cy - 0.6f * r, cx + 0.85f * r, cy + 0.7f * r, 0.15f * r, C.white, ol, lw)
                draw(c, HEART, cx + 0.35f * r, cy + 0.05f * r, r * 0.35f)
            }
            PUZZLE -> {
                D.rrect(c, cx - 0.85f * r, cy - 0.85f * r, cx - 0.05f * r, cy - 0.05f * r, 0.12f * r, C.pink, ol, lw)
                D.rrect(c, cx + 0.05f * r, cy - 0.85f * r, cx + 0.85f * r, cy - 0.05f * r, 0.12f * r, C.mint, ol, lw)
                D.rrect(c, cx - 0.85f * r, cy + 0.05f * r, cx - 0.05f * r, cy + 0.85f * r, 0.12f * r, C.butter, ol, lw)
            }
            BAG -> {
                D.s(ol, lw * 1.4f)
                D.path.reset(); D.path.addArc(cx - 0.4f * r, cy - 0.85f * r, cx + 0.4f * r, cy + 0.05f * r, 180f, 180f)
                c.drawPath(D.path, D.p)
                D.rrect(c, cx - 0.7f * r, cy - 0.35f * r, cx + 0.7f * r, cy + 0.8f * r, 0.15f * r, C.pinkD, ol, lw)
                draw(c, HEART, cx, cy + 0.25f * r, r * 0.3f)
            }
            HAT -> {
                D.poly(c, C.lav, ol, lw, cx - 0.55f * r, cy + 0.6f * r, cx + 0.55f * r, cy + 0.6f * r, cx, cy - 0.7f * r)
                D.circle(c, cx, cy - 0.75f * r, 0.18f * r, C.pink, ol, lw)
                D.circle(c, cx - 0.1f * r, cy + 0.2f * r, 0.08f * r, C.white)
                D.circle(c, cx + 0.15f * r, cy - 0.1f * r, 0.08f * r, C.white)
            }
            PAW -> {
                D.oval(c, cx, cy + 0.3f * r, 0.55f * r, 0.45f * r, C.pinkD)
                D.circle(c, cx - 0.55f * r, cy - 0.15f * r, 0.2f * r, C.pinkD)
                D.circle(c, cx - 0.2f * r, cy - 0.5f * r, 0.2f * r, C.pinkD)
                D.circle(c, cx + 0.2f * r, cy - 0.5f * r, 0.2f * r, C.pinkD)
                D.circle(c, cx + 0.55f * r, cy - 0.15f * r, 0.2f * r, C.pinkD)
            }
            ROCK -> {
                D.oval(c, cx, cy + 0.15f * r, 0.8f * r, 0.6f * r, Color.rgb(190, 190, 205), ol, lw)
                D.oval(c, cx - 0.25f * r, cy - 0.05f * r, 0.2f * r, 0.12f * r, D.alpha(C.white, 160))
            }
            SPONGE -> {
                D.rrect(c, cx - 0.8f * r, cy - 0.5f * r, cx + 0.8f * r, cy + 0.5f * r, 0.25f * r, C.butter, ol, lw)
                for (i in 0 until 5) D.circle(c, cx + (i - 2) * 0.3f * r, cy + (if (i % 2 == 0) -0.12f else 0.12f) * r, 0.07f * r, C.gold)
            }
            SHOWER -> {
                D.s(C.blue, lw * 2f)
                D.path.reset(); D.path.moveTo(cx - 0.8f * r, cy + 0.8f * r); D.path.lineTo(cx - 0.8f * r, cy - 0.7f * r)
                D.path.quadTo(cx - 0.8f * r, cy - 0.9f * r, cx - 0.5f * r, cy - 0.9f * r); D.path.lineTo(cx, cy - 0.9f * r)
                c.drawPath(D.path, D.p)
                D.poly(c, C.blue, ol, lw, cx - 0.2f * r, cy - 0.95f * r, cx + 0.6f * r, cy - 0.95f * r, cx + 0.8f * r, cy - 0.45f * r, cx - 0.4f * r, cy - 0.45f * r)
                for (i in 0 until 4) D.line(c, cx - 0.3f * r + i * 0.3f * r, cy - 0.2f * r, cx - 0.35f * r + i * 0.3f * r, cy + 0.2f * r, lw, D.alpha(C.blue, 200))
            }
            SPARK -> drawSpark(c, cx, cy, r)
            LAMP -> {
                D.poly(c, C.butter, ol, lw, cx - 0.5f * r, cy - 0.7f * r, cx + 0.5f * r, cy - 0.7f * r, cx + 0.8f * r, cy + 0.05f * r, cx - 0.8f * r, cy + 0.05f * r)
                D.line(c, cx, cy + 0.05f * r, cx, cy + 0.65f * r, lw * 1.6f, C.brown)
                D.oval(c, cx, cy + 0.72f * r, 0.4f * r, 0.12f * r, C.brown)
            }
            COIN -> {
                D.circle(c, cx, cy, r, C.gold, ol, lw)
                D.circle(c, cx, cy, r * 0.65f, C.butter)
                D.text(c, "B", cx, cy + r * 0.32f, r * 0.9f, C.gold)
            }
            BED -> {
                D.rrect(c, cx - 0.9f * r, cy - 0.2f * r, cx + 0.9f * r, cy + 0.6f * r, 0.2f * r, C.pink, ol, lw)
                D.rrect(c, cx - 0.9f * r, cy - 0.8f * r, cx - 0.5f * r, cy + 0.6f * r, 0.15f * r, C.peach, ol, lw)
            }
            BOWL -> {
                D.oval(c, cx, cy, 0.8f * r, 0.4f * r, C.peach, ol, lw)
            }
            CEVICHE -> {
                // Tigela de Ceviche peruano com peixe marinado, lima, milho e cebola roxa
                D.oval(c, cx, cy + 0.15f * r, 0.82f * r, 0.42f * r, C.sky, ol, lw)
                D.oval(c, cx, cy - 0.02f * r, 0.72f * r, 0.35f * r, D.alpha(C.white, 240), ol, lw * 0.7f)
                // Cubos de peixe fresco marinados
                D.rrect(c, cx - 0.38f * r, cy - 0.15f * r, cx - 0.12f * r, cy + 0.05f * r, 0.05f * r, Color.rgb(255, 235, 240), ol, lw * 0.6f)
                D.rrect(c, cx + 0.05f * r, cy - 0.18f * r, cx + 0.32f * r, cy + 0.02f * r, 0.05f * r, Color.rgb(255, 235, 240), ol, lw * 0.6f)
                D.rrect(c, cx - 0.15f * r, cy - 0.05f * r, cx + 0.12f * r, cy + 0.15f * r, 0.05f * r, Color.rgb(255, 235, 240), ol, lw * 0.6f)
                // Tiras de cebola roxa peruana
                D.line(c, cx - 0.35f * r, cy - 0.08f * r, cx + 0.2f * r, cy - 0.16f * r, lw * 1.3f, C.purpleAndes)
                D.line(c, cx - 0.1f * r, cy + 0.08f * r, cx + 0.35f * r, cy, lw * 1.3f, C.purpleAndes)
                // Grãos de milho choclo gigante
                D.circle(c, cx - 0.45f * r, cy + 0.05f * r, 0.1f * r, C.chocloYellow, ol, lw * 0.6f)
                D.circle(c, cx - 0.32f * r, cy + 0.16f * r, 0.09f * r, C.chocloYellow, ol, lw * 0.6f)
                // Rodela de lima verde
                D.circle(c, cx + 0.45f * r, cy - 0.15f * r, 0.16f * r, C.greenD, ol, lw * 0.7f)
                D.circle(c, cx + 0.45f * r, cy - 0.15f * r, 0.12f * r, C.green)
            }
            LOMO_SALTADO -> {
                // Prato de Lomo Saltado com carne salteada, batatas fritas e tomate
                D.circle(c, cx, cy, 0.85f * r, C.white, ol, lw)
                D.circle(c, cx, cy, 0.72f * r, C.cream, ol, lw * 0.5f)
                // Batatas fritas douradas
                D.rrect(c, cx - 0.5f * r, cy + 0.05f * r, cx - 0.1f * r, cy + 0.22f * r, 0.04f * r, C.incaGold, ol, lw * 0.6f)
                D.rrect(c, cx - 0.45f * r, cy - 0.18f * r, cx - 0.08f * r, cy - 0.02f * r, 0.04f * r, C.incaGold, ol, lw * 0.6f)
                // Tiras de carne lomo
                D.rrect(c, cx - 0.1f * r, cy - 0.22f * r, cx + 0.38f * r, cy - 0.06f * r, 0.05f * r, C.cocoa, ol, lw * 0.7f)
                D.rrect(c, cx, cy + 0.02f * r, cx + 0.45f * r, cy + 0.18f * r, 0.05f * r, C.cocoa, ol, lw * 0.7f)
                // Fatias de tomate e cebola
                D.oval(c, cx + 0.12f * r, cy - 0.08f * r, 0.14f * r, 0.08f * r, C.red, ol, lw * 0.6f)
                D.line(c, cx - 0.2f * r, cy - 0.05f * r, cx + 0.25f * r, cy - 0.05f * r, lw * 1.2f, C.purpleAndes)
            }
            CHICHA_MORADA -> {
                // Copo alto de Chicha Morada peruana com milho roxo e palhinha
                D.poly(c, D.alpha(C.sky, 100), ol, lw, cx - 0.32f * r, cy - 0.7f * r, cx + 0.32f * r, cy - 0.7f * r, cx + 0.22f * r, cy + 0.75f * r, cx - 0.22f * r, cy + 0.75f * r)
                D.poly(c, C.chichaMorada, 0, 0f, cx - 0.28f * r, cy - 0.35f * r, cx + 0.28f * r, cy - 0.35f * r, cx + 0.2f * r, cy + 0.7f * r, cx - 0.2f * r, cy + 0.7f * r)
                // Palhinha listrada
                D.line(c, cx + 0.1f * r, cy + 0.2f * r, cx + 0.28f * r, cy - 0.9f * r, lw * 1.8f, C.peruRed)
                // Rodela de limão no topo
                D.circle(c, cx - 0.3f * r, cy - 0.65f * r, 0.18f * r, C.butter, C.gold, lw * 0.7f)
            }
            ALFAJOR -> {
                // Alfajor peruano com manjar branco polvilhado com açúcar em pó
                D.oval(c, cx, cy - 0.18f * r, 0.62f * r, 0.22f * r, C.butter, ol, lw)
                D.oval(c, cx, cy, 0.65f * r, 0.2f * r, C.caramel)
                D.oval(c, cx, cy + 0.18f * r, 0.62f * r, 0.22f * r, C.butter, ol, lw)
                // Açúcar em pó salpicado
                for (i in -2..2) D.circle(c, cx + i * 0.15f * r, cy - 0.18f * r + (if (i % 2 == 0) -0.04f else 0.04f) * r, 0.035f * r, C.white)
            }
            CAUSA -> {
                // Causa Limeña em camadas (batata amarela e abacate)
                D.rrect(c, cx - 0.55f * r, cy + 0.1f * r, cx + 0.55f * r, cy + 0.45f * r, 0.08f * r, C.incaGold, ol, lw)
                D.rrect(c, cx - 0.55f * r, cy - 0.15f * r, cx + 0.55f * r, cy + 0.12f * r, 0.04f * r, C.green, ol, lw * 0.6f)
                D.rrect(c, cx - 0.55f * r, cy - 0.45f * r, cx + 0.55f * r, cy - 0.13f * r, 0.08f * r, C.incaGold, ol, lw)
                // Azeitona preta decorativa no topo
                D.oval(c, cx, cy - 0.55f * r, 0.14f * r, 0.11f * r, C.ink, ol, lw * 0.6f)
            }
            CHOCLO -> {
                // Maçaroca de milho choclo andino gigante
                D.oval(c, cx - 0.12f * r, cy, 0.35f * r, 0.65f * r, C.chocloYellow, ol, lw)
                for (j in -2..2) for (i in -1..1) {
                    D.circle(c, cx - 0.12f * r + i * 0.16f * r, cy + j * 0.18f * r, 0.06f * r, C.butter)
                }
                // Bloco de queijo fresco
                D.rrect(c, cx + 0.2f * r, cy + 0.15f * r, cx + 0.65f * r, cy + 0.6f * r, 0.06f * r, C.white, ol, lw)
            }
            CHULLO -> {
                // Gorro Chullo tradicional dos Andes com abas e borlas
                D.poly(c, C.peruRed, ol, lw, cx - 0.55f * r, cy + 0.25f * r, cx + 0.55f * r, cy + 0.25f * r, cx, cy - 0.7f * r)
                // Padrões geométricos andinos
                D.rrect(c, cx - 0.5f * r, cy - 0.1f * r, cx + 0.5f * r, cy + 0.1f * r, 0.02f * r, C.incaGold)
                for (i in -2..2) D.poly(c, C.turquoise, 0, 0f, cx + i * 0.18f * r, cy - 0.08f * r, cx + (i + 0.08f) * r, cy + 0.08f * r, cx + (i - 0.08f) * r, cy + 0.08f * r)
                // Abas para as orelhas
                D.poly(c, C.peruRed, ol, lw, cx - 0.5f * r, cy + 0.2f * r, cx - 0.28f * r, cy + 0.2f * r, cx - 0.38f * r, cy + 0.65f * r)
                D.poly(c, C.peruRed, ol, lw, cx + 0.5f * r, cy + 0.2f * r, cx + 0.28f * r, cy + 0.2f * r, cx + 0.38f * r, cy + 0.65f * r)
                // Pompons / borlas
                D.circle(c, cx, cy - 0.75f * r, 0.15f * r, C.incaGold, ol, lw * 0.7f)
                D.circle(c, cx - 0.38f * r, cy + 0.75f * r, 0.09f * r, C.turquoise)
                D.circle(c, cx + 0.38f * r, cy + 0.75f * r, 0.09f * r, C.turquoise)
            }
            PONCHO -> {
                // Poncho peruano com riscas coloridas e franjas
                D.poly(c, C.peruRed, ol, lw, cx, cy - 0.6f * r, cx + 0.75f * r, cy + 0.3f * r, cx, cy + 0.75f * r, cx - 0.75f * r, cy + 0.3f * r)
                // Riscas geométricas aguayo
                D.poly(c, C.incaGold, 0, 0f, cx, cy - 0.25f * r, cx + 0.45f * r, cy + 0.25f * r, cx, cy + 0.55f * r, cx - 0.45f * r, cy + 0.25f * r)
                D.poly(c, C.turquoise, 0, 0f, cx, cy - 0.05f * r, cx + 0.25f * r, cy + 0.2f * r, cx, cy + 0.38f * r, cx - 0.25f * r, cy + 0.2f * r)
                D.circle(c, cx, cy - 0.25f * r, 0.14f * r, C.white, ol, lw * 0.7f)
            }
            LLAMA -> {
                // Lhama fofinha andina com borlas coloridas
                D.oval(c, cx, cy + 0.2f * r, 0.45f * r, 0.4f * r, C.white, ol, lw)
                D.rrect(c, cx - 0.18f * r, cy - 0.5f * r, cx + 0.18f * r, cy + 0.2f * r, 0.12f * r, C.white, ol, lw)
                // Orelhas compridas
                D.oval(c, cx - 0.18f * r, cy - 0.65f * r, 0.08f * r, 0.25f * r, C.white, ol, lw)
                D.oval(c, cx + 0.18f * r, cy - 0.65f * r, 0.08f * r, 0.25f * r, C.white, ol, lw)
                // Borlas coloridas nas orelhas
                D.circle(c, cx - 0.22f * r, cy - 0.52f * r, 0.06f * r, C.peruRed)
                D.circle(c, cx + 0.22f * r, cy - 0.52f * r, 0.06f * r, C.turquoise)
                // Olhos e focinho
                D.circle(c, cx - 0.08f * r, cy - 0.32f * r, 0.04f * r, C.ink)
                D.circle(c, cx + 0.08f * r, cy - 0.32f * r, 0.04f * r, C.ink)
                D.oval(c, cx, cy - 0.18f * r, 0.08f * r, 0.06f * r, C.peach)
            }
            SNEAKER -> {
                // Sapatilha estilosa com sola de borracha e atacadores
                D.poly(c, C.peruRed, ol, lw, cx - 0.55f * r, cy + 0.25f * r, cx - 0.5f * r, cy - 0.3f * r, cx - 0.05f * r, cy - 0.15f * r, cx + 0.55f * r, cy + 0.1f * r, cx + 0.55f * r, cy + 0.25f * r)
                // Sola de borracha branca
                D.rrect(c, cx - 0.6f * r, cy + 0.25f * r, cx + 0.65f * r, cy + 0.45f * r, 0.08f * r, C.white, ol, lw)
                // Biqueira e atacadores
                D.oval(c, cx + 0.5f * r, cy + 0.2f * r, 0.15f * r, 0.12f * r, C.white, ol, lw * 0.7f)
                D.line(c, cx - 0.15f * r, cy - 0.08f * r, cx + 0.15f * r, cy + 0.02f * r, lw * 1.5f, C.white)
                D.line(c, cx - 0.05f * r, cy - 0.02f * r, cx + 0.25f * r, cy + 0.08f * r, lw * 1.5f, C.white)
            }
            POTION -> {
                // Frasco de poção estilo Pou (mágica, cura/energia)
                D.poly(c, C.turquoise, ol, lw, cx - 0.12f * r, cy - 0.6f * r, cx + 0.12f * r, cy - 0.6f * r, cx + 0.45f * r, cy + 0.35f * r, cx, cy + 0.65f * r, cx - 0.45f * r, cy + 0.35f * r)
                D.circle(c, cx, cy + 0.15f * r, 0.12f * r, C.white)
                // Rolha de cortiça
                D.rrect(c, cx - 0.15f * r, cy - 0.78f * r, cx + 0.15f * r, cy - 0.58f * r, 0.04f * r, C.brown, ol, lw * 0.7f)
                drawSpark(c, cx + 0.3f * r, cy - 0.15f * r, r * 0.22f)
            }
            COLOR_PALETTE -> {
                // Paleta de cores para pintar o Bunny
                D.oval(c, cx, cy, 0.75f * r, 0.6f * r, C.butter, ol, lw)
                D.circle(c, cx + 0.35f * r, cy + 0.15f * r, 0.1f * r, C.cream, ol, lw * 0.7f)
                // Gotas de tinta coloridas
                D.circle(c, cx - 0.38f * r, cy - 0.2f * r, 0.1f * r, C.peruRed)
                D.circle(c, cx - 0.15f * r, cy - 0.32f * r, 0.1f * r, C.turquoise)
                D.circle(c, cx + 0.15f * r, cy - 0.3f * r, 0.1f * r, C.incaGold)
                D.circle(c, cx - 0.35f * r, cy + 0.15f * r, 0.1f * r, C.purpleAndes)
            }
            CAT -> {
                // Gatinha fofinha companheira
                // Orelhas pontiagudas
                D.poly(c, C.catFur, ol, lw, cx - 0.55f * r, cy - 0.15f * r, cx - 0.2f * r, cy - 0.65f * r, cx, cy - 0.25f * r)
                D.poly(c, C.catFur, ol, lw, cx + 0.55f * r, cy - 0.15f * r, cx + 0.2f * r, cy - 0.65f * r, cx, cy - 0.25f * r)
                D.poly(c, C.pink, 0, 0f, cx - 0.45f * r, cy - 0.2f * r, cx - 0.24f * r, cy - 0.55f * r, cx - 0.08f * r, cy - 0.26f * r)
                D.poly(c, C.pink, 0, 0f, cx + 0.45f * r, cy - 0.2f * r, cx + 0.24f * r, cy - 0.55f * r, cx + 0.08f * r, cy - 0.26f * r)
                // Cabeça redondinha
                D.circle(c, cx, cy + 0.05f * r, 0.55f * r, C.catFur, ol, lw)
                // Mancha fofa
                D.oval(c, cx - 0.25f * r, cy - 0.15f * r, 0.22f * r, 0.16f * r, C.catSpot)
                // Olhos grandes brilhantes
                D.oval(c, cx - 0.2f * r, cy + 0.02f * r, 0.08f * r, 0.11f * r, C.ink)
                D.oval(c, cx + 0.2f * r, cy + 0.02f * r, 0.08f * r, 0.11f * r, C.ink)
                D.circle(c, cx - 0.22f * r, cy - 0.02f * r, 0.035f * r, C.white)
                D.circle(c, cx + 0.18f * r, cy - 0.02f * r, 0.035f * r, C.white)
                // Narizinho rosa e boquinha :3
                D.poly(c, C.pinkD, 0, 0f, cx - 0.04f * r, cy + 0.15f * r, cx + 0.04f * r, cy + 0.15f * r, cx, cy + 0.19f * r)
                D.line(c, cx - 0.08f * r, cy + 0.25f * r, cx, cy + 0.21f * r, lw, ol)
                D.line(c, cx, cy + 0.21f * r, cx + 0.08f * r, cy + 0.25f * r, lw, ol)
                // Bigodes
                D.line(c, cx - 0.35f * r, cy + 0.15f * r, cx - 0.65f * r, cy + 0.12f * r, lw * 0.7f, ol)
                D.line(c, cx - 0.35f * r, cy + 0.22f * r, cx - 0.62f * r, cy + 0.25f * r, lw * 0.7f, ol)
                D.line(c, cx + 0.35f * r, cy + 0.15f * r, cx + 0.65f * r, cy + 0.12f * r, lw * 0.7f, ol)
                D.line(c, cx + 0.35f * r, cy + 0.22f * r, cx + 0.62f * r, cy + 0.25f * r, lw * 0.7f, ol)
            }
            WAND -> {
                // Varinha de brincar com penas coloridas
                D.line(c, cx - 0.55f * r, cy + 0.65f * r, cx + 0.15f * r, cy - 0.25f * r, lw * 1.6f, C.brown)
                D.line(c, cx + 0.15f * r, cy - 0.25f * r, cx + 0.38f * r, cy - 0.05f * r, lw * 0.8f, C.ink)
                // Peninhas fofas
                D.oval(c, cx + 0.45f * r, cy - 0.2f * r, 0.22f * r, 0.09f * r, C.pinkD, ol, lw * 0.6f)
                D.oval(c, cx + 0.52f * r, cy - 0.05f * r, 0.2f * r, 0.08f * r, C.turquoise, ol, lw * 0.6f)
                D.oval(c, cx + 0.35f * r, cy + 0.08f * r, 0.18f * r, 0.08f * r, C.incaGold, ol, lw * 0.6f)
            }
            CAR_TOY -> {
                // Carrinho de brincar de madeira pastel
                D.rrect(c, cx - 0.55f * r, cy - 0.05f * r, cx + 0.55f * r, cy + 0.35f * r, 0.1f * r, C.turquoise, ol, lw)
                D.poly(c, C.sky, ol, lw, cx - 0.35f * r, cy - 0.05f * r, cx - 0.15f * r, cy - 0.38f * r, cx + 0.25f * r, cy - 0.38f * r, cx + 0.42f * r, cy - 0.05f * r)
                // Rodas
                D.circle(c, cx - 0.32f * r, cy + 0.35f * r, 0.16f * r, C.butter, ol, lw)
                D.circle(c, cx + 0.32f * r, cy + 0.35f * r, 0.16f * r, C.butter, ol, lw)
                D.circle(c, cx - 0.32f * r, cy + 0.35f * r, 0.06f * r, C.ink)
                D.circle(c, cx + 0.32f * r, cy + 0.35f * r, 0.06f * r, C.ink)
            }
            CARROT_PLUSH -> {
                // Peluche fofo de cenourinha
                D.poly(c, C.orange, ol, lw, cx - 0.42f * r, cy - 0.25f * r, cx + 0.42f * r, cy - 0.25f * r, cx, cy + 0.75f * r)
                D.oval(c, cx, cy - 0.45f * r, 0.16f * r, 0.3f * r, C.green, ol, lw)
                // Olhinhos e sorriso bordados
                D.circle(c, cx - 0.15f * r, cy - 0.05f * r, 0.04f * r, C.ink)
                D.circle(c, cx + 0.15f * r, cy - 0.05f * r, 0.04f * r, C.ink)
                D.line(c, cx - 0.08f * r, cy + 0.12f * r, cx + 0.08f * r, cy + 0.12f * r, lw, ol)
            }
        }
    }

    /** Desenha um brilho/estrela em destaque. */
    fun drawSpark(c: Canvas, cx: Float, cy: Float, r: Float) {
        D.poly(c, C.butter, C.gold, max(1f, r * 0.08f), cx, cy - r, cx + 0.25f * r, cy - 0.25f * r, cx + r, cy, cx + 0.25f * r, cy + 0.25f * r,
            cx, cy + r, cx - 0.25f * r, cy + 0.25f * r, cx - r, cy, cx - 0.25f * r, cy - 0.25f * r)
    }

    private fun starPath(c: Canvas, cx: Float, cy: Float, r: Float, fill: Int, line: Int, lw: Float) {
        D.path.reset()
        for (i in 0 until 10) {
            val a = (-90 + i * 36) * PI / 180
            val rad = if (i % 2 == 0) r else r * 0.48f
            val x = cx + (cos(a) * rad).toFloat(); val y = cy + (sin(a) * rad).toFloat()
            if (i == 0) D.path.moveTo(x, y) else D.path.lineTo(x, y)
        }
        D.path.close()
        D.f(fill); c.drawPath(D.path, D.p)
        D.s(line, lw); c.drawPath(D.path, D.p)
    }
}
