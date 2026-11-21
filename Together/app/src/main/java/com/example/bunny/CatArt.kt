package com.example.bunny

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.max
import kotlin.math.sin

object CatArt {

    /** Paleta de pelo da gatinha (Estilo Mochi Figma 3D): tons pêssego-creme quentes, brancos e rosa suave. ZERO azul, vinho ou roxo! */
    fun catFurPalette(colorId: String): Triple<Int, Int, Int> {
        return when (colorId) {
            "cat_col_pink" -> Triple(Color.rgb(255, 218, 233), C.white, Color.rgb(255, 175, 200))
            "cat_col_cream", "cat_col_mochi" -> Triple(Color.rgb(248, 206, 172), Color.rgb(255, 248, 242), Color.rgb(255, 170, 166)) // Mochi Figma 3D Peach-Cream
            "cat_col_silver" -> Triple(Color.rgb(240, 244, 250), C.white, Color.rgb(255, 200, 218))
            else -> Triple(Color.rgb(248, 206, 172), Color.rgb(255, 248, 242), Color.rgb(255, 170, 166)) // Default Mochi Figma
        }
    }

    /**
     * Desenha a gatinha fofinha lindinha com guarda-roupa completo e personalização de cores.
     * @param cx centro X
     * @param baseY chão/base Y
     * @param s escala da gatinha
     * @param t tempo para animações (abana cauda, piscar, respiração)
     * @param mood 0 feliz/ronronar, 1 curiosa, 2 soninho, 3 dormir
     * @param pounce salto ao brincar (0f repouso, >0f no ar)
     */
    fun draw(
        c: Canvas, cx: Float, baseY: Float, s: Float, t: Float,
        mood: Int = 0, pounce: Float = 0f, lookX: Float = 0f,
        dirt: Float = 0f, foam: Float = 0f, mouth: Float = 0f,
        hat: String = Save.hat, outfit: String = Save.outfit,
        shoes: String = Save.shoes, color: String = Save.petColor,
        acc: String = Save.acc
    ) {
        val ol = Color.rgb(240, 175, 198) // Contorno rosa suave
        val lw = max(1.2f, s * 0.015f)
        val breathe = sin(t * 3f) * 0.03f
        val (furC, bellyC, earInnerC) = catFurPalette(color)

        c.save()
        c.translate(cx, baseY - pounce * s)
        c.scale(1f + breathe, 1f - breathe)

        // Capa de algodão rosa (atrás do corpinho)
        if (outfit == "cat_fit_cape") {
            D.poly(c, Color.rgb(255, 190, 215), ol, lw,
                -0.28f * s, -0.42f * s, 0.28f * s, -0.42f * s,
                0.35f * s, -0.08f * s, -0.35f * s, -0.08f * s)
            // Borda fofa de pom-pons brancos
            for (i in -4..4) {
                D.circle(c, i * 0.08f * s, -0.07f * s, 0.035f * s, C.white, ol, lw * 0.7f)
            }
        }

        // Cauda fofa a abanar suavemente
        val tailAngle = sin(t * 3.5f) * 20f
        c.save()
        c.translate(-0.24f * s, -0.15f * s)
        c.rotate(tailAngle)
        D.path.reset()
        D.path.moveTo(0f, 0f)
        D.path.quadTo(-0.25f * s, -0.15f * s, -0.18f * s, -0.42f * s)
        D.path.quadTo(-0.1f * s, -0.55f * s, 0f, -0.45f * s)
        D.path.quadTo(-0.06f * s, -0.2f * s, 0.06f * s, -0.05f * s)
        D.path.close()
        D.f(furC); c.drawPath(D.path, D.p)
        D.s(ol, lw); c.drawPath(D.path, D.p)
        // Pontinha da cauda rosa fofa
        D.circle(c, -0.1f * s, -0.46f * s, 0.06f * s, bellyC)
        c.restore()

        // Patinhas traseiras
        D.oval(c, -0.20f * s, -0.04f * s, 0.12f * s, 0.07f * s, furC, ol, lw)
        D.oval(c, 0.20f * s, -0.04f * s, 0.12f * s, 0.07f * s, furC, ol, lw)

        // Calçado nas patas traseiras
        if (shoes.isNotEmpty()) {
            drawCatShoe(c, -0.20f * s, -0.03f * s, s, shoes, ol, lw)
            drawCatShoe(c, 0.20f * s, -0.03f * s, s, shoes, ol, lw)
        }

        // Corpo redondinho e fofo
        D.oval(c, 0f, -0.26f * s, 0.28f * s, 0.25f * s, furC, ol, lw)
        // Barriguinha fofa
        D.oval(c, 0f, -0.22f * s, 0.18f * s, 0.17f * s, bellyC)

        // Roupa no corpo
        if (outfit.isNotEmpty()) {
            drawCatOutfit(c, s, outfit, ol, lw)
        }

        // Patinhas dianteiras
        D.oval(c, -0.09f * s, -0.03f * s, 0.065f * s, 0.08f * s, furC, ol, lw)
        D.oval(c, 0.09f * s, -0.03f * s, 0.065f * s, 0.08f * s, furC, ol, lw)

        // Calçado nas patas dianteiras
        if (shoes.isNotEmpty()) {
            drawCatShoe(c, -0.09f * s, -0.02f * s, s * 0.85f, shoes, ol, lw)
            drawCatShoe(c, 0.09f * s, -0.02f * s, s * 0.85f, shoes, ol, lw)
        }

        // Coleira / Acessório de pescoço
        if (acc == "cat_acc_pearls") {
            // Colar de pérolas brancas reluzentes
            for (i in -4..4) {
                val px = i * 0.042f * s
                val py = -0.44f * s + (i * i) * 0.003f * s
                D.circle(c, px, py, 0.030f * s, C.white, ol, lw * 0.7f)
                D.circle(c, px - 0.008f * s, py - 0.008f * s, 0.009f * s, Color.rgb(255, 230, 240))
            }
        } else if (acc == "cat_acc_bow") {
            // Lacinho rosa doce no peito
            D.oval(c, -0.05f * s, -0.44f * s, 0.045f * s, 0.03f * s, C.pinkD, ol, lw * 0.8f)
            D.oval(c, 0.05f * s, -0.44f * s, 0.045f * s, 0.03f * s, C.pinkD, ol, lw * 0.8f)
            D.circle(c, 0f, -0.44f * s, 0.022f * s, C.white, ol, lw * 0.8f)
        } else {
            // Coleira fofa rosa choque com guizo dourado/branco padrão
            D.oval(c, 0f, -0.44f * s, 0.22f * s, 0.05f * s, C.pinkD, ol, lw * 0.8f)
            D.circle(c, 0f, -0.41f * s, 0.045f * s, C.gold, ol, lw * 0.8f)
            D.circle(c, 0f, -0.41f * s, 0.015f * s, C.pinkD)
        }

        // Orelhas pontiagudas arredondadas de gatinha (Estilo 3D Figma)
        val earTwitch = sin(t * 1.5f) * 2f
        for (sg in -1..1 step 2) {
            c.save()
            c.translate(sg * 0.18f * s, -0.72f * s)
            c.rotate(sg * (12f + earTwitch))
            D.poly(c, furC, ol, lw, -0.14f * s, 0.05f * s, 0.14f * s, 0.05f * s, 0f, -0.28f * s)
            D.poly(c, earInnerC, 0, 0f, -0.08f * s, 0.03f * s, 0.08f * s, 0.03f * s, 0f, -0.20f * s)
            // Tufo de pelo fofo branco na base da orelha (Figma Mochi)
            D.circle(c, -0.04f * s, 0.02f * s, 0.035f * s, bellyC)
            D.circle(c, 0.04f * s, 0.02f * s, 0.030f * s, bellyC)
            c.restore()
        }

        // Cabeça redondinha e fofa com bochechas volumosas
        D.circle(c, 0f, -0.62f * s, 0.32f * s, furC, ol, lw)
        // Brilho suave 3D de vinil na testa
        D.oval(c, -0.04f * s, -0.80f * s, 0.12f * s, 0.05f * s, D.alpha(C.white, 80))
        // Mancha fofa cremosa na orelha
        D.oval(c, -0.15f * s, -0.75f * s, 0.14f * s, 0.10f * s, bellyC)

        // Bochechinhas coradas rosa amoroso (Blush circular Figma)
        D.circle(c, -0.20f * s, -0.55f * s, 0.065f * s, D.alpha(Color.rgb(255, 140, 165), 160))
        D.circle(c, 0.20f * s, -0.55f * s, 0.065f * s, D.alpha(Color.rgb(255, 140, 165), 160))

        // Olhos da gatinha: Grandes, brilhantes, estilo vinil 3D Pop Mart com duplo reflexo
        val ey = -0.63f * s
        val blink = (t % 3.6f) < 0.12f
        for (sg in -1..1 step 2) {
            val ex = sg * 0.13f * s
            if (mood == 2 || mood == 3 || blink) {
                // Olhos fechados felizes / a ronronar (^.^)
                D.path.reset()
                D.path.moveTo(ex - 0.055f * s, ey)
                D.path.quadTo(ex, ey - 0.045f * s, ex + 0.055f * s, ey)
                D.s(Color.rgb(85, 45, 60), lw * 1.8f); c.drawPath(D.path, D.p)
            } else {
                // Olhos esféricos chocolate profundo
                val lx = lookX * 0.015f * s
                val eyeDark = Color.rgb(38, 24, 32)
                D.oval(c, ex + lx, ey, 0.068f * s, 0.090f * s, eyeDark)
                // Íris sutil quente
                D.oval(c, ex + lx, ey + 0.024f * s, 0.050f * s, 0.040f * s, Color.rgb(78, 45, 58))
                // Brilho principal superior esquerdo (Specular highlight)
                D.circle(c, ex + lx - 0.022f * s, ey - 0.028f * s, 0.028f * s, C.white)
                // Brilho secundário inferior direito
                D.circle(c, ex + lx + 0.022f * s, ey + 0.028f * s, 0.014f * s, C.white)
            }
        }

        // Óculos rosa fofos (Acessório)
        if (acc == "cat_acc_glasses") {
            val gCol = Color.rgb(255, 105, 180)
            D.circle(c, -0.13f * s, ey, 0.09f * s, 0, gCol, lw * 1.5f)
            D.circle(c, 0.13f * s, ey, 0.09f * s, 0, gCol, lw * 1.5f)
            D.line(c, -0.04f * s, ey, 0.04f * s, ey, lw * 1.5f, gCol)
        }

        // Narizinho botão rosa doce (Figma Mochi)
        D.circle(c, 0f, -0.535f * s, 0.022f * s, Color.rgb(255, 150, 175))

        // Boquinha de gatinha :3 (ou aberta ao comer)
        val my = -0.510f * s
        if (mouth > 0.05f) {
            val oh = (0.02f + 0.04f * mouth) * s
            D.oval(c, 0f, my + oh * 0.4f, 0.045f * s, oh, Color.rgb(255, 140, 165), ol, lw)
            D.circle(c, 0f, my + 0.01f * s, 0.018f * s, C.white)
        } else {
            D.path.reset()
            D.path.moveTo(-0.055f * s, my - 0.008f * s)
            D.path.quadTo(-0.026f * s, my + 0.022f * s, 0f, my)
            D.path.quadTo(0.026f * s, my + 0.022f * s, 0.055f * s, my - 0.008f * s)
            D.s(Color.rgb(130, 65, 80), lw * 1.3f); c.drawPath(D.path, D.p)
        }

        // Manchas de sujidade suaves
        if (dirt > 0.05f) {
            val dc = D.alpha(Color.rgb(205, 165, 150), (dirt * 180).toInt())
            D.circle(c, -0.15f * s, -0.40f * s, 0.05f * s, dc)
            D.circle(c, 0.18f * s, -0.32f * s, 0.045f * s, dc)
            D.circle(c, 0.05f * s, -0.68f * s, 0.04f * s, dc)
        }

        // Bolhas de sabão brancas e rosas
        if (foam > 0.05f) {
            for (i in 0 until (foam * 10).toInt()) {
                val fx = ((i * 37) % 7 - 3) * 0.08f * s
                val fy = -0.3f * s - (i % 5) * 0.12f * s
                D.circle(c, fx, fy, 0.07f * s, D.alpha(C.white, 230), D.alpha(C.pink, 255), lw)
            }
        }

        // Bigodes fofos
        for (sg in -1..1 step 2) {
            D.line(c, sg * 0.16f * s, -0.53f * s, sg * 0.36f * s, -0.56f * s, lw * 0.8f, D.alpha(ol, 190))
            D.line(c, sg * 0.16f * s, -0.50f * s, sg * 0.36f * s, -0.48f * s, lw * 0.8f, D.alpha(ol, 190))
        }

        // Chapéus da gatinha
        if (hat.isNotEmpty()) {
            drawCatHat(c, s, hat, ol, lw)
        }

        c.restore()
    }

