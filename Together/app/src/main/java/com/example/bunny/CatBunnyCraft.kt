package com.example.bunny

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.KeyEvent
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan
import kotlin.random.Random

/**
 * CatBunnyCraft: O Coração 3D do Jogo em Machu Picchu (Peru).
 *
 * Arquitetura em Modo Paisagem (Landscape) 3D na 3ª Pessoa:
 * - Orientação 100% Landscape (Horizontal) cinematográfica e imersiva para jogar.
 * - Terreno 100% Plano (Superflat) em relva esmeralda verde, sem montanhas a obstruir a visão ou o movimento.
 * - Inventário massivo expandido com mais de 80 blocos, madeiras, flores peruanas, decorações, mobília e ferramentas.
 * - Rotação livre de 360° / 180° com o analógico direito do comando PS4 ou arrasto no lado direito do ecrã.
 * - Joystick virtual ergonómico no polegar esquerdo e botões de ação / salto no polegar direito.
 * - Efeitos sonoros procedurais autênticos de colocar blocos, minar e abraços carinhosos.
 * - Casinhas do Bunny e da Gatinha com teletransporte imediato.
 */
class CatBunnyCraftScreen(g: BunnyView) : Screen(g) {
    override val fullBleed = true

    // ================================================================= CATÁLOGO DE BLOCOS & ITENS
    class CraftItem(
        val id: Int,
        val namePt: String,
        val nameEs: String,
        val category: Int,
        val color: Int,
        val isTool: Boolean = false,
        val coinValue: Int = 10
    )

    companion object {
        const val CAT_BUILD = 0
        const val CAT_WOOD = 1
        const val CAT_ORES = 2
        const val CAT_NATURE = 3
        const val CAT_DECOR = 4
        const val CAT_TOOLS = 5

        // 1. CONSTRUÇÃO & ESTRUTURA (15 itens)
        const val ID_GRASS = 1
        const val ID_DIRT = 2
        const val ID_INCA_STONE = 3
        const val ID_COBBLE = 4
        const val ID_STONE_BRICK = 5
        const val ID_BRICK = 6
        const val ID_ADOBE = 7
        const val ID_SAND = 8
        const val ID_SANDSTONE = 9
        const val ID_GLASS = 10
        const val ID_GLASS_PINK = 11
        const val ID_GLASS_BLUE = 12
        const val ID_GLASS_PURPLE = 13
        const val ID_MARBLE = 14
        const val ID_OBSIDIAN = 15

        // 2. MADEIRAS & FLORESTA (12 itens)
        const val ID_OAK_LOG = 16
        const val ID_WOOD_PLANK = 17
        const val ID_BIRCH_LOG = 18
        const val ID_BIRCH_PLANK = 19
        const val ID_DARK_LOG = 20
        const val ID_DARK_PLANK = 21
        const val ID_CHERRY_LOG = 22
        const val ID_CHERRY_PLANK = 23
        const val ID_BAMBOO = 24
        const val ID_FENCE = 25
        const val ID_GATE = 26
        const val ID_DOOR = 27

        // 3. MINÉRIOS, METAIS & GEMAS (15 itens)
        const val ID_ORE_COAL = 28
        const val ID_ORE_IRON = 29
        const val ID_ORE_COPPER = 30
        const val ID_ORE_GOLD = 31
        const val ID_ORE_DIAMOND = 32
        const val ID_ORE_EMERALD = 33
        const val ID_ORE_LAPIS = 34
        const val ID_ORE_REDSTONE = 35
        const val ID_BLOCK_COAL = 36
        const val ID_BLOCK_IRON = 37
        const val ID_BLOCK_GOLD = 38
        const val ID_BLOCK_DIAMOND = 39
        const val ID_BLOCK_EMERALD = 40
        const val ID_BLOCK_AMETHYST = 41
        const val ID_BLOCK_QUARTZ = 42

        // 4. NATUREZA, FLORA & CULTURAS (15 itens)
        const val ID_FLOWER_CANTUTA = 43
        const val ID_ORCHID = 44
        const val ID_ROSE = 45
        const val ID_DANDELION = 46
        const val ID_TULIP_PINK = 47
        const val ID_FLOWER_BLUE = 48
        const val ID_SUNFLOWER = 49
        const val ID_CORN_CHOCLO = 50
        const val ID_POTATO = 51
        const val ID_CARROT = 52
        const val ID_BERRY_BUSH = 53
        const val ID_WHEAT = 54
        const val ID_MELON = 55
        const val ID_PUMPKIN = 56
        const val ID_LEAVES = 57

        // 5. LÃS & DECORAÇÃO COLORIDA (12 itens)
        const val ID_BLOCK_HEART = 58
        const val ID_AGUAYO = 59
        const val ID_WOOL_WHITE = 60
        const val ID_WOOL_PINK = 61
        const val ID_WOOL_BLUE = 62
        const val ID_WOOL_WINE = 63
        const val ID_WOOL_PURPLE = 64
        const val ID_WOOL_GOLD = 65
        const val ID_WOOL_MINT = 66
        const val ID_WOOL_RED = 67
        const val ID_WOOL_BLACK = 68
        const val ID_CARPET = 69

        // 6. MOBÍLIA & FERRAMENTAS (15 itens)
        const val ID_CRAFT_TABLE = 70
        const val ID_FURNACE = 71
        const val ID_CHEST = 72
        const val ID_BED_RED = 73
        const val ID_BED_PINK = 74
        const val ID_BED_BLUE = 75
        const val ID_TORCH = 76
        const val ID_LANTERN = 77
        const val ID_GLOWSTONE = 78
        const val ID_BOOKSHELF = 79
        const val ID_TOOL_PICKAXE = 80
        const val ID_TOOL_SHOVEL = 81
        const val ID_TOOL_AXE = 82
        const val ID_TOOL_SWORD = 83
        const val ID_GOLDEN_APPLE = 84
    }

