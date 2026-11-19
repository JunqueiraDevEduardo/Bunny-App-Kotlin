package com.example.bunny

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

class Item(val id: String, val name: String, val cat: Int, val price: Int, val desc: String = "")

object Items {
    const val FOOD = 0; const val HAT = 1; const val OUTFIT = 2; const val SHOES = 3; const val COLOR = 4
    const val ACC = 5; const val WALL = 6; const val FLOOR = 7; const val DECOR = 8

    val tabNames = listOf("Comida", "Chapéus", "Roupa", "Calçado", "Cores", "Acess.", "Paredes", "Chão", "Objetos")

    val all = listOf(
        // Comida e Delícias Peruanas
        Item("food_ceviche", "Ceviche Peruano", FOOD, 35, "Peixe marinado fresco com lima e milho choclo"),
        Item("food_lomo", "Lomo Saltado", FOOD, 40, "Carne salteada andina com batatas douradas"),
        Item("food_chicha", "Chicha Morada", FOOD, 20, "Bebida tradicional de milho roxo e canela"),
        Item("food_alfajor", "Alfajores Peruanos", FOOD, 25, "Biscoito com doce de leite e açúcar em pó"),
        Item("food_causa", "Causa Limeña", FOOD, 30, "Massa de batata amarela com abacate"),
        Item("food_choclo", "Choclo c/ Queijo", FOOD, 20, "Milho gigante dos Andes com queijo"),
        Item("food_carrot", "Cenoura Doce", FOOD, 0, "A favorita do coelho"),
        Item("food_apple", "Maçã Fresca", FOOD, 15, "Doce e crocante"),
        Item("food_berry", "Bagas Silvestres", FOOD, 18, "Frutos doces do bosque"),
        Item("food_cupcake", "Cupcake Festivo", FOOD, 28, "Bolo fofo com cobertura rosa"),
        Item("food_potion_energy", "Poção Energia", FOOD, 60, "Recupera 100% da energia imediatamente"),
        Item("food_potion_clean", "Poção Banho", FOOD, 50, "Deixa o Bunny limpo e cheiroso"),

        // Chapéus (inclui cultura peruana)
        Item("hat_chullo", "Chullo Andino", HAT, 85, "Gorro peruano tradicional de lã com borlas"),
        Item("hat_montera", "Montera Peruana", HAT, 90, "Chapéu tradicional de festa andina"),
        Item("hat_bow", "Laço", HAT, 40),
        Item("hat_flower", "Flores", HAT, 60),
        Item("hat_straw", "Palha", HAT, 70),
        Item("hat_party", "Festa", HAT, 50),
        Item("hat_beanie", "Gorro", HAT, 80),
        Item("hat_crown", "Coroa", HAT, 150),

        // Roupa (inclui cultura peruana)
        Item("fit_poncho", "Poncho Andino", OUTFIT, 110, "Poncho tradicional peruano colorido com franjas"),
        Item("fit_andina", "Camisola Andina", OUTFIT, 95, "Camisola com padrões geométricos incaicos"),
        Item("fit_scarf", "Cachecol", OUTFIT, 50),
        Item("fit_overall", "Jardineiras", OUTFIT, 90),
        Item("fit_sweater", "Camisola Riscas", OUTFIT, 100),
        Item("fit_cape", "Capa", OUTFIT, 130),

        // Sapatilhas / Calçado
        Item("shoe_andean", "Sapatilhas Andinas", SHOES, 85, "Ténis com tecido peruano artesanal colorido"),
        Item("shoe_red", "Sapatilhas Vermelhas", SHOES, 60, "Ténis vermelhos desportivos clássicos"),
        Item("shoe_blue", "Sapatilhas Azuis", SHOES, 60, "Ténis azuis com sola branca"),
        Item("shoe_boots", "Botas Altiplano", SHOES, 80, "Botas quentinhas de lã andina"),
        Item("shoe_gold", "Ténis Dourados", SHOES, 120, "Sapatilhas douradas brilhantes de campeão"),

        // Cores do Coelho
        Item("col_white", "Branco Neve", COLOR, 0, "O pelo original branquinho e fofo"),
        Item("col_caramel", "Caramelo Doce", COLOR, 70, "Pelo castanho claro macio"),
        Item("col_grey", "Prateado Suave", COLOR, 70, "Pelo cinzento aveludado"),
        Item("col_pink", "Rosa Algodão", COLOR, 90, "Pelo cor-de-rosa doce e amoroso"),
        Item("col_gold", "Sol Inca Dourado", COLOR, 120, "Dourado reluzente dos Andes"),
        Item("col_cocoa", "Cacau Chocolate", COLOR, 80, "Castanho chocolate reconfortante"),
        Item("col_sky", "Azul Celestial", COLOR, 85, "Azul pastel suave como uma nuvem"),

        // Acessórios
        Item("acc_glasses", "Óculos", ACC, 60),
        Item("acc_bow", "Laço pescoço", ACC, 50),
        Item("acc_pearls", "Colar", ACC, 80),

        // Paredes (inclui cultura peruana)
        Item("wall_pink", "Rosa Clássico", WALL, 0),
        Item("wall_inca", "Muro Inca", WALL, 90, "Pedra esculpida de Cusco com símbolos Incas"),
        Item("wall_aguayo", "Tecido Aguayo", WALL, 100, "Cores vibrantes tradicionais peruanas"),
        Item("wall_mint", "Menta", WALL, 60),
        Item("wall_sky", "Céu", WALL, 60),
        Item("wall_lav", "Lavanda", WALL, 80),
        Item("wall_stripes", "Riscas", WALL, 120),
        Item("wall_stars", "Estrelas", WALL, 150),

        // Chão (inclui cultura peruana)
        Item("floor_wood", "Madeira", FLOOR, 0),
        Item("floor_inca", "Calçada Cusco", FLOOR, 85, "Pedra talhada histórica dos Andes"),
        Item("floor_peru", "Madeira Andina", FLOOR, 75, "Soalho de madeira nobre com borda artesanal"),
        Item("floor_check", "Xadrez", FLOOR, 80),
        Item("floor_grass", "Relva", FLOOR, 90),
        Item("floor_cloud", "Nuvens", FLOOR, 140),

        // Objetos / Decoração (inclui cultura peruana)
        Item("dec_machupicchu", "Machu Picchu", DECOR, 85, "Quadro ilustrado da cidadela sagrada nos Andes"),
        Item("dec_tapete_peruano", "Tapete Andino", DECOR, 75, "Tapete geométrico peruano tecido à mão"),
        Item("dec_lhama", "Peluche Lhama", DECOR, 95, "Lhama fofa com borlas coloridas nas orelhas"),
        Item("dec_zampogna", "Flauta de Pã", DECOR, 70, "Flauta musical andina de bambu tradicional"),
        Item("dec_plant", "Planta", DECOR, 50),
        Item("dec_rug", "Tapete Rosa", DECOR, 70),
        Item("dec_lamp", "Candeeiro", DECOR, 80),
        Item("dec_frame", "Quadro Cenoura", DECOR, 60),
        Item("dec_plush", "Peluche Urso", DECOR, 90),
        Item("dec_window", "Janela", DECOR, 110),
        Item("dec_shelf", "Estante", DECOR, 130)
    )

