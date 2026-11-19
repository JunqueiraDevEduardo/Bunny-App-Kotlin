package com.example.bunny

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

// ================================================================= SALTA (RUNNER)
class RunnerScreen(g: BunnyView) : Screen(g) {
    private class Ob(var x: Float, val w: Float, val h: Float, val kind: Int)
    private class Cn(var x: Float, val y: Float, val isGold: Boolean = false) { var got = false }

    override val minigame: Boolean get() = true
    private val pose = Pose()
    private var state = 0
    private var off = 0f
    private var vy = 0f
    private var jumpsLeft = 2 // Suporte a salto duplo!
    private val obs = ArrayList<Ob>()
    private val cns = ArrayList<Cn>()
    private var spawnT = 1.2f
    private var cnT = 2f
    private var dist = 0f
    private var carrots = 0
    private var scroll = 0f
    private var overT = 0f
    private var reward = 0

    // Mapas temáticos: 0 Campo Florido, 1 Machu Picchu (Andes), 2 Noite Estrelada, 3 Praia Tropical
    private var mapIndex = 0
    private val mapNames = arrayOf("Campo Florido", "Machu Picchu", "Noite Estrelada", "Praia Tropical")
    private val mapBtn = RectF()

    private fun reset() {
        state = 1; off = 0f; vy = 0f; jumpsLeft = 2; obs.clear(); cns.clear(); spawnT = 1.2f; cnT = 2f
        dist = 0f; carrots = 0; overT = 0f; pose.mood = 0
    }

    override fun update(dt: Float) {
        pose.t += dt
        if (state == 2) overT += dt
        if (state != 1) return

        val k = 1f + dist / 450f
        val sp = 0.52f * min(k, 2.3f)
        scroll += sp * dt
        dist += sp * dt * 22f

        vy -= 4.2f * dt
        off += vy * dt
        if (off <= 0f) {
            off = 0f; vy = 0f; jumpsLeft = 2
        }

        for (o in obs) o.x -= sp * dt
        for (cn in cns) cn.x -= sp * dt
        obs.removeAll { it.x < -0.2f }
        cns.removeAll { it.x < -0.2f }

        spawnT -= dt
        if (spawnT <= 0f) {
            obs.add(Ob(1.1f, 0.07f + Random.nextFloat() * 0.04f, 0.06f + Random.nextFloat() * 0.05f, Random.nextInt(2)))
            spawnT = (0.95f + Random.nextFloat() * 1.0f) / min(k, 1.8f)
        }

        cnT -= dt
        if (cnT <= 0f) {
            val y = 0.12f + Random.nextFloat() * 0.22f
            val gold = Random.nextFloat() < 0.25f
            for (i in 0 until 3) cns.add(Cn(1.1f + i * 0.09f, y, gold && i == 1))
            cnT = 2.0f + Random.nextFloat() * 1.4f
        }

        for (cn in cns) {
            if (!cn.got && abs(cn.x - 0.25f) < 0.07f && abs(cn.y - (off + 0.08f)) < 0.12f) {
                cn.got = true
                carrots += if (cn.isGold) 3 else 1
            }
        }

        for (o in obs) {
            if (abs(o.x - 0.25f) < o.w / 2f + 0.04f && off < o.h * 0.78f) {
                state = 2; overT = 0f; pose.mood = 2
                reward = carrots + (dist / 25f).toInt()
                g.earn(reward)
            }
        }
    }