    val allItems = listOf(
        // Construção & Estrutura
        CraftItem(ID_GRASS, "Relva Verde", "Césped Verde", CAT_BUILD, Color.rgb(92, 172, 75)),
        CraftItem(ID_DIRT, "Terra Fértil", "Tierra Fértil", CAT_BUILD, Color.rgb(134, 92, 58)),
        CraftItem(ID_INCA_STONE, "Pedra Inca Polida", "Piedra Inca Pulida", CAT_BUILD, Color.rgb(165, 170, 180)),
        CraftItem(ID_COBBLE, "Pedregulho dos Andes", "Adoquín Andino", CAT_BUILD, Color.rgb(120, 125, 135)),
        CraftItem(ID_STONE_BRICK, "Tijolos de Pedra", "Ladrillos de Piedra", CAT_BUILD, Color.rgb(145, 148, 155)),
        CraftItem(ID_BRICK, "Tijolos Vermelhos", "Ladrillos Rojos", CAT_BUILD, Color.rgb(180, 85, 70)),
        CraftItem(ID_ADOBE, "Bloco de Adobe", "Bloque de Adobe", CAT_BUILD, Color.rgb(195, 135, 90)),
        CraftItem(ID_SAND, "Areia Dourada", "Arena Dorada", CAT_BUILD, Color.rgb(235, 215, 145)),
        CraftItem(ID_SANDSTONE, "Arenito Esculpido", "Arenisca Tallada", CAT_BUILD, Color.rgb(220, 195, 130)),
        CraftItem(ID_GLASS, "Vidro Cristalino", "Vidrio Cristalino", CAT_BUILD, Color.rgb(215, 240, 255)),
        CraftItem(ID_GLASS_PINK, "Vidro Rosa Amor", "Vidrio Rosa Amor", CAT_BUILD, Color.rgb(255, 185, 215)),
        CraftItem(ID_GLASS_BLUE, "Vidro Azul Celeste", "Vidrio Azul Celeste", CAT_BUILD, Color.rgb(135, 195, 255)),
        CraftItem(ID_GLASS_PURPLE, "Vidro Roxo Real", "Vidrio Morado Real", CAT_BUILD, Color.rgb(175, 120, 220)),
        CraftItem(ID_MARBLE, "Mármore Branco", "Mármol Blanco", CAT_BUILD, Color.rgb(245, 245, 250)),
        CraftItem(ID_OBSIDIAN, "Obsidiana Negra", "Obsidiana Negra", CAT_BUILD, Color.rgb(35, 25, 45), coinValue = 40),

        // Madeiras & Floresta
        CraftItem(ID_OAK_LOG, "Tronco de Carvalho", "Tronco de Roble", CAT_WOOD, Color.rgb(105, 75, 45)),
        CraftItem(ID_WOOD_PLANK, "Tábuas de Carvalho", "Tablas de Roble", CAT_WOOD, Color.rgb(175, 135, 85)),
        CraftItem(ID_BIRCH_LOG, "Tronco de Bétula", "Tronco de Abedul", CAT_WOOD, Color.rgb(235, 230, 220)),
        CraftItem(ID_BIRCH_PLANK, "Tábuas de Bétula", "Tablas de Abedul", CAT_WOOD, Color.rgb(225, 210, 175)),
        CraftItem(ID_DARK_LOG, "Tronco Escuro Andino", "Tronco Oscuro Andino", CAT_WOOD, Color.rgb(70, 50, 35)),
        CraftItem(ID_DARK_PLANK, "Tábuas Escuras", "Tablas Oscuras", CAT_WOOD, Color.rgb(115, 85, 55)),
        CraftItem(ID_CHERRY_LOG, "Tronco de Cerejeira", "Tronco de Cerezo", CAT_WOOD, Color.rgb(180, 110, 120)),
        CraftItem(ID_CHERRY_PLANK, "Tábuas Rosa Cerejeira", "Tablas Rosa Cerezo", CAT_WOOD, Color.rgb(245, 185, 195)),
        CraftItem(ID_BAMBOO, "Bambu da Floresta", "Bambú de la Selva", CAT_WOOD, Color.rgb(115, 185, 65)),
        CraftItem(ID_FENCE, "Cerca de Madeira", "Valla de Madera", CAT_WOOD, Color.rgb(160, 120, 75)),
        CraftItem(ID_GATE, "Portão de Madeira", "Puerta de Valla", CAT_WOOD, Color.rgb(145, 105, 65)),
        CraftItem(ID_DOOR, "Porta de Casa", "Puerta de Casa", CAT_WOOD, Color.rgb(175, 125, 75)),

        // Minérios & Gemas
        CraftItem(ID_ORE_COAL, "Minério de Carvão", "Mineral de Carbón", CAT_ORES, Color.rgb(55, 55, 60)),
        CraftItem(ID_ORE_IRON, "Minério de Ferro", "Mineral de Hierro", CAT_ORES, Color.rgb(215, 185, 165)),
        CraftItem(ID_ORE_COPPER, "Minério de Cobre", "Mineral de Cobre", CAT_ORES, Color.rgb(210, 125, 85)),
        CraftItem(ID_ORE_GOLD, "Ouro Sagrado Inca", "Oro Sagrado Inca", CAT_ORES, Color.rgb(255, 215, 45), coinValue = 50),
        CraftItem(ID_ORE_DIAMOND, "Diamante dos Andes", "Diamante Andino", CAT_ORES, Color.rgb(85, 235, 230), coinValue = 75),
        CraftItem(ID_ORE_EMERALD, "Esmeralda Peruana", "Esmeralda Peruana", CAT_ORES, Color.rgb(45, 215, 115), coinValue = 60),
        CraftItem(ID_ORE_LAPIS, "Minério Lápis-Lazúli", "Mineral Lapislázuli", CAT_ORES, Color.rgb(45, 85, 195)),
        CraftItem(ID_ORE_REDSTONE, "Minério de Redstone", "Mineral de Redstone", CAT_ORES, Color.rgb(235, 45, 45)),
        CraftItem(ID_BLOCK_COAL, "Bloco de Carvão", "Bloque de Carbón", CAT_ORES, Color.rgb(35, 35, 38)),
        CraftItem(ID_BLOCK_IRON, "Bloco de Ferro", "Bloque de Hierro", CAT_ORES, Color.rgb(225, 225, 230)),
        CraftItem(ID_BLOCK_GOLD, "Bloco de Ouro Puro", "Bloque de Oro Puro", CAT_ORES, Color.rgb(255, 205, 30), coinValue = 100),
        CraftItem(ID_BLOCK_DIAMOND, "Bloco de Diamante Radiante", "Bloque de Diamante", CAT_ORES, Color.rgb(95, 245, 240), coinValue = 150),
        CraftItem(ID_BLOCK_EMERALD, "Bloco de Esmeralda", "Bloque de Esmeralda", CAT_ORES, Color.rgb(55, 225, 125)),
        CraftItem(ID_BLOCK_AMETHYST, "Ametista Roxa Cristalina", "Amatista Cristalina", CAT_ORES, Color.rgb(165, 75, 215)),
        CraftItem(ID_BLOCK_QUARTZ, "Bloco de Quartzo Branco", "Bloque de Cuarzo", CAT_ORES, Color.rgb(240, 238, 235)),

        // Natureza & Flores
        CraftItem(ID_FLOWER_CANTUTA, "Flor Cantuta Sagrada", "Flor de Cantuta", CAT_NATURE, Color.rgb(235, 35, 85)),
        CraftItem(ID_ORCHID, "Orquídea de Machu Picchu", "Orquídea Andina", CAT_NATURE, Color.rgb(255, 105, 180)),
        CraftItem(ID_ROSE, "Rosa Vermelha Paixão", "Rosa Roja Pasión", CAT_NATURE, Color.rgb(220, 25, 55)),
        CraftItem(ID_DANDELION, "Dente-de-Leão Amarelo", "Diente de León", CAT_NATURE, Color.rgb(255, 225, 55)),
        CraftItem(ID_TULIP_PINK, "Tulipa Rosa Doce", "Tulipán Rosa", CAT_NATURE, Color.rgb(255, 145, 185)),
        CraftItem(ID_FLOWER_BLUE, "Flor Azul Celeste", "Flor Azul Celeste", CAT_NATURE, Color.rgb(75, 145, 245)),
        CraftItem(ID_SUNFLOWER, "Girassol Andino", "Girasol Andino", CAT_NATURE, Color.rgb(255, 220, 45)),
        CraftItem(ID_CORN_CHOCLO, "Milho Choclo Gigante", "Maíz Choclo Gigante", CAT_NATURE, Color.rgb(255, 240, 160)),
        CraftItem(ID_POTATO, "Batata Nativa Peruana", "Papa Nativa Peruana", CAT_NATURE, Color.rgb(180, 130, 85)),
        CraftItem(ID_CARROT, "Cenoura Doce", "Zanahoria Dulce", CAT_NATURE, Color.rgb(255, 135, 35)),
        CraftItem(ID_BERRY_BUSH, "Bagas Silvestres", "Bayas Silvestres", CAT_NATURE, Color.rgb(195, 35, 85)),
        CraftItem(ID_WHEAT, "Trigo Dourado", "Trigo Dorado", CAT_NATURE, Color.rgb(225, 195, 95)),
        CraftItem(ID_MELON, "Melancia Suculenta", "Sandía Dulce", CAT_NATURE, Color.rgb(115, 175, 55)),
        CraftItem(ID_PUMPKIN, "Abóbora Festiva", "Calabaza Festiva", CAT_NATURE, Color.rgb(225, 125, 35)),
        CraftItem(ID_LEAVES, "Folhas da Floresta", "Hojas del Bosque", CAT_NATURE, Color.rgb(65, 155, 55)),

        // Lãs & Decoração
        CraftItem(ID_BLOCK_HEART, "Coração de Amor a Dois", "Corazón de Amor", CAT_DECOR, Color.rgb(255, 45, 95), coinValue = 30),
        CraftItem(ID_AGUAYO, "Tecido Tradicional Aguayo", "Aguayo Tradicional", CAT_DECOR, Color.rgb(225, 45, 75)),
        CraftItem(ID_WOOL_WHITE, "Lã Branca Neve", "Lana Blanca Suave", CAT_DECOR, Color.rgb(255, 255, 255)),
        CraftItem(ID_WOOL_PINK, "Lã Rosa Bebé", "Lana Rosa Bebé", CAT_DECOR, Color.rgb(255, 165, 205)),
        CraftItem(ID_WOOL_BLUE, "Lã Azul Celestial", "Lana Azul Celestial", CAT_DECOR, Color.rgb(65, 125, 225)),
        CraftItem(ID_WOOL_WINE, "Lã Vinho Nobre", "Lana Vino Noble", CAT_DECOR, Color.rgb(135, 35, 65)),
        CraftItem(ID_WOOL_PURPLE, "Lã Roxa Andina", "Lana Morada Andina", CAT_DECOR, Color.rgb(145, 55, 175)),
        CraftItem(ID_WOOL_GOLD, "Lã Dourada Solar", "Lana Dorada Solar", CAT_DECOR, Color.rgb(245, 205, 45)),
        CraftItem(ID_WOOL_MINT, "Lã Menta Suave", "Lana Menta Suave", CAT_DECOR, Color.rgb(145, 225, 185)),
        CraftItem(ID_WOOL_RED, "Lã Vermelha Amor", "Lana Roja Amor", CAT_DECOR, Color.rgb(215, 35, 55)),
        CraftItem(ID_WOOL_BLACK, "Lã Preta Carvão", "Lana Negra Carbón", CAT_DECOR, Color.rgb(35, 35, 40)),
        CraftItem(ID_CARPET, "Tapete Colorido", "Alfombra Colorida", CAT_DECOR, Color.rgb(240, 115, 160)),

        // Mobília, Luz & Ferramentas
        CraftItem(ID_CRAFT_TABLE, "Bancada de Trabalho", "Mesa de Crafteo", CAT_TOOLS, Color.rgb(165, 115, 65)),
        CraftItem(ID_FURNACE, "Fornalha de Pedra", "Horno de Piedra", CAT_TOOLS, Color.rgb(110, 110, 115)),
        CraftItem(ID_CHEST, "Baú do Tesouro a Dois", "Cofre del Tesoro", CAT_TOOLS, Color.rgb(185, 135, 55)),
        CraftItem(ID_BED_RED, "Cama Vermelha Macia", "Cama Roja Suave", CAT_TOOLS, Color.rgb(215, 45, 65)),
        CraftItem(ID_BED_PINK, "Cama Rosa Princesa", "Cama Rosa Princesa", CAT_TOOLS, Color.rgb(255, 155, 195)),
        CraftItem(ID_BED_BLUE, "Cama Azul Andina", "Cama Azul Andina", CAT_TOOLS, Color.rgb(55, 115, 215)),
        CraftItem(ID_TORCH, "Tocha de Fogo", "Antorcha de Fuego", CAT_TOOLS, Color.rgb(255, 165, 45)),
        CraftItem(ID_LANTERN, "Lanterna Solar Inca", "Linterna Solar Inca", CAT_TOOLS, Color.rgb(255, 235, 120)),
        CraftItem(ID_GLOWSTONE, "Pedra Luminosa Glowstone", "Piedra Luminosa", CAT_TOOLS, Color.rgb(255, 225, 140)),
        CraftItem(ID_BOOKSHELF, "Estante de Livros", "Librería de Madera", CAT_TOOLS, Color.rgb(155, 105, 65)),
        CraftItem(ID_TOOL_PICKAXE, "Picareta de Diamante", "Pico de Diamante", CAT_TOOLS, Color.rgb(85, 235, 230), isTool = true),
        CraftItem(ID_TOOL_SHOVEL, "Pá de Diamante", "Pala de Diamante", CAT_TOOLS, Color.rgb(85, 235, 230), isTool = true),
        CraftItem(ID_TOOL_AXE, "Machado de Diamante", "Hacha de Diamante", CAT_TOOLS, Color.rgb(85, 235, 230), isTool = true),
        CraftItem(ID_TOOL_SWORD, "Espada Dourada Nobre", "Espada Dorada Noble", CAT_TOOLS, Color.rgb(255, 215, 45), isTool = true),
        CraftItem(ID_GOLDEN_APPLE, "Maçã Dourada dos Andes", "Manzana Dorada", CAT_TOOLS, Color.rgb(255, 225, 45), isTool = true)
    )

