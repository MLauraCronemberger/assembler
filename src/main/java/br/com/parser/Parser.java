package br.com.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser — lê um arquivo .asm linha por linha e expõe cada instrução.
 *
 * Responsabilidades:
 *   - Ignorar comentários (//) e linhas vazias
 *   - Classificar cada instrução: A_INSTRUCTION, C_INSTRUCTION ou LABEL
 *   - Expor symbol(), dest(), comp() e jump() de cada instrução
 */
public class Parser {

    public enum InstructionType {
        A_INSTRUCTION,  // @valor ou @simbolo
        C_INSTRUCTION,  // dest=comp;jump
        LABEL           // (SIMBOLO)
    }

    private final List<String> instructions;
    private int current = -1;
    private String currentInstruction;

    public Parser(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);

        this.instructions = lines.stream()
            .map(line -> {
                int commentIndex = line.indexOf("//");
                return commentIndex >= 0 ? line.substring(0, commentIndex) : line;
            })
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toList());
    }

    public boolean hasMoreInstructions() {
        return current + 1 < instructions.size();
    }

    public void advance() {
        current++;
        currentInstruction = instructions.get(current);
    }

    public void reset() {
        current = -1;
        currentInstruction = null;
    }

    /**
     * Classifica a instrução atual:
     *   @valor        → A_INSTRUCTION
     *   (SIMBOLO)     → LABEL
     *   dest=comp;jump → C_INSTRUCTION
     */
    public InstructionType instructionType() {
        if (currentInstruction.startsWith("@")) return InstructionType.A_INSTRUCTION;
        if (currentInstruction.startsWith("(")) return InstructionType.LABEL;
        return InstructionType.C_INSTRUCTION;
    }

    /**
     * Para A_INSTRUCTION: retorna o valor ou símbolo após o @
     *   "@7"     → "7"
     *   "@LOOP"  → "LOOP"
     *
     * Para LABEL: retorna o nome do símbolo sem parênteses
     *   "(LOOP)" → "LOOP"
     */
    public String symbol() {
        if (instructionType() == InstructionType.A_INSTRUCTION) {
            return currentInstruction.substring(1);
        }
        // LABEL: remove '(' e ')'
        return currentInstruction.substring(1, currentInstruction.length() - 1);
    }

    /**
     * Para C_INSTRUCTION: retorna a parte dest (antes do '=').
     * Se não tiver '=', retorna null.
     *   "D=M+1"  → "D"
     *   "0;JMP"  → null
     */
    public String dest() {
        if (currentInstruction.contains("=")) {
            return currentInstruction.split("=")[0];
        }
        return null;
    }

    /**
     * Para C_INSTRUCTION: retorna a parte comp (entre '=' e ';').
     *   "D=M+1"    → "M+1"
     *   "D=M+1;JGT"→ "M+1"
     *   "0;JMP"    → "0"
     */
    public String comp() {
        String instruction = currentInstruction;

        // remove o dest se existir
        if (instruction.contains("=")) {
            instruction = instruction.split("=")[1];
        }

        // remove o jump se existir
        if (instruction.contains(";")) {
            instruction = instruction.split(";")[0];
        }

        return instruction;
    }

    /**
     * Para C_INSTRUCTION: retorna a parte jump (após o ';').
     * Se não tiver ';', retorna null.
     *   "D;JGT"  → "JGT"
     *   "D=M"    → null
     */
    public String jump() {
        if (currentInstruction.contains(";")) {
            return currentInstruction.split(";")[1];
        }
        return null;
    }
}