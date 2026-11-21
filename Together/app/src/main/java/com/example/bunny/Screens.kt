package com.example.bunny

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
    private val btns = Array(4) { RectF() }
    private val btnIcons = intArrayOf(Icons.BAG, Icons.HAT, Icons.HOUSE, Icons.WIDGET)
    private val btnNames = arrayOf("Loja", "Roupas", "Decorar", "Widgets")
    private val multiBtn = RectF()
    private val settBtn = RectF()
    private val craftBtn = RectF()
    private val kartBtn = RectF()
    private val petPillBtn = RectF()
    private val vitalBtns = Array(3) { RectF() }
    private var bx = 0f; private var by = 0f; private var bs = 0f

    // Companheiro(a)
    private var compX = 0f; private var compY = 0f; private var compS = 0f
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

        // 1. Pílula Superior de Seleção de Pet (Figma Screen 2)
        val pillH = r.height() * 0.040f
        val petEmoji = if (Theme.isBunny) "🐰" else "🐱"
        val pillText = "$petEmoji ${Save.currentPetName()} ▾  ·  Lv. ${Save.level()}"
        val pillTxtSize = r.height() * 0.016f
        val pillW = D.textW(pillText, pillTxtSize) + r.width() * 0.09f
        val pillX = r.centerX() - pillW / 2f
        val pillY = r.top + r.height() * 0.048f
        petPillBtn.set(pillX, pillY, pillX + pillW, pillY + pillH)
        val pillBg = if (Theme.isBunny) Theme.bNavyLight else Color.WHITE
        val pillStroke = if (Theme.isBunny) Theme.bWine else Color.rgb(255, 205, 200)
        D.rrect(c, petPillBtn, pillH * 0.5f, pillBg, pillStroke, 2.5f)
        val pillTxtCol = if (Theme.isBunny) Theme.bSilver else Theme.text
        D.text(c, pillText, petPillBtn.centerX(), pillY + pillH * 0.65f, pillTxtSize, pillTxtCol)

        // Badges Laterais Figma: Streak Flame à esquerda e Corações/Moedas à direita
        val badgeH = pillH * 0.88f
        val flameX = r.left + r.width() * 0.11f
        val flameW = r.width() * 0.16f
        D.rrect(c, flameX - flameW / 2f, pillY, flameX + flameW / 2f, pillY + badgeH, badgeH * 0.5f, pillBg, pillStroke, 2f)
        Icons.draw(c, Icons.FLAME, flameX - flameW * 0.28f, pillY + badgeH * 0.5f, badgeH * 0.32f)
        D.text(c, "14", flameX + flameW * 0.18f, pillY + badgeH * 0.66f, badgeH * 0.50f, pillTxtCol)

        val heartBadgeX = r.right - r.width() * 0.11f
        val heartBadgeW = r.width() * 0.18f
        D.rrect(c, heartBadgeX - heartBadgeW / 2f, pillY, heartBadgeX + heartBadgeW / 2f, pillY + badgeH, badgeH * 0.5f, pillBg, pillStroke, 2f)
        Icons.draw(c, Icons.HEART, heartBadgeX - heartBadgeW * 0.28f, pillY + badgeH * 0.5f, badgeH * 0.32f)
        D.text(c, "${Save.coins}", heartBadgeX + heartBadgeW * 0.18f, pillY + badgeH * 0.66f, badgeH * 0.50f, pillTxtCol)

        pose.mood = Save.mood()
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        val breathe = sin(pose.t * 2f) * 0.02f
        pose.squash += breathe

        // Renderiza o Pet Ativo no Centro
        if (Theme.isBunny) {
            BunnyArt.draw(c, bx, by, bs, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        } else {
            CatArt.draw(c, bx, by, bs, pose.t, mood = pose.mood, pounce = pose.hop)
        }
        pose.squash -= breathe

        // Renderiza o Companheiro ao Lado
        if (Save.hasCat) {
            compS = r.height() * 0.20f
            compX = bx - bs * 0.55f - compS * 0.35f
            compY = by
            if (Theme.isBunny) {
                // No Bunny: companheira é a Gatinha
                CatArt.draw(c, compX, compY, compS, pose.t, mood = if (Save.catHappiness > 60f) 0 else 1, pounce = catPounce, hat = Save.catHat, outfit = Save.catOutfit, shoes = Save.catShoes, color = Save.catColor, acc = Save.catAcc)
            } else {
                // Na Cat: companheiro é o Bunny
                BunnyArt.draw(c, compX, compY, compS, pose, Save.bunnyHat, Save.bunnyOutfit, Save.bunnyAcc, Save.bunnyShoes, Save.bunnyColor)
            }
        }

        // Balão de fala estilo Pou quando alguma necessidade está baixa
        val w = Save.worst()
        if (w.second < 40f) {
            val txt = arrayOf("Tenho fome!", "Preciso de banho!", "Estou com sono...", "Vamos brincar?")[w.first]
            val ic = intArrayOf(Icons.CARROT, Icons.BUBBLE, Icons.MOON, Icons.BALL)[w.first]
            val bTxtSize = r.height() * 0.018f
            val tw = D.textW(txt, bTxtSize) + r.height() * 0.06f
            val x0 = bx + bs * 0.35f; val y0 = by - bs * 1.35f
            val bubbleFill = if (Theme.isBunny) Theme.bNavyLight else Theme.cWhitePure
            val bubbleTxt = if (Theme.isBunny) Theme.bSilver else Theme.text
            D.rrect(c, x0 - tw * 0.1f, y0 - r.height() * 0.022f, x0 - tw * 0.1f + tw, y0 + r.height() * 0.025f, r.height() * 0.02f, bubbleFill, Theme.outline, 2.5f)
            Icons.draw(c, ic, x0 - tw * 0.1f + r.height() * 0.022f, y0 + r.height() * 0.002f, r.height() * 0.015f)
            D.text(c, txt, x0 - tw * 0.1f + tw * 0.58f, y0 + r.height() * 0.007f, bTxtSize, bubbleTxt)
        }

        // Botões de Acesso à Direita (Loja, Roupas, Decorar, Widgets)
        val rad = r.width() * 0.062f
        val btnFill = if (Theme.isBunny) Theme.bNavyLight else Theme.cWhitePure
        val btnTxt = if (Theme.isBunny) Theme.bSilver else Theme.text
        for (i in 0 until 4) {
            val cx = r.right - r.width() * 0.11f
            val cy = r.top + r.height() * 0.16f + i * r.height() * 0.108f
            btns[i].set(cx - rad, cy - rad, cx + rad, cy + rad)
            val fillCol = if (i == 3) (if (Theme.isBunny) Theme.bWine else Color.rgb(255, 125, 145)) else btnFill
            D.circle(c, cx, cy, rad, fillCol, Theme.outline, 2.5f)
            Icons.draw(c, btnIcons[i], cx, cy - rad * 0.08f, rad * 0.52f)
            val txtCol = if (i == 3) Color.WHITE else btnTxt
            D.text(c, btnNames[i], cx, cy + rad * 1.32f, rad * 0.32f, txtCol)
        }

        // Botões de Acesso à Esquerda (Multiplayer, Definições, Craft 3D e TogetherKart 3D)
        val lcx = r.left + r.width() * 0.11f
        val multiCy = r.top + r.height() * 0.16f
        multiBtn.set(lcx - rad, multiCy - rad, lcx + rad, multiCy + rad)
        D.circle(c, lcx, multiCy, rad, if (Save.partnerConnected) Theme.primary else btnFill, Theme.outline, 2.5f)
        Icons.draw(c, Icons.MULTIPLAYER, lcx, multiCy - rad * 0.08f, rad * 0.52f)
        D.text(c, "A Dois", lcx, multiCy + rad * 1.32f, rad * 0.32f, btnTxt)

        val settCy = multiCy + r.height() * 0.108f
        settBtn.set(lcx - rad, settCy - rad, lcx + rad, settCy + rad)
        D.circle(c, lcx, settCy, rad, btnFill, Theme.outline, 2.5f)
        Icons.draw(c, Icons.SETTINGS, lcx, settCy - rad * 0.08f, rad * 0.52f)
        D.text(c, "Ajustes", lcx, settCy + rad * 1.32f, rad * 0.32f, btnTxt)

        // Botão direto para CatBunnyCraft 3D
        val craftCy = settCy + r.height() * 0.108f
        craftBtn.set(lcx - rad, craftCy - rad, lcx + rad, craftCy + rad)
        D.circle(c, lcx, craftCy, rad, btnFill, Theme.outline, 2.5f)
        Icons.draw(c, Icons.CRAFT, lcx, craftCy - rad * 0.08f, rad * 0.52f)
        D.text(c, "Craft ⛏️", lcx, craftCy + rad * 1.32f, rad * 0.32f, btnTxt)

        // Botão direto para TogetherKart 3D em Landscape
        val kartCy = craftCy + r.height() * 0.108f
        kartBtn.set(lcx - rad, kartCy - rad, lcx + rad, kartCy + rad)
        val kartCol = if (Theme.isBunny) Theme.bWine else Theme.primary
        D.circle(c, lcx, kartCy, rad, kartCol, Theme.outline, 2.5f)
        Icons.draw(c, Icons.STAR, lcx, kartCy - rad * 0.08f, rad * 0.52f)
        D.text(c, "Kart 🏎️", lcx, kartCy + rad * 1.32f, rad * 0.32f, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure)

        // 5. Mini Barra de Vitais Figma no Fundo da Sala (Fome, Humor, Energia)
        val vY = r.bottom - r.height() * 0.075f
        val vTotalW = r.width() * 0.82f
        val vCardW = (vTotalW - r.width() * 0.03f) / 3f
        val vCardH = r.height() * 0.042f
        val vLeft = r.centerX() - vTotalW / 2f
        val vIcons = intArrayOf(Icons.CARROT, Icons.HEART, Icons.LIGHTNING)
        val vVals = intArrayOf(Save.hunger.toInt(), Save.joy.toInt(), Save.energy.toInt())
        val vCols = if (Theme.isBunny) {
            intArrayOf(Color.rgb(42, 22, 54), Color.rgb(58, 20, 42), Color.rgb(24, 38, 64))
        } else {
            intArrayOf(Color.rgb(255, 240, 235), Color.rgb(255, 234, 240), Color.rgb(232, 248, 242))
        }
        for (i in 0 until 3) {
            val vx = vLeft + i * (vCardW + r.width() * 0.015f)
            vitalBtns[i].set(vx, vY, vx + vCardW, vY + vCardH)
            D.rrect(c, vitalBtns[i], vCardH * 0.5f, vCols[i], pillStroke, 1.8f)
            Icons.draw(c, vIcons[i], vx + vCardH * 0.48f, vY + vCardH * 0.50f, vCardH * 0.28f)
            D.text(c, "${vVals[i]}%", vx + vCardW * 0.68f, vY + vCardH * 0.65f, vCardH * 0.44f, pillTxtCol)
        }

        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (petPillBtn.contains(x, y)) { g.go(Nav.PROFILE); return }
        for (i in 0 until 3) if (vitalBtns[i].contains(x, y)) { g.go(Nav.PROFILE); return }
        for (i in 0 until 3) if (btns[i].contains(x, y)) { g.go(Nav.SHOP + i); return }
        if (btns[3].contains(x, y)) { g.go(Nav.WIDGETS); return }
        if (multiBtn.contains(x, y)) { g.go(Nav.MULTIPLAYER); return }
        if (settBtn.contains(x, y)) { g.go(Nav.SETTINGS); return }
        if (craftBtn.contains(x, y)) { g.go(Nav.CRAFT); return }
        if (kartBtn.contains(x, y)) { g.go(Nav.KART); return }

        // Carinho no Pet Principal
        if (hypot(x - bx, y - (by - bs * 0.6f)) < bs * 0.6f) {
            hopStart(pose, hv)
            Save.joy = min(100f, Save.joy + 3f)
            burst(bx, by - bs * 1.1f, Icons.HEART, 4, bs * 0.07f)
            SoundFx.playPlay(Theme.isCat)
            val happyMsg = if (Theme.isBunny) "Bunny adorou o teu carinho! ♥" else "Miau! :3 A gatinha ronrona feliz!"
            g.say(happyMsg)
            return
        }

        // Carinho no Companheiro
        if (Save.hasCat && hypot(x - compX, y - (compY - compS * 0.5f)) < compS * 0.6f) {
            catPounceV = 2.2f; catPounce = 0.01f
            Save.catHappiness = min(100f, Save.catHappiness + 8f)
            Save.joy = min(100f, Save.joy + 2f)
            burst(compX, compY - compS * 0.9f, Icons.HEART, 4, compS * 0.08f)
            SoundFx.playPlay(!Theme.isCat)
            val compMsg = if (Theme.isBunny) "Miau! A gatinha adorou brincar contigo!" else "Bunny deu um pulinho de alegria!"
            g.say(compMsg)
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

        if (Theme.isBunny) {
            BunnyArt.draw(c, bx, by, s, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        } else {
            CatArt.draw(c, bx, by, s, pose.t, mood = pose.mood, dirt = pose.dirt, mouth = pose.mouth)
        }
        hx = bx; hy = by - s * 0.72f; hr = s * 0.5f

        // Bandeja de comida na cozinha
        val tray = RectF(r.left + r.width() * 0.04f, r.bottom - r.height() * 0.19f, r.right - r.width() * 0.04f, r.bottom - r.height() * 0.02f)
        D.rrect(c, tray, tray.height() * 0.25f, Theme.card, Theme.outline, 4f)

        // Botões de navegação da bandeja
        val btnW = tray.width() * 0.10f
        prevBtn.set(tray.left + 8f, tray.top + 8f, tray.left + btnW, tray.bottom - 8f)
        nextBtn.set(tray.right - btnW, tray.top + 8f, tray.right - 8f, tray.bottom - 8f)
        val navBtnFill = if (Theme.isBunny) Theme.bNavyLight else Theme.cWhitePure
        val navBtnTxt = if (Theme.isBunny) Theme.bSilver else Theme.text
        D.button(c, prevBtn, "<", navBtnFill, navBtnTxt, prevBtn.height() * 0.4f)
        D.button(c, nextBtn, ">", navBtnFill, navBtnTxt, nextBtn.height() * 0.4f)

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
                    val slotFill = if (Theme.isBunny) Theme.bNavyLight else Theme.cWhitePure
                    D.rrect(c, slots[i], slots[i].height() * 0.18f, slotFill, Theme.outline, 2f)
                    // Conforme solicitado: o nome do alimento NÃO aparece na bandeja de dar comida (apenas na loja)!
                    Icons.draw(c, item.icon, slots[i].centerX(), slots[i].centerY(), slots[i].height() * 0.35f)
                }
            }
        }

        D.text(c, "Arrasta a comida até à boca do pet!", r.centerX(), tray.top - r.height() * 0.015f, r.height() * 0.026f, navBtnTxt)

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
                SoundFx.playEat(Theme.isCat)
                val isFavFood = Save.activeFavFood.isNotEmpty() && food.name.contains(Save.activeFavFood.substringBefore(" "), ignoreCase = true)
                val bonusJoy = if (isFavFood) 15f else 0f
                Save.hunger = min(100f, Save.hunger + food.nutri)
                Save.joy = min(100f, Save.joy + food.joy + bonusJoy)
                eatT = 1.3f
                burst(hx, hy - hr * 0.5f, Icons.HEART, if (isFavFood) 10 else 5, hr * 0.15f)
                val msg = if (isFavFood) {
                    "É a minha comida favorita da Ficha! ♥ (+15 Alegria extra!)"
                } else {
                    when (food.icon) {
                        Icons.CEVICHE -> "Nham nham! Ceviche peruano delicioso!"
                        Icons.LOMO_SALTADO -> "Uau! Lomo Saltado saboroso!"
                        Icons.CHICHA_MORADA -> "Que chicha morada fresquinha!"
                        Icons.ALFAJOR -> "Alfajor andino docinho!"
                        Icons.CAUSA -> "Causa Limeña cremosa!"
                        else -> "Nham nham! Que bom!"
                    }
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
                g.say("${Save.currentPetName()} ficou cheiroso(a) e feliz!")
            }
        }
        pose.mood = if (Save.mood() == 3) 0 else Save.mood()
        pose.foam = foam
        pose.dirt = ((60f - Save.hygiene) / 60f).coerceIn(0f, 1f)
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        // Casa de Banho decorada pelo utilizador (Divisão 3)
        Rooms.draw(c, r, 3, g.night, g.time, false)

        val scale = Save.bunnyScale()
        bs = r.height() * 0.30f * scale
        bx = r.centerX()
        by = r.top + r.height() * 0.66f

        // Banheira (fundo)
        val tubFill = if (Theme.isBunny) Theme.bNavyLight else Theme.cWhitePure
        val tub = RectF(r.left + r.width() * 0.08f, r.top + r.height() * 0.55f, r.right - r.width() * 0.08f, r.top + r.height() * 0.88f)
        D.rrect(c, tub, tub.height() * 0.35f, tubFill, Theme.outline, 5f)

        if (Theme.isBunny) {
            BunnyArt.draw(c, bx, by, bs, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        } else {
            CatArt.draw(c, bx, by, bs, pose.t, mood = pose.mood, dirt = pose.dirt, foam = pose.foam)
        }

        // Água e rebordo da banheira
        val waterFill = if (Theme.isBunny) Theme.bBlueSky else Theme.cPinkSoft
        val water = RectF(tub.left + 6f, by - bs * 0.15f, tub.right - 6f, tub.bottom - 6f)
        D.rrect(c, water, tub.height() * 0.3f, D.alpha(waterFill, 190))
        D.rrect(c, tub.left, tub.top + tub.height() * 0.38f, tub.right, tub.top + tub.height() * 0.5f, tub.height() * 0.06f, tubFill, Theme.outline, 4f)

        // =================================================================
        // CHUVEIRO: NO CENTRO EM CIMA DA CABEÇA DO PET!
        // =================================================================
        val shW = r.width() * 0.26f
        val shH = r.height() * 0.15f
        shower.set(bx - shW / 2f, r.top + r.height() * 0.03f, bx + shW / 2f, r.top + r.height() * 0.03f + shH)

        // Cano que desce do teto até ao chuveiro no centro
        val pipeC = if (Theme.isBunny) Theme.bBlueRoyal else Color.rgb(230, 185, 205)
        D.line(c, bx, r.top, bx, shower.top + shH * 0.2f, 8f, pipeC)

        // Cabeça do chuveiro arredondada no centro
        val shFill = if (rinsing) (if (Theme.isBunny) Theme.bWine else Theme.cPinkDark) else tubFill
        D.circle(c, shower.centerX(), shower.centerY(), shower.height() * 0.48f, D.alpha(shFill, 235), Theme.outline, 3.5f)
        Icons.draw(c, Icons.SHOWER, shower.centerX(), shower.centerY(), shower.height() * 0.34f)
        val txtColor = if (Theme.isBunny) Theme.bSilver else Theme.text
        D.text(c, if (rinsing) "A enxaguar..." else "Mantém no chuveiro para enxaguar", bx, shower.bottom + r.height() * 0.035f, r.height() * 0.024f, txtColor)

        // Esponja de sabão
        if (!spongeOn) {
            spongeX = r.right - r.width() * 0.16f
            spongeY = r.top + r.height() * 0.38f
        }
        sponge.set(spongeX - bs * 0.28f, spongeY - bs * 0.18f, spongeX + bs * 0.28f, spongeY + bs * 0.18f)
        Icons.draw(c, Icons.SPONGE, spongeX, spongeY, bs * 0.26f)
        if (!spongeOn) D.text(c, "Esfrega com a esponja!", min(spongeX, r.right - r.width() * 0.18f), spongeY + bs * 0.38f, r.height() * 0.020f, txtColor)

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
            if (Random.nextFloat() < 0.12f) SoundFx.playBath(Theme.isCat)
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
                zT = 1.4f
                fx.add(Fx(g.width * 0.58f, g.height * 0.45f, g.width * 0.04f, -g.height * 0.05f, 2.2f, -1, g.height * 0.035f))
                SoundFx.playSleep(Theme.isCat)
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

        // Cama fofinha respeitando a paleta do personagem ativo
        val bed = RectF(r.left + w * 0.08f, Rooms.floorY(r) - h * 0.02f, r.right - w * 0.08f, Rooms.floorY(r) + h * 0.3f)
        val headFill = if (Theme.isBunny) Theme.bWineDark else Theme.cPinkCream
        val matFill = if (Theme.isBunny) Theme.bBlueIce else Theme.cPinkSoft
        val pilFill = if (Theme.isBunny) Theme.bBlueIce else Theme.cWhitePure

        D.rrect(c, bed.left, bed.top - h * 0.16f, bed.left + w * 0.12f, bed.bottom, w * 0.04f, headFill, Theme.outline, 4f)
        D.rrect(c, bed, h * 0.04f, matFill, Theme.outline, 4f)
        D.rrect(c, bed.left + w * 0.04f, bed.top - h * 0.04f, bed.left + w * 0.4f, bed.top + h * 0.06f, h * 0.04f, pilFill, Theme.outline, 4f)

        val scale = Save.bunnyScale()
        val s = h * 0.25f * scale
        val petX = bed.left + w * 0.28f
        val petY = bed.top + h * 0.12f
        if (Theme.isBunny) {
            BunnyArt.draw(c, petX, petY, s, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        } else {
            CatArt.draw(c, petX, petY, s, pose.t, mood = 3)
        }

        // Cobertor com estrelas
        val blanketFill = if (Theme.isBunny) Theme.bWine else Theme.cPinkCream
        val starFill = if (Theme.isBunny) Theme.bWineLight else C.gold
        D.rrect(c, bed.left + w * 0.12f, bed.top + h * 0.06f, bed.right - w * 0.02f, bed.bottom - h * 0.01f, h * 0.04f, blanketFill, Theme.outline, 4f)
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
        val lampFill = if (lightOn) C.butter else (if (Theme.isBunny) Theme.bWineDark else Theme.cWhitePure)
        D.circle(c, lamp.centerX(), lamp.centerY(), rad, lampFill, Theme.outline, 3f)
        Icons.draw(c, Icons.LAMP, lamp.centerX(), lamp.centerY(), rad * 0.55f)
        val txtCol = if (dark > 0.5f) (if (Theme.isBunny) Theme.bBlueIce else C.white) else Theme.text
        D.text(c, if (lightOn) "Apagar luz" else "Acender luz", lamp.centerX(), lamp.bottom + h * 0.04f, h * 0.024f, txtCol)

        // Temporizador de sono
        if (Save.sleeping) {
            val sleepRate = if (Save.activePersonality.contains("Soneca")) (100f / 80f) else (100f / 120f)
            val remainSec = ((100f - Save.energy) / sleepRate).toInt().coerceAtLeast(0)
            val min = remainSec / 60
            val sec = remainSec % 60
            val petName = Save.currentPetName()
            val sleepTxt = if (remainSec > 0) "$petName a dormir... (${min}m ${sec}s para 100%)" else "Energia a 100%!"
            D.text(c, sleepTxt, r.centerX(), r.top + h * 0.09f, h * 0.032f, if (Theme.isBunny) Theme.bBlueIce else C.white)
            D.text(c, "Energia: ${Save.energy.toInt()}%", r.centerX(), r.top + h * 0.13f, h * 0.026f, if (Theme.isBunny) Theme.bBlueSky else C.butter)
        }
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (lamp.contains(x, y)) {
            lightOn = !lightOn
            Save.sleeping = !lightOn
            val petName = Save.currentPetName()
            if (Save.sleeping) {
                Save.sleepStartTime = System.currentTimeMillis()
                SoundFx.playSleep(Theme.isCat)
                g.say("Boa noite $petName! Recupera em 2 minutos.")
            } else {
                g.say("Bom dia $petName!")
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

    // Posições dos personagens no quarto de brincar
    private var compX = 0f; private var compY = 0f; private var compS = 0f
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

                // Reação aos brinquedos próximos
                if (Save.hasCat && hypot(t.x - compX, t.y - (compY - compS * 0.4f)) < compS * 0.85f && abs(t.vy) > 10f) {
                    catPounceV = 2.4f; catPounce = 0.01f
                    t.vx = (t.x - compX) * 4f + (Random.nextFloat() - 0.5f) * g.width * 0.3f
                    t.vy = -g.height * 0.65f
                    burst(compX, compY - compS * 0.7f, Icons.SPARK, 3, compS * 0.08f)
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

        compS = r.height() * 0.20f
        compX = r.left + r.width() * 0.22f
        compY = by

        if (Theme.isBunny) {
            BunnyArt.draw(c, bx, by, s, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
            if (Save.hasCat) CatArt.draw(c, compX, compY, compS, pose.t, mood = 0, pounce = catPounce, hat = Save.catHat, outfit = Save.catOutfit, shoes = Save.catShoes, color = Save.catColor, acc = Save.catAcc)
        } else {
            CatArt.draw(c, bx, by, s, pose.t, mood = 0, pounce = pose.hop, hat = Save.hat, outfit = Save.outfit, shoes = Save.shoes, color = Save.petColor, acc = Save.acc)
            if (Save.hasCat) BunnyArt.draw(c, compX, compY, compS, pose, Save.bunnyHat, Save.bunnyOutfit, Save.bunnyAcc, Save.bunnyShoes, Save.bunnyColor)
        }

        // Desenha todos os brinquedos no ecrã
        for (t in toys) {
            Icons.draw(c, t.icon, t.x, t.y, r.width() * 0.075f)
        }

        // Barra inferior para escolher e lançar brinquedos novos
        val barH = r.height() * 0.09f
        val bw = r.width() / 5f
        val btnFill = if (Theme.isBunny) Theme.bNavyLight else Theme.cWhitePure
        val btnTxt = if (Theme.isBunny) Theme.bSilver else Theme.text
        for (i in 0 until 5) {
            toyButtons[i].set(r.left + i * bw + 4f, r.bottom - barH, r.left + (i + 1) * bw - 4f, r.bottom - 4f)
            D.rrect(c, toyButtons[i], barH * 0.35f, btnFill, Theme.outline, 3f)
            Icons.draw(c, toyIcons[i], toyButtons[i].centerX(), toyButtons[i].top + barH * 0.38f, barH * 0.24f)
            D.text(c, toyNames[i], toyButtons[i].centerX(), toyButtons[i].bottom - barH * 0.14f, barH * 0.18f, btnTxt)
        }

        D.text(c, "Toca nos brinquedos ou escolhe em baixo!", r.centerX(), r.bottom - barH - r.height() * 0.015f, r.height() * 0.026f, btnTxt)

        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        for (i in 0 until 5) {
            if (toyButtons[i].contains(x, y)) {
                val newToy = Toy(r.centerX() + (Random.nextFloat() - 0.5f) * r.width() * 0.4f, r.top + r.height() * 0.18f, toyIcons[i], toyNames[i])
                newToy.vy = -g.height * 0.4f
                newToy.vx = (Random.nextFloat() - 0.5f) * g.width * 0.5f
                if (toys.size >= 4) toys.removeAt(0)
                toys.add(newToy)
                SoundFx.playPlay(Theme.isCat)
                burst(newToy.x, newToy.y, Icons.SPARK, 4, r.width() * 0.03f)
                hopStart(pose, hv, 2.0f)
                g.say("Novo brinquedo: ${toyNames[i]}!")
                return
            }
        }

        for (t in toys) {
            if (hypot(x - t.x, y - t.y) < r.width() * 0.13f) {
                t.vy = -g.height * 0.95f
                t.vx = (t.x - x) * 6f + (Random.nextFloat() - 0.5f) * g.width * 0.4f
                Save.joy = min(100f, Save.joy + 1.5f)
                taps++
                SoundFx.playPlay(Theme.isCat)
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

// ================================================================= AUTENTICAÇÃO
class AuthScreen(g: BunnyView) : Screen(g) {
    override val fullBleed = true
    private var inputMode = 0 // 0: Nome, 1: Senha
    private var nameText = ""
    private var passText = ""
    private var animT = 0f

    private val langPtBtn = RectF()
    private val langEsBtn = RectF()
    private val settingsBtn = RectF()
    private val quickBunnyBtn = RectF()
    private val quickCatBtn = RectF()
    private val nameFieldBox = RectF()
    private val passFieldBox = RectF()
    private val loginActionBtn = RectF()
    private val keyBtns = Array(12) { RectF() }
    private val keyLabels = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Limpar", "0", "⌫")

    override fun update(dt: Float) {
        animT += dt
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        val bgCol = if (Theme.isBunny) Theme.bNavyDark else Theme.cPinkCream
        D.f(bgCol); c.drawRect(r, D.p)

        // Barra Superior: Idioma e Definições
        val barH = r.height() * 0.050f
        langPtBtn.set(r.left + r.width() * 0.06f, r.top + r.height() * 0.035f, r.left + r.width() * 0.30f, r.top + r.height() * 0.035f + barH)
        langEsBtn.set(langPtBtn.right + r.width() * 0.025f, langPtBtn.top, langPtBtn.right + r.width() * 0.265f, langPtBtn.bottom)
        settingsBtn.set(r.right - r.width() * 0.15f, langPtBtn.top, r.right - r.width() * 0.05f, langPtBtn.bottom)

        val activeLang = Save.language
        val langTxt = if (Theme.isBunny) Theme.bSilver else Theme.text
        D.button(c, langPtBtn, "🇵🇹 PT", if (activeLang == "pt") Theme.primary else Theme.card, langTxt, barH * 0.32f)
        D.button(c, langEsBtn, "🇪🇸 ES", if (activeLang == "es") Theme.primary else Theme.card, langTxt, barH * 0.32f)
        D.rrect(c, settingsBtn, barH * 0.35f, Theme.card, Theme.outline, 2f)
        Icons.draw(c, Icons.SETTINGS, settingsBtn.centerX(), settingsBtn.centerY(), barH * 0.32f)

        // Avatar animado de Coelhinho e Gatinha juntos com o logótipo oficial TOGETHER
        val petY = r.top + r.height() * 0.17f
        val petS = r.height() * 0.13f
        val floatHop = sin(animT * 2.5f) * 0.03f
        CatArt.draw(c, r.centerX() - r.width() * 0.18f, petY, petS, animT, mood = 0)
        val p = Pose().apply { squash = floatHop }
        BunnyArt.draw(c, r.centerX() + r.width() * 0.18f, petY, petS, p, "", "", "")
        Icons.drawTogetherLogo(c, r.centerX(), petY - petS * 0.35f, petS * 1.10f)

        // Título e Subtítulo
        D.text(c, I18n.t("app_title"), r.centerX(), petY + r.height() * 0.042f, r.height() * 0.024f, Theme.text)
        D.text(c, I18n.t("auth_sub"), r.centerX(), petY + r.height() * 0.070f, r.height() * 0.016f, Theme.textDim)

        // Campos de Texto / PIN Manual Misterioso (sem sugestões)
        val fH = r.height() * 0.052f
        val fY = petY + r.height() * 0.095f
        nameFieldBox.set(r.left + r.width() * 0.08f, fY, r.right - r.width() * 0.08f, fY + fH)
        passFieldBox.set(nameFieldBox.left, nameFieldBox.bottom + r.height() * 0.012f, nameFieldBox.right, nameFieldBox.bottom + r.height() * 0.012f + fH)

        // Campo Nome
        D.rrect(c, nameFieldBox, fH * 0.3f, Theme.card, if (inputMode == 0) Theme.primary else Theme.outline, if (inputMode == 0) 3f else 1.8f)
        val displayN = if (nameText.isEmpty()) "👤 " + I18n.t("user_label") else "👤 $nameText"
        D.text(c, displayN, nameFieldBox.centerX(), nameFieldBox.centerY() + fH * 0.18f, fH * 0.28f, if (nameText.isEmpty()) Theme.textDim else Theme.text)

        // Campo Senha
        D.rrect(c, passFieldBox, fH * 0.3f, Theme.card, if (inputMode == 1) Theme.primary else Theme.outline, if (inputMode == 1) 3f else 1.8f)
        val hiddenPass = if (passText.isEmpty()) "🔒 " + I18n.t("pass_label") else "🔒 " + "●".repeat(passText.length)
        D.text(c, hiddenPass, passFieldBox.centerX(), passFieldBox.centerY() + fH * 0.18f, fH * 0.28f, if (passText.isEmpty()) Theme.textDim else Theme.text)

        // Botão Entrar
        loginActionBtn.set(nameFieldBox.left, passFieldBox.bottom + r.height() * 0.016f, nameFieldBox.right, passFieldBox.bottom + r.height() * 0.016f + fH * 1.15f)
        D.button(c, loginActionBtn, "✨ ${I18n.t("login_btn")} ✨", Theme.primary, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure, fH * 0.32f)

        // Teclado numérico rápido para PIN
        val keyPadTop = loginActionBtn.bottom + r.height() * 0.014f
        val keyPadH = r.bottom - keyPadTop - r.height() * 0.014f
        val kw = (r.width() * 0.84f) / 3f
        val kh = keyPadH / 4f
        for (row in 0 until 4) {
            for (col in 0 until 3) {
                val idx = row * 3 + col
                val kx = r.left + r.width() * 0.08f + col * kw
                val ky = keyPadTop + row * kh
                keyBtns[idx].set(kx + 4f, ky + 3f, kx + kw - 4f, ky + kh - 3f)
                D.rrect(c, keyBtns[idx], kh * 0.32f, Theme.card, Theme.outline, 1.5f)
                D.text(c, keyLabels[idx], keyBtns[idx].centerX(), keyBtns[idx].centerY() + kh * 0.18f, kh * 0.32f, Theme.text)
            }
        }

        drawFx(c)
    }

    private fun tryLogin(targetName: String, targetPass: String) {
        val n = targetName.trim().lowercase()
        val p = targetPass.trim()
        if (n == "meu coelhinho" && p == "150225") {
            Save.activeChar = "bunny"
            Save.loggedIn = true
            Save.save()
            burst(g.width / 2f, g.height / 2f, Icons.HEART, 10, g.width * 0.04f)
            g.say("Bem-vindo, Meu Coelhinho! 🐰♥")
            if (!Save.reportDoneBunny) g.go(Nav.REPORT) else g.go(Nav.HOME)
        } else if (n == "minha gatinha" && p == "150225") {
            Save.activeChar = "cat"
            Save.loggedIn = true
            Save.save()
            burst(g.width / 2f, g.height / 2f, Icons.HEART, 10, g.width * 0.04f)
            g.say("Bem-vinda, Minha Gatinha! 🐱♥")
            if (!Save.reportDoneCat) g.go(Nav.REPORT) else g.go(Nav.HOME)
        } else {
            g.say(I18n.t("login_error"))
        }
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (langPtBtn.contains(x, y)) {
            Save.language = "pt"; Save.save(); g.say("Idioma: Português 🇵🇹"); return
        }
        if (langEsBtn.contains(x, y)) {
            Save.language = "es"; Save.save(); g.say("Idioma: Español 🇪🇸"); return
        }
        if (settingsBtn.contains(x, y)) {
            g.go(Nav.SETTINGS); return
        }
        if (nameFieldBox.contains(x, y)) {
            inputMode = 0
            val act = g.context as? android.app.Activity
            act?.runOnUiThread {
                val input = android.widget.EditText(g.context).apply {
                    setText(nameText)
                    hint = "Escreve aqui..."
                    isSingleLine = true
                }
                android.app.AlertDialog.Builder(g.context)
                    .setTitle(I18n.t("user_label"))
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        nameText = input.text.toString().trim()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            return
        }
        if (passFieldBox.contains(x, y)) {
            inputMode = 1; return
        }
        if (loginActionBtn.contains(x, y)) {
            tryLogin(nameText, passText); return
        }

        for (i in 0 until 12) {
            if (keyBtns[i].contains(x, y)) {
                val lbl = keyLabels[i]
                if (lbl == "Limpar") {
                    passText = ""
                } else if (lbl == "⌫") {
                    if (passText.isNotEmpty()) passText = passText.substring(0, passText.length - 1)
                } else {
                    if (passText.length < 8) passText += lbl
                }
                burst(keyBtns[i].centerX(), keyBtns[i].centerY(), Icons.SPARK, 2, r.width() * 0.02f)
                return
            }
        }
    }
}

// ================================================================= DEFINIÇÕES
class SettingsScreen(g: BunnyView) : Screen(g) {
    override val fullBleed = true
    private val backBtn = RectF()
    private val ptBtn = RectF()
    private val esBtn = RectF()
    private val soundBtn = RectF()
    private val reminderBtn = RectF()
    private val multiBtn = RectF()
    private val logoutBtn = RectF()
    private val editReportBtn = RectF()

    override fun update(dt: Float) {
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        D.f(Theme.bg); c.drawRect(r, D.p)

        val topH = r.height() * 0.065f
        backBtn.set(r.left + r.width() * 0.05f, r.top + r.height() * 0.035f, r.left + r.width() * 0.24f, r.top + r.height() * 0.035f + topH * 0.70f)
        D.button(c, backBtn, "← " + I18n.t("home"), Theme.card, Theme.text, backBtn.height() * 0.32f)

        D.text(c, I18n.t("settings"), r.centerX(), r.top + r.height() * 0.072f, r.height() * 0.026f, Theme.text)

        val cardH = r.height() * 0.065f
        var curY = r.top + r.height() * 0.125f
        val w = r.width() * 0.88f
        val left = r.left + r.width() * 0.06f

        // 1. Idioma
        D.text(c, I18n.t("language"), left + w * 0.5f, curY, r.height() * 0.018f, Theme.textDim)
        curY += r.height() * 0.022f
        ptBtn.set(left, curY, left + w * 0.48f, curY + cardH * 0.75f)
        esBtn.set(left + w * 0.52f, curY, left + w, curY + cardH * 0.75f)
        val langTxt = if (Theme.isBunny) Theme.bSilver else Theme.text
        D.button(c, ptBtn, "🇵🇹 Português", if (Save.language == "pt") Theme.primary else Theme.card, langTxt, ptBtn.height() * 0.30f)
        D.button(c, esBtn, "🇪🇸 Español", if (Save.language == "es") Theme.primary else Theme.card, langTxt, esBtn.height() * 0.30f)

        // 2. Som
        curY += cardH * 0.90f
        soundBtn.set(left, curY, left + w, curY + cardH * 0.75f)
        val sndTxt = if (Save.soundEnabled) "🔊 ${I18n.t("sound")}: Ligado" else "🔇 ${I18n.t("sound")}: Desligado"
        D.button(c, soundBtn, sndTxt, Theme.card, Theme.text, soundBtn.height() * 0.30f)

        // 3. Lembretes
        curY += cardH * 0.85f
        reminderBtn.set(left, curY, left + w, curY + cardH * 0.75f)
        val remTxt = if (Save.remindersEnabled) "🔔 ${I18n.t("reminders")}: Sim" else "🔕 ${I18n.t("reminders")}: Não"
        D.button(c, reminderBtn, remTxt, Theme.card, Theme.text, reminderBtn.height() * 0.30f)

        // 4. Ficha / Relatório do Pet
        curY += cardH * 0.85f
        editReportBtn.set(left, curY, left + w, curY + cardH * 0.75f)
        D.button(c, editReportBtn, "📝 Ver / Editar Ficha do Pet", Theme.cardAccent, Theme.text, editReportBtn.height() * 0.30f)

        // 5. Multiplayer Conexão
        curY += cardH * 0.85f
        multiBtn.set(left, curY, left + w, curY + cardH * 0.75f)
        D.button(c, multiBtn, "💑 ${I18n.t("multiplayer")} (Sala ${Save.roomCode})", Theme.primary, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure, multiBtn.height() * 0.30f)

        // 6. Logout
        curY += cardH * 0.90f
        logoutBtn.set(left, curY, left + w, curY + cardH * 0.75f)
        D.button(c, logoutBtn, "🚪 ${I18n.t("logout")}", if (Theme.isBunny) Theme.bWineDark else Theme.cPinkDark, Theme.cWhitePure, logoutBtn.height() * 0.30f)

        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (backBtn.contains(x, y)) {
            if (Save.loggedIn) g.go(Nav.HOME) else g.go(Nav.AUTH)
            return
        }
        if (ptBtn.contains(x, y)) {
            Save.language = "pt"; Save.save(); g.say("Idioma alterado para Português 🇵🇹"); return
        }
        if (esBtn.contains(x, y)) {
            Save.language = "es"; Save.save(); g.say("Idioma cambiado a Español 🇪🇸"); return
        }
        if (soundBtn.contains(x, y)) {
            Save.soundEnabled = !Save.soundEnabled; Save.save()
            g.say(if (Save.soundEnabled) "Som ativado!" else "Som desativado!")
            return
        }
        if (reminderBtn.contains(x, y)) {
            Save.remindersEnabled = !Save.remindersEnabled; Save.save()
            g.say(if (Save.remindersEnabled) "Lembretes ativados!" else "Lembretes pausados!")
            return
        }
        if (editReportBtn.contains(x, y)) {
            g.go(Nav.REPORT); return
        }
        if (multiBtn.contains(x, y)) {
            g.go(Nav.MULTIPLAYER); return
        }
        if (logoutBtn.contains(x, y)) {
            Save.loggedIn = false
            Save.save()
            g.say("Sessão terminada!")
            g.go(Nav.AUTH)
            return
        }
    }
}

// ================================================================= FICHA / RELATÓRIO
class ReportScreen(g: BunnyView) : Screen(g) {
    override val fullBleed = true
    private var page = 0
    private var isCelebrating = false
    private var celebrateT = 0f
    private var t = 0f
    private val pose = Pose()

    private var repName = if (Theme.isBunny) Save.bunnyName else Save.catName
    private var repLikes = if (Theme.isBunny) Save.bunnyLikes else Save.catLikes
    private var repDislikes = if (Theme.isBunny) Save.bunnyDislikes else Save.catDislikes
    private var repColor = if (Theme.isBunny) Save.bunnyFavColor else Save.catFavColor
    private var repFood = if (Theme.isBunny) Save.bunnyFavFood else Save.catFavFood
    private var repPersonality = if (Theme.isBunny) Save.bunnyPersonality else Save.catPersonality
    private var repSeason = if (Theme.isBunny) Save.bunnySeason else Save.catSeason
    private var repHobby = if (Theme.isBunny) Save.bunnyHobby else Save.catHobby
    private var repPower = if (Theme.isBunny) Save.bunnyPower else Save.catPower
    private var repMsg = if (Theme.isBunny) Save.bunnyMessage else Save.catMessage

    private val nameOptions = if (Theme.isBunny) {
        arrayOf("Meu Coelhinho 🐰", "O Meu Amor 💖", "Bunny Fofinho 🐾", "Meu Príncipe 👑")
    } else {
        arrayOf("Minha Gatinha 🐱", "A Minha Princesa 👑", "Gatinha Linda 💖", "Amorzinho Doce 🌸")
    }

    private val likesOptions = arrayOf(
        "Dar abraços e beijinhos quentinhos ♥",
        "Comer coisas deliciosas juntos 🥟",
        "Dormir abraçadinhos quentinhos 💤",
        "Ver filmes e séries no sofá 🎬",
        "Passear de mãos dadas ao sol 🌸"
    )

    private val dislikesOptions = arrayOf(
        "Ficar com saudades longe de ti 💔",
        "Tempo frio e chuvoso ❄",
        "Acordar cedo com pressa ⏰",
        "Esperar quando estou com pressa ⌛",
        "Discussões ou caras tristes 🥺"
    )

    // Cores estritamente respeitando as regras por personagem:
    // Bunny: azul, vinho, roxo (sem rosa, sem branco)
    // Cat: rosa, branco (sem azul, sem vinho, sem roxo)
    private val colorOptions = if (Theme.isBunny) {
        arrayOf("Azul Celestial 🌊", "Vinho Paixão 🍷", "Roxo Andino 🔮", "Azul Royal 🌌")
    } else {
        arrayOf("Rosa Algodão Doce 🌸", "Branco Nuvem ☁", "Rosa Choque 🎀", "Branco Pérola 🤍")
    }

    private val foodOptions = arrayOf(
        "Ceviche Peruano 🐟",
        "Alfajores com Doce de Leite 🍪",
        "Lomo Saltado Andino 🥩",
        "Chicha Morada com Canela 🍇",
        "Cenouras Docinhas Crocantes 🥕"
    )

    private val personalityOptions = if (Theme.isBunny) {
        arrayOf(
            "Doce & Carinhoso 🥰",
            "Brincalhão & Alegre 🐾",
            "Soneca & Aconchegante 💤",
            "Protetor & Fiel 🛡️",
            "Romântico Incurável 💖"
        )
    } else {
        arrayOf(
            "Doce & Carinhosa 🥰",
            "Brincalhona & Fofa 🐾",
            "Soneca & Aconchegante 💤",
            "Protetora & Fiel 🛡️",
            "Romântica Incurável 💖"
        )
    }

    private val seasonOptions = arrayOf(
        "Primavera Florida 🌸",
        "Verão Radiante ☀️",
        "Outono Dourado 🍂",
        "Inverno Aconchegante ❄️"
    )

    private val hobbyOptions = arrayOf(
        "Corridas no TogetherKart 🏎️",
        "Construir no CatBunnyCraft 🏰",
        "Filmes & Mimos no Sofá 🎬",
        "Passear sob as Estrelas ✨",
        "Cozinhar Jantares a Dois 🍲"
    )

    private val powerOptions = arrayOf(
        "Abraço Mágico que Cura Tudo 🫂",
        "Fazer o Outro Sorrir Sempre 😊",
        "Telepatia de Amor & Sintonia 🧠💖",
        "Amor Eterno sem Fim ♾️"
    )

    private val msgOptions = arrayOf(
        "Amo-te daqui até à lua e volta! ♥",
        "És o melhor que me aconteceu na vida! ✨",
        "O meu coração é para sempre teu! 💖",
        "Quero estar juntinho a ti para sempre! 💑"
    )

    private val optButtons = Array(5) { RectF() }
    private val nextBtn = RectF()
    private val prevBtn = RectF()
    private val finishBtn = RectF()

    override fun update(dt: Float) {
        t += dt
        pose.t += dt
        if (isCelebrating) {
            celebrateT -= dt
            pose.squash = sin(t * 12f) * 0.15f
            if (Random.nextFloat() < 0.6f) {
                burst(g.width * Random.nextFloat(), g.height * Random.nextFloat(), Icons.HEART, 4, g.width * 0.04f)
                burst(g.width * Random.nextFloat(), g.height * Random.nextFloat(), Icons.STAR, 4, g.width * 0.04f)
            }
            if (celebrateT <= 0f) {
                if (Theme.isBunny) {
                    Save.bunnyName = repName
                    Save.bunnyLikes = repLikes
                    Save.bunnyDislikes = repDislikes
                    Save.bunnyFavColor = repColor
                    Save.bunnyFavFood = repFood
                    Save.bunnyPersonality = repPersonality
                    Save.bunnySeason = repSeason
                    Save.bunnyHobby = repHobby
                    Save.bunnyPower = repPower
                    Save.bunnyMessage = repMsg
                    Save.reportDoneBunny = true
                } else {
                    Save.catName = repName
                    Save.catLikes = repLikes
                    Save.catDislikes = repDislikes
                    Save.catFavColor = repColor
                    Save.catFavFood = repFood
                    Save.catPersonality = repPersonality
                    Save.catSeason = repSeason
                    Save.catHobby = repHobby
                    Save.catPower = repPower
                    Save.catMessage = repMsg
                    Save.reportDoneCat = true
                }
                Save.save()
                Save.syncOnline()
                g.go(Nav.HOME)
            }
        }
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        D.f(Theme.bg); c.drawRect(r, D.p)

        if (isCelebrating) {
            val scale = if (Theme.isBunny) Save.bunnyScale() else 0.85f
            val s = r.height() * 0.28f * scale
            val bx = r.centerX()
            val by = r.centerY() + r.height() * 0.08f
            if (Theme.isBunny) {
                BunnyArt.draw(c, bx, by, s, pose, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
            } else {
                CatArt.draw(c, bx, by, s, t, mood = 0, pounce = abs(sin(t * 8f) * 0.4f))
            }

            val banW = r.width() * 0.86f
            val banH = r.height() * 0.12f
            val ban = RectF(r.centerX() - banW / 2f, r.top + r.height() * 0.18f, r.centerX() + banW / 2f, r.top + r.height() * 0.18f + banH)
            D.rrect(c, ban, banH * 0.3f, Theme.primary, Theme.outline, 4f)
            D.text(c, "✨ " + I18n.t("rep_done_anim") + " ✨", ban.centerX(), ban.centerY() + banH * 0.15f, banH * 0.32f, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure)
            D.text(c, "A entrar na tua casinha fofa...", r.centerX(), ban.bottom + r.height() * 0.05f, r.height() * 0.026f, Theme.textDim)

            drawFx(c)
            return
        }

        val topTitle = if (Theme.isBunny) I18n.t("report_title_bunny") else I18n.t("report_title_cat")
        D.text(c, topTitle, r.centerX(), r.top + r.height() * 0.060f, r.height() * 0.024f, Theme.text)
        D.text(c, I18n.t("report_sub"), r.centerX(), r.top + r.height() * 0.088f, r.height() * 0.016f, Theme.textDim)

        val indY = r.top + r.height() * 0.115f
        for (i in 0 until 10) {
            val ix = r.centerX() + (i - 4.5f) * r.width() * 0.046f
            val active = i == page
            D.circle(c, ix, indY, if (active) r.width() * 0.016f else r.width() * 0.010f, if (active) Theme.primary else Theme.card, Theme.outline, 1.6f)
        }

        val (qTitle, currentVal, curOptions) = when (page) {
            0 -> Triple(I18n.t("rep_name"), repName, nameOptions)
            1 -> Triple(I18n.t("rep_likes"), repLikes, likesOptions)
            2 -> Triple(I18n.t("rep_dislikes"), repDislikes, dislikesOptions)
            3 -> Triple(I18n.t("rep_color"), repColor, colorOptions)
            4 -> Triple(I18n.t("rep_food"), repFood, foodOptions)
            5 -> Triple(I18n.t("rep_personality"), repPersonality, personalityOptions)
            6 -> Triple(I18n.t("rep_season"), repSeason, seasonOptions)
            7 -> Triple(I18n.t("rep_hobby"), repHobby, hobbyOptions)
            8 -> Triple(I18n.t("rep_power"), repPower, powerOptions)
            else -> Triple(I18n.t("rep_msg"), repMsg, msgOptions)
        }

        val cardW = r.width() * 0.90f
        val cardH = r.height() * 0.095f
        val qCard = RectF(r.centerX() - cardW / 2f, r.top + r.height() * 0.138f, r.centerX() + cardW / 2f, r.top + r.height() * 0.138f + cardH)
        D.rrect(c, qCard, cardH * 0.22f, Theme.card, Theme.outline, 2.5f)
        D.text(c, qTitle, qCard.centerX(), qCard.top + cardH * 0.35f, cardH * 0.22f, Theme.textDim)
        D.text(c, currentVal, qCard.centerX(), qCard.bottom - cardH * 0.24f, cardH * 0.26f, Theme.text)

        val optTop = qCard.bottom + r.height() * 0.014f
        val optH = r.height() * 0.058f
        val nOpts = curOptions.size
        for (i in 0 until nOpts) {
            val oy = optTop + i * (optH + r.height() * 0.009f)
            optButtons[i].set(r.left + r.width() * 0.06f, oy, r.right - r.width() * 0.06f, oy + optH)
            val isSelected = currentVal == curOptions[i]
            val fill = if (isSelected) Theme.primary else Theme.card
            val txtC = if (isSelected) (if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure) else Theme.text
            D.button(c, optButtons[i], curOptions[i], fill, txtC, optH * 0.26f)
        }

        val navY = r.bottom - r.height() * 0.088f
        val navH = r.height() * 0.052f

        if (page > 0) {
            prevBtn.set(r.left + r.width() * 0.06f, navY, r.left + r.width() * 0.32f, navY + navH)
            D.button(c, prevBtn, "← Anterior", Theme.card, Theme.text, navH * 0.30f)
        } else {
            prevBtn.setEmpty()
        }

        if (page < 9) {
            nextBtn.set(r.right - r.width() * 0.36f, navY, r.right - r.width() * 0.06f, navY + navH)
            D.button(c, nextBtn, "Seguinte →", Theme.primary, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure, navH * 0.30f)
            finishBtn.setEmpty()
        } else {
            nextBtn.setEmpty()
            finishBtn.set(r.right - r.width() * 0.60f, navY, r.right - r.width() * 0.06f, navY + navH)
            D.button(c, finishBtn, I18n.t("rep_finish"), Theme.primary, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure, navH * 0.30f)
        }

        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (isCelebrating) return

        val curOptions = when (page) {
            0 -> nameOptions; 1 -> likesOptions; 2 -> dislikesOptions
            3 -> colorOptions; 4 -> foodOptions; 5 -> personalityOptions
            6 -> seasonOptions; 7 -> hobbyOptions; 8 -> powerOptions
            else -> msgOptions
        }
        for (i in 0 until curOptions.size) {
            if (optButtons[i].contains(x, y)) {
                when (page) {
                    0 -> repName = curOptions[i]
                    1 -> repLikes = curOptions[i]
                    2 -> repDislikes = curOptions[i]
                    3 -> repColor = curOptions[i]
                    4 -> repFood = curOptions[i]
                    5 -> repPersonality = curOptions[i]
                    6 -> repSeason = curOptions[i]
                    7 -> repHobby = curOptions[i]
                    8 -> repPower = curOptions[i]
                    9 -> repMsg = curOptions[i]
                }
                burst(x, y, Icons.HEART, 4, r.width() * 0.03f)
                return
            }
        }

        if (prevBtn.contains(x, y)) {
            page = max(0, page - 1); return
        }
        if (nextBtn.contains(x, y)) {
            page = min(9, page + 1); return
        }
        if (finishBtn.contains(x, y)) {
            isCelebrating = true
            celebrateT = 2.4f
            burst(r.centerX(), r.centerY(), Icons.HEART, 12, r.width() * 0.05f)
            burst(r.centerX(), r.centerY(), Icons.STAR, 10, r.width() * 0.05f)
        }
    }
}

// ================================================================= MULTIPLAYER / AMOR A DOIS
class MultiplayerScreen(g: BunnyView) : Screen(g) {
    private var t = 0f
    private val poseB = Pose()
    private val backBtn = RectF()
    private val craftGameBtn = RectF()
    private val kartGameBtn = RectF()
    private val sendKissBtn = RectF()
    private val sendFoodBtn = RectF()
    private val sendLetterBtn = RectF()
    private val copyCodeBtn = RectF()
    private val shareAppBtn = RectF()
    private val pasteCodeBtn = RectF()
    private val syncCloudBtn = RectF()

    override fun update(dt: Float) {
        t += dt
        poseB.t += dt
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        D.f(Theme.bg); c.drawRect(r, D.p)

        backBtn.set(r.left + r.width() * 0.05f, r.top + r.height() * 0.032f, r.left + r.width() * 0.22f, r.top + r.height() * 0.032f + r.height() * 0.045f)
        D.button(c, backBtn, "← " + I18n.t("home"), Theme.card, Theme.text, backBtn.height() * 0.32f)

        D.text(c, I18n.t("multiplayer"), r.centerX(), r.top + r.height() * 0.068f, r.height() * 0.024f, Theme.text)

        // Desenha Coelhinho e Gatinha juntos
        val petY = r.top + r.height() * 0.18f
        val petS = r.height() * 0.14f
        val catX = r.centerX() - r.width() * 0.16f
        val bunX = r.centerX() + r.width() * 0.16f

        CatArt.draw(c, catX, petY, petS, t, mood = 0)
        BunnyArt.draw(c, bunX, petY, petS, poseB, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        Icons.draw(c, Icons.HEART, r.centerX(), petY - petS * 0.50f, petS * 0.28f)

        // Estado da Ligação
        val statusText = if (Save.partnerConnected) {
            "♥ ${I18n.t("partner_conn")} (${Save.partnerName} · Nv. ${Save.partnerLvl})"
        } else {
            I18n.t("partner_wait")
        }
        D.text(c, statusText, r.centerX(), petY + r.height() * 0.038f, r.height() * 0.018f, if (Save.partnerConnected) Theme.primary else Theme.textDim)

        // Cartão com Resumo do Parceiro
        val cardW = r.width() * 0.90f
        val cardH = r.height() * 0.13f
        val partnerCard = RectF(r.centerX() - cardW / 2f, petY + r.height() * 0.055f, r.centerX() + cardW / 2f, petY + r.height() * 0.055f + cardH)
        D.rrect(c, partnerCard, cardH * 0.2f, Theme.card, Theme.outline, 2f)

        if (Save.partnerConnected) {
            val pLikes = if (Save.partnerLikes.isNotEmpty()) Save.partnerLikes else "Estar juntinhos"
            val pFood = if (Save.partnerFavFood.isNotEmpty()) Save.partnerFavFood else "Alfajor Peruano"
            val pPerson = if (Save.partnerPersonality.isNotEmpty()) Save.partnerPersonality else "Doce 🥰"
            val pPower = if (Save.partnerPower.isNotEmpty()) Save.partnerPower else "Abraço Mágico 🫂"
            val pMsg = if (Save.partnerMessage.isNotEmpty()) Save.partnerMessage else "Amo-te muito! ♥"
            D.text(c, "💌 Ficha de ${Save.partnerName} ($pPerson)", partnerCard.centerX(), partnerCard.top + cardH * 0.20f, cardH * 0.18f, Theme.primary)
            D.text(c, "Poder: $pPower · Gosta: $pLikes", partnerCard.centerX(), partnerCard.top + cardH * 0.48f, cardH * 0.14f, Theme.text)
            D.text(c, "\"$pMsg\"", partnerCard.centerX(), partnerCard.top + cardH * 0.76f, cardH * 0.15f, if (Theme.isBunny) Theme.bSilver else Theme.text)
        } else {
            D.text(c, "Partilha o teu código ou cola o do parceiro", partnerCard.centerX(), partnerCard.top + cardH * 0.38f, cardH * 0.17f, Theme.text)
            D.text(c, "para conectarem os dois telemóveis à distância!", partnerCard.centerX(), partnerCard.top + cardH * 0.66f, cardH * 0.15f, Theme.textDim)
        }

        // Cartão em destaque para o jogo estilo Minecraft: CatBunnyCraft em Machu Picchu
        val craftH = r.height() * 0.055f
        val craftY = partnerCard.bottom + r.height() * 0.008f
        craftGameBtn.set(partnerCard.left, craftY, partnerCard.right, craftY + craftH)
        val craftFill = if (Theme.isBunny) Color.rgb(26, 36, 68) else Color.rgb(255, 235, 244)
        val craftLine = if (Theme.isBunny) Color.rgb(255, 205, 45) else Color.rgb(222, 72, 122)
        D.rrect(c, craftGameBtn, craftH * 0.35f, craftFill, craftLine, 2.5f)
        Icons.draw(c, Icons.CRAFT, craftGameBtn.left + craftH * 0.65f, craftGameBtn.centerY(), craftH * 0.36f)
        val craftTitleCol = if (Theme.isBunny) Color.rgb(255, 225, 80) else Color.rgb(222, 72, 122)
        D.text(c, I18n.t("catbunnycraft"), craftGameBtn.centerX() + craftH * 0.25f, craftGameBtn.centerY() - craftH * 0.10f, craftH * 0.32f, craftTitleCol)
        D.text(c, I18n.t("craft_sub"), craftGameBtn.centerX() + craftH * 0.25f, craftGameBtn.centerY() + craftH * 0.26f, craftH * 0.20f, Theme.textDim)

        // Novo Jogo de Carros Multiplayer: TogetherKart
        val kartH = r.height() * 0.055f
        val kartY = craftGameBtn.bottom + r.height() * 0.008f
        kartGameBtn.set(partnerCard.left, kartY, partnerCard.right, kartY + kartH)
        val kartFill = if (Theme.isBunny) Color.rgb(32, 28, 65) else Color.rgb(255, 228, 240)
        val kartLine = if (Theme.isBunny) Color.rgb(105, 215, 255) else Color.rgb(255, 105, 165)
        D.rrect(c, kartGameBtn, kartH * 0.35f, kartFill, kartLine, 2.5f)
        Icons.draw(c, Icons.STAR, kartGameBtn.left + kartH * 0.65f, kartGameBtn.centerY(), kartH * 0.36f)
        val kartTitleCol = if (Theme.isBunny) Color.rgb(135, 225, 255) else Color.rgb(235, 45, 115)
        D.text(c, "🏎️ TogetherKart · Corrida Bunny vs Cat!", kartGameBtn.centerX() + kartH * 0.25f, kartGameBtn.centerY() - kartH * 0.10f, kartH * 0.32f, kartTitleCol)
        D.text(c, "Corrida 3D em Machu Picchu · 2 Carros Multiplayer", kartGameBtn.centerX() + kartH * 0.25f, kartGameBtn.centerY() + kartH * 0.26f, kartH * 0.20f, Theme.textDim)

        // Botões de Ações de Amor (Beijinho, Comida, Carta)
        val actW = (cardW - r.width() * 0.04f) / 3f
        val actH = r.height() * 0.046f
        val actY = kartGameBtn.bottom + r.height() * 0.008f

        sendKissBtn.set(partnerCard.left, actY, partnerCard.left + actW, actY + actH)
        sendFoodBtn.set(sendKissBtn.right + r.width() * 0.02f, actY, sendKissBtn.right + r.width() * 0.02f + actW, actY + actH)
        sendLetterBtn.set(sendFoodBtn.right + r.width() * 0.02f, actY, partnerCard.right, actY + actH)

        D.button(c, sendKissBtn, "💋 Beijinho", Theme.primary, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure, actH * 0.28f)
        D.button(c, sendFoodBtn, "🥟 Comida", Theme.primary, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure, actH * 0.28f)
        D.button(c, sendLetterBtn, "💌 Carta", Theme.primary, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure, actH * 0.28f)

        // Botões de Transferência de Conexão
        val connH = r.height() * 0.050f
        val connW = (cardW - r.width() * 0.03f) / 2f
        val cY1 = sendKissBtn.bottom + r.height() * 0.014f

        copyCodeBtn.set(partnerCard.left, cY1, partnerCard.left + connW, cY1 + connH)
        shareAppBtn.set(copyCodeBtn.right + r.width() * 0.03f, cY1, partnerCard.right, cY1 + connH)

        D.button(c, copyCodeBtn, I18n.t("copy_code"), Theme.card, Theme.text, connH * 0.26f)
        D.button(c, shareAppBtn, I18n.t("share_code"), Theme.card, Theme.text, connH * 0.26f)

        val cY2 = copyCodeBtn.bottom + r.height() * 0.010f
        pasteCodeBtn.set(partnerCard.left, cY2, partnerCard.left + connW, cY2 + connH)
        syncCloudBtn.set(pasteCodeBtn.right + r.width() * 0.03f, cY2, partnerCard.right, cY2 + connH)

        D.button(c, pasteCodeBtn, I18n.t("paste_code"), Theme.cardAccent, Theme.text, connH * 0.26f)
        D.button(c, syncCloudBtn, I18n.t("sync_cloud"), Theme.primary, if (Theme.isBunny) Theme.bSilver else Theme.cWhitePure, connH * 0.26f)

        drawFx(c)
    }

    private fun copyCode() {
        val code = Save.exportSyncCode()
        try {
            val cm = g.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("BunnyLink", code))
            burst(g.width / 2f, g.height / 2f, Icons.SPARK, 6, g.width * 0.03f)
            g.say("Código copiado! Podes enviar no WhatsApp para o parceiro!")
        } catch (e: Exception) {
            g.say("Código pronto: " + code.take(15) + "...")
        }
    }

    private fun shareViaWhatsApp() {
        val code = Save.exportSyncCode()
        val text = "Olá meu amor! ♥ Aqui está o meu código de conexão do Bunny & Cat:\n$code\nAbre a app e clica em 'Colar Código' para nos conectarmos!"
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            g.context.startActivity(Intent.createChooser(intent, "Partilhar com o meu amor"))
        } catch (e: Exception) {
            copyCode()
        }
    }

    private fun pasteCode() {
        try {
            val cm = g.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val item = cm.primaryClip?.getItemAt(0)
            val clipText = item?.text?.toString() ?: ""
            if (clipText.isNotEmpty()) {
                val ok = Save.importSyncCode(clipText)
                if (ok) {
                    burst(g.width / 2f, g.height / 2f, Icons.HEART, 10, g.width * 0.04f)
                    g.say("Conectado com sucesso a ${Save.partnerName}!")
                } else {
                    g.say("Código inválido na área de transferência!")
                }
            } else {
                g.say("A área de transferência está vazia!")
            }
        } catch (e: Exception) {
            g.say("Não foi possível ler a área de transferência.")
        }
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (backBtn.contains(x, y)) {
            g.go(Nav.HOME); return
        }
        if (craftGameBtn.contains(x, y)) {
            g.go(Nav.CRAFT); return
        }
        if (kartGameBtn.contains(x, y)) {
            g.go(Nav.KART); return
        }
        if (sendKissBtn.contains(x, y)) {
            Save.partnerLastGift = "kiss"
            SoundFx.playKiss()
            burst(r.centerX(), r.centerY(), Icons.KISS, 8, r.width() * 0.04f)
            Save.syncOnline { _, _ -> }
            g.say(I18n.t("gift_sent") + " 💋")
            return
        }
        if (sendFoodBtn.contains(x, y)) {
            Save.partnerLastGift = "food"
            SoundFx.playEat(Theme.isCat)
            burst(r.centerX(), r.centerY(), Icons.ALFAJOR, 6, r.width() * 0.04f)
            Save.syncOnline { _, _ -> }
            g.say("Comidinha peruana enviada quentinha! 🥟")
            return
        }
        if (sendLetterBtn.contains(x, y)) {
            Save.partnerLastGift = "letter"
            burst(r.centerX(), r.centerY(), Icons.LETTER, 6, r.width() * 0.04f)
            Save.syncOnline { _, _ -> }
            g.say("Cartinha de amor enviada com carinho! 💌")
            return
        }
        if (copyCodeBtn.contains(x, y)) {
            copyCode(); return
        }
        if (shareAppBtn.contains(x, y)) {
            shareViaWhatsApp(); return
        }
        if (pasteCodeBtn.contains(x, y)) {
            pasteCode(); return
        }
        if (syncCloudBtn.contains(x, y)) {
            g.say("A sincronizar com a nuvem...")
            Save.syncOnline { ok, msg ->
                g.say(if (ok) "Nuvem sincronizada com sucesso! ♥" else "Modo offline. Usa o código de partilha!")
            }
            return
        }
    }
}

// ================================================================= PERFIL DO PET (FIGMA 3D)
class PetProfileScreen(g: BunnyView) : Screen(g) {
    override val fullBleed: Boolean get() = true
    private val pose = Pose()
    private val hv = floatArrayOf(0f)
    private val backBtn = RectF()
    private val favBtn = RectF()
    private val switchPetBtn = RectF()
    private val cuddleBtn = RectF()
    private val wardrobeBtn = RectF()
    private val vitalCards = Array(3) { RectF() }
    private val outfitSlotBtns = Array(4) { RectF() }
    private var isFav = true
    private var heroBounce = 0f
    private var heroBounceV = 0f

    override fun enter() {
        pose.t = 0f
    }

    override fun update(dt: Float) {
        pose.t += dt
        hopStep(pose, dt, hv)
        if (heroBounce > 0f || heroBounceV != 0f) {
            heroBounceV -= 9f * dt
            heroBounce += heroBounceV * dt
            if (heroBounce <= 0f) { heroBounce = 0f; heroBounceV = 0f }
        }
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        val isBunny = Theme.isBunny
        val heroBgTop = if (isBunny) Color.rgb(18, 24, 48) else Color.rgb(255, 238, 230)
        val heroBgBottom = if (isBunny) Color.rgb(38, 18, 42) else Color.rgb(255, 222, 214)

        // Gradiente vertical do estúdio heroico
        val heroHeight = r.height() * 0.44f
        val steps = 18
        for (i in 0 until steps) {
            val k = i / steps.toFloat()
            val col = D.mix(heroBgTop, heroBgBottom, k)
            val y1 = r.top + heroHeight * k
            val y2 = r.top + heroHeight * (k + 1f / steps)
            D.f(col); c.drawRect(r.left, y1, r.right, y2 + 1f, D.p)
        }

        // Halos de luz de estúdio 3D
        val haloCol = if (isBunny) D.alpha(Theme.bBlueSky, 30) else D.alpha(Color.WHITE, 90)
        D.circle(c, r.centerX(), r.top + heroHeight * 0.52f, r.width() * 0.42f, haloCol)
        D.circle(c, r.centerX(), r.top + heroHeight * 0.52f, r.width() * 0.26f, haloCol)

        // 1. Barra de Topo
        val topY = r.top + r.height() * 0.052f
        val btnRad = r.width() * 0.062f

        // Botão Voltar `<`
        val backX = r.left + r.width() * 0.10f
        backBtn.set(backX - btnRad, topY - btnRad, backX + btnRad, topY + btnRad)
        val circleFill = if (isBunny) Theme.bNavyLight else Color.WHITE
        val strokeCol = if (isBunny) Theme.bWine else Color.rgb(255, 215, 210)
        D.circle(c, backX, topY, btnRad, circleFill, strokeCol, 2.5f)
        D.text(c, "❮", backX, topY + btnRad * 0.32f, btnRad * 0.9f, if (isBunny) Theme.bSilver else Theme.text)

        // Título central
        val headerTitle = if (isBunny) "🐰 Bun Bun 3D" else "🐱 Mochi 3D"
        D.text(c, headerTitle, r.centerX(), topY + btnRad * 0.28f, r.height() * 0.024f, if (isBunny) Theme.bSilver else Theme.text)

        // Botão Favorito `♡` / `❤️`
        val favX = r.right - r.width() * 0.10f
        favBtn.set(favX - btnRad, topY - btnRad, favX + btnRad, topY + btnRad)
        D.circle(c, favX, topY, btnRad, circleFill, strokeCol, 2.5f)
        if (isFav) {
            Icons.draw(c, Icons.HEART, favX, topY, btnRad * 0.60f)
        } else {
            D.text(c, "♡", favX, topY + btnRad * 0.32f, btnRad * 0.9f, strokeCol)
        }

        // 2. Renderização do Pet 3D Gigante / Hero
        val petS = r.height() * 0.26f
        val petX = r.centerX()
        val petY = r.top + heroHeight * 0.82f - heroBounce * 28f

        // Sombra de estúdio sob o pet
        val shadowScale = (1f - heroBounce * 0.3f).coerceIn(0.6f, 1.1f)
        D.oval(c, petX, r.top + heroHeight * 0.84f, petS * 0.44f * shadowScale, petS * 0.10f * shadowScale, D.alpha(Color.BLACK, 40))

        // Pet com pose e personalização
        if (isBunny) {
            BunnyArt.draw(c, petX, petY, petS, pose, Save.bunnyHat, Save.bunnyOutfit, Save.bunnyAcc, Save.bunnyShoes, Save.bunnyColor)
        } else {
            CatArt.draw(c, petX, petY, petS, pose.t, mood = if (Save.joy > 50f) 0 else 1, pounce = heroBounce, hat = Save.catHat, outfit = Save.catOutfit, shoes = Save.catShoes, color = Save.catColor, acc = Save.catAcc)
        }

        // 3. Folha de Informações Estilo Figma Bottom Sheet (Overlapping Card)
        val sheetTop = r.top + heroHeight * 0.88f
        val sheetFill = if (isBunny) Color.rgb(18, 24, 46) else Color.WHITE
        val sheetOutline = if (isBunny) Theme.bWine else Color.rgb(255, 230, 225)
        D.rrect(c, r.left, sheetTop, r.right, r.bottom + 50f, r.width() * 0.08f, sheetFill, sheetOutline, 3f)

        // Pega / Drag handle no topo da folha
        val handleW = r.width() * 0.12f
        val handleH = r.height() * 0.006f
        val handleCol = if (isBunny) D.alpha(Theme.bSilver, 80) else Color.rgb(230, 220, 215)
        D.rrect(c, r.centerX() - handleW / 2f, sheetTop + r.height() * 0.014f, r.centerX() + handleW / 2f, sheetTop + r.height() * 0.014f + handleH, handleH / 2f, handleCol)

        // Linha 1: Nome do Pet + Pílula de Nível + Botão Trocar
        val contentY = sheetTop + r.height() * 0.045f
        val petName = if (isBunny) "Bun Bun" else "Mochi"
        val nameText = if (isBunny) "🐰 $petName" else "🐱 $petName"
        val nameSize = r.height() * 0.030f
        val nameW = D.textW(nameText, nameSize)
        D.text(c, nameText, r.left + r.width() * 0.08f + nameW / 2f, contentY + nameSize * 0.35f, nameSize, if (isBunny) Theme.bSilver else Theme.text)

        // Pílula Nv. 7 (Figma)
        val pillX = r.left + r.width() * 0.08f + nameW + r.width() * 0.04f
        val pillW = r.width() * 0.16f
        val pillH = r.height() * 0.030f
        val pillCol = if (isBunny) Theme.bWine else Color.rgb(255, 170, 166)
        D.rrect(c, pillX, contentY - pillH * 0.5f, pillX + pillW, contentY + pillH * 0.5f, pillH * 0.5f, pillCol)
        D.text(c, "Lv. ${Save.level()}", pillX + pillW * 0.5f, contentY + pillH * 0.28f, pillH * 0.55f, Color.WHITE)

        // Botão Trocar de Pet à Direita
        val switchW = r.width() * 0.26f
        val switchH = r.height() * 0.032f
        val switchX = r.right - r.width() * 0.08f - switchW
        switchPetBtn.set(switchX, contentY - switchH * 0.5f, switchX + switchW, contentY + switchH * 0.5f)
        val switchFill = if (isBunny) Theme.bNavyLight else Color.rgb(255, 243, 238)
        D.rrect(c, switchPetBtn, switchH * 0.5f, switchFill, strokeCol, 2f)
        val switchLabel = if (isBunny) "⇄ Ver Mochi" else "⇄ Ver Bunny"
        D.text(c, switchLabel, switchPetBtn.centerX(), contentY + switchH * 0.26f, switchH * 0.44f, if (isBunny) Theme.bBlueIce else Color.rgb(255, 115, 125))

        // Linha 2: Subtítulo / Bio do Figma
        val bioY = contentY + r.height() * 0.038f
        val bioTxt = if (isBunny) "Coelhinho meigo · adora abraços e cantinhos suaves 🌸" else "Gatinha dorminhoca · adora cantinhos ao sol ☀️"
        D.text(c, bioTxt, r.left + r.width() * 0.08f, bioY, r.height() * 0.015f, if (isBunny) Theme.bSilver else Color.rgb(150, 140, 135), align = Paint.Align.LEFT)

        // Linha 3: Barra de Crescimento / XP do Figma
        val xpY = bioY + r.height() * 0.036f
        val xpW = r.width() * 0.84f
        val xpH = r.height() * 0.012f
        val xpLeft = r.left + r.width() * 0.08f
        val curXp = ((Save.level() * 75 + Save.joy.toInt() * 3) % 500)
        D.text(c, "Crescimento & Vínculo", xpLeft, xpY - r.height() * 0.007f, r.height() * 0.014f, if (isBunny) Theme.bSilver else Theme.text, align = Paint.Align.LEFT)
        D.text(c, "$curXp / 500 XP", xpLeft + xpW, xpY - r.height() * 0.007f, r.height() * 0.013f, if (isBunny) Theme.bBlueIce else Color.rgb(255, 115, 125), align = Paint.Align.RIGHT)

        // Fundo da barra XP
        D.rrect(c, xpLeft, xpY, xpLeft + xpW, xpY + xpH, xpH * 0.5f, if (isBunny) Theme.bNavyDark else Color.rgb(245, 238, 235))
        val xpFillW = xpW * (curXp / 500f).coerceIn(0.05f, 1f)
        val xpColor = if (isBunny) Theme.bWine else Color.rgb(255, 135, 145)
        D.rrect(c, xpLeft, xpY, xpLeft + xpFillW, xpY + xpH, xpH * 0.5f, xpColor)

        // Linha 4: 3 Cartões de Vitais em grelha (Figma Vital Cards: Hunger 82%, Mood 95%, Energy 60%)
        val cardsY = xpY + r.height() * 0.032f
        val cardW = (xpW - r.width() * 0.04f) / 3f
        val cardH = r.height() * 0.095f

        // Cartão 1: Fome (Peach)
        val c1Left = xpLeft
        vitalCards[0].set(c1Left, cardsY, c1Left + cardW, cardsY + cardH)
        val c1Bg = if (isBunny) Color.rgb(42, 22, 54) else Color.rgb(255, 240, 235)
        val c1Border = if (isBunny) Theme.bWine else Color.rgb(255, 215, 205)
        D.rrect(c, vitalCards[0], cardH * 0.18f, c1Bg, c1Border, 2f)
        Icons.draw(c, Icons.CARROT, vitalCards[0].centerX(), cardsY + cardH * 0.28f, cardH * 0.20f)
        D.text(c, "${Save.hunger.toInt()}%", vitalCards[0].centerX(), cardsY + cardH * 0.64f, cardH * 0.24f, if (isBunny) Theme.bSilver else Theme.text)
        D.text(c, "Fome", vitalCards[0].centerX(), cardsY + cardH * 0.86f, cardH * 0.16f, if (isBunny) Theme.bBlueIce else Color.rgb(160, 140, 135))

        // Cartão 2: Humor (Pink)
        val c2Left = c1Left + cardW + r.width() * 0.02f
        vitalCards[1].set(c2Left, cardsY, c2Left + cardW, cardsY + cardH)
        val c2Bg = if (isBunny) Color.rgb(58, 20, 42) else Color.rgb(255, 234, 240)
        val c2Border = if (isBunny) Theme.bWineLight else Color.rgb(255, 200, 215)
        D.rrect(c, vitalCards[1], cardH * 0.18f, c2Bg, c2Border, 2f)
        Icons.draw(c, Icons.HEART, vitalCards[1].centerX(), cardsY + cardH * 0.28f, cardH * 0.20f)
        D.text(c, "${Save.joy.toInt()}%", vitalCards[1].centerX(), cardsY + cardH * 0.64f, cardH * 0.24f, if (isBunny) Theme.bSilver else Theme.text)
        D.text(c, "Humor", vitalCards[1].centerX(), cardsY + cardH * 0.86f, cardH * 0.16f, if (isBunny) Theme.bBlueIce else Color.rgb(160, 140, 135))

        // Cartão 3: Energia (Mint)
        val c3Left = c2Left + cardW + r.width() * 0.02f
        vitalCards[2].set(c3Left, cardsY, c3Left + cardW, cardsY + cardH)
        val c3Bg = if (isBunny) Color.rgb(24, 38, 64) else Color.rgb(232, 248, 242)
        val c3Border = if (isBunny) Theme.bBlueSky else Color.rgb(180, 230, 210)
        D.rrect(c, vitalCards[2], cardH * 0.18f, c3Bg, c3Border, 2f)
        Icons.draw(c, Icons.LIGHTNING, vitalCards[2].centerX(), cardsY + cardH * 0.28f, cardH * 0.20f)
        D.text(c, "${Save.energy.toInt()}%", vitalCards[2].centerX(), cardsY + cardH * 0.64f, cardH * 0.24f, if (isBunny) Theme.bSilver else Theme.text)
        D.text(c, "Energia", vitalCards[2].centerX(), cardsY + cardH * 0.86f, cardH * 0.16f, if (isBunny) Theme.bBlueIce else Color.rgb(160, 140, 135))

        // Linha 5: Gaveta de Roupas / Outfits (Figma Outfit Drawer)
        val outfitY = cardsY + cardH + r.height() * 0.026f
        D.text(c, "Guarda-roupa 3D", xpLeft, outfitY, r.height() * 0.015f, if (isBunny) Theme.bSilver else Theme.text, align = Paint.Align.LEFT)
        val wardrobeLinkW = D.textW("Ver todos ❯", r.height() * 0.014f)
        wardrobeBtn.set(xpLeft + xpW - wardrobeLinkW - 10f, outfitY - r.height() * 0.015f, xpLeft + xpW + 10f, outfitY + r.height() * 0.010f)
        D.text(c, "Ver todos ❯", xpLeft + xpW, outfitY, r.height() * 0.014f, if (isBunny) Theme.bBlueIce else Color.rgb(255, 115, 125), align = Paint.Align.RIGHT)

        val outW = (xpW - r.width() * 0.045f) / 4f
        val outH = r.height() * 0.055f
        val outIcons = intArrayOf(Icons.HAT, Icons.BAG, Icons.BALL, Icons.STAR)
        val outNames = arrayOf("Acessório", "Roupa", "Calçado", "Especial")
        for (i in 0 until 4) {
            val ox = xpLeft + i * (outW + r.width() * 0.015f)
            val oy = outfitY + r.height() * 0.014f
            outfitSlotBtns[i].set(ox, oy, ox + outW, oy + outH)
            val slotBg = if (isBunny) Theme.bNavyLight else Color.rgb(250, 246, 245)
            D.rrect(c, outfitSlotBtns[i], outH * 0.24f, slotBg, strokeCol, 1.5f)
            Icons.draw(c, outIcons[i], ox + outW * 0.5f, oy + outH * 0.38f, outH * 0.28f)
            D.text(c, outNames[i], ox + outW * 0.5f, oy + outH * 0.82f, outH * 0.18f, if (isBunny) Theme.bSilver else Color.rgb(150, 140, 135))
        }

        // Linha 6: Grande Botão CTA Figma: "Enviar Miminho ao Pet"
        val ctaY = r.bottom - r.height() * 0.075f
        val ctaH = r.height() * 0.054f
        cuddleBtn.set(xpLeft, ctaY, xpLeft + xpW, ctaY + ctaH)
        val ctaCol = if (isBunny) Theme.bWine else Color.rgb(255, 115, 135)
        D.rrect(c, cuddleBtn, ctaH * 0.5f, ctaCol)
        Icons.draw(c, Icons.HEART, cuddleBtn.centerX() - r.width() * 0.26f, cuddleBtn.centerY(), ctaH * 0.32f)
        D.text(c, "Enviar Miminho ao Pet 💕", cuddleBtn.centerX() + r.width() * 0.04f, cuddleBtn.centerY() + ctaH * 0.24f, ctaH * 0.40f, Color.WHITE)

        drawFx(c)
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (backBtn.contains(x, y)) {
            g.go(Nav.HOME); return
        }
        if (favBtn.contains(x, y)) {
            isFav = !isFav
            burst(favBtn.centerX(), favBtn.centerY(), Icons.HEART, 6, r.width() * 0.04f)
            SoundFx.playKiss()
            g.say(if (isFav) "Guardado nos teus favoritos! ♥" else "Removido dos favoritos.")
            return
        }
        if (switchPetBtn.contains(x, y)) {
            Save.activeChar = if (Save.activeChar == "cat") "bunny" else "cat"
            burst(switchPetBtn.centerX(), switchPetBtn.centerY(), Icons.SPARK, 6, r.width() * 0.04f)
            g.say("A alternar para " + (if (Theme.isBunny) "Bunny" else "Gatinha") + "!")
            return
        }
        if (wardrobeBtn.contains(x, y)) {
            g.go(Nav.WARDROBE); return
        }
        for (i in 0 until 4) {
            if (outfitSlotBtns[i].contains(x, y)) {
                g.go(Nav.WARDROBE); return
            }
        }
        for (i in 0 until 3) {
            if (vitalCards[i].contains(x, y)) {
                when (i) {
                    0 -> g.go(Nav.EAT)
                    1 -> g.go(Nav.PLAY)
                    2 -> g.go(Nav.SLEEP)
                }
                return
            }
        }
        if (cuddleBtn.contains(x, y)) {
            heroBounceV = 3.5f; heroBounce = 0.01f
            Save.joy = min(100f, Save.joy + 8f)
            Save.catHappiness = min(100f, Save.catHappiness + 8f)
            burst(cuddleBtn.centerX(), cuddleBtn.centerY(), Icons.HEART, 10, r.width() * 0.05f)
            burst(r.centerX(), r.top + r.height() * 0.28f, Icons.SPARK, 8, r.width() * 0.04f)
            SoundFx.playPlay(Theme.isCat)
            g.say(if (Theme.isBunny) "Bun Bun adorou o teu miminho carinhoso! 🌸" else "Mochi ronrona de felicidade com o teu miminho! 🐱💕")
            return
        }

        // Tap direto no Pet Heroico
        val heroHeight = r.height() * 0.44f
        if (hypot(x - r.centerX(), y - (r.top + heroHeight * 0.65f)) < r.width() * 0.32f) {
            heroBounceV = 3.2f; heroBounce = 0.01f
            Save.joy = min(100f, Save.joy + 3f)
            burst(x, y, Icons.HEART, 6, r.width() * 0.04f)
            SoundFx.playPlay(Theme.isCat)
            g.say(if (Theme.isBunny) "Pulo fofinho do Bun Bun! 🐰" else "Miau! :3 Mochi dá uma cambalhota!")
        }
    }
}

// ================================================================= WIDGETS TOGETHER (FIGMA 3D)
class WidgetsScreen(g: BunnyView) : Screen(g) {
    override val fullBleed: Boolean get() = true
    private val pose = Pose()
    private val backBtn = RectF()
    private val tabBtns = Array(3) { RectF() }
    private var activeTab = 0 // 0: Médio (2x1), 1: Together a Dois, 2: Pequeno (1x1)
    private val widgetActionBtn = RectF()
    private val installBtn = RectF()
    private val companionKissBtn = RectF()
    private var widgetPetBounce = 0f
    private var widgetPetBounceV = 0f

    override fun update(dt: Float) {
        pose.t += dt
        if (widgetPetBounce > 0f || widgetPetBounceV != 0f) {
            widgetPetBounceV -= 9f * dt
            widgetPetBounce += widgetPetBounceV * dt
            if (widgetPetBounce <= 0f) { widgetPetBounce = 0f; widgetPetBounceV = 0f }
        }
        updFx(dt)
    }

    override fun draw(c: Canvas, r: RectF) {
        val isBunny = Theme.isBunny
        val bgCol = if (isBunny) Color.rgb(14, 18, 36) else Color.rgb(250, 245, 243)
        D.f(bgCol); c.drawRect(r, D.p)

        // 1. Barra de Topo
        val topY = r.top + r.height() * 0.052f
        val btnRad = r.width() * 0.062f
        val backX = r.left + r.width() * 0.10f
        backBtn.set(backX - btnRad, topY - btnRad, backX + btnRad, topY + btnRad)
        val circleFill = if (isBunny) Theme.bNavyLight else Color.WHITE
        val strokeCol = if (isBunny) Theme.bWine else Color.rgb(255, 215, 210)
        D.circle(c, backX, topY, btnRad, circleFill, strokeCol, 2.5f)
        D.text(c, "❮", backX, topY + btnRad * 0.32f, btnRad * 0.9f, if (isBunny) Theme.bSilver else Theme.text)

        D.text(c, "Widgets Together", r.centerX(), topY + btnRad * 0.22f, r.height() * 0.024f, if (isBunny) Theme.bSilver else Theme.text)
        D.text(c, "Leva o Bunny e a Gatinha para o teu ecrã inicial", r.centerX(), topY + btnRad * 0.90f, r.height() * 0.014f, if (isBunny) Theme.bBlueIce else Color.rgb(150, 140, 135))

        // 2. Segmented Tabs no topo (Figma Tab Switcher)
        val tabY = topY + r.height() * 0.062f
        val tabTotalW = r.width() * 0.88f
        val tabH = r.height() * 0.044f
        val tabW = tabTotalW / 3f
        val tabLeft = r.centerX() - tabTotalW / 2f

        // Fundo do segmentador
        val tabBg = if (isBunny) Theme.bNavyDark else Color.rgb(240, 232, 230)
        D.rrect(c, tabLeft, tabY, tabLeft + tabTotalW, tabY + tabH, tabH * 0.5f, tabBg)

        val tabLabels = arrayOf("Médio (2x1)", "Together", "Pequeno (1x1)")
        for (i in 0 until 3) {
            val tx = tabLeft + i * tabW
            tabBtns[i].set(tx, tabY, tx + tabW, tabY + tabH)
            if (activeTab == i) {
                val activeCol = if (isBunny) Theme.bWine else Color.rgb(255, 135, 145)
                D.rrect(c, tx + 2f, tabY + 2f, tx + tabW - 2f, tabY + tabH - 2f, tabH * 0.48f, activeCol)
                D.text(c, tabLabels[i], tx + tabW * 0.5f, tabY + tabH * 0.62f, tabH * 0.38f, Color.WHITE)
            } else {
                val inactiveCol = if (isBunny) Theme.bSilver else Color.rgb(130, 120, 115)
                D.text(c, tabLabels[i], tx + tabW * 0.5f, tabY + tabH * 0.62f, tabH * 0.36f, inactiveCol)
            }
        }

        // 3. Simulador de Ecrã de Telemóvel / Área de Pré-visualização
        val previewTop = tabY + tabH + r.height() * 0.025f
        val previewW = r.width() * 0.88f
        val previewH = r.height() * 0.44f
        val previewLeft = r.centerX() - previewW / 2f
        val phoneBg = if (isBunny) Color.rgb(22, 28, 54) else Color.rgb(255, 252, 250)
        val phoneBorder = if (isBunny) Theme.bWine else Color.rgb(255, 218, 212)
        D.rrect(c, previewLeft, previewTop, previewLeft + previewW, previewTop + previewH, previewW * 0.08f, phoneBg, phoneBorder, 2.5f)

        // Simulação de relógio do telemóvel no topo da tela simulada
        val simClockY = previewTop + r.height() * 0.030f
        D.text(c, "09:41", previewLeft + previewW * 0.16f, simClockY, r.height() * 0.016f, if (isBunny) Theme.bSilver else Color.rgb(140, 130, 125))
        D.text(c, "Together OS · 5G 100%", previewLeft + previewW * 0.72f, simClockY, r.height() * 0.013f, if (isBunny) Theme.bBlueIce else Color.rgb(160, 150, 145))

        // Conteúdo do Widget Ativo
        when (activeTab) {
            0 -> drawMediumWidget(c, previewLeft, previewTop, previewW, previewH, isBunny)
            1 -> drawTogetherWidget(c, previewLeft, previewTop, previewW, previewH, isBunny)
            2 -> drawSmallWidget(c, previewLeft, previewTop, previewW, previewH, isBunny)
        }

        // 4. Guia de Instalação e Dicas do Figma
        val guideY = previewTop + previewH + r.height() * 0.025f
        val guideLeft = previewLeft
        val guideTxtCol = if (isBunny) Theme.bSilver else Theme.text
        val subCol = if (isBunny) Theme.bBlueIce else Color.rgb(150, 140, 135)
        D.text(c, "Como adicionar ao teu Ecrã Inicial:", guideLeft, guideY, r.height() * 0.017f, guideTxtCol, align = Paint.Align.LEFT)
        D.text(c, "1. No ecrã do Android, pressiona num espaço vazio.", guideLeft, guideY + r.height() * 0.024f, r.height() * 0.014f, subCol, align = Paint.Align.LEFT)
        D.text(c, "2. Escolhe 'Widgets' e seleciona 'Together'.", guideLeft, guideY + r.height() * 0.044f, r.height() * 0.014f, subCol, align = Paint.Align.LEFT)
        D.text(c, "3. Arrasta este formato para onde quiseres!", guideLeft, guideY + r.height() * 0.064f, r.height() * 0.014f, subCol, align = Paint.Align.LEFT)

        // 5. Botão de Ação / Ativar Widget
        val ctaY = r.bottom - r.height() * 0.075f
        val ctaH = r.height() * 0.054f
        installBtn.set(guideLeft, ctaY, guideLeft + previewW, ctaY + ctaH)
        val installCol = if (isBunny) Theme.bWine else Color.rgb(255, 115, 135)
        D.rrect(c, installBtn, ctaH * 0.5f, installCol)
        Icons.draw(c, Icons.WIDGET, installBtn.centerX() - r.width() * 0.28f, installBtn.centerY(), ctaH * 0.32f)
        D.text(c, "Ativar Widget no Ecrã Principal ✨", installBtn.centerX() + r.width() * 0.04f, installBtn.centerY() + ctaH * 0.24f, ctaH * 0.38f, Color.WHITE)

        drawFx(c)
    }

    private fun drawMediumWidget(c: Canvas, px: Float, py: Float, pw: Float, ph: Float, isBunny: Boolean) {
        val wx = px + pw * 0.05f
        val wy = py + ph * 0.16f
        val ww = pw * 0.90f
        val wh = ph * 0.72f

        val wBg = if (isBunny) Color.rgb(28, 18, 48) else Color.rgb(255, 246, 242)
        val wBorder = if (isBunny) Theme.bWine else Color.rgb(255, 215, 210)
        D.rrect(c, wx, wy, wx + ww, wy + wh, wh * 0.22f, wBg, wBorder, 3f)

        // Pet 3D à esquerda dentro do widget
        val petX = wx + ww * 0.26f
        val petY = wy + wh * 0.86f - widgetPetBounce * 20f
        val petS = wh * 0.68f

        // Sombra suave sob o pet
        D.oval(c, petX, wy + wh * 0.88f, petS * 0.40f, petS * 0.12f, D.alpha(Color.BLACK, 35))

        if (isBunny) {
            BunnyArt.draw(c, petX, petY, petS, pose, Save.bunnyHat, Save.bunnyOutfit, Save.bunnyAcc, Save.bunnyShoes, Save.bunnyColor)
        } else {
            CatArt.draw(c, petX, petY, petS, pose.t, mood = 0, pounce = widgetPetBounce, hat = Save.catHat, outfit = Save.catOutfit, shoes = Save.catShoes, color = Save.catColor, acc = Save.catAcc)
        }

        // Informações à direita
        val infoX = wx + ww * 0.52f
        val title = if (isBunny) "Bun Bun quer mimo 💕" else "Mochi tem saudades 🐱"
        D.text(c, title, infoX, wy + wh * 0.24f, wh * 0.11f, if (isBunny) Theme.bSilver else Theme.text, align = Paint.Align.LEFT)
        D.text(c, "Último mimo: há 2h · Nv. ${Save.level()}", infoX, wy + wh * 0.40f, wh * 0.08f, if (isBunny) Theme.bBlueIce else Color.rgb(150, 140, 135), align = Paint.Align.LEFT)

        // 5 Corações de Vínculo
        val heartS = wh * 0.08f
        for (i in 0 until 5) {
            val hx = infoX + i * (heartS * 2.1f) + heartS * 0.5f
            val hy = wy + wh * 0.55f
            if (i < 4) {
                Icons.draw(c, Icons.HEART, hx, hy, heartS)
            } else {
                D.text(c, "♡", hx, hy + heartS * 0.35f, heartS * 1.5f, if (isBunny) Theme.bWine else Color.rgb(255, 175, 175))
            }
        }

        // Botão Interativo 'Dar Mimo' dentro do Widget
        val btnW = ww * 0.42f
        val btnH = wh * 0.24f
        widgetActionBtn.set(infoX, wy + wh * 0.68f, infoX + btnW, wy + wh * 0.68f + btnH)
        val btnCol = if (isBunny) Theme.bWine else Color.rgb(255, 130, 148)
        D.rrect(c, widgetActionBtn, btnH * 0.5f, btnCol)
        Icons.draw(c, Icons.PAW, widgetActionBtn.centerX() - btnW * 0.28f, widgetActionBtn.centerY(), btnH * 0.35f)
        D.text(c, "Dar Mimo 🐾", widgetActionBtn.centerX() + btnW * 0.12f, widgetActionBtn.centerY() + btnH * 0.22f, btnH * 0.42f, Color.WHITE)
    }

    private fun drawTogetherWidget(c: Canvas, px: Float, py: Float, pw: Float, ph: Float, isBunny: Boolean) {
        val wx = px + pw * 0.05f
        val wy = py + ph * 0.16f
        val ww = pw * 0.90f
        val wh = ph * 0.72f

        val wBg = if (isBunny) Color.rgb(32, 16, 44) else Color.rgb(255, 242, 245)
        val wBorder = if (isBunny) Theme.bWine else Color.rgb(255, 200, 215)
        D.rrect(c, wx, wy, wx + ww, wy + wh, wh * 0.22f, wBg, wBorder, 3f)

        // Casal 3D Snuggle à esquerda (Bunny e Cat juntinhos)
        val bX = wx + ww * 0.18f
        val cX = wx + ww * 0.36f
        val pY = wy + wh * 0.86f
        val pS = wh * 0.56f

        BunnyArt.draw(c, bX, pY, pS, pose, Save.bunnyHat, Save.bunnyOutfit, Save.bunnyAcc, Save.bunnyShoes, Save.bunnyColor)
        CatArt.draw(c, cX, pY, pS * 0.94f, pose.t, mood = 0, pounce = widgetPetBounce, hat = Save.catHat, outfit = Save.catOutfit, shoes = Save.catShoes, color = Save.catColor, acc = Save.catAcc)

        // Coração interligado no topo
        Icons.draw(c, Icons.HEART, (bX + cX) / 2f, wy + wh * 0.20f, wh * 0.12f)

        // Informações à direita
        val infoX = wx + ww * 0.52f
        D.text(c, "128 dias juntos ♥", infoX, wy + wh * 0.24f, wh * 0.11f, if (isBunny) Theme.bSilver else Theme.text, align = Paint.Align.LEFT)

        // Badge Streak Flame
        val flameY = wy + wh * 0.40f
        Icons.draw(c, Icons.FLAME, infoX + wh * 0.08f, flameY, wh * 0.09f)
        D.text(c, "14 dias seguidos de streak", infoX + wh * 0.20f, flameY + wh * 0.035f, wh * 0.08f, if (isBunny) Theme.bBlueIce else Color.rgb(255, 95, 45), align = Paint.Align.LEFT)

        D.text(c, "Conectados com amor 💕", infoX, wy + wh * 0.56f, wh * 0.08f, if (isBunny) Theme.bSilver else Color.rgb(150, 140, 135), align = Paint.Align.LEFT)

        // Botão Enviar Beijo
        val btnW = ww * 0.42f
        val btnH = wh * 0.24f
        companionKissBtn.set(infoX, wy + wh * 0.68f, infoX + btnW, wy + wh * 0.68f + btnH)
        val kissCol = if (isBunny) Theme.bWine else Color.rgb(255, 95, 135)
        D.rrect(c, companionKissBtn, btnH * 0.5f, kissCol)
        Icons.draw(c, Icons.KISS, companionKissBtn.centerX() - btnW * 0.28f, companionKissBtn.centerY(), btnH * 0.35f)
        D.text(c, "Enviar Beijo 💋", companionKissBtn.centerX() + btnW * 0.12f, companionKissBtn.centerY() + btnH * 0.22f, btnH * 0.40f, Color.WHITE)
    }

    private fun drawSmallWidget(c: Canvas, px: Float, py: Float, pw: Float, ph: Float, isBunny: Boolean) {
        val cardW = pw * 0.42f
        val cardH = ph * 0.72f
        val cy = py + ph * 0.16f

        // Widget 1: Solo Avatar Pet
        val c1x = px + pw * 0.05f
        val wBg = if (isBunny) Color.rgb(28, 18, 48) else Color.rgb(255, 246, 242)
        val wBorder = if (isBunny) Theme.bWine else Color.rgb(255, 215, 210)
        D.rrect(c, c1x, cy, c1x + cardW, cy + cardH, cardH * 0.22f, wBg, wBorder, 3f)

        val petX = c1x + cardW * 0.5f
        val petY = cy + cardH * 0.68f
        val petS = cardH * 0.52f
        if (isBunny) {
            BunnyArt.draw(c, petX, petY, petS, pose, Save.bunnyHat, Save.bunnyOutfit, Save.bunnyAcc, Save.bunnyShoes, Save.bunnyColor)
        } else {
            CatArt.draw(c, petX, petY, petS, pose.t, mood = 0, pounce = widgetPetBounce, hat = Save.catHat, outfit = Save.catOutfit, shoes = Save.catShoes, color = Save.catColor, acc = Save.catAcc)
        }
        val petLabel = if (isBunny) "🐰 Bun Bun" else "🐱 Mochi"
        D.text(c, petLabel, petX, cy + cardH * 0.20f, cardH * 0.11f, if (isBunny) Theme.bSilver else Theme.text)
        D.text(c, "🟢 Feliz · Nv. ${Save.level()}", petX, cy + cardH * 0.88f, cardH * 0.085f, if (isBunny) Theme.bBlueIce else Color.rgb(120, 110, 105))

        // Widget 2: Together Duo
        val c2x = c1x + cardW + pw * 0.06f
        D.rrect(c, c2x, cy, c2x + cardW, cy + cardH, cardH * 0.22f, wBg, wBorder, 3f)

        val duoY = cy + cardH * 0.68f
        val duoS = cardH * 0.40f
        BunnyArt.draw(c, c2x + cardW * 0.32f, duoY, duoS, pose, Save.bunnyHat, Save.bunnyOutfit, Save.bunnyAcc, Save.bunnyShoes, Save.bunnyColor)
        CatArt.draw(c, c2x + cardW * 0.68f, duoY, duoS * 0.95f, pose.t, mood = 0, hat = Save.catHat, outfit = Save.catOutfit, shoes = Save.catShoes, color = Save.catColor, acc = Save.catAcc)

        Icons.draw(c, Icons.HEART, c2x + cardW * 0.5f, cy + cardH * 0.20f, cardH * 0.12f)
        D.text(c, "128D Juntos", c2x + cardW * 0.5f, cy + cardH * 0.34f, cardH * 0.09f, if (isBunny) Theme.bSilver else Theme.text)
        D.text(c, "Amor a Dois 💕", c2x + cardW * 0.5f, cy + cardH * 0.88f, cardH * 0.085f, if (isBunny) Theme.bBlueIce else Color.rgb(255, 115, 135))
    }

    override fun down(x: Float, y: Float, r: RectF) {
        if (backBtn.contains(x, y)) {
            g.go(Nav.HOME); return
        }
        for (i in 0 until 3) {
            if (tabBtns[i].contains(x, y)) {
                activeTab = i
                SoundFx.playPlay(Theme.isCat)
                return
            }
        }
        if (activeTab == 0 && widgetActionBtn.contains(x, y)) {
            widgetPetBounceV = 3.5f; widgetPetBounce = 0.01f
            Save.joy = min(100f, Save.joy + 6f)
            burst(widgetActionBtn.centerX(), widgetActionBtn.centerY(), Icons.HEART, 8, r.width() * 0.04f)
            SoundFx.playPlay(Theme.isCat)
            g.say(if (Theme.isBunny) "Bun Bun recebeu o teu carinho pelo Widget! 🌸" else "Mochi ronrona feliz através do Widget! 🐱💕")
            return
        }
        if (activeTab == 1 && companionKissBtn.contains(x, y)) {
            Save.partnerLastGift = "kiss"
            SoundFx.playKiss()
            burst(companionKissBtn.centerX(), companionKissBtn.centerY(), Icons.KISS, 8, r.width() * 0.04f)
            Save.syncOnline { _, _ -> }
            g.say("Beijinho enviado com sucesso através do Widget Together! 💋")
            return
        }
        if (installBtn.contains(x, y)) {
            burst(installBtn.centerX(), installBtn.centerY(), Icons.STAR, 12, r.width() * 0.05f)
            burst(installBtn.centerX(), installBtn.centerY(), Icons.HEART, 8, r.width() * 0.04f)
            SoundFx.playPlay(Theme.isCat)
            g.say("Widget Together sincronizado com sucesso no teu telemóvel! 📱✨")
            return
        }
    }
}