    /** Desenha os chapéus / tiaras / laços da gatinha. */
    private fun drawCatHat(c: Canvas, s: Float, hatId: String, ol: Int, lw: Float) {
        when (hatId) {
            "cat_hat_bow" -> {
                // Grande laço de cetim rosa na cabeça
                val bx = 0.10f * s; val by = -0.88f * s
                D.oval(c, bx - 0.07f * s, by - 0.03f * s, 0.07f * s, 0.045f * s, C.pinkD, ol, lw)
                D.oval(c, bx + 0.07f * s, by - 0.03f * s, 0.07f * s, 0.045f * s, C.pinkD, ol, lw)
                D.circle(c, bx, by - 0.03f * s, 0.032f * s, C.white, ol, lw)
                D.circle(c, bx, by - 0.03f * s, 0.016f * s, C.pinkD)
            }
            "cat_hat_flower" -> {
                // Tiara de flores brancas e rosa
                val fy = -0.86f * s
                for (i in -2..2) {
                    val fx = i * 0.09f * s
                    val fCol = if (i % 2 == 0) C.white else Color.rgb(255, 185, 210)
                    for (k in 0 until 5) {
                        val a = k * 72 * Math.PI / 180.0
                        D.circle(c, fx + (Math.cos(a) * 0.025f * s).toFloat(), fy + (Math.sin(a) * 0.025f * s).toFloat(), 0.016f * s, fCol)
                    }
                    D.circle(c, fx, fy, 0.014f * s, C.butter, ol, lw * 0.6f)
                }
            }
            "cat_hat_crown" -> {
                // Coroa de Princesa Dourada com joia rosa
                val cy = -0.92f * s
                D.poly(c, C.incaGold, ol, lw,
                    -0.12f * s, cy + 0.04f * s,
                    -0.12f * s, cy - 0.06f * s,
                    -0.06f * s, cy - 0.02f * s,
                    0f, cy - 0.10f * s,
                    0.06f * s, cy - 0.02f * s,
                    0.12f * s, cy - 0.06f * s,
                    0.12f * s, cy + 0.04f * s)
                // Joia rosa no topo central
                D.circle(c, 0f, cy - 0.10f * s, 0.022f * s, C.pinkD, C.white, lw * 0.8f)
                D.circle(c, -0.12f * s, cy - 0.06f * s, 0.015f * s, C.white)
                D.circle(c, 0.12f * s, cy - 0.06f * s, 0.015f * s, C.white)
            }
            "cat_hat_beret" -> {
                // Boina branca de lã fofa
                D.oval(c, -0.04f * s, -0.88f * s, 0.22f * s, 0.08f * s, C.white, ol, lw)
                D.circle(c, -0.04f * s, -0.96f * s, 0.025f * s, C.pinkD)
            }
            "cat_hat_party" -> {
                // Chapéu de festa rosa com pom-pom branco
                val cy = -0.85f * s
                D.poly(c, Color.rgb(255, 140, 180), ol, lw,
                    -0.10f * s, cy, 0.10f * s, cy, 0f, cy - 0.26f * s)
                // Bolinhas brancas
                D.circle(c, -0.03f * s, cy - 0.08f * s, 0.018f * s, C.white)
                D.circle(c, 0.03f * s, cy - 0.16f * s, 0.016f * s, C.white)
                // Pom-pom branco no topo
                D.circle(c, 0f, cy - 0.26f * s, 0.035f * s, C.white, ol, lw * 0.8f)
            }
        }
    }