    fun byCat(cat: Int) = all.filter { it.cat == cat }
    fun get(id: String) = all.firstOrNull { it.id == id } ?: all.first()
}

object Save {
    private lateinit var prefs: SharedPreferences
    var coins = 80
    var hunger = 85f
    var hygiene = 85f
    var energy = 85f
    var joy = 85f

    // Personalização
    var hat = ""
    var outfit = ""
    var shoes = ""
    var bunnyColor = "col_white"
    var acc = ""

    // Sono & Energia
    var sleeping = false
    var sleepStartTime = 0L

    // Progressão por Dias e Níveis (0 a 100)
    var birthTime = 0L
    var bonusLevels = 0

    // Companhia (Gatinha fofinha)
    var hasCat = true
    var catHappiness = 95f

    val owned = HashSet<String>()
    val wall = arrayOf("wall_pink", "wall_pink", "wall_pink")
    val floor = arrayOf("floor_wood", "floor_wood", "floor_wood")
    val decor = HashSet<String>() // "id@sala"

    private var autosave = 0f

    /** Nível de 0 a 100: sobe 1 nível ao fim de cada dia real (24h). */
    fun level(): Int {
        if (birthTime <= 0L) birthTime = System.currentTimeMillis()
        val days = ((System.currentTimeMillis() - birthTime) / 86400000L).toInt()
        return (days + bonusLevels).coerceIn(0, 100)
    }

