package com.example.bunny

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

class Item(val id: String, val name: String, val cat: Int, val price: Int)

object Items {
    const val HAT = 0; const val OUTFIT = 1; const val ACC = 2; const val WALL = 3; const val FLOOR = 4; const val DECOR = 5
    val tabNames = listOf("Chapéus", "Roupa", "Acess.", "Paredes", "Chão", "Objetos")
    val all = listOf(
        Item("hat_bow", "Laço", HAT, 40), Item("hat_flower", "Flores", HAT, 60), Item("hat_straw", "Palha", HAT, 70),
        Item("hat_party", "Festa", HAT, 50), Item("hat_beanie", "Gorro", HAT, 80), Item("hat_crown", "Coroa", HAT, 150),
        Item("fit_scarf", "Cachecol", OUTFIT, 50), Item("fit_overall", "Jardineiras", OUTFIT, 90),
        Item("fit_sweater", "Camisola", OUTFIT, 100), Item("fit_cape", "Capa", OUTFIT, 130),
        Item("acc_glasses", "Óculos", ACC, 60), Item("acc_bow", "Laço pescoço", ACC, 50), Item("acc_pearls", "Colar", ACC, 80),
        Item("wall_pink", "Rosa", WALL, 0), Item("wall_mint", "Menta", WALL, 60), Item("wall_sky", "Céu", WALL, 60),
        Item("wall_lav", "Lavanda", WALL, 80), Item("wall_stripes", "Riscas", WALL, 120), Item("wall_stars", "Estrelas", WALL, 150),
        Item("floor_wood", "Madeira", FLOOR, 0), Item("floor_check", "Xadrez", FLOOR, 80),
        Item("floor_grass", "Relva", FLOOR, 90), Item("floor_cloud", "Nuvens", FLOOR, 140),
        Item("dec_plant", "Planta", DECOR, 50), Item("dec_rug", "Tapete", DECOR, 70), Item("dec_lamp", "Candeeiro", DECOR, 80),
        Item("dec_frame", "Quadro", DECOR, 60), Item("dec_plush", "Peluche", DECOR, 90), Item("dec_window", "Janela", DECOR, 110),
        Item("dec_shelf", "Estante", DECOR, 130)
    )

    fun byCat(cat: Int) = all.filter { it.cat == cat }
    fun get(id: String) = all.first { it.id == id }
}

object Save {
    private lateinit var prefs: SharedPreferences
    var coins = 80
    var hunger = 85f
    var hygiene = 85f
    var energy = 85f
    var joy = 85f
    var hat = ""
    var outfit = ""
    var acc = ""
    var sleeping = false
    val owned = HashSet<String>()
    val wall = arrayOf("wall_pink", "wall_pink", "wall_pink")
    val floor = arrayOf("floor_wood", "floor_wood", "floor_wood")
    val decor = HashSet<String>() // "id@sala"

    private var autosave = 0f

    fun load(ctx: Context) {
        prefs = ctx.getSharedPreferences("bunny", Context.MODE_PRIVATE)
        coins = prefs.getInt("coins", 80)
        hunger = prefs.getFloat("hunger", 85f)
        hygiene = prefs.getFloat("hygiene", 85f)
        energy = prefs.getFloat("energy", 85f)
        joy = prefs.getFloat("joy", 85f)
        hat = prefs.getString("hat", "") ?: ""
        outfit = prefs.getString("outfit", "") ?: ""
        acc = prefs.getString("acc", "") ?: ""
        owned.clear(); owned.addAll(prefs.getStringSet("owned", emptySet()) ?: emptySet())
        owned.add("wall_pink"); owned.add("floor_wood")
        decor.clear(); decor.addAll(prefs.getStringSet("decor", emptySet()) ?: emptySet())
        for (i in 0 until 3) {
            wall[i] = prefs.getString("wall$i", "wall_pink") ?: "wall_pink"
            floor[i] = prefs.getString("floor$i", "floor_wood") ?: "floor_wood"
        }
        // o tempo passa também com a app fechada (mais devagar)
        val last = prefs.getLong("last", System.currentTimeMillis())
        val el = ((System.currentTimeMillis() - last) / 1000f).coerceIn(0f, 28800f) * 0.4f
        hunger = (hunger - 0.020f * el).coerceIn(5f, 100f)
        hygiene = (hygiene - 0.013f * el).coerceIn(5f, 100f)
        joy = (joy - 0.017f * el).coerceIn(5f, 100f)
        energy = (energy - 0.010f * el).coerceIn(5f, 100f)
    }

    fun save() {
        prefs.edit()
            .putInt("coins", coins).putFloat("hunger", hunger).putFloat("hygiene", hygiene)
            .putFloat("energy", energy).putFloat("joy", joy)
            .putString("hat", hat).putString("outfit", outfit).putString("acc", acc)
            .putStringSet("owned", HashSet(owned)).putStringSet("decor", HashSet(decor))
            .putString("wall0", wall[0]).putString("wall1", wall[1]).putString("wall2", wall[2])
            .putString("floor0", floor[0]).putString("floor1", floor[1]).putString("floor2", floor[2])
            .putLong("last", System.currentTimeMillis())
            .apply()
    }

    fun tick(dt: Float) {
        hunger = (hunger - 0.020f * dt).coerceIn(0f, 100f)
        hygiene = (hygiene - 0.013f * dt).coerceIn(0f, 100f)
        joy = (joy - 0.017f * dt).coerceIn(0f, 100f)
        energy = (energy + (if (sleeping) 0.15f else -0.010f) * dt).coerceIn(0f, 100f)
        autosave += dt
        if (autosave > 10f) { autosave = 0f; save() }
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