    /** Desenha as roupinhas da gatinha. */
    private fun drawCatOutfit(c: Canvas, s: Float, fitId: String, ol: Int, lw: Float) {
        when (fitId) {
            "cat_fit_dress" -> {
                // Vestidinho rosa de princesa com folhos e laço
                val topY = -0.40f * s
                val botY = -0.10f * s
                D.poly(c, Color.rgb(255, 185, 215), ol, lw,
                    -0.16f * s, topY, 0.16f * s, topY,
                    0.24f * s, botY, -0.24f * s, botY)
                // Folho branco na barra
                for (i in -3..3) {
                    D.circle(c, i * 0.07f * s, botY + 0.01f * s, 0.035f * s, C.white, ol, lw * 0.7f)
                }
                // Lacinho branco no peito
                D.oval(c, 0f, topY + 0.04f * s, 0.05f * s, 0.025f * s, C.white, ol, lw * 0.7f)
            }
            "cat_fit_sweater" -> {
                // Camisola nuvem branca macia
                D.oval(c, 0f, -0.26f * s, 0.27f * s, 0.22f * s, C.white, ol, lw)
                // Riscas rosa pastel suaves
                D.line(c, -0.18f * s, -0.28f * s, 0.18f * s, -0.28f * s, lw * 1.5f, Color.rgb(255, 205, 225))
                D.line(c, -0.19f * s, -0.20f * s, 0.19f * s, -0.20f * s, lw * 1.5f, Color.rgb(255, 205, 225))
            }
            "cat_fit_scarf" -> {
                // Cachecol rosa e branco quente
                D.rrect(c, -0.20f * s, -0.46f * s, 0.20f * s, -0.38f * s, 0.04f * s, Color.rgb(255, 140, 180), ol, lw)
                // Ponta a cair
                D.rrect(c, 0.06f * s, -0.42f * s, 0.16f * s, -0.18f * s, 0.03f * s, Color.rgb(255, 140, 180), ol, lw)
                D.line(c, 0.06f * s, -0.32f * s, 0.16f * s, -0.32f * s, lw * 1.6f, C.white)
                D.line(c, 0.06f * s, -0.24f * s, 0.16f * s, -0.24f * s, lw * 1.6f, C.white)
            }
            "cat_fit_overall" -> {
                // Jardineiras rosa fofas
                D.rrect(c, -0.16f * s, -0.36f * s, 0.16f * s, -0.06f * s, 0.06f * s, Color.rgb(255, 170, 205), ol, lw)
                // Alças
                D.line(c, -0.11f * s, -0.42f * s, -0.11f * s, -0.36f * s, lw * 2.2f, Color.rgb(255, 140, 180))
                D.line(c, 0.11f * s, -0.42f * s, 0.11f * s, -0.36f * s, lw * 2.2f, Color.rgb(255, 140, 180))
                // Botões brancos
                D.circle(c, -0.11f * s, -0.34f * s, 0.018f * s, C.white)
                D.circle(c, 0.11f * s, -0.34f * s, 0.018f * s, C.white)
            }
        }
    }

