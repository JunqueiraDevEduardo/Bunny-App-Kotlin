package com.example.bunny

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

class Item(
    val id: String,
    val name: String,
    val cat: Int,
    val price: Int,
    val desc: String = "",
    val forChar: String = "both" // "bunny", "cat" ou "both"
)

/** Bloco de construção do CatBunnyCraft em Machu Picchu */
class CraftBlock(val x: Int, val y: Int, val z: Int, val type: Int)

object Items {
    const val FOOD = 0; const val HAT = 1; const val OUTFIT = 2; const val SHOES = 3; const val COLOR = 4
    const val ACC = 5; const val WALL = 6; const val FLOOR = 7; const val DECOR = 8

    val tabNames = listOf("Comida", "Chapéus", "Roupa", "Calçado", "Cores", "Acess.", "Paredes", "Chão", "Objetos")

    val all = listOf(
        // ==========================================
        // COMIDA & DELÍCIAS (Disponível para ambos)
        // ==========================================
        Item("food_ceviche", "Ceviche Peruano", FOOD, 35, "Peixe marinado fresco com lima e milho choclo", "both"),
        Item("food_lomo", "Lomo Saltado", FOOD, 40, "Carne salteada andina com batatas douradas", "both"),
        Item("food_chicha", "Chicha Morada", FOOD, 20, "Bebida tradicional de milho roxo e canela", "both"),
        Item("food_alfajor", "Alfajores Peruanos", FOOD, 25, "Biscoito com doce de leite e açúcar em pó", "both"),
        Item("food_causa", "Causa Limeña", FOOD, 30, "Massa de batata amarela com abacate", "both"),
        Item("food_choclo", "Choclo c/ Queijo", FOOD, 20, "Milho gigante dos Andes com queijo", "both"),
        Item("food_carrot", "Cenoura Doce", FOOD, 0, "A favorita doce e fresca", "both"),
        Item("food_apple", "Maçã Fresca", FOOD, 15, "Doce e crocante", "both"),
        Item("food_berry", "Bagas Silvestres", FOOD, 18, "Frutos doces do bosque", "both"),
        Item("food_cupcake", "Cupcake Festivo", FOOD, 28, "Bolo fofo de festa", "both"),
        Item("food_potion_energy", "Poção Energia", FOOD, 60, "Recupera 100% da energia imediatamente", "both"),
        Item("food_potion_clean", "Poção Banho", FOOD, 50, "Deixa o pet limpo e cheiroso", "both"),

        // ==========================================
        // CHAPÉUS: BUNNY (Azul/Vinho/Roxo/Peru)
        // ==========================================
        Item("hat_chullo", "Chullo Andino", HAT, 85, "Gorro peruano tradicional de lã com borlas", "bunny"),
        Item("hat_montera", "Montera Peruana", HAT, 90, "Chapéu tradicional de festa andina", "bunny"),
        Item("hat_beanie", "Gorro Azul Andino", HAT, 75, "Gorro quente azul meia-noite", "bunny"),
        Item("hat_cap", "Boné Roxo Imperial", HAT, 70, "Boné desportivo roxo", "bunny"),
        Item("hat_crown_inca", "Coroa de Ouro Inca", HAT, 150, "Coroa sagrada do imperador inca", "bunny"),
        Item("hat_straw", "Chapéu de Palha", HAT, 65, "Chapéu campestre tradicional", "bunny"),

        // CHAPÉUS: CAT (Rosa/Brancos)
        Item("cat_hat_bow", "Laço Rosa Princesa", HAT, 45, "Laço cor-de-rosa fofinho", "cat"),
        Item("cat_hat_flower", "Tiara de Flores", HAT, 65, "Flores brancas e rosa delicadas", "cat"),
        Item("cat_hat_crown", "Coroa de Princesa", HAT, 140, "Coroa dourada com joia rosa", "cat"),
        Item("cat_hat_beret", "Boina Branca de Lã", HAT, 75, "Boina elegante e quentinha", "cat"),
        Item("cat_hat_party", "Chapéu de Festa Rosa", HAT, 50, "Cone festivo com pom-pom", "cat"),

        // ==========================================
        // ROUPA: BUNNY (Azul/Vinho/Roxo/Peru)
        // ==========================================
        Item("fit_poncho", "Poncho Andino", OUTFIT, 110, "Poncho tradicional peruano colorido com franjas", "bunny"),
        Item("fit_andina", "Camisola Andina", OUTFIT, 95, "Camisola com padrões geométricos incas", "bunny"),
        Item("fit_overall", "Jardineiras Azuis", OUTFIT, 90, "Jardineiras azuis clássicas", "bunny"),
        Item("fit_sweater", "Camisola Vinho", OUTFIT, 85, "Camisola vinho quente e fofa", "bunny"),
        Item("fit_cape", "Capa Imperial Roxa", OUTFIT, 130, "Capa nobre com fecho dourado", "bunny"),
        Item("fit_scarf", "Cachecol Andino", OUTFIT, 50, "Cachecol quente azul e vinho", "bunny"),

        // ROUPA: CAT (Rosa/Brancos)
        Item("cat_fit_dress", "Vestidinho Rosa", OUTFIT, 110, "Vestidinho com rendas brancas de princesa", "cat"),
        Item("cat_fit_sweater", "Camisola Nuvem Branca", OUTFIT, 90, "Camisola macia e fofa", "cat"),
        Item("cat_fit_cape", "Capa de Algodão Rosa", OUTFIT, 120, "Capa doce com pom-pons", "cat"),
        Item("cat_fit_scarf", "Cachecol Rosa e Branco", OUTFIT, 55, "Cachecol às riscas macio", "cat"),
        Item("cat_fit_overall", "Jardineiras Rosa Bebé", OUTFIT, 85, "Jardineiras doces de gatinha", "cat"),

        // ==========================================
        // CALÇADO: BUNNY (Azul/Vinho/Andino)
        // ==========================================
        Item("shoe_andean", "Sapatilhas Andinas", SHOES, 85, "Ténis com tecido peruano artesanal colorido", "bunny"),
        Item("shoe_blue", "Sapatilhas Azuis", SHOES, 60, "Ténis desportivos azuis", "bunny"),
        Item("shoe_wine", "Sapatilhas Vinho", SHOES, 65, "Sapatilhas elegantes cor de vinho", "bunny"),
        Item("shoe_boots", "Botas Altiplano", SHOES, 80, "Botas quentinhas de couro andino", "bunny"),
        Item("shoe_gold", "Ténis Dourados Incas", SHOES, 120, "Sapatilhas douradas brilhantes de campeão", "bunny"),

        // CALÇADO: CAT (Rosa/Brancos)
        Item("cat_shoe_pink", "Sabrinas Rosa Bebé", SHOES, 60, "Sabrinas fofas com lacinho", "cat"),
        Item("cat_shoe_boots", "Botinhas de Lã Branca", SHOES, 75, "Botinhas fofas quentinhas", "cat"),
        Item("cat_shoe_slippers", "Pantufas Nuvem", SHOES, 70, "Pantufas brancas fofas como nuvem", "cat"),
        Item("cat_shoe_gold", "Sabrinas Douradas", SHOES, 110, "Sabrinas brilhantes de princesa", "cat"),

        // ==========================================
        // CORES: BUNNY (Azuis, Vinho, Roxo - ZERO Rosa/Branco)
        // ==========================================
        Item("col_sky", "Azul Celestial", COLOR, 0, "Pelo azul suave e celestial", "bunny"),
        Item("col_royal", "Azul Real", COLOR, 75, "Pelo azul profundo e nobre", "bunny"),
        Item("col_wine", "Vinho Andino", COLOR, 80, "Pelo vinho aveludado e acolhedor", "bunny"),
        Item("col_purple", "Roxo Imperial", COLOR, 85, "Pelo roxo majestoso", "bunny"),
        Item("col_caramel", "Caramelo Quente", COLOR, 70, "Pelo caramelo escuro andino", "bunny"),

        // CORES: CAT (Rosa, Brancos - ZERO Azul/Vinho/Roxo)
        Item("cat_col_white", "Branco Algodão Doce", COLOR, 0, "Pelo branco puro fofinho", "cat"),
        Item("cat_col_pink", "Rosa Pastel Macio", COLOR, 85, "Pelo cor-de-rosa doce e amoroso", "cat"),
        Item("cat_col_cream", "Creme Baunilha", COLOR, 70, "Pelo creme baunilha aveludado", "cat"),
        Item("cat_col_silver", "Prateado Suave", COLOR, 75, "Pelo prateado clarinho brilhante", "cat"),

        // ==========================================
        // ACESSÓRIOS: BUNNY (Azul/Vinho)
        // ==========================================
        Item("acc_glasses", "Óculos Azuis", ACC, 60, "Óculos intelectuais azuis", "bunny"),
        Item("acc_bow_wine", "Laço Vinho Nobre", ACC, 55, "Laço formal cor de vinho", "bunny"),
        Item("acc_chullo_scarf", "Lenço Andino", ACC, 65, "Lenço artesanal tradicional", "bunny"),

        // ACESSÓRIOS: CAT (Rosa/Brancos)
        Item("cat_acc_pearls", "Colar de Pérolas", ACC, 80, "Colar de pérolas brancas brilhantes", "cat"),
        Item("cat_acc_bow", "Lacinho Rosa no Peito", ACC, 50, "Laço de cetim rosa doce", "cat"),
        Item("cat_acc_glasses", "Óculos Rosa Fofos", ACC, 60, "Armação rosa com formato de coração", "cat"),

        // ==========================================
        // PAREDES: BUNNY (Inca, Meia-Noite, Roxo)
        // ==========================================
        Item("wall_inca", "Muro Inca de Cusco", WALL, 0, "Pedra esculpida de Cusco com símbolos Incas", "bunny"),
        Item("wall_navy", "Azul Meia-Noite", WALL, 70, "Azul escuro tranquilo e moderno", "bunny"),
        Item("wall_purple", "Roxo Imperial", WALL, 80, "Parede em tom roxo nobre", "bunny"),
        Item("wall_aguayo", "Tecido Aguayo", WALL, 100, "Cores vibrantes tradicionais peruanas", "bunny"),
        Item("wall_stars", "Céu Estrelado", WALL, 120, "Céu azul escuro com estrelas douradas", "bunny"),

        // PAREDES: CAT (Rosa, Nuvens, Corações)
        Item("cat_wall_pink", "Rosa Bebé Suave", WALL, 0, "Rosa acolhedor e doce", "cat"),
        Item("cat_wall_cloud", "Nuvens Brancas", WALL, 80, "Céu rosa claro com nuvens fofas", "cat"),
        Item("cat_wall_hearts", "Corações Amorosos", WALL, 90, "Parede cheia de corações cor-de-rosa", "cat"),
        Item("cat_wall_stars", "Estrelas Brilhantes", WALL, 100, "Estrelinhas brancas suaves", "cat"),
        Item("cat_wall_stripes", "Riscas Rosa & Brancas", WALL, 95, "Padrão às riscas elegante", "cat"),

        // ==========================================
        // CHÃO: BUNNY (Inca, Madeira Peruana)
        // ==========================================
        Item("floor_inca", "Calçada Inca", FLOOR, 0, "Pedra talhada histórica dos Andes", "bunny"),
        Item("floor_peru", "Madeira Andina", FLOOR, 75, "Soalho de madeira nobre com borda artesanal", "bunny"),
        Item("floor_wood", "Madeira Nobre", FLOOR, 60, "Soalho de madeira escura", "bunny"),
        Item("floor_grass", "Relva dos Andes", FLOOR, 80, "Verde natural fresco", "bunny"),

        // CHÃO: CAT (Nuvens, Rosa, Branco)
        Item("cat_floor_cloud", "Chão Nuvens Fofas", FLOOR, 0, "Chão suave como caminhar no céu", "cat"),
        Item("cat_floor_pink", "Soalho Rosa Pastel", FLOOR, 70, "Madeira em tom rosa claro", "cat"),
        Item("cat_floor_white", "Madeira Clara Suave", FLOOR, 65, "Soalho branco macio e acolhedor", "cat"),
        Item("cat_floor_check", "Xadrez Rosa & Branco", FLOOR, 75, "Mosaico xadrez fofinho", "cat"),

        // ==========================================
        // DECORAÇÃO: BUNNY (Peru & Andes)
        // ==========================================
        Item("dec_machupicchu", "Machu Picchu", DECOR, 85, "Quadro ilustrado da cidadela sagrada nos Andes", "bunny"),
        Item("dec_tapete_peruano", "Tapete Andino", DECOR, 75, "Tapete geométrico peruano tecido à mão", "bunny"),
        Item("dec_lhama", "Peluche Lhama", DECOR, 95, "Lhama fofa com borlas coloridas nas orelhas", "bunny"),
        Item("dec_zampogna", "Flauta de Pã", DECOR, 70, "Flauta musical andina de bambu tradicional", "bunny"),

        // DECORAÇÃO: CAT (Princesa, Ursinho, Corações)
        Item("cat_dec_plush", "Peluche Ursinho Rosa", DECOR, 85, "Ursinho cor-de-rosa super fofo", "cat"),
        Item("cat_dec_rug", "Tapete Coração", DECOR, 70, "Tapete fofo em forma de coração", "cat"),
        Item("cat_dec_lamp", "Candeeiro Nuvem", DECOR, 75, "Candeeiro quentinho em forma de nuvem", "cat"),
        Item("cat_dec_mirror", "Espelho Princesa", DECOR, 90, "Espelho oval com pérolas e lacinho", "cat")
    )