    override fun draw(c: Canvas, r: RectF) {
        val w = r.width(); val h = r.height()
        val gy = r.top + h * 0.74f

        // Cenário baseado no mapa escolhido
        when (mapIndex) {
            1 -> {
                // Machu Picchu (Andes): céu pôr do sol incaico e montanhas
                D.f(Color.rgb(255, 175, 120)); c.drawRect(r.left, r.top, r.right, gy, D.p)
                D.circle(c, r.right - w * 0.2f, r.top + h * 0.16f, w * 0.08f, C.incaGold)
                // Montanhas andinas majestosas
                D.poly(c, Color.rgb(180, 110, 85), 0, 0f, r.left, gy, r.left + w * 0.35f, r.top + h * 0.32f, r.left + w * 0.7f, gy)
                D.poly(c, Color.rgb(145, 85, 65), 0, 0f, r.left + w * 0.4f, gy, r.left + w * 0.75f, r.top + h * 0.38f, r.right, gy)
                // Chão de pedra inca
                D.f(Color.rgb(195, 180, 160)); c.drawRect(r.left, gy, r.right, r.bottom, D.p)
                D.f(C.incaGold); c.drawRect(r.left, gy, r.right, gy + h * 0.012f, D.p)
            }
            2 -> {
                // Noite Estrelada: céu roxo escuro com estrelas e lua
                D.f(Color.rgb(35, 25, 75)); c.drawRect(r.left, r.top, r.right, gy, D.p)
                Icons.draw(c, Icons.MOON, r.right - w * 0.18f, r.top + h * 0.14f, w * 0.07f)
                for (i in 0 until 12) {
                    val sx = ((i * 0.22f - scroll * 0.05f) % 1.2f + 1.2f) % 1.2f * w
                    val sy = r.top + (0.08f + (i % 4) * 0.09f) * h
                    Icons.drawSpark(c, sx, sy, w * 0.015f)
                }
                D.f(Color.rgb(45, 55, 95)); c.drawRect(r.left, gy, r.right, r.bottom, D.p)
                D.f(C.lav); c.drawRect(r.left, gy, r.right, gy + h * 0.012f, D.p)
            }
            3 -> {
                // Praia Tropical: céu azul-turquesa, oceano e areia dourada
                D.f(Color.rgb(175, 230, 245)); c.drawRect(r.left, r.top, r.right, gy, D.p)
                D.circle(c, r.right - w * 0.18f, r.top + h * 0.14f, w * 0.07f, C.butter)
                D.f(C.turquoise); c.drawRect(r.left, gy - h * 0.12f, r.right, gy, D.p)
                D.f(C.incaGold); c.drawRect(r.left, gy, r.right, r.bottom, D.p)
                D.f(C.white); c.drawRect(r.left, gy, r.right, gy + h * 0.012f, D.p)
            }
            else -> {
                // Campo Florido clássico
                D.f(C.sky); c.drawRect(r.left, r.top, r.right, gy, D.p)
                D.circle(c, r.right - w * 0.18f, r.top + h * 0.14f, w * 0.07f, C.butter)
                for (i in 0 until 4) D.oval(c, r.left + ((i * 0.35f - scroll * 0.2f) % 1.4f + 1.4f) % 1.4f * w - w * 0.1f, gy, w * 0.28f, h * 0.1f, D.mix(C.mint, C.green, 0.3f))
                D.f(C.green); c.drawRect(r.left, gy, r.right, r.bottom, D.p)
                D.f(C.greenD); c.drawRect(r.left, gy, r.right, gy + h * 0.015f, D.p)
            }
        }

        // Obstáculos
        for (o in obs) {
            val ox = r.left + o.x * w; val oh = o.h * h
            if (mapIndex == 1) {
                // Cactos ou pedras incas
                if (o.kind == 0) Icons.draw(c, Icons.ROCK, ox, gy - oh * 0.45f, oh * 0.75f)
                else D.rrect(c, ox - oh * 0.35f, gy - oh * 0.8f, ox + oh * 0.35f, gy, 0.05f * oh, C.greenD, C.outline, 3f)
            } else if (mapIndex == 2) {
                // Cristais da noite
                D.poly(c, C.turquoise, C.outline, 2.5f, ox, gy - oh, ox + oh * 0.4f, gy, ox - oh * 0.4f, gy)
            } else {
                if (o.kind == 0) Icons.draw(c, Icons.ROCK, ox, gy - oh * 0.45f, oh * 0.75f)
                else {
                    D.circle(c, ox - oh * 0.4f, gy - oh * 0.4f, oh * 0.4f, C.greenD, C.outline, 3f)
                    D.circle(c, ox + oh * 0.4f, gy - oh * 0.4f, oh * 0.4f, C.greenD, C.outline, 3f)
                    D.circle(c, ox, gy - oh * 0.6f, oh * 0.5f, C.green, C.outline, 3f)
                }
            }
        }

        // Colecionáveis
        for (cn in cns) {
            if (!cn.got) {
                val icon = if (cn.isGold) Icons.GOLDEN else if (mapIndex == 1) Icons.CHOCLO else Icons.CARROT
                Icons.draw(c, icon, r.left + cn.x * w, gy - cn.y * h, h * 0.035f)
            }
        }

        // Coelho corredor
        val scale = Save.bunnyScale()
        BunnyArt.draw(c, r.left + 0.25f * w, gy - off * h, h * 0.13f * scale, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)

        // UI do Runner
        D.text(c, "${dist.toInt()} m", r.left + w * 0.05f, r.top + h * 0.06f, h * 0.04f, C.ink, android.graphics.Paint.Align.LEFT)
        Icons.draw(c, Icons.CARROT, r.left + w * 0.06f, r.top + h * 0.11f, h * 0.022f)
        D.text(c, "x $carrots", r.left + w * 0.1f, r.top + h * 0.12f, h * 0.035f, C.ink, android.graphics.Paint.Align.LEFT)

        // Botão para trocar de mapa
        mapBtn.set(r.right - w * 0.44f, r.top + h * 0.04f, r.right - w * 0.04f, r.top + h * 0.10f)
        D.rrect(c, mapBtn, mapBtn.height() * 0.4f, C.white, C.outline, 3f)
        D.text(c, mapNames[mapIndex], mapBtn.centerX(), mapBtn.centerY() + mapBtn.height() * 0.16f, mapBtn.height() * 0.42f, C.ink)

        if (state == 0) {
            D.text(c, "Toca para saltar! (Salto duplo permitido)", r.centerX(), r.top + h * 0.42f, h * 0.038f, C.ink)
        }
        if (state == 2) {
            overlay(c, r, "Ups!", "+$reward moedas", "Toca para correr de novo")
        }
    }

