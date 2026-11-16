package com.example.bunny

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private fun skyBackground(c: Canvas, r: RectF, t: Float) {
    D.f(C.sky); c.drawRect(r.left, r.top, r.right, r.top + r.height() * 0.55f, D.p)
    D.f(D.mix(C.sky, C.pink, 0.5f)); c.drawRect(r.left, r.top + r.height() * 0.55f, r.right, r.top + r.height() * 0.8f, D.p)
    D.circle(c, r.right - r.width() * 0.18f, r.top + r.height() * 0.14f, r.width() * 0.07f, C.butter)
    for (i in 0 until 3) {
        val x = ((i * 0.4f + 0.1f - t * 0.01f * (i + 1)) % 1.3f + 1.3f) % 1.3f - 0.15f
        Icons.draw(c, Icons.CLOUD, r.left + x * r.width(), r.top + r.height() * (0.12f + 0.1f * i), r.width() * 0.08f)
    }
}

// ================================================================= SALTA
class RunnerScreen(g: BunnyView) : Screen(g) {
    private class Ob(var x: Float, val w: Float, val h: Float, val kind: Int)
    private class Cn(var x: Float, val y: Float) { var got = false }

    override val minigame: Boolean get() = true
    private val pose = Pose()
    private var state = 0
    private var off = 0f
    private var vy = 0f
    private val obs = ArrayList<Ob>()
    private val cns = ArrayList<Cn>()
    private var spawnT = 1.2f
    private var cnT = 2f
    private var dist = 0f
    private var carrots = 0
    private var scroll = 0f
    private var overT = 0f
    private var reward = 0

    private fun reset() {
        state = 1; off = 0f; vy = 0f; obs.clear(); cns.clear(); spawnT = 1.2f; cnT = 2f
        dist = 0f; carrots = 0; overT = 0f; pose.mood = 0
    }

