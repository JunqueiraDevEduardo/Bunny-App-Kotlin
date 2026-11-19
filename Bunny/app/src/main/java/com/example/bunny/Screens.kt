package com.example.bunny

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Física de salto do coelhinho e da gatinha. */
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

    // Gatinha fofinha companheira
    private var catX = 0f; private var catY = 0f; private var catS = 0f
    private var catPounce = 0f
    private var catPounceV = 0f

    override fun update(dt: Float) {
        pose.t += dt
        hopStep(pose, dt, hv)
        idle -= dt
        if (idle <= 0f) {
            idle = 4f + Random.nextFloat() * 4f
            if (Save.mood() == 0) hopStart(pose, hv, 1.8f)
        }
        pose.squash = max(pose.squash, 0f)

        // Física do salto da gatinha
        if (catPounce > 0f || catPounceV != 0f) {
            catPounceV -= 8f * dt
            catPounce += catPounceV * dt
            if (catPounce <= 0f) { catPounce = 0f; catPounceV = 0f }
        }

        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        Rooms.draw(c, r, 0, g.night, g.time)
        val scale = Save.bunnyScale()
        bs = r.height() * 0.34f * scale
        bx = r.centerX() + r.width() * 0.06f
        by = Rooms.floorY(r) + r.height() * 0.24f

        // Título e Fase do Coelho
        val title = "Bunny · ${Save.stage()} (Nv. ${Save.level()})"
        D.text(c, title, r.centerX(), r.top + r.height() * 0.075f, r.height() * 0.045f, D.alpha(C.white, 245))

        pose.mood = Save.mood()
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        val breathe = sin(pose.t * 2f) * 0.02f
        pose.squash += breathe
        BunnyArt.draw(c, bx, by, bs, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        pose.squash -= breathe

        // Gatinha fofinha lindinha companheira ao lado do Bunny
        if (Save.hasCat) {
            catS = r.height() * 0.20f
            catX = bx - bs * 0.55f - catS * 0.35f
            catY = by
            CatArt.draw(c, catX, catY, catS, pose.t, mood = if (Save.catHappiness > 60f) 0 else 1, pounce = catPounce)
        }

        // Balão de fala estilo Pou quando alguma necessidade está baixa
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

        // Botões de Acesso (Loja, Roupas, Decorar)
        val rad = r.width() * 0.07f
        for (i in 0 until 3) {
            val cx = r.right - r.width() * 0.11f
            val cy = r.top + r.height() * 0.2f + i * r.height() * 0.13f
            btns[i].set(cx - rad, cy - rad, cx + rad, cy + rad)
            D.circle(c, cx, cy, rad, D.alpha(C.white, 235), C.outline, 3f)
            Icons.draw(c, btnIcons[i], cx, cy - rad * 0.08f, rad * 0.55f)
            D.text(c, btnNames[i], cx, cy + rad * 1.35f, rad * 0.48f, C.ink)
        }

        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        for (i in 0 until 3) if (btns[i].contains(x, y)) { g.go(Nav.SHOP + i); return }

        // Carinho no Bunny
        if (hypot(x - bx, y - (by - bs * 0.6f)) < bs * 0.6f) {
            hopStart(pose, hv)
            Save.joy = min(100f, Save.joy + 3f)
            burst(bx, by - bs * 1.1f, Icons.HEART, 4, bs * 0.07f)
            g.say("Bunny adorou o carinho! ♥")
            return
        }

        // Carinho na Gatinha fofinha
        if (Save.hasCat && hypot(x - catX, y - (catY - catS * 0.5f)) < catS * 0.6f) {
            catPounceV = 2.2f; catPounce = 0.01f
            Save.catHappiness = min(100f, Save.catHappiness + 8f)
            Save.joy = min(100f, Save.joy + 2f)
            burst(catX, catY - catS * 0.9f, Icons.HEART, 4, catS * 0.08f)
            g.say("Miau! :3 A gatinha ronrona feliz!")
        }
    }
}