    private fun overlay(c: Canvas, r: RectF, a: String, b: String, d: String) {
        D.rrect(c, r.left + r.width() * 0.12f, r.top + r.height() * 0.28f, r.right - r.width() * 0.12f, r.top + r.height() * 0.58f, r.height() * 0.04f, D.alpha(C.white, 240), C.outline, 4f)
        D.text(c, a, r.centerX(), r.top + r.height() * 0.38f, r.height() * 0.06f, C.pinkD)
        D.text(c, b, r.centerX(), r.top + r.height() * 0.46f, r.height() * 0.04f, C.ink)
        D.text(c, d, r.centerX(), r.top + r.height() * 0.53f, r.height() * 0.028f, C.ink)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (mapBtn.contains(x, y)) {
            mapIndex = (mapIndex + 1) % mapNames.size
            reset()
            return
        }
        when (state) {
            0 -> reset()
            1 -> {
                if (off <= 0.001f) {
                    vy = 1.48f; jumpsLeft = 1
                } else if (jumpsLeft > 0) {
                    vy = 1.35f; jumpsLeft = 0 // Salto duplo!
                    burst(r.left + 0.25f * r.width(), r.top + r.height() * 0.74f - off * r.height(), Icons.SPARK, 3, r.height() * 0.02f)
                }
            }
            else -> if (overT > 0.6f) reset()
        }
    }
}

// ================================================================= MEMÓRIA
class MemoryScreen(g: BunnyView) : Screen(g) {
    override val minigame: Boolean get() = true

    // Temas: 0 Peru Andino, 1 Doces & Comida, 2 Céu & Natureza
    private var themeIndex = 0
    private val themeNames = arrayOf("Peru Andino", "Doces & Frutas", "Natureza")
    private val peruIds = intArrayOf(Icons.LLAMA, Icons.CHULLO, Icons.CEVICHE, Icons.CHICHA_MORADA, Icons.ALFAJOR, Icons.CHOCLO, Icons.PONCHO, Icons.GOLDEN, Icons.STAR, Icons.CARROT)
    private val sweetIds = intArrayOf(Icons.CUPCAKE, Icons.ALFAJOR, Icons.APPLE, Icons.BERRY, Icons.CARROT, Icons.CHICHA_MORADA, Icons.HEART, Icons.STAR)
    private val natureIds = intArrayOf(Icons.FLOWER, Icons.STAR, Icons.MOON, Icons.CLOUD, Icons.HEART, Icons.BUBBLE, Icons.HOP, Icons.PAW)

    // Dificuldades: 0 Fácil (12 cartas - 3x4), 1 Normal (16 cartas - 4x4)
    private var diffIndex = 1
    private val diffNames = arrayOf("12 Cartas", "16 Cartas")
    private val themeBtn = RectF()
    private val diffBtn = RectF()

    private var totalCards = 16
    private var cards = IntArray(16)
    private var fl = FloatArray(16)
    private var faceUp = BooleanArray(16)
    private var matched = BooleanArray(16)
    private val sel = ArrayList<Int>()
    private val rects = Array(20) { RectF() }
    private var wait = 0f
    private var moves = 0
    private var done = false
    private var reward = 0