    override fun update(dt: Float) {
        pose.t += dt
        if (state == 2) overT += dt
        if (state != 1) return
        val k = 1f + dist / 500f
        val sp = 0.5f * min(k, 2.2f)
        scroll += sp * dt
        dist += sp * dt * 20f
        vy -= 4.1f * dt
        off += vy * dt
        if (off <= 0f) { off = 0f; vy = 0f }
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
            val y = 0.12f + Random.nextFloat() * 0.2f
            for (i in 0 until 3) cns.add(Cn(1.1f + i * 0.09f, y))
            cnT = 2.2f + Random.nextFloat() * 1.5f
        }
        for (cn in cns) if (!cn.got && abs(cn.x - 0.25f) < 0.07f && abs(cn.y - (off + 0.08f)) < 0.1f) { cn.got = true; carrots++ }
        for (o in obs) if (abs(o.x - 0.25f) < o.w / 2f + 0.04f && off < o.h * 0.8f) {
            state = 2; overT = 0f; pose.mood = 2
            reward = carrots + (dist / 30f).toInt()
            g.earn(reward)
        }
    }

    override fun draw(c: Canvas, r: RectF) {
        skyBackground(c, r, g.time)
        val w = r.width(); val h = r.height()
        val gy = r.top + h * 0.74f
        for (i in 0 until 4) D.oval(c, r.left + ((i * 0.35f - scroll * 0.2f) % 1.4f + 1.4f) % 1.4f * w - w * 0.1f, gy, w * 0.28f, h * 0.1f, D.mix(C.mint, C.green, 0.3f))
        D.f(C.green); c.drawRect(r.left, gy, r.right, r.bottom, D.p)
        D.f(C.greenD); c.drawRect(r.left, gy, r.right, gy + h * 0.015f, D.p)
        for (i in 0 until 9) {
            val x = ((i * 0.13f - scroll) % 1.2f + 1.2f) % 1.2f * w - w * 0.1f
            D.line(c, x, gy + h * 0.08f, x + w * 0.04f, gy + h * 0.08f, 6f, C.greenD)
        }
        for (o in obs) {
            val ox = r.left + o.x * w; val oh = o.h * h
            if (o.kind == 0) Icons.draw(c, Icons.ROCK, ox, gy - oh * 0.45f, oh * 0.75f)
            else {
                D.circle(c, ox - oh * 0.4f, gy - oh * 0.4f, oh * 0.4f, C.greenD, C.outline, 3f)
                D.circle(c, ox + oh * 0.4f, gy - oh * 0.4f, oh * 0.4f, C.greenD, C.outline, 3f)
                D.circle(c, ox, gy - oh * 0.6f, oh * 0.5f, C.green, C.outline, 3f)
            }
        }
        for (cn in cns) if (!cn.got) Icons.draw(c, Icons.CARROT, r.left + cn.x * w, gy - cn.y * h, h * 0.035f)
        pose.hop = 0f
        BunnyArt.draw(c, r.left + 0.25f * w, gy - off * h, h * 0.13f, pose, Save.hat, Save.outfit, Save.acc)
        D.text(c, "${dist.toInt()} m", r.left + w * 0.05f, r.top + h * 0.06f, h * 0.04f, C.ink, android.graphics.Paint.Align.LEFT)
        Icons.draw(c, Icons.CARROT, r.left + w * 0.06f, r.top + h * 0.11f, h * 0.022f)
        D.text(c, "x $carrots", r.left + w * 0.1f, r.top + h * 0.12f, h * 0.035f, C.ink, android.graphics.Paint.Align.LEFT)
        if (state == 0) D.text(c, "Toca para saltar!", r.centerX(), r.top + h * 0.4f, h * 0.045f, C.ink)
        if (state == 2) overlay(c, r, "Ups!", "+$reward moedas", "Toca para jogar de novo")
    }

    private fun overlay(c: Canvas, r: RectF, a: String, b: String, d: String) {
        D.rrect(c, r.left + r.width() * 0.12f, r.top + r.height() * 0.28f, r.right - r.width() * 0.12f, r.top + r.height() * 0.58f, r.height() * 0.04f, D.alpha(C.white, 240), C.outline, 4f)
        D.text(c, a, r.centerX(), r.top + r.height() * 0.38f, r.height() * 0.06f, C.pinkD)
        D.text(c, b, r.centerX(), r.top + r.height() * 0.46f, r.height() * 0.04f, C.ink)
        D.text(c, d, r.centerX(), r.top + r.height() * 0.53f, r.height() * 0.028f, C.ink)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        when (state) {
            0 -> { reset() }
            1 -> if (off <= 0.001f) vy = 1.45f
            else -> if (overT > 0.7f) reset()
        }
    }
}

// ================================================================= MEMÓRIA
class MemoryScreen(g: BunnyView) : Screen(g) {
    override val minigame: Boolean get() = true
    private val ids = intArrayOf(Icons.CARROT, Icons.APPLE, Icons.FLOWER, Icons.STAR, Icons.HEART, Icons.MOON, Icons.CLOUD, Icons.BERRY)
    private var cards = IntArray(16)
    private val fl = FloatArray(16)
    private val faceUp = BooleanArray(16)
    private val matched = BooleanArray(16)
    private val sel = ArrayList<Int>()
    private val rects = Array(16) { RectF() }
    private var wait = 0f
    private var moves = 0
    private var done = false
    private var reward = 0

    override fun enter() {
        cards = (0 until 8).flatMap { listOf(it, it) }.shuffled().toIntArray()
        for (i in 0 until 16) { fl[i] = 0f; faceUp[i] = false; matched[i] = false }
        sel.clear(); wait = 0f; moves = 0; done = false
    }

