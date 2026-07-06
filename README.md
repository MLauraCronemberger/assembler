# ⚙️ Assembler Hack (Nand2Tetris)

Este projeto implementa o **montador (assembler) completo do Project 06** do curso **Nand2Tetris**.

Desenvolvido em **Java**, o sistema recebe um arquivo `.asm` (Assembly Hack) e o traduz para **código de máquina binário** (`.hack`), executável no CPU Emulator oficial do curso ou na plataforma Hack real.

A implementação suporta:

* A-instructions (`@valor` / `@simbolo`);
* C-instructions (`dest=comp;jump`), incluindo todos os campos `comp` com `a=0` e `a=1`;
* Labels (`(LOOP)`, `(END)`), resolvidos em uma primeira passagem;
* Variáveis não predefinidas, alocadas a partir de `RAM[16]`;
* Símbolos predefinidos (`R0`–`R15`, `SP`, `LCL`, `ARG`, `THIS`, `THAT`, `SCREEN`, `KBD`);
* Tradução em duas passagens (resolução de símbolos → geração de código).

---

## 📁 Estrutura do Projeto

```text
assembler/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── br/com/
│   │           ├── Assembler.java             🔹 Ponto de entrada — orquestra as duas passagens
│   │           ├── parser/
│   │           │   └── Parser.java            🔹 Lê o .asm, filtra e classifica cada instrução
│   │           ├── code/
│   │           │   └── Code.java              🔹 Traduz dest/comp/jump para binário
│   │           └── symboltable/
│   │               └── SymbolTable.java       🔹 Mapeia símbolos (labels e variáveis) para endereços
│   │
│   └── test/
│       └── resources/
│           └── 6/                             🔹 Arquivos de teste do Project 06
│               ├── add/    → Add.asm
│               ├── max/    → Max.asm, MaxL.asm
│               ├── rect/   → Rect.asm, RectL.asm
│               └── pong/   → Pong.asm, PongL.asm
│
├── pom.xml                                    🔹 Configuração Maven (dependências, build, plugins)
├── README.md
└── .gitignore
```

---

## 🔍 Como o montador funciona

O arquivo `.asm` passa por duas passagens antes de virar código de máquina:

```text
Arquivo .asm
     │
     ▼
┌─────────────────────────────────────────────┐
│  1ª PASSAGEM — Coleta de labels             │
│                                             │
│  Percorre o arquivo com o Parser.           │
│  Ignora comentários e linhas vazias.        │
│  Toda vez que encontra um LABEL...          │
│                                             │
│  "(LOOP)" na linha de instrução N           │
│    → SymbolTable.addEntry("LOOP", N)        │
│                                             │
│  Instruções A e C apenas incrementam o      │
│  contador de endereço da ROM.               │
└─────────────────┬───────────────────────────┘
                  │ tabela de labels pronta
                  ▼
┌─────────────────────────────────────────────┐
│  2ª PASSAGEM — Geração de código            │
│                                             │
│  Percorre o arquivo novamente (reset()).    │
│                                             │
│  A_INSTRUCTION "@valor" ou "@simbolo"       │
│    → se for número, usa direto              │
│    → se for símbolo novo, SymbolTable       │
│      aloca a partir de RAM[16]              │
│    → escreve "0" + 15 bits do endereço      │
│                                             │
│  C_INSTRUCTION "dest=comp;jump"             │
│    → Code.comp() + Code.dest() + Code.jump()│
│    → escreve "111" + 7 + 3 + 3 bits         │
│                                             │
│  LABEL → não gera código, apenas é pulado   │
└────────────────┬────────────────────────────┘
                 │
                 ▼
        Arquivo .hack gerado
   (compatível com CPU Emulator)
```

### O que cada classe faz

* **Parser:** lê o arquivo `.asm`, remove comentários (`//`) e linhas em branco, classifica cada instrução (`A_INSTRUCTION`, `C_INSTRUCTION`, `LABEL`) e expõe seus componentes (`symbol()`, `dest()`, `comp()`, `jump()`). Também permite `reset()` para reiniciar a leitura na segunda passagem;

* **SymbolTable:** funciona como dicionário de símbolos. É inicializada com os predefinidos da arquitetura Hack (`R0`–`R15`, `SP`, `LCL`, `ARG`, `THIS`, `THAT`, `SCREEN`, `KBD`). Na primeira passagem recebe os labels via `addEntry()`; na segunda, aloca variáveis novas via `addVariable()`, sempre a partir de `RAM[16]`;

* **Code:** não faz parsing nem leitura de arquivo — apenas traduz os mnemônicos `dest`, `comp` e `jump` para seus respectivos bits, usando tabelas de mapeamento fixas (`HashMap`);