    override fun enter() {
        totalCards = if (diffIndex == 0) 12 else 16
        val pairs = totalCards / 2
        cards = (0 until pairs).flatMap { listOf(it, it) }.shuffled().toIntArray()
        fl = FloatArray(totalCards)
        faceUp = BooleanArray(totalCards)
        matched = BooleanArray(totalCards)
        sel.clear(); wait = 0f; moves = 0; done = false
    }

    override fun update(dt: Float) {
        for (i in 0 until totalCards) {
            val target = if (faceUp[i] || matched[i]) 1f else 0f
            fl[i] += (target - fl[i]) * min(1f, dt * 12f)
        }
        if (wait > 0f) {
            wait -= dt
            if (wait <= 0f) {
                for (i in sel) if (!matched[i]) faceUp[i] = false
                sel.clear()
            }
        }
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        D.f(D.mix(C.cream, C.lav, 0.35f)); c.drawRect(r, D.p)

        val w = r.width(); val h = r.height()
        // Botões de Tema e Dificuldade
        val btnH = h * 0.055f
        themeBtn.set(r.left + w * 0.05f, r.top + h * 0.02f, r.left + w * 0.46f, r.top + h * 0.02f + btnH)
        diffBtn.set(r.right - w * 0.46f, r.top + h * 0.02f, r.right - w * 0.05f, r.top + h * 0.02f + btnH)
        D.rrect(c, themeBtn, btnH * 0.35f, C.white, C.outline, 2.5f)
        D.text(c, themeNames[themeIndex], themeBtn.centerX(), themeBtn.centerY() + btnH * 0.15f, btnH * 0.42f, C.ink)
        D.rrect(c, diffBtn, btnH * 0.35f, C.white, C.outline, 2.5f)
        D.text(c, diffNames[diffIndex], diffBtn.centerX(), diffBtn.centerY() + btnH * 0.15f, btnH * 0.42f, C.ink)

        D.text(c, "Jogadas: $moves", r.centerX(), r.top + h * 0.105f, h * 0.038f, C.ink)

        val cols = 4
        val rows = totalCards / cols
        val cs = min(r.width() * 0.21f, (r.height() * 0.65f) / rows)
        val gap = cs * 0.12f
        val gw = cols * cs + (cols - 1) * gap
        val x0 = r.centerX() - gw / 2f
        val y0 = r.top + h * 0.14f

        val activeList = when (themeIndex) {
            0 -> peruIds
            1 -> sweetIds
            else -> natureIds
        }

        for (i in 0 until totalCards) {
            val col = i % cols; val row = i / cols
            val l = x0 + col * (cs + gap); val t = y0 + row * (cs + gap)
            rects[i].set(l, t, l + cs, t + cs)
            val k = abs(1f - 2f * fl[i])
            val cx = l + cs / 2f
            val hw = cs / 2f * k

            if (fl[i] > 0.5f) {
                D.rrect(c, cx - hw, t, cx + hw, t + cs, cs * 0.15f, if (matched[i]) C.mint else C.white, C.outline, 4f)
                if (k > 0.2f) Icons.draw(c, activeList[cards[i] % activeList.size], cx, t + cs / 2f, cs * 0.30f * k)
            } else {
                val backColor = if (themeIndex == 0) C.peruRed else if (themeIndex == 1) C.pink else C.sky
                D.rrect(c, cx - hw, t, cx + hw, t + cs, cs * 0.15f, backColor, C.outline, 4f)
                if (k > 0.2f) {
                    val backIcon = if (themeIndex == 0) Icons.LLAMA else Icons.PAW
                    Icons.draw(c, backIcon, cx, t + cs / 2f, cs * 0.24f * k)
                }
            }
        }

        if (done) {
            D.rrect(c, r.left + r.width() * 0.12f, r.top + r.height() * 0.38f, r.right - r.width() * 0.12f, r.top + r.height() * 0.64f, r.height() * 0.04f, D.alpha(C.white, 245), C.outline, 4f)
            D.text(c, "Parabéns!", r.centerX(), r.top + r.height() * 0.47f, r.height() * 0.055f, C.pinkD)
            D.text(c, "+$reward moedas", r.centerX(), r.top + r.height() * 0.54f, r.height() * 0.04f, C.ink)
            D.text(c, "Toca para jogar de novo", r.centerX(), r.top + r.height() * 0.60f, r.height() * 0.028f, C.ink)
        }
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (themeBtn.contains(x, y)) {
            themeIndex = (themeIndex + 1) % themeNames.size
            enter(); return
        }
        if (diffBtn.contains(x, y)) {
            diffIndex = (diffIndex + 1) % diffNames.size
            enter(); return
        }
        if (done) { enter(); return }
        if (sel.size >= 2) return

        for (i in 0 until totalCards) {
            if (rects[i].contains(x, y) && !faceUp[i] && !matched[i]) {
                faceUp[i] = true; sel.add(i)
                if (sel.size == 2) {
                    moves++
                    if (cards[sel[0]] == cards[sel[1]]) {
                        matched[sel[0]] = true; matched[sel[1]] = true; sel.clear()
                        if (matched.all { it }) {
                            done = true
                            reward = max(10, 45 - moves * 2)
                            g.earn(reward)
                        }
                    } else {
                        wait = 0.75f
                    }
                }
                return
            }
        }
    }
}