    override fun update(dt: Float) {
        for (i in 0 until 16) {
            val target = if (faceUp[i] || matched[i]) 1f else 0f
            fl[i] += (target - fl[i]) * min(1f, dt * 12f)
        }
        if (wait > 0f) {
            wait -= dt
            if (wait <= 0f) { for (i in sel) if (!matched[i]) faceUp[i] = false; sel.clear() }
        }
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        D.f(D.mix(C.cream, C.lav, 0.35f)); c.drawRect(r, D.p)
        val cs = min(r.width() * 0.21f, r.height() * 0.19f)
        val gap = cs * 0.12f
        val gw = 4 * cs + 3 * gap
        val x0 = r.centerX() - gw / 2f
        val y0 = r.top + r.height() * 0.12f
        D.text(c, "Jogadas: $moves", r.centerX(), r.top + r.height() * 0.07f, r.height() * 0.04f, C.ink)
        for (i in 0 until 16) {
            val col = i % 4; val row = i / 4
            val l = x0 + col * (cs + gap); val t = y0 + row * (cs + gap)
            rects[i].set(l, t, l + cs, t + cs)
            val k = abs(1f - 2f * fl[i])
            val cx = l + cs / 2f
            val hw = cs / 2f * k
            if (fl[i] > 0.5f) {
                D.rrect(c, cx - hw, t, cx + hw, t + cs, cs * 0.15f, if (matched[i]) C.mint else C.white, C.outline, 4f)
                if (k > 0.2f) Icons.draw(c, ids[cards[i]], cx, t + cs / 2f, cs * 0.3f * k)
            } else {
                D.rrect(c, cx - hw, t, cx + hw, t + cs, cs * 0.15f, C.pink, C.outline, 4f)
                if (k > 0.2f) Icons.draw(c, Icons.PAW, cx, t + cs / 2f, cs * 0.24f * k)
            }
        }
        if (done) {
            D.rrect(c, r.left + r.width() * 0.12f, r.top + r.height() * 0.4f, r.right - r.width() * 0.12f, r.top + r.height() * 0.65f, r.height() * 0.04f, D.alpha(C.white, 245), C.outline, 4f)
            D.text(c, "Parabéns!", r.centerX(), r.top + r.height() * 0.49f, r.height() * 0.055f, C.pinkD)
            D.text(c, "+$reward moedas", r.centerX(), r.top + r.height() * 0.55f, r.height() * 0.04f, C.ink)
            D.text(c, "Toca para jogar de novo", r.centerX(), r.top + r.height() * 0.61f, r.height() * 0.028f, C.ink)
        }
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (done) { enter(); return }
        if (sel.size >= 2) return
        for (i in 0 until 16) if (rects[i].contains(x, y) && !faceUp[i] && !matched[i]) {
            faceUp[i] = true; sel.add(i)
            if (sel.size == 2) {
                moves++
                if (cards[sel[0]] == cards[sel[1]]) {
                    matched[sel[0]] = true; matched[sel[1]] = true; sel.clear()
                    if (matched.all { it }) { done = true; reward = max(8, 40 - moves * 2); g.earn(reward) }
                } else wait = 0.8f
            }
            return
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
            items.add(It(0.1f + Random.nextFloat() * 0.8f, -0.05f, if (q < 0.08f) 1 else if (q < 0.25f) 2 else 0))
            spawn = max(0.35f, 0.9f - el * 0.012f)
        }
        val sp = 0.35f * (1f + el / 40f)
        for (i in items) i.y += sp * dt
        val it = items.iterator()
        while (it.hasNext()) {
            val i = it.next()
            if (i.y > 0.84f && i.y < 0.94f && abs(i.x - bx) < 0.12f) {
                when (i.kind) {
                    0 -> score += 1
                    1 -> score += 5
                    else -> { lives--; g.say("Ai! Uma pedra!"); pose.mood = 2 }
                }
                it.remove()
            } else if (i.y > 1.05f) it.remove()
        }
        if (pose.mood == 2 && state == 1 && items.none { it.kind == 2 && it.y > 0.7f }) pose.mood = 0
        if (timeLeft <= 0f || lives <= 0) { state = 2; overT = 0f; pose.mood = 0; g.earn(score) }
    }