    /** Progresso do dia atual (0.0f a 1.0f) para a barra/anel de nível. */
    fun dayProgress(): Float {
        if (birthTime <= 0L) return 0f
        val ms = (System.currentTimeMillis() - birthTime) % 86400000L
        return (ms / 86400000f).coerceIn(0f, 1f)
    }

    /** Horas que faltam para o próximo nível. */
    fun nextLevelHours(): Int {
        if (birthTime <= 0L) return 24
        val ms = (System.currentTimeMillis() - birthTime) % 86400000L
        val hours = 24 - (ms / 3600000L).toInt()
        return hours.coerceIn(1, 24)
    }

    /** Escala gradual do coelho: bebé pequeno (0.65) até adulto pleno (1.0). */
    fun bunnyScale(): Float = 0.65f + 0.35f * (level() / 100f)

    /** Fase de vida do coelho. */
    fun stage(): String = when (level()) {
        in 0..9 -> "Bebé"
        in 10..29 -> "Criança"
        in 30..59 -> "Jovem"
        else -> "Adulto"
    }

    fun load(ctx: Context) {
        prefs = ctx.getSharedPreferences("bunny", Context.MODE_PRIVATE)
        coins = prefs.getInt("coins", 80)
        hunger = prefs.getFloat("hunger", 85f)
        hygiene = prefs.getFloat("hygiene", 85f)
        energy = prefs.getFloat("energy", 85f)
        joy = prefs.getFloat("joy", 85f)

        hat = prefs.getString("hat", "") ?: ""
        outfit = prefs.getString("outfit", "") ?: ""
        shoes = prefs.getString("shoes", "") ?: ""
        bunnyColor = prefs.getString("bunnyColor", "col_white") ?: "col_white"
        acc = prefs.getString("acc", "") ?: ""

        sleeping = prefs.getBoolean("sleeping", false)
        sleepStartTime = prefs.getLong("sleepStartTime", 0L)

        birthTime = prefs.getLong("birthTime", 0L)
        if (birthTime <= 0L) birthTime = System.currentTimeMillis()
        bonusLevels = prefs.getInt("bonusLevels", 0)

        hasCat = prefs.getBoolean("hasCat", true)
        catHappiness = prefs.getFloat("catHappiness", 95f)

        owned.clear(); owned.addAll(prefs.getStringSet("owned", emptySet()) ?: emptySet())
        owned.add("wall_pink"); owned.add("floor_wood"); owned.add("col_white"); owned.add("food_carrot")
        decor.clear(); decor.addAll(prefs.getStringSet("decor", emptySet()) ?: emptySet())

        for (i in 0 until 3) {
            wall[i] = prefs.getString("wall$i", "wall_pink") ?: "wall_pink"
            floor[i] = prefs.getString("floor$i", "floor_wood") ?: "floor_wood"
        }

        // Tempo decorrido com a app fechada
        val last = prefs.getLong("last", System.currentTimeMillis())
        val elapsedSec = ((System.currentTimeMillis() - last) / 1000f).coerceIn(0f, 86400f)

        if (sleeping) {
            // Dorme em 2 minutos (120s = 100% energia -> 100/120 = 0.833f/s)
            val sleepGain = elapsedSec * (100f / 120f)
            energy = (energy + sleepGain).coerceIn(0f, 100f)
            hunger = (hunger - 0.006f * elapsedSec).coerceIn(5f, 100f)
            hygiene = (hygiene - 0.004f * elapsedSec).coerceIn(5f, 100f)
        } else {
            val el = elapsedSec * 0.4f
            hunger = (hunger - 0.020f * el).coerceIn(5f, 100f)
            hygiene = (hygiene - 0.013f * el).coerceIn(5f, 100f)
            joy = (joy - 0.017f * el).coerceIn(5f, 100f)
            energy = (energy - 0.010f * el).coerceIn(5f, 100f)
        }
    }

