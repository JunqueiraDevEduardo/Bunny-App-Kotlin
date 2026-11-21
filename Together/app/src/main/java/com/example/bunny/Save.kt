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
        // COMIDA & DELÍCIAS (Peru, Portugal, Doces - 5X Preços)
        // ==========================================
        Item("food_ceviche", "Ceviche Peruano", FOOD, 175, "Peixe marinado fresco com lima, cebola roxa e milho choclo", "both"),
        Item("food_lomo", "Lomo Saltado", FOOD, 200, "Carne salteada andina no wok com batatas douradas", "both"),
        Item("food_chicha", "Chicha Morada", FOOD, 100, "Bebida tradicional de milho roxo, canela e cravinho", "both"),
        Item("food_alfajor", "Alfajores Peruanos", FOOD, 125, "Biscoito com doce de leite e açúcar em pó", "both"),
        Item("food_causa", "Causa Limeña", FOOD, 150, "Massa de batata amarela com abacate e lima", "both"),
        Item("food_choclo", "Choclo c/ Queijo", FOOD, 100, "Milho gigante dos Andes com fatia de queijo fresco", "both"),
        Item("food_anticuchos", "Anticuchos Andinos", FOOD, 180, "Espetadas tradicionais temperadas com aji panca", "both"),
        Item("food_huancaina", "Papa a la Huancaína", FOOD, 160, "Batatas macias com molho cremoso de queijo e aji amarillo", "both"),
        Item("food_picarones", "Picarones Melosos", FOOD, 120, "Filhoses de abóbora e batata-doce com calda de chancaca", "both"),
        Item("food_mazamorra", "Mazamorra Morada", FOOD, 110, "Sobremesa quente de milho roxo com frutas secas e canela", "both"),
        Item("food_aji_gallina", "Ají de Gallina", FOOD, 195, "Creme andino aveludado de frango com azeitonas e nozes", "both"),
        Item("food_pastel_nata", "Pastel de Nata", FOOD, 130, "Pastel folhado estaladiço com creme polvilhado de canela", "both"),
        Item("food_francesinha", "Francesinha do Porto", FOOD, 220, "Tosta farta com carnes, queijo derretido e molho picante", "both"),
        Item("food_bacalhau", "Bacalhau à Brás", FOOD, 210, "Bacalhau desfiado com batata-palha fina, ovos e salsa", "both"),
        Item("food_caldo_verde", "Caldo Verde Minhoto", FOOD, 105, "Sopa tradicional de couve-galega e azeite português", "both"),
        Item("food_sardinha", "Sardinha Assada", FOOD, 150, "Sardinha fresca assada na brasa sobre fatia de broa", "both"),
        Item("food_arroz_pato", "Arroz de Pato", FOOD, 205, "Arroz tostado no forno com rodelas de chouriço tradicional", "both"),
        Item("food_bifana", "Bifana à Moda do Porto", FOOD, 145, "Pão quente com febras tenras em molho de cerveja e louro", "both"),
        Item("food_queijada", "Queijada de Sintra", FOOD, 115, "Doce histórico com queijo fresco e canela perfumada", "both"),
        Item("food_travesseiro", "Travesseiro de Sintra", FOOD, 125, "Folhado crocante recheado com creme de ovo e amêndoa", "both"),
        Item("food_bolo_bolacha", "Bolo de Bolacha", FOOD, 135, "Camadas de bolacha embebidas em café com creme aveludado", "both"),
        Item("food_carrot", "Cenoura Doce", FOOD, 0, "A favorita doce e fresca acabada de colher", "both"),
        Item("food_apple", "Maçã de Alcobaça", FOOD, 75, "Maçã fresca, sumarenta e estaladiça", "both"),
        Item("food_berry", "Bagas Silvestres", FOOD, 90, "Frutos vermelhos doces colhidos no bosque", "both"),
        Item("food_cupcake", "Cupcake Festivo", FOOD, 140, "Bolinho fofo com cobertura colorida de festa", "both"),
        Item("food_potion_energy", "Super Poção Energia", FOOD, 300, "Recupera 100% da energia imediatamente", "both"),
        Item("food_potion_clean", "Super Poção Banho", FOOD, 250, "Deixa o pet 100% limpo, cheiroso e radiante", "both"),

        // ==========================================
        // CHAPÉUS: BUNNY (Azul / Vinho / Roxo / Peru / Portugal)
        // ==========================================
        Item("hat_chullo", "Chullo Andino", HAT, 425, "Gorro peruano tradicional de lã com borlas e abas para as orelhas", "bunny"),
        Item("hat_montera", "Montera Peruana", HAT, 450, "Chapéu tradicional de festa andina decorado", "bunny"),
        Item("hat_barrete_pt", "Barrete Campino Português", HAT, 390, "Gorro clássico português verde e preto com borla de lã", "bunny"),
        Item("hat_bone_porto", "Boné Azul Invicta", HAT, 380, "Boné desportivo azul escuro de campeão", "bunny"),
        Item("hat_beanie", "Gorro Azul Meia-Noite", HAT, 375, "Gorro quente de lã azul escuro", "bunny"),
        Item("hat_cap", "Boné Roxo Imperial", HAT, 350, "Boné elegante de veludo roxo", "bunny"),
        Item("hat_crown_inca", "Coroa de Ouro Inca", HAT, 750, "Coroa sagrada do imperador inca esculpida em ouro", "bunny"),
        Item("hat_straw", "Chapéu de Palha Minhoto", HAT, 325, "Chapéu campestre fresco para dias de sol", "bunny"),
        Item("hat_cartola_fado", "Cartola Lisboa Antiga", HAT, 480, "Cartola nobre de veludo para noites de fado", "bunny"),
        Item("hat_coroa_cusco", "Coroa Imperial de Cusco", HAT, 700, "Coroa dourada majestosa com pedras de lápis-lazúli", "bunny"),
        Item("hat_boina_alentejo", "Boina Alentejana", HAT, 360, "Boina de lã pura azul escura muito elegante", "bunny"),

        // CHAPÉUS: CAT (Rosa / Brancos / Princesa / Portugal)
        Item("cat_hat_bow", "Laço Rosa Princesa", HAT, 225, "Laço cor-de-rosa de cetim fofinho", "cat"),
        Item("cat_hat_flower", "Tiara de Flores Rosa", HAT, 325, "Flores brancas e rosa delicadas com folhas verdes", "cat"),
        Item("cat_hat_viana", "Lenço de Viana Rosa", HAT, 460, "Lenço minhoto tradicional bordado com coração de Viana", "cat"),
        Item("cat_hat_tiara_perolas", "Tiara de Pérolas Brancas", HAT, 490, "Pérolas brancas brilhantes com pequeno laço rosa", "cat"),
        Item("cat_hat_crown", "Coroa de Princesa Rosa", HAT, 700, "Coroa dourada com brilhante rubi rosa central", "cat"),
        Item("cat_hat_beret", "Boina Branca de Lã", HAT, 375, "Boina elegante e macia como uma nuvem", "cat"),
        Item("cat_hat_party", "Chapéu de Festa Rosa", HAT, 250, "Cone festivo brilhante com pom-pom branco", "cat"),
        Item("cat_hat_veu", "Véu de Princesa", HAT, 550, "Véu suave em tule branco transparente com tiara", "cat"),
        Item("cat_hat_orelhinhas", "Capuz de Gatinha Rosa", HAT, 380, "Capuz quentinho de lã com orelhinhas fofas", "cat"),
        Item("cat_hat_diadema_estrelas", "Diadema Estrelas Rosa", HAT, 440, "Diadema com estrelas douradas e brilhantes", "cat"),
        Item("cat_hat_boina_lisboa", "Boina Romântica Branca", HAT, 420, "Boina clássica com bordado floral artesanal", "cat"),

        // ==========================================
        // ROUPA: BUNNY (Azul / Vinho / Roxo / Peru / Portugal)
        // ==========================================
        Item("fit_poncho", "Poncho Andino", OUTFIT, 550, "Poncho tradicional peruano colorido com franjas e motivos geométricos", "bunny"),
        Item("fit_samarra", "Samarra Alentejana Nobre", OUTFIT, 600, "Casaco de burel alentejano com gola farta de ovelha", "bunny"),
        Item("fit_traje_viana_azul", "Traje de Viana Azul", OUTFIT, 620, "Traje minhoto de festa em azul meia-noite bordado à mão", "bunny"),
        Item("fit_camisola_poveira", "Camisola Poveira Tradicional", OUTFIT, 520, "Malha branca de lã pura com símbolos marítimos azuis da Póvoa", "bunny"),
        Item("fit_andina", "Camisola Andina", OUTFIT, 475, "Camisola com padrões geométricos incas de lã de alpaca", "bunny"),
        Item("fit_overall", "Jardineiras Azuis", OUTFIT, 450, "Jardineiras de ganga azul clássicas", "bunny"),
        Item("fit_sweater", "Camisola Vinho Andina", OUTFIT, 425, "Camisola quente e acolhedora em tom vinho aveludado", "bunny"),
        Item("fit_cape", "Capa Imperial Roxa", OUTFIT, 650, "Capa nobre com forro de cetim e fecho dourado", "bunny"),
        Item("fit_fato_fado", "Fato Nobre de Lisboa", OUTFIT, 680, "Fato formal em azul escuro e colete vinho para grandes noites", "bunny"),
        Item("fit_fato_kart", "Fato Piloto Cósmico Azul", OUTFIT, 590, "Macacão aerodinâmico azul e vinho com faixas refletoras de corrida", "bunny"),
        Item("fit_scarf", "Cachecol Andino", OUTFIT, 250, "Cachecol comprido às riscas azul, vinho e roxo", "bunny"),

        // ROUPA: CAT (Rosa / Brancos / Princesa / Portugal)
        Item("cat_fit_dress", "Vestidinho Rosa Princesa", OUTFIT, 550, "Vestidinho com rendas brancas e folhos delicados", "cat"),
        Item("cat_fit_traje_viana", "Traje de Viana Rosa Minhota", OUTFIT, 620, "Traje minhoto tradicional bordado em tons rosa e ouro", "cat"),
        Item("cat_fit_sweater", "Camisola Nuvem Branca", OUTFIT, 450, "Camisola macia, quente e felpuda como uma nuvem", "cat"),
        Item("cat_fit_gala", "Vestido de Gala Rosa", OUTFIT, 680, "Vestido comprido de princesa em cetim brilhante e tule", "cat"),
        Item("cat_fit_cape", "Capa de Algodão Doce", OUTFIT, 600, "Capa rosa claro com forro felpudo e pom-pons brancos", "cat"),
        Item("cat_fit_overall", "Jardineiras Rosa Bebé", OUTFIT, 425, "Jardineiras confortáveis com bolso de coração", "cat"),
        Item("cat_fit_kart", "Fato Piloto Cósmico Rosa", OUTFIT, 590, "Macacão desportivo rosa e branco com estrelas douradas", "cat"),
        Item("cat_fit_pijama_nuvem", "Pijama Nuvem com Corações", OUTFIT, 400, "Algodão macio cor-de-rosa perfeito para dormir quentinha", "cat"),
        Item("cat_fit_floral", "Vestido Floral de Sintra", OUTFIT, 480, "Vestido primaveril com flores delicadas e laço de cetim", "cat"),
        Item("cat_fit_camisola_renda", "Camisola Renda Portuguesa", OUTFIT, 460, "Renda fina e macia inspirada na tradição de Peniche", "cat"),
        Item("cat_fit_scarf", "Cachecol Rosa e Branco", OUTFIT, 275, "Cachecol fofo às riscas rosa e branco de lã suave", "cat"),

        // ==========================================
        // CALÇADO: BUNNY (Azul / Vinho / Andino / Portugal)
        // ==========================================
        Item("shoe_andean", "Sapatilhas Andinas", SHOES, 425, "Ténis com tecido peruano artesanal colorido e sola macia", "bunny"),
        Item("shoe_botas_alentejo", "Botas Alentejanas de Couro", SHOES, 480, "Botas tradicionais artesanais de couro escuro resistentes", "bunny"),
        Item("shoe_blue", "Sapatilhas Azuis", SHOES, 300, "Ténis desportivos azuis flexíveis para correr", "bunny"),
        Item("shoe_wine", "Sapatilhas Vinho Nobre", SHOES, 325, "Sapatilhas elegantes em tom vinho aveludado", "bunny"),
        Item("shoe_boots", "Botas Altiplano", SHOES, 400, "Botas quentinhas de couro andino com sola reforçada", "bunny"),
        Item("shoe_gold", "Ténis Dourados Incas", SHOES, 600, "Sapatilhas douradas brilhantes de grande campeão", "bunny"),
        Item("shoe_sapatilhas_kart", "Ténis Racing Cósmicos", SHOES, 450, "Calçado de corrida com aderência aos pedais do TogetherKart", "bunny"),
        Item("shoe_pantufas_azul", "Pantufas Azuis de Lã", SHOES, 280, "Pantufas confortáveis para andar em casa", "bunny"),
        Item("shoe_sapatos_fado", "Sapatos de Verniz Escuro", SHOES, 470, "Sapatos formais brilhantes para cerimónias e noites de gala", "bunny"),
        Item("shoe_alpargatas", "Alpargatas Rústicas", SHOES, 310, "Calçado leve de lona com sola de corda andina", "bunny"),

        // CALÇADO: CAT (Rosa / Brancos / Princesa / Portugal)
        Item("cat_shoe_pink", "Sabrinas Rosa Bebé", SHOES, 300, "Sabrinas fofas com lacinho de cetim", "cat"),
        Item("cat_shoe_botas_viana", "Botinhas Bordadas de Viana", SHOES, 480, "Botinhas de camurça rosa com motivos florais bordados", "cat"),
        Item("cat_shoe_boots", "Botinhas de Lã Branca", SHOES, 375, "Botinhas fofas de lã quentinha forradas a veludo", "cat"),
        Item("cat_shoe_slippers", "Pantufas Nuvem Fofa", SHOES, 350, "Pantufas brancas macias como caminhar sobre nuvens", "cat"),
        Item("cat_shoe_gold", "Sabrinas Douradas de Princesa", SHOES, 550, "Sabrinas reluzentes com fivela dourada", "cat"),
        Item("cat_shoe_sabrinas_cetim", "Sabrinas de Cetim Rosa", SHOES, 420, "Sabrinas suaves de bailarina com fita no tornozelo", "cat"),
        Item("cat_shoe_racing", "Ténis Racing Rosa Pastel", SHOES, 450, "Sapatilhas aerodinâmicas para acelerar no TogetherKart", "cat"),
        Item("cat_shoe_botas_neve", "Botas de Neve Brancas", SHOES, 460, "Botas altas impermeáveis forradas com pele sintética fofa", "cat"),
        Item("cat_shoe_pantufas_ursinho", "Pantufas Ursinho Rosa", SHOES, 320, "Pantufas doces com cara de ursinho cor-de-rosa", "cat"),
        Item("cat_shoe_sandalias_sol", "Sandálias Brancas de Verão", SHOES, 340, "Sandálias abertas com pequenas pérolas decorativas", "cat"),

        // ==========================================
        // CORES DE PELO: BUNNY (Azuis, Vinho, Roxo - ZERO Rosa/Branco)
        // ==========================================
        Item("col_sky", "Azul Celestial", COLOR, 0, "Pelo azul suave e celestial", "bunny"),
        Item("col_royal", "Azul Real Nobre", COLOR, 375, "Pelo azul profundo e majestoso", "bunny"),
        Item("col_wine", "Vinho Andino", COLOR, 400, "Pelo vinho aveludado e acolhedor", "bunny"),
        Item("col_purple", "Roxo Imperial", COLOR, 425, "Pelo roxo nobre do império inca", "bunny"),
        Item("col_caramel", "Caramelo Andino", COLOR, 350, "Pelo caramelo escuro das montanhas andinas", "bunny"),
        Item("col_navy_night", "Azul Noite Profundo", COLOR, 450, "Pelo azul-escuro como o céu noturno do Alentejo", "bunny"),

        // CORES DE PELO: CAT (Rosa, Brancos - ZERO Azul/Vinho/Roxo)
        Item("cat_col_white", "Branco Algodão Doce", COLOR, 0, "Pelo branco puro fofinho e imaculado", "cat"),
        Item("cat_col_pink", "Rosa Pastel Macio", COLOR, 425, "Pelo cor-de-rosa doce e carinhoso", "cat"),
        Item("cat_col_cream", "Creme Baunilha", COLOR, 350, "Pelo creme suave e aveludado", "cat"),
        Item("cat_col_silver", "Prateado Suave", COLOR, 375, "Pelo prateado clarinho brilhante", "cat"),
        Item("cat_col_rosinha_bebe", "Rosa Bebé Princesa", COLOR, 450, "Pelo rosa suave com reflexos perolados", "cat"),

        // ==========================================
        // ACESSÓRIOS: BUNNY (Azul / Vinho / Peru / Portugal)
        // ==========================================
        Item("acc_glasses", "Óculos Azuis Intelectuais", ACC, 300, "Óculos elegantes com armação azul", "bunny"),
        Item("acc_bow_wine", "Laço Vinho Nobre", ACC, 275, "Laço formal cor de vinho para usar ao peito", "bunny"),
        Item("acc_chullo_scarf", "Lenço Andino Colorido", ACC, 325, "Lenço artesanal tradicional tecido à mão", "bunny"),
        Item("acc_relogio_bolso", "Relógio de Bolso Dourado", ACC, 450, "Relógio clássico de corrente com detalhes incas", "bunny"),
        Item("acc_guitarra_miniatura", "Mini Guitarra Portuguesa", ACC, 480, "Pequena guitarra de Fado colocada às costas", "bunny"),
        Item("acc_medalhao_inca", "Medalhão do Sol Inca", ACC, 500, "Medalhão sagrado em ouro polido dos Andes", "bunny"),

        // ACESSÓRIOS: CAT (Rosa / Brancos / Princesa / Portugal)
        Item("cat_acc_pearls", "Colar de Pérolas Brancas", ACC, 400, "Colar de pérolas brilhantes de princesa", "cat"),
        Item("cat_acc_bow", "Lacinho Rosa no Peito", ACC, 250, "Laço de cetim rosa doce com brilhante", "cat"),
        Item("cat_acc_glasses", "Óculos Coração Rosa", ACC, 300, "Armação em formato de coração rosa fofinha", "cat"),
        Item("cat_acc_coracao_viana", "Coração de Viana em Ouro", ACC, 520, "Filigrana portuguesa autêntica em ouro puro", "cat"),
        Item("cat_acc_pulseira_flores", "Pulseira Floral Rosa", ACC, 350, "Flores delicadas entrelaçadas em fita de cetim", "cat"),
        Item("cat_acc_brincos_perola", "Brincos de Pérola Branca", ACC, 380, "Pérolas cintilantes muito elegantes", "cat"),

        // ==========================================
        // PAREDES: BUNNY (Inca, Portugal, Casa de Banho Azul)
        // ==========================================
        Item("wall_inca", "Muro Inca de Cusco", WALL, 0, "Pedra esculpida de Cusco com símbolos Incas históricos", "bunny"),
        Item("wall_azulejos_pt", "Azulejos Tradicionais de Lisboa", WALL, 480, "Padrão histórico azul e branco de azulejos portugueses pintados", "bunny"),
        Item("wall_navy", "Azul Meia-Noite Nobre", WALL, 350, "Azul escuro tranquilo, acolhedor e moderno", "bunny"),
        Item("wall_purple", "Roxo Imperial", WALL, 400, "Parede em tom roxo nobre do império andino", "bunny"),
        Item("wall_aguayo", "Tecido Aguayo dos Andes", WALL, 500, "Cores vibrantes tradicionais peruanas tecidas à mão", "bunny"),
        Item("wall_stars", "Céu Estrelado dos Andes", WALL, 600, "Céu azul escuro salpicado de estrelas douradas", "bunny"),
        Item("wall_marmore_wc_azul", "Mármore Azul de Casa de Banho", WALL, 450, "Lajes impermeáveis de mármore azul real para o WC", "bunny"),
        Item("wall_granito_porto", "Granito da Ribeira do Porto", WALL, 420, "Pedra rústica e sólida do norte de Portugal", "bunny"),
        Item("wall_madeira_alentejo", "Madeira de Sobreiro Alentejano", WALL, 380, "Painéis aconchegantes de madeira portuguesa", "bunny"),
        Item("wall_pedra_machupicchu", "Pedra Polida de Machu Picchu", WALL, 460, "Muralhas talhadas com precisão milimétrica inca", "bunny"),

        // PAREDES: CAT (Rosa, Nuvens, Corações, Casa de Banho Rosa)
        Item("cat_wall_pink", "Rosa Bebé Suave", WALL, 0, "Rosa acolhedor e doce como um abraço", "cat"),
        Item("cat_wall_azulejos_rosa", "Azulejos Lisboa Rosa e Branco", WALL, 480, "Painel de azulejos portugueses em tons rosa pastel", "cat"),
        Item("cat_wall_cloud", "Nuvens Brancas e Céu Rosa", WALL, 400, "Céu rosa claro salpicado de nuvens fofas", "cat"),
        Item("cat_wall_hearts", "Corações Amorosos", WALL, 450, "Parede cheia de corações cor-de-rosa e brancos", "cat"),
        Item("cat_wall_stars", "Estrelas Brilhantes", WALL, 500, "Estrelinhas brancas suaves a cintilar na parede", "cat"),
        Item("cat_wall_stripes", "Riscas Rosa e Brancas", WALL, 475, "Padrão às riscas verticais elegante e clássico", "cat"),
        Item("cat_wall_marmore_wc_rosa", "Mármore Rosa de Casa de Banho", WALL, 450, "Mármore polido brilhante e reluzente para o WC de princesa", "cat"),
        Item("cat_wall_azulejo_wc_branco", "Azulejo WC Branco Puro", WALL, 430, "Azulejo cerâmico vidrado fácil de limpar para o duche", "cat"),
        Item("cat_wall_renda_sintra", "Papel de Renda de Sintra", WALL, 440, "Motivos florais finos inspirados nos palácios de Sintra", "cat"),
        Item("cat_wall_nuvens_douradas", "Céu Rosa com Nuvens Douradas", WALL, 520, "Cenário de conto de fadas celestial acolhedor", "cat"),

        // ==========================================
        // CHÃO: BUNNY (Calçada Portuguesa, Inca, Mármore WC)
        // ==========================================
        Item("floor_inca", "Calçada Inca", FLOOR, 0, "Pedra talhada histórica dos Andes resistente e firme", "bunny"),
        Item("floor_calcada_portuguesa", "Calçada Portuguesa de Lisboa", FLOOR, 480, "Mosaico calcário preto e branco tradicional em ondas do mar", "bunny"),
        Item("floor_peru", "Madeira Andina Nobre", FLOOR, 375, "Soalho de madeira escura com borda artesanal inca", "bunny"),
        Item("floor_wood", "Madeira Maciça de Pinho", FLOOR, 300, "Soalho de tábua corrida aconchegante", "bunny"),
        Item("floor_grass", "Relva Fresca dos Andes", FLOOR, 400, "Verde natural fresco e macio como um jardim", "bunny"),
        Item("floor_marmore_wc_azul", "Mármore Branco de WC", FLOOR, 440, "Mosaico de mármore branco brilhante ideal para a casa de banho", "bunny"),
        Item("floor_tabua_corrida", "Tábua Corrida Portuguesa", FLOOR, 360, "Madeira encerada tradicional com acabamento sedoso", "bunny"),
        Item("floor_granito", "Lajes de Granito do Norte", FLOOR, 390, "Pedra polida fresca e muito durável", "bunny"),
        Item("floor_tapete_arraiolos", "Tapete de Arraiolos Azul", FLOOR, 520, "Tapeçaria histórica alentejana bordada com motivos nobres", "bunny"),

        // CHÃO: CAT (Nuvens, Calçada Rosa, Mármore WC Rosa)
        Item("cat_floor_cloud", "Chão Nuvens Fofas", FLOOR, 0, "Chão suave como caminhar nas nuvens do céu", "cat"),
        Item("cat_floor_calcada_rosa", "Calçada Portuguesa Rosa & Branca", FLOOR, 480, "Padrão geométrico suave de calçada em tons rosa e branco", "cat"),
        Item("cat_floor_pink", "Soalho Rosa Pastel", FLOOR, 350, "Madeira encerada em tom rosa claro aconchegante", "cat"),
        Item("cat_floor_white", "Madeira Clara Suave", FLOOR, 325, "Soalho branco macio que ilumina toda a sala", "cat"),
        Item("cat_floor_check", "Xadrez Rosa & Branco", FLOOR, 375, "Mosaico xadrez fofinho de princesa", "cat"),
        Item("cat_floor_marmore_wc_rosa", "Mármore Rosa de Casa de Banho", FLOOR, 440, "Piso de mármore polido rosa suave à prova de salpicos de água", "cat"),
        Item("cat_floor_tapete_felpudo", "Tapete Felpudo Branco Puro", FLOOR, 420, "Algodão espesso super macio para pisar descalço", "cat"),
        Item("cat_floor_madeira_clara", "Parquet Branco Escandinavo", FLOOR, 340, "Madeira nobre envernizada luminosa e fresca", "cat"),
        Item("cat_floor_petalas", "Mosaico Pétalas de Rosa", FLOOR, 460, "Mosaico cerâmico com pequenas pétalas de flor rosa", "cat"),

        // ==========================================
        // DECORAÇÃO & CASA DE BANHO & CULTURA (25+ Itens)
        // ==========================================
        // Casa de Banho (WC) completa para ambos
        Item("dec_banheira", "Banheira com Patinho", DECOR, 550, "Banheira de porcelana branca com torneira dourada e patinho de borracha amarelo", "both"),
        Item("dec_sanita", "Sanita Moderna de Porcelana", DECOR, 420, "Sanita elegante de porcelana brilhante com autoclismo embutido", "both"),
        Item("dec_lavatorio", "Lavatório em Mármore", DECOR, 480, "Móvel de lavatório com cuba em mármore, saboneteira e toalha", "both"),
        Item("dec_toalheiro", "Toalheiro Aquecido", DECOR, 360, "Toalheiro de parede com toalhas felpudas macias e quentes", "both"),
        Item("dec_espelho_wc", "Espelho Iluminado de WC", DECOR, 390, "Espelho redondo com iluminação LED suave para a casa de banho", "both"),

        // Cultura Portuguesa
        Item("dec_galo_barcelos", "Galo de Barcelos Português", DECOR, 480, "Símbolo icónico de Portugal pintado à mão com corações vermelhos", "bunny"),
        Item("dec_guitarra_fado", "Guitarra Portuguesa de Fado", DECOR, 540, "Instrumento nobre de 12 cordas em madeira trabalhada", "bunny"),
        Item("dec_azulejo_lisboa", "Painel de Azulejo de Lisboa", DECOR, 460, "Quadro com caravela e motivos marítimos portugueses", "bunny"),
        Item("dec_andorinhas", "Andorinhas Bordalo Pinheiro", DECOR, 410, "Três andorinhas pretas de cerâmica para a parede", "bunny"),

        // Cultura Peruana (Andes & Machu Picchu)
        Item("dec_machupicchu", "Quadro Machu Picchu", DECOR, 425, "Quadro ilustrado da cidadela sagrada nos picos andinos", "bunny"),
        Item("dec_tapete_peruano", "Tapete Geométrico Andino", DECOR, 375, "Tapete peruano tecido à mão com padrões geométricos incas", "bunny"),
        Item("dec_lhama", "Peluche Lhama com Borlas", DECOR, 475, "Lhama andina super fofa com borlas coloridas nas orelhas", "bunny"),
        Item("dec_zampogna", "Flauta de Pã Andina", DECOR, 350, "Flauta musical de bambu tradicional dos Andes", "bunny"),
        Item("dec_sol_inti", "Sol Inca Inti em Ouro", DECOR, 520, "Escultura de parede do sagrado deus Sol esculpida em ouro", "bunny"),

        // Casa de Banho e Quarto Princesa da Cat
        Item("cat_dec_banheira_rosa", "Banheira Rosa Vitoriana", DECOR, 560, "Banheira clássica rosa com pés dourados e espuma perfumada", "cat"),
        Item("cat_dec_lavatorio_rosa", "Toucador e Lavatório Rosa", DECOR, 490, "Móvel toucador com espelho oval e frascos de perfume rosa", "cat"),
        Item("cat_dec_toalheiro_rosa", "Toalheiro Rosa com Lacinho", DECOR, 370, "Toalheiro com toalhas rosa bordadas e sabonetes de lavanda", "cat"),
        Item("cat_dec_plush", "Peluche Ursinho Rosa", DECOR, 425, "Ursinho cor-de-rosa super fofo com lacinho branco", "cat"),
        Item("cat_dec_rug", "Tapete Coração Rosa", DECOR, 350, "Tapete macio em formato de coração", "cat"),
        Item("cat_dec_lamp", "Candeeiro Nuvem Quente", DECOR, 375, "Luz de presença acolhedora em forma de nuvem", "cat"),
        Item("cat_dec_mirror", "Espelho Princesa com Pérolas", DECOR, 450, "Espelho oval com moldura de pérolas e laço rosa", "cat"),
        Item("cat_dec_andorinhas_rosa", "Andorinhas Rosa em Cerâmica", DECOR, 410, "Andorinhas decorativas em cerâmica rosa para a parede", "cat"),
        Item("cat_dec_jarra_flores", "Jarra de Flores de Sintra", DECOR, 380, "Flores campestres frescas perfumadas numa jarra de porcelana", "cat"),

        // Itens Universais
        Item("dec_window", "Janela Panorâmica", DECOR, 400, "Janela com vista para as montanhas e céu estrelado à noite", "both"),
        Item("dec_rug", "Tapete Circular Acolhedor", DECOR, 340, "Tapete suave para colocar no meio da sala", "both"),
        Item("dec_plant", "Vaso com Planta Verde", DECOR, 320, "Planta natural viçosa num vaso de barro", "both"),
        Item("dec_lamp", "Candeeiro de Pé Alto", DECOR, 360, "Candeeiro com abajur que emite luz quente e suave", "both"),
        Item("dec_plush", "Peluche Fofinho", DECOR, 380, "Amiguinho de peluche doce para fazer companhia no quarto", "both")
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
    var bunnyFavColor = "Azul Celestial 🌊"
    var bunnyFavFood = "Ceviche Peruano 🐟"
    var bunnyPersonality = "Doce & Carinhoso 🥰"
    var bunnySeason = "Primavera Florida 🌸"
    var bunnyHobby = "Corridas no TogetherKart 🏎️"
    var bunnyPower = "Abraço Mágico que Cura Tudo 🫂"
    var bunnyMessage = "Amo-te daqui até à lua! ♥"

    // Ficha / Relatório da Cat (Gatinha)
    var reportDoneCat = false
    var catName = "Minha Gatinha"
    var catLikes = "Ronronar e receber carinhos fofos"
    var catDislikes = "Frio e banhos frios"
    var catFavColor = "Rosa Algodão Doce 🌸"
    var catFavFood = "Alfajores com Doce de Leite 🍪"
    var catPersonality = "Doce & Carinhosa 🥰"
    var catSeason = "Primavera Florida 🌸"
    var catHobby = "Corridas no TogetherKart 🏎️"
    var catPower = "Abraço Mágico que Cura Tudo 🫂"
    var catMessage = "És o amor da minha vida! ♥"

    // Conexão a Dois / Multiplayer à distância
    var partnerConnected = false
    var partnerChar = "cat"
    var partnerName = "Minha Gatinha"
    var partnerLvl = 1
    var partnerLikes = "Estar juntos e rir"
    var partnerDislikes = "Distância"
    var partnerFavColor = "Rosa Algodão Doce 🌸"
    var partnerFavFood = "Alfajores com Doce de Leite 🍪"
    var partnerPersonality = "Doce & Carinhosa 🥰"
    var partnerSeason = "Primavera Florida 🌸"
    var partnerHobby = "Corridas no TogetherKart 🏎️"
    var partnerPower = "Abraço Mágico que Cura Tudo 🫂"
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
    val bunnyWall = arrayOf("wall_inca", "wall_inca", "wall_inca", "wall_marmore_wc_azul")
    val bunnyFloor = arrayOf("floor_inca", "floor_inca", "floor_inca", "floor_marmore_wc_azul")
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
    val catWall = arrayOf("cat_wall_pink", "cat_wall_pink", "cat_wall_pink", "cat_wall_marmore_wc_rosa")
    val catFloor = arrayOf("cat_floor_cloud", "cat_floor_cloud", "cat_floor_cloud", "cat_floor_marmore_wc_rosa")
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

    // Propriedades Dinâmicas Expandidas da Ficha
    val activePersonality: String
        get() = if (activeChar == "bunny") bunnyPersonality else catPersonality

    val activeSeason: String
        get() {
            val s = if (activeChar == "bunny") bunnySeason else catSeason
            return when {
                s.contains("Primavera", ignoreCase = true) -> "Primavera"
                s.contains("Verão", ignoreCase = true) || s.contains("Verao", ignoreCase = true) -> "Verão"
                s.contains("Outono", ignoreCase = true) -> "Outono"
                s.contains("Inverno", ignoreCase = true) -> "Inverno"
                else -> "Primavera"
            }
        }

    val activeHobby: String
        get() = if (activeChar == "bunny") bunnyHobby else catHobby

    val activePower: String
        get() = if (activeChar == "bunny") bunnyPower else catPower

    val activeFavFood: String
        get() = if (activeChar == "bunny") bunnyFavFood else catFavFood

    val activeFavColor: String
        get() = if (activeChar == "bunny") bunnyFavColor else catFavColor

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
            json.put("personality", if (activeChar == "bunny") bunnyPersonality else catPersonality)
            json.put("season", if (activeChar == "bunny") bunnySeason else catSeason)
            json.put("hobby", if (activeChar == "bunny") bunnyHobby else catHobby)
            json.put("power", if (activeChar == "bunny") bunnyPower else catPower)
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
            partnerPersonality = json.optString("personality", "Doce & Carinhoso(a) 🥰")
            partnerSeason = json.optString("season", "Primavera Florida 🌸")
            partnerHobby = json.optString("hobby", "Corridas no TogetherKart 🏎️")
            partnerPower = json.optString("power", "Abraço Mágico que Cura Tudo 🫂")
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
                json.put("personality", if (activeChar == "bunny") bunnyPersonality else catPersonality)
                json.put("season", if (activeChar == "bunny") bunnySeason else catSeason)
                json.put("hobby", if (activeChar == "bunny") bunnyHobby else catHobby)
                json.put("power", if (activeChar == "bunny") bunnyPower else catPower)
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
        bunnyFavColor = prefs.getString("bunnyFavColor", "Azul Celestial 🌊") ?: "Azul Celestial 🌊"
        bunnyFavFood = prefs.getString("bunnyFavFood", "Ceviche Peruano 🐟") ?: "Ceviche Peruano 🐟"
        bunnyPersonality = prefs.getString("bunnyPersonality", "Doce & Carinhoso 🥰") ?: "Doce & Carinhoso 🥰"
        bunnySeason = prefs.getString("bunnySeason", "Primavera Florida 🌸") ?: "Primavera Florida 🌸"
        bunnyHobby = prefs.getString("bunnyHobby", "Corridas no TogetherKart 🏎️") ?: "Corridas no TogetherKart 🏎️"
        bunnyPower = prefs.getString("bunnyPower", "Abraço Mágico que Cura Tudo 🫂") ?: "Abraço Mágico que Cura Tudo 🫂"
        bunnyMessage = prefs.getString("bunnyMessage", "Amo-te daqui até à lua! ♥") ?: "Amo-te daqui até à lua! ♥"

        // Ficha Cat
        reportDoneCat = prefs.getBoolean("reportDoneCat", false)
        catName = prefs.getString("catName", "Minha Gatinha") ?: "Minha Gatinha"
        catLikes = prefs.getString("catLikes", "Ronronar e receber carinhos fofos") ?: "Ronronar e receber carinhos fofos"
        catDislikes = prefs.getString("catDislikes", "Frio e banhos frios") ?: "Frio e banhos frios"
        catFavColor = prefs.getString("catFavColor", "Rosa Algodão Doce 🌸") ?: "Rosa Algodão Doce 🌸"
        catFavFood = prefs.getString("catFavFood", "Alfajores com Doce de Leite 🍪") ?: "Alfajores com Doce de Leite 🍪"
        catPersonality = prefs.getString("catPersonality", "Doce & Carinhosa 🥰") ?: "Doce & Carinhosa 🥰"
        catSeason = prefs.getString("catSeason", "Primavera Florida 🌸") ?: "Primavera Florida 🌸"
        catHobby = prefs.getString("catHobby", "Corridas no TogetherKart 🏎️") ?: "Corridas no TogetherKart 🏎️"
        catPower = prefs.getString("catPower", "Abraço Mágico que Cura Tudo 🫂") ?: "Abraço Mágico que Cura Tudo 🫂"
        catMessage = prefs.getString("catMessage", "És o amor da minha vida! ♥") ?: "És o amor da minha vida! ♥"

        // Conexão / Parceiro
        partnerConnected = prefs.getBoolean("partnerConnected", false)
        partnerChar = prefs.getString("partnerChar", if (activeChar == "bunny") "cat" else "bunny") ?: "cat"
        partnerName = prefs.getString("partnerName", "Amor") ?: "Amor"
        partnerLvl = prefs.getInt("partnerLvl", 1)
        partnerLikes = prefs.getString("partnerLikes", "") ?: ""
        partnerDislikes = prefs.getString("partnerDislikes", "") ?: ""
        partnerFavColor = prefs.getString("partnerFavColor", "Rosa Algodão Doce 🌸") ?: "Rosa Algodão Doce 🌸"
        partnerFavFood = prefs.getString("partnerFavFood", "Alfajores com Doce de Leite 🍪") ?: "Alfajores com Doce de Leite 🍪"
        partnerPersonality = prefs.getString("partnerPersonality", "Doce & Carinhoso(a) 🥰") ?: "Doce & Carinhoso(a) 🥰"
        partnerSeason = prefs.getString("partnerSeason", "Primavera Florida 🌸") ?: "Primavera Florida 🌸"
        partnerHobby = prefs.getString("partnerHobby", "Corridas no TogetherKart 🏎️") ?: "Corridas no TogetherKart 🏎️"
        partnerPower = prefs.getString("partnerPower", "Abraço Mágico que Cura Tudo 🫂") ?: "Abraço Mágico que Cura Tudo 🫂"
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
            bunnyOwned.addAll(listOf("wall_inca", "floor_inca", "wall_marmore_wc_azul", "floor_marmore_wc_azul", "col_sky", "food_carrot", "food_ceviche"))
        }
        bunnyDecor.clear()
        bunnyDecor.addAll(prefs.getStringSet("bunny_decor", emptySet()) ?: emptySet())
        for (i in 0 until 4) {
            val defWall = if (i == 3) "wall_marmore_wc_azul" else "wall_inca"
            val defFloor = if (i == 3) "floor_marmore_wc_azul" else "floor_inca"
            bunnyWall[i] = prefs.getString("bunny_wall$i", defWall) ?: defWall
            bunnyFloor[i] = prefs.getString("bunny_floor$i", defFloor) ?: defFloor
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
            catOwned.addAll(listOf("cat_wall_pink", "cat_floor_cloud", "cat_wall_marmore_wc_rosa", "cat_floor_marmore_wc_rosa", "cat_col_white", "food_alfajor", "food_cupcake"))
        }
        catDecor.clear()
        catDecor.addAll(prefs.getStringSet("cat_decor", emptySet()) ?: emptySet())
        for (i in 0 until 4) {
            val defWall = if (i == 3) "cat_wall_marmore_wc_rosa" else "cat_wall_pink"
            val defFloor = if (i == 3) "cat_floor_marmore_wc_rosa" else "cat_floor_cloud"
            catWall[i] = prefs.getString("cat_wall$i", defWall) ?: defWall
            catFloor[i] = prefs.getString("cat_floor$i", defFloor) ?: defFloor
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
            .putString("bunnyPersonality", bunnyPersonality)
            .putString("bunnySeason", bunnySeason)
            .putString("bunnyHobby", bunnyHobby)
            .putString("bunnyPower", bunnyPower)
            .putString("bunnyMessage", bunnyMessage)
            .putBoolean("reportDoneCat", reportDoneCat)
            .putString("catName", catName)
            .putString("catLikes", catLikes)
            .putString("catDislikes", catDislikes)
            .putString("catFavColor", catFavColor)
            .putString("catFavFood", catFavFood)
            .putString("catPersonality", catPersonality)
            .putString("catSeason", catSeason)
            .putString("catHobby", catHobby)
            .putString("catPower", catPower)
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
            .putString("partnerPersonality", partnerPersonality)
            .putString("partnerSeason", partnerSeason)
            .putString("partnerHobby", partnerHobby)
            .putString("partnerPower", partnerPower)
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
            .putString("bunny_wall0", bunnyWall[0]).putString("bunny_wall1", bunnyWall[1]).putString("bunny_wall2", bunnyWall[2]).putString("bunny_wall3", bunnyWall[3])
            .putString("bunny_floor0", bunnyFloor[0]).putString("bunny_floor1", bunnyFloor[1]).putString("bunny_floor2", bunnyFloor[2]).putString("bunny_floor3", bunnyFloor[3])
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
            .putString("cat_wall0", catWall[0]).putString("cat_wall1", catWall[1]).putString("cat_wall2", catWall[2]).putString("cat_wall3", catWall[3])
            .putString("cat_floor0", catFloor[0]).putString("cat_floor1", catFloor[1]).putString("cat_floor2", catFloor[2]).putString("cat_floor3", catFloor[3])
            // CatBunnyCraft
            .putString("craftBlocks", craftBlocksToJson())
            .putLong("last", System.currentTimeMillis())
            .apply()
    }

    fun tick(dt: Float) {
        // Dinâmica baseada na Personalidade da Ficha do Bunny
        val bSleepRate = if (bunnyPersonality.contains("Soneca")) (100f / 80f) else (100f / 120f)
        val bJoyDecay = if (bunnyPersonality.contains("Doce") || bunnyPersonality.contains("Romântic")) 0.011f else 0.017f
        val bHungerDecay = if (bunnyPersonality.contains("Protetor")) 0.015f else 0.020f
        val bHygieneDecay = if (bunnyPersonality.contains("Protetor")) 0.010f else 0.013f

        // Atualiza Bunny
        if (bunnySleeping) {
            bunnyEnergy = (bunnyEnergy + bSleepRate * dt).coerceIn(0f, 100f)
            bunnyHunger = (bunnyHunger - 0.005f * dt).coerceIn(0f, 100f)
            bunnyHygiene = (bunnyHygiene - 0.003f * dt).coerceIn(0f, 100f)
            if (bunnyEnergy >= 100f) bunnySleeping = false
        } else {
            bunnyHunger = (bunnyHunger - bHungerDecay * dt).coerceIn(0f, 100f)
            bunnyHygiene = (bunnyHygiene - bHygieneDecay * dt).coerceIn(0f, 100f)
            bunnyJoy = (bunnyJoy - bJoyDecay * dt).coerceIn(0f, 100f)
            bunnyEnergy = (bunnyEnergy - 0.010f * dt).coerceIn(0f, 100f)
        }

        // Dinâmica baseada na Personalidade da Ficha da Cat
        val cSleepRate = if (catPersonality.contains("Soneca")) (100f / 80f) else (100f / 120f)
        val cJoyDecay = if (catPersonality.contains("Doce") || catPersonality.contains("Romântic")) 0.011f else 0.017f
        val cHungerDecay = if (catPersonality.contains("Protetor")) 0.015f else 0.020f
        val cHygieneDecay = if (catPersonality.contains("Protetor")) 0.010f else 0.013f

        // Atualiza Cat
        if (catSleeping) {
            catEnergy = (catEnergy + cSleepRate * dt).coerceIn(0f, 100f)
            catHunger = (catHunger - 0.005f * dt).coerceIn(0f, 100f)
            catHygiene = (catHygiene - 0.003f * dt).coerceIn(0f, 100f)
            if (catEnergy >= 100f) catSleeping = false
        } else {
            catHunger = (catHunger - cHungerDecay * dt).coerceIn(0f, 100f)
            catHygiene = (catHygiene - cHygieneDecay * dt).coerceIn(0f, 100f)
            catJoy = (catJoy - cJoyDecay * dt).coerceIn(0f, 100f)
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
