package com.example.bunny

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Física simples de salto, em unidades de s. */
private fun hopStep(p: Pose, dt: Float, v: FloatArray) {
    if (p.hop > 0f || v[0] != 0f) {
        v[0] -= 9f * dt
        p.hop += v[0] * dt
        if (p.hop <= 0f) { p.hop = 0f; v[0] = 0f; p.squash = 1f }
    }
    p.squash = max(0f, p.squash - dt * 6f)
}

private fun hopStart(p: Pose, v: FloatArray, power: Float = 2.6f) {
    if (p.hop == 0f) { v[0] = power; p.hop = 0.001f }
}

// ================================================================= CASA
class HomeScreen(g: BunnyView) : Screen(g) {
    private val pose = Pose()
    private val hv = floatArrayOf(0f)
    private var idle = 4f
    private val btns = Array(3) { RectF() }
    private val btnIcons = intArrayOf(Icons.BAG, Icons.HAT, Icons.HOUSE)
    private val btnNames = arrayOf("Loja", "Roupas", "Decorar")
    private var bx = 0f; private var by = 0f; private var bs = 0f

    override fun update(dt: Float) {
        pose.t += dt
        hopStep(pose, dt, hv)
        idle -= dt
        if (idle <= 0f) { idle = 4f + Random.nextFloat() * 4f; if (Save.mood() == 0) hopStart(pose, hv, 1.8f) }
        pose.squash = max(pose.squash, 0f)
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        Rooms.draw(c, r, 0, g.night, g.time)
        bs = r.height() * 0.34f
        bx = r.centerX()
        by = Rooms.floorY(r) + r.height() * 0.24f
        D.text(c, "Bunny", r.centerX(), r.top + r.height() * 0.085f, r.height() * 0.07f, D.alpha(C.white, 235))
        pose.mood = Save.mood()
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        val breathe = sin(pose.t * 2f) * 0.02f
        pose.squash += breathe
        BunnyArt.draw(c, bx, by, bs, pose, Save.hat, Save.outfit, Save.acc)
        pose.squash -= breathe

        val w = Save.worst()
        if (w.second < 40f) {
            val txt = arrayOf("Tenho fome!", "Preciso de banho!", "Estou com sono...", "Vamos brincar?")[w.first]
            val ic = intArrayOf(Icons.CARROT, Icons.BUBBLE, Icons.MOON, Icons.BALL)[w.first]
            val tw = D.textW(txt, r.height() * 0.03f) + r.height() * 0.1f
            val x0 = bx + bs * 0.35f; val y0 = by - bs * 1.35f
            D.rrect(c, x0 - tw * 0.1f, y0 - r.height() * 0.035f, x0 - tw * 0.1f + tw, y0 + r.height() * 0.04f, r.height() * 0.03f, C.white, C.outline, 3f)
            Icons.draw(c, ic, x0 - tw * 0.1f + r.height() * 0.035f, y0 + r.height() * 0.003f, r.height() * 0.022f)
            D.text(c, txt, x0 - tw * 0.1f + tw * 0.56f, y0 + r.height() * 0.012f, r.height() * 0.03f, C.ink)
        }
        val rad = r.width() * 0.07f
        for (i in 0 until 3) {
            val cx = r.right - r.width() * 0.11f
            val cy = r.top + r.height() * 0.2f + i * r.height() * 0.13f
            btns[i].set(cx - rad, cy - rad, cx + rad, cy + rad)
            D.circle(c, cx, cy, rad, D.alpha(C.white, 235), C.outline, 3f)
            Icons.draw(c, btnIcons[i], cx, cy - rad * 0.08f, rad * 0.55f)
            D.text(c, btnNames[i], cx, cy + rad * 1.35f, rad * 0.5f, C.ink)
        }
        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        for (i in 0 until 3) if (btns[i].contains(x, y)) { g.go(Nav.SHOP + i); return }
        if (hypot(x - bx, y - (by - bs * 0.6f)) < bs * 0.6f) {
            hopStart(pose, hv)
            Save.joy = min(100f, Save.joy + 3f)
            burst(bx, by - bs * 1.1f, Icons.HEART, 4, bs * 0.07f)
        }
    }
}

// ================================================================= COMER
class EatScreen(g: BunnyView) : Screen(g) {
    private val pose = Pose()
    private val foods = intArrayOf(Icons.CARROT, Icons.APPLE, Icons.LETTUCE, Icons.BERRY, Icons.CUPCAKE)
    private val nutri = floatArrayOf(14f, 12f, 10f, 12f, 20f)
    private val slots = Array(5) { RectF() }
    private var drag = -1
    private var dx = 0f; private var dy = 0f
    private var eatT = 0f
    private var shake = 0f
    private var hx = 0f; private var hy = 0f; private var hr = 0f

