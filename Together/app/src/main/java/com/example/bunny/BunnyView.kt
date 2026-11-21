package com.example.bunny

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

object Nav {
    const val AUTH = -1
    const val SETTINGS = -2
    const val REPORT = -3
    const val MULTIPLAYER = -4
    const val CRAFT = -5
    const val KART = -6
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
    open val fullBleed: Boolean get() = false
    open fun enter() {}
    open fun leave() {}
    abstract fun update(dt: Float)
    abstract fun draw(c: Canvas, r: RectF)
    open fun down(x: Float, y: Float, r: RectF) {}
    open fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {}
    open fun up(x: Float, y: Float, r: RectF) {}
    open fun onGamepadKey(event: KeyEvent): Boolean = false
    open fun onGamepadMotion(event: MotionEvent): Boolean = false

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
            if (f.icon < 0) {
                val zCol = if (Theme.isBunny) Theme.bBlueIce else C.lav
                D.text(c, "Z", f.x, f.y, f.size * (0.8f + 0.6f * (1f - k)), D.alpha(zCol, (k * 255).toInt()))
            } else {
                Icons.draw(c, f.icon, f.x, f.y, f.size * (0.5f + 0.5f * k))
            }
        }
    }
}

class BunnyView(ctx: Context) : View(ctx) {
    val top = RectF(); val strip = RectF(); val bottom = RectF(); val content = RectF(); val full = RectF()
    private lateinit var screen: Screen
    var current = Nav.AUTH
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

    init {
        go(Nav.AUTH)
    }

