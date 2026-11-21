package com.example.bunny

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.KeyEvent
import android.view.MotionEvent
import kotlin.random.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

/**
 * TogetherKart.kt
 * Jogo de corridas pseudo-3D adaptado do CosmoRacers para TOGETHER:
 * - 2 Carros principais: 🐰 Carro do Bunny (Azul/Vinho/Roxo) & 🐱 Carro da Cat (Rosa/Branco)
 * - Pista panorâmica (Landscape) pelos vales andinos de Machu Picchu
 * - Itens colecionáveis: Cenouras Douradas 🥕 e Corações 💖 para encher o Turbo
 * - Plataformas de impulso na estrada e suporte a comando PS4
 * - Sincronização Multiplayer dos dois companheiros
 */
class KartScreen(g: BunnyView) : Screen(g) {

    // ---------------------------------------------------------------- PROJEÇÃO & PISTA
    private class P {
        var cx = 0.0; var cy = 0.0; var cz = 0.0; var scale = 0.0
        var sx = 0f; var sy = 0f; var sw = 0f
    }

    private class Spr(val kind: Int, val offset: Double, val color: Int) { // 0 cristal inca, 1 árvore andina, 2 cenoura/coração
        var taken = false
    }

    private class Seg(val index: Int, val z1: Double, val z2: Double, val y1: Double, val y2: Double, val curve: Double) {
        val p1 = P(); val p2 = P()
        var clip = 0f
        var boost = false
        val sprites = ArrayList<Spr>()
    }

    private class Car(val name: String, val isBunny: Boolean, val color: Int, val dark: Int, val frac: Double, val baseOffset: Double) {
        var z = 0.0; var offset = 0.0; var speed = 0.0
        var phase = 0.0
        var finished = false
    }

    private class RacerDef(val name: String, val isBunny: Boolean, val color: Int, val dark: Int)

    private enum class State { MENU, COUNTDOWN, RACE, FINISH }

    // Parâmetros da pista e câmara
    private val segLen = 200.0
    private val roadW = 2000.0
    private val camH = 1000.0
    private val drawDist = 135
    private val cameraDepth = 1.0 / tan(Math.toRadians(95.0 / 2))
    private val playerZ = camH * cameraDepth
    private val maxSpeed = 8800.0
    private val boostMax = maxSpeed * 1.40
    private val totalLaps = 3

    private val segments = ArrayList<Seg>()
    private var trackLength = 0.0
    private var lastY = 0.0

    // Estado da corrida
    private var state = State.RACE
    private var d0 = playerZ
    private var d = playerZ
    private var playerX = 0.0
    private var speed = 0.0
    private var energy = 60.0
    private var padTimer = 0.0
    private var timer = 3.5
    private var raceTime = 0.0
    private var clock = 0.0
    private var skyOff = 0.0
    private var currentLap = 1
    private var finishedCount = 0
    private var playerPlace = 1
    private var steerVis = 0.0
    private var partnerSyncTimer = 0f

    // 10 Pilotos estilo Ben 10 Galactic Racing (Bunny, Cat e 8 Bots companheiros)
    private val racers = listOf(
        RacerDef("🐰 Bunny", true, Color.rgb(26, 42, 85), Color.rgb(105, 20, 50)),
        RacerDef("🐱 Cat", false, Color.rgb(255, 135, 185), Color.rgb(235, 75, 135)),
        RacerDef("🦙 Lhama Estelar", true, Color.rgb(240, 220, 195), Color.rgb(160, 120, 85)),
        RacerDef("🦅 Cóndor Cósmico", false, Color.rgb(255, 205, 45), Color.rgb(205, 145, 15)),
        RacerDef("🐆 Puma Galáctico", true, Color.rgb(245, 155, 65), Color.rgb(180, 95, 30)),
        RacerDef("🦙 Vicunha Neon", false, Color.rgb(65, 235, 215), Color.rgb(25, 160, 145)),
        RacerDef("🐓 Galo de Órbita", true, Color.rgb(255, 55, 75), Color.rgb(185, 25, 45)),
        RacerDef("🦊 Zorro Astral", false, Color.rgb(255, 125, 45), Color.rgb(195, 75, 20)),
        RacerDef("🐻 Urso Andino", true, Color.rgb(115, 85, 165), Color.rgb(75, 45, 115)),
        RacerDef("☀️ Sol Inti", false, Color.rgb(255, 235, 75), Color.rgb(215, 165, 30))
    )

    private val cars = ArrayList<Car>()

    // Controlos táteis e gamepad
    private var leftDown = false
    private var rightDown = false
    private var boostDown = false

    // Gamepad PS4
    private var ps4SteerX = 0f
    private var ps4Boost = false