    override fun update(dt: Float) {
        pose.t += dt
        if (eatT > 0f) { eatT -= dt; pose.mouth = 0.5f + 0.5f * sin(eatT * 16f) } else pose.mouth = 0f
        pose.look = sin(g.time * 30f) * shake
        shake = max(0f, shake - dt * 1.5f)
        pose.squash = max(0f, pose.squash - dt * 6f)
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        Rooms.draw(c, r, 2, g.night, g.time)
        val s = r.height() * 0.32f
        val bx = r.centerX(); val by = Rooms.floorY(r) + r.height() * 0.2f
        pose.mood = if (Save.mood() == 3) 1 else Save.mood()
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        BunnyArt.draw(c, bx, by, s, pose, Save.hat, Save.outfit, Save.acc)
        hx = bx; hy = by - s * 0.72f; hr = s * 0.5f
        // bandeja
        val tray = RectF(r.left + r.width() * 0.04f, r.bottom - r.height() * 0.17f, r.right - r.width() * 0.04f, r.bottom - r.height() * 0.02f)
        D.rrect(c, tray, tray.height() * 0.3f, C.peach, C.outline, 4f)
        val sw = tray.width() / 5f
        for (i in 0 until 5) {
            slots[i].set(tray.left + i * sw, tray.top, tray.left + (i + 1) * sw, tray.bottom)
            if (i != drag) Icons.draw(c, foods[i], slots[i].centerX(), slots[i].centerY(), tray.height() * 0.32f)
        }
        D.text(c, "Arrasta a comida para o Bunny", r.centerX(), tray.top - r.height() * 0.015f, r.height() * 0.026f, C.ink)
        if (drag >= 0) Icons.draw(c, foods[drag], dx, dy - tray.height() * 0.3f, tray.height() * 0.42f)
        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        for (i in 0 until 5) if (slots[i].contains(x, y)) { drag = i; dx = x; dy = y }
    }

    override fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {
        if (drag >= 0) { this.dx = x; this.dy = y }
    }

    override fun up(x: Float, y: Float, r: RectF) {
        if (drag < 0) return
        if (hypot(dx - hx, dy - 0.3f * hr - hy) < hr * 1.3f) {
            if (Save.hunger >= 99f) {
                shake = 1f; g.say("Estou cheio!")
            } else {
                Save.hunger = min(100f, Save.hunger + nutri[drag])
                if (foods[drag] == Icons.CUPCAKE) Save.joy = min(100f, Save.joy + 4f)
                eatT = 1.3f
                burst(hx, hy - hr, Icons.HEART, 4, hr * 0.15f)
            }
        }
        drag = -1
    }
}

// ================================================================= BANHO
class BathScreen(g: BunnyView) : Screen(g) {
    private val pose = Pose()
    private var foam = 0f
    private var spongeX = 0f; private var spongeY = 0f
    private var spongeOn = false
    private var rinsing = false
    private var rinsedBonus = false
    private var bx = 0f; private var by = 0f; private var bs = 0f
    private val sponge = RectF(); private val shower = RectF()

