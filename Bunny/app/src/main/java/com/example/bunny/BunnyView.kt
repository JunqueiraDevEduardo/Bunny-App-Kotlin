package com.example.bunny

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

object Nav {
    const val HOME = 0; const val EAT = 1; const val BATH = 2; const val SLEEP = 3; const val PLAY = 4
    const val RUNNER = 5; const val MEMORY = 6; const val CARROTS = 7; const val PUZZLE = 8
    const val SHOP = 9; const val WARDROBE = 10; const val DECOR = 11
}

class Fx(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val icon: Int, val size: Float) {
    val max = life
}

abstract class Screen(val g: BunnyView) {
    protected val fx = ArrayList<Fx>()
    open val minigame: Boolean get() = false
    open fun enter() {}
    open fun leave() {}
    abstract fun update(dt: Float)
    abstract fun draw(c: Canvas, r: RectF)
    open fun down(x: Float, y: Float, r: RectF) {}
    open fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {}
    open fun up(x: Float, y: Float, r: RectF) {}

    fun burst(x: Float, y: Float, icon: Int, n: Int, size: Float) {
        for (i in 0 until n) {
            val a = Math.random() * 6.283
            val sp = (0.4 + Math.random() * 0.8).toFloat() * size * 5f
            fx.add(Fx(x, y, (Math.cos(a) * sp).toFloat(), (Math.sin(a) * sp).toFloat() - size * 3f, 0.9f, icon, size))
        }
    }

    protected fun updFx(dt: Float) {
        for (f in fx) { f.x += f.vx * dt; f.y += f.vy * dt; f.vy += f.size * 4f * dt; f.life -= dt }
        fx.removeAll { it.life <= 0f }
    }

    protected fun drawFx(c: Canvas) {
        for (f in fx) {
            val k = (f.life / f.max).coerceIn(0f, 1f)
            if (f.icon < 0) D.text(c, "Z", f.x, f.y, f.size * (0.8f + 0.6f * (1f - k)), D.alpha(C.lav, (k * 255).toInt()))
            else Icons.draw(c, f.icon, f.x, f.y, f.size * (0.5f + 0.5f * k))
        }
    }
}

class BunnyView(ctx: Context) : View(ctx) {
    val top = RectF(); val strip = RectF(); val bottom = RectF(); val content = RectF()
    private lateinit var screen: Screen
    var current = Nav.HOME
    var time = 0f
    var night = 0f
    private var nightT = 99f
    private var last = System.nanoTime()
    private var toast = ""
    private var toastT = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var inBanner = false
    private val topNav = intArrayOf(Nav.RUNNER, Nav.MEMORY, Nav.CARROTS, Nav.PUZZLE)
    private val topIcons = intArrayOf(Icons.HOP, Icons.CARDS, Icons.CARROT, Icons.PUZZLE)
    private val topNames = arrayOf("Salta", "Memória", "Cenouras", "Puzzle")
    private val botNav = intArrayOf(Nav.HOME, Nav.EAT, Nav.BATH, Nav.SLEEP, Nav.PLAY)
    private val botIcons = intArrayOf(Icons.HOUSE, Icons.APPLE, Icons.BUBBLE, Icons.MOON, Icons.BALL)
    private val botNames = arrayOf("Casa", "Comer", "Banho", "Dormir", "Brincar")

    init {
        go(Nav.HOME)
    }

    fun go(n: Int) {
        if (this::screen.isInitialized) screen.leave()
        current = n
        screen = when (n) {
            Nav.EAT -> EatScreen(this)
            Nav.BATH -> BathScreen(this)
            Nav.SLEEP -> SleepScreen(this)
            Nav.PLAY -> PlayScreen(this)
            Nav.RUNNER -> RunnerScreen(this)
            Nav.MEMORY -> MemoryScreen(this)
            Nav.CARROTS -> CarrotScreen(this)
            Nav.PUZZLE -> PuzzleScreen(this)
            Nav.SHOP -> ShopScreen(this)
            Nav.WARDROBE -> WardrobeScreen(this)
            Nav.DECOR -> DecorScreen(this)
            else -> HomeScreen(this)
        }
        screen.enter()
    }

    fun say(msg: String) { toast = msg; toastT = 2.2f }

    fun earn(n: Int) {
        if (n <= 0) return
        Save.coins += n
        say("+$n moedas")
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        val wf = w.toFloat(); val hf = h.toFloat()
        top.set(0f, 0f, wf, hf * 0.125f)
        strip.set(0f, top.bottom, wf, top.bottom + hf * 0.065f)
        bottom.set(0f, hf * 0.875f, wf, hf)
        content.set(0f, strip.bottom, wf, bottom.top)
    }

