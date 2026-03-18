package com.jbank.exceptions;

public class SaldoInsuficinteException extends Exception {
    public SaldoInsuficinteException( double valor ) {
        super("Saldo insuficiente para usar R$" + valor);
    }
}