// ================================================================= CENOURAS
class CarrotScreen(g: BunnyView) : Screen(g) {
    private class It(var x: Float, var y: Float, val kind: Int)

    override val minigame: Boolean get() = true
    private val pose = Pose()
    private val items = ArrayList<It>()
    private var state = 0
    private var bx = 0.5f
    private var tx = 0.5f
    private var timeLeft = 40f
    private var lives = 3
    private var score = 0
    private var spawn = 0.5f
    private var overT = 0f

    // Mapas: 0 Horta do Coelho, 1 Vale dos Andes, 2 Docelândia
    private var mapIndex = 0
    private val mapNames = arrayOf("Horta Verde", "Vale dos Andes", "Docelândia")
    private val mapBtn = RectF()

    private fun reset() {
        state = 1; items.clear(); timeLeft = 40f; lives = 3; score = 0; spawn = 0.4f; overT = 0f; pose.mood = 0
    }

    override fun update(dt: Float) {
        pose.t += dt
        bx += (tx - bx) * min(1f, dt * 10f)
        if (state == 2) overT += dt
        if (state != 1) return

        timeLeft -= dt
        val el = 40f - timeLeft
        spawn -= dt
        if (spawn <= 0f) {
            val q = Random.nextFloat()
            // 0: Normal (Cenoura/Choclo/Alfajor), 1: Dourado (+5), 2: Pedra (-1 vida)
            val kind = if (q < 0.12f) 1 else if (q < 0.30f) 2 else 0
            items.add(It(0.1f + Random.nextFloat() * 0.8f, -0.05f, kind))
            spawn = max(0.32f, 0.85f - el * 0.012f)
        }

        val sp = 0.36f * (1f + el / 40f)
        for (i in items) i.y += sp * dt

        val it = items.iterator()
        while (it.hasNext()) {
            val i = it.next()
            if (i.y > 0.83f && i.y < 0.94f && abs(i.x - bx) < 0.13f) {
                when (i.kind) {
                    0 -> score += 1
                    1 -> score += 5
                    else -> { lives--; g.say("Ai! Uma pedra!"); pose.mood = 2 }
                }
                it.remove()
            } else if (i.y > 1.05f) {
                it.remove()
            }
        }

        if (pose.mood == 2 && state == 1 && items.none { it.kind == 2 && it.y > 0.7f }) pose.mood = 0
        if (timeLeft <= 0f || lives <= 0) {
            state = 2; overT = 0f; pose.mood = 0; g.earn(score)
        }
    }