    override fun draw(c: Canvas, r: RectF) {
        skyBackground(c, r, g.time)
        val w = r.width(); val h = r.height()
        D.f(C.green); c.drawRect(r.left, r.top + h * 0.82f, r.right, r.bottom, D.p)
        D.f(C.greenD); c.drawRect(r.left, r.top + h * 0.82f, r.right, r.top + h * 0.835f, D.p)
        for (i in items) {
            val ic = if (i.kind == 0) Icons.CARROT else if (i.kind == 1) Icons.GOLDEN else Icons.ROCK
            Icons.draw(c, ic, r.left + i.x * w, r.top + i.y * h, h * 0.035f)
        }
        val s = h * 0.15f
        val px = r.left + bx * w
        val by = r.top + h * 0.97f
        BunnyArt.draw(c, px, by, s, pose, Save.hat, Save.outfit, Save.acc)
        // cesto à frente do coelho
        D.rrect(c, px - s * 0.32f, by - s * 0.5f, px + s * 0.32f, by - s * 0.12f, s * 0.08f, C.peach, C.outline, 3f)
        D.line(c, px - s * 0.32f, by - s * 0.38f, px + s * 0.32f, by - s * 0.38f, 3f, C.outline)
        D.line(c, px - s * 0.32f, by - s * 0.26f, px + s * 0.32f, by - s * 0.26f, 3f, C.outline)
        D.text(c, "Pontos: $score", r.left + w * 0.05f, r.top + h * 0.06f, h * 0.04f, C.ink, android.graphics.Paint.Align.LEFT)
        D.text(c, "${timeLeft.toInt().coerceAtLeast(0)} s", r.centerX(), r.top + h * 0.06f, h * 0.04f, C.ink)
        for (i in 0 until lives) Icons.draw(c, Icons.HEART, r.right - w * 0.07f - i * w * 0.075f, r.top + h * 0.045f, h * 0.022f)
        if (state == 0) D.text(c, "Arrasta para apanhar cenouras!", r.centerX(), r.top + h * 0.4f, h * 0.035f, C.ink)
        if (state == 2) {
            D.rrect(c, r.left + w * 0.12f, r.top + h * 0.3f, r.right - w * 0.12f, r.top + h * 0.58f, h * 0.04f, D.alpha(C.white, 242), C.outline, 4f)
            D.text(c, "Acabou!", r.centerX(), r.top + h * 0.4f, h * 0.06f, C.pinkD)
            D.text(c, "+$score moedas", r.centerX(), r.top + h * 0.47f, h * 0.04f, C.ink)
            D.text(c, "Toca para jogar de novo", r.centerX(), r.top + h * 0.54f, h * 0.028f, C.ink)
        }
    }

    override fun down(x: Float, y: Float, r: RectF) {
        tx = ((x - r.left) / r.width()).coerceIn(0.08f, 0.92f)
        if (state == 0) reset() else if (state == 2 && overT > 0.7f) reset()
    }

    override fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {
        tx = ((x - r.left) / r.width()).coerceIn(0.08f, 0.92f)
    }
}

// ================================================================= PUZZLE
class PuzzleScreen(g: BunnyView) : Screen(g) {
    override val minigame: Boolean get() = true
    private var bmp: Bitmap? = null
    private val tiles = IntArray(9)
    private var moves = 0
    private var won = false
    private var reward = 0
    private val boardRect = RectF()
    private val shuffleBtn = RectF()

    override fun enter() {
        if (bmp == null) {
            val b = Bitmap.createBitmap(420, 420, Bitmap.Config.ARGB_8888)
            val bc = Canvas(b)
            D.f(C.sky); bc.drawRect(0f, 0f, 420f, 420f, D.p)
            D.f(D.mix(C.sky, C.pink, 0.5f)); bc.drawRect(0f, 200f, 420f, 420f, D.p)
            D.circle(bc, 340f, 70f, 45f, C.butter)
            Icons.draw(bc, Icons.CLOUD, 90f, 80f, 50f)
            Icons.draw(bc, Icons.CLOUD, 250f, 130f, 36f)
            D.oval(bc, 100f, 420f, 260f, 130f, C.green)
            D.oval(bc, 340f, 430f, 240f, 120f, C.greenD)
            Icons.draw(bc, Icons.FLOWER, 50f, 380f, 18f)
            Icons.draw(bc, Icons.FLOWER, 370f, 360f, 16f)
            Icons.draw(bc, Icons.CARROT, 330f, 385f, 18f)
            val p = Pose()
            BunnyArt.draw(bc, 205f, 395f, 235f, p, Save.hat, Save.outfit, Save.acc)
            bmp = b
        }
        shuffle()
    }