    /** Filtra itens pela categoria e estritamente pelo pet ativo (Bunny vs Cat). */
    fun byCat(cat: Int) = all.filter { it.cat == cat && (it.forChar == "both" || it.forChar == Save.activeChar) }
    fun get(id: String) = all.firstOrNull { it.id == id } ?: all.first()
}

object Save {
    private lateinit var prefs: SharedPreferences

    // Autenticação & Definições Globais
    var activeChar = "bunny" // "bunny" ou "cat"
    var loggedIn = false
    var language = "pt" // "pt" ou "es"
    var soundEnabled = true
    var remindersEnabled = true
    var roomCode = "150225"

    // Ficha / Relatório do Bunny
    var reportDoneBunny = false
    var bunnyName = "Meu Coelhinho"
    var bunnyLikes = "Dar abraços e mimos quentinhos"
    var bunnyDislikes = "Ficar com saudades"
    var bunnyFavColor = "Azul Celestial"
    var bunnyFavFood = "Ceviche Peruano"
    var bunnyMessage = "Amo-te daqui até à lua! ♥"

    // Ficha / Relatório da Cat (Gatinha)
    var reportDoneCat = false
    var catName = "Minha Gatinha"
    var catLikes = "Ronronar e receber carinhos fofos"
    var catDislikes = "Frio e banhos frios"
    var catFavColor = "Rosa Algodão Doce"
    var catFavFood = "Alfajores com Doce de Leite"
    var catMessage = "És o amor da minha vida! ♥"