* **Assembler:** é o ponto de entrada. Recebe o caminho do `.asm`, executa a primeira passagem (coleta de labels), reseta o Parser, executa a segunda passagem (resolve variáveis e gera o binário) e escreve o arquivo `.hack` resultante.

### Exemplo

A instrução:

```asm
@2
D=A
```

É processada assim: o Parser classifica `@2` como `A_INSTRUCTION` com `symbol() = "2"`. Como é numérico, o Assembler usa o valor direto e escreve `0000000000000010`. Em seguida, `D=A` é `C_INSTRUCTION` com `dest() = "D"` e `comp() = "A"`; o Code traduz para `111` + `0110000` (comp de `A`) + `010` (dest de `D`) + `000` (sem jump), gerando `1110110000010000`.

---

## 🚀 Como executar

### Pré-requisitos

* Java 17+
* Maven 3.6+

### 🔹 Build

```bash
mvn clean package
```

Gera o executável em `target/assembler.jar`.

---

### 🔹 Traduzir um arquivo `.asm` para código de máquina

```bash
java -jar target/assembler.jar <caminho-do-arquivo.asm>
```

O arquivo `.hack` é gerado no mesmo diretório do `.asm` (ex: `Max.asm` → `Max.hack`).

---

### 🔹 Rodar os testes do Project 06

**Add** — soma R0 + R1

```bash
java -jar target/assembler.jar src/test/resources/6/add/Add.asm
```

**Max** — máximo entre R0 e R1 (com labels)

```bash
java -jar target/assembler.jar src/test/resources/6/max/Max.asm
```

**Max (sem símbolos)** — versão de referência, usada para validar a resolução de labels

```bash
java -jar target/assembler.jar src/test/resources/6/max/MaxL.asm
```

**Rect** — desenha um retângulo na tela

```bash
java -jar target/assembler.jar src/test/resources/6/rect/Rect.asm
```

**Pong** — jogo completo (teste avançado, ~27 mil linhas geradas)

```bash
java -jar target/assembler.jar src/test/resources/6/pong/Pong.asm
```

Para validar no CPU Emulator, abra o `.hack` gerado (ou o `.tst` correspondente, quando disponível) em `File → Load Program` / `Load Script`, execute e compare o resultado esperado.

---

## ✅ Status de validação — Project 06

Todos os programas de teste foram traduzidos com sucesso pelo montador:

| Programa | Foco | Resultado |
| --- | --- | --- |
| `add/Add.asm` | A e C-instructions básicas, sem símbolos | ✅ Passou |
| `max/Max.asm` | Labels (`ITSR0`, `OUTPUT_D`, `END`) e símbolos predefinidos (`R0`–`R2`) | ✅ Passou |
| `max/MaxL.asm` | Versão sem símbolos, usada como gabarito de comparação | ✅ Passou |
| `rect/Rect.asm` / `RectL.asm` | Desenho na tela (`SCREEN`), loops e variáveis | ✅ Passou |
| `pong/Pong.asm` / `PongL.asm` | Programa completo, muitas labels e variáveis | ✅ Passou |

A tradução de `Max.asm` foi comparada byte a byte com `MaxL.asm` (sua versão sem símbolos) e o resultado é **idêntico**, confirmando a resolução correta de labels e variáveis. O mesmo procedimento foi aplicado a `Rect`/`RectL` e `Pong`/`PongL`.

> 💡 Recomenda-se ainda carregar os `.hack` gerados no CPU Emulator oficial para validação visual (especialmente `Rect` e `Pong`, que têm efeito na tela).

---

## 📌 Observações

* O montador aceita apenas um arquivo `.asm` por execução (não há modo de diretório, já que o Project 06 trabalha com um programa por arquivo);
* O arquivo `.hack` gerado aparece no mesmo diretório do `.asm` de origem;
* Símbolos são resolvidos em **duas passagens**: a primeira mapeia apenas labels (`(LABEL)`) para o endereço da próxima instrução; a segunda resolve variáveis não predefinidas, alocando-as sequencialmente a partir de `RAM[16]`;
* Os arquivos `.hack` não são versionados no repositório (estão no `.gitignore`), pois são saídas geradas em tempo de execução — apenas os `.asm` de teste em `src/test/resources/` são versionados;

---

## 🎥 Vídeo de Apresentação

🔗https://drive.google.com/drive/folders/1ODN_dncm8fF-MABNcNIFfyX9GwQE7O6t?usp=sharing

---

## 👥 Créditos

**Aluna:** Maria Laura Rangel Urbano Cronemberger  
**Matrícula:** 20250071287  
**Disciplina:** EECP0026 — Compiladores  
**Professor:** Prof. Dr. Sergio Souza Costa  
**Instituição:** UFMA — Universidade Federal do Maranhão  
**Semestre:** 2026.1  

---

<div align="center">

**Este repositório possui fins acadêmicos.**

</div>

---