    override fun draw(c: Canvas, r: RectF) {
        val w = r.width(); val h = r.height()

        // Cenário baseado no mapa
        when (mapIndex) {
            1 -> {
                // Vale dos Andes
                D.f(Color.rgb(255, 190, 130)); c.drawRect(r.left, r.top, r.right, r.bottom, D.p)
                D.poly(c, Color.rgb(180, 120, 90), 0, 0f, r.left, r.top + h * 0.82f, r.left + w * 0.4f, r.top + h * 0.45f, r.right, r.top + h * 0.82f)
                D.f(Color.rgb(205, 185, 150)); c.drawRect(r.left, r.top + h * 0.82f, r.right, r.bottom, D.p)
            }
            2 -> {
                // Docelândia
                D.f(Color.rgb(255, 225, 238)); c.drawRect(r.left, r.top, r.right, r.bottom, D.p)
                D.f(C.pinkD); c.drawRect(r.left, r.top + h * 0.82f, r.right, r.bottom, D.p)
            }
            else -> {
                // Horta Verde
                D.f(C.sky); c.drawRect(r.left, r.top, r.right, r.bottom, D.p)
                D.f(C.green); c.drawRect(r.left, r.top + h * 0.82f, r.right, r.bottom, D.p)
            }
        }

        // Itens a cair
        for (i in items) {
            val ic = when {
                i.kind == 1 -> Icons.GOLDEN
                i.kind == 2 -> Icons.ROCK
                mapIndex == 1 -> Icons.CHOCLO
                mapIndex == 2 -> Icons.ALFAJOR
                else -> Icons.CARROT
            }
            Icons.draw(c, ic, r.left + i.x * w, r.top + i.y * h, h * 0.035f)
        }

        val scale = Save.bunnyScale()
        val s = h * 0.15f * scale
        val px = r.left + bx * w
        val by = r.top + h * 0.97f
        BunnyArt.draw(c, px, by, s, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)

        // Cesto à frente do coelho
        D.rrect(c, px - s * 0.32f, by - s * 0.5f, px + s * 0.32f, by - s * 0.12f, s * 0.08f, C.peach, C.outline, 3f)
        D.line(c, px - s * 0.32f, by - s * 0.38f, px + s * 0.32f, by - s * 0.38f, 3f, C.outline)
        D.line(c, px - s * 0.32f, by - s * 0.26f, px + s * 0.32f, by - s * 0.26f, 3f, C.outline)

        // HUD
        D.text(c, "Pontos: $score", r.left + w * 0.05f, r.top + h * 0.06f, h * 0.04f, C.ink, android.graphics.Paint.Align.LEFT)
        D.text(c, "${timeLeft.toInt().coerceAtLeast(0)} s", r.centerX(), r.top + h * 0.06f, h * 0.04f, C.ink)
        for (i in 0 until lives) {
            Icons.draw(c, Icons.HEART, r.right - w * 0.07f - i * w * 0.075f, r.top + h * 0.045f, h * 0.022f)
        }

        // Botão para mudar de mapa
        mapBtn.set(r.centerX() - w * 0.20f, r.top + h * 0.09f, r.centerX() + w * 0.20f, r.top + h * 0.135f)
        D.rrect(c, mapBtn, mapBtn.height() * 0.4f, C.white, C.outline, 2.5f)
        D.text(c, mapNames[mapIndex], mapBtn.centerX(), mapBtn.centerY() + mapBtn.height() * 0.18f, mapBtn.height() * 0.42f, C.ink)

        if (state == 0) {
            D.text(c, "Arrasta o Bunny para apanhar cenouras!", r.centerX(), r.top + h * 0.4f, h * 0.035f, C.ink)
        }
        if (state == 2) {
            D.rrect(c, r.left + w * 0.12f, r.top + h * 0.3f, r.right - w * 0.12f, r.top + h * 0.58f, h * 0.04f, D.alpha(C.white, 242), C.outline, 4f)
            D.text(c, "Acabou!", r.centerX(), r.top + h * 0.4f, h * 0.06f, C.pinkD)
            D.text(c, "+$score moedas", r.centerX(), r.top + h * 0.47f, h * 0.04f, C.ink)
            D.text(c, "Toca para jogar de novo", r.centerX(), r.top + h * 0.54f, h * 0.028f, C.ink)
        }
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (mapBtn.contains(x, y)) {
            mapIndex = (mapIndex + 1) % mapNames.size
            reset(); return
        }
        tx = ((x - r.left) / r.width()).coerceIn(0.08f, 0.92f)
        if (state == 0) reset() else if (state == 2 && overT > 0.6f) reset()
    }

    override fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {
        tx = ((x - r.left) / r.width()).coerceIn(0.08f, 0.92f)
    }
}

// ================================================================= PUZZLE (QUEBRA-CABEÇAS)
class PuzzleScreen(g: BunnyView) : Screen(g) {
    override val minigame: Boolean get() = true
    private var bmps = arrayOfNulls<Bitmap>(4)
    private var tiles = IntArray(9)
    private var moves = 0
    private var won = false
    private var reward = 0
    private val boardRect = RectF()
    private val shuffleBtn = RectF()
    private val imgBtn = RectF()
    private val sizeBtn = RectF()

    // Imagens: 0 Jardim Florido, 1 Machu Picchu c/ Lhama, 2 Bunny & Gatinha, 3 Quarto Estrelado
    private var imageIndex = 0
    private val imageNames = arrayOf("Jardim", "Machu Picchu", "Gatinha", "Estrelas")