    // Conexão a Dois / Multiplayer à distância
    var partnerConnected = false
    var partnerChar = "cat"
    var partnerName = "Minha Gatinha"
    var partnerLvl = 1
    var partnerLikes = "Estar juntos e rir"
    var partnerDislikes = "Distância"
    var partnerFavColor = "Rosa"
    var partnerFavFood = "Doces"
    var partnerMessage = "Saudades tuas! ♥"
    var partnerLastGift = ""
    var partnerLastGiftTime = 0L
    var partnerLastActive = 0L
    var pendingGiftReceived = ""

    // Companhia no Home
    var hasCat = true
    var catHappiness = 95f

    // =======================================================
    // ESTADO SEPARADO: BUNNY (Azul, Vinho, Roxo, Peru)
    // =======================================================
    var bunnyCoins = 100
    var bunnyHunger = 85f
    var bunnyHygiene = 85f
    var bunnyEnergy = 85f
    var bunnyJoy = 85f
    var bunnyHat = ""
    var bunnyOutfit = ""
    var bunnyShoes = ""
    var bunnyColor = "col_sky"
    var bunnyAcc = ""
    var bunnySleeping = false
    var bunnySleepStartTime = 0L
    var bunnyBirthTime = 0L
    var bunnyBonusLevels = 0
    val bunnyOwned = HashSet<String>()
    val bunnyWall = arrayOf("wall_inca", "wall_inca", "wall_inca")
    val bunnyFloor = arrayOf("floor_inca", "floor_inca", "floor_inca")
    val bunnyDecor = HashSet<String>()