    // ================================================================= HOTBAR & INVENTÁRIO
    private val hotbarSlotsCount = 9
    private val hotbarItemIds = intArrayOf(
        ID_TOOL_PICKAXE, ID_GRASS, ID_WOOD_PLANK, ID_STONE_BRICK,
        ID_BLOCK_HEART, ID_BLOCK_GOLD, ID_FLOWER_CANTUTA, ID_TORCH, ID_GLASS
    )
    private var selectedHotbarIdx = 0
    private var isInventoryOpen = false
    private var inventoryTab = CAT_BUILD

    // ================================================================= TERRENO 100% PLANO (SUPERFLAT EXPANDIDO 5X)
    private val worldW = 64
    private val worldH = 64
    private val heightMap = Array(worldW) { IntArray(worldH) }
    private val surfaceBlockType = Array(worldW) { IntArray(worldH) }

    // ================================================================= JOGADOR & PARCEIRO (3ª PESSOA)
    private var playerX = 16.0f
    private var playerY = 20.0f
    private var playerZ = 1.0f
    private var playerVy = 0f
    private var isGrounded = true
    private var isMoving = false
    private var moveStepT = 0f

    private var partnerX = 48.0f
    private var partnerY = 44.0f
    private var partnerZ = 1.0f

    // ================================================================= CÂMARA 3D AO NÍVEL DO CHÃO
    private var camYaw = 0.45f
    private var camPitch = 0.22f
    private val camDist = 3.8f

    // Entrada PS4 Gamepad
    private var ps4LStickX = 0f
    private var ps4LStickY = 0f
    private var ps4RStickX = 0f
    private var ps4RStickY = 0f
    private var isGamepadConnected = false

    // Toque no ecrã para rotação da câmara
    private var touchRotating = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    // Joystick virtual para movimento no polegar esquerdo
    private var joyActive = false
    private var joyCenterX = 0f; private var joyCenterY = 0f
    private var joyThumbX = 0f; private var joyThumbY = 0f
    private val joyRadius = 90f

    // Botões da interface Landscape
    private val backBtn = RectF()
    private val invToggleBtn = RectF()
    private val modeToggleBtn = RectF()
    private val jumpActionBtn = RectF()
    private val placeActionBtn = RectF()
    private val mineActionBtn = RectF()
    private val partnerInteractBtn = RectF()
    private val enterHouseBtn = RectF()
    private var nearHouseTarget = 0 // 1 = Bunny, 2 = Cat
    private val hotbarSlotRects = Array(hotbarSlotsCount) { RectF() }

    // Janela do Inventário Massivo
    private val invWindow = RectF()
    private val invCloseBtn = RectF()
    private val invTabBtns = Array(6) { RectF() }
    private val invItemSlots = ArrayList<Pair<RectF, CraftItem>>()

    // Animais Andinos
    class Animal(
        val type: Int, // 0 = Lhama, 1 = Ovelha, 2 = Coelho, 3 = Borboleta
        var x: Float,
        var y: Float,
        var z: Float,
        var facing: Float = 1f,
        var idleT: Float = 2f
    )
    private val animals = ArrayList<Animal>()

    // Nuvens & Ambiente
    private class Cloud(var x: Float, val y: Float, val speed: Float, val size: Float)
    private val clouds = ArrayList<Cloud>()
    private var worldTime = 0f

    private val posePlayer = Pose()
    private val posePartner = Pose()

    private class RenderItem(val depth: Float, val drawAction: () -> Unit)
    private val renderList = ArrayList<RenderItem>()

    init {
        initFlatWorld()
        initFauna()
        for (i in 0 until 8) {
            clouds.add(Cloud(Random.nextFloat() * 1600f, 20f + Random.nextFloat() * 180f, 16f + Random.nextFloat() * 22f, 75f + Random.nextFloat() * 60f))
        }
    }

    /** Terreno 100% plano (Superflat): relva esmeralda lisa e aberta sem montanhas. */
    private fun initFlatWorld() {
        for (x in 0 until worldW) {
            for (y in 0 until worldH) {
                heightMap[x][y] = 1 // Plano absoluto na altura 1
                surfaceBlockType[x][y] = ID_GRASS // Relva verde andina em todo o mapa
            }
        }
    }

    private fun initFauna() {
        animals.clear()
        animals.add(Animal(0, 16.5f, 18.5f, 1f)) // Lhama
        animals.add(Animal(0, 32.5f, 35.5f, 1f)) // Lhama
        animals.add(Animal(0, 45.5f, 42.5f, 1f)) // Lhama
        animals.add(Animal(1, 22.0f, 25.5f, 1f)) // Ovelha
        animals.add(Animal(1, 38.5f, 20.0f, 1f)) // Ovelha
        animals.add(Animal(2, 18.5f, 24.0f, 1f)) // Coelho
        animals.add(Animal(2, 42.0f, 38.0f, 1f)) // Coelho
        animals.add(Animal(3, 20.0f, 22.0f, 2.2f)) // Borboleta
        animals.add(Animal(3, 34.0f, 30.0f, 2.2f)) // Borboleta
        animals.add(Animal(3, 48.0f, 44.0f, 2.2f)) // Borboleta
    }