    // Grade: 3 (3x3 = 9 peças) ou 4 (4x4 = 16 peças)
    private var gridSize = 3

    override fun enter() {
        if (bmps[imageIndex] == null) {
            generateBitmaps()
        }
        shuffle()
    }

    private fun generateBitmaps() {
        // Imagem 0: Jardim Florido
        val b0 = Bitmap.createBitmap(420, 420, Bitmap.Config.ARGB_8888)
        val c0 = Canvas(b0)
        D.f(C.sky); c0.drawRect(0f, 0f, 420f, 420f, D.p)
        D.f(D.mix(C.sky, C.pink, 0.5f)); c0.drawRect(0f, 200f, 420f, 420f, D.p)
        D.circle(c0, 340f, 70f, 45f, C.butter)
        Icons.draw(c0, Icons.CLOUD, 90f, 80f, 50f)
        D.oval(c0, 210f, 430f, 260f, 130f, C.green)
        BunnyArt.draw(c0, 210f, 395f, 230f, Pose(), Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        bmps[0] = b0

        // Imagem 1: Bunny no Peru em Machu Picchu com Lhama
        val b1 = Bitmap.createBitmap(420, 420, Bitmap.Config.ARGB_8888)
        val c1 = Canvas(b1)
        D.f(Color.rgb(255, 185, 130)); c1.drawRect(0f, 0f, 420f, 420f, D.p)
        D.circle(c1, 330f, 80f, 45f, C.incaGold)
        // Montanhas dos Andes
        D.poly(c1, C.greenD, 0, 0f, 0f, 420f, 150f, 160f, 320f, 420f)
        D.poly(c1, C.green, 0, 0f, 180f, 420f, 320f, 210f, 420f, 420f)
        // Bunny com poncho andino e lhama ao lado
        BunnyArt.draw(c1, 260f, 390f, 210f, Pose(), "hat_chullo", "fit_poncho", "", "shoe_andean", Save.bunnyColor)
        Icons.draw(c1, Icons.LLAMA, 110f, 330f, 85f)
        bmps[1] = b1

        // Imagem 2: Bunny e a Gatinha Fofinha a Brincar
        val b2 = Bitmap.createBitmap(420, 420, Bitmap.Config.ARGB_8888)
        val c2 = Canvas(b2)
        D.f(Color.rgb(255, 235, 245)); c2.drawRect(0f, 0f, 420f, 420f, D.p)
        D.circle(c2, 210f, 210f, 190f, C.white)
        Icons.draw(c2, Icons.HEART, 210f, 90f, 30f)
        BunnyArt.draw(c2, 275f, 390f, 220f, Pose(), Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        CatArt.draw(c2, 135f, 390f, 150f, 0f, mood = 0)
        Icons.draw(c2, Icons.YARN, 210f, 380f, 32f)
        bmps[2] = b2

        // Imagem 3: Bunny no Quarto das Estrelas
        val b3 = Bitmap.createBitmap(420, 420, Bitmap.Config.ARGB_8888)
        val c3 = Canvas(b3)
        D.f(Color.rgb(38, 30, 80)); c3.drawRect(0f, 0f, 420f, 420f, D.p)
        Icons.draw(c3, Icons.MOON, 320f, 90f, 50f)
        for (i in 0 until 8) {
            Icons.drawSpark(c3, 60f + i * 45f, 60f + (i % 3) * 40f, 16f)
        }
        D.rrect(c3, 60f, 260f, 360f, 400f, 20f, C.pink, C.outline, 4f)
        BunnyArt.draw(c3, 190f, 350f, 180f, Pose().apply { mood = 3 }, "hat_beanie", "", "", "", Save.bunnyColor)
        bmps[3] = b3
    }

    private fun shuffle() {
        val total = gridSize * gridSize
        tiles = IntArray(total) { it }
        var e = total - 1
        var last = -1
        val iters = if (gridSize == 3) 120 else 240
        for (n in 0 until iters) {
            val row = e / gridSize; val col = e % gridSize
            val opts = ArrayList<Int>()
            if (row > 0) opts.add(e - gridSize)
            if (row < gridSize - 1) opts.add(e + gridSize)
            if (col > 0) opts.add(e - 1)
            if (col < gridSize - 1) opts.add(e + 1)
            opts.remove(last)
            val nx = opts[Random.nextInt(opts.size)]
            tiles[e] = tiles[nx]; tiles[nx] = total - 1
            last = e; e = nx
        }
        moves = 0; won = false
    }

    override fun update(dt: Float) {}

    override fun draw(c: Canvas, r: RectF) {
        D.f(D.mix(C.cream, C.mint, 0.35f)); c.drawRect(r, D.p)
        val w = r.width(); val h = r.height()

        // Controles no topo: Imagem e Tamanho
        val btnH = h * 0.05f
        imgBtn.set(r.left + w * 0.05f, r.top + h * 0.02f, r.left + w * 0.48f, r.top + h * 0.02f + btnH)
        sizeBtn.set(r.right - w * 0.42f, r.top + h * 0.02f, r.right - w * 0.05f, r.top + h * 0.02f + btnH)
        D.rrect(c, imgBtn, btnH * 0.35f, C.white, C.outline, 2.5f)
        D.text(c, "Foto: ${imageNames[imageIndex]}", imgBtn.centerX(), imgBtn.centerY() + btnH * 0.16f, btnH * 0.40f, C.ink)
        D.rrect(c, sizeBtn, btnH * 0.35f, C.white, C.outline, 2.5f)
        D.text(c, "Grade: ${gridSize}x${gridSize}", sizeBtn.centerX(), sizeBtn.centerY() + btnH * 0.16f, btnH * 0.40f, C.ink)

        val bs = min(r.width() * 0.88f, r.height() * 0.62f)
        boardRect.set(r.centerX() - bs / 2f, r.top + r.height() * 0.14f, r.centerX() + bs / 2f, r.top + r.height() * 0.14f + bs)
        val ts = bs / gridSize
        D.text(c, "Puzzle · Jogadas: $moves", r.centerX(), r.top + r.height() * 0.10f, r.height() * 0.038f, C.ink)

        if (bmps[imageIndex] == null) generateBitmaps()
        val b = bmps[imageIndex] ?: return
        val total = gridSize * gridSize
        val srcStep = 420 / gridSize

        for (k in 0 until total) {
            val p = tiles[k]
            val dst = RectF(boardRect.left + (k % gridSize) * ts, boardRect.top + (k / gridSize) * ts, boardRect.left + (k % gridSize + 1) * ts, boardRect.top + (k / gridSize + 1) * ts)
            if (p == total - 1 && !won) {
                D.rrect(c, dst.left + 3f, dst.top + 3f, dst.right - 3f, dst.bottom - 3f, ts * 0.08f, D.alpha(C.outline, 85))
                continue
            }
            val src = Rect((p % gridSize) * srcStep, (p / gridSize) * srcStep, (p % gridSize + 1) * srcStep, (p / gridSize + 1) * srcStep)
            c.drawBitmap(b, src, dst, null)
            D.s(C.white, 4f); c.drawRect(dst, D.p)
        }

        shuffleBtn.set(r.centerX() - r.width() * 0.28f, boardRect.bottom + r.height() * 0.03f, r.centerX() + r.width() * 0.28f, boardRect.bottom + r.height() * 0.03f + r.height() * 0.075f)
        D.button(c, shuffleBtn, if (won) "Jogar de novo" else "Baralhar", C.pink)
        if (won) D.text(c, "Puzzle Completo! +$reward moedas", r.centerX(), boardRect.bottom + r.height() * 0.02f, r.height() * 0.032f, C.pinkD)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (imgBtn.contains(x, y)) {
            imageIndex = (imageIndex + 1) % imageNames.size
            if (bmps[imageIndex] == null) generateBitmaps()
            shuffle(); return
        }
        if (sizeBtn.contains(x, y)) {
            gridSize = if (gridSize == 3) 4 else 3
            shuffle(); return
        }
        if (shuffleBtn.contains(x, y)) {
            shuffle(); return
        }
        if (won || !boardRect.contains(x, y)) return

        val ts = boardRect.width() / gridSize
        val col = ((x - boardRect.left) / ts).toInt().coerceIn(0, gridSize - 1)
        val row = ((y - boardRect.top) / ts).toInt().coerceIn(0, gridSize - 1)
        val k = row * gridSize + col
        val total = gridSize * gridSize
        val emptyPos = tiles.indexOf(total - 1)

        if (abs(emptyPos / gridSize - row) + abs(emptyPos % gridSize - col) == 1) {
            tiles[emptyPos] = tiles[k]
            tiles[k] = total - 1
            moves++
            if ((0 until total).all { tiles[it] == it }) {
                won = true
                reward = max(12, 60 - moves / 2)
                g.earn(reward)
            }
        }
    }
}
