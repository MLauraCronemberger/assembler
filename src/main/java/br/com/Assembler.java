package br.com;

import br.com.code.Code;
import br.com.parser.Parser;
import br.com.parser.Parser.InstructionType;
import br.com.symboltable.SymbolTable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Assembler {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.out.println("Uso: java Assembler arquivo.asm");
            return;
        }

        Path input = Path.of(args[0]);
        Path output = Path.of(
                input.toString().replace(".asm", ".hack")
        );

        Parser parser = new Parser(input);
        SymbolTable symbolTable = new SymbolTable();

        // ============================
        // PRIMEIRA PASSAGEM
        // ============================

        int romAddress = 0;

        while (parser.hasMoreInstructions()) {

            parser.advance();

            if (parser.instructionType() == InstructionType.LABEL) {

                String label = parser.symbol();

                if (!symbolTable.contains(label)) {
                    symbolTable.addEntry(label, romAddress);
                }

            } else {
                romAddress++;
            }
        }

        // volta para o início do arquivo
        parser.reset();

        // ============================
        // SEGUNDA PASSAGEM
        // ============================

        try (BufferedWriter writer = Files.newBufferedWriter(output)) {

            while (parser.hasMoreInstructions()) {

                parser.advance();

                switch (parser.instructionType()) {

                    case LABEL:
                        // labels não geram código
                        break;

                    case A_INSTRUCTION:

                        String symbol = parser.symbol();
                        int value;

                        if (symbol.matches("\\d+")) {

                            value = Integer.parseInt(symbol);

                        } else {

                            if (!symbolTable.contains(symbol)) {
                                symbolTable.addVariable(symbol);
                            }

                            value = symbolTable.getAddress(symbol);
                        }

                        writer.write(
                                "0" + String.format("%15s",
                                        Integer.toBinaryString(value))
                                        .replace(' ', '0')
                        );

                        writer.newLine();
                        break;

                    case C_INSTRUCTION:

                        String binary =
                                "111"
                                        + Code.comp(parser.comp())
                                        + Code.dest(parser.dest())
                                        + Code.jump(parser.jump());

                        writer.write(binary);
                        writer.newLine();
                        break;
                }
            }
        }

        System.out.println("Arquivo gerado: " + output);
    }
}