    override fun update(dt: Float) {
        pose.t += dt
        if (rinsing) {
            foam = max(0f, foam - dt * 0.5f)
            val sx = shower.centerX()
            for (i in 0 until 2) fx.add(Fx(sx + (Random.nextFloat() - 0.5f) * bs * 0.5f, shower.bottom, 0f, bs * 2.2f, 0.7f, Icons.BUBBLE, bs * 0.03f))
            if (foam <= 0.02f && rinsedBonus) { rinsedBonus = false; Save.hygiene = min(100f, Save.hygiene + 8f); g.say("Que cheirinho!") }
        }
        pose.mood = if (Save.mood() == 3) 0 else Save.mood()
        pose.foam = foam
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        // azulejos
        D.f(Colors.tile); c.drawRect(r, D.p)
        val n = 6; val tw = r.width() / n
        for (j in 0 until (r.height() / tw).toInt() + 1) for (i in 0 until n) {
            D.rrect(c, r.left + i * tw + 3f, r.top + j * tw + 3f, r.left + (i + 1) * tw - 3f, r.top + (j + 1) * tw - 3f, tw * 0.12f, if ((i + j) % 2 == 0) C.white else D.alpha(C.sky, 140))
        }
        bs = r.height() * 0.3f; bx = r.centerX(); by = r.top + r.height() * 0.66f
        // banheira (parte de trás)
        val tub = RectF(r.left + r.width() * 0.08f, r.top + r.height() * 0.55f, r.right - r.width() * 0.08f, r.top + r.height() * 0.88f)
        D.rrect(c, tub, tub.height() * 0.35f, C.white, C.outline, 5f)
        BunnyArt.draw(c, bx, by, bs, pose, Save.hat, Save.outfit, Save.acc)
        // água e rebordo
        val water = RectF(tub.left + 6f, by - bs * 0.15f, tub.right - 6f, tub.bottom - 6f)
        D.rrect(c, water, tub.height() * 0.3f, D.alpha(C.sky, 190))
        D.rrect(c, tub.left, tub.top + tub.height() * 0.38f, tub.right, tub.top + tub.height() * 0.5f, tub.height() * 0.06f, C.white, C.outline, 4f)
        for (i in 0 until 5) D.circle(c, tub.left + (i + 0.5f) * tub.width() / 5f, tub.top + tub.height() * 0.7f, tub.height() * 0.04f, D.alpha(C.white, 200))
        // chuveiro
        shower.set(r.left + r.width() * 0.06f, r.top + r.height() * 0.04f, r.left + r.width() * 0.3f, r.top + r.height() * 0.2f)
        D.circle(c, shower.centerX(), shower.centerY(), shower.height() * 0.62f, D.alpha(if (rinsing) C.blue else C.white, 230), C.outline, 3f)
        Icons.draw(c, Icons.SHOWER, shower.centerX(), shower.centerY(), shower.height() * 0.4f)
        D.text(c, "Mantém para enxaguar", shower.centerX() + shower.width() * 0.55f, shower.bottom + r.height() * 0.045f, r.height() * 0.022f, C.ink, Paint.Align.LEFT)
        // esponja
        if (!spongeOn) { spongeX = r.right - r.width() * 0.17f; spongeY = r.top + r.height() * 0.36f }
        sponge.set(spongeX - bs * 0.3f, spongeY - bs * 0.2f, spongeX + bs * 0.3f, spongeY + bs * 0.2f)
        Icons.draw(c, Icons.SPONGE, spongeX, spongeY, bs * 0.26f)
        if (!spongeOn) D.text(c, "Esfrega!", spongeX, spongeY + bs * 0.4f, r.height() * 0.026f, C.ink)
        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (sponge.contains(x, y)) { spongeOn = true; spongeX = x; spongeY = y }
        else if (hypot(x - shower.centerX(), y - shower.centerY()) < shower.height() * 0.9f) rinsing = true
    }

    override fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {
        if (!spongeOn) return
        spongeX = x; spongeY = y
        if (hypot(x - bx, y - (by - bs * 0.55f)) < bs * 0.65f) {
            val d = hypot(dx, dy)
            foam = min(1f, foam + d * 0.0012f)
            Save.hygiene = min(100f, Save.hygiene + d * 0.03f)
            if (foam > 0.5f) rinsedBonus = true
            if (Random.nextFloat() < 0.4f) fx.add(Fx(x, y, (Random.nextFloat() - 0.5f) * bs * 0.8f, -bs * 0.6f, 0.8f, Icons.BUBBLE, bs * 0.045f))
        }
    }

    override fun up(x: Float, y: Float, r: RectF) { spongeOn = false; rinsing = false }
}

private object Colors { val tile = C.sky }

// ================================================================= DORMIR
class SleepScreen(g: BunnyView) : Screen(g) {
    private val pose = Pose()
    private var lightOn = true
    private var dark = 0f
    private var zT = 0f
    private val lamp = RectF()

    override fun enter() { lightOn = g.night < 0.5f; Save.sleeping = !lightOn; dark = if (lightOn) 0f else 1f }
    override fun leave() { Save.sleeping = false }