// ================================================================= COMER
class EatScreen(g: BunnyView) : Screen(g) {
    private val pose = Pose()
    // Itens de comida incluindo pratos e bebidas típicas peruanas
    private class FoodItem(val icon: Int, val name: String, val nutri: Float, val joy: Float, val isEnergyPotion: Boolean = false, val isSoapPotion: Boolean = false)

    private val allFoods = listOf(
        FoodItem(Icons.CEVICHE, "Ceviche Peruano", 28f, 15f),
        FoodItem(Icons.LOMO_SALTADO, "Lomo Saltado", 32f, 18f),
        FoodItem(Icons.CHICHA_MORADA, "Chicha Morada", 16f, 14f),
        FoodItem(Icons.ALFAJOR, "Alfajor Peruano", 20f, 20f),
        FoodItem(Icons.CAUSA, "Causa Limeña", 24f, 14f),
        FoodItem(Icons.CHOCLO, "Choclo c/ Queijo", 18f, 10f),
        FoodItem(Icons.CARROT, "Cenoura Doce", 14f, 6f),
        FoodItem(Icons.APPLE, "Maçã Fresca", 12f, 8f),
        FoodItem(Icons.BERRY, "Bagas Silvestres", 12f, 10f),
        FoodItem(Icons.CUPCAKE, "Cupcake Rosa", 22f, 22f),
        FoodItem(Icons.POTION, "Poção Energia", 0f, 25f, isEnergyPotion = true),
        FoodItem(Icons.POTION, "Poção Banho", 0f, 20f, isSoapPotion = true)
    )

    private var page = 0
    private val pageSize = 4
    private val slots = Array(4) { RectF() }
    private val prevBtn = RectF()
    private val nextBtn = RectF()
    private var drag = -1
    private var dx = 0f; private var dy = 0f
    private var eatT = 0f
    private var shake = 0f
    private var hx = 0f; private var hy = 0f; private var hr = 0f