    private fun shuffle() {
        for (i in 0 until 9) tiles[i] = i
        var e = 8
        var last = -1
        for (n in 0 until 160) {
            val row = e / 3; val col = e % 3
            val opts = ArrayList<Int>()
            if (row > 0) opts.add(e - 3)
            if (row < 2) opts.add(e + 3)
            if (col > 0) opts.add(e - 1)
            if (col < 2) opts.add(e + 1)
            opts.remove(last)
            val nx = opts[Random.nextInt(opts.size)]
            tiles[e] = tiles[nx]; tiles[nx] = 8
            last = e; e = nx
        }
        moves = 0; won = false
    }

    override fun update(dt: Float) {}

    override fun draw(c: Canvas, r: RectF) {
        D.f(D.mix(C.cream, C.mint, 0.35f)); c.drawRect(r, D.p)
        val bs = min(r.width() * 0.9f, r.height() * 0.66f)
        boardRect.set(r.centerX() - bs / 2f, r.top + r.height() * 0.13f, r.centerX() + bs / 2f, r.top + r.height() * 0.13f + bs)
        val ts = bs / 3f
        D.text(c, "Puzzle · Jogadas: $moves", r.centerX(), r.top + r.height() * 0.075f, r.height() * 0.04f, C.ink)
        val b = bmp ?: return
        for (k in 0 until 9) {
            val p = tiles[k]
            val dst = RectF(boardRect.left + (k % 3) * ts, boardRect.top + (k / 3) * ts, boardRect.left + (k % 3 + 1) * ts, boardRect.top + (k / 3 + 1) * ts)
            if (p == 8 && !won) { D.rrect(c, dst.left + 4f, dst.top + 4f, dst.right - 4f, dst.bottom - 4f, ts * 0.08f, D.alpha(C.outline, 90)); continue }
            val src = Rect((p % 3) * 140, (p / 3) * 140, (p % 3 + 1) * 140, (p / 3 + 1) * 140)
            c.drawBitmap(b, src, dst, null)
            D.rrect(c, dst.left + 2f, dst.top + 2f, dst.right - 2f, dst.bottom - 2f, ts * 0.06f, 0, 0, 0f)
            D.s(C.white, 5f); c.drawRect(dst, D.p)
        }
        shuffleBtn.set(r.centerX() - r.width() * 0.25f, boardRect.bottom + r.height() * 0.04f, r.centerX() + r.width() * 0.25f, boardRect.bottom + r.height() * 0.04f + r.height() * 0.08f)
        D.button(c, shuffleBtn, if (won) "Jogar de novo" else "Baralhar", C.pink)
        if (won) D.text(c, "Completo! +$reward moedas", r.centerX(), boardRect.bottom + r.height() * 0.025f, r.height() * 0.035f, C.pinkD)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (shuffleBtn.contains(x, y)) { shuffle(); return }
        if (won || !boardRect.contains(x, y)) return
        val ts = boardRect.width() / 3f
        val col = ((x - boardRect.left) / ts).toInt().coerceIn(0, 2)
        val row = ((y - boardRect.top) / ts).toInt().coerceIn(0, 2)
        val k = row * 3 + col
        val e = tiles.indexOf(8)
        if (abs(e / 3 - row) + abs(e % 3 - col) == 1) {
            tiles[e] = tiles[k]; tiles[k] = 8
            moves++
            if ((0 until 9).all { tiles[it] == it }) { won = true; reward = max(10, 50 - moves / 2); g.earn(reward) }
        }
    }
}
