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

/** Paleta pastel (estilo livro infantil). */
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
}

object D {
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    val path = Path()
    private val rf = RectF()
    private val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }

    fun f(color: Int) {
        p.style = Paint.Style.FILL; p.color = color; p.strokeWidth = 0f
    }

    fun s(color: Int, w: Float) {
        p.style = Paint.Style.STROKE; p.color = color; p.strokeWidth = w
        p.strokeCap = Paint.Cap.ROUND; p.strokeJoin = Paint.Join.ROUND
    }

    fun alpha(color: Int, a: Int) = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color))

    fun mix(a: Int, b: Int, t: Float): Int {
        val k = t.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(a) + (Color.red(b) - Color.red(a)) * k).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * k).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * k).toInt()
        )
    }

    fun oval(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, fill: Int, line: Int = 0, lw: Float = 0f) {
        rf.set(cx - rx, cy - ry, cx + rx, cy + ry)
        f(fill); c.drawOval(rf, p)
        if (lw > 0f) { s(line, lw); c.drawOval(rf, p) }
    }

    fun circle(c: Canvas, cx: Float, cy: Float, r: Float, fill: Int, line: Int = 0, lw: Float = 0f) =
        oval(c, cx, cy, r, r, fill, line, lw)

    fun rrect(c: Canvas, l: Float, t: Float, r: Float, b: Float, rad: Float, fill: Int, line: Int = 0, lw: Float = 0f) {
        rf.set(l, t, r, b)
        f(fill); c.drawRoundRect(rf, rad, rad, p)
        if (lw > 0f) { s(line, lw); c.drawRoundRect(rf, rad, rad, p) }
    }

    fun rrect(c: Canvas, r: RectF, rad: Float, fill: Int, line: Int = 0, lw: Float = 0f) =
        rrect(c, r.left, r.top, r.right, r.bottom, rad, fill, line, lw)

    fun line(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, w: Float, color: Int) {
        s(color, w); c.drawLine(x1, y1, x2, y2, p)
    }

    fun poly(c: Canvas, fill: Int, line: Int, lw: Float, vararg v: Float) {
        path.reset()
        path.moveTo(v[0], v[1])
        var i = 2
        while (i < v.size) { path.lineTo(v[i], v[i + 1]); i += 2 }
        path.close()
        f(fill); c.drawPath(path, p)
        if (lw > 0f) { s(line, lw); c.drawPath(path, p) }
    }

    fun text(c: Canvas, str: String, x: Float, y: Float, size: Float, color: Int, align: Paint.Align = Paint.Align.CENTER) {
        tp.textSize = size; tp.textAlign = align; tp.color = color
        c.drawText(str, x, y, tp)
    }

    fun textW(str: String, size: Float): Float {
        tp.textSize = size
        return tp.measureText(str)
    }

    /** Botão arredondado com texto. */
    fun button(c: Canvas, r: RectF, label: String, fill: Int, textColor: Int = C.ink, size: Float = r.height() * 0.36f) {
        rrect(c, r, r.height() * 0.4f, fill, C.outline, max(2f, r.height() * 0.03f))
        text(c, label, r.centerX(), r.centerY() + size * 0.35f, size, textColor)
    }
}

object Icons {
    const val CARROT = 0; const val APPLE = 1; const val FLOWER = 2; const val STAR = 3; const val HEART = 4
    const val MOON = 5; const val CLOUD = 6; const val BERRY = 7; const val CUPCAKE = 8; const val LETTUCE = 9
    const val BALL = 10; const val YARN = 11; const val BUBBLE = 12; const val HOUSE = 13; const val HOP = 14
    const val CARDS = 15; const val PUZZLE = 16; const val BAG = 17; const val HAT = 18; const val PAW = 19
    const val ROCK = 20; const val GOLDEN = 21; const val SPONGE = 22; const val SHOWER = 23; const val SPARK = 24
    const val LAMP = 25; const val COIN = 26; const val BED = 27; const val BOWL = 28

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
        }
    }

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