    private fun bannerButtons(c: Canvas, band: RectF, icons: IntArray, names: Array<String>, nav: IntArray, isTop: Boolean) {
        val n = icons.size
        val bw = band.width() / n
        val cy = if (isTop) band.top + band.height() * 0.50f else band.top + band.height() * 0.42f
        for (i in 0 until n) {
            val cx = band.left + (i + 0.5f) * bw
            val active = current == nav[i]
            val rad = band.height() * 0.22f
            if (active) D.rrect(c, cx - bw * 0.42f, cy - band.height() * 0.30f, cx + bw * 0.42f, cy + band.height() * 0.46f, band.height() * 0.2f, C.white, C.outline, 3f)
            Icons.draw(c, icons[i], cx, cy - band.height() * 0.03f, rad)
            D.text(c, names[i], cx, cy + band.height() * 0.38f, band.height() * 0.15f, C.ink)
        }
    }

    private fun drawStrip(c: Canvas) {
        val h = strip.height()
        val coinW = strip.width() * 0.26f
        if (!screen.minigame) {
            val vals = floatArrayOf(Save.hunger, Save.hygiene, Save.energy, Save.joy)
            val icons = intArrayOf(Icons.CARROT, Icons.BUBBLE, Icons.MOON, Icons.HEART)
            val cols = intArrayOf(C.orange, C.sky, C.lav, C.pinkD)
            val areaW = strip.width() - coinW - strip.width() * 0.04f
            val bw = areaW / 4f
            for (i in 0 until 4) {
                val x = strip.left + strip.width() * 0.02f + i * bw
                Icons.draw(c, icons[i], x + h * 0.32f, strip.centerY(), h * 0.26f)
                val bx = x + h * 0.68f
                val bwid = bw - h * 0.85f
                D.rrect(c, bx, strip.centerY() - h * 0.11f, bx + bwid, strip.centerY() + h * 0.11f, h * 0.11f, D.alpha(C.outline, 120))
                val fill = bwid * (vals[i] / 100f).coerceIn(0f, 1f)
                if (fill > h * 0.1f) D.rrect(c, bx, strip.centerY() - h * 0.11f, bx + fill, strip.centerY() + h * 0.11f, h * 0.11f, if (vals[i] < 25f) C.red else cols[i])
            }
        }
        val pill = RectF(strip.right - coinW - strip.width() * 0.02f, strip.top + h * 0.12f, strip.right - strip.width() * 0.02f, strip.bottom - h * 0.12f)
        D.rrect(c, pill, pill.height() / 2f, C.butter, C.gold, 3f)
        Icons.draw(c, Icons.COIN, pill.left + pill.height() * 0.55f, pill.centerY(), pill.height() * 0.34f)
        D.text(c, Save.coins.toString(), pill.centerX() + pill.height() * 0.3f, pill.centerY() + pill.height() * 0.17f, pill.height() * 0.5f, C.ink)
    }

    override fun onDraw(c: Canvas) {
        val now = System.nanoTime()
        val dt = min(0.05f, (now - last) / 1e9f)
        last = now
        time += dt
        nightT += dt
        if (nightT > 5f) { nightT = 0f; night = Save.nightFactor() }
        Save.tick(dt)
        screen.update(dt)

        D.f(C.cream); c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), D.p)
        c.save()
        c.clipRect(content)
        screen.draw(c, content)
        c.restore()
        // faixas
        D.f(D.alpha(C.cream, 255)); c.drawRect(strip, D.p)
        drawStrip(c)
        D.rrect(c, top.left, top.top - top.height(), top.right, top.bottom, top.height() * 0.28f, C.lav, C.outline, 3f)
        bannerButtons(c, top, topIcons, topNames, topNav, true)
        D.rrect(c, bottom.left, bottom.top, bottom.right, bottom.bottom + bottom.height(), bottom.height() * 0.28f, C.peach, C.outline, 3f)
        bannerButtons(c, bottom, botIcons, botNames, botNav, false)

        if (toastT > 0f) {
            toastT -= dt
            val a = min(1f, toastT * 2f)
            val tw = D.textW(toast, height * 0.026f) + width * 0.1f
            val ty = content.top + content.height() * 0.1f
            D.rrect(c, width / 2f - tw / 2f, ty - height * 0.03f, width / 2f + tw / 2f, ty + height * 0.02f, height * 0.025f, D.alpha(C.white, (a * 240).toInt()), D.alpha(C.outline, (a * 255).toInt()), 3f)
            D.text(c, toast, width / 2f, ty + height * 0.008f, height * 0.026f, D.alpha(C.ink, (a * 255).toInt()))
        }
        postInvalidateOnAnimation()
    }

    private fun bannerHit(x: Float, y: Float): Boolean {
        if (y < top.bottom) {
            val i = (x / (top.width() / 4f)).toInt().coerceIn(0, 3)
            go(topNav[i]); return true
        }
        if (y > bottom.top) {
            val i = (x / (bottom.width() / 5f)).toInt().coerceIn(0, 4)
            go(botNav[i]); return true
        }
        return false
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x; val y = e.y
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x; lastY = y
                inBanner = bannerHit(x, y)
                if (!inBanner) screen.down(x, y, content)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!inBanner) screen.move(x, y, x - lastX, y - lastY, content)
                lastX = x; lastY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!inBanner) screen.up(x, y, content)
                inBanner = false
            }
        }
        return true
    }
}
