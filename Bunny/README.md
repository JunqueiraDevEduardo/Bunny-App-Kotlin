# TOGETHER 🐰💖🐱

Um jogo e bichinho virtual multiplayer para casais e companheiros, inspirado no estilo e carinho do **Pou**, **Minecraft** e na cultura andina do **Peru**. Desenvolvido em Kotlin puro para Android com renderização nativa de alto desempenho via Canvas 2D e motor 3D procedural.

---

## 🌟 Identidade do Jogo: TOGETHER

O jogo evoluiu para uma experiência partilhada entre dois companheiros:
- **🐰 O Coelho (Bunny)**: Mundo em tons de **azul meia-noite, vinho profundo e roxo estelar** (sem rosa nem branco).
- **🐱 A Gatinha (Cat)**: Mundo em tons de **rosa pastel e branco puro** (sem azul, vinho ou roxo).
- **💖 CatBunnyCraft**: O mundo 3D conjunto em **Machu Picchu**, jogado em modo panorâmico (Landscape), 100% plano, com inventário gigante de mais de 80 blocos e suporte a comando PS4.

---

## 🚀 Como Abrir e Executar

1. Abre no **Android Studio**: `File > Open > pasta do projeto`.
2. O Gradle está configurado com `rootProject.name = "Together"`.
3. Garante que o Gradle JDK está configurado como **Java 17 ou 21** (`Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`).
4. Clica em **Sync Project with Gradle Files** e depois em **Run ▶** (ou compila `./gradlew assembleDebug`).

---

## 🔑 Funcionalidades Principais

### 1. Autenticação e Entrada Misteriosa
- Ecrã de login limpo e sem sugestões reveladoras: apenas **Nome de Utilizador** e **Palavra-passe (PIN)**.
  - Credenciais Bunny: `meu coelhinho` / PIN `150225`
  - Credenciais Gatinha: `minha gatinha` / PIN `150225`
- Acesso misterioso com botão de configurações de idioma (Português / Espanhol) e definições antes do login.
- Ficha de perfil inicial personalizada com nome, o que gosta, o que não gosta e cor favorita.

### 2. Sons Procedurais Dedicados (SoundFx)
Motor de áudio em tempo real sintetizado via `AudioTrack` PCM (sem necessidade de ficheiros de áudio pesados):
- **Comer**: Mordidinhas suaves "nom nom" para o Bunny; ronronar e lambidelas doces para a Gatinha.
- **Banho**: Borbulhas de sabão e água para o Bunny; salpicos suaves e miados para a Gatinha.
- **Brincar**: Saltos elásticos "boing" para o Bunny; miados carinhosos para a Gatinha.
- **Beijinhos e Carinho**: Sons adoráveis de beijo e amor entre ambos.

### 3. CatBunnyCraft 3D em Machu Picchu
- **Orientação Landscape (Horizontal)**: Ecrã panorâmico automático ao entrar no mundo 3D.
- **Mundo 100% Plano (Superflat)**: Vista limpa e desobstruída das montanhas sagradas ao fundo com relva plana.
- **Casas dos Dois Pets**: A casa acolhedora do Bunny e a casa da Gatinha lado a lado, com o baú partilhado.
- **Inventário Expandido (+84 Itens)**: 6 abas completas com blocos de construção, madeiras, minérios e gemas preciosas, natureza e flora peruana, decoração e mobília, e ferramentas completas estilo Minecraft.
- **Suporte a Comando PS4 / Gamepad**: Joysticks analógicos, gatilhos de minar (R2/RT), colocar bloco (L2/LT), salto (X/A) e inventário (Triângulo/Y).
- **Multiplayer P2P Local e Wi-Fi**: Sincronização em tempo real das posições, blocos construídos, quebras e interação de abraços.

### 4. Sistema Pou & Níveis (0 a 100)
- Níveis baseados em tempo real (1 dia = 1 nível).
- Crescimento gradual do pet desde pequenino até adulto.
- Sono melhorado de 2 minutos que continua a contar mesmo com a aplicação fechada.
- Chuveiro centralizado no banho.
- Loja com comida tradicional peruana (Ceviche, Lomo Saltado, Chicha Morada, Alfajores, Choclo, Causa Limeña), roupas típicas andinas, sapatilhas e acessórios.