    override fun update(dt: Float) {
        pose.t += dt
        if (eatT > 0f) {
            eatT -= dt
            pose.mouth = 0.5f + 0.5f * sin(eatT * 16f)
        } else {
            pose.mouth = 0f
        }
        pose.look = sin(g.time * 30f) * shake
        shake = max(0f, shake - dt * 1.5f)
        pose.squash = max(0f, pose.squash - dt * 6f)
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        Rooms.draw(c, r, 2, g.night, g.time)
        val scale = Save.bunnyScale()
        val s = r.height() * 0.32f * scale
        val bx = r.centerX()
        val by = Rooms.floorY(r) + r.height() * 0.20f
        pose.mood = if (Save.mood() == 3) 1 else Save.mood()
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        BunnyArt.draw(c, bx, by, s, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        hx = bx; hy = by - s * 0.72f; hr = s * 0.5f

        // Bandeja de comida na cozinha
        val tray = RectF(r.left + r.width() * 0.04f, r.bottom - r.height() * 0.19f, r.right - r.width() * 0.04f, r.bottom - r.height() * 0.02f)
        D.rrect(c, tray, tray.height() * 0.25f, C.peach, C.outline, 4f)

        // Botões de navegação da bandeja
        val btnW = tray.width() * 0.10f
        prevBtn.set(tray.left + 8f, tray.top + 8f, tray.left + btnW, tray.bottom - 8f)
        nextBtn.set(tray.right - btnW, tray.top + 8f, tray.right - 8f, tray.bottom - 8f)
        D.button(c, prevBtn, "<", C.white, C.ink, prevBtn.height() * 0.4f)
        D.button(c, nextBtn, ">", C.white, C.ink, nextBtn.height() * 0.4f)

        val startIdx = page * pageSize
        val foodAreaW = (nextBtn.left - prevBtn.right)
        val sw = foodAreaW / pageSize
        for (i in 0 until pageSize) {
            val idx = startIdx + i
            val sl = prevBtn.right + i * sw
            slots[i].set(sl + 4f, tray.top + 6f, sl + sw - 4f, tray.bottom - 6f)
            if (idx < allFoods.size) {
                val item = allFoods[idx]
                if (i != drag) {
                    D.rrect(c, slots[i], slots[i].height() * 0.18f, C.white, C.outline, 2f)
                    Icons.draw(c, item.icon, slots[i].centerX(), slots[i].top + slots[i].height() * 0.42f, slots[i].height() * 0.28f)
                    D.text(c, item.name, slots[i].centerX(), slots[i].bottom - slots[i].height() * 0.12f, slots[i].height() * 0.16f, C.ink)
                }
            }
        }

        D.text(c, "Arrasta a comida peruana até ao Bunny!", r.centerX(), tray.top - r.height() * 0.015f, r.height() * 0.026f, C.ink)

        // Desenha o item arrastado
        if (drag >= 0 && (startIdx + drag) < allFoods.size) {
            val item = allFoods[startIdx + drag]
            Icons.draw(c, item.icon, dx, dy, tray.height() * 0.44f)
        }

        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (prevBtn.contains(x, y)) {
            page = if (page > 0) page - 1 else (allFoods.size - 1) / pageSize
            return
        }
        if (nextBtn.contains(x, y)) {
            page = if ((page + 1) * pageSize < allFoods.size) page + 1 else 0
            return
        }
        val startIdx = page * pageSize
        for (i in 0 until pageSize) {
            if (slots[i].contains(x, y) && (startIdx + i) < allFoods.size) {
                drag = i; dx = x; dy = y
                return
            }
        }
    }

    override fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {
        if (drag >= 0) { this.dx = x; this.dy = y }
    }

    override fun up(x: Float, y: Float, r: RectF) {
        if (drag < 0) return
        val startIdx = page * pageSize
        val foodIdx = startIdx + drag
        if (foodIdx < allFoods.size && hypot(dx - hx, dy - hy) < hr * 1.4f) {
            val food = allFoods[foodIdx]
            if (food.isEnergyPotion) {
                Save.energy = 100f
                eatT = 1.2f
                burst(hx, hy - hr * 0.5f, Icons.STAR, 6, hr * 0.15f)
                g.say("Poção de Energia! Bunny está 100% acordado!")
            } else if (food.isSoapPotion) {
                Save.hygiene = 100f
                eatT = 1.2f
                burst(hx, hy - hr * 0.5f, Icons.BUBBLE, 6, hr * 0.15f)
                g.say("Poção de Banho! Bunny está limpíssimo!")
            } else if (Save.hunger >= 99f) {
                shake = 1f
                g.say("Estou cheio! Não cabe mais nada!")
            } else {
                Save.hunger = min(100f, Save.hunger + food.nutri)
                Save.joy = min(100f, Save.joy + food.joy)
                eatT = 1.3f
                burst(hx, hy - hr * 0.5f, Icons.HEART, 5, hr * 0.15f)
                val msg = when (food.icon) {
                    Icons.CEVICHE -> "Nham nham! Ceviche peruano delicioso!"
                    Icons.LOMO_SALTADO -> "Uau! Lomo Saltado saboroso!"
                    Icons.CHICHA_MORADA -> "Que chicha morada fresquinha!"
                    Icons.ALFAJOR -> "Alfajor andino docinho!"
                    Icons.CAUSA -> "Causa Limeña cremosa!"
                    else -> "Nham nham! Que bom!"
                }
                g.say(msg)
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
            foam = max(0f, foam - dt * 0.55f)
            val sx = shower.centerX()
            // Gotas de água caem diretamente sobre a cabeça e corpo do Bunny no centro
            for (i in 0 until 4) {
                fx.add(Fx(sx + (Random.nextFloat() - 0.5f) * bs * 0.5f, shower.bottom + 10f, 0f, bs * 2.8f, 0.65f, Icons.BUBBLE, bs * 0.032f))
            }
            if (foam <= 0.02f && rinsedBonus) {
                rinsedBonus = false
                Save.hygiene = min(100f, Save.hygiene + 12f)
                g.say("Bunny ficou cheiroso e feliz!")
            }
        }
        pose.mood = if (Save.mood() == 3) 0 else Save.mood()
        pose.foam = foam
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        // Azulejos do banheiro
        D.f(C.sky); c.drawRect(r, D.p)
        val n = 6; val tw = r.width() / n
        for (j in 0 until (r.height() / tw).toInt() + 1) for (i in 0 until n) {
            D.rrect(c, r.left + i * tw + 3f, r.top + j * tw + 3f, r.left + (i + 1) * tw - 3f, r.top + (j + 1) * tw - 3f, tw * 0.12f, if ((i + j) % 2 == 0) C.white else D.alpha(C.sky, 140))
        }

        val scale = Save.bunnyScale()
        bs = r.height() * 0.30f * scale
        bx = r.centerX()
        by = r.top + r.height() * 0.66f

        // Banheira (fundo)
        val tub = RectF(r.left + r.width() * 0.08f, r.top + r.height() * 0.55f, r.right - r.width() * 0.08f, r.top + r.height() * 0.88f)
        D.rrect(c, tub, tub.height() * 0.35f, C.white, C.outline, 5f)

        BunnyArt.draw(c, bx, by, bs, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)

        // Água e rebordo da banheira
        val water = RectF(tub.left + 6f, by - bs * 0.15f, tub.right - 6f, tub.bottom - 6f)
        D.rrect(c, water, tub.height() * 0.3f, D.alpha(C.sky, 190))
        D.rrect(c, tub.left, tub.top + tub.height() * 0.38f, tub.right, tub.top + tub.height() * 0.5f, tub.height() * 0.06f, C.white, C.outline, 4f)
        for (i in 0 until 5) D.circle(c, tub.left + (i + 0.5f) * tub.width() / 5f, tub.top + tub.height() * 0.7f, tub.height() * 0.04f, D.alpha(C.white, 200))

        // =================================================================
        // CHUVEIRO: NO CENTRO EM CIMA DA CABEÇA DO COELHO!
        // =================================================================
        val shW = r.width() * 0.26f
        val shH = r.height() * 0.15f
        shower.set(bx - shW / 2f, r.top + r.height() * 0.03f, bx + shW / 2f, r.top + r.height() * 0.03f + shH)

        // Cano que desce do teto até ao chuveiro no centro
        D.line(c, bx, r.top, bx, shower.top + shH * 0.2f, 8f, Color.rgb(170, 195, 220))

        // Cabeça do chuveiro arredondada no centro
        D.circle(c, shower.centerX(), shower.centerY(), shower.height() * 0.48f, D.alpha(if (rinsing) C.blue else C.white, 235), C.outline, 3.5f)
        Icons.draw(c, Icons.SHOWER, shower.centerX(), shower.centerY(), shower.height() * 0.34f)
        D.text(c, if (rinsing) "A enxaguar..." else "Mantém no chuveiro para enxaguar", bx, shower.bottom + r.height() * 0.035f, r.height() * 0.024f, C.ink)

        // Esponja de sabão
        if (!spongeOn) {
            spongeX = r.right - r.width() * 0.16f
            spongeY = r.top + r.height() * 0.38f
        }
        sponge.set(spongeX - bs * 0.28f, spongeY - bs * 0.18f, spongeX + bs * 0.28f, spongeY + bs * 0.18f)
        Icons.draw(c, Icons.SPONGE, spongeX, spongeY, bs * 0.26f)
        if (!spongeOn) D.text(c, "Esfrega com a esponja!", spongeX, spongeY + bs * 0.38f, r.height() * 0.024f, C.ink)

        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (sponge.contains(x, y)) {
            spongeOn = true; spongeX = x; spongeY = y
        } else if (hypot(x - shower.centerX(), y - shower.centerY()) < shower.height() * 0.95f) {
            rinsing = true
        }
    }

    override fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {
        if (!spongeOn) return
        spongeX = x; spongeY = y
        if (hypot(x - bx, y - (by - bs * 0.55f)) < bs * 0.70f) {
            val d = hypot(dx, dy)
            foam = min(1f, foam + d * 0.0015f)
            Save.hygiene = min(100f, Save.hygiene + d * 0.035f)
            if (foam > 0.4f) rinsedBonus = true
            if (Random.nextFloat() < 0.45f) {
                fx.add(Fx(x, y, (Random.nextFloat() - 0.5f) * bs * 0.8f, -bs * 0.6f, 0.8f, Icons.BUBBLE, bs * 0.045f))
            }
        }
    }

    override fun up(x: Float, y: Float, r: RectF) {
        spongeOn = false
        rinsing = false
    }
}

// ================================================================= DORMIR
class SleepScreen(g: BunnyView) : Screen(g) {
    private val pose = Pose()
    private var lightOn = true
    private var dark = 0f
    private var zT = 0f
    private val lamp = RectF()

    override fun enter() {
        // Se já estava a dormir, continua a dormir
        lightOn = !Save.sleeping
        dark = if (lightOn) 0f else 1f
    }

    override fun leave() {
        // NÃO cancela Save.sleeping se a luz estiver apagada!
        // Permite que o coelho continue a dormir ao sair do quarto ou da app.
    }

    override fun update(dt: Float) {
        pose.t += dt
        dark += ((if (lightOn) 0f else 1f) - dark) * min(1f, dt * 4f)

        if (Save.sleeping) {
            zT -= dt
            if (zT <= 0f) {
                zT = 1.2f
                fx.add(Fx(g.width * 0.58f, g.height * 0.45f, g.width * 0.04f, -g.height * 0.05f, 2.2f, -1, g.height * 0.035f))
            }
            if (Save.energy >= 100f) {
                // Acordar com energia cheia
                g.say("Energia a 100%! Bunny está descansado!")
            }
        }

        pose.mood = if (Save.sleeping) 3 else Save.mood()
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        Rooms.draw(c, r, 1, g.night, g.time)
        val w = r.width(); val h = r.height()

        // Cama fofinha
        val bed = RectF(r.left + w * 0.08f, Rooms.floorY(r) - h * 0.02f, r.right - w * 0.08f, Rooms.floorY(r) + h * 0.3f)
        D.rrect(c, bed.left, bed.top - h * 0.16f, bed.left + w * 0.12f, bed.bottom, w * 0.04f, C.peach, C.outline, 4f)
        D.rrect(c, bed, h * 0.04f, C.pink, C.outline, 4f)
        D.rrect(c, bed.left + w * 0.04f, bed.top - h * 0.04f, bed.left + w * 0.4f, bed.top + h * 0.06f, h * 0.04f, C.white, C.outline, 4f)

        val scale = Save.bunnyScale()
        val s = h * 0.25f * scale
        BunnyArt.draw(c, bed.left + w * 0.28f, bed.top + h * 0.12f, s, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)

        // Cobertor com estrelas
        D.rrect(c, bed.left + w * 0.12f, bed.top + h * 0.06f, bed.right - w * 0.02f, bed.bottom - h * 0.01f, h * 0.04f, C.lav, C.outline, 4f)
        for (i in 0 until 5) Icons.draw(c, Icons.STAR, bed.left + w * (0.25f + i * 0.14f), bed.top + h * 0.17f, h * 0.012f)

        // Escuridão / Modo noturno ao apagar a luz
        if (dark > 0.01f) {
            D.f(Color.argb((dark * 175).toInt(), 18, 14, 60))
            c.drawRect(r, D.p)
        }

        drawFx(c)

        // Interruptor / Candeeiro
        val rad = w * 0.075f
        lamp.set(r.right - w * 0.17f, r.top + h * 0.04f, r.right - w * 0.03f, r.top + h * 0.04f + w * 0.14f)
        D.circle(c, lamp.centerX(), lamp.centerY(), rad, if (lightOn) C.butter else C.white, C.outline, 3f)
        Icons.draw(c, Icons.LAMP, lamp.centerX(), lamp.centerY(), rad * 0.55f)
        D.text(c, if (lightOn) "Apagar luz" else "Acender luz", lamp.centerX(), lamp.bottom + h * 0.04f, h * 0.024f, if (dark > 0.5f) C.white else C.ink)

        // Temporizador de sono (2 minutos completos de 0 a 100%)
        if (Save.sleeping) {
            val remainSec = ((100f - Save.energy) / (100f / 120f)).toInt().coerceAtLeast(0)
            val min = remainSec / 60
            val sec = remainSec % 60
            val sleepTxt = if (remainSec > 0) "A dormir... (Faltam ${min}m ${sec}s para 100%)" else "Energia máxima atingida!"
            D.text(c, sleepTxt, r.centerX(), r.top + h * 0.09f, h * 0.032f, C.white)
            D.text(c, "Energia: ${Save.energy.toInt()}%", r.centerX(), r.top + h * 0.13f, h * 0.026f, C.butter)
        }
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (lamp.contains(x, y)) {
            lightOn = !lightOn
            Save.sleeping = !lightOn
            if (Save.sleeping) {
                Save.sleepStartTime = System.currentTimeMillis()
                g.say("Boa noite Bunny! Recupera em 2 minutos.")
            } else {
                g.say("Bom dia Bunny!")
            }
            Save.save()
        }
    }
}

// ================================================================= BRINCAR
class PlayScreen(g: BunnyView) : Screen(g) {
    private class Toy(var x: Float, var y: Float, val icon: Int, val name: String) {
        var vx = 0f; var vy = 0f
    }

    private val pose = Pose()
    private val hv = floatArrayOf(0f)
    private val toys = ArrayList<Toy>()
    private var taps = 0
    private var gy = 0f
    private var ready = false

    // Posições da Gatinha e do Bunny no quarto de brincar
    private var catX = 0f; private var catY = 0f; private var catS = 0f
    private var catPounce = 0f
    private var catPounceV = 0f

    // Botões para trocar ou adicionar brinquedos novos
    private val toyButtons = Array(5) { RectF() }
    private val toyIcons = intArrayOf(Icons.BALL, Icons.YARN, Icons.WAND, Icons.CARROT_PLUSH, Icons.CAR_TOY)
    private val toyNames = arrayOf("Bola", "Lã", "Peninha", "Peluche", "Carrinho")

    override fun update(dt: Float) {
        pose.t += dt
        hopStep(pose, dt, hv)

        if (catPounce > 0f || catPounceV != 0f) {
            catPounceV -= 8f * dt
            catPounce += catPounceV * dt
            if (catPounce <= 0f) { catPounce = 0f; catPounceV = 0f }
        }

        if (ready) {
            for (t in toys) {
                t.vy += g.height * 1.5f * dt
                t.x += t.vx * dt; t.y += t.vy * dt
                // Chão
                if (t.y > gy) {
                    t.y = gy
                    t.vy = -t.vy * 0.65f
                    if (abs(t.vy) < g.height * 0.08f) t.vy = 0f
                }
                // Paredes
                if (t.x < g.width * 0.08f || t.x > g.width * 0.92f) {
                    t.vx = -t.vx * 0.8f
                    t.x = t.x.coerceIn(g.width * 0.08f, g.width * 0.92f)
                }
                t.vx *= 0.992f

                // Reação da Gatinha aos brinquedos próximos
                if (Save.hasCat && hypot(t.x - catX, t.y - (catY - catS * 0.4f)) < catS * 0.85f && abs(t.vy) > 10f) {
                    catPounceV = 2.4f; catPounce = 0.01f
                    t.vx = (t.x - catX) * 4f + (Random.nextFloat() - 0.5f) * g.width * 0.3f
                    t.vy = -g.height * 0.65f
                    burst(catX, catY - catS * 0.7f, Icons.SPARK, 3, catS * 0.08f)
                    Save.joy = min(100f, Save.joy + 0.8f)
                }
            }
        }
        pose.mood = 0
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        Rooms.draw(c, r, 0, g.night, g.time)
        gy = Rooms.floorY(r) + r.height() * 0.28f

        if (!ready) {
            toys.add(Toy(r.width() * 0.32f, r.top + r.height() * 0.25f, Icons.BALL, "Bola"))
            toys.add(Toy(r.width() * 0.68f, r.top + r.height() * 0.20f, Icons.YARN, "Lã"))
            ready = true
        }

        val scale = Save.bunnyScale()
        val s = r.height() * 0.30f * scale
        val bx = r.centerX() + r.width() * 0.12f
        val by = Rooms.floorY(r) + r.height() * 0.20f
        BunnyArt.draw(c, bx, by, s, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)

        // Gatinha fofinha no quarto de brincar
        if (Save.hasCat) {
            catS = r.height() * 0.20f
            catX = r.left + r.width() * 0.22f
            catY = by
            CatArt.draw(c, catX, catY, catS, pose.t, mood = 0, pounce = catPounce)
        }

        // Desenha todos os brinquedos no ecrã
        for (t in toys) {
            Icons.draw(c, t.icon, t.x, t.y, r.width() * 0.075f)
        }

        // Barra inferior para escolher e lançar brinquedos novos
        val barH = r.height() * 0.09f
        val bw = r.width() / 5f
        for (i in 0 until 5) {
            toyButtons[i].set(r.left + i * bw + 4f, r.bottom - barH, r.left + (i + 1) * bw - 4f, r.bottom - 4f)
            D.rrect(c, toyButtons[i], barH * 0.35f, C.white, C.outline, 3f)
            Icons.draw(c, toyIcons[i], toyButtons[i].centerX(), toyButtons[i].top + barH * 0.38f, barH * 0.24f)
            D.text(c, toyNames[i], toyButtons[i].centerX(), toyButtons[i].bottom - barH * 0.14f, barH * 0.18f, C.ink)
        }

        D.text(c, "Toca nos brinquedos ou escolhe em baixo!", r.centerX(), r.bottom - barH - r.height() * 0.015f, r.height() * 0.026f, C.ink)

        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        // Toca num botão de brinquedo em baixo para adicionar/lançar
        for (i in 0 until 5) {
            if (toyButtons[i].contains(x, y)) {
                val newToy = Toy(r.centerX() + (Random.nextFloat() - 0.5f) * r.width() * 0.4f, r.top + r.height() * 0.18f, toyIcons[i], toyNames[i])
                newToy.vy = -g.height * 0.4f
                newToy.vx = (Random.nextFloat() - 0.5f) * g.width * 0.5f
                if (toys.size >= 4) toys.removeAt(0)
                toys.add(newToy)
                burst(newToy.x, newToy.y, Icons.SPARK, 4, r.width() * 0.03f)
                hopStart(pose, hv, 2.0f)
                g.say("Novo brinquedo: ${toyNames[i]}!")
                return
            }
        }

        // Toca num brinquedo existente para o atirar / chutar
        for (t in toys) {
            if (hypot(x - t.x, y - t.y) < r.width() * 0.13f) {
                t.vy = -g.height * 0.95f
                t.vx = (t.x - x) * 6f + (Random.nextFloat() - 0.5f) * g.width * 0.4f
                Save.joy = min(100f, Save.joy + 1.5f)
                taps++
                hopStart(pose, hv, 2.2f)
                burst(t.x, t.y, Icons.SPARK, 3, r.width() * 0.02f)
                if (taps % 8 == 0) {
                    g.earn(2)
                    burst(r.centerX(), r.centerY(), Icons.HEART, 5, r.width() * 0.03f)
                }
                break
            }
        }
    }
}
