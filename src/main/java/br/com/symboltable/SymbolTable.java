package br.com.symboltable;

import java.util.HashMap;
import java.util.Map;

/**
 * SymbolTable — mapeia símbolos (labels e variáveis) para endereços de memória.
 *
 * Inicializada com os símbolos predefinidos da arquitetura Hack.
 * Durante a primeira passagem, labels são adicionados com seu endereço de instrução.
 * Durante a segunda passagem, variáveis novas recebem endereços a partir de RAM[16].
 */
public class SymbolTable {

    private final Map<String, Integer> table;
    private int nextVariableAddress = 16; // variáveis começam em RAM[16]

    public SymbolTable() {
        table = new HashMap<>();

        // registradores de propósito geral
        for (int i = 0; i <= 15; i++) {
            table.put("R" + i, i);
        }

        // ponteiros da VM
        table.put("SP",   0);
        table.put("LCL",  1);
        table.put("ARG",  2);
        table.put("THIS", 3);
        table.put("THAT", 4);

        // I/O memory-mapped
        table.put("SCREEN", 16384);
        table.put("KBD",    24576);
    }

    /**
     * Adiciona um símbolo com um endereço específico.
     * Usado na primeira passagem para registrar labels.
     *
     * Exemplo: (LOOP) na linha 10 → addEntry("LOOP", 10)
     */
    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
    }

    /**
     * Verifica se um símbolo já está na tabela.
     */
    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    /**
     * Retorna o endereço de um símbolo já cadastrado.
     */
    public int getAddress(String symbol) {
        return table.get(symbol);
    }

    /**
     * Adiciona uma nova variável e atribui o próximo endereço disponível.
     * Usado na segunda passagem para variáveis não declaradas.
     *
     * Exemplo: @contador (não visto antes) → RAM[16], RAM[17], ...
     */
    public int addVariable(String symbol) {
        table.put(symbol, nextVariableAddress);
        return nextVariableAddress++;
    }
}