    // =======================================================
    // ESTADO SEPARADO: CAT (Rosa, Brancos, Princesa)
    // =======================================================
    var catCoins = 100
    var catHunger = 85f
    var catHygiene = 85f
    var catEnergy = 85f
    var catJoy = 85f
    var catHat = ""
    var catOutfit = ""
    var catShoes = ""
    var catColor = "cat_col_white"
    var catAcc = ""
    var catSleeping = false
    var catSleepStartTime = 0L
    var catBirthTime = 0L
    var catBonusLevels = 0
    val catOwned = HashSet<String>()
    val catWall = arrayOf("cat_wall_pink", "cat_wall_pink", "cat_wall_pink")
    val catFloor = arrayOf("cat_floor_cloud", "cat_floor_cloud", "cat_floor_cloud")
    val catDecor = HashSet<String>()

    // =======================================================
    // PROPRIEDADES DINÂMICAS DO PET ATIVO
    // =======================================================
    var coins: Int
        get() = if (activeChar == "bunny") bunnyCoins else catCoins
        set(v) { if (activeChar == "bunny") bunnyCoins = v else catCoins = v }

    var hunger: Float
        get() = if (activeChar == "bunny") bunnyHunger else catHunger
        set(v) { if (activeChar == "bunny") bunnyHunger = v else catHunger = v }

    var hygiene: Float
        get() = if (activeChar == "bunny") bunnyHygiene else catHygiene
        set(v) { if (activeChar == "bunny") bunnyHygiene = v else catHygiene = v }

    var energy: Float
        get() = if (activeChar == "bunny") bunnyEnergy else catEnergy
        set(v) { if (activeChar == "bunny") bunnyEnergy = v else catEnergy = v }

    var joy: Float
        get() = if (activeChar == "bunny") bunnyJoy else catJoy
        set(v) { if (activeChar == "bunny") bunnyJoy = v else catJoy = v }

    var hat: String
        get() = if (activeChar == "bunny") bunnyHat else catHat
        set(v) { if (activeChar == "bunny") bunnyHat = v else catHat = v }

    var outfit: String
        get() = if (activeChar == "bunny") bunnyOutfit else catOutfit
        set(v) { if (activeChar == "bunny") bunnyOutfit = v else catOutfit = v }

    var shoes: String
        get() = if (activeChar == "bunny") bunnyShoes else catShoes
        set(v) { if (activeChar == "bunny") bunnyShoes = v else catShoes = v }

    var petColor: String
        get() = if (activeChar == "bunny") bunnyColor else catColor
        set(v) { if (activeChar == "bunny") bunnyColor = v else catColor = v }

    var acc: String
        get() = if (activeChar == "bunny") bunnyAcc else catAcc
        set(v) { if (activeChar == "bunny") bunnyAcc = v else catAcc = v }

    var sleeping: Boolean
        get() = if (activeChar == "bunny") bunnySleeping else catSleeping
        set(v) { if (activeChar == "bunny") bunnySleeping = v else catSleeping = v }

    var sleepStartTime: Long
        get() = if (activeChar == "bunny") bunnySleepStartTime else catSleepStartTime
        set(v) { if (activeChar == "bunny") bunnySleepStartTime = v else catSleepStartTime = v }

    var birthTime: Long
        get() = if (activeChar == "bunny") bunnyBirthTime else catBirthTime
        set(v) { if (activeChar == "bunny") bunnyBirthTime = v else catBirthTime = v }

    var bonusLevels: Int
        get() = if (activeChar == "bunny") bunnyBonusLevels else catBonusLevels
        set(v) { if (activeChar == "bunny") bunnyBonusLevels = v else catBonusLevels = v }

    val owned: HashSet<String>
        get() = if (activeChar == "bunny") bunnyOwned else catOwned

    val wall: Array<String>
        get() = if (activeChar == "bunny") bunnyWall else catWall

    val floor: Array<String>
        get() = if (activeChar == "bunny") bunnyFloor else catFloor

    val decor: HashSet<String>
        get() = if (activeChar == "bunny") bunnyDecor else catDecor

    // CatBunnyCraft - Mundo 3D dos Amigos em Machu Picchu
    val craftBlocks = ArrayList<CraftBlock>()

    fun craftBlocksToJson(): String {
        val arr = org.json.JSONArray()
        for (b in craftBlocks) {
            val o = JSONObject()
            o.put("x", b.x); o.put("y", b.y); o.put("z", b.z); o.put("t", b.type)
            arr.put(o)
        }
        return arr.toString()
    }