    fun save() {
        if (!this::prefs.isInitialized) return
        prefs.edit()
            .putInt("coins", coins).putFloat("hunger", hunger).putFloat("hygiene", hygiene)
            .putFloat("energy", energy).putFloat("joy", joy)
            .putString("hat", hat).putString("outfit", outfit).putString("shoes", shoes)
            .putString("bunnyColor", bunnyColor).putString("acc", acc)
            .putBoolean("sleeping", sleeping).putLong("sleepStartTime", sleepStartTime)
            .putLong("birthTime", birthTime).putInt("bonusLevels", bonusLevels)
            .putBoolean("hasCat", hasCat).putFloat("catHappiness", catHappiness)
            .putStringSet("owned", HashSet(owned)).putStringSet("decor", HashSet(decor))
            .putString("wall0", wall[0]).putString("wall1", wall[1]).putString("wall2", wall[2])
            .putString("floor0", floor[0]).putString("floor1", floor[1]).putString("floor2", floor[2])
            .putLong("last", System.currentTimeMillis())
            .apply()
    }

    fun tick(dt: Float) {
        if (sleeping) {
            // 2 minutos de sono completo (100 pontos em 120 segundos)
            energy = (energy + (100f / 120f) * dt).coerceIn(0f, 100f)
            hunger = (hunger - 0.005f * dt).coerceIn(0f, 100f)
            hygiene = (hygiene - 0.003f * dt).coerceIn(0f, 100f)
        } else {
            hunger = (hunger - 0.020f * dt).coerceIn(0f, 100f)
            hygiene = (hygiene - 0.013f * dt).coerceIn(0f, 100f)
            joy = (joy - 0.017f * dt).coerceIn(0f, 100f)
            energy = (energy - 0.010f * dt).coerceIn(0f, 100f)
        }
        autosave += dt
        if (autosave > 8f) { autosave = 0f; save() }
    }

    fun mood(): Int {
        if (sleeping) return 3
        val m = minOf(minOf(hunger, hygiene), minOf(energy, joy))
        val avg = (hunger + hygiene + energy + joy) / 4f
        return if (m < 20f || avg < 40f) 2 else if (avg < 65f) 1 else 0
    }

    /** Devolve (índice, valor) da necessidade mais baixa: 0 fome, 1 higiene, 2 energia, 3 diversão. */
    fun worst(): Pair<Int, Float> {
        val v = floatArrayOf(hunger, hygiene, energy, joy)
        var k = 0
        for (i in 1 until 4) if (v[i] < v[k]) k = i
        return Pair(k, v[k])
    }

    fun nightFactor(): Float {
        val cal = Calendar.getInstance()
        val h = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
        return when {
            h >= 21f || h < 6f -> 1f
            h >= 19f -> (h - 19f) / 2f
            h < 8f -> (8f - h) / 2f
            else -> 0f
        }
    }

    fun has(id: String) = owned.contains(id)
}