    fun go(n: Int) {
        if (this::screen.isInitialized) screen.leave()
        current = n

        screen = when (n) {
            Nav.AUTH -> AuthScreen(this)
            Nav.SETTINGS -> SettingsScreen(this)
            Nav.REPORT -> ReportScreen(this)
            Nav.MULTIPLAYER -> MultiplayerScreen(this)
            Nav.CRAFT -> CatBunnyCraftScreen(this)
            Nav.KART -> KartScreen(this)
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

    fun say(msg: String) { toast = msg; toastT = 2.4f }

    fun earn(n: Int) {
        if (n <= 0) return
        Save.coins += n
        SoundFx.playCoin()
        say("+$n moedas!")
    }

    fun onGamepadKey(event: KeyEvent): Boolean {
        return if (this::screen.isInitialized) screen.onGamepadKey(event) else false
    }

    fun onGamepadMotion(event: MotionEvent): Boolean {
        return if (this::screen.isInitialized) screen.onGamepadMotion(event) else false
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        val wf = w.toFloat(); val hf = h.toFloat()
        full.set(0f, 0f, wf, hf)
        top.set(0f, 0f, wf, hf * 0.125f)
        strip.set(0f, top.bottom, wf, top.bottom + hf * 0.070f)
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
            if (active) {
                val actFill = if (Theme.isBunny) Theme.bWineLight else Theme.cWhitePure
                val actLine = Theme.outline
                D.rrect(c, cx - bw * 0.42f, cy - band.height() * 0.30f, cx + bw * 0.42f, cy + band.height() * 0.46f, band.height() * 0.2f, actFill, actLine, 3f)
            }
            Icons.draw(c, icons[i], cx, cy - band.height() * 0.03f, rad)
            val txtColor = if (Theme.isBunny) Theme.bSilver else Theme.text
            D.text(c, names[i], cx, cy + band.height() * 0.38f, band.height() * 0.12f, txtColor)
        }
    }

    /** Faixa de estado com Nível (estilo Pou), Necessidades e Moedas (Apenas na Loja). */
    private fun drawStrip(c: Canvas) {
        val h = strip.height()
        val lvlW = strip.width() * 0.22f
        val showCoins = current == Nav.SHOP
        val coinW = if (showCoins) strip.width() * 0.22f else 0f

        val pillFill = if (Theme.isBunny) Theme.bNavyLight else Theme.cWhitePure
        val pillOutline = Theme.outline
        val textColor = if (Theme.isBunny) Theme.bSilver else Theme.text

        // 1. Pílula de Nível (Estilo Pou com anel de progresso do dia)
        val lvlPill = RectF(strip.left + strip.width() * 0.02f, strip.top + h * 0.10f, strip.left + strip.width() * 0.02f + lvlW, strip.bottom - h * 0.10f)
        D.rrect(c, lvlPill, lvlPill.height() * 0.5f, pillFill, pillOutline, 3f)
        // Círculo com progresso do dia (24h para subir 1 nível)
        val lcx = lvlPill.left + lvlPill.height() * 0.5f
        val lcy = lvlPill.centerY()
        val lr = lvlPill.height() * 0.38f
        val circFill = if (Theme.isBunny) Theme.bBlueRoyal else C.butter
        val circLine = if (Theme.isBunny) Theme.bWine else C.gold
        D.circle(c, lcx, lcy, lr, circFill, circLine, 2f)
        val dProg = Save.dayProgress()
        if (dProg > 0.05f) {
            val progColor = if (Theme.isBunny) Theme.bBlueSky else C.greenD
            D.s(progColor, 3.5f)
            D.path.reset()
            D.path.addArc(lcx - lr, lcy - lr, lcx + lr, lcy + lr, -90f, 360f * dProg)
            c.drawPath(D.path, D.p)
        }
        Icons.draw(c, Icons.STAR, lcx, lcy, lr * 0.48f)
        D.text(c, "Nv.${Save.level()}", lvlPill.centerX() + lvlPill.height() * 0.28f, lvlPill.centerY() + lvlPill.height() * 0.15f, lvlPill.height() * 0.32f, textColor)

        // 2. Barras de Necessidades no Centro (se não estiver num minigame)
        if (!screen.minigame) {
            val vals = floatArrayOf(Save.hunger, Save.hygiene, Save.energy, Save.joy)
            val icons = intArrayOf(Icons.CARROT, Icons.BUBBLE, Icons.MOON, Icons.HEART)
            val cols = if (Theme.isBunny) {
                intArrayOf(Theme.bWine, Theme.bBlueSky, Theme.bPurple, Theme.bWineLight)
            } else {
                intArrayOf(Theme.cPink, C.peach, Theme.cPinkDark, Theme.cPinkSoft)
            }
            val areaLeft = lvlPill.right + strip.width() * 0.015f
            val areaRight = strip.right - coinW - strip.width() * 0.025f
            val areaW = areaRight - areaLeft
            val bw = areaW / 4f
            for (i in 0 until 4) {
                val x = areaLeft + i * bw
                Icons.draw(c, icons[i], x + h * 0.22f, strip.centerY(), h * 0.22f)
                val bx = x + h * 0.50f
                val bwid = bw - h * 0.58f
                D.rrect(c, bx, strip.centerY() - h * 0.11f, bx + bwid, strip.centerY() + h * 0.11f, h * 0.11f, D.alpha(pillOutline, 120))
                val fill = bwid * (vals[i] / 100f).coerceIn(0f, 1f)
                if (fill > h * 0.08f) {
                    val lowCol = if (Theme.isBunny) Theme.bWineDark else Theme.cPinkDark
                    D.rrect(c, bx, strip.centerY() - h * 0.11f, bx + fill, strip.centerY() + h * 0.11f, h * 0.11f, if (vals[i] < 25f) lowCol else cols[i])
                }
            }
        } else {
            // Em minigames mostra o nome da fase
            val stageName = if (Theme.isBunny) "Bunny ${Save.stage()}" else "Gatinha ${Save.stage()}"
            D.text(c, stageName, strip.centerX(), strip.centerY() + h * 0.14f, h * 0.26f, textColor)
        }

        // 3. Pílula de Moedas à direita (Visível exclusivamente na Loja a pedido do utilizador!)
        if (showCoins) {
            val pill = RectF(strip.right - coinW - strip.width() * 0.02f, strip.top + h * 0.10f, strip.right - strip.width() * 0.02f, strip.bottom - h * 0.10f)
            val coinFill = if (Theme.isBunny) Theme.bNavyLight else C.butter
            val coinOutline = if (Theme.isBunny) Theme.bWine else C.gold
            D.rrect(c, pill, pill.height() / 2f, coinFill, coinOutline, 3f)
            Icons.draw(c, Icons.COIN, pill.left + pill.height() * 0.55f, pill.centerY(), pill.height() * 0.34f)
            D.text(c, Save.coins.toString(), pill.centerX() + pill.height() * 0.25f, pill.centerY() + pill.height() * 0.14f, pill.height() * 0.35f, textColor)
        }
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

        // Se há presentes pendentes recebidos da nuvem/código
        if (Save.pendingGiftReceived.isNotEmpty()) {
            val gift = Save.pendingGiftReceived
            Save.pendingGiftReceived = ""
            val msg = when (gift) {
                "kiss" -> { SoundFx.playKiss(); "Recebeste um beijinho doce do teu amor! ♥" }
                "food" -> { SoundFx.playEat(Theme.isCat); "Recebeste comidinha com muito amor! 🥟" }
                "letter" -> { SoundFx.playPlay(Theme.isCat); "Recebeste uma linda cartinha de amor! 💌" }
                else -> { SoundFx.playPlay(Theme.isCat); "Recebeste um carinho à distância! ✨" }
            }
            say(msg)
        }

        val isFull = current == Nav.AUTH || current == Nav.SETTINGS || current == Nav.REPORT || current == Nav.CRAFT || current == Nav.KART

        D.f(Theme.bg); c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), D.p)

