package br.com.code;

import java.util.HashMap;
import java.util.Map;

/**
 * Code — responsável por traduzir os campos de uma instrução C
 * para seus respectivos códigos binários.
 *
 * Não realiza parsing nem leitura de arquivos.
 * Apenas converte:
 *   dest -> 3 bits
 *   comp -> 7 bits
 *   jump -> 3 bits
 */
public class Code {

    private static final Map<String, String> DEST = new HashMap<>();
    private static final Map<String, String> COMP = new HashMap<>();
    private static final Map<String, String> JUMP = new HashMap<>();

    static {

        // ---------- DEST ----------

        DEST.put(null,  "000");
        DEST.put("M",   "001");
        DEST.put("D",   "010");
        DEST.put("MD",  "011");
        DEST.put("A",   "100");
        DEST.put("AM",  "101");
        DEST.put("AD",  "110");
        DEST.put("AMD", "111");


        // ---------- JUMP ----------

        JUMP.put(null,  "000");
        JUMP.put("JGT", "001");
        JUMP.put("JEQ", "010");
        JUMP.put("JGE", "011");
        JUMP.put("JLT", "100");
        JUMP.put("JNE", "101");
        JUMP.put("JLE", "110");
        JUMP.put("JMP", "111");


        // ---------- COMP (a = 0) ----------

        COMP.put("0",   "0101010");
        COMP.put("1",   "0111111");
        COMP.put("-1",  "0111010");

        COMP.put("D",   "0001100");
        COMP.put("A",   "0110000");

        COMP.put("!D",  "0001101");
        COMP.put("!A",  "0110001");

        COMP.put("-D",  "0001111");
        COMP.put("-A",  "0110011");

        COMP.put("D+1", "0011111");
        COMP.put("A+1", "0110111");

        COMP.put("D-1", "0001110");
        COMP.put("A-1", "0110010");

        COMP.put("D+A", "0000010");
        COMP.put("D-A", "0010011");
        COMP.put("A-D", "0000111");

        COMP.put("D&A", "0000000");
        COMP.put("D|A", "0010101");


        // ---------- COMP (a = 1) ----------

        COMP.put("M",   "1110000");
        COMP.put("!M",  "1110001");
        COMP.put("-M",  "1110011");

        COMP.put("M+1", "1110111");
        COMP.put("M-1", "1110010");

        COMP.put("D+M", "1000010");
        COMP.put("D-M", "1010011");
        COMP.put("M-D", "1000111");

        COMP.put("D&M", "1000000");
        COMP.put("D|M", "1010101");
    }

    /**
     * Retorna os 3 bits correspondentes ao campo dest.
     */
    public static String dest(String mnemonic) {
        return DEST.get(mnemonic);
    }

    /**
     * Retorna os 7 bits correspondentes ao campo comp.
     */
    public static String comp(String mnemonic) {
        return COMP.get(mnemonic);
    }

    /**
     * Retorna os 3 bits correspondentes ao campo jump.
     */
    public static String jump(String mnemonic) {
        return JUMP.get(mnemonic);
    }

}