    fun loadCraftBlocksFromJson(str: String) {
        if (str.isEmpty()) return
        try {
            val arr = org.json.JSONArray(str)
            craftBlocks.clear()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                craftBlocks.add(CraftBlock(o.getInt("x"), o.getInt("y"), o.getInt("z"), o.getInt("t")))
            }
        } catch (e: Exception) {}
    }

    fun addCraftBlock(x: Int, y: Int, z: Int, type: Int) {
        craftBlocks.removeAll { it.x == x && it.y == y && it.z == z }
        craftBlocks.add(CraftBlock(x, y, z, type))
        save()
    }

    fun removeCraftBlock(x: Int, y: Int, z: Int): Boolean {
        val rem = craftBlocks.removeAll { it.x == x && it.y == y && it.z == z }
        if (rem) save()
        return rem
    }

    private var autosave = 0f

    /** Retorna se a ficha da personagem atual já foi preenchida. */
    fun isCurrentReportDone(): Boolean =
        if (activeChar == "bunny") reportDoneBunny else reportDoneCat

    /** Nome do pet ativo. */
    fun currentPetName(): String =
        if (activeChar == "bunny") bunnyName else catName

    /** Nível de 0 a 100: sobe 1 nível ao fim de cada dia real (24h). */
    fun level(): Int {
        val bTime = birthTime
        if (bTime <= 0L) {
            val now = System.currentTimeMillis()
            if (activeChar == "bunny") bunnyBirthTime = now else catBirthTime = now
            return bonusLevels.coerceIn(0, 100)
        }
        val days = ((System.currentTimeMillis() - bTime) / 86400000L).toInt()
        return (days + bonusLevels).coerceIn(0, 100)
    }

    /** Progresso do dia atual (0.0f a 1.0f) para a barra/anel de nível. */
    fun dayProgress(): Float {
        val bTime = birthTime
        if (bTime <= 0L) return 0f
        val ms = (System.currentTimeMillis() - bTime) % 86400000L
        return (ms / 86400000f).coerceIn(0f, 1f)
    }

    /** Horas que faltam para o próximo nível. */
    fun nextLevelHours(): Int {
        val bTime = birthTime
        if (bTime <= 0L) return 24
        val ms = (System.currentTimeMillis() - bTime) % 86400000L
        val hours = 24 - (ms / 3600000L).toInt()
        return hours.coerceIn(1, 24)
    }

    /** Escala gradual do pet ativo: bebé pequeno (0.65) até adulto pleno (1.0). */
    fun bunnyScale(): Float = 0.65f + 0.35f * (level() / 100f)
    fun catScale(): Float = 0.65f + 0.35f * (level() / 100f)

    /** Fase de vida do pet ativo. */
    fun stage(): String = when (level()) {
        in 0..9 -> "Bebé"
        in 10..29 -> "Criança"
        in 30..59 -> "Jovem"
        else -> "Adulto"
    }

    /** Gera o código compacto em Base64 para partilha rápida. */
    fun exportSyncCode(): String {
        return try {
            val json = JSONObject()
            json.put("char", activeChar)
            json.put("name", currentPetName())
            json.put("lvl", level())
            json.put("hunger", hunger)
            json.put("joy", joy)
            json.put("energy", energy)
            json.put("likes", if (activeChar == "bunny") bunnyLikes else catLikes)
            json.put("dislikes", if (activeChar == "bunny") bunnyDislikes else catDislikes)
            json.put("color", if (activeChar == "bunny") bunnyFavColor else catFavColor)
            json.put("food", if (activeChar == "bunny") bunnyFavFood else catFavFood)
            json.put("msg", if (activeChar == "bunny") bunnyMessage else catMessage)
            json.put("gift", partnerLastGift)
            json.put("craft", craftBlocksToJson())
            json.put("time", System.currentTimeMillis())
            val bytes = json.toString().toByteArray(Charsets.UTF_8)
            "BUNNY_LINK:" + Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    /** Importa o código do parceiro gerado noutro telemóvel. */
    fun importSyncCode(raw: String): Boolean {
        return try {
            val code = raw.trim()
            val b64 = if (code.startsWith("BUNNY_LINK:")) code.substring(11) else code
            val bytes = Base64.decode(b64, Base64.NO_WRAP)
            val json = JSONObject(String(bytes, Charsets.UTF_8))
            partnerChar = json.optString("char", if (activeChar == "bunny") "cat" else "bunny")
            partnerName = json.optString("name", "Amor")
            partnerLvl = json.optInt("lvl", 1)
            partnerLikes = json.optString("likes", "")
            partnerDislikes = json.optString("dislikes", "")
            partnerFavColor = json.optString("color", "")
            partnerFavFood = json.optString("food", "")
            partnerMessage = json.optString("msg", "")
            val g = json.optString("gift", "")
            if (g.isNotEmpty() && g != partnerLastGift) {
                pendingGiftReceived = g
                partnerLastGift = g
            }
            val craftStr = json.optString("craft", "")
            if (craftStr.isNotEmpty()) {
                loadCraftBlocksFromJson(craftStr)
            }
            partnerLastActive = json.optLong("time", System.currentTimeMillis())
            partnerConnected = true
            save()
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Sincronização automática pela Nuvem REST. */
    fun syncOnline(onDone: ((Boolean, String) -> Unit)? = null) {
        Thread {
            try {
                val room = roomCode.ifEmpty { "150225" }
                val myKey = "${room}_$activeChar"

                val json = JSONObject()
                json.put("char", activeChar)
                json.put("name", currentPetName())
                json.put("lvl", level())
                json.put("hunger", hunger)
                json.put("joy", joy)
                json.put("likes", if (activeChar == "bunny") bunnyLikes else catLikes)
                json.put("dislikes", if (activeChar == "bunny") bunnyDislikes else catDislikes)
                json.put("color", if (activeChar == "bunny") bunnyFavColor else catFavColor)
                json.put("food", if (activeChar == "bunny") bunnyFavFood else catFavFood)
                json.put("msg", if (activeChar == "bunny") bunnyMessage else catMessage)
                json.put("gift", partnerLastGift)
                json.put("craft", craftBlocksToJson())
                json.put("time", System.currentTimeMillis())

                val urlPut = URL("https://api.restful-api.dev/objects")
                val connPut = (urlPut.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val reqBody = JSONObject()
                reqBody.put("name", myKey)
                reqBody.put("data", json)
                OutputStreamWriter(connPut.outputStream).use { it.write(reqBody.toString()) }
                connPut.responseCode

                partnerConnected = true
                save()
                onDone?.invoke(true, "Sincronizado!")
            } catch (e: Exception) {
                onDone?.invoke(false, "Offline / Código Local")
            }
        }.start()
    }

    fun load(ctx: Context) {
        prefs = ctx.getSharedPreferences("together_pets", Context.MODE_PRIVATE)

        activeChar = prefs.getString("activeChar", "bunny") ?: "bunny"
        language = prefs.getString("language", "pt") ?: "pt"
        soundEnabled = prefs.getBoolean("soundEnabled", true)
        remindersEnabled = prefs.getBoolean("remindersEnabled", true)
        roomCode = prefs.getString("roomCode", "150225") ?: "150225"

        // Ficha Bunny
        reportDoneBunny = prefs.getBoolean("reportDoneBunny", false)
        bunnyName = prefs.getString("bunnyName", "Meu Coelhinho") ?: "Meu Coelhinho"
        bunnyLikes = prefs.getString("bunnyLikes", "Dar abraços e mimos quentinhos") ?: "Dar abraços e mimos quentinhos"
        bunnyDislikes = prefs.getString("bunnyDislikes", "Ficar com saudades") ?: "Ficar com saudades"
        bunnyFavColor = prefs.getString("bunnyFavColor", "Azul Celestial") ?: "Azul Celestial"
        bunnyFavFood = prefs.getString("bunnyFavFood", "Ceviche Peruano") ?: "Ceviche Peruano"
        bunnyMessage = prefs.getString("bunnyMessage", "Amo-te daqui até à lua! ♥") ?: "Amo-te daqui até à lua! ♥"

        // Ficha Cat
        reportDoneCat = prefs.getBoolean("reportDoneCat", false)
        catName = prefs.getString("catName", "Minha Gatinha") ?: "Minha Gatinha"
        catLikes = prefs.getString("catLikes", "Ronronar e receber carinhos fofos") ?: "Ronronar e receber carinhos fofos"
        catDislikes = prefs.getString("catDislikes", "Frio e banhos frios") ?: "Frio e banhos frios"
        catFavColor = prefs.getString("catFavColor", "Rosa Algodão Doce") ?: "Rosa Algodão Doce"
        catFavFood = prefs.getString("catFavFood", "Alfajores com Doce de Leite") ?: "Alfajores com Doce de Leite"
        catMessage = prefs.getString("catMessage", "És o amor da minha vida! ♥") ?: "És o amor da minha vida! ♥"

        // Conexão / Parceiro
        partnerConnected = prefs.getBoolean("partnerConnected", false)
        partnerChar = prefs.getString("partnerChar", if (activeChar == "bunny") "cat" else "bunny") ?: "cat"
        partnerName = prefs.getString("partnerName", "Amor") ?: "Amor"
        partnerLvl = prefs.getInt("partnerLvl", 1)
        partnerLikes = prefs.getString("partnerLikes", "") ?: ""
        partnerDislikes = prefs.getString("partnerDislikes", "") ?: ""
        partnerFavColor = prefs.getString("partnerFavColor", "") ?: ""
        partnerFavFood = prefs.getString("partnerFavFood", "") ?: ""
        partnerMessage = prefs.getString("partnerMessage", "") ?: ""
        partnerLastGift = prefs.getString("partnerLastGift", "") ?: ""

        // Companhia
        hasCat = prefs.getBoolean("hasCat", true)
        catHappiness = prefs.getFloat("catHappiness", 95f)

        // 1. CARREGAR BUNNY (com fallback para chaves antigas)
        bunnyCoins = prefs.getInt("bunny_coins", prefs.getInt("coins", 100))
        bunnyHunger = prefs.getFloat("bunny_hunger", prefs.getFloat("hunger", 85f))
        bunnyHygiene = prefs.getFloat("bunny_hygiene", prefs.getFloat("hygiene", 85f))
        bunnyEnergy = prefs.getFloat("bunny_energy", prefs.getFloat("energy", 85f))
        bunnyJoy = prefs.getFloat("bunny_joy", prefs.getFloat("joy", 85f))
        bunnyHat = prefs.getString("bunny_hat", prefs.getString("hat", "")) ?: ""
        bunnyOutfit = prefs.getString("bunny_outfit", prefs.getString("outfit", "")) ?: ""
        bunnyShoes = prefs.getString("bunny_shoes", prefs.getString("shoes", "")) ?: ""
        bunnyColor = prefs.getString("bunny_color", "col_sky") ?: "col_sky"
        bunnyAcc = prefs.getString("bunny_acc", prefs.getString("acc", "")) ?: ""
        bunnySleeping = prefs.getBoolean("bunny_sleeping", false)
        bunnySleepStartTime = prefs.getLong("bunny_sleepStartTime", 0L)
        bunnyBirthTime = prefs.getLong("bunny_birthTime", System.currentTimeMillis())
        bunnyBonusLevels = prefs.getInt("bunny_bonusLevels", 0)

        bunnyOwned.clear()
        bunnyOwned.addAll(prefs.getStringSet("bunny_owned", emptySet()) ?: emptySet())
        if (bunnyOwned.isEmpty()) {
            bunnyOwned.addAll(listOf("wall_inca", "floor_inca", "col_sky", "food_carrot", "food_ceviche"))
        }
        bunnyDecor.clear()
        bunnyDecor.addAll(prefs.getStringSet("bunny_decor", emptySet()) ?: emptySet())
        for (i in 0 until 3) {
            bunnyWall[i] = prefs.getString("bunny_wall$i", "wall_inca") ?: "wall_inca"
            bunnyFloor[i] = prefs.getString("bunny_floor$i", "floor_inca") ?: "floor_inca"
        }

        // 2. CARREGAR CAT (perfil completamente independente)
        catCoins = prefs.getInt("cat_coins", 100)
        catHunger = prefs.getFloat("cat_hunger", 85f)
        catHygiene = prefs.getFloat("cat_hygiene", 85f)
        catEnergy = prefs.getFloat("cat_energy", 85f)
        catJoy = prefs.getFloat("cat_joy", 85f)
        catHat = prefs.getString("cat_hat", "") ?: ""
        catOutfit = prefs.getString("cat_outfit", "") ?: ""
        catShoes = prefs.getString("cat_shoes", "") ?: ""
        catColor = prefs.getString("cat_color", "cat_col_white") ?: "cat_col_white"
        catAcc = prefs.getString("cat_acc", "") ?: ""
        catSleeping = prefs.getBoolean("cat_sleeping", false)
        catSleepStartTime = prefs.getLong("cat_sleepStartTime", 0L)
        catBirthTime = prefs.getLong("cat_birthTime", System.currentTimeMillis())
        catBonusLevels = prefs.getInt("cat_bonusLevels", 0)

        catOwned.clear()
        catOwned.addAll(prefs.getStringSet("cat_owned", emptySet()) ?: emptySet())
        if (catOwned.isEmpty()) {
            catOwned.addAll(listOf("cat_wall_pink", "cat_floor_cloud", "cat_col_white", "food_alfajor", "food_cupcake"))
        }
        catDecor.clear()
        catDecor.addAll(prefs.getStringSet("cat_decor", emptySet()) ?: emptySet())
        for (i in 0 until 3) {
            catWall[i] = prefs.getString("cat_wall$i", "cat_wall_pink") ?: "cat_wall_pink"
            catFloor[i] = prefs.getString("cat_floor$i", "cat_floor_cloud") ?: "cat_floor_cloud"
        }

        loadCraftBlocksFromJson(prefs.getString("craftBlocks", "") ?: "")

        // Tempo decorrido com a app fechada
        val last = prefs.getLong("last", System.currentTimeMillis())
        val elapsedSec = ((System.currentTimeMillis() - last) / 1000f).coerceIn(0f, 86400f)

        // Processa sono e necessidades para Bunny
        if (bunnySleeping) {
            val sleepGain = elapsedSec * (100f / 120f)
            bunnyEnergy = (bunnyEnergy + sleepGain).coerceIn(0f, 100f)
            bunnyHunger = (bunnyHunger - 0.006f * elapsedSec).coerceIn(5f, 100f)
            bunnyHygiene = (bunnyHygiene - 0.004f * elapsedSec).coerceIn(5f, 100f)
            if (bunnyEnergy >= 100f) bunnySleeping = false
        } else {
            val el = elapsedSec * 0.4f
            bunnyHunger = (bunnyHunger - 0.020f * el).coerceIn(5f, 100f)
            bunnyHygiene = (bunnyHygiene - 0.013f * el).coerceIn(5f, 100f)
            bunnyJoy = (bunnyJoy - 0.017f * el).coerceIn(5f, 100f)
            bunnyEnergy = (bunnyEnergy - 0.010f * el).coerceIn(5f, 100f)
        }

        // Processa sono e necessidades para Cat
        if (catSleeping) {
            val sleepGain = elapsedSec * (100f / 120f)
            catEnergy = (catEnergy + sleepGain).coerceIn(0f, 100f)
            catHunger = (catHunger - 0.006f * elapsedSec).coerceIn(5f, 100f)
            catHygiene = (catHygiene - 0.004f * elapsedSec).coerceIn(5f, 100f)
            if (catEnergy >= 100f) catSleeping = false
        } else {
            val el = elapsedSec * 0.4f
            catHunger = (catHunger - 0.020f * el).coerceIn(5f, 100f)
            catHygiene = (catHygiene - 0.013f * el).coerceIn(5f, 100f)
            catJoy = (catJoy - 0.017f * el).coerceIn(5f, 100f)
            catEnergy = (catEnergy - 0.010f * el).coerceIn(5f, 100f)
        }
    }

    fun save() {
        if (!this::prefs.isInitialized) return
        prefs.edit()
            .putString("activeChar", activeChar)
            .putString("language", language)
            .putBoolean("soundEnabled", soundEnabled)
            .putBoolean("remindersEnabled", remindersEnabled)
            .putString("roomCode", roomCode)
            // Fichas
            .putBoolean("reportDoneBunny", reportDoneBunny)
            .putString("bunnyName", bunnyName)
            .putString("bunnyLikes", bunnyLikes)
            .putString("bunnyDislikes", bunnyDislikes)
            .putString("bunnyFavColor", bunnyFavColor)
            .putString("bunnyFavFood", bunnyFavFood)
            .putString("bunnyMessage", bunnyMessage)
            .putBoolean("reportDoneCat", reportDoneCat)
            .putString("catName", catName)
            .putString("catLikes", catLikes)
            .putString("catDislikes", catDislikes)
            .putString("catFavColor", catFavColor)
            .putString("catFavFood", catFavFood)
            .putString("catMessage", catMessage)
            // Conexão
            .putBoolean("partnerConnected", partnerConnected)
            .putString("partnerChar", partnerChar)
            .putString("partnerName", partnerName)
            .putInt("partnerLvl", partnerLvl)
            .putString("partnerLikes", partnerLikes)
            .putString("partnerDislikes", partnerDislikes)
            .putString("partnerFavColor", partnerFavColor)
            .putString("partnerFavFood", partnerFavFood)
            .putString("partnerMessage", partnerMessage)
            .putString("partnerLastGift", partnerLastGift)
            // Companhia
            .putBoolean("hasCat", hasCat)
            .putFloat("catHappiness", catHappiness)
            // 1. GRAVAR DADOS DO BUNNY
            .putInt("bunny_coins", bunnyCoins)
            .putFloat("bunny_hunger", bunnyHunger)
            .putFloat("bunny_hygiene", bunnyHygiene)
            .putFloat("bunny_energy", bunnyEnergy)
            .putFloat("bunny_joy", bunnyJoy)
            .putString("bunny_hat", bunnyHat)
            .putString("bunny_outfit", bunnyOutfit)
            .putString("bunny_shoes", bunnyShoes)
            .putString("bunny_color", bunnyColor)
            .putString("bunny_acc", bunnyAcc)
            .putBoolean("bunny_sleeping", bunnySleeping)
            .putLong("bunny_sleepStartTime", bunnySleepStartTime)
            .putLong("bunny_birthTime", bunnyBirthTime)
            .putInt("bunny_bonusLevels", bunnyBonusLevels)
            .putStringSet("bunny_owned", HashSet(bunnyOwned))
            .putStringSet("bunny_decor", HashSet(bunnyDecor))
            .putString("bunny_wall0", bunnyWall[0]).putString("bunny_wall1", bunnyWall[1]).putString("bunny_wall2", bunnyWall[2])
            .putString("bunny_floor0", bunnyFloor[0]).putString("bunny_floor1", bunnyFloor[1]).putString("bunny_floor2", bunnyFloor[2])
            // 2. GRAVAR DADOS DA CAT
            .putInt("cat_coins", catCoins)
            .putFloat("cat_hunger", catHunger)
            .putFloat("cat_hygiene", catHygiene)
            .putFloat("cat_energy", catEnergy)
            .putFloat("cat_joy", catJoy)
            .putString("cat_hat", catHat)
            .putString("cat_outfit", catOutfit)
            .putString("cat_shoes", catShoes)
            .putString("cat_color", catColor)
            .putString("cat_acc", catAcc)
            .putBoolean("cat_sleeping", catSleeping)
            .putLong("cat_sleepStartTime", catSleepStartTime)
            .putLong("cat_birthTime", catBirthTime)
            .putInt("cat_bonusLevels", catBonusLevels)
            .putStringSet("cat_owned", HashSet(catOwned))
            .putStringSet("cat_decor", HashSet(catDecor))
            .putString("cat_wall0", catWall[0]).putString("cat_wall1", catWall[1]).putString("cat_wall2", catWall[2])
            .putString("cat_floor0", catFloor[0]).putString("cat_floor1", catFloor[1]).putString("cat_floor2", catFloor[2])
            // CatBunnyCraft
            .putString("craftBlocks", craftBlocksToJson())
            .putLong("last", System.currentTimeMillis())
            .apply()
    }

    fun tick(dt: Float) {
        // Atualiza Bunny
        if (bunnySleeping) {
            bunnyEnergy = (bunnyEnergy + (100f / 120f) * dt).coerceIn(0f, 100f)
            bunnyHunger = (bunnyHunger - 0.005f * dt).coerceIn(0f, 100f)
            bunnyHygiene = (bunnyHygiene - 0.003f * dt).coerceIn(0f, 100f)
            if (bunnyEnergy >= 100f) bunnySleeping = false
        } else {
            bunnyHunger = (bunnyHunger - 0.020f * dt).coerceIn(0f, 100f)
            bunnyHygiene = (bunnyHygiene - 0.013f * dt).coerceIn(0f, 100f)
            bunnyJoy = (bunnyJoy - 0.017f * dt).coerceIn(0f, 100f)
            bunnyEnergy = (bunnyEnergy - 0.010f * dt).coerceIn(0f, 100f)
        }

        // Atualiza Cat
        if (catSleeping) {
            catEnergy = (catEnergy + (100f / 120f) * dt).coerceIn(0f, 100f)
            catHunger = (catHunger - 0.005f * dt).coerceIn(0f, 100f)
            catHygiene = (catHygiene - 0.003f * dt).coerceIn(0f, 100f)
            if (catEnergy >= 100f) catSleeping = false
        } else {
            catHunger = (catHunger - 0.020f * dt).coerceIn(0f, 100f)
            catHygiene = (catHygiene - 0.013f * dt).coerceIn(0f, 100f)
            catJoy = (catJoy - 0.017f * dt).coerceIn(0f, 100f)
            catEnergy = (catEnergy - 0.010f * dt).coerceIn(0f, 100f)
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