    override fun update(dt: Float) {
        pose.t += dt
        dark += ((if (lightOn) 0f else 1f) - dark) * min(1f, dt * 4f)
        if (Save.sleeping) {
            zT -= dt
            if (zT <= 0f) { zT = 1.3f; fx.add(Fx(g.width * 0.58f, g.height * 0.45f, g.width * 0.04f, -g.height * 0.05f, 2.2f, -1, g.height * 0.035f)) }
        }
        pose.mood = if (Save.sleeping) 3 else Save.mood()
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        Rooms.draw(c, r, 1, g.night, g.time)
        val w = r.width(); val h = r.height()
        // cama
        val bed = RectF(r.left + w * 0.08f, Rooms.floorY(r) - h * 0.02f, r.right - w * 0.08f, Rooms.floorY(r) + h * 0.3f)
        D.rrect(c, bed.left, bed.top - h * 0.16f, bed.left + w * 0.12f, bed.bottom, w * 0.04f, C.peach, C.outline, 4f)
        D.rrect(c, bed, h * 0.04f, C.pink, C.outline, 4f)
        D.rrect(c, bed.left + w * 0.04f, bed.top - h * 0.04f, bed.left + w * 0.4f, bed.top + h * 0.06f, h * 0.04f, C.white, C.outline, 4f)
        val s = h * 0.25f
        BunnyArt.draw(c, bed.left + w * 0.28f, bed.top + h * 0.12f, s, pose, Save.hat, Save.outfit, Save.acc)
        // cobertor
        D.rrect(c, bed.left + w * 0.12f, bed.top + h * 0.06f, bed.right - w * 0.02f, bed.bottom - h * 0.01f, h * 0.04f, C.lav, C.outline, 4f)
        for (i in 0 until 5) Icons.draw(c, Icons.STAR, bed.left + w * (0.25f + i * 0.14f), bed.top + h * 0.17f, h * 0.012f)
        // escuridão
        if (dark > 0.01f) { D.f(android.graphics.Color.argb((dark * 175).toInt(), 18, 14, 60)); c.drawRect(r, D.p) }
        drawFx(c)
        // interruptor
        val rad = w * 0.075f
        lamp.set(r.right - w * 0.17f, r.top + h * 0.04f, r.right - w * 0.03f, r.top + h * 0.04f + w * 0.14f)
        D.circle(c, lamp.centerX(), lamp.centerY(), rad, if (lightOn) C.butter else C.white, C.outline, 3f)
        Icons.draw(c, Icons.LAMP, lamp.centerX(), lamp.centerY(), rad * 0.55f)
        D.text(c, if (lightOn) "Apagar luz" else "Acender", lamp.centerX(), lamp.bottom + h * 0.04f, h * 0.024f, if (dark > 0.5f) C.white else C.ink)
        if (Save.sleeping) D.text(c, "A recuperar energia...", r.centerX(), r.top + h * 0.1f, h * 0.03f, C.white)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (lamp.contains(x, y)) { lightOn = !lightOn; Save.sleeping = !lightOn }
    }
}

// ================================================================= BRINCAR
class PlayScreen(g: BunnyView) : Screen(g) {
    private class Toy(var x: Float, var y: Float, val icon: Int) { var vx = 0f; var vy = 0f }

    private val pose = Pose()
    private val hv = floatArrayOf(0f)
    private val toys = ArrayList<Toy>()
    private var taps = 0
    private var gy = 0f
    private var ready = false

    override fun update(dt: Float) {
        pose.t += dt
        hopStep(pose, dt, hv)
        if (ready) for (t in toys) {
            t.vy += g.height * 1.6f * dt
            t.x += t.vx * dt; t.y += t.vy * dt
            if (t.y > gy) { t.y = gy; t.vy = -t.vy * 0.62f; if (abs(t.vy) < g.height * 0.1f) t.vy = 0f }
            if (t.x < g.width * 0.08f || t.x > g.width * 0.92f) { t.vx = -t.vx * 0.8f; t.x = t.x.coerceIn(g.width * 0.08f, g.width * 0.92f) }
            t.vx *= 0.995f
        }
        pose.mood = if (Save.mood() == 3) 0 else min(Save.mood(), 1)
        pose.mood = 0
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        Rooms.draw(c, r, 0, g.night, g.time)
        gy = Rooms.floorY(r) + r.height() * 0.3f
        if (!ready) {
            toys.add(Toy(r.width() * 0.3f, r.top + r.height() * 0.3f, Icons.BALL))
            toys.add(Toy(r.width() * 0.7f, r.top + r.height() * 0.2f, Icons.YARN))
            ready = true
        }
        val s = r.height() * 0.3f
        BunnyArt.draw(c, r.centerX(), Rooms.floorY(r) + r.height() * 0.2f, s, pose, Save.hat, Save.outfit, Save.acc)
        for (t in toys) Icons.draw(c, t.icon, t.x, t.y, r.width() * 0.07f)
        D.text(c, "Toca nos brinquedos!", r.centerX(), r.bottom - r.height() * 0.03f, r.height() * 0.028f, C.ink)
        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        for (t in toys) {
            if (hypot(x - t.x, y - t.y) < r.width() * 0.12f) {
                t.vy = -g.height * 0.9f; t.vx = (t.x - x) * 6f + (Random.nextFloat() - 0.5f) * g.width * 0.4f
                Save.joy = min(100f, Save.joy + 1.2f)
                taps++
                hopStart(pose, hv, 2.2f)
                burst(t.x, t.y, Icons.SPARK, 3, r.width() * 0.02f)
                if (taps % 10 == 0) { g.earn(1); burst(r.centerX(), r.centerY(), Icons.HEART, 5, r.width() * 0.03f) }
                break
            }
        }
    }
}