        c.save()
        val activeTarget = if (isFull) full else content
        c.clipRect(activeTarget)
        screen.draw(c, activeTarget)
        c.restore()

        // Faixas de navegação e estado (apenas quando em jogo)
        if (!isFull) {
            D.f(Theme.stripBg); c.drawRect(strip, D.p)
            drawStrip(c)
            D.rrect(c, top.left, top.top - top.height(), top.right, top.bottom, top.height() * 0.28f, Theme.bannerTop, Theme.outline, 3f)
            bannerButtons(c, top, topIcons, topNames, topNav, true)
            val botLabels = arrayOf(I18n.t("home"), I18n.t("eat"), I18n.t("bath"), I18n.t("sleep"), I18n.t("play"))
            D.rrect(c, bottom.left, bottom.top, bottom.right, bottom.bottom + bottom.height(), bottom.height() * 0.28f, Theme.bannerBottom, Theme.outline, 3f)
            bannerButtons(c, bottom, botIcons, botLabels, botNav, false)
        }

        if (toastT > 0f) {
            toastT -= dt
            val a = min(1f, toastT * 2f)
            val tSize = height * 0.018f
            val tw = D.textW(toast, tSize) + width * 0.08f
            val ty = if (isFull) height * 0.10f else content.top + content.height() * 0.08f
            val toastBg = if (Theme.isBunny) Theme.bNavyLight else Theme.cWhitePure
            val toastTxt = if (Theme.isBunny) Theme.bSilver else Theme.text
            D.rrect(c, width / 2f - tw / 2f, ty - height * 0.022f, width / 2f + tw / 2f, ty + height * 0.018f, height * 0.020f, D.alpha(toastBg, (a * 245).toInt()), D.alpha(Theme.outline, (a * 255).toInt()), 2.5f)
            D.text(c, toast, width / 2f, ty + height * 0.005f, tSize, D.alpha(toastTxt, (a * 255).toInt()))
        }
        postInvalidateOnAnimation()
    }

    private fun bannerHit(x: Float, y: Float): Boolean {
        if (current == Nav.AUTH || current == Nav.SETTINGS || current == Nav.REPORT || current == Nav.CRAFT || current == Nav.KART) return false

        if (y < top.bottom) {
            val i = (x / (top.width() / 4f)).toInt().coerceIn(0, 3)
            go(topNav[i]); return true
        }
        if (y >= strip.top && y <= strip.bottom) {
            val lvlW = strip.width() * 0.25f
            if (x <= strip.left + lvlW) {
                val petName = Save.currentPetName()
                say("$petName (${Save.stage()}) · Nível ${Save.level()}/100 · Faltam ${Save.nextLevelHours()}h para subir!")
                return true
            }
            val coinW = if (current == Nav.SHOP) strip.width() * 0.25f else 0f
            if (current == Nav.SHOP && x >= strip.right - coinW) {
                say("Tens ${Save.coins} moedas! Ganha mais nos mini-jogos!")
                return true
            }
        }
        if (y > bottom.top) {
            val i = (x / (bottom.width() / 5f)).toInt().coerceIn(0, 4)
            go(botNav[i]); return true
        }
        return false
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x; val y = e.y
        val target = if (current == Nav.AUTH || current == Nav.SETTINGS || current == Nav.REPORT || current == Nav.CRAFT) full else content
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x; lastY = y
                inBanner = bannerHit(x, y)
                if (!inBanner) screen.down(x, y, target)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!inBanner) screen.move(x, y, x - lastX, y - lastY, target)
                lastX = x; lastY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!inBanner) screen.up(x, y, target)
                inBanner = false
            }
        }
        return true
    }
}
