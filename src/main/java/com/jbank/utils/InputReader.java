package com.jbank.utils;

import java.util.Scanner;

public class InputReader {
    private static final Scanner scanner = new Scanner( System.in);

    public static int lerInt() {
        // System.out.println("[DEBUG] Entrei no lerInt"); // Pode manter para testar
        while ( true ) {
            try {
                 // Evita erro se o fluxo fechar

                String entrada = scanner.next();

                // Se for vazio, o loop continua AUTOMATICAMENTE até achar um texto
                if ( entrada.isEmpty() ) continue;

                if ( entrada.equalsIgnoreCase("C") ) return -1;

                return Integer.parseInt( entrada );

            } catch ( NumberFormatException e ) {
                System.out.println("❌ ERRO: Digite apenas números.");
                System.out.print("Tente novamente [C para Cancelar]: ");
            }
        }
    }


    public static String lerString() { return scanner.nextLine(); }

    public static double lerDouble() {

        while ( true ) {
            try {
                // Evita erro se o fluxo fechar

                String entrada = scanner.next();

                // Se for vazio, o loop continua AUTOMATICAMENTE até achar um texto
                if ( entrada.isEmpty() ) continue;

                if ( entrada.equalsIgnoreCase("C") ) return 0;

                return Double.parseDouble( entrada );

            } catch ( NumberFormatException e ) {
                System.out.println("❌ ERRO: Digite apenas números.");
                System.out.print("Tente novamente [C para Cancelar]: ");

            }
        }
    }
}