    override fun enter() {
        super.enter()
        // Ativa orientação LANDSCAPE (Horizontal) cinematográfica para CatBunnyCraft
        (g.context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        if (Theme.isBunny) {
            playerX = 16.0f; playerY = 20.0f; playerZ = 1.0f
            partnerX = 48.0f; partnerY = 44.0f; partnerZ = 1.0f
        } else {
            playerX = 48.0f; playerY = 44.0f; playerZ = 1.0f
            partnerX = 16.0f; partnerY = 20.0f; partnerZ = 1.0f
        }
        camYaw = 0.45f
        camPitch = 0.20f
        g.say("CatBunnyCraft 3D · Mundo 100% Plano & Landscape! ⛏️🎮")
    }

    override fun leave() {
        super.leave()
        // Restaura orientação VERTICAL (Retrato) ao sair
        (g.context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun update(dt: Float) {
        worldTime += dt
        posePlayer.t += dt
        posePartner.t += dt

        for (cl in clouds) {
            cl.x += cl.speed * dt
            if (cl.x > g.width + cl.size * 2f) cl.x = -cl.size * 2f
        }

        // ============================ ROTAÇÃO DA CÂMARA (COMANDO PS4)
        if (abs(ps4RStickX) > 0.15f) {
            camYaw += ps4RStickX * dt * 2.8f
        }
        if (abs(ps4RStickY) > 0.15f) {
            camPitch = (camPitch - ps4RStickY * dt * 1.8f).coerceIn(-0.10f, 0.58f)
        }

        // ============================ MOVIMENTO RELATIVO À CÂMARA (ESTILO MINECRAFT)
        var inX = 0f
        var inY = 0f
        if (joyActive) {
            inX = (joyThumbX - joyCenterX) / joyRadius
            inY = (joyThumbY - joyCenterY) / joyRadius
        } else if (abs(ps4LStickX) > 0.15f || abs(ps4LStickY) > 0.15f) {
            inX = ps4LStickX
            inY = ps4LStickY
            isGamepadConnected = true
        }

        isMoving = false
        val inDist = hypot(inX, inY)
        if (inDist > 0.15f) {
            isMoving = true
            moveStepT += dt * 6.5f
            val speed = 4.2f * dt

            val forwardX = sin(camYaw)
            val forwardY = cos(camYaw)
            val rightX = cos(camYaw)
            val rightY = -sin(camYaw)

            val moveWx = (forwardX * -inY + rightX * inX) * speed
            val moveWy = (forwardY * -inY + rightY * inX) * speed

            playerX = (playerX + moveWx).coerceIn(1.5f, worldW - 2.5f)
            playerY = (playerY + moveWy).coerceIn(1.5f, worldH - 2.5f)
            posePlayer.hop = abs(sin(moveStepT)) * 0.14f
        } else {
            posePlayer.hop = 0f
        }

        // Gravidade e salto no terreno plano
        if (!isGrounded) {
            playerVy -= 24f * dt
            playerZ += playerVy * dt
            if (playerZ <= 1.0f) {
                playerZ = 1.0f
                playerVy = 0f
                isGrounded = true
            }
        }

        // Movimento suave dos animais no plano
        for (an in animals) {
            an.idleT -= dt
            if (an.idleT <= 0f) {
                an.idleT = 2f + Random.nextFloat() * 4f
                if (an.type == 3) {
                    an.x = (an.x + (Random.nextFloat() - 0.5f) * 1.2f).coerceIn(2f, worldW - 3f)
                    an.y = (an.y + (Random.nextFloat() - 0.5f) * 1.2f).coerceIn(2f, worldH - 3f)
                } else {
                    an.x = (an.x + (Random.nextFloat() - 0.5f) * 1.8f).coerceIn(2f, worldW - 3f)
                    an.y = (an.y + (Random.nextFloat() - 0.5f) * 1.8f).coerceIn(2f, worldH - 3f)
                    an.z = 1f
                }
            }
        }

        // Proximidade das casinhas
        val distBunnyH = hypot(playerX - 14.0f, playerY - 18.0f)
        val distCatH = hypot(playerX - 50.0f, playerY - 46.0f)
        nearHouseTarget = when {
            distBunnyH < 3.2f -> 1
            distCatH < 3.2f -> 2
            else -> 0
        }

        updFx(dt)
    }

    /** Projeção em Perspetiva 3D na 3ª pessoa ao nível do chão. */
    private fun project3D(wx: Float, wy: Float, wz: Float, r: RectF): Triple<Float, Float, Float>? {
        val camEyeX = playerX - camDist * cos(camPitch) * sin(camYaw)
        val camEyeY = playerY - camDist * cos(camPitch) * cos(camYaw)
        val camEyeZ = playerZ + 0.95f + camDist * sin(camPitch)

        val dx = wx - camEyeX
        val dy = wy - camEyeY
        val dz = wz - camEyeZ

        val x1 = dx * cos(camYaw) - dy * sin(camYaw)
        val y1 = dx * sin(camYaw) + dy * cos(camYaw)
        val z1 = dz

        val xCam = x1
        val yCam = y1 * cos(camPitch) + z1 * sin(camPitch)
        val zCam = -y1 * sin(camPitch) + z1 * cos(camPitch)

        if (yCam < 0.35f) return null

        val focal = r.height() * 0.95f
        val sx = r.centerX() + (xCam / yCam) * focal
        val sy = (r.centerY() + r.height() * 0.04f) - (zCam / yCam) * focal
        return Triple(sx, sy, yCam)
    }

    override fun draw(c: Canvas, r: RectF) {
        // 1. Céu Panorâmico Andino Landscape
        drawPanoramicSky(c, r)

        // 2. Chão 100% Liso, Contínuo e Unido (Sem perceção de blocos separados)
        drawSeamlessFlatTerrain(c, r)

        renderList.clear()

        // Blocos colocados pelos jogadores (Construção 3D)
        for (cb in Save.craftBlocks) {
            val baseH = heightMap[cb.x.coerceIn(0, worldW - 1)][cb.y.coerceIn(0, worldH - 1)]
            val actualZ = (baseH + cb.z).toFloat()
            val proj = project3D(cb.x + 0.5f, cb.y + 0.5f, actualZ, r) ?: continue
            renderList.add(RenderItem(proj.third) {
                drawVoxelBlock3D(c, cb.x + 0.5f, cb.y + 0.5f, actualZ, cb.type, proj.first, proj.second, proj.third, r, isCustom = true)
            })
        }

        // Casinha do Bunny
        val bHProj = project3D(14.0f, 18.0f, 1.0f, r)
        if (bHProj != null) {
            renderList.add(RenderItem(bHProj.third) {
                drawBunnyHouse(c, bHProj.first, bHProj.second, bHProj.third, r)
            })
        }

        // Casinha da Gatinha
        val cHProj = project3D(50.0f, 46.0f, 1.0f, r)
        if (cHProj != null) {
            renderList.add(RenderItem(cHProj.third) {
                drawCatHouse(c, cHProj.first, cHProj.second, cHProj.third, r)
            })
        }

        // Baú do Tesouro no plano central
        val chestProj = project3D(32.0f, 32.0f, 1.0f, r)
        if (chestProj != null) {
            renderList.add(RenderItem(chestProj.third) {
                drawIncaChest(c, chestProj.first, chestProj.second, chestProj.third, r)
            })
        }

        // Animais Andinos
        for (an in animals) {
            val aProj = project3D(an.x, an.y, an.z, r) ?: continue
            renderList.add(RenderItem(aProj.third) {
                drawAnimal3D(c, an, aProj.first, aProj.second, aProj.third, r)
            })
        }

        // Parceiro(a) na 3ª pessoa
        val partProj = project3D(partnerX, partnerY, partnerZ, r)
        if (partProj != null) {
            renderList.add(RenderItem(partProj.third) {
                drawPartner3D(c, partProj.first, partProj.second, partProj.third, r)
            })
        }

        // Jogador(a) na 3ª pessoa
        val playProj = project3D(playerX, playerY, playerZ, r)
        if (playProj != null) {
            renderList.add(RenderItem(playProj.third) {
                drawPlayer3D(c, playProj.first, playProj.second, playProj.third, r)
            })
        }

        // Painter's Algorithm: ordena por profundidade
        renderList.sortByDescending { it.depth }
        for (item in renderList) item.drawAction.invoke()

        drawFx(c)

        // 3. Interface Landscape oficial estilo Minecraft
        drawHudLandscape(c, r)

        // 4. Joystick Virtual
        if (joyActive) drawJoystick(c)

        // 5. Janela do Inventário Massivo
        if (isInventoryOpen) drawInventoryWindow(c, r)
    }

    private fun drawPanoramicSky(c: Canvas, r: RectF) {
        val skyCol = if (Theme.isBunny) Color.rgb(22, 38, 75) else Color.rgb(95, 175, 245)
        D.f(skyCol); c.drawRect(r, D.p)

        // Sol Radiante Andino
        val sunX = r.right - r.width() * 0.14f
        val sunY = r.top + r.height() * 0.18f
        val sunR = r.height() * 0.14f
        D.circle(c, sunX, sunY, sunR * 1.3f, D.alpha(Color.rgb(255, 235, 140), 75))
        D.circle(c, sunX, sunY, sunR, Color.rgb(255, 225, 60), Color.rgb(255, 195, 30), 2.5f)

        // Silhueta verde suave das montanhas andinas no horizonte distante
        val g1 = Color.rgb(45, 110, 70)
        val g2 = Color.rgb(60, 135, 85)
        D.poly(c, g1, 0, 0f,
            r.left - 40f, r.top + r.height() * 0.52f,
            r.left + r.width() * 0.35f, r.top + r.height() * 0.32f,
            r.left + r.width() * 0.75f, r.top + r.height() * 0.52f
        )
        D.poly(c, g2, 0, 0f,
            r.left + r.width() * 0.45f, r.top + r.height() * 0.52f,
            r.left + r.width() * 0.85f, r.top + r.height() * 0.35f,
            r.right + 40f, r.top + r.height() * 0.52f
        )

        for (cl in clouds) {
            val col = D.alpha(Color.WHITE, 210)
            D.circle(c, cl.x, cl.y, cl.size * 0.40f, col)
            D.circle(c, cl.x + cl.size * 0.35f, cl.y - cl.size * 0.10f, cl.size * 0.48f, col)
            D.circle(c, cl.x + cl.size * 0.70f, cl.y, cl.size * 0.35f, col)
        }
    }

    /** Chão 100% liso, contínuo e unido: base sólida fixa que nunca falha na rotação 360°. */
    private fun drawSeamlessFlatTerrain(c: Canvas, r: RectF) {
        val grassCol = Color.rgb(96, 172, 80)
        val focal = r.height() * 0.95f
        val horizonY = (r.centerY() + r.height() * 0.04f) + tan(camPitch) * focal
        val hy = horizonY.coerceIn(r.top, r.bottom)

        // 1. Chão contínuo 100% liso e sólido preenchendo até ao horizonte
        D.f(grassCol)
        c.drawRect(r.left, hy, r.right, r.bottom, D.p)

        // 2. Haze atmosférico andino na linha do horizonte
        if (hy < r.bottom) {
            val hazeH = (r.height() * 0.12f).coerceAtMost(r.bottom - hy)
            val hazeCol = if (Theme.isBunny) Color.argb(90, 32, 55, 95) else Color.argb(80, 160, 215, 255)
            D.f(hazeCol)
            c.drawRect(r.left, hy, r.right, hy + hazeH, D.p)
        }

        // 3. Detalhes andinos suaves dispersos naturalmente com ordenação
        val pGridX = playerX.toInt()
        val pGridY = playerY.toInt()
        val renderRad = 12

        val minX = max(0, pGridX - renderRad)
        val maxX = min(worldW - 1, pGridX + renderRad)
        val minY = max(0, pGridY - renderRad)
        val maxY = min(worldH - 1, pGridY + renderRad)

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                if ((x * 7 + y * 13) % 9 == 0) {
                    val p = project3D(x + 0.5f, y + 0.5f, 1.0f, r) ?: continue
                    if (p.second >= hy && p.third in 1.2f..35.0f) {
                        val flwCol = if ((x + y) % 2 == 0) Color.rgb(255, 235, 75) else Color.WHITE
                        val sz = (focal / p.third) * 0.07f
                        if (sz in 1.2f..7f) {
                            D.circle(c, p.first, p.second, sz, flwCol)
                        }
                    }
                }
            }
        }

        // Cursor / Mira de Seleção suave no chão
        val forwardX = sin(camYaw)
        val forwardY = cos(camYaw)
        val targetX = (playerX + forwardX * 1.8f).toInt().coerceIn(0, worldW - 1)
        val targetY = (playerY + forwardY * 1.8f).toInt().coerceIn(0, worldH - 1)

        val tp0 = project3D(targetX.toFloat(), targetY.toFloat(), 1.0f, r)
        val tp1 = project3D(targetX + 1f, targetY.toFloat(), 1.0f, r)
        val tp2 = project3D(targetX + 1f, targetY + 1f, 1.0f, r)
        val tp3 = project3D(targetX.toFloat(), targetY + 1f, 1.0f, r)

        if (tp0 != null && tp1 != null && tp2 != null && tp3 != null) {
            val hasBlocks = Save.craftBlocks.any { it.x == targetX && it.y == targetY }
            val reticleColor = if (hasBlocks) Color.rgb(255, 75, 75) else Color.rgb(255, 225, 65)
            val reticleFill = if (hasBlocks) 0x44FF3333 else 0x33FFE140
            D.poly(c, reticleFill, reticleColor, 2.5f,
                tp0.first, tp0.second,
                tp1.first, tp1.second,
                tp2.first, tp2.second,
                tp3.first, tp3.second
            )
        }
    }

    /** Desenha um bloco cúbico estilo Minecraft em perspetiva 3D. */
    private fun drawVoxelBlock3D(c: Canvas, wx: Float, wy: Float, wz: Float, id: Int, sx: Float, sy: Float, depth: Float, r: RectF, isCustom: Boolean = false) {
        val focal = r.height() * 0.95f
        val bSize = (focal / depth) * 0.95f
        if (bSize < 4f) return

        val hw = bSize * 0.50f
        val hh = bSize * 0.35f
        val bH = bSize * 0.65f

        val item = allItems.firstOrNull { it.id == id } ?: allItems.first()
        val baseCol = item.color
        val topCol = baseCol
        val leftCol = D.mix(baseCol, Color.rgb(20, 15, 15), 0.22f)
        val rightCol = D.mix(baseCol, Color.rgb(10, 8, 8), 0.40f)

        // Face Esquerda
        D.poly(c, leftCol, Color.rgb(45, 38, 35), 1.2f,
            sx - hw, sy,
            sx, sy + hh,
            sx, sy + hh + bH,
            sx - hw, sy + bH
        )

        // Face Direita
        D.poly(c, rightCol, Color.rgb(45, 38, 35), 1.2f,
            sx, sy + hh,
            sx + hw, sy,
            sx + hw, sy + bH,
            sx, sy + hh + bH
        )

        // Face Superior (Tampo)
        D.poly(c, topCol, Color.rgb(45, 38, 35), 1.2f,
            sx, sy - hh,
            sx + hw, sy,
            sx, sy + hh,
            sx - hw, sy
        )

        if (id == ID_BLOCK_HEART) {
            D.heart(c, sx, sy, hw * 0.45f, Color.WHITE)
        }
    }

    /** Casinha do Bunny. */
    private fun drawBunnyHouse(c: Canvas, sx: Float, sy: Float, depth: Float, r: RectF) {
        val focal = r.height() * 0.95f
        val s = (focal / depth) * 2.5f
        if (s < 12f) return

        D.rrect(c, sx - s * 0.45f, sy - s * 0.45f, sx + s * 0.45f, sy + s * 0.25f, 6f, Color.rgb(32, 45, 80), Color.rgb(22, 28, 50), 2.5f)
        D.poly(c, Color.rgb(135, 35, 65), Color.rgb(85, 20, 40), 2f,
            sx - s * 0.52f, sy - s * 0.42f,
            sx, sy - s * 0.85f,
            sx + s * 0.52f, sy - s * 0.42f)
        D.rrect(c, sx - s * 0.12f, sy - s * 0.05f, sx + s * 0.12f, sy + s * 0.25f, 3f, Color.rgb(180, 135, 85))
        D.rrect(c, sx - s * 0.35f, sy - s * 0.32f, sx - s * 0.15f, sy - s * 0.12f, 2f, Color.rgb(215, 240, 255))
        D.rrect(c, sx + s * 0.15f, sy - s * 0.32f, sx + s * 0.35f, sy - s * 0.12f, 2f, Color.rgb(215, 240, 255))
        D.rrect(c, sx - s * 0.38f, sy - s * 0.98f, sx + s * 0.38f, sy - s * 0.84f, 4f, Color.rgb(255, 205, 45))
        D.text(c, "🐰 Casinha Bunny", sx, sy - s * 0.88f, s * 0.10f, Color.BLACK)
    }

    /** Casinha da Gatinha. */
    private fun drawCatHouse(c: Canvas, sx: Float, sy: Float, depth: Float, r: RectF) {
        val focal = r.height() * 0.95f
        val s = (focal / depth) * 2.5f
        if (s < 12f) return

        D.rrect(c, sx - s * 0.45f, sy - s * 0.45f, sx + s * 0.45f, sy + s * 0.25f, 6f, Color.rgb(255, 222, 235), Color.rgb(235, 175, 195), 2.5f)
        D.poly(c, Color.rgb(255, 105, 160), Color.rgb(215, 65, 120), 2f,
            sx - s * 0.52f, sy - s * 0.42f,
            sx, sy - s * 0.85f,
            sx + s * 0.52f, sy - s * 0.42f)
        D.poly(c, Color.rgb(255, 222, 235), Color.rgb(255, 105, 160), 1.5f,
            sx - s * 0.32f, sy - s * 0.65f,
            sx - s * 0.20f, sy - s * 0.88f,
            sx - s * 0.08f, sy - s * 0.65f)
        D.poly(c, Color.rgb(255, 222, 235), Color.rgb(255, 105, 160), 1.5f,
            sx + s * 0.08f, sy - s * 0.65f,
            sx + s * 0.20f, sy - s * 0.88f,
            sx + s * 0.32f, sy - s * 0.65f)
        D.rrect(c, sx - s * 0.12f, sy - s * 0.05f, sx + s * 0.12f, sy + s * 0.25f, 3f, Color.rgb(255, 165, 195))
        D.heart(c, sx, sy - s * 0.22f, s * 0.12f, Color.rgb(255, 65, 125))
        D.rrect(c, sx - s * 0.38f, sy - s * 0.98f, sx + s * 0.38f, sy - s * 0.84f, 4f, Color.rgb(255, 205, 45))
        D.text(c, "🐱 Casinha Cat", sx, sy - s * 0.88f, s * 0.10f, Color.BLACK)
    }

    /** Baú do Tesouro. */
    private fun drawIncaChest(c: Canvas, sx: Float, sy: Float, depth: Float, r: RectF) {
        val focal = r.height() * 0.95f
        val s = (focal / depth) * 1.4f
        if (s < 8f) return

        D.rrect(c, sx - s * 0.4f, sy - s * 0.3f, sx + s * 0.4f, sy + s * 0.25f, 4f, Color.rgb(185, 135, 55), Color.BLACK, 2f)
        D.line(c, sx - s * 0.4f, sy - s * 0.05f, sx + s * 0.4f, sy - s * 0.05f, 2.5f, Color.rgb(255, 215, 45))
        D.circle(c, sx, sy - s * 0.05f, s * 0.08f, Color.rgb(255, 215, 45))
    }

    /** Fauna Andina em 3D. */
    private fun drawAnimal3D(c: Canvas, an: Animal, sx: Float, sy: Float, depth: Float, r: RectF) {
        val focal = r.height() * 0.95f
        val s = (focal / depth) * 1.15f
        if (s < 6f) return

        when (an.type) {
            0 -> Icons.draw(c, Icons.LLAMA, sx, sy, s * 0.55f)
            1 -> {
                D.oval(c, sx, sy, s * 0.45f, s * 0.32f, Color.WHITE, Color.rgb(120, 120, 120), 1.5f)
                D.circle(c, sx + s * 0.30f, sy - s * 0.12f, s * 0.18f, Color.rgb(240, 230, 220), Color.rgb(120, 120, 120), 1.2f)
            }
            2 -> {
                D.oval(c, sx, sy, s * 0.32f, s * 0.24f, Color.WHITE, Color.rgb(150, 150, 150), 1.2f)
                D.oval(c, sx - s * 0.06f, sy - s * 0.25f, s * 0.06f, s * 0.16f, Color.WHITE)
                D.oval(c, sx + s * 0.06f, sy - s * 0.25f, s * 0.06f, s * 0.16f, Color.WHITE)
            }
            3 -> {
                val flap = sin(worldTime * 14f) * s * 0.18f
                D.circle(c, sx - s * 0.15f, sy - flap, s * 0.12f, Color.rgb(255, 105, 180))
                D.circle(c, sx + s * 0.15f, sy - flap, s * 0.12f, Color.rgb(105, 185, 255))
            }
        }
    }

    private fun drawPlayer3D(c: Canvas, sx: Float, sy: Float, depth: Float, r: RectF) {
        val focal = r.height() * 0.95f
        val scale = Save.bunnyScale()
        val s = (focal / depth) * 1.45f * scale
        if (s < 10f) return

        D.oval(c, sx, sy + 5f, s * 0.30f, s * 0.12f, D.alpha(Color.BLACK, 65))
        if (Theme.isBunny) {
            BunnyArt.draw(c, sx, sy, s, posePlayer, Save.hat, Save.outfit, Save.acc, Save.shoes, Save.bunnyColor)
        } else {
            CatArt.draw(c, sx, sy, s, posePlayer.t, mood = 0, pounce = posePlayer.squash, hat = Save.hat, outfit = Save.outfit, shoes = Save.shoes, color = Save.petColor, acc = Save.acc)
        }
        val tagY = sy - s * 1.18f
        D.rrect(c, sx - 22f, tagY - 9f, sx + 22f, tagY + 9f, 6f, Theme.primary, Color.WHITE, 1.2f)
        D.text(c, "TU", sx, tagY + 4f, 11f, Color.WHITE)
    }

    private fun drawPartner3D(c: Canvas, sx: Float, sy: Float, depth: Float, r: RectF) {
        val focal = r.height() * 0.95f
        val s = (focal / depth) * 1.35f * 0.85f
        if (s < 10f) return

        D.oval(c, sx, sy + 4f, s * 0.28f, s * 0.11f, D.alpha(Color.BLACK, 55))
        if (Theme.isBunny) {
            CatArt.draw(c, sx, sy, s, posePartner.t, mood = 0, pounce = posePartner.squash, hat = Save.catHat, outfit = Save.catOutfit, shoes = Save.catShoes, color = Save.catColor, acc = Save.catAcc)
        } else {
            BunnyArt.draw(c, sx, sy, s, posePartner, Save.bunnyHat, Save.bunnyOutfit, Save.bunnyAcc, Save.bunnyShoes, Save.bunnyColor)
        }
        val heartY = sy - s * 1.20f
        D.heart(c, sx, heartY, 12f, Color.rgb(255, 45, 95), Color.WHITE, 1.2f)
        val pName = if (Save.partnerConnected) Save.partnerName else if (Theme.isBunny) "Gatinha" else "Bunny"
        D.text(c, pName, sx, heartY - 11f, 11f, Theme.primary)
    }

    /** Interface Landscape elegante e ampla com suporte completo a comando PS4. */
    private fun drawHudLandscape(c: Canvas, r: RectF) {
        // Miradouro central (Crosshair estilo Minecraft)
        val cx = r.centerX()
        val cy = r.centerY() + r.height() * 0.04f
        D.line(c, cx - 14f, cy, cx + 14f, cy, 2.8f, D.alpha(Color.WHITE, 200))
        D.line(c, cx, cy - 14f, cx, cy + 14f, 2.8f, D.alpha(Color.WHITE, 200))

        // Barra Superior Landscape
        val btnH = r.height() * 0.09f
        backBtn.set(r.left + 24f, r.top + 16f, r.left + 140f, r.top + 16f + btnH)
        D.button(c, backBtn, "← Voltar", Theme.card, Theme.text, btnH * 0.35f)

        // Botão Inventário
        invToggleBtn.set(backBtn.right + 14f, backBtn.top, backBtn.right + 190f, backBtn.bottom)
        D.rrect(c, invToggleBtn, btnH * 0.35f, Color.rgb(35, 40, 55), Color.rgb(255, 205, 45), 2f)
        D.text(c, "🎒 Inventário", invToggleBtn.centerX(), invToggleBtn.centerY() + btnH * 0.16f, btnH * 0.34f, Color.rgb(255, 225, 80))

        // Alternador de Modo (Construir / Minar)
        val modeW = 160f
        modeToggleBtn.set(invToggleBtn.right + 14f, backBtn.top, invToggleBtn.right + 14f + modeW, backBtn.bottom)
        val activeItem = allItems.firstOrNull { it.id == hotbarItemIds[selectedHotbarIdx] }
        val isTool = activeItem?.isTool == true
        val modeTxt = if (isTool) "⛏️ Minar" else "🧱 Construir"
        val modeCol = if (isTool) Color.rgb(182, 42, 42) else Color.rgb(42, 142, 62)
        D.rrect(c, modeToggleBtn, btnH * 0.35f, modeCol, Color.WHITE, 1.8f)
        D.text(c, modeTxt, modeToggleBtn.centerX(), modeToggleBtn.centerY() + btnH * 0.16f, btnH * 0.34f, Color.WHITE)

        // Moedas à direita
        val coinPill = RectF(r.right - 180f, backBtn.top, r.right - 24f, backBtn.bottom)
        D.rrect(c, coinPill, btnH * 0.4f, Color.rgb(26, 36, 68), Color.rgb(255, 205, 45), 2f)
        Icons.draw(c, Icons.COIN, coinPill.left + btnH * 0.55f, coinPill.centerY(), btnH * 0.35f)
        D.text(c, Save.coins.toString(), coinPill.centerX() + btnH * 0.2f, coinPill.centerY() + btnH * 0.16f, btnH * 0.38f, Color.rgb(255, 225, 80))

        // Botões de Ação no canto inferior direito: Saltar, Colocar Bloco, Eliminar Bloco
        val actBtnH = r.height() * 0.12f
        val actBtnW = 120f
        jumpActionBtn.set(r.right - actBtnW - 20f, r.bottom - actBtnH - 16f, r.right - 20f, r.bottom - 16f)
        D.button(c, jumpActionBtn, "Saltar ⬆", Theme.primary, Color.WHITE, actBtnH * 0.30f)

        val placeW = 115f
        placeActionBtn.set(jumpActionBtn.left - placeW - 10f, jumpActionBtn.top, jumpActionBtn.left - 10f, jumpActionBtn.bottom)
        D.button(c, placeActionBtn, "🧱 Colocar", Color.rgb(45, 142, 65), Color.WHITE, actBtnH * 0.28f)

        val mineW = 115f
        mineActionBtn.set(placeActionBtn.left - mineW - 10f, jumpActionBtn.top, placeActionBtn.left - 10f, jumpActionBtn.bottom)
        D.button(c, mineActionBtn, "⛏️ Eliminar", Color.rgb(195, 45, 45), Color.WHITE, actBtnH * 0.28f)

        // Botão de Interação com parceiro
        val distToPartner = hypot(playerX - partnerX, playerY - partnerY)
        if (distToPartner < 3.2f) {
            val actW = 220f
            partnerInteractBtn.set(r.centerX() - actW / 2f, r.top + r.height() * 0.15f, r.centerX() + actW / 2f, r.top + r.height() * 0.15f + btnH)
            D.button(c, partnerInteractBtn, "♥ Abraço (+25 🪙)", Color.rgb(235, 35, 85), Color.WHITE, btnH * 0.34f)
        } else {
            partnerInteractBtn.setEmpty()
        }

        // Botão de Entrar na Casinha
        if (nearHouseTarget != 0) {
            val hW = 260f
            val hY = if (partnerInteractBtn.isEmpty) r.top + r.height() * 0.15f else partnerInteractBtn.bottom + 10f
            enterHouseBtn.set(r.centerX() - hW / 2f, hY, r.centerX() + hW / 2f, hY + btnH)
            val hTxt = if (nearHouseTarget == 1) "🏠 Entrar Casinha Bunny" else "🏠 Entrar Casinha Cat"
            val hCol = if (nearHouseTarget == 1) Color.rgb(42, 92, 172) else Color.rgb(222, 72, 122)
            D.button(c, enterHouseBtn, hTxt, hCol, Color.WHITE, btnH * 0.32f)
        } else {
            enterHouseBtn.setEmpty()
        }

        // Hotbar oficial com 9 slots na base landscape
        drawHotbarLandscape(c, r)
    }

    private fun drawHotbarLandscape(c: Canvas, r: RectF) {
        val barH = r.height() * 0.13f
        val slotSize = min(56f, (r.width() * 0.52f) / hotbarSlotsCount)
        val barW = slotSize * hotbarSlotsCount + 16f
        val barLeft = r.centerX() - barW / 2f
        val barTop = r.bottom - barH - 12f

        D.rrect(c, barLeft, barTop, barLeft + barW, barTop + barH, 8f, Color.rgb(35, 38, 48), Color.rgb(85, 90, 105), 2f)

        for (i in 0 until hotbarSlotsCount) {
            val sx = barLeft + 8f + i * slotSize
            hotbarSlotRects[i].set(sx + 2f, barTop + 4f, sx + slotSize - 2f, barTop + barH - 4f)
            val isSelected = i == selectedHotbarIdx
            val slotCol = if (isSelected) Color.rgb(255, 205, 45) else Color.rgb(55, 60, 75)
            val slotBorder = if (isSelected) Color.WHITE else Color.rgb(75, 80, 95)
            D.rrect(c, hotbarSlotRects[i], 6f, slotCol, slotBorder, if (isSelected) 2.5f else 1.2f)

            val itemId = hotbarItemIds[i]
            val item = allItems.firstOrNull { it.id == itemId }
            if (item != null) {
                val cx = hotbarSlotRects[i].centerX()
                val cy = hotbarSlotRects[i].centerY()
                if (item.isTool) {
                    Icons.draw(c, Icons.PICKAXE, cx, cy, slotSize * 0.35f)
                } else {
                    D.rrect(c, cx - slotSize * 0.28f, cy - slotSize * 0.28f, cx + slotSize * 0.28f, cy + slotSize * 0.28f, 3f, item.color, Color.BLACK, 1.2f)
                    if (item.id == ID_BLOCK_HEART) D.heart(c, cx, cy, slotSize * 0.18f, Color.WHITE)
                }
            }
        }

        val activeItem = allItems.firstOrNull { it.id == hotbarItemIds[selectedHotbarIdx] }
        val name = if (Save.language == "es") activeItem?.nameEs else activeItem?.namePt
        D.text(c, "$name", r.centerX(), barTop - 8f, r.height() * 0.035f, Color.WHITE)
    }

    /** Janela do Inventário Massivo Expandido (84 blocos e itens em 6 categorias). */
    private fun drawInventoryWindow(c: Canvas, r: RectF) {
        D.f(D.alpha(Color.BLACK, 195)); c.drawRect(r, D.p)

        val winW = r.width() * 0.88f
        val winH = r.height() * 0.88f
        invWindow.set(r.centerX() - winW / 2f, r.centerY() - winH / 2f, r.centerX() + winW / 2f, r.centerY() + winH / 2f)
        D.rrect(c, invWindow, 14f, Color.rgb(36, 40, 52), Color.rgb(255, 205, 45), 3f)

        invCloseBtn.set(invWindow.right - 42f, invWindow.top + 10f, invWindow.right - 10f, invWindow.top + 42f)
        D.button(c, invCloseBtn, "✕", Color.rgb(195, 45, 45), Color.WHITE, 16f)

        D.text(c, "🎒 Inventário Completo estilo Minecraft", invWindow.centerX(), invWindow.top + 28f, 16f, Color.rgb(255, 225, 80))

        val tabNames = arrayOf("Construção", "Madeira", "Minérios", "Natureza", "Lãs / Cores", "Mobília / Ferramentas")
        val tabW = (winW - 24f) / 6f
        val tabH = 32f
        val tabY0 = invWindow.top + 48f

        for (i in 0 until 6) {
            val tx = invWindow.left + 12f + i * tabW
            invTabBtns[i].set(tx + 2f, tabY0, tx + tabW - 2f, tabY0 + tabH)
            val isSelTab = i == inventoryTab
            val tCol = if (isSelTab) Color.rgb(255, 205, 45) else Color.rgb(55, 60, 75)
            val txtCol = if (isSelTab) Color.rgb(30, 35, 45) else Color.WHITE
            D.rrect(c, invTabBtns[i], 6f, tCol, Color.rgb(85, 90, 105), 1.2f)
            D.text(c, tabNames[i], invTabBtns[i].centerX(), invTabBtns[i].centerY() + 4.5f, 10.5f, txtCol)
        }

        invItemSlots.clear()
        val filtered = allItems.filter { it.category == inventoryTab }
        val cols = 8
        val slotW = (winW - 48f) / cols
        val slotH = min(slotW, (winH - 120f) / 3f)
        val gridTop = tabY0 + tabH + 14f

        for (idx in filtered.indices) {
            val col = idx % cols
            val row = idx / cols
            val ix = invWindow.left + 24f + col * slotW
            val iy = gridTop + row * (slotH + 6f)
            val sRect = RectF(ix + 3f, iy + 3f, ix + slotW - 3f, iy + slotH - 3f)
            val itm = filtered[idx]
            invItemSlots.add(Pair(sRect, itm))

            D.rrect(c, sRect, 6f, Color.rgb(50, 55, 70), Color.rgb(80, 85, 105), 1.2f)
            val cx = sRect.centerX(); val cy = sRect.centerY()
            if (itm.isTool) {
                Icons.draw(c, Icons.PICKAXE, cx, cy - 6f, slotH * 0.35f)
            } else {
                D.rrect(c, cx - slotH * 0.28f, cy - slotH * 0.28f - 6f, cx + slotH * 0.28f, cy + slotH * 0.28f - 6f, 3f, itm.color, Color.BLACK, 1.2f)
                if (itm.id == ID_BLOCK_HEART) D.heart(c, cx, cy - 6f, slotH * 0.16f, Color.WHITE)
            }
            val name = if (Save.language == "es") itm.nameEs else itm.namePt
            D.text(c, name, cx, sRect.bottom - 4f, 8.5f, Color.rgb(220, 230, 245))
        }

        D.text(c, "Toca num bloco para equipar na Hotbar!", invWindow.centerX(), invWindow.bottom - 12f, 12f, Color.rgb(205, 225, 255))
    }

    private fun drawJoystick(c: Canvas) {
        D.circle(c, joyCenterX, joyCenterY, joyRadius, D.alpha(Color.BLACK, 85), D.alpha(Color.WHITE, 140), 2.5f)
        D.circle(c, joyThumbX, joyThumbY, joyRadius * 0.38f, Color.rgb(255, 205, 45), Color.WHITE, 2f)
    }

    // ================================================================= SUPORTE TOTAL A COMANDO PS4
    override fun onGamepadKey(event: KeyEvent): Boolean {
        isGamepadConnected = true
        if (event.action != KeyEvent.ACTION_DOWN) return false

        when (event.keyCode) {
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (isGrounded) {
                    playerVy = 9.5f
                    isGrounded = false
                    burst(g.width / 2f, g.height / 2f, Icons.SPARK, 4, 10f)
                }
                return true
            }
            KeyEvent.KEYCODE_BUTTON_X -> {
                interactWithPartnerOrAnimal()
                return true
            }
            KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_BUTTON_SELECT -> {
                isInventoryOpen = !isInventoryOpen
                return true
            }
            KeyEvent.KEYCODE_BUTTON_L1 -> {
                selectedHotbarIdx = (selectedHotbarIdx - 1 + hotbarSlotsCount) % hotbarSlotsCount
                return true
            }
            KeyEvent.KEYCODE_BUTTON_R1 -> {
                selectedHotbarIdx = (selectedHotbarIdx + 1) % hotbarSlotsCount
                return true
            }
            KeyEvent.KEYCODE_BUTTON_R2 -> {
                mineCurrentTargetBlock()
                return true
            }
            KeyEvent.KEYCODE_BUTTON_L2 -> {
                placeCurrentHotbarBlock()
                return true
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
        isGamepadConnected = true
        ps4LStickX = event.getAxisValue(MotionEvent.AXIS_X)
        ps4LStickY = event.getAxisValue(MotionEvent.AXIS_Y)

        ps4RStickX = event.getAxisValue(MotionEvent.AXIS_Z)
        ps4RStickY = event.getAxisValue(MotionEvent.AXIS_RZ)
        if (abs(ps4RStickX) < 0.1f) ps4RStickX = event.getAxisValue(MotionEvent.AXIS_RX)
        if (abs(ps4RStickY) < 0.1f) ps4RStickY = event.getAxisValue(MotionEvent.AXIS_RY)

        val rTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        val lTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
        if (rTrigger > 0.6f) mineCurrentTargetBlock()
        if (lTrigger > 0.6f) placeCurrentHotbarBlock()

        return true
    }

    // ================================================================= TOQUE / CLIQUE LANDSCAPE
    override fun down(x: Float, y: Float, r: RectF) {
        if (isInventoryOpen) {
            if (invCloseBtn.contains(x, y)) { isInventoryOpen = false; return }
            for (i in 0 until 6) {
                if (invTabBtns[i].contains(x, y)) { inventoryTab = i; return }
            }
            for (slot in invItemSlots) {
                if (slot.first.contains(x, y)) {
                    hotbarItemIds[selectedHotbarIdx] = slot.second.id
                    g.say("Equipado: ${slot.second.namePt}!")
                    isInventoryOpen = false
                    return
                }
            }
            return
        }

        if (backBtn.contains(x, y)) {
            (g.context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            g.go(Nav.MULTIPLAYER)
            return
        }

        if (invToggleBtn.contains(x, y)) {
            isInventoryOpen = true; return
        }

        if (modeToggleBtn.contains(x, y)) {
            val curId = hotbarItemIds[selectedHotbarIdx]
            val itm = allItems.firstOrNull { it.id == curId }
            if (itm?.isTool == true) {
                hotbarItemIds[selectedHotbarIdx] = ID_GRASS
            } else {
                hotbarItemIds[selectedHotbarIdx] = ID_TOOL_PICKAXE
            }
            return
        }

        if (jumpActionBtn.contains(x, y)) {
            if (isGrounded) {
                playerVy = 9.5f
                isGrounded = false
                burst(r.centerX(), r.centerY(), Icons.SPARK, 4, 10f)
            }
            return
        }

        if (placeActionBtn.contains(x, y)) {
            placeCurrentHotbarBlock()
            return
        }

        if (mineActionBtn.contains(x, y)) {
            mineCurrentTargetBlock()
            return
        }

        if (!partnerInteractBtn.isEmpty && partnerInteractBtn.contains(x, y)) {
            interactWithPartnerOrAnimal()
            return
        }

        if (!enterHouseBtn.isEmpty && enterHouseBtn.contains(x, y)) {
            tryEnterHouse()
            return
        }

        for (i in 0 until hotbarSlotsCount) {
            if (hotbarSlotRects[i].contains(x, y)) {
                selectedHotbarIdx = i
                return
            }
        }

        // Lado esquerdo: Joystick virtual
        if (x < r.width() * 0.45f && y > r.height() * 0.35f) {
            joyActive = true
            joyCenterX = x; joyCenterY = y
            joyThumbX = x; joyThumbY = y
            return
        }

        // Lado direito: Rotação da câmara em 360° / 180°
        if (x >= r.width() * 0.45f && y < r.bottom - r.height() * 0.20f) {
            touchRotating = true
            lastTouchX = x
            lastTouchY = y
            return
        }

        handleWorldTapAction()
    }

    override fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {
        if (joyActive) {
            val dist = hypot(x - joyCenterX, y - joyCenterY)
            if (dist <= joyRadius) {
                joyThumbX = x; joyThumbY = y
            } else {
                joyThumbX = joyCenterX + (x - joyCenterX) / dist * joyRadius
                joyThumbY = joyCenterY + (y - joyCenterY) / dist * joyRadius
            }
            return
        }

        if (touchRotating) {
            val deltaX = x - lastTouchX
            val deltaY = y - lastTouchY
            lastTouchX = x
            lastTouchY = y

            camYaw += (deltaX / r.width()) * 3.6f
            camPitch = (camPitch - (deltaY / r.height()) * 2.2f).coerceIn(-0.10f, 0.58f)
        }
    }

    override fun up(x: Float, y: Float, r: RectF) {
        joyActive = false
        if (touchRotating) {
            touchRotating = false
        }
    }

    private fun interactWithPartnerOrAnimal() {
        val distToPartner = hypot(playerX - partnerX, playerY - partnerY)
        if (distToPartner < 3.2f) {
            g.earn(25)
            burst(partnerX * 20f, partnerY * 20f, Icons.HEART, 12, 20f)
            SoundFx.playKiss()
            SoundFx.playPlay(Theme.isCat)
            Save.partnerLastGift = "kiss"
            Save.syncOnline { _, _ -> }
            Save.save()
            val msg = if (Save.partnerConnected && Save.partnerMessage.isNotEmpty()) Save.partnerMessage else "Amo-te tanto! Machu Picchu é nosso! ♥"
            g.say("Abraço Doce! +25 moedas! \"$msg\"")
        }
    }

    private fun tryEnterHouse() {
        (g.context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (nearHouseTarget == 1) {
            Save.activeChar = "bunny"
            g.say("A entrar na Casinha do Bunny! 🐰")
        } else {
            Save.activeChar = "cat"
            g.say("A entrar na Casinha da Gatinha! 🐱")
        }
        Save.save()
        g.go(Nav.HOME)
    }

    private fun placeCurrentHotbarBlock() {
        val forwardX = sin(camYaw)
        val forwardY = cos(camYaw)
        val targetX = (playerX + forwardX * 1.8f).toInt().coerceIn(0, worldW - 1)
        val targetY = (playerY + forwardY * 1.8f).toInt().coerceIn(0, worldH - 1)

        val selId = hotbarItemIds[selectedHotbarIdx]
        val itm = allItems.firstOrNull { it.id == selId }
        if (itm?.isTool == true) return

        val existing = Save.craftBlocks.filter { it.x == targetX && it.y == targetY }
        val newZ = existing.size + 1
        Save.addCraftBlock(targetX, targetY, newZ, selId)
        SoundFx.playBuild()
        burst(g.width / 2f, g.height / 2f, Icons.SPARK, 6, 12f)
        g.earn(10)
        g.say("+10 moedas! ${itm?.namePt} colocado!")
    }

    private fun mineCurrentTargetBlock() {
        val forwardX = sin(camYaw)
        val forwardY = cos(camYaw)
        val targetX = (playerX + forwardX * 1.8f).toInt().coerceIn(0, worldW - 1)
        val targetY = (playerY + forwardY * 1.8f).toInt().coerceIn(0, worldH - 1)

        val existing = Save.craftBlocks.filter { it.x == targetX && it.y == targetY }
        if (existing.isNotEmpty()) {
            val topBlock = existing.maxByOrNull { it.z }!!
            Save.removeCraftBlock(targetX, targetY, topBlock.z)
            SoundFx.playMine()
            burst(g.width / 2f, g.height / 2f, Icons.ROCK, 8, 14f)
            g.earn(20)
            g.say("+20 moedas mineradas! ⛏️")
        } else {
            SoundFx.playMine()
            burst(g.width / 2f, g.height / 2f, Icons.SPARK, 4, 10f)
            g.say("Chão base protegido! Usa 'Colocar' para construir e 'Eliminar' para retirar!")
        }
    }

    private fun handleWorldTapAction() {
        val selId = hotbarItemIds[selectedHotbarIdx]
        val itm = allItems.firstOrNull { it.id == selId }
        if (itm?.isTool == true) {
            mineCurrentTargetBlock()
        } else {
            placeCurrentHotbarBlock()
        }
    }
}
