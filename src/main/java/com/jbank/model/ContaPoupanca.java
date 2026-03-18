package com.jbank.model;

import com.jbank.exceptions.ValorInvalidoException;

public class ContaPoupanca extends Conta {
    public ContaPoupanca( String titular, int numero, double saldo ) {
        super( titular, numero, saldo );
    }

    private void renderJuros ( double taxa ) {
        try {
            this.depositar( this.getSaldo() * taxa );

        } catch ( ValorInvalidoException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

}