    /** Desenha os sapatinhos fofos nas patinhas da gatinha. */
    private fun drawCatShoe(c: Canvas, px: Float, py: Float, s: Float, shoeId: String, ol: Int, lw: Float) {
        val sw = 0.09f * s; val sh = 0.055f * s
        when (shoeId) {
            "cat_shoe_pink" -> {
                // Sabrinas rosa com lacinho
                D.oval(c, px, py, sw, sh, Color.rgb(255, 140, 185), ol, lw)
                D.circle(c, px, py - sh * 0.3f, 0.018f * s, C.white)
            }
            "cat_shoe_boots" -> {
                // Botinhas de lã branca
                D.oval(c, px, py, sw, sh * 1.2f, C.white, ol, lw)
                D.oval(c, px, py - sh * 0.5f, sw * 0.9f, sh * 0.45f, Color.rgb(255, 220, 235), ol, lw * 0.7f)
            }
            "cat_shoe_slippers" -> {
                // Pantufas nuvem fofas
                D.oval(c, px, py, sw * 1.15f, sh * 1.1f, Color.rgb(255, 235, 245), ol, lw)
                D.circle(c, px - sw * 0.35f, py, sh * 0.5f, C.white)
                D.circle(c, px + sw * 0.35f, py, sh * 0.5f, C.white)
            }
            "cat_shoe_gold" -> {
                // Sabrinas douradas de princesa
                D.oval(c, px, py, sw, sh, C.gold, ol, lw)
                D.circle(c, px, py - sh * 0.2f, 0.015f * s, C.white)
            }
        }
    }
}