    // Desenho
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD) }
    private val path = Path()

    // Botões do HUD
    private val backBtn = RectF()
    private val steerLeftBtn = RectF()
    private val steerRightBtn = RectF()
    private val turboBtn = RectF()
    private val restartBtn = RectF()

    override fun enter() {
        super.enter()
        // Ativa Landscape
        (g.context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        buildTrack()
        resetRace()
        startRace()
        g.say("TogetherKart 3D Cósmico · Corrida Ben 10 Galactic Racing! 🏎️💨")
    }

    override fun leave() {
        super.leave()
        // Restaura Portrait
        (g.context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // ---------------------------------------------------------------- CONSTRUÇÃO DA PISTA (EXPANDIDA 5X)
    private fun ease(a: Double, b: Double, p: Double) = a + (b - a) * ((-cos(p * PI) / 2) + 0.5)
    private fun easeIn(a: Double, b: Double, p: Double) = a + (b - a) * p * p

    private fun addSeg(curve: Double, y: Double) {
        val n = segments.size
        segments.add(Seg(n, n * segLen, (n + 1) * segLen, lastY, y, curve))
        lastY = y
    }

    private fun road(enter: Int, hold: Int, leave: Int, curve: Double, hill: Double) {
        val startY = lastY
        val endY = startY + hill * segLen
        val total = (enter + hold + leave).toDouble()
        for (n in 0 until enter) addSeg(easeIn(0.0, curve, n.toDouble() / enter), ease(startY, endY, n / total))
        for (n in 0 until hold) addSeg(curve, ease(startY, endY, (enter + n) / total))
        for (n in 0 until leave) addSeg(ease(curve, 0.0, n.toDouble() / leave), ease(startY, endY, (enter + hold + n) / total))
    }

    private fun buildTrack() {
        segments.clear(); lastY = 0.0
        // Pista 5x mais longa com montanhas-russas cósmicas estilo Ben 10 Galactic Racing
        val layout = arrayOf(
            intArrayOf(15, 25, 15, 0, 0),
            intArrayOf(25, 45, 25, 4, 15),     // Curva para a direita a subir
            intArrayOf(20, 35, 20, -5, -25),   // Curva para a esquerda a descer (Rollercoaster drop!)
            intArrayOf(30, 55, 30, 6, 20),     // Super Curva em subida
            intArrayOf(25, 40, 25, -7, -15),   // Curva acentuada
            intArrayOf(20, 30, 20, 0, 30),     // Grande colina cósmica
            intArrayOf(20, 30, 20, 0, -30),    // Queda livre
            intArrayOf(30, 60, 30, 7, 0),      // Curva parabólica
            intArrayOf(25, 50, 25, -6, 20),    // Curva oposta
            intArrayOf(20, 40, 20, 5, -20),    // Salto em declive
            intArrayOf(25, 45, 25, -4, 15),    // S-curve 1
            intArrayOf(25, 45, 25, 5, -15),    // S-curve 2
            intArrayOf(30, 55, 30, -5, 0),     // Reta de alta velocidade com curvas
            intArrayOf(20, 35, 20, 4, 10),     // Subida final
            intArrayOf(20, 30, 20, 0, 0)       // Reta da meta
        )
        for (m in intArrayOf(1, -1)) for (s in layout) road(s[0], s[1], s[2], (s[3] * m).toDouble(), s[4].toDouble())
        road(5, 5, 5, 0.0, -lastY / segLen)
        trackLength = segments.size * segLen

        val rnd = Random(15)
        for (s in segments) {
            // Cristais espaciais e árvores cósmicas nas margens (do CosmoRacers)
            if (s.index % 4 == 0 && s.index > 6) {
                val kind = if (rnd.nextBoolean()) 0 else 1
                val col = intArrayOf(0xFFFF4DA6.toInt(), 0xFF4DA3FF.toInt(), 0xFFFFC94D.toInt(), 0xFF3DDC84.toInt(), 0xFFB07CFF.toInt())[rnd.nextInt(5)]
                s.sprites.add(Spr(kind, -(1.4 + rnd.nextDouble() * 1.5), col))
                s.sprites.add(Spr(1 - kind, 1.4 + rnd.nextDouble() * 1.5, col))
            }
            // Bandeiras de Corrida Quadriculadas 🏁 nas bermas
            if (s.index % 8 == 0 && s.index > 2) {
                s.sprites.add(Spr(3, -1.34, Color.WHITE))
                s.sprites.add(Spr(3, 1.34, Color.WHITE))
            }
            // Cenouras douradas (Bunny) / Corações (Cat) e orbes
            if (s.index > 15 && s.index % 25 in 0..2) {
                s.sprites.add(Spr(2, rnd.nextDouble() * 1.1 - 0.55, 0))
            }
            // Plataformas de impulso neon na estrada
            if (s.index > 18 && s.index % 50 in 0..2) s.boost = true
        }
    }

    private fun seg(z: Double): Seg {
        val zz = ((z % trackLength) + trackLength) % trackLength
        return segments[(zz / segLen).toInt() % segments.size]
    }

    // ---------------------------------------------------------------- LÓGICA DA CORRIDA (10 CARROS)
    private fun resetRace() {
        d = d0; playerX = 0.0; speed = 0.0; energy = 60.0; padTimer = 0.0
        raceTime = 0.0; finishedCount = 0; playerPlace = 1; currentLap = 1
        for (s in segments) for (sp in s.sprites) sp.taken = false
        cars.clear()

        // O jogador pilota o pet atual (Bunny ou Cat)
        val myIdx = if (Theme.isBunny) 0 else 1
        val otherIndices = racers.indices.filter { it != myIdx }

        // Os 9 oponentes bots/companheiros na pista
        val fr = doubleArrayOf(0.88, 0.84, 0.81, 0.79, 0.76, 0.74, 0.72, 0.70, 0.68)
        val off = doubleArrayOf(-0.55, 0.55, -0.25, 0.25, -0.75, 0.75, 0.0, -0.40, 0.40)
        otherIndices.forEachIndexed { k, i ->
            val rDef = racers[i]
            val c = Car(rDef.name, rDef.isBunny, rDef.color, rDef.dark, fr[k % fr.size], off[k % off.size])
            c.z = d0 + 260.0 * (k + 1)
            c.offset = off[k % off.size]
            c.phase = k * 1.4
            cars.add(c)
        }
    }

    private fun startRace() {
        resetRace()
        state = State.COUNTDOWN
        timer = 3.6
    }

    override fun update(dt: Float) {
        val dtd = dt.toDouble()
        clock += dtd

        if (state == State.COUNTDOWN) {
            timer -= dtd
            if (timer <= 0) {
                state = State.RACE
                timer = 0.0
                SoundFx.playTurbo()
            }
            return
        }

        val racing = state == State.RACE
        if (racing) raceTime += dtd

        // Turbo / Boost
        val isBoosting = boostDown || ps4Boost
        val canBoost = racing && isBoosting && energy > 0
        if (canBoost) {
            energy = max(0.0, energy - 28.0 * dtd)
            if (Random.nextInt(8) == 0) SoundFx.playTurbo()
        }
        padTimer = max(0.0, padTimer - dtd)

        val curMax = if (!racing) 0.0 else if (canBoost || padTimer > 0) boostMax else maxSpeed
        val accel = if (canBoost || padTimer > 0) maxSpeed / 1.5 else maxSpeed / 3.8
        if (speed < curMax) speed = min(curMax, speed + accel * dtd)
        else speed = max(curMax, speed - maxSpeed * 0.5 * dtd)

        val pct = speed / maxSpeed
        // Direção muito mais responsiva e ágil
        val dx = dtd * 3.4 * max(0.45, pct)
        var steer = 0.0
        if (racing) {
            if (leftDown) steer -= 1.0
            if (rightDown) steer += 1.0
            if (abs(ps4SteerX) > 0.15f) steer += ps4SteerX
            steer = steer.coerceIn(-1.0, 1.0)
        }
        steerVis += (steer - steerVis) * min(1.0, dtd * 10.0)
        playerX += steer * dx

        val pSeg = seg(d)
        // Redução suave da força centrífuga: curvas acentuadas já não atiram o jogador fora da pista!
        playerX -= dx * pct * pSeg.curve * 0.065
        skyOff += pSeg.curve * 0.000035 * speed * dtd

        // Auto-Assist de Estabilização: centra suavemente o kart na faixa quando não se vira ativamente
        if (racing && abs(steer) < 0.1) {
            playerX -= playerX * dtd * 1.3
        }

        // Bumper Físico 3D de Proteção nas Paredes:
        // Mantém o jogador 100% dentro da pista com ressalto arcade elástico e seguro!
        val wallBound = 0.86
        if (playerX > wallBound) {
            playerX = wallBound - 0.02
            speed = max(maxSpeed * 0.70, speed * 0.96)
            burst(g.width * 0.75f, g.height * 0.85f, Icons.SPARK, 4, 12f)
            if (Random.nextInt(4) == 0) SoundFx.playBuild()
        } else if (playerX < -wallBound) {
            playerX = -wallBound + 0.02
            speed = max(maxSpeed * 0.70, speed * 0.96)
            burst(g.width * 0.25f, g.height * 0.85f, Icons.SPARK, 4, 12f)
            if (Random.nextInt(4) == 0) SoundFx.playBuild()
        }
        playerX = playerX.coerceIn(-wallBound, wallBound)

        val oldD = d
        d += speed * dtd

        // Plataformas de impulso na estrada
        if (pSeg.boost && abs(playerX) < 0.6) {
            padTimer = 1.3
            SoundFx.playTurbo()
        }

        // Apanhar itens de turbo (Cenoura ou Coração)
        for (sp in pSeg.sprites) {
            if (sp.kind == 2 && !sp.taken && abs(playerX - sp.offset) < 0.38) {
                sp.taken = true
                energy = min(100.0, energy + 28.0)
                SoundFx.playCoin()
                burst(g.width / 2f, g.height * 0.8f, Icons.STAR, 4, 14f)
            }
        }

        // Contagem de voltas
        if (oldD < trackLength && d >= trackLength) {
            currentLap++
            if (currentLap > totalLaps) {
                state = State.FINISH
                val hasBonus = Save.activePersonality.contains("Brincalh") || Save.activeHobby.contains("TogetherKart")
                val coinsEarned = if (hasBonus) 75 else 50
                g.earn(coinsEarned)
                SoundFx.playKiss()
                burst(g.width / 2f, g.height / 2f, Icons.HEART, 15, 20f)
                val bonusNote = if (hasBonus) " (Bónus Ficha: +25! 🏎️)" else ""
                g.say("🏁 Corrida Concluída! +$coinsEarned moedas!$bonusNote ${if (playerPlace == 1) "🏆 Vitória a dois!" else "Grande corrida!"}")
            } else {
                g.say("Volta $currentLap / $totalLaps!")
                SoundFx.playPlay(Theme.isCat)
            }
        }
        d %= trackLength

        // Oponentes
        for (c in cars) {
            c.phase += dtd
            c.offset = c.baseOffset + sin(c.phase) * 0.45
            val cMax = maxSpeed * c.frac
            c.speed = cMax
            val oldCz = c.z
            c.z = (c.z + c.speed * dtd) % trackLength

            // Colisão com o jogador
            if (racing && abs((d % trackLength) - c.z) < 220.0 && abs(playerX - c.offset) < 0.42) {
                speed = max(maxSpeed * 0.25, speed * 0.72)
                c.speed = max(maxSpeed * 0.25, c.speed * 0.72)
                SoundFx.playMine()
            }
        }

        // Posição na corrida
        var place = 1
        for (c in cars) {
            val playerTotal = (currentLap - 1) * trackLength + (d % trackLength)
            val carTotal = (currentLap - 1) * trackLength + (c.z % trackLength)
            if (carTotal > playerTotal) place++
        }
        playerPlace = place

        // Sincronização periódica do parceiro
        partnerSyncTimer += dt
        if (partnerSyncTimer >= 1.5f) {
            partnerSyncTimer = 0f
            if (Save.partnerConnected) {
                Save.syncOnline { _, _ -> }
            }
        }

        updFx(dt)
    }

    // ---------------------------------------------------------------- PROJEÇÃO 3D
    private fun project(p: P, cy: Double, cz: Double, camX: Double, camY: Double, camZ: Double, w: Float, h: Float) {
        p.cx = -camX
        p.cy = cy - camY
        p.cz = cz - camZ
        p.scale = if (p.cz > 0.0) cameraDepth / p.cz else 0.0
        p.sx = (w / 2 + p.scale * p.cx * roadW * (w / 1000f) / 2).toFloat()
        p.sy = (h / 2 - p.scale * p.cy * roadW * (w / 1000f) / 2).toFloat()
        p.sw = (p.scale * roadW * (w / 1000f) / 2).toFloat()
    }

    override fun draw(c: Canvas, r: RectF) {
        val w = r.width()
        val h = r.height()
        val ww = w / 1000f

        // 1. Céu Andino & Montanhas de Machu Picchu
        drawPanoramicSky(c, w, h)

        // 2. Projeção e desenho da estrada
        val baseSeg = seg(d)
        val basePct = (d % segLen) / segLen
        val playerY = ease(baseSeg.y1, baseSeg.y2, basePct)
        var maxy = h

        var x = 0.0
        var dx = -(baseSeg.curve * basePct)
        val camX = playerX * roadW / 2
        val camZ = d - playerZ

        for (n in 0 until drawDist) {
            val s = segments[(baseSeg.index + n) % segments.size]
            val looped = s.index < baseSeg.index
            val cz1 = s.z1 + (if (looped) trackLength else 0.0)
            val cz2 = s.z2 + (if (looped) trackLength else 0.0)

            project(s.p1, s.y1, cz1, camX - x, playerY + camH, camZ, w, h)
            project(s.p2, s.y2, cz2, camX - x - dx, playerY + camH, camZ, w, h)
            x += dx
            dx += s.curve
            s.clip = maxy
            if (s.p1.cz <= cameraDepth || s.p2.sy >= s.p1.sy || s.p2.sy >= maxy) continue
            renderSegment(c, w, s)
            maxy = s.p1.sy
        }

        // 4. Calcular tamanho proporcional do kart do jogador e dos oponentes
        // "quero pista grande carros pequenos mas não muito pequenos"
        val pw = (w * 0.165f).coerceIn(150f, 310f)
        val scPlayer = cameraDepth / playerZ

        // 3. Sprites de trás para a frente (Vegetação, Bandeiras, Cristais, Oponentes)
        for (n in drawDist - 1 downTo 0) {
            val s = segments[(baseSeg.index + n) % segments.size]
            if (s.p1.cz <= cameraDepth) continue
            c.save()
            c.clipRect(0f, 0f, w, s.clip)

            for (sp in s.sprites) {
                if (sp.kind == 2 && sp.taken) continue
                val sc = s.p1.scale
                val sx = s.p1.sx + (sc * sp.offset * roadW * ww / 2).toFloat()
                drawScenery(c, sp, sx, s.p1.sy, (sc * roadW * ww / 2).toFloat())
            }

            for (car in cars) {
                if (seg(car.z).index != s.index) continue
                val pcts = ((car.z % trackLength) % segLen) / segLen
                val sc = s.p1.scale + (s.p2.scale - s.p1.scale) * pcts
                val sx = s.p1.sx + (s.p2.sx - s.p1.sx) * pcts.toFloat() + (sc * car.offset * roadW * ww / 2).toFloat()
                val sy = s.p1.sy + (s.p2.sy - s.p1.sy) * pcts.toFloat()
                // Escala otimizada dos outros carros: permanecem sempre visíveis e proporcionais na pista
                val relSc = (sc / scPlayer).toFloat()
                val cw = (pw * (0.42f + 0.65f * relSc.coerceIn(0f, 1.2f))).coerceIn(pw * 0.42f, pw * 1.35f)
                drawKartRacer(c, sx, sy, cw, car.isBunny, car.color, car.dark, 0.0, false, car.name)
            }
            c.restore()
        }

        // 4. Jogador Principal no centro
        val bounce = (sin(clock * 38) * 1.5 * (speed / maxSpeed)).toFloat() * h * 0.005f
        val myIsBunny = Theme.isBunny
        val myColor = if (myIsBunny) Color.rgb(26, 42, 85) else Color.rgb(255, 135, 185)
        val myDark = if (myIsBunny) Color.rgb(105, 20, 50) else Color.rgb(235, 75, 135)
        drawKartRacer(c, w / 2, h * 0.88f + bounce, pw, myIsBunny, myColor, myDark, steerVis, speed > maxSpeed * 1.05)

        // Efeitos de velocidade
        if (speed > maxSpeed * 1.05) speedLines(c, w, h)

        drawFx(c)

        // 5. Interface HUD Landscape (limpa, sem texto de comandos)
        drawHud(c, w, h)
    }

    private var skyShader: LinearGradient? = null

    private fun drawPanoramicSky(c: Canvas, w: Float, h: Float) {
        if (skyShader == null) {
            skyShader = LinearGradient(0f, 0f, 0f, h * 0.55f, 0xFF0D0320.toInt(), 0xFF2A1054.toInt(), Shader.TileMode.CLAMP)
        }
        fill.shader = skyShader
        c.drawRect(0f, 0f, w, h * 0.55f + 4f, fill)
        fill.shader = null

        // Planetas cósmicos em paralaxe (CosmoRacers)
        val off = (skyOff * w * 6).toFloat()
        // Planeta magenta
        fill.color = 0xFFFFA1C6.toInt()
        c.drawCircle(((w * 0.22f + off) % (w * 1.2f) + w * 1.2f) % (w * 1.2f) - w * 0.1f, h * 0.17f, h * 0.09f, fill)
        // Lua roxa
        fill.color = 0xFFC76BFF.toInt()
        c.drawCircle(((w * 0.70f + off * 0.6f) % (w * 1.2f) + w * 1.2f) % (w * 1.2f) - w * 0.1f, h * 0.12f, h * 0.05f, fill)

        // Colinas cósmicas em paralaxe
        hills(c, w, h, 0xFF3A1B78.toInt(), h * 0.07f, 0.004f, skyOff * 400)
        hills(c, w, h, 0xFF2A1458.toInt(), h * 0.045f, 0.007f, skyOff * 900)
        fill.color = 0xFF31226A.toInt()
        c.drawRect(0f, h * 0.50f, w, h, fill)
    }

    private fun hills(c: Canvas, w: Float, h: Float, color: Int, amp: Float, freq: Float, off: Double) {
        path.reset()
        path.moveTo(0f, h * 0.50f)
        var x = 0f
        while (x <= w + 20f) {
            val y = h * 0.50f - amp * (0.6f + 0.4f * sin(x * freq + off).toFloat() + 0.3f * sin(x * freq * 2.3 + off * 1.7).toFloat())
            path.lineTo(x, y)
            x += 20f
        }
        path.lineTo(w, h * 0.50f); path.close()
        fill.color = color
        c.drawPath(path, fill)
    }

    private fun renderSegment(c: Canvas, w: Float, s: Seg) {
        val p1 = s.p1; val p2 = s.p2
        val light = (s.index / 3) % 2 == 0
        val start = s.index < 4

        // Espaço cósmico nas margens da pista (CosmoRacers)
        fill.color = if (light) 0xFF3B2A78.toInt() else 0xFF31226A.toInt()
        c.drawRect(0f, p2.sy, w, p1.sy, fill)

        // Bermas largas e luminosas (rumble neon vibrante)
        val rumCol = if (start) (if (s.index % 2 == 0) Color.WHITE else Color.BLACK) else if (light) Color.rgb(255, 45, 120) else Color.rgb(240, 245, 255)
        val r1 = p1.sw * 1.22f; val r2 = p2.sw * 1.22f
        quad(c, rumCol, p1.sx - r1, p1.sy, p1.sx + r1, p1.sy, p2.sx + r2, p2.sy, p2.sx - r2, p2.sy)

        // 3D Paredes / Guardrails futuristas altos de proteção ("melhores a visualização da pista com paredes")
        val wallH1 = p1.sw * 0.44f
        val wallH2 = p2.sw * 0.44f
        val wallBaseCol = if (light) Color.rgb(48, 60, 90) else Color.rgb(32, 42, 68)
        val wallNeonCol = if (light) Color.rgb(0, 240, 255) else Color.rgb(255, 35, 135)

        // 1. Parede Esquerda 3D Alta
        quad(c, wallBaseCol,
            p1.sx - r1, p1.sy,
            p1.sx - r1, p1.sy - wallH1,
            p2.sx - r2, p2.sy - wallH2,
            p2.sx - r2, p2.sy
        )
        // Faixa de luz néon intermediária na Parede Esquerda
        val midH1 = wallH1 * 0.52f; val midH2 = wallH2 * 0.52f
        val stH1 = p1.sw * 0.05f; val stH2 = p2.sw * 0.05f
        quad(c, wallNeonCol,
            p1.sx - r1 * 0.995f, p1.sy - midH1,
            p1.sx - r1 * 0.995f, p1.sy - midH1 - stH1,
            p2.sx - r2 * 0.995f, p2.sy - midH2 - stH2,
            p2.sx - r2 * 0.995f, p2.sy - midH2
        )
        // Topo néon luminoso da Parede Esquerda
        val rimH1 = p1.sw * 0.07f
        val rimH2 = p2.sw * 0.07f
        quad(c, wallNeonCol,
            p1.sx - r1, p1.sy - wallH1,
            p1.sx - r1 * 0.975f, p1.sy - wallH1 - rimH1,
            p2.sx - r2 * 0.975f, p2.sy - wallH2 - rimH2,
            p2.sx - r2, p2.sy - wallH2
        )

        // 2. Parede Direita 3D Alta
        quad(c, wallBaseCol,
            p1.sx + r1, p1.sy,
            p1.sx + r1, p1.sy - wallH1,
            p2.sx + r2, p2.sy - wallH2,
            p2.sx + r2, p2.sy
        )
        // Faixa de luz néon intermediária na Parede Direita
        quad(c, wallNeonCol,
            p1.sx + r1 * 0.995f, p1.sy - midH1,
            p1.sx + r1 * 0.995f, p1.sy - midH1 - stH1,
            p2.sx + r2 * 0.995f, p2.sy - midH2 - stH2,
            p2.sx + r2 * 0.995f, p2.sy - midH2
        )
        // Topo néon luminoso da Parede Direita
        quad(c, wallNeonCol,
            p1.sx + r1, p1.sy - wallH1,
            p1.sx + r1 * 0.975f, p1.sy - wallH1 - rimH1,
            p2.sx + r2 * 0.975f, p2.sy - wallH2 - rimH2,
            p2.sx + r2, p2.sy - wallH2
        )

        // 3. Postes verticais de luz e setas de curva nas paredes
        if (s.index % 5 == 0) {
            val pilH1 = wallH1 * 1.35f
            val pilW1 = p1.sw * 0.06f
            // Poste esquerdo
            fill.color = Color.rgb(20, 25, 40)
            c.drawRect(p1.sx - r1 - pilW1 / 2f, p1.sy - pilH1, p1.sx - r1 + pilW1 / 2f, p1.sy, fill)
            fill.color = Color.rgb(0, 245, 255)
            c.drawCircle(p1.sx - r1, p1.sy - pilH1, pilW1 * 0.9f, fill)
            // Poste direito
            fill.color = Color.rgb(20, 25, 40)
            c.drawRect(p1.sx + r1 - pilW1 / 2f, p1.sy - pilH1, p1.sx + r1 + pilW1 / 2f, p1.sy, fill)
            fill.color = Color.rgb(255, 40, 140)
            c.drawCircle(p1.sx + r1, p1.sy - pilH1, pilW1 * 0.9f, fill)
        }

        // Setas néon de curva perigosa (chevrons)
        if (abs(s.curve) > 2.0 && s.index % 2 == 0) {
            val arrowCol = Color.rgb(255, 235, 45)
            if (s.curve > 0) { // Curva para a direita: aviso na parede esquerda
                quad(c, arrowCol,
                    p1.sx - r1 * 0.98f, p1.sy - wallH1 * 0.8f,
                    p1.sx - r1 * 0.98f, p1.sy - wallH1 * 0.2f,
                    p2.sx - r2 * 0.98f, p2.sy - wallH2 * 0.2f,
                    p2.sx - r2 * 0.98f, p2.sy - wallH2 * 0.8f
                )
            } else { // Curva para a esquerda: aviso na parede direita
                quad(c, arrowCol,
                    p1.sx + r1 * 0.98f, p1.sy - wallH1 * 0.8f,
                    p1.sx + r1 * 0.98f, p1.sy - wallH1 * 0.2f,
                    p2.sx + r2 * 0.98f, p2.sy - wallH2 * 0.2f,
                    p2.sx + r2 * 0.98f, p2.sy - wallH2 * 0.8f
                )
            }
        }

        // Asfalto nítido e escuro de alta aderência
        val rdCol = if (start) (if (s.index % 2 == 0) Color.WHITE else Color.rgb(34, 34, 38)) else if (light) Color.rgb(75, 78, 98) else Color.rgb(65, 68, 88)
        quad(c, rdCol, p1.sx - p1.sw, p1.sy, p1.sx + p1.sw, p1.sy, p2.sx + p2.sw, p2.sy, p2.sx - p2.sw, p2.sy)

        // Linhas de faixa brancas
        if (light && !start) {
            val l1 = p1.sw / 32f; val l2 = p2.sw / 32f
            val lanes = 3
            val lw1 = p1.sw * 2 / lanes; val lw2 = p2.sw * 2 / lanes
            var lx1 = p1.sx - p1.sw + lw1; var lx2 = p2.sx - p2.sw + lw2
            for (i in 1 until lanes) {
                quad(c, Color.WHITE, lx1 - l1 / 2, p1.sy, lx1 + l1 / 2, p1.sy, lx2 + l2 / 2, p2.sy, lx2 - l2 / 2, p2.sy)
                lx1 += lw1; lx2 += lw2
            }
        }

        // Plataforma de impulso neon cyan (CosmoRacers)
        if (s.boost) {
            val a = 0.32f
            val bCol = if (light) 0xFF38E8FF.toInt() else 0xFF8CF4FF.toInt()
            quad(c, bCol, p1.sx - p1.sw * a, p1.sy, p1.sx + p1.sw * a, p1.sy, p2.sx + p2.sw, p2.sy, p2.sx - p2.sw * a, p2.sy)
        }

        // Pórtico de Meta 3D sobre a pista (Segmento 0) com bandeiras quadriculadas
        if (s.index == 0) {
            val archTop1 = p1.sy - p1.sw * 0.72f
            val archTop2 = p2.sy - p2.sw * 0.72f
            // Pilar esquerdo
            quad(c, Color.rgb(215, 220, 230), p1.sx - r1 - p1.sw * 0.08f, p1.sy, p1.sx - r1, p1.sy, p1.sx - r1, archTop1, p1.sx - r1 - p1.sw * 0.08f, archTop1)
            // Pilar direito
            quad(c, Color.rgb(215, 220, 230), p1.sx + r1, p1.sy, p1.sx + r1 + p1.sw * 0.08f, p1.sy, p1.sx + r1 + p1.sw * 0.08f, archTop1, p1.sx + r1, archTop1)
            // Travessa superior do pórtico
            quad(c, Color.rgb(30, 35, 48), p1.sx - r1, archTop1 + p1.sw * 0.16f, p1.sx + r1, archTop1 + p1.sw * 0.16f, p1.sx + r1, archTop1, p1.sx - r1, archTop1)
            // Padrão quadriculado preto e branco de meta
            val chkW = (r1 * 2f) / 10f
            for (ci in 0 until 10) {
                val cx1 = p1.sx - r1 + ci * chkW
                val cx2 = cx1 + chkW
                val chkCol = if (ci % 2 == 0) Color.WHITE else Color.BLACK
                quad(c, chkCol, cx1, archTop1 + p1.sw * 0.15f, cx2, archTop1 + p1.sw * 0.15f, cx2, archTop1 + p1.sw * 0.01f, cx1, archTop1 + p1.sw * 0.01f)
            }
        }
    }

    private fun quad(c: Canvas, col: Int, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) {
        fill.color = col
        path.reset()
        path.moveTo(x1, y1); path.lineTo(x2, y2); path.lineTo(x3, y3); path.lineTo(x4, y4); path.close()
        c.drawPath(path, fill)
    }

    private fun speedLines(c: Canvas, w: Float, h: Float) {
        fill.color = 0x66FFFFFF
        fill.strokeWidth = h * 0.005f
        val rnd = Random((clock * 28).toLong())
        for (i in 0 until 12) {
            val a = rnd.nextFloat() * 6.283f
            val r1 = h * (0.32f + rnd.nextFloat() * 0.22f)
            val r2 = r1 + h * (0.10f + rnd.nextFloat() * 0.16f)
            c.drawLine(w / 2 + cos(a) * r1 * 1.5f, h / 2 + sin(a) * r1, w / 2 + cos(a) * r2 * 1.5f, h / 2 + sin(a) * r2, fill)
        }
    }

    private fun drawScenery(c: Canvas, sp: Spr, x: Float, y: Float, unit: Float) {
        when (sp.kind) {
            0 -> { // Cristal Inca
                val bw = unit * 0.50f; val hh = unit * 1.25f
                path.reset()
                path.moveTo(x - bw / 2, y); path.lineTo(x - bw / 4, y - hh * 0.6f); path.lineTo(x, y - hh)
                path.lineTo(x + bw / 4, y - hh * 0.55f); path.lineTo(x + bw / 2, y); path.close()
                fill.color = sp.color; c.drawPath(path, fill)
            }
            1 -> { // Árvore Andina
                val tw = unit * 0.09f; val th = unit * 1.4f
                fill.color = Color.rgb(95, 65, 45)
                c.drawRect(x - tw / 2, y - th, x + tw / 2, y, fill)
                fill.color = sp.color
                c.drawOval(x - unit * 0.35f, y - th - unit * 0.35f, x + unit * 0.35f, y - th + unit * 0.2f, fill)
            }
            2 -> { // Cenoura Dourada (Bunny) ou Coração (Cat)
                val r = unit * 0.16f
                val cy = y - unit * 0.35f
                fill.color = 0x55FFE866; c.drawCircle(x, cy, r * 1.8f, fill)
                fill.color = Color.rgb(255, 215, 45); c.drawCircle(x, cy, r, fill)
                fill.color = Color.WHITE; c.drawCircle(x - r * 0.3f, cy - r * 0.3f, r * 0.3f, fill)
            }
            else -> { // 3: Bandeira de Corrida Quadriculada (🏁) em mastro ("poe bandeiras")
                val poleW = unit * 0.05f
                val poleH = unit * 1.6f
                fill.color = Color.rgb(215, 220, 230)
                c.drawRect(x - poleW / 2, y - poleH, x + poleW / 2, y, fill)
                fill.color = Color.rgb(255, 215, 45)
                c.drawCircle(x, y - poleH, poleW * 1.5f, fill)

                val flagW = unit * 0.55f
                val flagH = unit * 0.38f
                val wave = (sin(clock * 14.0 + x * 0.05) * unit * 0.06f).toFloat()
                val fx = if (sp.offset < 0) x - flagW else x
                val fy = y - poleH + poleW

                val rows = 3; val cols = 4
                val cellW = flagW / cols; val cellH = flagH / rows
                for (ro in 0 until rows) {
                    for (co in 0 until cols) {
                        fill.color = if ((ro + co) % 2 == 0) Color.WHITE else Color.BLACK
                        val cLeft = fx + co * cellW + (ro * wave * 0.3f)
                        val cTop = fy + ro * cellH
                        c.drawRect(cLeft, cTop, cLeft + cellW, cTop + cellH, fill)
                    }
                }
            }
        }
    }

    /** Desenha o carro desportivo/kart do Bunny ou da Cat com piloto claramente visível ao volante. */
    private fun drawKartRacer(
        c: Canvas, cx: Float, by: Float, w: Float,
        isBunny: Boolean, color: Int, dark: Int,
        steer: Double, boost: Boolean, name: String = ""
    ) {
        if (w < 4f) return
        val h = w * 0.64f
        c.save()
        c.rotate((steer * 9).toFloat(), cx, by)

        // 1. Sombra suave no asfalto
        fill.color = 0x55000000
        c.drawOval(cx - w * 0.55f, by - h * 0.16f, cx + w * 0.55f, by + h * 0.16f, fill)

        // 2. Rodas traseiras largas de alta aderência
        fill.color = Color.rgb(20, 20, 25)
        val wheelW = w * 0.15f
        val wheelH = h * 0.52f
        c.drawRoundRect(cx - w * 0.53f, by - wheelH * 0.95f, cx - w * 0.53f + wheelW, by + wheelH * 0.05f, 5f, 5f, fill)
        c.drawRoundRect(cx + w * 0.53f - wheelW, by - wheelH * 0.95f, cx + w * 0.53f, by + wheelH * 0.05f, 5f, 5f, fill)
        // Jantes prateadas
        fill.color = Color.rgb(180, 185, 195)
        c.drawCircle(cx - w * 0.455f, by - wheelH * 0.45f, wheelW * 0.32f, fill)
        c.drawCircle(cx + w * 0.455f, by - wheelH * 0.45f, wheelW * 0.32f, fill)

        // 3. Chassi Inferior e Tubos de Escape Cromados
        fill.color = Color.rgb(40, 42, 50)
        c.drawRoundRect(cx - w * 0.44f, by - h * 0.35f, cx + w * 0.44f, by - h * 0.08f, 6f, 6f, fill)

        // Escapes duplos com chamas
        fill.color = Color.rgb(140, 145, 155)
        c.drawCircle(cx - w * 0.22f, by - h * 0.20f, w * 0.065f, fill)
        c.drawCircle(cx + w * 0.22f, by - h * 0.20f, w * 0.065f, fill)

        val flameColor = if (boost) Color.rgb(85, 235, 255) else Color.rgb(255, 145, 30)
        val coreColor = if (boost) Color.WHITE else Color.rgb(255, 240, 90)
        val flameR = w * (if (boost) 0.16f else 0.09f)

        fill.color = flameColor
        c.drawCircle(cx - w * 0.22f, by - h * 0.20f + flameR * 0.4f, flameR, fill)
        c.drawCircle(cx + w * 0.22f, by - h * 0.20f + flameR * 0.4f, flameR, fill)
        fill.color = coreColor
        c.drawCircle(cx - w * 0.22f, by - h * 0.20f + flameR * 0.4f, flameR * 0.55f, fill)
        c.drawCircle(cx + w * 0.22f, by - h * 0.20f + flameR * 0.4f, flameR * 0.55f, fill)

        // 4. Carroçaria Principal Aerodinâmica do Kart (Cores do Bunny/Cat)
        fill.color = color
        c.drawRoundRect(cx - w * 0.42f, by - h * 0.72f, cx + w * 0.42f, by - h * 0.12f, w * 0.08f, w * 0.08f, fill)

        // Faixas desportivas e saias laterais
        fill.color = dark
        c.drawRoundRect(cx - w * 0.40f, by - h * 0.42f, cx + w * 0.40f, by - h * 0.16f, 6f, 6f, fill)
        c.drawRoundRect(cx - w * 0.08f, by - h * 0.72f, cx + w * 0.08f, by - h * 0.12f, 4f, 4f, fill)

        // 5. Aileron / Asa Traseira Aerodinâmica com orelhas decorativas
        fill.color = dark
        c.drawRoundRect(cx - w * 0.48f, by - h * 0.94f, cx + w * 0.48f, by - h * 0.78f, 5f, 5f, fill)
        // Suportes do aileron
        fill.color = Color.rgb(30, 32, 40)
        c.drawRect(cx - w * 0.32f, by - h * 0.82f, cx - w * 0.26f, by - h * 0.65f, fill)
        c.drawRect(cx + w * 0.26f, by - h * 0.82f, cx + w * 0.32f, by - h * 0.65f, fill)

        // Orelhinhas temáticas no aileron traseiro
        if (isBunny) {
            fill.color = Color.WHITE
            c.drawRoundRect(cx - w * 0.42f, by - h * 1.18f, cx - w * 0.32f, by - h * 0.82f, 6f, 6f, fill)
            c.drawRoundRect(cx + w * 0.32f, by - h * 1.18f, cx + w * 0.42f, by - h * 0.82f, 6f, 6f, fill)
            fill.color = Color.rgb(255, 180, 205)
            c.drawRoundRect(cx - w * 0.40f, by - h * 1.14f, cx - w * 0.34f, by - h * 0.86f, 4f, 4f, fill)
            c.drawRoundRect(cx + w * 0.34f, by - h * 1.14f, cx + w * 0.40f, by - h * 0.86f, 4f, 4f, fill)
        } else {
            path.reset()
            path.moveTo(cx - w * 0.45f, by - h * 0.88f); path.lineTo(cx - w * 0.35f, by - h * 1.14f); path.lineTo(cx - w * 0.25f, by - h * 0.88f); path.close()
            fill.color = Color.rgb(255, 185, 215); c.drawPath(path, fill)
            path.reset()
            path.moveTo(cx + w * 0.25f, by - h * 0.88f); path.lineTo(cx + w * 0.35f, by - h * 1.14f); path.lineTo(cx + w * 0.45f, by - h * 0.88f); path.close()
            fill.color = Color.rgb(255, 185, 215); c.drawPath(path, fill)
        }

        // 6. Cockpit Aberto
        fill.color = Color.rgb(28, 30, 38)
        c.drawOval(cx - w * 0.24f, by - h * 0.88f, cx + w * 0.24f, by - h * 0.38f, fill)

        // Volante de Corrida
        fill.color = Color.rgb(45, 48, 60)
        c.drawOval(cx - w * 0.15f, by - h * 0.70f, cx + w * 0.15f, by - h * 0.50f, fill)

        // 7. PILOTO BUNNY / CAT CLARAMENTE VISÍVEL DENTRO DO CARRO
        val headY = by - h * 0.84f
        val headR = w * 0.14f

        // Ombros do piloto com jersey desportivo
        fill.color = if (isBunny) Color.rgb(35, 55, 110) else Color.rgb(255, 145, 180)
        c.drawRoundRect(cx - w * 0.20f, by - h * 0.68f, cx + w * 0.20f, by - h * 0.36f, 8f, 8f, fill)

        // Patinhas segurando o volante
        val pawColor = if (isBunny) Color.WHITE else Color.rgb(255, 245, 248)
        fill.color = pawColor
        c.drawCircle(cx - w * 0.13f, by - h * 0.56f, w * 0.045f, fill)
        c.drawCircle(cx + w * 0.13f, by - h * 0.56f, w * 0.045f, fill)

        // Cabeça do Piloto
        fill.color = pawColor
        c.drawCircle(cx, headY, headR, fill)

        if (isBunny) {
            // Orelhas longas do Bunny a abanar ao vento!
            val windFlap = (sin(clock * 32.0) * 4.5 * (1.0 + (speed / maxSpeed))).toFloat()
            val earW = headR * 0.44f
            val earH = headR * 1.85f
            // Orelha Esquerda
            c.save()
            c.rotate(-8f + windFlap * 0.8f, cx - headR * 0.45f, headY - headR * 0.5f)
            fill.color = Color.WHITE
            c.drawRoundRect(cx - headR * 0.7f, headY - headR * 0.5f - earH, cx - headR * 0.7f + earW, headY - headR * 0.5f, earW / 2, earW / 2, fill)
            fill.color = Color.rgb(255, 180, 205)
            c.drawRoundRect(cx - headR * 0.66f, headY - headR * 0.5f - earH * 0.88f, cx - headR * 0.7f + earW * 0.85f, headY - headR * 0.5f, earW * 0.35f, earW * 0.35f, fill)
            c.restore()

            // Orelha Direita
            c.save()
            c.rotate(8f - windFlap * 0.8f, cx + headR * 0.45f, headY - headR * 0.5f)
            fill.color = Color.WHITE
            c.drawRoundRect(cx + headR * 0.7f - earW, headY - headR * 0.5f - earH, cx + headR * 0.7f, headY - headR * 0.5f, earW / 2, earW / 2, fill)
            fill.color = Color.rgb(255, 180, 205)
            c.drawRoundRect(cx + headR * 0.7f - earW * 0.85f, headY - headR * 0.5f - earH * 0.88f, cx + headR * 0.66f, headY - headR * 0.5f, earW * 0.35f, earW * 0.35f, fill)
            c.restore()

            // Óculos de corrida / Viseira no capacete do Bunny
            fill.color = Color.rgb(40, 95, 215)
            c.drawRoundRect(cx - headR * 0.85f, headY - headR * 0.45f, cx + headR * 0.85f, headY + headR * 0.05f, 6f, 6f, fill)
            fill.color = Color.rgb(135, 215, 255)
            c.drawRoundRect(cx - headR * 0.75f, headY - headR * 0.35f, cx - headR * 0.08f, headY - headR * 0.02f, 4f, 4f, fill)
            c.drawRoundRect(cx + headR * 0.08f, headY - headR * 0.35f, cx + headR * 0.75f, headY - headR * 0.02f, 4f, 4f, fill)

            // Rabinho de algodão pom-pom na traseira
            fill.color = Color.WHITE
            c.drawCircle(cx, by - h * 0.32f, w * 0.07f, fill)

        } else {
            // Orelhinhas pontiagudas de Gatinha
            path.reset()
            path.moveTo(cx - headR * 0.95f, headY - headR * 0.2f)
            path.lineTo(cx - headR * 0.65f, headY - headR * 1.55f)
            path.lineTo(cx - headR * 0.15f, headY - headR * 0.55f)
            path.close()
            fill.color = Color.rgb(255, 240, 245); c.drawPath(path, fill)
            path.reset()
            path.moveTo(cx - headR * 0.85f, headY - headR * 0.25f)
            path.lineTo(cx - headR * 0.65f, headY - headR * 1.35f)
            path.lineTo(cx - headR * 0.25f, headY - headR * 0.52f)
            path.close()
            fill.color = Color.rgb(255, 175, 205); c.drawPath(path, fill)

            path.reset()
            path.moveTo(cx + headR * 0.15f, headY - headR * 0.55f)
            path.lineTo(cx + headR * 0.65f, headY - headR * 1.55f)
            path.lineTo(cx + headR * 0.95f, headY - headR * 0.2f)
            path.close()
            fill.color = Color.rgb(255, 240, 245); c.drawPath(path, fill)
            path.reset()
            path.moveTo(cx + headR * 0.25f, headY - headR * 0.52f)
            path.lineTo(cx + headR * 0.65f, headY - headR * 1.35f)
            path.lineTo(cx + headR * 0.85f, headY - headR * 0.25f)
            path.close()
            fill.color = Color.rgb(255, 175, 205); c.drawPath(path, fill)

            // Lacinho rosa fofo
            fill.color = Color.rgb(255, 65, 135)
            c.drawCircle(cx - headR * 0.70f, headY - headR * 0.85f, headR * 0.22f, fill)

            // Óculos de piloto da Gatinha
            fill.color = Color.rgb(235, 75, 135)
            c.drawRoundRect(cx - headR * 0.85f, headY - headR * 0.45f, cx + headR * 0.85f, headY + headR * 0.05f, 6f, 6f, fill)
            fill.color = Color.rgb(255, 215, 235)
            c.drawRoundRect(cx - headR * 0.75f, headY - headR * 0.35f, cx - headR * 0.08f, headY - headR * 0.02f, 4f, 4f, fill)
            c.drawRoundRect(cx + headR * 0.08f, headY - headR * 0.35f, cx + headR * 0.75f, headY - headR * 0.02f, 4f, 4f, fill)

            // Cauda da gatinha a abanar suavemente
            val tailWag = (sin(clock * 12.0) * headR * 0.4f).toFloat()
            path.reset()
            path.moveTo(cx + w * 0.15f, by - h * 0.34f)
            path.quadTo(cx + w * 0.30f + tailWag, by - h * 0.50f, cx + w * 0.22f + tailWag * 1.2f, by - h * 0.65f)
            fill.color = Color.rgb(255, 240, 245)
            fill.style = Paint.Style.STROKE
            fill.strokeWidth = w * 0.05f
            c.drawPath(path, fill)
            fill.style = Paint.Style.FILL
        }

        // Pílula com o nome do piloto oponente no topo do carro
        if (name.isNotEmpty()) {
            val tagY = headY - headR * 1.6f
            val tagSize = (w * 0.16f).coerceIn(13f, 24f)
            val tagW = D.textW(name, tagSize) + w * 0.14f
            val tagH = tagSize * 1.45f
            val tagRect = RectF(cx - tagW / 2f, tagY - tagH / 2f, cx + tagW / 2f, tagY + tagH / 2f)
            fill.color = Color.argb(205, 20, 24, 38)
            c.drawRoundRect(tagRect, tagH * 0.5f, tagH * 0.5f, fill)
            fill.color = color
            fill.style = Paint.Style.STROKE
            fill.strokeWidth = 2.5f
            c.drawRoundRect(tagRect, tagH * 0.5f, tagH * 0.5f, fill)
            fill.style = Paint.Style.FILL
            D.text(c, name, cx, tagY + tagSize * 0.35f, tagSize, Color.WHITE)
        }

        c.restore()
    }

    // ---------------------------------------------------------------- INTERFACE HUD LANDSCAPE (SEM TEXTO DE CONTROLOS)
    private fun drawHud(c: Canvas, w: Float, h: Float) {
        val u = h / 10f

        // 1. Calcular Classificação em tempo real de 1º a 10º Lugares
        val myName = if (Theme.isBunny) "🐰 Bunny" else "🐱 Cat"
        val playerTotal = (currentLap - 1) * trackLength + (d % trackLength)

        class RankEntry(val name: String, val dist: Double, val isMe: Boolean, val col: Int)
        val standings = ArrayList<RankEntry>()
        standings.add(RankEntry(myName, playerTotal, true, if (Theme.isBunny) Color.rgb(105, 175, 255) else Color.rgb(255, 165, 215)))
        for (car in cars) {
            val carTotal = (currentLap - 1) * trackLength + (car.z % trackLength)
            standings.add(RankEntry(car.name, carTotal, false, car.color))
        }
        standings.sortByDescending { it.dist }
        playerPlace = standings.indexOfFirst { it.isMe } + 1

        // Botão Voltar (Apenas ícone limpo)
        backBtn.set(24f, 16f, 84f, 16f + u * 0.75f)
        fill.color = Color.rgb(32, 40, 60)
        c.drawRoundRect(backBtn, backBtn.height() * 0.35f, backBtn.height() * 0.35f, fill)
        label(c, "←", backBtn.centerX(), backBtn.centerY() + u * 0.14f, u * 0.38f, Color.WHITE, Paint.Align.CENTER)

        // Contador de Voltas & Posição
        val lapTxt = "$currentLap / $totalLaps"
        label(c, lapTxt, w * 0.12f, u * 0.60f, u * 0.38f, Color.rgb(255, 225, 80))
        val placeTxt = "${playerPlace}º"
        val placeCol = if (playerPlace == 1) Color.rgb(255, 215, 45) else Color.WHITE
        label(c, placeTxt, w * 0.24f, u * 0.60f, u * 0.44f, placeCol)

        // Velocímetro
        val kmh = (speed / maxSpeed * 240).toInt()
        label(c, "$kmh km/h", w * 0.35f, u * 0.60f, u * 0.38f, Color.rgb(105, 240, 175))

        // Barra de Energia do Turbo (sem texto "TURBO" a poluir)
        val barW = w * 0.16f
        val barH = u * 0.22f
        val barX = w * 0.48f
        val barY = u * 0.42f
        fill.color = Color.rgb(35, 38, 48)
        c.drawRoundRect(barX, barY, barX + barW, barY + barH, barH / 2, barH / 2, fill)
        fill.color = Color.rgb(255, 185, 45)
        val fillW = (barW * (energy / 100.0)).toFloat().coerceIn(0f, barW)
        c.drawRoundRect(barX, barY, barX + fillW, barY + barH, barH / 2, barH / 2, fill)

        // 2. Tabela de Classificação de 1º a 10º Jogadores (Live Leaderboard Minimalista)
        val leadW = w * 0.21f
        val leadRowH = u * 0.25f
        val leadH = leadRowH * 11 + 6f
        val leadX = w - leadW - 16f
        val leadY = 16f

        fill.color = 0xAA141228.toInt()
        c.drawRoundRect(leadX, leadY, leadX + leadW, leadY + leadH, 12f, 12f, fill)

        text.textSize = u * 0.18f
        text.textAlign = Paint.Align.LEFT
        text.color = Color.rgb(255, 215, 65)
        c.drawText("🏁 1º - 10º", leadX + 10f, leadY + leadRowH * 0.70f, text)

        for (i in 0 until min(10, standings.size)) {
            val entry = standings[i]
            val ry = leadY + leadRowH * 0.85f + i * leadRowH
            val rankPrefix = "${i + 1}º"
            val rowCol = if (entry.isMe) Color.rgb(255, 235, 65) else if (i == 0) Color.rgb(255, 215, 55) else Color.WHITE

            text.textSize = u * 0.17f
            text.textAlign = Paint.Align.LEFT
            text.color = rowCol
            c.drawText(rankPrefix, leadX + 8f, ry + leadRowH * 0.65f, text)

            val dName = if (entry.name.length > 13) entry.name.take(12) + "…" else entry.name
            c.drawText(dName, leadX + 34f, ry + leadRowH * 0.65f, text)
        }

        // Áreas de toque limpas e translúcidas SEM TEXTO DE COMANDOS
        // (o comando PS4 opera nativamente pelo joystick e botões)
        val btnH = h * 0.18f
        val btnW = w * 0.13f

        steerLeftBtn.set(24f, h - btnH - 18f, 24f + btnW, h - 18f)
        steerRightBtn.set(steerLeftBtn.right + 12f, steerLeftBtn.top, steerLeftBtn.right + 12f + btnW, steerLeftBtn.bottom)

        fill.color = if (leftDown) 0x66FF69A5 else 0x22FFFFFF
        c.drawRoundRect(steerLeftBtn, btnH * 0.28f, btnH * 0.28f, fill)

        fill.color = if (rightDown) 0x66FF69A5 else 0x22FFFFFF
        c.drawRoundRect(steerRightBtn, btnH * 0.28f, btnH * 0.28f, fill)

        // Botão de Turbo sem texto no canto direito
        val turbW = w * 0.15f
        turboBtn.set(w - turbW - 24f, h - btnH - 18f, w - 24f, h - 18f)
        fill.color = if (boostDown || ps4Boost) 0xAAFFC323.toInt() else 0x33EB2D5F.toInt()
        c.drawRoundRect(turboBtn, btnH * 0.32f, btnH * 0.32f, fill)

        // Ecrã de Contagem Decrescente
        if (state == State.COUNTDOWN) {
            val countSec = (timer).toInt() + 1
            val countTxt = if (countSec > 3) "..." else "$countSec"
            label(c, countTxt, w / 2, h * 0.48f, u * 1.5f, Color.rgb(255, 225, 60), Paint.Align.CENTER)
        }

        // Ecrã de Fim de Corrida
        if (state == State.FINISH) {
            val boxW = w * 0.60f
            val boxH = h * 0.55f
            val box = RectF(w / 2 - boxW / 2, h / 2 - boxH / 2, w / 2 + boxW / 2, h / 2 + boxH / 2)
            fill.color = Color.rgb(26, 36, 68)
            c.drawRoundRect(box, 24f, 24f, fill)

            val winTxt = if (playerPlace == 1) "🏆 1º LUGAR!" else "🏁 ${playerPlace}º LUGAR"
            label(c, winTxt, box.centerX(), box.top + boxH * 0.28f, u * 0.55f, Color.rgb(255, 215, 55), Paint.Align.CENTER)

            restartBtn.set(box.centerX() - boxW * 0.30f, box.bottom - boxH * 0.30f, box.centerX() + boxW * 0.30f, box.bottom - boxH * 0.10f)
            fill.color = Theme.primary
            c.drawRoundRect(restartBtn, restartBtn.height() * 0.35f, restartBtn.height() * 0.35f, fill)
            label(c, "🔄", restartBtn.centerX(), restartBtn.centerY() + restartBtn.height() * 0.18f, restartBtn.height() * 0.45f, Color.WHITE, Paint.Align.CENTER)
        }
    }

    private fun label(c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int = Color.WHITE, align: Paint.Align = Paint.Align.LEFT) {
        text.textSize = size
        text.textAlign = align
        text.color = 0xAA000000.toInt()
        c.drawText(s, x + size * 0.05f, y + size * 0.05f, text)
        text.color = color
        c.drawText(s, x, y, text)
    }

    // ---------------------------------------------------------------- TOQUES & GAMEPAD
    override fun down(x: Float, y: Float, r: RectF) {
        if (backBtn.contains(x, y)) {
            (g.context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            g.go(Nav.MULTIPLAYER)
            return
        }

        if (state == State.FINISH && restartBtn.contains(x, y)) {
            startRace()
            return
        }

        if (steerLeftBtn.contains(x, y)) {
            leftDown = true; return
        }
        if (steerRightBtn.contains(x, y)) {
            rightDown = true; return
        }
        if (turboBtn.contains(x, y)) {
            boostDown = true; return
        }

        // Toques gerais
        if (x < r.width() * 0.40f) leftDown = true
        else if (x > r.width() * 0.60f) boostDown = true
    }

    override fun up(x: Float, y: Float, r: RectF) {
        leftDown = false
        rightDown = false
        boostDown = false
    }

    override fun onGamepadKey(event: KeyEvent): Boolean {
        val down = event.action == KeyEvent.ACTION_DOWN
        when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> { leftDown = down; return true }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { rightDown = down; return true }
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_R2 -> {
                ps4Boost = down; return true
            }
            KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_ESCAPE -> {
                (g.context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                g.go(Nav.MULTIPLAYER)
                return true
            }
        }
        return false
    }

    override fun onGamepadMotion(event: MotionEvent): Boolean {
        ps4SteerX = event.getAxisValue(MotionEvent.AXIS_X)
        val rTrig = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        ps4Boost = rTrig > 0.5f
        return true
    }
}
